//
//  BlockReader.h
//  pcos
//
//  Created by Gilbert Cheung on 8/28/13.
//  Copyright (c) 2013 PushCoin. All rights reserved.
//

#ifndef __pcos__BlockReader__
#define __pcos__BlockReader__

#include <string>
#include "Common.h"

namespace pcos {
    
    struct BlockReader
    {
    private:
        byte const * input_;
        size_t beg_, end_, offset_;
        std::string name_;
        
    public:
        BlockReader(byte const * input, size_t offset, size_t len, std::string const & name);
        
        BlockReader() = default;
        BlockReader(BlockReader const &) = default;
        BlockReader(BlockReader &&) = default;
        
        bool readBool();
        byte readByte();
        size_t readBytes(byte const * & buf, size_t size);
        size_t readByteStr(byte const * & buf, size_t maxLen);
        int32_t readInt();
        uint32_t readUInt();
        int64_t readLong();
        uint64_t readULong();
        double readDouble();
        std::string readString(size_t maxLen);
        
        size_t readPos() const;
        size_t size() const;
        std::string const & name() const;
        
    private:
        int64_t readVarInt(uint32_t max_octets);
    };
    
}

#endif /* defined(__pcos__BlockReader__) */
