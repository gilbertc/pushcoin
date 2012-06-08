//
//  TransactionDetailController.h
//  PushCoin
//
//  Created by Gilbert Cheung on 6/7/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <AddressBook/AddressBook.h>
#import <MessageUI/MessageUI.h>
#import <MessageUI/MFMailComposeViewController.h>

#import "PushCoinAddressBook.h"
#import "PushCoinTransaction.h"

@interface TransactionDetailController : UITableViewController<MFMailComposeViewControllerDelegate>
{
    NSNumberFormatter * numberFormatter;
    NSDateFormatter * dateFormatter;
    NSDateFormatter * timeFormatter;
   
    UIImage * image;
}
@property (nonatomic, strong) PushCoinEntity * entity;
@property (nonatomic, strong) PushCoinTransaction * transaction;
@end
