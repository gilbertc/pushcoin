//
//  TransactionDetailSenderCell.h
//  PushCoin
//
//  Created by Gilbert Cheung on 6/7/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface TransactionDetailSenderCell : UITableViewCell
@property (nonatomic, weak) IBOutlet UIImageView * image;
@property (nonatomic, weak) IBOutlet UILabel * nameLabel;
@property (nonatomic, weak) IBOutlet UILabel * emailLabel;

@end
