//
//  DocumentWriter.h
//  pcos
//
//  Created by Gilbert Cheung on 8/28/13.
//  Copyright (c) 2013 PushCoin. All rights reserved.
//

#ifndef __pcos__DocumentWriter__
#define __pcos__DocumentWriter__

#include <iostream>
#include <vector>
#include "Common.h"
#include "BlockWriter.h"

namespace pcos {
    class DocumentWriter
    {
    public:
        using Blocks = std::vector<BlockWriter>;
 
    private:
        Blocks blocks_;
        BlockWriter writer_;
        std::string name_;
        
    public:
        DocumentWriter(std::string const & name, size_t bufSize = DOCUMENT_BUF_SIZE);
        
        template<typename... Args>
        BlockWriter & addBlock(Args&&... args)
        {
            blocks_.emplace_back(std::forward<Args>(args)...);
            return blocks_.back();
        }
        
        bool editable() const { return !writer_.size(); }
        
        // These functions will seal the writability of this class
        byte * bytes();
        size_t size();
        
    private:
        BlockWriter & write();
    };
}

#endif /* defined(__pcos__DocumentWriter__) */
