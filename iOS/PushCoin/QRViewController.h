//
//  QRViewController.h
//  PushCoin
//
//  Created by Gilbert Cheung on 4/21/12.
//  Copyright (c) 2012 PushCoin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MessageUI/MessageUI.h>
#import <MessageUI/MFMailComposeViewController.h>

#import "PushCoinMessages.h"
#import "PushCoinPayment.h"
#import "PaymentDetailsController.h"
#import "SelectReceiverController.h"

@class QRViewController;
@class PushCoinEntity;

@protocol QRViewControllerDelegate <NSObject>
- (void)qrViewControllerDidClose:(QRViewController *)controller;
@end

@interface QRViewController : UIViewController<PaymentDetailsControllerDelegate, SelectReceiverControllerDelegate, UIActionSheetDelegate, MFMailComposeViewControllerDelegate>
{
    CGFloat savedBrightness;
    BOOL sendEmailAfterReceiverSet;
}

@property (weak, nonatomic) IBOutlet UILabel *receiverLabel;
@property (weak, nonatomic) IBOutlet UILabel *expirationLabel;
@property (weak, nonatomic) IBOutlet UIView *receiverBackground;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *actionBarButton;

@property (nonatomic, strong) PushCoinMessageParser * parser;
@property (nonatomic, strong) NSMutableData * buffer;
@property (nonatomic, strong) PushCoinPayment * payment;
@property (nonatomic, strong) PushCoinEntity * receiver;
@property (nonatomic, weak) id <QRViewControllerDelegate> delegate;
@property (weak, nonatomic) IBOutlet UIImageView *imageView;
@property (weak, nonatomic) IBOutlet UINavigationBar *navigationBar;
@property (nonatomic, strong) NSString * passcode;
@property (weak, nonatomic) IBOutlet UILabel *amountLabel;
@property (weak, nonatomic) IBOutlet UILabel *centLabel;
@property (weak, nonatomic) IBOutlet UILabel *tipLabel;
@property (nonatomic, assign) SInt64 expiration;
@property (nonatomic, assign) SInt64 ttl;
@property (strong, nonatomic) NSTimer * timer;


- (IBAction)closeButtonTapped:(id)sender;
- (IBAction)actionButtonTapped:(id)sender;


@end
