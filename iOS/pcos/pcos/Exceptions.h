//
//  Exceptions.h
//  pcos
//
//  Created by Gilbert Cheung on 8/28/13.
//  Copyright (c) 2013 PushCoin. All rights reserved.
//

#ifndef pcos_Exceptions_h
#define pcos_Exceptions_h

#include <string>

namespace pcos {
    
    struct PcosException : public std::exception
    {
    public:
        PcosException(std::string const & what)
        : what_(what)
        { }
        
        const char * what() const noexcept override
        {
            return what_.c_str();
        }
        
    private:
        std::string what_;
    };
    
    struct MalformedException : public PcosException
    {
    public:
        MalformedException(std::string const & msg)
        : PcosException(msg)
        { }
        
        MalformedException()
        : PcosException("Malformed PCOS")
        { }
        
    };
    
    struct BadMagicException : public PcosException
    {
    public:
        BadMagicException()
        : PcosException("Bad Magic")
        { }
        
    };
}

#endif
