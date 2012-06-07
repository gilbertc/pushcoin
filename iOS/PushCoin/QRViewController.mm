//
//  QRViewController.m
//  PushCoin
//
//  Created by Gilbert Cheung on 4/21/12.
//  Copyright (c) 2012 PushCoin. All rights reserved.
//

#import "QRViewController.h"
#import "QREncoder.h"
#import "PaymentCell.h"
#import "AppDelegate.h"
#import "NSString+HexStringToBytes.h"

@implementation QRViewController
@synthesize payment = payment_;
@synthesize receiver = receiver_;
@synthesize navigationBar;
@synthesize delegate;
@synthesize imageView;
@synthesize parser;
@synthesize buffer;
@synthesize passcode;
@synthesize amountLabel;
@synthesize centLabel;
@synthesize tipLabel;
@synthesize toolbar;
@synthesize receiverLabel;


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
    
    UISwipeGestureRecognizer * swipeRecognizer = 
        [[UISwipeGestureRecognizer alloc] initWithTarget:self   
                                                  action:@selector(handleSwipe:)];
    swipeRecognizer.direction = UISwipeGestureRecognizerDirectionDown;
    [self.view addGestureRecognizer:swipeRecognizer];
    
    [self prepareQR];
}

- (void) prepareQR
{
    NSDate * now = [NSDate date];
    
    [self.appDelegate unlockDsaPrivateKeyWithPasscode:self.passcode];
    
    //set data
    PaymentTransferAuthorizationMessage * msgOut = [[PaymentTransferAuthorizationMessage alloc] init];
    PCOSRawData * dataOut = [[PCOSRawData alloc] initWithData:self.buffer];
    
    msgOut.prv_block.mat.data = self.appDelegate.authToken.hexStringToBytes;
    msgOut.prv_block.ref_data.string=@"";
    
    msgOut.pub_block.utc_ctime.val = (SInt64)[now timeIntervalSince1970];
    msgOut.pub_block.utc_etime.val = (SInt64)[now timeIntervalSince1970] + 60;    
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
        self.amountLabel.text = [NSString stringWithFormat:@"%d", (int)self.payment.amount];
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
            self.receiverLabel.text = self.receiver.name;
        }
        else 
        {
            self.receiverLabel.text = @"Any";
        }
        
    }
    else 
    {
        [self.delegate qrViewControllerDidClose:self];
    }
}

- (IBAction) handleSwipe:(UISwipeGestureRecognizer *) recognizer
{
    [self.delegate qrViewControllerDidClose:self];
}

- (void)viewDidUnload
{
    [self setImageView:nil];
    [self setNavigationBar:nil];
    [self setAmountLabel:nil];
    [self setCentLabel:nil];
    [self setTipLabel:nil];
    [self setToolbar:nil];
    [self setReceiverLabel:nil];
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

- (IBAction)addTipsButtonTapped:(id)sender 
{
    [self showPaymentDetails];
}

- (IBAction)actionButtonTapped:(id)sender 
{
    UIActionSheet * sheet = [[UIActionSheet alloc] initWithTitle:@"PushCoin Coupon" delegate:self cancelButtonTitle:@"Cancel"destructiveButtonTitle:nil otherButtonTitles:@"Set Receiver", @"Email to Receiver", nil];
    
    [sheet showFromToolbar:self.toolbar];
}

-(void)actionSheetCancel:(UIActionSheet *)actionSheet
{
    
}

-(void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (buttonIndex == 0)
    {
        //set receiver
        [self showSelectReceiver];
    }
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
    [self prepareQR];

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

@end
