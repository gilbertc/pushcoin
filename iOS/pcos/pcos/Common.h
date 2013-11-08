//
//  Common.h
//  pcos
//
//  Created by Gilbert Cheung on 8/28/13.
//  Copyright (c) 2013 PushCoin. All rights reserved.
//

#ifndef pcos_Common_h
#define pcos_Common_h

namespace pcos {
    using byte = uint8_t;

    enum {
        TYPE_WIRE_SIZE_DOUBLE = 8,
        MAX_MESSAGE_ID_LEN = 128,
        MAX_BLOCK_ID_LENGTH = 64,
        PROTOCOL_MAGIC_LEN = 4,
        PROTOCOL_FLAGS = 0x0,
        BLOCK_BUF_SIZE = 3000,
        DOCUMENT_BUF_SIZE = 3000
    };
    
    static const char * const PROTOCOL_MAGIC = "PCOS";
    
}

#endif
