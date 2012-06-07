//
//  QRViewController.h
//  PushCoin
//
//  Created by Gilbert Cheung on 4/21/12.
//  Copyright (c) 2012 PushCoin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PushCoinMessages.h"
#import "PushCoinPayment.h"
#import "PaymentDetailsController.h"
#import "SelectReceiverController.h"

@class QRViewController;
@class Entity;

@protocol QRViewControllerDelegate <NSObject>

- (void)qrViewControllerDidClose:
(QRViewController *)controller;

@end

@interface QRViewController : UIViewController<PaymentDetailsControllerDelegate, SelectReceiverControllerDelegate, UIActionSheetDelegate>

@property (nonatomic, strong) PushCoinMessageParser * parser;
@property (nonatomic, strong) NSMutableData * buffer;
@property (nonatomic, strong) PushCoinPayment * payment;
@property (nonatomic, strong) Entity * receiver;
@property (nonatomic, weak) id <QRViewControllerDelegate> delegate;
@property (weak, nonatomic) IBOutlet UIImageView *imageView;
@property (weak, nonatomic) IBOutlet UINavigationBar *navigationBar;
@property (nonatomic, strong) NSString * passcode;
@property (weak, nonatomic) IBOutlet UILabel *amountLabel;
@property (weak, nonatomic) IBOutlet UILabel *centLabel;
@property (weak, nonatomic) IBOutlet UILabel *tipLabel;
@property (weak, nonatomic) IBOutlet UIToolbar *toolbar;
@property (weak, nonatomic) IBOutlet UILabel *receiverLabel;

- (IBAction)closeButtonTapped:(id)sender;
- (IBAction)addTipsButtonTapped:(id)sender;
- (IBAction)actionButtonTapped:(id)sender;


@end
