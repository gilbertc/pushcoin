//
//  SecondViewController.h
//  PushCoin
//
//  Created by Gilbert Cheung on 4/20/12.
//  Copyright (c) 2012 PushCoin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <QuartzCore/QuartzCore.h>

#import "PushCoinWebService.h"
#import "PushCoinMessages.h"

@interface ReceiveController : UIViewController<PushCoinWebServiceDelegate, PushCoinMessageReceiver, UITextFieldDelegate, CLLocationManagerDelegate>
{
    NSNumberFormatter * numberFormatter;
    PushCoinMessageParser * parser;
    PushCoinWebService * webService;
    NSMutableData * buffer;
    NSMutableString * storedValue;
}

@property (weak, nonatomic) IBOutlet UITextField *paymentTextField;
@property (strong, nonatomic) NSData * ptaData;

- (IBAction)backgroundTouched:(id)sender;
- (IBAction)submitButtonTapped:(id)sender;

@end
