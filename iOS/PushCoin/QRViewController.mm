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
        // Custom initialization
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
    
    [self prepareQRWithTTL:self.ttl];    
}

- (void) prepareQRWithTTL:(SInt64) timeToLive
{
    NSDate * now = [NSDate date];
    
    [self.appDelegate unlockDsaPrivateKeyWithPasscode:self.passcode];
    
    //set data
    PaymentTransferAuthorizationMessage * msgOut = [[PaymentTransferAuthorizationMessage alloc] init];
    PCOSRawData * dataOut = [[PCOSRawData alloc] initWithData:self.buffer];
    
    msgOut.prv_block.mat.data = self.appDelegate.authToken.hexStringToBytes;
    msgOut.prv_block.ref_data.string=@"";
    
    msgOut.pub_block.utc_ctime.val = (SInt64)[now timeIntervalSince1970];
    msgOut.pub_block.utc_etime.val = (self.expiration = (SInt64)[now timeIntervalSince1970] + timeToLive);    
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
    NSData * data = dataOut.consumedData;
    
    if (data.length)
    {
        int qrcodeImageDimension = self.imageView.frame.size.width;    
        
        DataMatrix *qrMatrix = [QREncoder encodeWithECLevel:QR_ECLEVEL_AUTO
                                                    version:QR_VERSION_AUTO
                                                      bytes:data];
        
        UIImage *qrcodeImage = [QREncoder renderDataMatrix:qrMatrix 
                                            imageDimension:qrcodeImageDimension];
        
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
    UIActionSheet * sheet = [[UIActionSheet alloc] initWithTitle:@"" delegate:self cancelButtonTitle:@"Cancel"destructiveButtonTitle:nil otherButtonTitles:@"Renew Coupon", @"Add Tips", @"Set Receiver", @"Email Coupon", nil];
    
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
            [self prepareQRWithTTL:self.ttl];
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
        [self.appDelegate showAlert:@"For security reasons, please set a receiver before emailing." 
                          withTitle:@"Security Alert"];
        return;
    }
    
    NSMutableString *emailBody = [[NSMutableString alloc] 
                                  initWithString:@"Please accept this using PushCoin. The coupon will be expired in 24 hours."];

    [self prepareQRWithTTL:24*60*60]; //24 hours
    
    UIImage *emailImage = [self.imageView.image copy];
    NSData *imageData = [NSData dataWithData:UIImagePNGRepresentation(emailImage)];
   
    //Create the mail composer window
    MFMailComposeViewController *controller = [[MFMailComposeViewController alloc] init];
    controller.mailComposeDelegate = self;
    [controller setSubject:@"PushCoin Payment"];
    [controller setToRecipients:[NSArray arrayWithObject:[NSString stringWithFormat:@"%@ <%@>", self.receiver.name, self.receiver.email]]];
    [controller setMessageBody:emailBody isHTML:NO];
    [controller addAttachmentData:imageData mimeType:@"image/png" fileName:@"PushCoin.png"];
   
    [self presentViewController:controller animated:YES completion:nil];
}

-(void) showSelectReceiver
{   
    SelectReceiverController * controller = [self.appDelegate viewControllerWithIdentifier:@"SelectReceiverController"];
    
    if (controller)
    {
        controller.delegate = self;
        controller.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
        [self presentModalViewController:controller animated:YES];
    }
}

-(void) selectReceiverControllerDidCancel:(SelectReceiverController *)controller
{
    [self dismissModalViewControllerAnimated:YES];
}


-(void) selectReceiverControllerDidClose:(SelectReceiverController *)controller
{
    [self dismissModalViewControllerAnimated:YES];
    self.receiver = [controller.receiver copy];
    [self prepareQRWithTTL:self.ttl];
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
    [self prepareQRWithTTL:self.ttl];
}

-(void) mailComposeController:(MFMailComposeViewController *)controller didFinishWithResult:(MFMailComposeResult)result error:(NSError *)error
{
    [self dismissViewControllerAnimated:YES completion:^
    {
        if (result == MFMailComposeResultSent || result == MFMailComposeResultSent)
        {
            [self.delegate qrViewControllerDidClose:self];
        }
        else
        {
            [self prepareQRWithTTL:self.ttl];
        }
    }];
}

@end
