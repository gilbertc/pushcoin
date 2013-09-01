#import <UIKit/UIKit.h>
#import "PushCoinWebService.h"
#import "KeychainItemWrapper.h"
#import "OpenSSLWrapper.h"

@class RegistrationController;

@protocol RegistrationControllerDelegate <NSObject>


- (void)registrationControllerDidClose:
(RegistrationController *)controller;

@end

@interface RegistrationController : UIViewController<UITextFieldDelegate, PushCoinWebServiceDelegate>
{
    PushCoinWebService * webService;
    NSMutableData * buffer;
}

@property (nonatomic, weak) id <RegistrationControllerDelegate> delegate;
@property (weak, nonatomic) IBOutlet UITextField *registrationIDTextBox;
@end
