//
//  FirstViewController.m
//  PushCoin
//
//  Created by Gilbert Cheung on 4/20/12.
//  Copyright (c) 2012 PushCoin. All rights reserved.
//
#import <QuartzCore/QuartzCore.h>
#import "PayController.h"
#import "PushCoinConfig.h"
#import "AppDelegate.h"


@implementation PayController
@synthesize gridView;

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    [[NSNotificationCenter defaultCenter] addObserver: self 
                                             selector: @selector(saveAndCleanup) 
                                                 name: @"handleCleanup" 
                                               object: nil];
    
   
    if ([[[NSFileManager alloc] init] fileExistsAtPath:self.dataFilePath] == YES)
        payments_ = [NSKeyedUnarchiver unarchiveObjectWithFile:self.dataFilePath];
    else
        payments_ = [[NSMutableArray alloc] init];    
    
    movingCell_ = NO;
    
    [self prepareNavigationBar];
    [self preparePaymentGrid];
    
    

}

-(NSString *) dataFilePath
{
    return [self.appDelegate.documentPath stringByAppendingPathComponent:@"payments.dat"];            
}
        
-(void)saveAndCleanup
{
    [NSKeyedArchiver archiveRootObject:payments_ toFile:self.dataFilePath];
}

-(void) prepareNavigationBar
{
    UIImageView * logoView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"title.png"]];
    logoView.contentMode = UIViewContentModeScaleAspectFit;
    logoView.bounds = CGRectInset(self.navigationController.navigationBar.bounds, 0, 5.0f);
    self.navigationItem.titleView = logoView;
}

-(void) preparePaymentGrid
{
    CGRect frame = self.view.bounds;
    self.gridView = [[GMGridView alloc] initWithFrame: frame];
    self.gridView.autoresizingMask = UIViewAutoresizingFlexibleHeight;
    self.gridView.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"shortcut_grid_background.png"]];  
    self.gridView.opaque = YES;
    self.gridView.alwaysBounceVertical = YES;
    self.gridView.centerGrid = NO;

    self.gridView.actionDelegate = self;
    self.gridView.sortingDelegate = self;
    self.gridView.dataSource = self;
    self.gridView.scrollEnabled = YES;
    [self.view addSubview:self.gridView];    
    [self.gridView reloadData];
}
- (void)viewDidUnload
{
    self.gridView = nil;
    
    [self setGridView:nil];
    [super viewDidUnload];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
}

- (void)viewWillDisappear:(BOOL)animated
{
	[super viewWillDisappear:animated];
    [self gridView].editing = NO;
    [self updateButtonStatus];
    [self saveAndCleanup];
}

- (void)viewDidDisappear:(BOOL)animated
{
	[super viewDidDisappear:animated];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation != UIInterfaceOrientationPortraitUpsideDown);
}

- (AppDelegate *)appDelegate
{
    return (AppDelegate *)[[UIApplication sharedApplication] delegate];
}

- (IBAction)push:(id)sender payment:(PushCoinPayment *)payment
{
    savedPayment_ = [payment copy];
    if (self.appDelegate.hasPasscode)
        [self.appDelegate requestPasscodeWithDelegate:self 
                                 navigationController:self.navigationController];
    else
        [self processPaymentWithPasscode:nil];
}

-(void)processPaymentWithPasscode:(NSString *)passcode
{
    PushCoinPayment * payment = savedPayment_;
    savedPayment_ = nil;
    
    if (payment)
    {
        QRViewController * controller = [self.appDelegate viewControllerWithIdentifier:@"QRViewController"];
    
        if (controller)
        {
            controller.delegate = self;
            controller.payment = [payment copy];
            controller.passcode = passcode;
            controller.modalTransitionStyle = UIModalTransitionStyleCoverVertical;
            [self presentModalViewController:controller animated:YES];
        }
    }
}

- (IBAction)editPayment:(id)sender 
{
    self.gridView.editing = YES;
    [self updateButtonStatus];
}

- (IBAction)editDonePayment:(id)sender 
{
    self.gridView.editing = NO;
    [self updateButtonStatus];
}

- (void) updateButtonStatus
{
    if (self.gridView.editing)
    {
        UIBarButtonItem * doneItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self action:@selector(editDonePayment:)];
        
        self.navigationItem.leftBarButtonItem = doneItem;
        
        UIBarButtonItem * addItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(addPayment:)];
        
        self.navigationItem.rightBarButtonItem = addItem;
    }
    else
    {
        UIBarButtonItem * editItem = [[UIBarButtonItem alloc] initWithTitle:@"Edit" style:UIBarButtonItemStyleBordered target:self action:@selector(editPayment:)];
        
        self.navigationItem.leftBarButtonItem = editItem;

        UIBarButtonItem * quickPayItem = [[UIBarButtonItem alloc] initWithTitle:@"Quick Pay" style:UIBarButtonItemStyleBordered target:self action:@selector(quickPayment:)];
        
        self.navigationItem.rightBarButtonItem = quickPayItem;
    }
}

- (IBAction)addPayment:(id)sender 
{
    AddPaymentController * controller = [self.appDelegate viewControllerWithIdentifier:@"AddPaymentController"];
    
    if (controller)
    {
        controller.delegate = self;
        [self.navigationController pushViewController:controller animated:YES];
    }
}

- (IBAction)quickPayment:(id)sender
{
    QuickPaymentController * controller = [self.appDelegate viewControllerWithIdentifier:@"QuickPaymentController"];
    if (controller)
    {
        controller.delegate = self;
        controller.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
        [self presentViewController:controller animated:YES completion:nil];
    }
}

#pragma mark -
#pragma mark QuickPaymentController
-(void)quickPaymentControllerDidClose:(QuickPaymentController *)controller
{
    [self dismissViewControllerAnimated:YES completion:^{
        if (controller.paymentValue != 0)
        {
            PushCoinPayment * payment = [[PushCoinPayment alloc] init];
            payment.amountValue = controller.paymentValue;
            payment.amountScale = controller.paymentScale;
            [self push:self payment:payment];
        }
    }];
}

-(void)quickPaymentControllerDidCancel:(QuickPaymentController *)controller
{
    [self dismissViewControllerAnimated:YES completion:nil];
}


#pragma mark -
#pragma mark AddPaymentController
-(void)addPaymentControllerDidAddPayment:(AddPaymentController *)controller
{
    if (controller.paymentValue != 0)
    {
        PushCoinPayment * payment = [[PushCoinPayment alloc] init];
        payment.amountValue = controller.paymentValue;
        payment.amountScale = controller.paymentScale;
        
        [payments_ addObject:payment];
        [self.gridView insertObjectAtIndex:payments_.count-1 withAnimation:GMGridViewItemAnimationFade];
    }
    [self.navigationController popToViewController:self animated:YES];
}

#pragma mark -
#pragma mark QRController
-(void)qrViewControllerDidClose:(QRViewController *)controller
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

//////////////////////////////////////////////////////////////
#pragma mark GMGridViewDataSource
//////////////////////////////////////////////////////////////

- (NSInteger)numberOfItemsInGMGridView:(GMGridView *)view
{
    return payments_.count;   
}

- (CGSize)GMGridView:(GMGridView *)view sizeForItemsInInterfaceOrientation:(UIInterfaceOrientation)orientation
{
    return ( CGSizeMake(150, 80) );
}

- (GMGridViewCell *)GMGridView:(GMGridView *)view cellForItemAtIndex:(NSInteger)index
{
    PaymentCell * cell = (PaymentCell *)[view dequeueReusableCell];
    if (!cell)
        cell = [[PaymentCell alloc] initWithFrame:CGRectMake(0.0, 0.0, 150.0, 80.0)];
    
    cell.payment = [[payments_ objectAtIndex:index] copy];
    return ( cell );
}


- (BOOL)GMGridView:(GMGridView *)gridView canDeleteItemAtIndex:(NSInteger)index
{
    return YES;
}



//////////////////////////////////////////////////////////////
#pragma mark GMGridViewActionDelegate
//////////////////////////////////////////////////////////////

- (void)GMGridView:(GMGridView *)view didTapOnItemAtIndex:(NSInteger)index;
{
    if (!movingCell_)
    {
        PaymentCell * cell = (PaymentCell *) [view cellForItemAtIndex:index];
        if (cell)
            [self push:view payment:cell.payment];
    }
}

-(void) GMGridViewDidTapOnEmptySpace:(GMGridView *)gridView
{
    self.gridView.editing = NO;
   [self updateButtonStatus];
}


- (void)GMGridView:(GMGridView *)gridView processDeleteActionForItemAtIndex:(NSInteger)index
{
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Confirm" 
                                                    message:@"Are you sure you want to delete this payment?" 
                                                   delegate:self 
                                          cancelButtonTitle:@"Cancel" 
                                          otherButtonTitles:@"Delete", nil];
    
    [alert show];
    lastDeleteItemIndexAsked_ = index;
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (buttonIndex == 1) 
    {
        [payments_ removeObjectAtIndex:lastDeleteItemIndexAsked_];
        [self.gridView removeObjectAtIndex:lastDeleteItemIndexAsked_ withAnimation:GMGridViewItemAnimationFade];
    }
}


//////////////////////////////////////////////////////////////
#pragma mark GMGridViewSortingDelegate
//////////////////////////////////////////////////////////////

- (void)GMGridView:(GMGridView *)gridView didStartMovingCell:(GMGridViewCell *)cell
{
    movingCell_ = YES;
}

- (void)GMGridView:(GMGridView *)gridView didEndMovingCell:(GMGridViewCell *)cell
{
    movingCell_ = NO;
}

- (BOOL)GMGridView:(GMGridView *)gridView shouldAllowShakingBehaviorWhenMovingCell:(GMGridViewCell *)cell atIndex:(NSInteger)index
{
    return YES;
}

- (void)GMGridView:(GMGridView *)gridView moveItemAtIndex:(NSInteger)oldIndex toIndex:(NSInteger)newIndex
{
    NSObject *object = [payments_ objectAtIndex:oldIndex];
    [payments_ removeObject:object];
    [payments_ insertObject:object atIndex:newIndex];
}

- (void)GMGridView:(GMGridView *)gridView exchangeItemAtIndex:(NSInteger)index1 withItemAtIndex:(NSInteger)index2
{
    [payments_ exchangeObjectAtIndex:index1 withObjectAtIndex:index2];
}
- (BOOL) validatePasscode:(NSString *)passcode
{
    return [self.appDelegate validatePasscode:passcode];
}

- (void)didPasscodeEnteredCorrectly:(KKPasscodeViewController*)viewController
{
    [self.navigationController popToViewController:self animated:YES];
    [self processPaymentWithPasscode:viewController.passcode];
}

- (void)didPasscodeEnteredIncorrectly:(KKPasscodeViewController*)viewController
{
    [self.navigationController popToViewController:self animated:YES];
}




@end
