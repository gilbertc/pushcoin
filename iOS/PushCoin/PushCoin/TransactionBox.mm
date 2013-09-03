//
//  TransactionBox.m
//  PushCoin
//
//  Created by Gilbert Cheung on 9/2/13.
//
//

#import "TransactionBox.h"
#import "MGBox.h"
#import "MGTableBoxStyled.h"
#import "MGLineStyled.h"
#import "MGScrollView.h"
#import "Common.h"

#define ROW_SIZE (CGSize){304, 44}

@implementation TransactionBox
{
    Transaction * trx_;
    BOOL isExpanded_;
    
    MGTableBoxStyled * mainBox;
    
    MGLineStyled * headerLine;
    MGLineStyled * trxIdLine;
    MGLineStyled * timeLine;
    MGLineStyled * typeLine;
    MGLineStyled * deviceLine;
    MGLineStyled * locationLine;
    MGLineStyled * taxLine;
    MGLineStyled * tipsLine;
}

+ (TransactionBox *)transactionBoxFor:(Transaction *)trx {
    
    // box with photo number tag
    TransactionBox *box = [TransactionBox box];
    box.sizingMode = MGResizingShrinkWrap;
    box.trx = trx;
    
    
    return box;
}

- (TransactionBox *) initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self != nil)
    {
        isExpanded_ = NO;
    }
    
    return self;
}

- (BOOL) isExpanded
{
    return isExpanded_;
}

- (Transaction *) trx
{
    return trx_;
}

- (void) setTrx:(Transaction *)trx
{
    trx_ = trx;
    mainBox = MGTableBoxStyled.box;
    [self.boxes addObject:mainBox];
    
    // header line
    headerLine = [MGLineStyled lineWithLeft:trx.counterPartyName
                                      right:[NSString stringWithFormat:@"%@ %@", trx.currency, trx.payment.text]
                                       size:ROW_SIZE];
    headerLine.font = [UIFont fontWithName:@"HelveticaNeue" size:18];
    [mainBox.topLines addObject:headerLine];
    
    __unsafe_unretained TransactionBox * weakSelf = self;
    self.onTap = ^{
        [weakSelf expand];

        MGBox * table = (MGBox *) weakSelf.parentBox;
        MGScrollView * scrollView = (MGScrollView *) table.parentBox;

        [table layout];
        [scrollView scrollToView:weakSelf withMargin:8];
    };
}

- (void) expand
{
    if (isExpanded_ == NO)
    {
        // Collapse others
        for (TransactionBox * other in self.parentBox.boxes)
        {
            if (other.isExpanded == YES)
            {
                [other collapse];
                break;
            }
        }
        
        isExpanded_ = YES;
        [self addDetails];
        
        headerLine.textColor = [UIColor orangeColor];
    }
}

- (void) collapse
{
    if (isExpanded_ == YES)
    {
        isExpanded_ = NO;
        [mainBox.topLines removeAllObjects];
        
        headerLine.backgroundColor = [UIColor colorWithRed:0.94 green:0.94 blue:0.95 alpha:1];
        headerLine.textColor = [UIColor blackColor];
        headerLine.textShadowColor = UIColor.whiteColor;
       
        [mainBox.topLines addObject:headerLine];
    }
}


- (void) addDetails
{
    if (trxIdLine == nil)
    {
        trxIdLine = [MGLineStyled lineWithLeft:@"Trx ID"
                                         right:self.trx.transactionId
                                          size:ROW_SIZE];
    }
    [mainBox.topLines addObject:trxIdLine];
    
    if (timeLine == nil)
    {
        timeLine = [MGLineStyled lineWithLeft:@"Time"
                                        right:UtcTimestampToString(self.trx.utcTransactionTime)
                                         size:ROW_SIZE];
    }
    [mainBox.topLines addObject:timeLine];
    
    if (typeLine == nil)
    {
        typeLine = [MGLineStyled lineWithLeft:@"Type"
                                        right:[NSString stringWithFormat:@"%@ %@",
                                               TxTypeToString(self.trx.txType),
                                               TxContextToString(self.trx.txContext)]
                                         size:ROW_SIZE];
    }
    [mainBox.topLines addObject:typeLine];
    
    
    if (deviceLine == nil)
    {
        deviceLine = [MGLineStyled lineWithLeft:@"Device"
                                          right:self.trx.deviceName
                                           size:ROW_SIZE];
    }
    [mainBox.topLines addObject:deviceLine];
    
    
    if (locationLine == nil)
    {
        locationLine = [MGLineStyled lineWithLeft:@"Location"
                                            right:[NSString stringWithFormat:@"%@, %@",
                                                   self.trx.address.city,
                                                   self.trx.address.state]
                                             size:ROW_SIZE];
    }
    [mainBox.topLines addObject:locationLine];
    
    if (self.trx.tax != nil)
    {
        if (taxLine == nil)
        {
            taxLine = [MGLineStyled lineWithLeft:@"Tax"
                                           right:self.trx.tax.text
                                            size:ROW_SIZE];
        }
        [mainBox.topLines addObject:taxLine];
    }
    
    if (self.trx.tip != nil)
    {
        if (tipsLine == nil)
        {
            tipsLine = [MGLineStyled lineWithLeft:@"Tips"
                                            right:self.trx.tip.text
                                             size:ROW_SIZE];
        }
        [mainBox.topLines addObject:tipsLine];
    }
}

- (void) layout {
    [super layout];
}

/*
 // Only override drawRect: if you perform custom drawing.
 // An empty implementation adversely affects performance during animation.
 - (void)drawRect:(CGRect)rect
 {
 // Drawing code
 }
 */

@end
