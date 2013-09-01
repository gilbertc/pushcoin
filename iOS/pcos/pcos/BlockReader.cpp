//
//  BlockReader.cpp
//  pcos
//
//  Created by Gilbert Cheung on 8/28/13.
//  Copyright (c) 2013 PushCoin. All rights reserved.
//

#include "BlockReader.h"
#include "Exceptions.h"

namespace pcos {
    
    BlockReader::BlockReader(byte const * input, size_t offset, size_t len, std::string const & name)
    : input_(input)
    , beg_(offset)
    , end_(offset + len)
    , offset_(0)
    , name_(name)
    {
    }
    
    bool BlockReader::readBool()
    {
        return readByte() != 0;
    }
    
    byte BlockReader::readByte()
    {
        if (end_ - offset_ > 0)
            return input_[beg_ + offset_++];
        throw MalformedException();
    }
    
    size_t BlockReader::readBytes(byte const * & buf, size_t len)
    {
        if (end_ - offset_ >= len)
        {
            buf = input_ + beg_ + offset_;
            offset_ += len;
            return len;
        }
        throw MalformedException();
    }
    
    size_t BlockReader::readByteStr(byte const * & buf, size_t maxLen)
    {
        uint32_t len = readUInt();
        if (maxLen != 0 && len > maxLen)
            throw MalformedException("input byte seq exceeds max length");
        return readBytes(buf, len);
    }
    
    int32_t BlockReader::readInt()
    {
        uint32_t val = readUInt();
        return (int32_t)((val >> 1) ^ (-(val & 1)));
    }

    uint32_t BlockReader::readUInt()
    {
        return (uint32_t)readVarInt(5); // 5 => max bytes uint can take on the wire
    }

    int64_t BlockReader::readLong()
    {
        uint64_t val = readULong();
        return (int64_t)(val >> 1) ^ (-(val & 1));
    }

    uint64_t BlockReader::readULong()
    {
        return readVarInt(10); // 10 => max bytes ulong can take on the wire
    }

    double BlockReader::readDouble()
    {
        byte const * ret;
        if (readBytes(ret, TYPE_WIRE_SIZE_DOUBLE) != TYPE_WIRE_SIZE_DOUBLE)
            throw MalformedException();
        return *((double *)ret);
    }

    std::string BlockReader::readString(size_t maxLen)
    {
        byte const * buf;
        size_t len = readByteStr(buf, maxLen);
        return std::string(reinterpret_cast<char const *>(buf), len);
    }
    
    size_t BlockReader::readPos() const
    {
        return beg_ + offset_;
    }
    
    std::string const & BlockReader::name() const
    {
        return name_;
    }
    
    size_t BlockReader::size() const
    {
        return end_ - beg_;
    }
    
    int64_t BlockReader::readVarInt(uint32_t max_octets)
    {
        int64_t val = 0;
        bool seen_end = false;
        while (max_octets > 0)
        {
            int octet = readByte();
            val |= (octet & 0x7f);
            
            seen_end = ((octet & 0x80) == 0);
            if (seen_end)
            {
                break;
            }
            else
            {
                val <<= 7;
                max_octets--;
            }
        }
        
        if (!seen_end)
            throw MalformedException();
        
        return val;
    }
    
}
