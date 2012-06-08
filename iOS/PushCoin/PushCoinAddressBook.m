//
//  EmailBook.m
//  PushCoin
//
//  Created by Gilbert Cheung on 6/7/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "PushCoinAddressBook.h"
#import "NSString+HexStringToBytes.h"
#import "NSData+BytesToHexString.h"
#import "OpenSSLWrapper.h"
#import <AddressBook/AddressBook.h>

@implementation PushCoinEntity
@synthesize name;
@synthesize email;
@synthesize recordID;

-(id) initWithRecordID:(NSInteger)rID
{
    self = [super init];
    if (self)
    {
        self.recordID = rID;
    }
    return self;
}

-(id) copyWithZone:(NSZone *)zone
{
    PushCoinEntity * ret = [self initWithRecordID:self.recordID];
    if (ret)
    {
        ret.name = [self.name copy];
        ret.email = [self.email copy];
    }
    return ret;
}
@end

@implementation PushCoinAddressBook
@synthesize dataStore;

-(id) init
{
    self = [super init];
    if (self)
    {
        dataStore = [[NSMutableDictionary alloc] init];
    }
    return self;
}

- (AppDelegate *)appDelegate
{
    return (AppDelegate *)[[UIApplication sharedApplication] delegate];
}

-(void) refresh
{
    [dataStore removeAllObjects];

    NSString * authToken = [self appDelegate].authToken;
    ABAddressBookRef addressBook = ABAddressBookCreate();
    CFArrayRef people = ABAddressBookCopyArrayOfAllPeople(addressBook);
    for (CFIndex i = 0; i < CFArrayGetCount(people); i++) 
    {
        ABRecordRef person = CFArrayGetValueAtIndex(people, i);
       
        ABMultiValueRef emails = ABRecordCopyValue(person, kABPersonEmailProperty);
        
        NSInteger recordID = ABRecordGetRecordID(person);
        
        NSString * firstName = (__bridge NSString *) ABRecordCopyValue(person, kABPersonFirstNameProperty);
        NSString * lastName = (__bridge NSString *) ABRecordCopyValue(person, kABPersonLastNameProperty);
        
        NSString * name;
        if ((firstName && firstName.length) && (lastName && lastName.length))
            name = [NSString stringWithFormat:@"%@ %@", firstName, lastName];
        else if (firstName && firstName.length)
            name = firstName;
        else if (lastName && lastName.length)
            name = lastName;
        else
            name = @"Unknown";
        
        for (CFIndex j=0; j < ABMultiValueGetCount(emails); j++) 
        {
            NSString * email = (__bridge NSString*)ABMultiValueCopyValueAtIndex(emails, j);
            if (email && email.length)
            {
                PushCoinEntity * entity = [[PushCoinEntity alloc] initWithRecordID:recordID];
                entity.name = name;
                entity.email = email;
                
            
                [self.dataStore setObject:entity forKey:[self hash:email authToken:authToken]];
            }
        }
        CFRelease(emails);
    }
    CFRelease(addressBook);
    CFRelease(people);
}

-(id) hash:(NSString *) email authToken:(NSString *)authToken
{
    NSMutableData * data = [[NSMutableData alloc] initWithData:authToken.hexStringToBytes];
    [data appendData:[NSData dataWithBytes:email.UTF8String length:email.length]];
    
    return [[OpenSSLWrapper instance] sha1_hashData:data].bytesToHexString;
}

@end
