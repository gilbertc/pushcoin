//
//  UILabel+CustomFont.m
//  PushCoin
//
//  Created by Gilbert Cheung on 9/1/13.
//
//

#import "UILabel+CustomFont.h"

@implementation UILabel (CustomFont)

- (NSString *)fontName {
    return self.font.fontName;
}

- (void)setFontName:(NSString *)fontName {
    self.font = [UIFont fontWithName:fontName size:self.font.pointSize];
}
@end
