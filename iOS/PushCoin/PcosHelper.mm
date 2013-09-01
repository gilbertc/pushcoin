//
//  PCOSHelper.m
//  PushCoin
//
//  Created by Gilbert Cheung on 8/31/13.
//
//

#import "PcosHelper.h"

using namespace pcos;

NSData * readByteStr(BlockReader & reader, size_t maxLen)
{
    byte const * bytes;
    size_t rlen = reader.readByteStr(bytes, maxLen);
    return [NSData dataWithBytesNoCopy:(void *)bytes length:rlen freeWhenDone:NO];
}

NSData * readBytes(BlockReader & reader, size_t len)
{
    byte const * bytes;
    size_t rlen = reader.readBytes(bytes, len);
    return [NSData dataWithBytesNoCopy:(void *)bytes length:rlen freeWhenDone:NO];
}

NSString * readString(BlockReader & reader, size_t maxLen)
{
    std::string res = reader.readString(maxLen);
    return [NSString stringWithUTF8String:res.c_str()];
}

void writeBytes(BlockWriter & writer, NSData * data)
{
    writer.writeBytes((byte const *) data.bytes, 0, data.length);
}

void writeByteStr(BlockWriter & writer, NSData * data)
{
    writer.writeByteStr((byte const *) data.bytes, 0, data.length);
}

void writeString(BlockWriter & writer, NSString * str)
{
    writer.writeString(str.UTF8String);
}
