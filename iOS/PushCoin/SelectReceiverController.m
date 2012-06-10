//
//  SelectReceiverController.m
//  PushCoin
//
//  Created by Gilbert Cheung on 5/24/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "SelectReceiverController.h"
#import "ReceiverCell.h"
#import "AppDelegate.h"

@implementation SelectReceiverController
@synthesize receiverTableView;
@synthesize dataStore;
@synthesize delegate;
@synthesize receiver;
@synthesize allowAnyOne;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) 
    {
        self.allowAnyOne = YES;
    }
    return self;
}

- (AppDelegate *)appDelegate
{
    return (AppDelegate *)[[UIApplication sharedApplication] delegate];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    self.receiverTableView.delegate = self;
    self.receiverTableView.dataSource = self;
    self.dataStore = [[self.appDelegate.addressBook.dataStore allValues] copy];
    self.dataStore = [self.dataStore sortedArrayUsingComparator:
                      ^NSComparisonResult(PushCoinEntity * a, PushCoinEntity * b)
                      {
                          return [a.name localizedCaseInsensitiveCompare:b.name];
                      }];
}

- (void)viewDidUnload
{
    [self setReceiverTableView:nil];
    [self setDataStore:nil];
    [self setDataStore:nil];
    [super viewDidUnload];
    // Release any retained subviews of the main view.
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

- (IBAction)cancelButtonTapped:(id)sender
{
    [self.delegate selectReceiverControllerDidCancel:self];
}

#pragma mark UITableViewDelegates

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.appDelegate.addressBook.dataStore count] + (self.allowAnyOne ? 1: 0);
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell * ret;
    
    if (indexPath.row == 0 && self.allowAnyOne)
    {
        ReceiverCell * cell = [self.receiverTableView dequeueReusableCellWithIdentifier:@"AnyReceiverCell"];
        if (!cell)
        {
            cell = [[ReceiverCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"AnyReceiverCell"];
        }
        
        cell.textLabel.text = @"Any One";
        ret = cell;
    }
    else
    {
        ReceiverCell * cell = [self.receiverTableView dequeueReusableCellWithIdentifier:@"ReceiverCell"];
        if (!cell)
        {
            cell = [[ReceiverCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:@"ReceiverCell"];
        }
    
        PushCoinEntity * entity = [self.dataStore objectAtIndex:indexPath.row - (self.allowAnyOne ? 1: 0)];
     
        cell.textLabel.text = entity.name;
        cell.detailTextLabel.text = entity.email;
        
        ret = cell;
    }
    return ret;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.row == 0 && self.allowAnyOne)
    {
        self.receiver = nil;
    }
    else
    {
        self.receiver = [self.dataStore objectAtIndex:indexPath.row - (self.allowAnyOne ? 1: 0)];
    }
    
    [self.delegate selectReceiverControllerDidClose:self];
}

@end
