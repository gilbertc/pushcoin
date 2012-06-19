//
//  ThirdViewController.h
//  PushCoin
//
//  Created by Gilbert Cheung on 4/20/12.
//  Copyright (c) 2012 PushCoin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PushCoinWebService.h"
#import "PushCoinMessages.h"
#import "KeychainItemWrapper.h"
#import "OpenSSLWrapper.h"
#import "RegistrationController.h"
#import "PasscodeViewController.h"
#import "InAppSettingsKit/Controllers/IASKAppSettingsViewController.h"


@interface SettingsController : IASKAppSettingsViewController< PushCoinWebServiceDelegate, PushCoinMessageReceiver,
    UIAlertViewDelegate, RegistrationControllerDelegate, KKPasscodeViewControllerDelegate, IASKSettingsDelegate>
{
    PushCoinMessageParser * parser;
    PushCoinWebService * webService;
    NSMutableData * buffer;
    KKPasscodeViewController * setPasscodeController;
    KKPasscodeViewController * preAuthTestPasscodeController;
}


@end
