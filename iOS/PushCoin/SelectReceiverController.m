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
#import "EmailBook.h"

@implementation SelectReceiverController
@synthesize receiverTableView;
@synthesize dataStore;
@synthesize delegate;
@synthesize receiver;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
     
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
    self.dataStore = [[self.appDelegate.emailBook.dataStore allValues] copy];
    self.dataStore = [self.dataStore sortedArrayUsingComparator:
                      ^NSComparisonResult(Entity * a, Entity * b)
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
    return [self.appDelegate.emailBook.dataStore count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    ReceiverCell * cell = [self.receiverTableView dequeueReusableCellWithIdentifier:@"ReceiverCell"];
    if (!cell)
    {
        cell = [[ReceiverCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:@"ReceiverCell"];
    }
    
    Entity * entity = [self.dataStore objectAtIndex:indexPath.row];
     
    cell.textLabel.text = entity.name;
    cell.detailTextLabel.text = entity.email;
        
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    self.receiver = [self.dataStore objectAtIndex:indexPath.row];
    [self.delegate selectReceiverControllerDidClose:self];
}

@end
