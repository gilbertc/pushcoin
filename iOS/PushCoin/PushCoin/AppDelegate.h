#import <UIKit/UIKit.h>
#import "KeychainItemWrapper.h"
#import "PasscodeViewController.h"
#import "RegistrationController.h"
#import "MainTabBarController.h"
#import "OpenSSLWrapper.h"

@interface SingleUseData : NSObject
{
    NSData * data_;
}
@property (nonatomic, strong) NSData * data;

+(id) dataWithData:(NSData *)d;
-(id) initWithData:(NSData *)d;
@end


@interface AppDelegate : UIResponder <UIApplicationDelegate, OpenSSLWrapperDSAPrivateKeyDelegate>
@property (strong, nonatomic) KeychainItemWrapper * privateKeyKeychainItem;
@property (strong, nonatomic) KeychainItemWrapper * authTokenKeychainItem;
@property (strong, nonatomic) KeychainItemWrapper * pinHashKeychainItem;

@property (strong, nonatomic) UIWindow * window;

@property (nonatomic, readonly) BOOL registered;
@property (nonatomic, readonly) BOOL hasPasscode;

@property (nonatomic, readonly) NSString * pemDsaPublicKey;
@property (nonatomic, readonly) NSString * documentPath;

@property (nonatomic) NSString * authToken;
@property (nonatomic, readonly) NSData * dsaPrivateKey;

@property (nonatomic, strong) SingleUseData * dsaDecryptedKey;

@property (nonatomic, strong) NSURL * fileURL;

-(void) setPasscode:(NSString *)passcode oldPasscode:(NSString *)oldPasscode;
-(BOOL) validatePasscode:(NSString *)passcode;

-(void) setDsaPrivateKey:(NSData *)dsaPrivateKey withPasscode:(NSString *)passcode;
-(BOOL) unlockDsaPrivateKeyWithPasscode:(NSString *)passcode;

-(void) synchronizeDefaults;

-(KKPasscodeViewController *) requestPasscodeWithDelegate:(NSObject<KKPasscodeViewControllerDelegate> *)delegate;
-(KKPasscodeViewController *) requestPasscodeWithDelegate:(NSObject<KKPasscodeViewControllerDelegate> *)delegate
                                           viewController:(UIViewController *)controller;
-(KKPasscodeViewController *)requestPasscodeWithDelegate:(NSObject<KKPasscodeViewControllerDelegate> *)delegate
                                    navigationController:(UINavigationController *)controller;

-(RegistrationController *) requestRegistrationWithDelegate:(NSObject<RegistrationControllerDelegate> *)delegate;
-(RegistrationController *) requestRegistrationWithDelegate:(NSObject<RegistrationControllerDelegate> *)delegate
                                             viewController:(UIViewController *)controller;

-(MainTabBarController * ) mainTabBarController;

-(bool) handleErrorMessage:(NSString *)reason withErrorCode:(UInt32)errorCode;
-(bool) handleUnknownMessage:(NSString *)documentName;

-(UIAlertView *) showAlert:(NSString *)message withTitle:(NSString *)title;

-(id)viewControllerWithIdentifier:(NSString *) identifier;

-(void) clearDevice;

@end
