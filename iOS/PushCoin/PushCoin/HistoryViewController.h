//
//  HistoryViewController.h
//  PushCoin
//
//  Created by Gilbert Cheung on 8/31/13.
//
//

#import <UIKit/UIKit.h>
#import "MessageUpdatedDelegate.h"

@interface HistoryViewController : UITableViewController<MessageUpdatedDelegate, UITableViewDataSource, UITableViewDelegate>
{
    
}
@property (strong, nonatomic) UIRefreshControl *refreshControl;

@end
