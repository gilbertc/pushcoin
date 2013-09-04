//
//  HistoryViewController.m
//  PushCoin
//
//  Created by Gilbert Cheung on 8/31/13.
//
//

#import "HistoryViewController.h"
#import "AppDelegate.h"
#import "Transaction.h"
#import "MGScrollView.h"
#import "MGTableBoxStyled.h"
#import "MGLineStyled.h"
#import "TransactionBox.h"

@interface HistoryViewController ()
@end

@implementation HistoryViewController
{
    UIRefreshControl * refreshControl;
    MGBox * tablesGrid;
}
@synthesize scroller;

- (void)viewDidLoad
{
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
    [self.scroller addSubview:refreshControl];
    
    self.scroller.contentLayoutMode = MGLayoutGridStyle;
    self.scroller.bottomPadding = 8;
    
    CGSize tablesGridSize = (CGSize){320, 0};
    tablesGrid = [MGBox boxWithSize:tablesGridSize];
    tablesGrid.contentLayoutMode = MGLayoutTableStyle;
    tablesGrid.borderStyle = MGBorderNone;
    
    [self.scroller.boxes addObject:tablesGrid];
    [tablesGrid layout];
    
    [self.appDelegate.messageUpdater registerMessageListener:self];
}

-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self.scroller layout];
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
    // Dispose of any resources that can be recreated.
}

- (void)messageDidFailedBy:(id <MessageUpdaterDelegate>)updater withDescription:description
{
    refreshControl.attributedTitle = [[NSAttributedString alloc] initWithString:@"Refresh Failed"];
    [refreshControl endRefreshing];
}

- (void)messageDidUpdatedBy:(id<MessageUpdaterDelegate>)updater
{
    refreshControl.attributedTitle = [[NSAttributedString alloc] initWithString:@"Pull to refresh"];
    
    [tablesGrid.boxes removeAllObjects];
    for (Transaction * trx in updater.transactions)
    {
        TransactionBox * transactionBox = [TransactionBox transactionBoxFor:trx];
        [tablesGrid.boxes addObject:transactionBox];
    }
    
    TransactionBox * firstTrxBox = tablesGrid.boxes.firstObject;
    if (firstTrxBox != nil)
        [firstTrxBox expand];
    
    [tablesGrid layout];
    [self.scroller layout];
    [refreshControl endRefreshing];
}

@end
