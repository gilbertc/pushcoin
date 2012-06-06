//
//  QuickPaymentController.h
//  PushCoin
//
//  Created by Gilbert Cheung on 6/5/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

@class QuickPaymentController;
@protocol QuickPaymentControllerDelegate <NSObject>

- (void) quickPaymentControllerDidClose:(QuickPaymentController *)controller;
- (void) quickPaymentControllerDidCancel:(QuickPaymentController *)controller;


@end

@interface QuickPaymentController : UIViewController<UITextFieldDelegate>
{
    NSMutableString * storedValue;
    NSNumberFormatter * numberFormatter;
}
@property (weak, nonatomic) IBOutlet UITextField *paymentTextField;
@property (weak, nonatomic) IBOutlet UIButton *quickPayButton;
@property (nonatomic, readonly) NSUInteger paymentValue;
@property (nonatomic, readonly) NSInteger paymentScale;
@property (weak, nonatomic) NSObject<QuickPaymentControllerDelegate> * delegate;


- (IBAction)cancelButtonTapped:(id)sender;
- (IBAction)quickPayButtonTapped:(id)sender;
- (IBAction)backgroundTapped:(id)sender;

@end
