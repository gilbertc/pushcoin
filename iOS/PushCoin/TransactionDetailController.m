//
//  TransactionDetailController.m
//  PushCoin
//
//  Created by Gilbert Cheung on 6/7/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "TransactionDetailController.h"
#import "TransactionDetailSenderCell.h"
#import "TransactionDetailCell.h"

#import "NSString+URLEncoding.h"

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
    
    self.navigationItem.title = self.transaction.transactionContext == 'T' ? @"Transfer" : @"Payment";
    
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
    
    if (self.transaction.transactionContext == 'T')
        return 4;
    else
        return 4;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // Return the number of rows in the section.
    switch (section)
    {
        case 0:
            return 1;
        case 1:
            return 3;
        case 2:
            return self.transaction.transactionContext == 'T' ? 4 : 5; //skipping invoice 
    }
    
    if (self.transaction.transactionContext == 'T')
    {
        switch (section)
        {
            case 3:
                return 1;
        }
    }
    else
    {
        switch (section)
        {
            case 3:
                return 3;
        }
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
            return @"Transaction";
    }
    
    if (self.transaction.transactionContext == 'T')
    {
        switch (section)
        {
            case 3:
                return @"Location";
        }
    }
    else
    {
        switch (section)
        {
            case 3:
                return @"Merchant Info";
        }
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
        
            cell.selectionStyle = UITableViewCellSelectionStyleNone;
            cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
            
            if (self.transaction.transactionContext == 'T')
            {
                cell.image.image = image;
                cell.nameLabel.text = self.entity.name.length? self.entity.name : self.transaction.merchantName;
                cell.emailLabel.text = self.entity.email.length? self.entity.email : self.transaction.contactEmail;
            }
            else
            {
                cell.image.image = nil;
                cell.nameLabel.text = self.transaction.merchantName;
                cell.emailLabel.text = self.transaction.contactEmail;
            }
            return cell;
        }
            
        case 1:
        {
            TransactionDetailCell * cell = [self.tableView dequeueReusableCellWithIdentifier:@"TransactionDetailCell"];
            if (!cell)
            {
                cell = [[TransactionDetailCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:@"TransactionDetailCell"];
            }
            
            cell.accessoryType = UITableViewCellAccessoryNone;
            cell.selectionStyle = UITableViewCellSelectionStyleNone;
        
            switch (indexPath.row)
            {
                case 0:
                {
                    Float32 amount = self.transaction.paymentValue * pow(10.0f, (Float32)self.transaction.paymentScale);
                    if (self.transaction.transactionType == 'D')
                        amount *= -1.0f;
                
                    NSNumber *c = [NSNumber numberWithFloat:amount];
                
                    cell.textLabel.text = @"amount";
                    cell.detailTextLabel.text = [numberFormatter stringFromNumber:c];
                    break;
                }
                case 1:
                {
                    Float32 tax = self.transaction.taxValue * pow(10.0f, (Float32)self.transaction.taxScale);
                    if (self.transaction.transactionType == 'D')
                        tax *= -1.0f;
                    
                    NSNumber *c = [NSNumber numberWithFloat:tax];
                    
                    cell.textLabel.text = @"tax";
                    cell.detailTextLabel.text = [numberFormatter stringFromNumber:c];
                    break;
                }
                case 2:
                {
                    Float32 tips = self.transaction.tipValue * pow(10.0f, (Float32)self.transaction.tipScale);
                    if (self.transaction.transactionType == 'D')
                        tips *= -1.0f;
                    
                    NSNumber *c = [NSNumber numberWithFloat:tips];
                    
                    cell.textLabel.text = @"tips";
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
                cell = [[TransactionDetailCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:@"TransactionDetailCell"];
            }
            
            cell.accessoryType = UITableViewCellAccessoryNone;
            cell.selectionStyle = UITableViewCellSelectionStyleNone;

            switch(indexPath.row)
            {
                case 0:
                {
                    NSDate * date = [NSDate dateWithTimeIntervalSince1970:self.transaction.timestamp];
                    cell.textLabel.text = @"date";
                    cell.detailTextLabel.text = [dateFormatter stringFromDate:date];
                    break;
                }
                case 1:
                {
                    NSDate * date = [NSDate dateWithTimeIntervalSince1970:self.transaction.timestamp];
                    cell.textLabel.text = @"time";
                    cell.detailTextLabel.text = [timeFormatter stringFromDate:date];
                    break;
                }
                case 2:
                {
                    cell.textLabel.text = @"context";
                    cell.detailTextLabel.text = self.transaction.transactionContext == 'P' ? @"Payment" : @"Transfer";
                    break;
                }
                case 3:
                {
                    cell.textLabel.text = @"recipient";
                    cell.detailTextLabel.text = self.transaction.recipient;
                    break;
                }
                case 4:
                {
                    cell.textLabel.text = @"invoice";
                    cell.detailTextLabel.text = self.transaction.invoice;
                    break;
                }
            }
            return cell;
        }
    }        
    
    if (self.transaction.transactionContext == 'T')
    {
        switch (indexPath.section)
        {
            case 3:
            {
                TransactionDetailCell * cell = [self.tableView dequeueReusableCellWithIdentifier:@"TransactionDetailLatLongCell"];
                if (!cell)
                {
                    cell = [[TransactionDetailCell alloc] initWithStyle:UITableViewCellStyleValue2 reuseIdentifier:@"TransactionDetailLatLongCell"];
                }
                
                cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
                cell.selectionStyle = UITableViewCellSelectionStyleNone;
                cell.textLabel.text = @"lat/long";
                
                cell.detailTextLabel.text = [NSString stringWithFormat:@"%f\n%f",
                                             self.transaction.latitude,
                                             self.transaction.longitude];
                
                cell.detailTextLabel.numberOfLines = 2;
                cell.detailTextLabel.lineBreakMode = UILineBreakModeWordWrap;
                
                return cell;
            }
        }
    }
    else
    {
        switch (indexPath.section)
        {
            case 3:
            {
                if (indexPath.row == 0)
                {
                    TransactionDetailCell * cell = [self.tableView dequeueReusableCellWithIdentifier:@"TransactionDetailAddressCell"];
                    if (!cell)
                    {
                        cell = [[TransactionDetailCell alloc] initWithStyle:UITableViewCellStyleValue2 reuseIdentifier:@"TransactionDetailAddressCell"];
                    }
            
                    cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
                    cell.selectionStyle = UITableViewCellSelectionStyleNone;
                    cell.textLabel.text = @"address";
                
                    if (self.transaction.addressCity.length 
                        || self.transaction.addressState.length 
                        || self.transaction.addressZip.length
                        || self.transaction.addressStreet.length
                        || self.transaction.addressCountry.length)
                    {
                        cell.detailTextLabel.text = [NSString stringWithFormat:@"%@\n%@, %@ %@\n%@",
                                                     self.transaction.addressStreet,
                                                     self.transaction.addressCity,
                                                     self.transaction.addressState,
                                                     self.transaction.addressZip,
                                                     self.transaction.addressCountry];
                    }
                    else
                    {
                        cell.detailTextLabel.text = @"\n\n";
                    }
                
                    cell.detailTextLabel.numberOfLines = 3;
                    cell.detailTextLabel.lineBreakMode = UILineBreakModeWordWrap;
            
                    return cell;
                }
                
                TransactionDetailCell * cell = [self.tableView dequeueReusableCellWithIdentifier:@"TransactionDetailCell"];
                if (!cell)
                {
                    cell = [[TransactionDetailCell alloc] initWithStyle:UITableViewCellStyleValue2 reuseIdentifier:@"TransactionDetailCell"];
                
                }
                cell.selectionStyle = UITableViewCellSelectionStyleNone;
                cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
            
                switch(indexPath.row)
                {
                    case 1:
                    {
                        cell.textLabel.text = @"email";
                        cell.detailTextLabel.text = self.transaction.contactEmail;
                        break;
                    }
                    case 2:
                    {
                        cell.textLabel.text = @"phone";
                        cell.detailTextLabel.text = self.transaction.contactPhone;
                        break;
                    }
                }
                return cell;
            }
        }
    }
    return nil;
}

- (void) openEmail:(NSString*) recipient
{
    if (recipient && recipient.length)
    {
        NSMutableString *emailBody = [[NSMutableString alloc] 
                                  initWithString:@"Join PushCoin today at https://pushcoin.com"];
    
        //Create the mail composer window
        MFMailComposeViewController *controller = [[MFMailComposeViewController alloc] init];
        controller.mailComposeDelegate = self;
        controller.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
    
        [controller setSubject:@"PushCoin"];
        [controller setToRecipients:[NSArray arrayWithObject:recipient]];
        [controller setMessageBody:emailBody isHTML:NO];
    
        [self presentViewController:controller animated:YES completion:nil];
    }
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
    }
    
    if (self.transaction.transactionContext == 'T')
    {
        switch (indexPath.section)
        {
            case 3:
                return 44 + 1 * 19;
        }
    }
    else
    {
        switch (indexPath.section)
        {
            case 3:
            {
                switch (indexPath.row)
                {
                    case 0: return 44 + 2 * 19;
                    default: return 40;
                }
            }
        }
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
    switch (indexPath.section)
    {
        case 0: [self openEmail:self.entity.email]; return;
        case 1: return;
        case 2: return;
    }
    
    if (self.transaction.transactionContext == 'T')
    {
        switch (indexPath.section)
        {
            //long/latitude
            case 3:
            {
                UIApplication * app = [UIApplication sharedApplication];
                [app openURL:[NSURL URLWithString:[NSString stringWithFormat:@"http://maps.google.com/maps?ll=%f,%f",
                                                   self.transaction.latitude, self.transaction.longitude]]];
                return;
            }
        }
    }
    else
    {
        switch (indexPath.section)
        {
            case 3:
            {
                switch (indexPath.row)
                {
                    //address
                    case 0: 
                    {
                        UIApplication * app = [UIApplication sharedApplication];
                        NSMutableString * addressString = [NSString stringWithFormat:@"%@ %@ %@ %@ %@",
                                                           self.transaction.addressStreet, 
                                                           self.transaction.addressCity, 
                                                           self.transaction.addressState,
                                                           self.transaction.addressZip,
                                                           self.transaction.addressCountry];
                        
                        NSMutableString * urlString = [NSString stringWithFormat:@"http://maps.google.com/maps?q=%@",
                                                       [addressString urlEncodeUsingEncoding:NSUTF8StringEncoding]];

                        [app openURL:[NSURL URLWithString:urlString]];
                        NSLog(@"%@", urlString);
                        return;
                    }
                        
                    //contact email
                    case 1: [self openEmail:self.transaction.contactEmail]; return;
                    
                    //contact phone
                    case 2: return;
                }
            }
        }
    }
}

@end
