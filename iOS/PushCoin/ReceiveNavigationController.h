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
#import <CoreLocation/CoreLocation.h>


@interface ReceiveNavigationController : UINavigationController<ZXingDelegate, CLLocationManagerDelegate>
{
    CLLocationManager * locationManager;
}

@property (strong, nonatomic) ZXingWidgetController * zxingController;
@property (strong, nonatomic) CLLocation * lastKnownLocation;

-(void) handleURL:(NSURL *) url;
@end
