
#import <UIKit/UIKit.h>
#import "MessageUpdatedDelegate.h"

@interface SummaryViewController : UIViewController<MessageUpdatedDelegate>
{
}
@property (weak, nonatomic) IBOutlet UILabel *balanceLabel;
@property (weak, nonatomic) IBOutlet UILabel *balanceDecimalLabel;

@property (weak, nonatomic) IBOutlet UILabel *lastPaymentLabel;
@property (weak, nonatomic) IBOutlet UILabel *lastAddressLabel;
@property (weak, nonatomic) IBOutlet UILabel *lastCounterPartyLabel;
@property (weak, nonatomic) IBOutlet UILabel *lastTimeLabel;

@property (weak, nonatomic) IBOutlet UILabel *timestampLabel;
@property (weak, nonatomic) IBOutlet UIScrollView *scrollView;
@end
