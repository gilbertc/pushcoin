#import "RegistrationController.h"
#import "AppDelegate.h"
#import "NSString+HexStringToBytes.h"
#import "NSData+BytesToHexString.h"
#import "NSData+Base64.h"
#import "PcosHelper.h"

#include <pcos/pcos.h>

using namespace pcos;

@implementation RegistrationController
@synthesize registrationIDTextBox;
@synthesize registrationView;
@synthesize waitingView;
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
    textField.enabled = NO;
    self.registrationView.hidden = YES;
    self.waitingView.hidden = NO;
    
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        NSData * message = [self register];
        dispatch_async(dispatch_get_main_queue(), ^{
            [webService sendMessage:message];
        });
    });
    
    return YES;
}

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range 
    replacementString:(NSString *)string 
{
    NSUInteger newLength = textField.text.length + string.length - range.length;
    return (newLength > 11) ? NO : YES;
}

- (AppDelegate *)appDelegate
{
    return (AppDelegate *)[[UIApplication sharedApplication] delegate];
}


-(NSData *)register
{
    NSData * privateKey;
    NSData * publicKey;
    
    OpenSSLWrapper * ssl = [OpenSSLWrapper instance];
    [ssl generateDsaPrivateKey:&privateKey andPublicKey:&publicKey withBits:512 toPEMFile:
    [self.appDelegate.documentPath stringByAppendingPathComponent:PushCoinDSAPublicKeyFile]];
    [self.appDelegate setDsaPrivateKey:privateKey withPasscode:@""];
    
    NSData * pemDsaPublickey = [NSData dataFromBase64String:self.appDelegate.pemDsaPublicKey];
    
    DocumentWriter writer("Register");
    auto & bo = writer.addBlock("Bo");
    bo.writeString(self.registrationIDTextBox.text.UTF8String);
    bo.writeByteStr((byte const *)pemDsaPublickey.bytes, 0, pemDsaPublickey.length);
    
    return [NSData dataWithBytes:writer.bytes() length:writer.size()];
}

- (void)webService:(PushCoinWebService *)webService didReceiveMessage:(NSData *)data
{
    self.registrationIDTextBox.enabled = YES;
    self.waitingView.hidden = YES;
    self.registrationView.hidden = NO;

    try{
        DocumentReader reader((byte const * )data.bytes, 0, data.length);
        NSString * documentName = [NSString stringWithUTF8String:reader.getDocumentName().c_str()];
        
        if ([documentName isEqualToString:@"RegisterAck"])
        {
            auto it = reader.find("Bo");
            if (it != reader.end())
            {
                auto & bo = it->second;
                self.appDelegate.authToken = [readByteStr(bo) bytesToHexString];
                [self dismissViewControllerAnimated:YES completion:NULL];
                if (self.delegate != nil)
                    [self.delegate registrationControllerDidClose:self];
                return;
            }
        }
        
        if ([documentName isEqualToString:@"Error"])
        {
            auto it = reader.find("Bo");
            if (it != reader.end())
            {
                auto & bo = it->second;
                
                NSString * transactionId = [readByteStr(bo, 0) bytesToHexString];
                UInt32 errorCode = bo.readUInt();
                NSString * reason = readString(bo, 0); // Reason
                
                [self.appDelegate showAlert:reason
                                  withTitle:[NSString stringWithFormat:@"Error %d", (unsigned int)errorCode]];
                
                [self.registrationIDTextBox becomeFirstResponder];
                return;
            }
        }
        [self.appDelegate handleUnknownMessage:documentName];
        [self.registrationIDTextBox becomeFirstResponder];
        
    }
    catch(PcosException ex)
    {
        NSLog(@"Exception");
    }   
}

- (void)webService:(PushCoinWebService *)webService didFailWithStatusCode:(NSInteger)statusCode 
    andDescription:(NSString *)description
{
    self.registrationIDTextBox.enabled = YES;
    self.waitingView.hidden = YES;
    self.registrationView.hidden = NO;

    [[self appDelegate] showAlert:description
                        withTitle:[NSString stringWithFormat:@"Webservice Error - %d", statusCode]];
    [self.registrationIDTextBox becomeFirstResponder];
}

@end
