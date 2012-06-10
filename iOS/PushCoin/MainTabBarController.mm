//
//  MainTabBarControllerViewController.m
//  PushCoin
//
//  Created by Gilbert Cheung on 5/15/12.
//  Copyright (c) 2012 PushCoin. All rights reserved.
//

#import "MainTabBarController.h"
#import "ReceiveNavigationController.h"
#import "AppDelegate.h"

@interface MainTabBarController ()

@end

@implementation MainTabBarController
@synthesize tabBar;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
}

- (void)viewDidUnload
{
    [self setTabBar:nil];
    [super viewDidUnload];
    // Release any retained subviews of the main view.
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:(BOOL)animated];
    [self.appDelegate requestRegistrationWithDelegate:self];
}

-(void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];

}

-(void) handleURL:(NSURL *)url
{
    if ([url isFileURL])
    {
        //handle file
        [self dismissModalViewControllerAnimated:NO];
        [self.appDelegate requestRegistrationWithDelegate:self];
        
        ReceiveNavigationController * controller = (ReceiveNavigationController *) [self.viewControllers objectAtIndex:1];
        if (controller)
        {
            [controller handleURL:url];
            [self setSelectedViewController:controller];
        }
    }
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

        
- (AppDelegate *)appDelegate
{
    return (AppDelegate *)[[UIApplication sharedApplication] delegate];
}


-(void) registrationControllerDidClose:(RegistrationController *)controller
{
    [self dismissModalViewControllerAnimated:YES];
}


@end
