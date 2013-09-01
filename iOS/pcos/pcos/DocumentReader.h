//
//  DocumentReader.h
//  pcos
//
//  Created by Gilbert Cheung on 8/28/13.
//  Copyright (c) 2013 PushCoin. All rights reserved.
//

#ifndef __pcos__DocumentReader__
#define __pcos__DocumentReader__

#include <string>
#include <unordered_map>
#include "Common.h"
#include "BlockReader.h"

namespace pcos {
    
    class DocumentReader
    {
    public:
        using BlockMap = std::unordered_map<std::string, BlockReader>;
        
    private:
        struct BlockMeta
        {
            size_t len;
            std::string name;
        };
   
    private:
        std::string magic_;
        byte pcosFlags_;
        std::string documentName_;
        size_t blockCount_;
        BlockMap blocks_;
        
    public:
        DocumentReader(byte const * input, size_t offset, size_t len);
        
        std::string const & getMagic() const { return magic_; }
        std::string const & getDocumentName() const { return documentName_; }
        size_t getBlockCount() const { return blockCount_; }
        
        BlockMap::iterator begin() { return blocks_.begin(); }
        BlockMap::iterator end() { return blocks_.end(); }
        BlockMap::iterator find(std::string const & name) { return blocks_.find(name); }
        BlockReader & operator[](std::string const & name) { return blocks_[name]; }
        
    };
}

#endif /* defined(__pcos__DocumentReader__) */
