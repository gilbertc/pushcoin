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
    
    UIView *backgroundSelectedCell = [[UIView alloc] init];
    [backgroundSelectedCell setBackgroundColor:[UIColor colorWithRed:80/256.0 green:80/256.0 blue:80/256.0 alpha:1.0]];
    
    for (int section = 0; section < [self.tableView numberOfSections]; section++)
    {
        for (int row = 0; row < [self.tableView numberOfRowsInSection:section]; row++)
        {
            NSIndexPath* cellPath = [NSIndexPath indexPathForRow:row inSection:section];
            UITableViewCell* cell = [self.tableView cellForRowAtIndexPath:cellPath];
            
            [cell setSelectedBackgroundView:backgroundSelectedCell];
        }
    }
}

- (void)viewDidAppear:(BOOL)animated
{
    self.tableView.delegate = self;
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
    return 2;
}

-(void)tableView:(UITableView *)tableView willDisplayCell:(UITableViewCell *)cell forRowAtIndexPath:(NSIndexPath *)indexPath
{
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
