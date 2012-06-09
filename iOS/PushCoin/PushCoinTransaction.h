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
@property (nonatomic, strong) NSString * invoice;
@property (nonatomic, assign) NSUInteger timestamp;

@property (nonatomic, strong) NSString * addressStreet;
@property (nonatomic, strong) NSString * addressCity;
@property (nonatomic, strong) NSString * addressState;
@property (nonatomic, strong) NSString * addressZip;
@property (nonatomic, strong) NSString * addressCountry;
@property (nonatomic, strong) NSString * contactPhone;
@property (nonatomic, strong) NSString * contactEmail;
@property (nonatomic, assign) CGFloat longitude;
@property (nonatomic, assign) CGFloat latitude;

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
         invoice:(NSString*)invoice
       timestamp:(NSUInteger)timestamp;
@end
