//
//  ThirdViewController.m
//  PushCoin
//
//  Created by Gilbert Cheung on 4/20/12.
//  Copyright (c) 2012 PushCoin. All rights reserved.
//

#import "SettingsController.h"
#import "AppDelegate.h"
#import "NSString+HexStringToBytes.h"
#import "NSData+BytesToHexString.h"
#import "NSData+Base64.h"

@implementation SettingsController
@synthesize unregisterButton;
@synthesize preAuthorizationTestButton;
@synthesize passcodeButton;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)didReceiveMemoryWarning
{
    
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    [unregisterButton setTitle:@"Unregister Device" forState:UIControlStateNormal];
    [unregisterButton setTitle:@"Unregister Device" forState:UIControlStateDisabled];
    
    webService = [[PushCoinWebService alloc] initWithDelegate:self];
    buffer =  [[NSMutableData alloc] initWithLength:PushCoinWebServiceOutBufferSize];
    parser = [[PushCoinMessageParser alloc] init];
    
    [self.unregisterButton setBackgroundImage:[[UIImage imageNamed:@"iphone_delete_button.png"]
                                           stretchableImageWithLeftCapWidth:8.0f
                                           topCapHeight:0.0f]
                                 forState:UIControlStateNormal];
    
    [self.unregisterButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    self.unregisterButton.titleLabel.font = [UIFont boldSystemFontOfSize:17];
    self.unregisterButton.titleLabel.shadowColor = [UIColor lightGrayColor];
    self.unregisterButton.titleLabel.shadowOffset = CGSizeMake(0, -1);
    
    [self updateRegisterButtonStatus];  
    [self updatePasscodeButtonStatus];
}

- (void) updateRegisterButtonStatus
{
    if (!self.appDelegate.registered)
    {
        unregisterButton.enabled = NO;
        preAuthorizationTestButton.enabled = NO;
    }
    else 
    {
        unregisterButton.enabled = YES;
        preAuthorizationTestButton.enabled = YES;
    }
}

-(void) updatePasscodeButtonStatus
{
    if (!self.appDelegate.hasPasscode)
    {
        [self.passcodeButton setTitle:@"Enable Passcode" forState:UIControlStateNormal];
    }
    else
    {
        [self.passcodeButton setTitle:@"Disable Passcode" forState:UIControlStateNormal];
    }
}
- (void)viewDidUnload
{
    [self setUnregisterButton:nil];
    [self setPreAuthorizationTestButton:nil];
    [self setPasscodeButton:nil];
    [super viewDidUnload];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

- (AppDelegate *)appDelegate
{
    return (AppDelegate *)[[UIApplication sharedApplication] delegate];
}
    
- (IBAction)unregister:(id)sender 
{
    UIAlertView * alert = [[UIAlertView alloc] initWithTitle:@"Unregistering device"
                                                     message:@"Are you sure?"
                                                    delegate:self 
                                           cancelButtonTitle:@"No" 
                                           otherButtonTitles:@"Yes", nil];
    [alert show];
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (buttonIndex == 1)
    {
        [self.appDelegate clearDevice];
        
        [self updateRegisterButtonStatus];
        [self updatePasscodeButtonStatus];
        
        [self.navigationController popToViewController:self animated:NO];
        [self.appDelegate requestRegistrationWithDelegate:self];
    }
}

-(void) registrationControllerDidClose:(RegistrationController *)controller
{
    [self updateRegisterButtonStatus];
}
- (void)webService:(PushCoinWebService *)webService didReceiveMessage:(NSData *)data
{
    [parser decode:data toReceiver:self];
}


- (void)webService:(PushCoinWebService *)webService didFailWithStatusCode:(NSInteger)statusCode 
    andDescription:(NSString *)description
{
    [[self appDelegate] showAlert:description
                        withTitle:[NSString stringWithFormat:@"Webservice Error - %d", statusCode]];
}

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
    [[self appDelegate] showAlert:@"Success"
                        withTitle:@"Success!"];
}

- (IBAction)preAuthorizationTest:(id)sender 
{
    if (self.appDelegate.hasPasscode)
        preAuthTestPasscodeController = [self.appDelegate requestPasscodeWithDelegate:self 
                                                                 navigationController:self.navigationController];
    else
        [self doPreAuthorizationTestWithPasscode:nil];
}

-(void) doPreAuthorizationTestWithPasscode:(NSString*) passcode
{    
    [self.appDelegate unlockDsaPrivateKeyWithPasscode:passcode];
    
    NSDate * now = [NSDate date];

    //set data
    PaymentTransferAuthorizationMessage * msgOut = [[PaymentTransferAuthorizationMessage alloc] init];
    PCOSRawData * dataOut = [[PCOSRawData alloc] initWithData:buffer];
    
    msgOut.prv_block.mat.data = self.appDelegate.authToken.hexStringToBytes;
    msgOut.prv_block.ref_data.string=@"";
    
    msgOut.pub_block.utc_ctime.val = (SInt64)[now timeIntervalSince1970];
    msgOut.pub_block.utc_etime.val = (SInt64)[now timeIntervalSince1970] + 60; /* exp in 1 min */
    
    msgOut.pub_block.payment_limit.value.val = 399;
    msgOut.pub_block.payment_limit.scale.val = -2;
    
    msgOut.pub_block.currency.string = @"USD";
    msgOut.pub_block.keyid.data = [PushCoinRSAPublicKeyID hexStringToBytes];
    msgOut.pub_block.receiver.string = @"";
    msgOut.pub_block.note.string = @"";
    
    [parser encodeMessage:msgOut to:dataOut];
    NSData * encodedData = dataOut.consumedData;
    
    
    // Create Preauth request
    PreauthorizationRequestMessage * msgOut2 = [[PreauthorizationRequestMessage alloc] init];
    PCOSRawData * dataOut2 = [[PCOSRawData alloc] initWithData:buffer];
    
    msgOut2.block.mat.data = self.appDelegate.authToken.hexStringToBytes;
    msgOut2.block.preauthorization_amount.value.val = 200;
    msgOut2.block.preauthorization_amount.scale.val = -2;
    msgOut2.block.currency.string = @"USD";
    msgOut2.block.ref_data.string=@"";

    msgOut2.pta_block.data = encodedData;
    
    [parser encodeMessage:msgOut2 to:dataOut2];
    [webService sendMessage:dataOut2.consumedData];
}

- (IBAction)enablePasscode:(id)sender {
    
    setPasscodeController = [[KKPasscodeViewController alloc] init];
    setPasscodeController.delegate = self;
    setPasscodeController.passcodeLockOn = self.appDelegate.hasPasscode;
    setPasscodeController.passcode = @"";
    setPasscodeController.eraseData = NO;
    setPasscodeController.mode = setPasscodeController.passcodeLockOn ? KKPasscodeModeDisabled : KKPasscodeModeSet;
    setPasscodeController.modalTransitionStyle = UIModalTransitionStyleCoverVertical;
    
    [self.navigationController pushViewController:setPasscodeController animated:YES];
}


- (BOOL)validatePasscode:(NSString *)passcode
{
    return [self.appDelegate validatePasscode:passcode];
}

- (void)didSettingsChanged:(KKPasscodeViewController *)viewController
{
    if (viewController == setPasscodeController)
    {
        [self.navigationController popToViewController:self animated:YES];

        if (viewController.passcodeLockOn)
            [self.appDelegate setPasscode:viewController.passcode oldPasscode:@""];
        else
            [self.appDelegate setPasscode:@"" oldPasscode:viewController.passcode];
    
        [self updatePasscodeButtonStatus];
    }
}

-(void)didPasscodeCancel:(KKPasscodeViewController *)viewController
{
    [self.navigationController popToViewController:self animated:YES];
}

-(void)didPasscodeEnteredIncorrectly:(KKPasscodeViewController *)viewController
{
    [self.navigationController popToViewController:self animated:YES];
}

-(void)didPasscodeEnteredCorrectly:(KKPasscodeViewController *)viewController
{
    if (viewController == preAuthTestPasscodeController)
    {
        [self.navigationController popToViewController:self animated:YES];
        [self doPreAuthorizationTestWithPasscode:viewController.passcode];
    }
}
@end























