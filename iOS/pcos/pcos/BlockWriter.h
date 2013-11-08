//
//  BlockWriter.h
//  pcos
//
//  Created by Gilbert Cheung on 8/28/13.
//  Copyright (c) 2013 PushCoin. All rights reserved.
//

#ifndef __pcos__BlockWriter__
#define __pcos__BlockWriter__

#include <string>
#include "Common.h"

namespace pcos {
    
    struct BlockWriter
    {
    private:
        enum {VARINT_BUF_POS_LAST = 9};

    private:
        std::unique_ptr<byte[]> output_;
        size_t offset_ = 0;
        size_t bufSize_ = BLOCK_BUF_SIZE;
        std::string name_;

    public:
        BlockWriter(std::string const & name, size_t bufSize = BLOCK_BUF_SIZE);
        
        BlockWriter() = default;
        BlockWriter(BlockWriter const &) = default;
        BlockWriter(BlockWriter &&) = default;
        
        void writeBool(bool const v);
        void writeByte(byte const v);
        void writeBytes(byte const * v, size_t offset, size_t len);
        void writeByteStr(byte const * v, size_t offset, size_t len);
        void writeInt(int32_t const v);
        void writeUInt(uint32_t const v);
        void writeLong(int64_t const v);
        void writeULong(uint64_t const v);
        void writeDouble(double const v);
        void writeString(std::string const & s);
        std::string const & name() const;
        size_t size() const;
        byte * bytes();
        
    private:
        void writeVarInt(uint64_t v);
    };
    
}

#endif /* defined(__pcos__BlockWriter__) */
