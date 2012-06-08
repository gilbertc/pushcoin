//
//  TransactionDetailAddressCell.m
//  PushCoin
//
//  Created by Gilbert Cheung on 6/8/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "TransactionDetailAddressCell.h"

@implementation TransactionDetailAddressCell
@synthesize streetLabel;
@synthesize cityLabel;
@synthesize countryLabel;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
