#import <UIKit/UIKit.h>
#import "RegistrationController.h"
#import "PushCoinWebService.h"
#import "MessageUpdaterDelegate.h"

@interface MainTabBarController : UITabBarController<RegistrationControllerDelegate,PushCoinWebServiceDelegate, MessageUpdaterDelegate>
{
    BOOL refreshing;
}

@property (strong, nonatomic) NSString * timestamp;
@property (strong, nonatomic) NSString * balance;
@property (strong, nonatomic) NSMutableArray * transactions;
@property (strong, nonatomic) NSMutableArray * messageListeners;

@end