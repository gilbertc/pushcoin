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
#import "UIFont+FlatUI.h"
#import "UIColor+FlatUI.h"


#define ROW_SIZE (CGSize){304, 34}

@implementation TransactionBox
{
    Transaction * trx_;
    BOOL isExpanded_;
    
    MGTableBoxStyled * mainBox;
    
    MGLineStyled * headerLine;
    MGLineStyled * timeLine;
    MGLineStyled * typeLine;
    MGLineStyled * deviceLine;
    MGLineStyled * locationLine;
    MGLineStyled * taxLine;
    MGLineStyled * tipsLine;
    
    UIFont * keyFont;
    UIFont * valueFont;
    UIColor * keyColor;
    UIColor * valueColor;
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
        keyFont = [UIFont fontWithName:@"HelveticaNeue" size:12];
        valueFont = [UIFont fontWithName:@"HelveticaNeue-Light" size:14];
        keyColor = [UIColor darkGrayColor];
        valueColor = [UIColor darkGrayColor];
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
    NSString * title;
    if ([trx.txContext compare:@"D"] == 0)
        title = trx.note;
    else
        title = trx.counterPartyName;
    
    headerLine = [MGLineStyled lineWithLeft:title
                                      right:trx.payment.text
                                       size:(CGSize){304, 44}];
    headerLine.font = [UIFont fontWithName:@"HelveticaNeue-Light" size:17];
    headerLine.rightFont = [UIFont fontWithName:@"HelveticaNeue" size:17];
    headerLine.sidePrecedence = MGSidePrecedenceRight;

    [mainBox.topLines addObject:headerLine];
    
    if ([trx.txType compare:@"C"] == 0)
        headerLine.rightTextColor = [UIColor greenSeaColor];
    else
        headerLine.rightTextColor = [UIColor pomegranateColor];
    
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
    }
}

- (void) collapse
{
    if (isExpanded_ == YES)
    {
        isExpanded_ = NO;
        [mainBox.topLines removeAllObjects];
        
        [mainBox.topLines addObject:headerLine];
    }
}

- (MGLineStyled *) prepareLine:(MGLineStyled *) line
{
    line.font = keyFont;
    line.textColor = keyColor;
    line.rightFont = valueFont;
    line.rightTextColor = valueColor;
    line.sidePrecedence = MGSidePrecedenceRight;
    return line;
}


- (void) addDetails
{
    if (timeLine == nil)
    {
        timeLine = [self prepareLine:[MGLineStyled lineWithLeft:@"Time"
                                                          right:UtcTimestampToPrettyDate(self.trx.utcTransactionTime)
                                                           size:ROW_SIZE]];
    }
    [mainBox.topLines addObject:timeLine];
    
    if (typeLine == nil)
    {
        typeLine = [self prepareLine:[MGLineStyled lineWithLeft:@"Type"
                                                          right:[NSString stringWithFormat:@"%@ %@",
                                                                 TxContextToString(self.trx.txContext),
                                                                 TxTypeToString(self.trx.txType)]
                                                           size:ROW_SIZE]];
    }
    [mainBox.topLines addObject:typeLine];
    
    if ([self.trx.txType compare:@"D"] == 0)
    {
        if (deviceLine == nil)
        {
            deviceLine = [self prepareLine:[MGLineStyled lineWithLeft:@"Device"
                                                                right:self.trx.deviceName
                                                                 size:ROW_SIZE]];
        }
        [mainBox.topLines addObject:deviceLine];
    }
    
    if ([self.trx.txContext compare:@"P"] == 0)
    {
        if (locationLine == nil)
        {
            locationLine =  [self prepareLine:[MGLineStyled lineWithLeft:@"Location"
                                                                   right:[NSString stringWithFormat:@"%@, %@",
                                                                          self.trx.address.city,
                                                                          self.trx.address.state]
                                                                    size:ROW_SIZE]];
        }
        [mainBox.topLines addObject:locationLine];
    }
    
    if (self.trx.tax != nil)
    {
        if (taxLine == nil)
        {
            taxLine = [self prepareLine:[MGLineStyled lineWithLeft:@"Tax"
                                                             right:self.trx.tax.text
                                                              size:ROW_SIZE]];
        }
        [mainBox.topLines addObject:taxLine];
    }
    
    if (self.trx.tip != nil)
    {
        if (tipsLine == nil)
        {
            tipsLine = [self prepareLine:[MGLineStyled lineWithLeft:@"Tips"
                                                              right:self.trx.tip.text
                                                               size:ROW_SIZE]];
        }
        [mainBox.topLines addObject:tipsLine];
    }
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
