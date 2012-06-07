//
//  RegistrationController.m
//  PushCoin
//
//  Created by Gilbert Cheung on 5/8/12.
//  Copyright (c) 2012 PushCoin. All rights reserved.
//

#import "RegistrationController.h"
#import "AppDelegate.h"
#import "NSString+HexStringToBytes.h"
#import "NSData+BytesToHexString.h"
#import "NSData+Base64.h"


@implementation RegistrationController
@synthesize registrationIDTextBox;
@synthesize delegate;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    
    webService = [[PushCoinWebService alloc] initWithDelegate:self];
    buffer =  [[NSMutableData alloc] initWithLength:PushCoinWebServiceOutBufferSize];
    parser = [[PushCoinMessageParser alloc] init];
    
    [self.registrationIDTextBox becomeFirstResponder];
    self.registrationIDTextBox.delegate = self;
}

- (void)viewDidUnload
{
    [self setRegistrationIDTextBox:nil];
    [super viewDidUnload];
    // Release any retained subviews of the main view.
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [self register];
    return YES;
}

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range 
    replacementString:(NSString *)string 
{
    NSUInteger newLength = textField.text.length + string.length - range.length;
    return (newLength > 8) ? NO : YES;
}

- (AppDelegate *)appDelegate
{
    return (AppDelegate *)[[UIApplication sharedApplication] delegate];
}


-(void)register
{
    NSData * privateKey;
    NSData * publicKey;
    
    OpenSSLWrapper * ssl = [OpenSSLWrapper instance];
    [ssl generateDsaPrivateKey:&privateKey andPublicKey:&publicKey withBits:512 toPEMFile:
    [self.appDelegate.documentPath stringByAppendingPathComponent:PushCoinDSAPublicKeyFile]];
    [self.appDelegate setDsaPrivateKey:privateKey withPasscode:@""];
    
    RegisterMessage * msgOut = [[RegisterMessage alloc] init];
    PCOSRawData * dataOut = [[PCOSRawData alloc] initWithData:buffer];
    
    msgOut.register_block.registration_id.string = self.registrationIDTextBox.text;
    msgOut.register_block.public_key.data = [NSData dataFromBase64String:self.appDelegate.pemDsaPublicKey];
    
    [msgOut.register_block.user_agent.val addObject:[[KeyStringValue alloc] initWithKey:@"appname" andValue:@"PushCoin"]];
    [msgOut.register_block.user_agent.val addObject:[[KeyStringValue alloc] initWithKey:@"appver" andValue:@"1.0"]];
    [msgOut.register_block.user_agent.val addObject:[[KeyStringValue alloc] initWithKey:@"appurl" andValue:@"https://pushcoin.com/Pub/Apps/PushCoinIOSGC"]];
    [msgOut.register_block.user_agent.val addObject:[[KeyStringValue alloc] initWithKey:@"author" andValue:@"Gilbert Cheung <gilbertc@asurada.org>"]];
    [msgOut.register_block.user_agent.val addObject:[[KeyStringValue alloc] initWithKey:@"manufacturer" andValue:@"Apple Inc."]];
    [msgOut.register_block.user_agent.val addObject:[[KeyStringValue alloc] initWithKey:@"model" andValue:[[UIDevice currentDevice] name]]];
    [msgOut.register_block.user_agent.val addObject:[[KeyStringValue alloc] initWithKey:@"os" andValue:[NSString stringWithFormat:@"%@/%@",
                                                                                                        [[UIDevice currentDevice] systemName],
                                                                                                        [[UIDevice currentDevice] systemVersion]]]];

    [parser encodeMessage:msgOut to:dataOut];
    [webService sendMessage:dataOut.consumedData];
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
    [self.registrationIDTextBox becomeFirstResponder];
}

-(void) didDecodeErrorMessage:(ErrorMessage *)msg withHeader:(PCOSHeaderBlock*)hdr
{
    if (msg.block.error_code.val == 201)
    {
        [self.appDelegate showAlert:@"Registration Code not found."
              withTitle:@"Error"];
    }
    else
    {
        [self.appDelegate handleErrorMessage:msg withHeader:hdr];
    }
    [self.registrationIDTextBox becomeFirstResponder];
}

-(void) didDecodeUnknownMessage:(PCOSMessage *)msg withHeader:(PCOSHeaderBlock*)hdr
{
    [self.appDelegate handleUnknownMessage:msg withHeader:hdr];
    [self.registrationIDTextBox becomeFirstResponder];    
}

-(void) didDecodeRegisterAckMessage:(RegisterAckMessage *)msg withHeader:(PCOSHeaderBlock*)hdr
{
    self.appDelegate.authToken = [msg.register_ack_block.mat.data bytesToHexString];
    [self dismissModalViewControllerAnimated:YES];
    
    if (self.delegate != nil)
        [self.delegate registrationControllerDidClose:self];
}

@end
