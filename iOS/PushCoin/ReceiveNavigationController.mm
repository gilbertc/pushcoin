//
//  ReceiveNavigationController.m
//  PushCoin
//
//  Created by Gilbert Cheung on 6/4/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "ReceiveNavigationController.h"
#import "ReceiveController.h"
#import "AppDelegate.h"

@interface ReceiveNavigationController ()

@end

@implementation ReceiveNavigationController
@synthesize zxingController;
@synthesize lastKnownLocation;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
      
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
 
    self.lastKnownLocation = nil;
    locationManager = [[CLLocationManager alloc] init];
    locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters;
    locationManager.distanceFilter = 100.0f;
    locationManager.delegate = self;
    [locationManager startUpdatingLocation];
    
    self.zxingController = [[ZXingWidgetController alloc] initWithDelegate:self
                                                                showCancel:NO 
                                                                  OneDMode:NO];
    
    QRCodeReader* qrcodeReader = [[QRCodeReader alloc] init];
    self.zxingController.readers = [[NSSet alloc] initWithObjects:qrcodeReader, nil];
    self.zxingController.navigationItem.title = @"Scan Payment";
    self.zxingController.overlayView.displayedMessage = @"";
       
    [self pushViewController:self.zxingController animated:NO];    
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    [locationManager stopUpdatingLocation];
    
    locationManager = nil;
    self.lastKnownLocation = nil;

    self.zxingController = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}


- (AppDelegate *)appDelegate
{
    return (AppDelegate *)[[UIApplication sharedApplication] delegate];
}

#pragma mark -
#pragma mark ZXingDelegateMethods

- (void)zxingController:(ZXingWidgetController*)controller didScanResult:(NSData*)data
{
    AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
    
    ReceiveController * receiveController = [self.appDelegate viewControllerWithIdentifier:@"ReceiveController"];
    if (receiveController)
    {
        receiveController.ptaData = data;
        [self pushViewController:receiveController animated:YES];
    }
}

- (void)zxingControllerDidCancel:(ZXingWidgetController*)controller 
{
}

#pragma mark -
#pragma mark CLLocationManagerDelegate

-(void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation
{
    self.lastKnownLocation = [newLocation copy];
}



@end
