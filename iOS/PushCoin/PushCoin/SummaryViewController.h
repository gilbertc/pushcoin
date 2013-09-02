
#import <UIKit/UIKit.h>
#import "MessageUpdatedDelegate.h"

@interface SummaryViewController : UIViewController<MessageUpdatedDelegate>
{
}
@property (weak, nonatomic) IBOutlet UILabel *balanceLabel;
@property (weak, nonatomic) IBOutlet UILabel *timestampLabel;
@property (weak, nonatomic) IBOutlet UIScrollView *scrollView;
@end
