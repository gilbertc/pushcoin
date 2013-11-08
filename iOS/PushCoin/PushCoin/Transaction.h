//
//  Transaction.h
//  PushCoin
//
//  Created by Gilbert Cheung on 8/31/13.
//
//

#import <Foundation/Foundation.h>
#include <PcosHelper.h>

@protocol PcosSerializable <NSObject>
-(void) read:(pcos::BlockReader &) reader;
@end

@interface Amount : NSObject<PcosSerializable>
{
}
@property (assign) int64_t value;
@property (assign) int32_t scale;
@property (assign, readonly) double doubleValue;

-(Amount *) initWithReader:(pcos::BlockReader &) reader;
-(NSString *)text;

@end

@interface Address : NSObject<PcosSerializable>
{
}
@property (strong, nonatomic) NSString * street;
@property (strong, nonatomic) NSString * city;
@property (strong, nonatomic) NSString * state;
@property (strong, nonatomic) NSString * zip;
@property (strong, nonatomic) NSString * country;

-(Address *) initWithReader:(pcos::BlockReader &) reader;

@end

@interface Contact : NSObject<PcosSerializable>
{
}
@property (strong, nonatomic) NSString * phone;
@property (strong, nonatomic) NSString * email;

-(Contact *) initWithReader:(pcos::BlockReader &) reader;

@end

@interface GeoLocation : NSObject<PcosSerializable>
{
}
@property (assign) double latitude;
@property (assign) double longitude;

-(GeoLocation *) initWithReader:(pcos::BlockReader &) reader;
@end

@interface Transaction : NSObject<PcosSerializable>
{
}
@property (strong, nonatomic) NSString * transactionId;
@property (strong, nonatomic) NSString * deviceName;
@property (assign) uint64_t utcTransactionTime;
@property (strong, nonatomic) NSString * txType;
@property (strong, nonatomic) NSString * txContext;
@property (strong, nonatomic) NSString * currency;
@property (strong, nonatomic) Amount * payment;
@property (strong, nonatomic) Amount * tax;
@property (strong, nonatomic) Amount * tip;
@property (strong, nonatomic) NSString * counterPartyName;
@property (strong, nonatomic) NSString * invoice;
@property (strong, nonatomic) NSString * note;
@property (strong, nonatomic) Address * address;
@property (strong, nonatomic) Contact * contact;
@property (strong, nonatomic) GeoLocation * geolocation;
@property (strong, nonatomic) NSString * status;
@property (assign) uint32_t rating;
@property (assign) double merchantScore;
@property (assign) uint32_t merchantVoteCount;

-(Transaction *) initWithReader:(pcos::BlockReader &) reader;

@end