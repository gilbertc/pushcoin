//
//  NSString+HexToBytes.h
//  PushCoin
//
//  Created by Gilbert Cheung on 5/13/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSString (HexStringToBytes)
-(NSData*) hexStringToBytes;
@end