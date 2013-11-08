//
//  BlockWriter.cpp
//  pcos
//
//  Created by Gilbert Cheung on 8/28/13.
//  Copyright (c) 2013 PushCoin. All rights reserved.
//

#include "BlockWriter.h"
#include "Exceptions.h"

namespace pcos {
    BlockWriter::BlockWriter(std::string const & name, size_t bufSize)
    : output_(new byte[bufSize])
    , bufSize_(bufSize)
    , name_(name)
    {
        
    }
    
    std::string const & BlockWriter::name() const
    {
        return name_;
    }
    
    size_t BlockWriter::size() const
    {
        return offset_;
    }
    
    void BlockWriter::writeBool(bool const v)
    {
        writeByte((byte)v);
    }
    
    void BlockWriter::writeByte(byte const v)
    {
        output_[offset_++] = v;
        if (offset_ >= bufSize_)
            throw MalformedException();
    }
    
    void BlockWriter::writeBytes(byte const * v, size_t offset, size_t len)
    {
        if (offset_ + len >= bufSize_)
            throw MalformedException("write bytes seq exceed buf len");
        
        memcpy(&output_[offset_], v + offset, len);
        offset_ += len;
    }
    
    void BlockWriter::writeDouble(double const v)
    {
        writeBytes((byte *) &v, 0, TYPE_WIRE_SIZE_DOUBLE);
    }
    
    void BlockWriter::writeByteStr(byte const * v, size_t offset, size_t len)
    {
        writeUInt(len);
        writeBytes(v, offset, len);
    }
    
    void BlockWriter::writeString(std::string const & v)
    {
        writeByteStr((byte *)v.c_str(), 0, v.size());
    }
    
    void BlockWriter::writeInt(int32_t const v)
    {
        uint64_t zz = ((v << 1) ^ (v >> 31));
        writeVarInt(zz);
    }
    
    void BlockWriter::writeUInt(uint32_t const v)
    {
        writeVarInt(v);
    }
    
    void BlockWriter::writeLong(int64_t const v)
    {
        uint64_t zz = ((v << 1) ^ (v >> 63));
        writeVarInt(zz);
    }
    
    void BlockWriter::writeULong(uint64_t const v)
    {
        writeVarInt(v);
    }
    
    void BlockWriter::writeVarInt(uint64_t v)
    {
        byte buf[VARINT_BUF_POS_LAST + 1];
        int pos = VARINT_BUF_POS_LAST;
        while (v > 0x7f)
        {
            uint32_t octet = (uint32_t) (v & 0x7f);
            if (pos != VARINT_BUF_POS_LAST)
            {
                octet |= 0x80;
            }
            
            buf[pos--] = (byte) octet;
            v >>= 7;
        }
        
        if (pos == VARINT_BUF_POS_LAST)
        {
            output_[offset_++] = (byte) v;
        }
        else
        {
            v |= 0x80;
            buf[pos] = (byte) v;
            writeBytes(buf, pos, VARINT_BUF_POS_LAST - pos + 1);
        }
    }
    
    byte * BlockWriter::bytes()
    {
        return output_.get();
    }
    
}