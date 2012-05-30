//
//  PushCoinTransaction.m
//  PushCoin
//
//  Created by Gilbert Cheung on 5/24/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "PushCoinTransaction.h"

@implementation PushCoinTransaction
@synthesize paymentScale = paymentScale_;
@synthesize paymentValue = paymentValue_;
@synthesize tipScale = tipScale_;
@synthesize tipValue = tipValue_;
@synthesize taxScale = taxScale_;
@synthesize taxValue = taxValue_;
@synthesize transactionID = transactionID_;
@synthesize transactionType = transactionType_;
@synthesize transactionContext = transactionContext_;
@synthesize counterpartyID = counterpartyID_;
@synthesize merchantName = merchantName_;
@synthesize timestamp = timestamp_;

-(id) init
{
    self = [super init];
    if (self)
    {
        self.transactionID = @"";
        self.counterpartyID = @"";
        self.transactionType = 'C';
        self.transactionContext = 'P';
        self.paymentValue = 0;
        self.paymentScale = 0;
        self.tipValue = 0;
        self.tipScale = 0;
        self.taxValue = 0;
        self.taxScale = 0;
        self.merchantName = @"";
        self.timestamp = 0;
    }
    return self;
}

-(id) initWithID:(NSString *)transactionID
  counterpartyID:(NSString *)counterpartyID
            type:(char)type
         context:(char)context
    paymentValue:(NSUInteger)paymentValue
    paymentScale:(NSInteger)paymentScale
        tipValue:(NSUInteger)tipValue
        tipScale:(NSInteger)tipScale
        taxValue:(NSUInteger)taxValue
        taxScale:(NSInteger)taxScale
    merchantName:(NSString*)merchantName
       timestamp:(NSUInteger)timestamp
{
    self = [super init];
    if (self)
    {
        self.paymentScale = paymentScale;
        self.paymentValue = paymentValue;
        self.tipValue = tipValue;
        self.tipScale = tipScale;
        self.taxValue = taxValue;
        self.taxScale = taxScale;
        self.merchantName = merchantName;
        self.transactionType = type;
        self.transactionContext = context;
        self.transactionID = transactionID;
        self.counterpartyID = counterpartyID;
        self.timestamp = timestamp;
    }
    return self;
}

-(NSString *) merchantName
{
    if (!merchantName_ || !merchantName_.length)
    {
        switch(self.transactionContext)
        {
            case 'P': return @"Payment";
            case 'T': return @"Transfer";
            default: return @"Unknown";
        }
    }
    return merchantName_;
}

-(id) copyWithZone:(NSZone *)zone
{
    PushCoinTransaction * other = [[PushCoinTransaction alloc] initWithID:self.transactionID
                                                           counterpartyID:self.counterpartyID
                                                                     type:self.transactionType
                                                                  context:self.transactionContext
                                                             paymentValue:self.paymentValue
                                                             paymentScale:self.paymentScale
                                                                 tipValue:self.tipValue
                                                                 tipScale:self.tipScale
                                                                 taxValue:self.taxValue
                                                                 taxScale:self.taxScale
                                                             merchantName:self.merchantName
                                                                timestamp:self.timestamp];
    return other;
    
}
@end
