//
//  SecondViewController.m
//  PushCoin
//
//  Created by Gilbert Cheung on 4/20/12.
//  Copyright (c) 2012 PushCoin. All rights reserved.
//

#import "ReceiveNavigationController.h"
#import "ReceiveController.h"
#import "PushCoinMessages.h"
#import "AppDelegate.h"
#import "NSString+HexStringToBytes.h"
#import "NSData+BytesToHexString.h"
#import "NSData+Base64.h"

@implementation ReceiveController
@synthesize paymentTextField;
@synthesize ptaData;

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    NSLocale *usLocale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US"];
    
    numberFormatter = [[NSNumberFormatter alloc] init];
    [numberFormatter setFormatterBehavior:NSNumberFormatterBehavior10_4];
    [numberFormatter setCurrencySymbol:@"$"];
    [numberFormatter setNumberStyle:NSNumberFormatterCurrencyStyle];
    [numberFormatter setLocale:usLocale];

    webService = [[PushCoinWebService alloc] initWithDelegate:self];
    buffer =  [[NSMutableData alloc] initWithLength:PushCoinWebServiceOutBufferSize];
    parser = [[PushCoinMessageParser alloc] init];

    storedValue = [NSMutableString stringWithString:@""];
    self.paymentTextField.delegate = self;
    self.paymentTextField.keyboardType = UIKeyboardTypeNumberPad;

    [self processData];
}

- (void)viewDidUnload
{
    [self setPaymentTextField:nil];
    [super viewDidUnload];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated
{
	[super viewWillDisappear:animated];
}

- (void)viewDidDisappear:(BOOL)animated
{
	[super viewDidDisappear:animated];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return (interfaceOrientation != UIInterfaceOrientationPortraitUpsideDown);
}

- (AppDelegate *)appDelegate
{
    return (AppDelegate *)[[UIApplication sharedApplication] delegate];
}

- (CLLocation *) lastKnownLocation
{
    return [(ReceiveNavigationController *)self.navigationController lastKnownLocation];
}

- (IBAction)backgroundTouched:(id)sender 
{
    [self.paymentTextField resignFirstResponder];
}

- (IBAction)submitButtonTapped:(id)sender 
{
    [self.paymentTextField resignFirstResponder];
    
    NSDate * now = [NSDate date];
    
    // Create transfer request
    TransferRequestMessage * msgOut = [[TransferRequestMessage alloc] init];
    PCOSRawData * dataOut = [[PCOSRawData alloc] initWithData:buffer];
    
    msgOut.block.mat.data = self.appDelegate.authToken.hexStringToBytes;
    msgOut.block.ref_data.string=@"";
    msgOut.block.utc_ctime.val = (SInt64)[now timeIntervalSince1970];
    
    msgOut.block.transfer.value.val = [storedValue intValue];
    msgOut.block.transfer.scale.val = -2;
    
    msgOut.block.currency.string = @"USD";
    msgOut.block.note.string = @"";
    
    if (self.lastKnownLocation != nil)
    {
        GeoLocation * location = [[GeoLocation alloc] init];
        location.latitude.val = self.lastKnownLocation.coordinate.latitude;
        location.longitude.val = self.lastKnownLocation.coordinate.longitude;
        [msgOut.block.geolocation.val addObject:location];
    }

    msgOut.pta_block.data = self.ptaData;
    
    [parser encodeMessage:msgOut to:dataOut];
    [webService sendMessage:dataOut.consumedData];
}

- (void) processData
{
    [parser decode:self.ptaData toReceiver:self];
}


#pragma mark PushCoinWebserviceDelegate

- (void)webService:(PushCoinWebService *)webService didReceiveMessage:(NSData *)data
{
    [parser decode:data toReceiver:self];
}

- (void)webService:(PushCoinWebService *)webService didFailWithStatusCode:(NSInteger)statusCode 
    andDescription:(NSString *)description
{
    [self.appDelegate showAlert:description
                      withTitle:[NSString stringWithFormat:@"HTTP Error - %d", statusCode]];
}

#pragma mark PushCoinMessageParserDelegate

-(void) didDecodeErrorMessage:(ErrorMessage *)msg withHeader:(PCOSHeaderBlock*)hdr
{
    [self.appDelegate handleErrorMessage:msg withHeader:hdr];
}

-(void) didDecodeUnknownMessage:(PCOSMessage *)msg withHeader:(PCOSHeaderBlock*)hdr
{
    [self.appDelegate handleUnknownMessage:msg withHeader:hdr];
}

-(void) didDecodeSuccessMessage:(SuccessMessage *)msg withHeader:(PCOSHeaderBlock*)hdr
{
    [self.appDelegate showAlert:@"Success!" 
                      withTitle:@"Success"];
}

-(void) didDecodePaymentTransferAuthorizationMessage:(PaymentTransferAuthorizationMessage *)msg withHeader:(PCOSHeaderBlock *)hdr
{
    Amount * amountField = msg.pub_block.payment_limit;
    Float32 amount = amountField.value.val * pow(10, (float) amountField.scale.val);
    self.paymentTextField.text = [NSString stringWithFormat:@"$ %.2f", amount];
    storedValue = [NSMutableString stringWithFormat:@"%d", (int) (amount * 100)];
}


#pragma mark UITextFieldDelegate

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string
{
    if (range.length > 0)
    {
        if (storedValue.length > 0)
            [storedValue replaceCharactersInRange:NSMakeRange([storedValue length]-1, 1) withString:@""];
    }
    else
    {
        if (storedValue.length + string.length <= 5)
            [storedValue appendString:string];
    }
    
    double value = storedValue.doubleValue;
    if (value == 0)
        storedValue.string = @"";
    
    NSString *newAmount = [self formatCurrencyValue:(value/100)];
    [textField setText:[NSString stringWithFormat:@"%@",newAmount]];
    return NO;
}


- (BOOL)textFieldShouldClear:(UITextField *)textField
{
    textField.text = @"$0.00";
    storedValue.string = @"";
    return NO;
}


-(NSString*) formatCurrencyValue:(double)value
{
    NSNumber *c = [NSNumber numberWithFloat:value];
    return [numberFormatter stringFromNumber:c];
}

-(void)textFieldDidBeginEditing:(UITextField *)textField
{
    UIBarButtonItem * hideItem = [[UIBarButtonItem alloc] initWithTitle:@"Hide" style:UIBarButtonItemStyleBordered
                                                                   target:self action:@selector(hideButtonTapped:)];
    hideItem.tintColor = UIColorFromRGB(0xC84131);
    
    self.navigationItem.rightBarButtonItem = hideItem;
}

-(void)textFieldDidEndEditing:(UITextField *)textField
{
    self.navigationItem.rightBarButtonItem = nil;
}

-(void) hideButtonTapped:(id)sender
{
    [self.paymentTextField resignFirstResponder];
}


@end






































