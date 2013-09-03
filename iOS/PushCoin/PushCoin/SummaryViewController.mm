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
{
    UIRefreshControl * refreshControl;
}

@synthesize balanceLabel;
@synthesize timestampLabel;
@synthesize scrollView;


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
    refreshControl = [[UIRefreshControl alloc] init];
    [refreshControl addTarget:self action:@selector(handleRefresh:) forControlEvents:UIControlEventValueChanged];
    refreshControl.attributedTitle = [[NSAttributedString alloc] initWithString:@"Pull to refresh"];
    [self.scrollView addSubview:refreshControl];
    
    [self.appDelegate.messageUpdater registerMessageListener:self];
}

- (void)viewDidAppear:(BOOL)animated
{
    self.scrollView.contentSize = self.scrollView.frame.size;
    self.scrollView.contentInset = UIEdgeInsetsMake(0, 0, 0, 0);
}

-(void) handleRefresh:(UIRefreshControl *) refresher
{
    refresher.attributedTitle = [[NSAttributedString alloc] initWithString:@"Refreshing..."];
    [self.appDelegate.messageUpdater refresh];
}

- (AppDelegate *)appDelegate
{
    return (AppDelegate *)[[UIApplication sharedApplication] delegate];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

- (void)messageDidFailedBy:(id <MessageUpdaterDelegate>)updater withDescription:description
{
    refreshControl.attributedTitle = [[NSAttributedString alloc] initWithString:@"Refresh Failed"];
    [refreshControl endRefreshing];
}

- (void) messageDidUpdatedBy:(id <MessageUpdaterDelegate>)updater
{
    refreshControl.attributedTitle = [[NSAttributedString alloc] initWithString:@"Pull to refresh"];
    self.timestampLabel.text = [NSString stringWithFormat:@"As of %@", updater.timestamp];
    self.balanceLabel.text = updater.balance;
    [refreshControl endRefreshing];
}

@end

