//
//  SidePanelController.h
//  PushCoin
//
//  Created by Gilbert Cheung on 9/2/13.
//
//
#import "JASidePanelController.h"
#import "RegistrationController.h"
#import "PushCoinWebService.h"
#import "MessageUpdaterDelegate.h"
#import "Transaction.h"

@interface SidePanelController : JASidePanelController<RegistrationControllerDelegate, PushCoinWebServiceDelegate, MessageUpdaterDelegate>
{
    BOOL refreshing;
}


@property (assign) uint64_t timestamp;
@property (strong, nonatomic) Amount * balance;
@property (strong, nonatomic) NSMutableArray * transactions;
@property (strong, nonatomic) NSMutableArray * messageListeners;

-(void) showSummary;
-(void) showHistory;

@end
