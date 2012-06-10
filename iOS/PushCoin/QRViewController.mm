//
//  QRViewController.m
//  PushCoin
//
//  Created by Gilbert Cheung on 4/21/12.
//  Copyright (c) 2012 PushCoin. All rights reserved.
//

//#define AUTOBRIGHTNESS

#import "QRViewController.h"
#import "QREncoder.h"
#import "PaymentCell.h"
#import "AppDelegate.h"
#import "PushCoinAddressBook.h"
#import "NSString+HexStringToBytes.h"
#import "NSData+Base64.h"


@implementation QRViewController
@synthesize expiration;
@synthesize payment = payment_;
@synthesize receiver = receiver_;
@synthesize navigationBar;
@synthesize delegate;
@synthesize imageView;
@synthesize receiverLabel;
@synthesize expirationLabel;
@synthesize receiverBackground;
@synthesize actionBarButton;
@synthesize parser;
@synthesize buffer;
@synthesize passcode;
@synthesize amountLabel;
@synthesize centLabel;
@synthesize tipLabel;
@synthesize timer;
@synthesize ttl;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) 
    {
        sendEmailAfterReceiverSet = NO;
    }
    return self;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];

    self.buffer = [[NSMutableData alloc] initWithLength:PushCoinWebServiceOutBufferSize];
    self.parser = [[PushCoinMessageParser alloc] init];
    self.ttl = MAX(60, self.ttl);
    
    UISwipeGestureRecognizer * swipeRecognizer;
    swipeRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self   
                                                                action:@selector(handleSwipeDown:)];
    swipeRecognizer.direction = UISwipeGestureRecognizerDirectionDown;
    [self.view addGestureRecognizer:swipeRecognizer];
    
    swipeRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self   
                                                                action:@selector(handleSwipeUp:)];
    swipeRecognizer.direction = UISwipeGestureRecognizerDirectionUp;
    [self.view addGestureRecognizer:swipeRecognizer];
    
    [self prepareQR];    
}

-(NSData *)generatePTAWithCreationTime:(SInt64) ctime andExpirationTime:(SInt64) etime
{
    [self.appDelegate unlockDsaPrivateKeyWithPasscode:self.passcode];
    
    //set data
    PaymentTransferAuthorizationMessage * msgOut = [[PaymentTransferAuthorizationMessage alloc] init];
    PCOSRawData * dataOut = [[PCOSRawData alloc] initWithData:self.buffer];
    
    msgOut.prv_block.mat.data = self.appDelegate.authToken.hexStringToBytes;
    msgOut.prv_block.ref_data.string=@"";
    
    msgOut.pub_block.utc_ctime.val = ctime;
    msgOut.pub_block.utc_etime.val = etime;    
    msgOut.pub_block.payment_limit.value.val = self.payment.amountValue;
    msgOut.pub_block.payment_limit.scale.val = self.payment.amountScale;
    
    if (self.payment.tipValue != 0)
    {
        Gratuity * tip = [[Gratuity alloc] init];
        tip.type.val = 'P';
        tip.add.value.val = self.payment.tipValue;
        tip.add.scale.val = self.payment.tipScale;
        
        [msgOut.pub_block.tip.val addObject:tip];
    }
    
    msgOut.pub_block.currency.string = @"USD";
    msgOut.pub_block.keyid.data = [PushCoinRSAPublicKeyID hexStringToBytes];
    msgOut.pub_block.receiver.string = [self.receiver email] ? self.receiver.email : @"";
    msgOut.pub_block.note.string = @"";
    
    [self.parser encodeMessage:msgOut to:dataOut];
    return dataOut.consumedData;
}

- (UIImage *) generateQRWithData:(NSData *)data
{
    if (data.length)
    {
        int qrcodeImageDimension = self.imageView.frame.size.width;    
        
        DataMatrix *qrMatrix = [QREncoder encodeWithECLevel:QR_ECLEVEL_AUTO
                                                    version:QR_VERSION_AUTO
                                                      bytes:data];
        
        return [QREncoder renderDataMatrix:qrMatrix 
                            imageDimension:qrcodeImageDimension];
        
    }
    return nil;
}

- (void) prepareQR
{
    NSDate * now = [NSDate date];
    SInt64 ctime = (SInt64)[now timeIntervalSince1970];
    SInt64 etime = (SInt64)ctime + self.ttl;
    
    UIImage * qrcodeImage = [self generateQRWithData:[self generatePTAWithCreationTime:ctime 
                                                                     andExpirationTime:etime]];
    if (qrcodeImage)
    {
        self.expiration = etime;
        self.imageView.image = qrcodeImage;
        self.amountLabel.text = [NSString stringWithFormat:@"$%d", (int)self.payment.amount];
        self.centLabel.text = [NSString stringWithFormat:@"%02d", (int)(self.payment.amount * 100) % 100];
        
        if (self.payment.tip != 0)
        {
            self.tipLabel.text = [NSString stringWithFormat:@"+ %d%% tips", (int)(self.payment.tip * 100)];        
        }
        else
        {
            self.tipLabel.text = @"";
        }
        
        if (self.receiver)
        {
            [self.receiverBackground setHidden:NO];
            self.receiverLabel.text = [NSString stringWithFormat:@"%@ <%@>",
                                       self.receiver.name,
                                       self.receiver.email];
        }
        else 
        {
            [self.receiverBackground setHidden:YES];
            self.receiverLabel.text = @"";
        }
        
        if (self.timer == nil)
        {
            self.timer = [NSTimer scheduledTimerWithTimeInterval:1.0 target:self selector:@selector(timerDidTick:) userInfo:nil repeats:YES];
        }
    }
    else 
    {
        [self.delegate qrViewControllerDidClose:self];
    }
}

- (IBAction) handleSwipeDown:(UISwipeGestureRecognizer *) recognizer
{
    [self.delegate qrViewControllerDidClose:self];
}

- (IBAction) handleSwipeUp:(UISwipeGestureRecognizer *) recognizer
{
    [self showActionSheet];
}

- (void) timerDidTick:(id) sender
{
    if (self.expiration)
    {
        NSTimeInterval timeDiff = self.expiration - [[[NSDate alloc] init] timeIntervalSince1970];
        
        if (timeDiff >= 0)
        {
            self.expirationLabel.text = [NSString stringWithFormat:@"coupon expires in %02d:%02d:%02d",
                                         (int) ((double)timeDiff / 60 / 60),
                                         ((int) ((double)timeDiff / 60)) % 60,
                                         ((int)timeDiff % 60)];
            
            if (timeDiff <= 10)
                self.expirationLabel.backgroundColor = [UIColor orangeColor];  
            else
                self.expirationLabel.backgroundColor = nil;
        }
        else
        {
            self.expirationLabel.backgroundColor = [UIColor redColor];
            self.expirationLabel.text = @"coupon expired - please renew";
            
            [self.timer invalidate];
            self.timer = nil;
        }
    }
    else 
    {
        self.expirationLabel.text = @"coupon has no expiration";
        self.expirationLabel.backgroundColor = nil;        
        
        [self.timer invalidate];
        self.timer = nil;
    }
    
}

- (void)viewDidAppear:(BOOL)animated
{
    
#ifdef AUTOBRIGHTNESS    
    savedBrightness = [[UIScreen mainScreen] brightness];
    if (savedBrightness < 0.5f)
    {
        [[UIScreen mainScreen] setBrightness:0.5f];
    }    
#endif
    
}

- (void)viewDidDisappear:(BOOL)animated
{
    
#ifdef AUTOBRIGHTNESS
    if (savedBrightness != [[UIScreen mainScreen] brightness])
    {
        [[UIScreen mainScreen] setBrightness:savedBrightness];
    }
#endif
    
}

- (void)viewDidUnload
{
    [self setImageView:nil];
    [self setNavigationBar:nil];
    [self setAmountLabel:nil];
    [self setCentLabel:nil];
    [self setTipLabel:nil];
    [self setReceiverLabel:nil];
    [self setExpirationLabel:nil];
    
    if (self.timer)
        [self.timer invalidate];
    [self setTimer:nil];
    
    [self setReceiverBackground:nil];
    [self setActionBarButton:nil];
    [super viewDidUnload];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

- (AppDelegate *)appDelegate
{
    return (AppDelegate *)[[UIApplication sharedApplication] delegate];
}

- (IBAction)closeButtonTapped:(id)sender 
{
    [self.delegate qrViewControllerDidClose:self];
}

- (IBAction)actionButtonTapped:(id)sender 
{
    [self showActionSheet];
}

- (void) showActionSheet
{
    UIActionSheet * sheet = [[UIActionSheet alloc] initWithTitle:@"" delegate:self cancelButtonTitle:@"Cancel"destructiveButtonTitle:nil otherButtonTitles:@"Renew Coupon", @"Add Tips", @"Set Receiver", @"Clear Receiver", @"Email Coupon", nil];
    
    [sheet showFromBarButtonItem:self.actionBarButton animated:YES];
}

-(void)actionSheetCancel:(UIActionSheet *)actionSheet
{
    
}

-(void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
    switch(buttonIndex)
    {
        case 0:
        {
            //renew coupon
            [self prepareQR];
            break;
        }
        case 1:
        {
            //add tips
            [self showPaymentDetails];
            break;            
        }
        case 2:
        {
            //set receiver
            [self showSelectReceiver];
            break;
        }
        case 3:
        {
            //clear receiver
            self.receiver = nil;
            [self prepareQR];
            break;
        }
        case 4:
        {
            // email coupon
            [self emailCoupon];
            break;
        }
    }
}

-(void) emailCoupon
{
    if (!self.receiver || !self.receiver.email || !self.receiver.email.length)
    {
        sendEmailAfterReceiverSet = YES;
        [self showSelectReceiver];
    }
    else 
    {
        [self doEmailCoupon];
    }
}

-(void) doEmailCoupon
{
    if (!self.receiver || !self.receiver.email || !self.receiver.email.length)
    {
        [self.appDelegate showAlert:@"For security reasons, please set a receiver before email."
                          withTitle:@"Security Alert"];
        return;
    }
    
    // reset flag
    sendEmailAfterReceiverSet = NO;
    
    NSDate * now = [NSDate date];
    SInt64 ctime = (SInt64)[now timeIntervalSince1970];
    SInt64 etime = (SInt64)ctime + 24 * 60 * 60; // 24 hours

    NSData * ptaData = [self generatePTAWithCreationTime:ctime andExpirationTime:etime];
    UIImage * ptaImage = [self generateQRWithData:ptaData];
    NSData *imageData = [NSData dataWithData:UIImagePNGRepresentation(ptaImage)];

    NSString *filePath = [[NSBundle mainBundle] pathForResource:@"email_template" ofType:@"html"];  
    NSMutableString * emailBody = [[NSMutableString alloc] initWithContentsOfFile:filePath
                                                                         encoding:NSUTF8StringEncoding 
                                                                            error:nil];
    
    NSDateFormatter * dateFormatter = [[NSDateFormatter alloc] init];
    
    [dateFormatter setDateFormat:@"MM/dd/yyyy hh:mm:ss a"];
    NSString * expirationString = [dateFormatter stringFromDate:[NSDate dateWithTimeIntervalSince1970:etime]];
    
    [emailBody replaceOccurrencesOfString:@"<%%EXPIRATION%%>"
                               withString:expirationString 
                                  options:NSCaseInsensitiveSearch 
                                    range:NSMakeRange(0, emailBody.length)];
    
    [emailBody replaceOccurrencesOfString:@"<%%AMOUNT%%>"
                               withString:[NSString stringWithFormat:@"$%.2f", self.payment.amount]
                                  options:NSCaseInsensitiveSearch 
                                    range:NSMakeRange(0, emailBody.length)];
    
    [emailBody replaceOccurrencesOfString:@"<%%NOTE%%>"
                               withString:@""
                                  options:NSCaseInsensitiveSearch 
                                    range:NSMakeRange(0, emailBody.length)];
    
    [dateFormatter setDateFormat:@"'PushCoin'-yyyyMMdd-HHmmss"];
    NSString * attachmentNameString = [dateFormatter stringFromDate:[NSDate dateWithTimeIntervalSince1970:etime]];
        
    //Create the mail composer window with all attachments
    MFMailComposeViewController *controller = [[MFMailComposeViewController alloc] init];
    controller.mailComposeDelegate = self;
    [controller setSubject:[NSString stringWithFormat:@"You received $%.2f via PushCoin", self.payment.amount]];
    [controller setToRecipients:[NSArray arrayWithObject:[NSString stringWithFormat:@"%@ <%@>", self.receiver.name, self.receiver.email]]];
    [controller setMessageBody:emailBody isHTML:YES];
    [controller addAttachmentData:imageData mimeType:@"image/png" fileName:[NSString stringWithFormat:@"%@.png", attachmentNameString]];
    [controller addAttachmentData:ptaData mimeType:@"application/pcos" fileName:[NSString stringWithFormat:@"%@.pcos", attachmentNameString]];
       
    controller.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
    [self presentViewController:controller animated:YES completion:nil];
}

-(void) showSelectReceiver
{   
    SelectReceiverController * controller = [self.appDelegate viewControllerWithIdentifier:@"SelectReceiverController"];
    
    if (controller)
    {
        controller.allowAnyOne = YES;
        controller.delegate = self;
        controller.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
        [self presentViewController:controller animated:YES completion:nil];
    }
}

-(void) selectReceiverControllerDidCancel:(SelectReceiverController *)controller
{
    [self dismissViewControllerAnimated:YES completion:nil];
}


-(void) selectReceiverControllerDidClose:(SelectReceiverController *)controller
{
    self.receiver = [controller.receiver copy];
    [self prepareQR];
    
    if (sendEmailAfterReceiverSet)
    {
        [self dismissViewControllerAnimated:YES completion:^{
            [self doEmailCoupon];
        }];
    }
    else
    {
        [self dismissViewControllerAnimated:YES completion:nil];
    }
}

- (void) showPaymentDetails
{
    PaymentDetailsController * controller = [self.appDelegate viewControllerWithIdentifier:@"PaymentDetailsController"];
    
    if (controller)
    {
        controller.delegate = self;
        controller.payment = self.payment;
        controller.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
        [self presentModalViewController:controller animated:YES];
    }
}

-(void) paymentDetailsControllerDidCancel:(PaymentDetailsController *)controller
{
    [self dismissModalViewControllerAnimated:YES];
}


-(void) paymentDetailsControllerDidClose:(PaymentDetailsController *)controller
{
    [self dismissModalViewControllerAnimated:YES];
    self.payment = controller.payment;
    [self prepareQR];
}

-(void) mailComposeController:(MFMailComposeViewController *)controller didFinishWithResult:(MFMailComposeResult)result error:(NSError *)error
{
    [self dismissViewControllerAnimated:YES completion:^
    {
        if (result == MFMailComposeResultSent || result == MFMailComposeResultSent)
        {
            [self.delegate qrViewControllerDidClose:self];
        }
    }];
}

@end
