//
//  PushCoinTransaction.h
//  PushCoin
//
//  Created by Gilbert Cheung on 5/24/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface PushCoinTransaction : NSObject<NSCopying>
@property (nonatomic, strong) NSString * transactionID;
@property (nonatomic, strong) NSString * counterpartyID;
@property (nonatomic, assign) char transactionType;
@property (nonatomic, assign) char transactionContext;
@property (nonatomic, assign) NSUInteger paymentValue;
@property (nonatomic, assign) NSInteger paymentScale;
@property (nonatomic, assign) NSUInteger taxValue;
@property (nonatomic, assign) NSInteger taxScale;
@property (nonatomic, assign) NSUInteger tipValue;
@property (nonatomic, assign) NSInteger tipScale;
@property (nonatomic, strong) NSString * merchantName;
@property (nonatomic, assign) NSUInteger timestamp;

-(id) initWithID:(NSString *)transactionID
  counterpartyID:(NSString *)counterpartyID
            type:(char)type
         context:(char)context
    paymentValue:(NSUInteger)paymentValue
    paymentScale:(NSInteger)paymentScale
        taxValue:(NSUInteger)taxValue
        taxScale:(NSInteger)taxScale
        tipValue:(NSUInteger)tipValue
        tipScale:(NSInteger)tipScale
    merchantName:(NSString*)merchantName
       timestamp:(NSUInteger)timestamp;
@end
