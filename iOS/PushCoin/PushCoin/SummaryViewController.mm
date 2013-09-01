//
//  SummaryViewController.m
//  PushCoin
//
//  Created by Gilbert Cheung on 8/31/13.
//
//

#import "SummaryViewController.h"
#import "AppDelegate.h"

@interface SummaryViewController ()

@end

@implementation SummaryViewController
@synthesize balanceLabel;
@synthesize timestampLabel;
@synthesize scrollView;
@synthesize refreshControl;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
    
    }
    return self;
}

- (void)viewDidLoad
{
    NSLog(@"SummaryViewDidLoad");
    [super viewDidLoad];
    self.refreshControl = [[UIRefreshControl alloc] init];
    [self.refreshControl addTarget:self action:@selector(handleRefresh:) forControlEvents:UIControlEventValueChanged];
    self.refreshControl.attributedTitle = [[NSAttributedString alloc] initWithString:@"Pull to refresh"];
    [self.scrollView addSubview:self.refreshControl];
    
    [self.appDelegate.mainTabBarController registerMessageListener:self];
}

- (void)viewDidAppear:(BOOL)animated
{
    self.scrollView.contentSize = self.scrollView.frame.size;
    self.scrollView.contentInset = UIEdgeInsetsMake(0, 0, 0, 0);
}

-(void) handleRefresh:(UIRefreshControl *) refresher
{
    refresher.attributedTitle = [[NSAttributedString alloc] initWithString:@"Refreshing..."];
    [self.appDelegate.mainTabBarController refresh];
}

- (AppDelegate *)appDelegate
{
    return (AppDelegate *)[[UIApplication sharedApplication] delegate];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

- (void) messageDidUpdatedBy:(id <MessageUpdaterDelegate>)updater
{
    NSLog(@"ending");
    self.refreshControl.attributedTitle = [[NSAttributedString alloc] initWithString:@"Pull to refresh"];
    self.timestampLabel.text = [NSString stringWithFormat:@"As of %@", updater.timestamp];
    self.balanceLabel.text = updater.balance;
    [self.refreshControl endRefreshing];
}

@end

