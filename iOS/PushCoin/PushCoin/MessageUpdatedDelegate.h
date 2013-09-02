//
//  MessageUpdatedDelegate.h
//  PushCoin
//
//  Created by Gilbert Cheung on 8/31/13.
//
//

#import <Foundation/Foundation.h>
#import "MessageUpdaterDelegate.h"

@protocol MessageUpdatedDelegate <NSObject>

- (void)messageDidUpdatedBy:(id <MessageUpdaterDelegate>)updater;
- (void)messageDidFailedBy:(id <MessageUpdaterDelegate>)updater withDescription:description;
@end
