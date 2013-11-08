//
//  DocumentReader.cpp
//  pcos
//
//  Created by Gilbert Cheung on 8/28/13.
//  Copyright (c) 2013 PushCoin. All rights reserved.
//

#include "DocumentReader.h"
#include "BlockReader.h"
#include "Common.h"
#include "Exceptions.h"

#include <vector>

namespace pcos {
    
    DocumentReader::DocumentReader(byte const * input, size_t offset, size_t len)
    {
        BlockReader inBlock(input, offset, len, "Hd");
        
        byte const * magic;
        size_t r = inBlock.readBytes(magic, PROTOCOL_MAGIC_LEN);

        if (::strncmp((char *)magic, PROTOCOL_MAGIC, PROTOCOL_MAGIC_LEN) != 0)
            throw BadMagicException();
        
        magic_ = std::string((char *) magic, r);
        pcosFlags_ = inBlock.readByte();
        documentName_ = inBlock.readString(MAX_MESSAGE_ID_LEN);
        blockCount_ = inBlock.readUInt();
        
        std::vector<BlockMeta> stageBlocks;
        stageBlocks.reserve(blockCount_);
        
        for (size_t i = 0; i < blockCount_; ++i)
        {
            stageBlocks.emplace_back();
            auto & blk = stageBlocks.back();
            blk.name = inBlock.readString(MAX_BLOCK_ID_LENGTH);
            blk.len = inBlock.readUInt();
        }
        
        size_t blockOffset = inBlock.readPos();
        
        for (auto & blk : stageBlocks)
        {
            blocks_.emplace(blk.name, BlockReader(input, blockOffset, blk.len, blk.name));
            blockOffset += blk.len;
        }
        
        if (blockOffset > len)
            throw MalformedException("Incomplete message or wrong block-meta info");
    }
}
