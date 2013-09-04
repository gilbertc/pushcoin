//
//  SettingsViewController.m
//  PushCoin
//
//  Created by Gilbert Cheung on 9/3/13.
//
//

#import "SettingsViewController.h"
#import "AppDelegate.h"
#import "NSString+HexStringToBytes.h"
#import "NSData+BytesToHexString.h"
#import "NSData+Base64.h"
#import "UIViewController+JASidePanel.h"
#import "SidePanelController.h"

#import <IASKSettingsReader.h>

@interface SettingsViewController ()
{
}
@end

@implementation SettingsViewController

-(id)init
{
    self = [super init];
    if (self)
    {
        self.showDoneButton = YES;
        self.showCreditsFooter = NO;
        self.delegate = self;
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
}


-(void) viewDidAppear:(BOOL)animated
{
    [self.appDelegate synchronizeDefaults];
    [[NSNotificationCenter defaultCenter] addObserver: self
                                             selector: @selector(settingsChanged:)
                                                 name: kIASKAppSettingChanged
                                               object: nil];
    [super viewDidAppear:animated];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


-(void) viewDidDisappear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}


- (AppDelegate *)appDelegate
{
    return (AppDelegate *)[[UIApplication sharedApplication] delegate];
}


- (void)settingsViewController:(IASKAppSettingsViewController *)sender buttonTappedForKey:(NSString *)key
{
    NSLog(@"%@", key);
    if ([key isEqualToString:@"KeyClearDevice"])
    {
        [self unregister];
    }
}


- (IBAction)unregister
{
    UIAlertView * alert = [[UIAlertView alloc] initWithTitle:@"Unregistering device"
                                                     message:@"Are you sure?"
                                                    delegate:self
                                           cancelButtonTitle:@"No"
                                           otherButtonTitles:@"Yes", nil];
    [alert show];
}


- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (buttonIndex == 1)
    {
        [self.appDelegate clearDevice];
        [self.appDelegate requestRegistrationWithDelegate:self.appDelegate.registrationController];
    }
}

- (void)settingsViewControllerDidEnd:(IASKAppSettingsViewController *)sender
{
    [self.sidePanelController showLeftPanelAnimated:YES];
}

- (void)settingsChanged:(id)key
{
}

@end
