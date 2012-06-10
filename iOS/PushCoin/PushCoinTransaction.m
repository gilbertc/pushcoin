//
//  PushCoinTransaction.m
//  PushCoin
//
//  Created by Gilbert Cheung on 5/24/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "PushCoinTransaction.h"
#import "PushCoinAddressBook.h"
#import "AppDelegate.h"

@implementation PushCoinTransaction
@synthesize paymentScale = paymentScale_;
@synthesize paymentValue = paymentValue_;
@synthesize taxScale = taxScale_;
@synthesize taxValue = taxValue_;
@synthesize tipScale = tipScale_;
@synthesize tipValue = tipValue_;
@synthesize transactionID = transactionID_;
@synthesize transactionType = transactionType_;
@synthesize transactionContext = transactionContext_;
@synthesize counterpartyID = counterpartyID_;
@synthesize merchantName = merchantName_;
@synthesize recipient = recipient_;
@synthesize invoice = invoice_;
@synthesize timestamp = timestamp_;

@synthesize addressStreet;
@synthesize addressCity;
@synthesize addressState;
@synthesize addressZip;
@synthesize addressCountry;
@synthesize contactPhone;
@synthesize contactEmail;

@synthesize longitude;
@synthesize latitude;

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
        self.taxValue = 0;
        self.taxScale = 0;
        self.tipValue = 0;
        self.tipScale = 0;
        self.merchantName = @"";
        self.recipient = @"";
        self.invoice = @"";
        self.timestamp = 0;
        
        self.addressStreet = @"";
        self.addressCity = @"";
        self.addressState = @"";
        self.addressZip = @"";
        self.addressCountry = @"";
        self.contactPhone = @"";
        self.contactEmail = @"";
        
        self.longitude = 0.0;
        self.latitude = 0.0;
    }
    return self;
}

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
       recipient:(NSString*)recipient
         invoice:(NSString*)invoice
       timestamp:(NSUInteger)timestamp
{
    self = [self init];
    if (self)
    {
        self.paymentScale = paymentScale;
        self.paymentValue = paymentValue;
        self.taxValue = taxValue;
        self.taxScale = taxScale;
        self.tipValue = tipValue;
        self.tipScale = tipScale;
        self.merchantName = merchantName;
        self.recipient = recipient;
        self.transactionType = type;
        self.transactionContext = context;
        self.transactionID = transactionID;
        self.counterpartyID = counterpartyID;
        self.invoice = invoice;
        self.timestamp = timestamp;
    }
    return self;
}

- (AppDelegate *)appDelegate
{
    return (AppDelegate *)[[UIApplication sharedApplication] delegate];
}

-(NSString *) merchantName
{
    if (!merchantName_ || !merchantName_.length)
    {
        // search the email book
        PushCoinEntity * entity = [self.appDelegate.addressBook.dataStore 
                                   objectForKey:self.counterpartyID];
        if (entity)
        {
            if (entity.name && entity.name.length)
                return entity.name;
            return entity.email;
        }
        
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
                                                                 taxValue:self.taxValue
                                                                 taxScale:self.taxScale
                                                                 tipValue:self.tipValue
                                                                 tipScale:self.tipScale
                                                             merchantName:self.merchantName
                                                                recipient:self.recipient
                                                                  invoice:self.invoice
                                                                timestamp:self.timestamp];
    if (other)
    {
        other.addressStreet = self.addressStreet;
        other.addressCity = self.addressCity;
        other.addressState = self.addressState;
        other.addressZip = self.addressZip;
        other.addressCountry = self.addressCountry;
        other.contactPhone = self.contactPhone;
        other.contactEmail = self.contactEmail;
        
        other.longitude = self.longitude;
        other.latitude = self.latitude;
    }
    return other;
    
}
@end
