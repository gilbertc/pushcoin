//
//  FUIAlertViewDelegateBlockWrapper.m
//  PushCoin
//
//  Created by Gilbert Cheung on 9/5/13.
//
//

#import "FUIAlertViewDelegateBlockWrapper.h"

@implementation FUIAlertViewDelegateBlockWrapper
@synthesize callbackBlock;
- (id) initWithBlock:(void(^)(NSInteger))callback
{
    if (self = [super init])
        self.callbackBlock = callback;
    return self;
}

- (void) alertView:(FUIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (callbackBlock != nil)
        callbackBlock(buttonIndex);
}
@end
