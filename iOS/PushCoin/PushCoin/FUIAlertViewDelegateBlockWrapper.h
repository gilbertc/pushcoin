//
//  FUIAlertViewDelegateBlockWrapper.h
//  PushCoin
//
//  Created by Gilbert Cheung on 9/5/13.
//
//

#import <Foundation/Foundation.h>
#import "FUIAlertView.h"

@interface FUIAlertViewDelegateBlockWrapper : NSObject <FUIAlertViewDelegate>
typedef void (^HeapBlock)(NSInteger);
@property (nonatomic, copy) HeapBlock callbackBlock;

- (id) initWithBlock:(void(^)(NSInteger))callback;

@end
