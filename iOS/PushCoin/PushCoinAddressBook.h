//
//  EmailBook.h
//  PushCoin
//
//  Created by Gilbert Cheung on 6/7/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AppDelegate.h"

@interface PushCoinEntity: NSObject<NSCopying>
@property (nonatomic, strong) NSString * name;
@property (nonatomic, strong) NSString * email;
@property (nonatomic, assign) NSInteger recordID;

-(id) initWithRecordID:(NSInteger) rID;
@end

@interface PushCoinAddressBook : NSObject
@property (nonatomic, strong) NSMutableDictionary * dataStore;
-(void) refresh;
@end
