#import "MainTabBarController.h"
#import "AppDelegate.h"
#import "NSString+HexStringToBytes.h"
#import "PcosHelper.h"
#import "PushCoinWebService.h"
#import "MessageUpdatedDelegate.h"
#import "Transaction.h"

@interface MainTabBarController ()

@end

using namespace pcos;

@implementation MainTabBarController

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

-(void) viewDidAppear:(BOOL)animated
{
    NSLog(@"MainTabBarController didAppear");
    [super viewDidAppear:(BOOL)animated];
    [self.appDelegate requestRegistrationWithDelegate:self];
}

- (void)viewDidLoad
{
    NSLog(@"MainTabBarController didLoad");
    [super viewDidLoad];
	// Do any additional setup after loading the view.
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
}

- (void)webService:(PushCoinWebService *)webService didReceiveMessage:(NSData *)data
{
    [self handleData: data];
    
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"MMM d, h:mm a"];
    
    self.timestamp = [formatter stringFromDate:[NSDate date]];
    
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
                
                long value = bl.readLong();
                int scale = bl.readInt();
                
                NSLog(@"Balance %ld %d", value, scale);
                self.balance = [NSString stringWithFormat:@"$%.02lf", value * pow(10, scale)];
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
    [[self appDelegate] showAlert:description
                        withTitle:[NSString stringWithFormat:@"Webservice Error - %d", statusCode]];
}


@end
