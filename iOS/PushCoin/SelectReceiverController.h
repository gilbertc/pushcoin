//
//  SelectReceiverController.h
//  PushCoin
//
//  Created by Gilbert Cheung on 6/7/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//


#import <UIKit/UIKit.h>
#import "PushCoinAddressBook.h"

@class SelectReceiverController;

@protocol SelectReceiverControllerDelegate <NSObject>

- (void)selectReceiverControllerDidClose:
(SelectReceiverController *)controller;
- (void)selectReceiverControllerDidCancel:
(SelectReceiverController *)controller;

@end


@interface SelectReceiverController : UIViewController<UITableViewDelegate, UITableViewDataSource>
@property (nonatomic, weak) NSObject<SelectReceiverControllerDelegate> * delegate;
@property (nonatomic, strong) NSArray * dataStore;
@property (weak, nonatomic) IBOutlet UITableView *receiverTableView;
@property (nonatomic, strong) PushCoinEntity * receiver;
@property (nonatomic, assign) BOOL allowAnyOne;
- (IBAction)cancelButtonTapped:(id)sender;
@end
