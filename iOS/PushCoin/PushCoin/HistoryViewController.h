//
//  HistoryViewController.h
//  PushCoin
//
//  Created by Gilbert Cheung on 8/31/13.
//
//

#import <UIKit/UIKit.h>
#import "MessageUpdatedDelegate.h"

@class MGScrollView;
@interface HistoryViewController : UIViewController<MessageUpdatedDelegate>
{
    
}
@property (weak, nonatomic) IBOutlet MGScrollView * scroller;

@end
