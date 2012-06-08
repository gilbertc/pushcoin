//
//  ReceiveNavigationController.h
//  PushCoin
//
//  Created by Gilbert Cheung on 6/4/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <QuartzCore/QuartzCore.h>
#import <ZXingWidgetController.h>
#import <QRCodeReader.h>


@interface ReceiveNavigationController : UINavigationController<ZXingDelegate>
@property (strong, nonatomic) ZXingWidgetController * zxingController;

@end
