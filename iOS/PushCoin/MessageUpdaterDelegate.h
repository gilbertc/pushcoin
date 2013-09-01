//
//  MessageUpdaterDelegate.h
//  PushCoin
//
//  Created by Gilbert Cheung on 8/31/13.
//
//

#import <Foundation/Foundation.h>

@protocol MessageUpdatedDelegate;
@protocol MessageUpdaterDelegate <NSObject>

- (NSString *) timestamp;
- (NSString *) balance;
- (NSMutableArray * ) transactions;

- (void)registerMessageListener:(id <MessageUpdatedDelegate>) listener;
- (void) refresh;

@end