//
//  SummaryViewController.m
//  PushCoin
//
//  Created by Gilbert Cheung on 8/31/13.
//
//

#import "SummaryViewController.h"
#import "Transaction.h"
#import "AppDelegate.h"
#import "Common.h"

@interface SummaryViewController ()

@end

@implementation SummaryViewController
{
    UIRefreshControl * refreshControl;
}

@synthesize balanceLabel;
@synthesize balanceDecimalLabel;
@synthesize lastPaymentLabel;
@synthesize lastAddressLabel;
@synthesize lastCounterPartyLabel;
@synthesize lastTimeLabel;
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

    // Title Bar
    UIImageView *imageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"pushcoin.png"]];
    imageView.contentMode = UIViewContentModeScaleAspectFit;
    CGRect newFrame = imageView.frame;
    newFrame.size.height = 24;
    imageView.frame = newFrame;
    self.navigationItem.titleView = imageView;
    
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
    
    double balance = updater.balance.doubleValue;
    
    self.balanceLabel.text = [NSString stringWithFormat:@"%d", (int) balance];
    self.balanceDecimalLabel.text = [NSString stringWithFormat:@"%d", (int)(balance * 100) - ((int)balance) * 100];
    
    if (updater.transactions.count > 0)
    {
        Transaction * trx = ((Transaction *)[updater.transactions objectAtIndex:0]);
        self.lastCounterPartyLabel.text = trx.counterPartyName;
        self.lastPaymentLabel.text = trx.payment.text;
        self.lastTimeLabel.text = UtcTimestampToString(trx.utcTransactionTime);
        
        if (trx.address != nil)
            self.lastAddressLabel.text = [NSString stringWithFormat:@"%@, %@", trx.address.city, trx.address.state];
        else
            self.lastAddressLabel.text = @"";
        
    }
    else
    {
        self.lastCounterPartyLabel.text = @"No Activity";
        self.lastAddressLabel.text = @"";
        self.lastPaymentLabel.text = @"";
        self.lastTimeLabel.text = @"";
    }
    
    [refreshControl endRefreshing];
}

@end

