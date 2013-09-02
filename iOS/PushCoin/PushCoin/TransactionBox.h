//
//  TransactionBox.h
//  PushCoin
//
//  Created by Gilbert Cheung on 9/2/13.
//
//

#import "MGBox.h"
#import "MGTableBoxStyled.h"
#import "Transaction.h"

@interface TransactionBox : MGBox
@property (strong, nonatomic) Transaction * trx;
@property (assign, readonly) BOOL isExpanded;

+ (TransactionBox *)transactionBoxFor:(Transaction *)trx;
- (void) expand;

@end
