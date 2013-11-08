//
//  DocumentWriter.cpp
//  pcos
//
//  Created by Gilbert Cheung on 8/28/13.
//  Copyright (c) 2013 PushCoin. All rights reserved.
//

#include "DocumentWriter.h"
#include "BlockWriter.h"
#include "Exceptions.h"

namespace pcos {
    DocumentWriter::DocumentWriter(std::string const & name, size_t bufSize)
    : writer_("??", bufSize)
    , name_(name)
    {
        if (name_.length() > MAX_MESSAGE_ID_LEN)
            throw MalformedException("Invalid message ID");
    }
    
    byte * DocumentWriter::bytes()
    {
        return write().bytes();
    }
    
    size_t DocumentWriter::size()
    {
        return write().size();
    }
    
    BlockWriter & DocumentWriter::write()
    {
        if (editable())
        {
            writer_.writeBytes((byte *) PROTOCOL_MAGIC, 0, PROTOCOL_MAGIC_LEN);
            writer_.writeByte(PROTOCOL_FLAGS);
            writer_.writeString(name_);
            writer_.writeUInt(blocks_.size());
            
            for (auto & blk : blocks_)
            {
                writer_.writeString(blk.name());
                writer_.writeUInt(blk.size());
            }
            
            for (auto & blk : blocks_)
                writer_.writeBytes(blk.bytes(), 0, blk.size());
        }
        return writer_;
    }
    
}