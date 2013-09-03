//
//  LeftSidePanelViewController.m
//  PushCoin
//
//  Created by Gilbert Cheung on 9/2/13.
//
//

#import "LeftSidePanelViewController.h"
#import "UIViewController+JASidePanel.h"
#import "JASidePanelController.h"
#import "SidePanelController.h"
@interface LeftSidePanelViewController ()

@end

@implementation LeftSidePanelViewController
{
}

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
}

- (void)viewDidAppear:(BOOL)animated
{
    [self.tableView selectRowAtIndexPath:[NSIndexPath indexPathForRow:0 inSection:0]
                                animated:NO
                          scrollPosition:UITableViewScrollPositionTop];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return 3;
}

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.row == 0)
    {
        [(SidePanelController *) self.sidePanelController showSummary];
    }
    else if (indexPath.row == 1)
    {
        [(SidePanelController *) self.sidePanelController showHistory];
    }
}

@end
