//
//  PCOSHelper.h
//  PushCoin
//
//  Created by Gilbert Cheung on 8/31/13.
//
//

#import <Foundation/Foundation.h>
#include <pcos/pcos.h>

NSString * readString(pcos::BlockReader & reader, size_t maxLen = 128);
NSData * readByteStr(pcos::BlockReader & reader, size_t maxLen = 128);
NSData * readBytes(pcos::BlockReader & reader, size_t len);

void writeBytes(pcos::BlockWriter & writer, NSData * data);
void writeByteStr(pcos::BlockWriter & writer, NSData * data);
void writeString(pcos::BlockWriter & writer, NSString * str);
