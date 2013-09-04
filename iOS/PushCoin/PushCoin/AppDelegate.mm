#import "AppDelegate.h"
#import "OpenSSLWrapper.h"

#import "NSString+HexStringToBytes.h"
#import "NSData+BytesToHexString.h"
#import "Common.h"

@implementation SingleUseData

+(id) dataWithData:(NSData *)d
{
    return [[SingleUseData alloc] initWithData:d];
}
-(id) initWithData:(NSData *)d
{
    self = [self init];
    if (self)
    {
        self.data = d;
    }
    return self;
}

-(void) setData:(NSData *)data
{
    data_ = [data copy];
}

-(NSData *) data
{
    NSData * res = data_;
    data_ = nil;
    return res;
}
@end


@implementation AppDelegate

@synthesize window = _window;
@synthesize privateKeyKeychainItem;
@synthesize pinHashKeychainItem;
@synthesize authTokenKeychainItem;
@synthesize pemDsaPublicKey = _pemDsaPublicKey;
@synthesize dsaDecryptedKey = _dsaDecryptedKey;
@synthesize fileURL = _fileURL;

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    NSUserDefaults * defaults = [NSUserDefaults standardUserDefaults];
    NSDictionary * dict = [NSDictionary dictionaryWithObjectsAndKeys:@"YES", @"first-time", nil];
    [defaults registerDefaults:dict];
    
    if ([defaults objectForKey:@"first-time"])
    {
        [self setDefaults];
        [defaults setObject:@"NO" forKey:@"first-time"];
        [defaults synchronize];
    }
    
    self.dsaDecryptedKey = [[SingleUseData alloc] init];
    
    [self prepareKeyFiles];
    [self prepareKeyChain];
    [self prepareOpenSSLWrapper];
    
    return YES;
}

- (void)setDefaults {
    
    //get the plist location from the settings bundle
    NSString *settingsPath = [[[NSBundle mainBundle] bundlePath] stringByAppendingPathComponent:@"InAppSettings.bundle"];
    NSString *plistPath = [settingsPath stringByAppendingPathComponent:@"Root.plist"];
    
    //get the preference specifiers array which contains the settings
    NSDictionary *settingsDictionary = [NSDictionary dictionaryWithContentsOfFile:plistPath];
    NSArray *preferencesArray = [settingsDictionary objectForKey:@"PreferenceSpecifiers"];
    
    //use the shared defaults object
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    
    //for each preference item, set its default if there is no value set
    for(NSDictionary *item in preferencesArray) {
        
        //get the item key, if there is no key then we can skip it
        NSString *key = [item objectForKey:@"Key"];
        if (key) {
            
            //check to see if the value and default value are set
            //if a default value exists and the value is not set, use the default
            id value = [defaults objectForKey:key];
            id defaultValue = [item objectForKey:@"DefaultValue"];
            if(defaultValue && !value) {
                [defaults setObject:defaultValue forKey:key];
            }
        }
    }
    
    //write the changes to disk
    [defaults synchronize];
}


-(BOOL) prepareKeyFiles
{
    NSError * error;
    NSFileManager * fileManager = [NSFileManager defaultManager];
    NSArray * files = [NSArray arrayWithObjects:PushCoinRSAPublicKeyFile, nil];
    NSString * fromPath = [[NSBundle mainBundle] bundlePath];
    NSString * toPath = [self documentPath];
    BOOL ret = YES;
    for (id file in files)
    {
        ret &= [fileManager copyItemAtPath:[fromPath stringByAppendingPathComponent:file]
                                    toPath:[toPath stringByAppendingPathComponent:file]
                                     error:&error];
    }
    return ret;
}

-(BOOL) prepareOpenSSLWrapper
{
    OpenSSLWrapper * ssl = [OpenSSLWrapper instance];
    ssl.delegate = self;
    
    return [self prepareRSA];
}

-(NSData *)sslNeedsDsaPrivateKey:(OpenSSLWrapper *)ssl
{
    return self.dsaPrivateKey;
}

-(BOOL) prepareKeyChain
{
    self.privateKeyKeychainItem = [[KeychainItemWrapper alloc] initWithIdentifier:@"com.pushcoin.privatekey" accessGroup:nil];
    [self.privateKeyKeychainItem setObject:@"com.pushcoin.privatekey" forKey:(__bridge id)kSecAttrService];
    
    self.authTokenKeychainItem = [[KeychainItemWrapper alloc] initWithIdentifier:@"com.pushcoin.authtoken" accessGroup:nil];
    [self.authTokenKeychainItem setObject:@"com.pushcoin.authtoken" forKey:(__bridge id)kSecAttrService];
    
    self.pinHashKeychainItem = [[KeychainItemWrapper alloc] initWithIdentifier:@"com.pushcoin.pinhash" accessGroup:nil];
    [self.pinHashKeychainItem setObject:@"com.pushcoin.pinhash" forKey:(__bridge id)kSecAttrService];
        
    return YES;
}

-(id<MessageUpdaterDelegate>) messageUpdater
{
    return (id<MessageUpdaterDelegate>) (self.window.rootViewController);
}

-(id<RegistrationControllerDelegate>) registrationController
{
    return (id<RegistrationControllerDelegate>) (self.window.rootViewController);
}

- (BOOL) prepareRSA
{
    OpenSSLWrapper * ssl = [OpenSSLWrapper instance];
    [ssl prepareRsaWithKeyFile:[NSString stringWithFormat:@"%@/%@", self.documentPath, PushCoinRSAPublicKeyFile]];
    return YES;
}

- (BOOL) registered
{
    return self.authToken.length != 0;
}

- (BOOL) hasPasscode
{
    NSString * saltedHash = [self.pinHashKeychainItem objectForKey:(__bridge id)kSecValueData];            
    return (saltedHash && saltedHash.length != 0);
}

- (void) setPasscode:(NSString *)passcode oldPasscode:(NSString *)oldPasscode
{
    [self unlockDsaPrivateKeyWithPasscode:oldPasscode];
    
    NSData * privateKey = [self dsaPrivateKey];
    if (privateKey && privateKey.length)
    {
        [self setDsaPrivateKey:privateKey withPasscode:passcode];
    }

    if (passcode && passcode.length != 0)
    {
        NSString * salt = [NSString stringWithFormat:@"%u", arc4random()];
        NSString * saltedPasscode = [NSString stringWithFormat:@"%@%@", salt, passcode];
        NSString * hash = [[OpenSSLWrapper instance] sha1_hashData:[saltedPasscode dataUsingEncoding:NSASCIIStringEncoding]].bytesToHexString;
        
        NSString * saltedHash =  [NSString stringWithFormat:@"%@|%@", salt, hash];
        [self.pinHashKeychainItem setObject:saltedHash forKey:(__bridge id)kSecValueData];
    }
    else
    {
        [self.pinHashKeychainItem setObject:@"" forKey:(__bridge id)kSecValueData];      
    }
}

-(void) synchronizeDefaults
{
    //passcode
    
    [[NSUserDefaults standardUserDefaults] setBool:self.hasPasscode forKey:@"passcode"];
    [[NSUserDefaults standardUserDefaults] synchronize];        
}

-(BOOL) validatePasscode:(NSString *)passcode
{
    NSString * saltedHash = [self.pinHashKeychainItem objectForKey:(__bridge id)kSecValueData];
    NSArray * array = [saltedHash componentsSeparatedByString:@"|"]; 

    if (array.count != 2) 
        return NO;
    
    NSString * salt = (NSString *) [array objectAtIndex:0];
    NSString * hash = (NSString *) [array objectAtIndex:1];
    NSString * saltedPasscode = [NSString stringWithFormat:@"%@%@", salt, passcode];
    
    if (!hash || hash.length == 0) 
        return YES;
    
    NSData * data = [[OpenSSLWrapper instance] sha1_hashData:[saltedPasscode dataUsingEncoding:NSASCIIStringEncoding]];
    return [data isEqualToData:hash.hexStringToBytes];
}

- (NSString *) authToken
{
    return [self.authTokenKeychainItem objectForKey:(__bridge id)kSecValueData];
}

- (void) setAuthToken:(NSString *)authToken
{
    [self.authTokenKeychainItem setObject:authToken forKey:(__bridge id)kSecValueData];
}

-(NSString *) pemDsaPublicKey
{
    NSString * pemPublicKey = [NSString stringWithContentsOfFile:[self.documentPath stringByAppendingPathComponent: PushCoinDSAPublicKeyFile] 
                                                        encoding:NSASCIIStringEncoding error:nil];
    
    NSRange headerRange = [pemPublicKey rangeOfString:@"---\n"];
    pemPublicKey = [pemPublicKey substringFromIndex:headerRange.location + headerRange.length];
    
    NSRange footerRange = [pemPublicKey rangeOfString:@"\n---"];
    pemPublicKey = [pemPublicKey substringToIndex:footerRange.location];
    
    return pemPublicKey;
}

- (BOOL) unlockDsaPrivateKeyWithPasscode:(NSString *)passcode
{
    NSData * encryptedKey = ((NSString *)[self.privateKeyKeychainItem objectForKey:(__bridge id)kSecValueData]).hexStringToBytes;
    if (encryptedKey && encryptedKey.length)
    {
        if (passcode && passcode.length)
        {
            OpenSSLWrapper * ssl = [OpenSSLWrapper instance];
            self.dsaDecryptedKey.data = [ssl des3_decrypt:encryptedKey withKey:passcode];
        }
        else 
        {
            self.dsaDecryptedKey.data = encryptedKey;
        }
    }
    return YES;
}

- (NSData *) dsaPrivateKey
{
    return self.dsaDecryptedKey.data;
}

- (void) setDsaPrivateKey:(NSData *)dsaPrivateKey withPasscode:(NSString *)passcode
{
    if (!passcode || passcode.length == 0)
        [self.privateKeyKeychainItem setObject:dsaPrivateKey.bytesToHexString forKey:(__bridge id)kSecValueData];
    else
    {
        OpenSSLWrapper * ssl = [OpenSSLWrapper instance];
        NSData * encryptedKey = [ssl des3_encrypt:dsaPrivateKey withKey:passcode];
        [self.privateKeyKeychainItem setObject:encryptedKey.bytesToHexString forKey:(__bridge id)kSecValueData];
    }
}

- (NSString *)documentPath
{
	NSString *dir = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
	return dir;
}

-(RegistrationController *)requestRegistrationWithDelegate:(NSObject<RegistrationControllerDelegate> *)delegate
{
    return [self requestRegistrationWithDelegate:delegate viewController:self.window.rootViewController];
}

-(RegistrationController *)requestRegistrationWithDelegate:(NSObject<RegistrationControllerDelegate> *)delegate
                                            viewController:(UIViewController *)viewController
{
    if (!self.registered)
    {
        RegistrationController * controller = [self viewControllerWithIdentifier:@"RegistrationController"];
        controller.delegate = delegate;
        controller.modalTransitionStyle =  UIModalTransitionStyleCoverVertical;
        
        [viewController presentModalViewController:controller animated:NO];
        return controller;
    }
    return nil;
}

-(KKPasscodeViewController *)requestPasscodeWithDelegate:(NSObject<KKPasscodeViewControllerDelegate> *)delegate
{
    return [self requestPasscodeWithDelegate:delegate viewController:self.window.rootViewController];
}

-(KKPasscodeViewController *)requestPasscodeWithDelegate:(NSObject<KKPasscodeViewControllerDelegate> *)delegate
                                          viewController:(UIViewController *)viewController
{
    KKPasscodeViewController * controller = [[KKPasscodeViewController alloc] init];
    controller.delegate = delegate;
    controller.mode = KKPasscodeModeEnter;
    controller.passcodeLockOn = YES;
    controller.eraseData = NO;
    controller.passcode = @"";
    controller.modalTransitionStyle = UIModalTransitionStyleCoverVertical;
    
    [viewController presentModalViewController:controller animated:YES];
    return controller;
}

-(KKPasscodeViewController *)requestPasscodeWithDelegate:(NSObject<KKPasscodeViewControllerDelegate> *)delegate
                                    navigationController:(UINavigationController *)navController
{
    KKPasscodeViewController * controller = [[KKPasscodeViewController alloc] init];
    controller.delegate = delegate;
    controller.mode = KKPasscodeModeEnter;
    controller.passcodeLockOn = YES;
    controller.eraseData = NO;
    controller.passcode = @"";

    [navController pushViewController:controller animated:YES];
    return controller;
}

-(id)viewControllerWithIdentifier:(NSString *) identifier
{
    UIStoryboard * storyboard = [UIStoryboard storyboardWithName:@"MainStoryboard" bundle:[NSBundle mainBundle]];
    id controller = [storyboard instantiateViewControllerWithIdentifier:identifier];
    return controller;
}

-(void) clearDevice
{
    NSData * emptyData = [[NSData alloc] init];
    self.authToken = @"";
    [self setPasscode:@"" oldPasscode:@""];
    [self setDsaPrivateKey:emptyData withPasscode:@""];
    [[NSFileManager defaultManager] removeItemAtPath:fileAtDocumentDirectory(PushCoinLastTxnHistoryFile) error:nil];
}

- (UIAlertView *) showAlert:(NSString *)message withTitle:(NSString *)title
{
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:title
                                                    message:message
                                                   delegate:nil
                                          cancelButtonTitle:@"Close" 
                                          otherButtonTitles:nil];
    [alert show];
    return alert;
}

-(bool) handleErrorMessage:(NSString *)reason withErrorCode:(UInt32)errorCode
{
    switch (errorCode)
    {
        case 1107: //not authorized
            [self clearDevice];
            [self showAlert:reason withTitle:@"Device needs registration"];
            [self requestRegistrationWithDelegate:nil];
            break;
        default:
            [self showAlert:reason
                  withTitle:[NSString stringWithFormat:@"Error - %d", (unsigned int)errorCode]];
    }
    return YES;
}

-(bool) handleUnknownMessage:(NSString *)documentName
{
    [self showAlert:[NSString stringWithFormat:@"unexpected message received: [%@]", documentName]
          withTitle:@"Error"];
    return YES;
}


- (void)applicationWillResignActive:(UIApplication *)application
{
    /*
     Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
     Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
     */
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    /*
     Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
     If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
     */
    
    [[NSNotificationCenter defaultCenter] postNotificationName: @"handleCleanup" 
                                                        object: nil 
                                                      userInfo: nil];
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    /*
     Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
     */
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    /*
     Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
     */
}

- (void)applicationWillTerminate:(UIApplication *)application
{
    /*
     Called when the application is about to terminate.
     Save data if appropriate.
     See also applicationDidEnterBackground:.
     */
}

@end
