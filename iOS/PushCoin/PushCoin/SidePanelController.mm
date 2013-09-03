
#import "SidePanelController.h"
#import "AppDelegate.h"
#import "NSString+HexStringToBytes.h"
#import "PcosHelper.h"
#import "PushCoinWebService.h"
#import "MessageUpdatedDelegate.h"
#import "Transaction.h"
#import "Common.h"

@interface SidePanelController ()

@end

using namespace pcos;

@implementation SidePanelController
{
    BOOL dataReceived;
    UIViewController *summaryView;
    UIViewController *historyView;
}

@synthesize timestamp;
@synthesize balance;
@synthesize transactions;
@synthesize messageListeners;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

-(void) awakeFromNib
{
    [self setLeftPanel:[self.storyboard instantiateViewControllerWithIdentifier:@"leftViewController"]];
    [self showSummary];
}

-(void) showSummary
{
    if (summaryView == nil)
        summaryView = [self.storyboard instantiateViewControllerWithIdentifier:@"SummaryViewController"];
    [self setCenterPanel:summaryView];
}

-(void) showHistory
{
    if (historyView == nil)
        historyView = [self.storyboard instantiateViewControllerWithIdentifier:@"HistoryViewController"];
    [self setCenterPanel:historyView];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    dataReceived = NO;
    
    NSString * storePath = fileAtDocumentDirectory(PushCoinLastTxnHistoryFile);
    if ([[NSFileManager defaultManager] fileExistsAtPath:storePath])
    {
        NSData * lastTxnHistory = [NSData dataWithContentsOfFile:storePath];
        if (lastTxnHistory != nil)
        {
            NSLog(@"Processing cached txn data: %@", storePath);
            [self handleData: lastTxnHistory];
        }
    }
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:(BOOL)animated];
    [self.appDelegate requestRegistrationWithDelegate:self];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


- (AppDelegate *)appDelegate
{
    return (AppDelegate *)[[UIApplication sharedApplication] delegate];
}

-(void) registrationControllerDidClose:(RegistrationController *)controller
{
    [self dismissModalViewControllerAnimated:YES];
}



-(void) refresh
{
    if (refreshing)
        return;
    
    refreshing = YES;
    PushCoinWebService * webService =  [[PushCoinWebService alloc] initWithDelegate:self];
    NSData * mat = [self.appDelegate.authToken hexStringToBytes];
    
    DocumentWriter writer("TxnHistoryQuery");
    auto & bo = writer.addBlock("Bo");
    writeByteStr(bo, mat);
    bo.writeUInt(0);
    bo.writeUInt(20);
    
    [webService sendMessage:[NSData dataWithBytes:writer.bytes() length:writer.size()]];
}

- (void)registerMessageListener:(id <MessageUpdatedDelegate>) listener
{
    if (!self.messageListeners)
        self.messageListeners = [[NSMutableArray alloc] init];
    
    [self.messageListeners addObject:listener];
    
    if (dataReceived)
        [listener messageDidUpdatedBy:self];
    else
        [self refresh];
}

- (void)webService:(PushCoinWebService *)webService didReceiveMessage:(NSData *)data
{
    [self handleData: data];
    
    
    for (id<MessageUpdatedDelegate> listener in self.messageListeners)
    {
        [listener messageDidUpdatedBy:self];
    }
    
    NSLog(@"Updated");
    refreshing = NO;
    
}

- (void)handleData: (NSData*) data
{
    try
    {
        DocumentReader reader((byte const * )data.bytes, 0, data.length);
        NSString * documentName = [NSString stringWithUTF8String:reader.getDocumentName().c_str()];
        
        if ([documentName isEqualToString:@"TxnHistoryReply"])
        {
            auto blIt = reader.find("Bl");
            if (blIt != reader.end())
            {
                auto & bl = blIt->second;
                self.balance = [[Amount alloc] initWithReader:bl];
                self.timestamp = UtcTimestampToString(bl.readULong(), @"MMM d, h:mm a");
            }
            
            auto trIt = reader.find("Tr");
            if (trIt != reader.end())
            {
                auto & tr = trIt->second;
                
                size_t total = tr.readUInt(); // total transactions
                size_t count = tr.readUInt(); // number of transaction for this message
                
                self.transactions = [[NSMutableArray alloc] init];
                for (size_t i = 0; i < count; ++i)
                    [self.transactions addObject:[[Transaction alloc] initWithReader:tr]];
            }
            
            dataReceived = YES;
            
            // Save this result for next use
            NSString * storePath = fileAtDocumentDirectory(PushCoinLastTxnHistoryFile);
            NSLog(@"Writing to cache file: %@", storePath);
            
            [data writeToFile:storePath atomically:YES];
        }
        
        if ([documentName isEqualToString:@"Error"])
        {
            auto it = reader.find("Bo");
            if (it != reader.end())
            {
                auto & bo = it->second;
                
                readByteStr(bo, 0); // transactionId
                UInt32 errorCode = bo.readUInt();
                NSString * reason = readString(bo, 0);
                
                [self.appDelegate handleErrorMessage:reason withErrorCode:errorCode];
                return;
            }
        }
    }
    catch(PcosException ex)
    {
        NSLog(@"Exception");
    }
}

- (void)webService:(PushCoinWebService *)webService didFailWithStatusCode:(NSInteger)statusCode
    andDescription:(NSString *)description
{
    /*
     [[self appDelegate] showAlert:description
     withTitle:[NSString stringWithFormat:@"Webservice Error - %d", statusCode]];
     */
    
    refreshing = NO;
    
    for (id<MessageUpdatedDelegate> listener in self.messageListeners)
    {
        [listener messageDidFailedBy:self withDescription:description];
    }
}

@end
