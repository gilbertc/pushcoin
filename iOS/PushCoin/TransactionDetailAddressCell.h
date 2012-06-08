//
//  TransactionDetailAddressCell.h
//  PushCoin
//
//  Created by Gilbert Cheung on 6/8/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface TransactionDetailAddressCell : UITableViewCell
@property (nonatomic, weak) IBOutlet UILabel * streetLabel;
@property (nonatomic, weak) IBOutlet UILabel * cityLabel;
@property (nonatomic, weak) IBOutlet UILabel * countryLabel;
@end
