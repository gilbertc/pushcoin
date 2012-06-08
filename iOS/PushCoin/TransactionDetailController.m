//
//  TransactionDetailController.m
//  PushCoin
//
//  Created by Gilbert Cheung on 6/7/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "TransactionDetailController.h"
#import "TransactionDetailSenderCell.h"
#import "TransactionDetailAddressCell.h"
#import "TransactionDetailCell.h"


@interface TransactionDetailController ()
@end

@implementation TransactionDetailController
@synthesize entity;
@synthesize transaction;

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

    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
 
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
    
    NSLocale *usLocale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US"];
    
    dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDoesRelativeDateFormatting:YES];
    [dateFormatter setDateStyle:NSDateFormatterFullStyle];
    [dateFormatter setTimeZone:[NSTimeZone defaultTimeZone]];
    [dateFormatter setLocale:usLocale];
    
    timeFormatter = [[NSDateFormatter alloc] init];
    [timeFormatter setDoesRelativeDateFormatting:YES];
    [timeFormatter setTimeStyle:NSTimeZoneNameStyleShortGeneric];
    [timeFormatter setTimeZone:[NSTimeZone defaultTimeZone]];
    [timeFormatter setLocale:usLocale];
    
    numberFormatter = [[NSNumberFormatter alloc] init];
    [numberFormatter setFormatterBehavior:NSNumberFormatterBehavior10_4];
    [numberFormatter setCurrencySymbol:@"$"];
    [numberFormatter setNumberStyle:NSNumberFormatterCurrencyStyle];
    [numberFormatter setLocale:usLocale];
    
    self.navigationItem.title = self.transaction.merchantName;
    
    ABAddressBookRef addressBook = ABAddressBookCreate();
    ABRecordRef person = ABAddressBookGetPersonWithRecordID(addressBook, self.entity.recordID);
    
    if (person)
    {
        NSData  *imageData = (__bridge NSData *)ABPersonCopyImageData(person);
        image = [UIImage imageWithData:imageData];
    }
    else
    {
        image = nil;
    }
    
    CFRelease(addressBook);

    self.tableView.delegate = self;

}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
    
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Return the number of sections.
    return 5;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // Return the number of rows in the section.
    switch (section)
    {
        case 0:
            return self.entity? 1 : 0;
        case 1:
            return 3;
        case 2:
            return 4;
        case 3:
            return 1;
        case 4:
            return 2;
    }
    
    return 0;
}

-(NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    switch (section)
    {
        case 0:
            return @"";
        case 1:
            return @"Payment";
        case 2:
            return @"Transaction Details";
        case 3:
            return @"Address";
        case 4:
            return @"Contact";
    }
    return @"";
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    switch (indexPath.section)
    {
        case 0:
        {
            TransactionDetailSenderCell * cell = [self.tableView dequeueReusableCellWithIdentifier:@"TransactionDetailSenderCell"];
            if (!cell)
            {
                cell = [[TransactionDetailSenderCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"TransactionDetailSenderCell"];
            }
        
            if (self.transaction.transactionContext != 'P')
            {
                cell.image.image = image;
                cell.nameLabel.text = self.entity.name;
                cell.emailLabel.text = self.entity.email;
            }
            else
            {
                cell.image.image = nil;
                cell.nameLabel.text = self.transaction.merchantName;
                cell.emailLabel.text = @"";
            }
            return cell;
        }
            
        case 1:
        {
            TransactionDetailCell * cell = [self.tableView dequeueReusableCellWithIdentifier:@"TransactionDetailCell"];
            if (!cell)
            {
                cell = [[TransactionDetailCell alloc] initWithStyle:UITableViewCellStyleValue2 reuseIdentifier:@"TransactionDetailCell"];
            }
            
            cell.accessoryType = UITableViewCellAccessoryNone;
        
            switch (indexPath.row)
            {
                case 0:
                {
                    Float32 amount = self.transaction.paymentValue * pow(10.0f, (Float32)self.transaction.paymentScale);
                    if (self.transaction.transactionType == 'D')
                        amount *= -1.0f;
                
                    NSNumber *c = [NSNumber numberWithFloat:amount];
                
                    cell.textLabel.text = @"Amount";
                    cell.detailTextLabel.text = [numberFormatter stringFromNumber:c];
                    break;
                }
                case 1:
                {
                    Float32 tax = self.transaction.taxValue * pow(10.0f, (Float32)self.transaction.taxScale);
                    if (self.transaction.transactionType == 'D')
                        tax *= -1.0f;
                    
                    NSNumber *c = [NSNumber numberWithFloat:tax];
                    
                    cell.textLabel.text = @"Tax";
                    cell.detailTextLabel.text = [numberFormatter stringFromNumber:c];
                    break;
                }
                case 2:
                {
                    Float32 tips = self.transaction.tipValue * pow(10.0f, (Float32)self.transaction.tipScale);
                    if (self.transaction.transactionType == 'D')
                        tips *= -1.0f;
                    
                    NSNumber *c = [NSNumber numberWithFloat:tips];
                    
                    cell.textLabel.text = @"Tips";
                    cell.detailTextLabel.text = [numberFormatter stringFromNumber:c];
                    break;
                }
            }
            return cell;
        }
            
        case 2:
        {
            TransactionDetailCell * cell = [self.tableView dequeueReusableCellWithIdentifier:@"TransactionDetailCell"];
            if (!cell)
            {
                cell = [[TransactionDetailCell alloc] initWithStyle:UITableViewCellStyleValue2 reuseIdentifier:@"TransactionDetailCell"];
            }
            
            cell.accessoryType = UITableViewCellAccessoryNone;

            switch(indexPath.row)
            {
                case 0:
                {
                    NSDate * date = [NSDate dateWithTimeIntervalSince1970:self.transaction.timestamp];
                    cell.textLabel.text = @"Date";
                    cell.detailTextLabel.text = [dateFormatter stringFromDate:date];
                    break;
                }
                case 1:
                {
                    NSDate * date = [NSDate dateWithTimeIntervalSince1970:self.transaction.timestamp];
                    cell.textLabel.text = @"Time";
                    cell.detailTextLabel.text = [timeFormatter stringFromDate:date];
                    break;
                }
                case 2:
                {
                    cell.textLabel.text = @"Context";
                    cell.detailTextLabel.text = self.transaction.transactionContext == 'P' ? @"Payment" : @"Transfer";
                    break;
                }
                case 3:
                {
                    cell.textLabel.text = @"Invoice";
                    cell.detailTextLabel.text = self.transaction.invoice;
                    break;
                }
            }
            return cell;
        }
        case 3:
        {
            TransactionDetailAddressCell * cell = [self.tableView dequeueReusableCellWithIdentifier:@"TransactionDetailAddressCell"];
            if (!cell)
            {
                cell = [[TransactionDetailAddressCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"TransactionDetailAddressCell"];
            }
            
            cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
            
            cell.streetLabel.text = self.transaction.addressStreet;
            
            if (self.transaction.addressCity.length || self.transaction.addressState.length || self.transaction.addressZip.length)
            {
                cell.cityLabel.text = [NSString stringWithFormat:@"%@, %@ %@", 
                                       self.transaction.addressCity,
                                       self.transaction.addressState,
                                       self.transaction.addressZip];
            }
            else
            {
                cell.cityLabel.text = @""; 
            }
            
            cell.countryLabel.text = self.transaction.addressCountry;
            
            return cell;
        }
        case 4:
        {
            TransactionDetailCell * cell = [self.tableView dequeueReusableCellWithIdentifier:@"TransactionDetailCell"];
            if (!cell)
            {
                cell = [[TransactionDetailCell alloc] initWithStyle:UITableViewCellStyleValue2 reuseIdentifier:@"TransactionDetailCell"];
                
            }
            
            cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
            
            switch(indexPath.row)
            {
                case 0:
                {
                    cell.textLabel.text = @"Email";
                    cell.detailTextLabel.text = self.transaction.contactEmail;
                    break;
                }
                case 1:
                {
                    cell.textLabel.text = @"Phone";
                    cell.detailTextLabel.text = self.transaction.contactPhone;
                    break;
                }
            }
            return cell;
        }
    }
    return nil;
}

- (void) openEmail:(NSString*) recipient
{
    NSMutableString *emailBody = [[NSMutableString alloc] 
                                  initWithString:@"Powered By PushCoin."];
    
    //Create the mail composer window
    MFMailComposeViewController *controller = [[MFMailComposeViewController alloc] init];
    controller.mailComposeDelegate = self;
    controller.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
    
    [controller setSubject:@"PushCoin"];
    [controller setToRecipients:[NSArray arrayWithObject:recipient]];
    [controller setMessageBody:emailBody isHTML:NO];
    
    [self presentViewController:controller animated:YES completion:nil];
}

-(void)mailComposeController:(MFMailComposeViewController *)controller didFinishWithResult:(MFMailComposeResult)result error:(NSError *)error
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    switch (indexPath.section)
    {
        case 0:
            return 102;
        case 1:
            return 40;
        case 2:
            return 40;
        case 3:
            return 102;
        case 4:
            return 40;
    }
    return 0;
}

-(void)tableView:(UITableView *)tableView accessoryButtonTappedForRowWithIndexPath:(NSIndexPath *)indexPath
{
    [self tableView:tableView actOnItemAtIndexPath:indexPath];
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [self tableView:tableView actOnItemAtIndexPath:indexPath];
}

-(void)tableView:(UITableView *)tableView actOnItemAtIndexPath:(NSIndexPath *)indexPath
{
    switch(indexPath.section)
    {
        case 0:
        {
            if (self.entity.email && self.entity.email.length)
                [self openEmail:self.entity.email];
            break;
        }
        case 3:
        {
            break;
        }
            
        case 4:
        {
            switch (indexPath.row)
            {
                case 0:
                {
                    if (self.transaction.contactEmail && self.transaction.contactEmail.length)
                        [self openEmail:self.transaction.contactEmail];
                    break;
                }
                case 1:
                {
                    break;
                }
            }
        }
    }
    
}

@end
