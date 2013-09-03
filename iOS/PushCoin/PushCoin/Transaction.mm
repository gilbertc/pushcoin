//
//  Transaction.m
//  PushCoin
//
//  Created by Gilbert Cheung on 8/31/13.
//
//

#import "Transaction.h"

@implementation Amount 
@synthesize value;
@synthesize scale;

-(void) read:(pcos::BlockReader &)reader
{
    self.value = reader.readLong();
    self.scale = reader.readInt();
}
-(Amount *) initWithReader:(pcos::BlockReader & ) reader
{
    self = [super init];
    [self read:reader];
    return self;
}

-(NSString *) text
{
    return [NSString stringWithFormat:@"$%.02lf", self.doubleValue];
}

-(double) doubleValue
{
    return self.value * pow(10, self.scale);
}

@end

@implementation Address 
@synthesize street;
@synthesize city;
@synthesize state;
@synthesize zip;
@synthesize country;

-(void) read:(pcos::BlockReader &)reader
{
    self.street = readString(reader);
    self.city = readString(reader);
    self.state = readString(reader);
    self.zip = readString(reader);
    self.country = readString(reader);
}
-(Address *) initWithReader:(pcos::BlockReader & ) reader
{
    self = [super init];
    [self read:reader];
    return self;
}

@end

@implementation Contact 
@synthesize phone;
@synthesize email;

-(void) read:(pcos::BlockReader &)reader
{
    self.phone = readString(reader);
    self.email = readString(reader);
}
-(Contact *) initWithReader:(pcos::BlockReader & ) reader
{
    self = [super init];
    [self read:reader];
    return self;
}
@end

@implementation GeoLocation
@synthesize latitude;
@synthesize longitude;

-(void) read:(pcos::BlockReader &)reader
{
    self.latitude = reader.readDouble();
    self.longitude = reader.readDouble();
}

-(GeoLocation *) initWithReader:(pcos::BlockReader & ) reader
{
    self = [super init];
    [self read:reader];
    return self;
}
@end

@implementation Transaction
@synthesize transactionId;
@synthesize deviceName;
@synthesize utcTransactionTime;
@synthesize txType;
@synthesize txContext;
@synthesize currency;
@synthesize payment;
@synthesize tax;
@synthesize tip;
@synthesize counterPartyName;
@synthesize invoice;
@synthesize note;
@synthesize address;
@synthesize contact;
@synthesize geolocation;
@synthesize status;

-(void) read:(pcos::BlockReader &)reader
{
    self.transactionId = readString(reader);
    self.deviceName = readString(reader);
    self.utcTransactionTime = reader.readULong();
    self.txType = readString(reader);
    self.txContext = readString(reader);
    self.currency = readString(reader);
    self.payment = [[Amount alloc] initWithReader:reader];
    self.tax = reader.readBool() ? [[Amount alloc] initWithReader:reader] : nil;
    self.tip = reader.readBool() ? [[Amount alloc] initWithReader:reader] : nil;
    self.counterPartyName = readString(reader);
    self.invoice = readString(reader);
    self.note = readString(reader);
    self.address = reader.readBool() ? [[Address alloc] initWithReader:reader] : nil;
    self.contact = reader.readBool() ? [[Contact alloc] initWithReader:reader] : nil;
    self.geolocation = reader.readBool() ? [[GeoLocation alloc] initWithReader:reader] : nil;
    self.status = readString(reader);
}
-(Transaction *) initWithReader:(pcos::BlockReader & ) reader
{
    self = [super init];
    [self read:reader];
    return self;
}
@end
