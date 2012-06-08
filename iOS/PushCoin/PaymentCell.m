//#import "Common.h"

#import <QuartzCore/QuartzCore.h>
#import "PaymentCell.h"
#import "AppDelegate.h"


@implementation PaymentCell
@synthesize payment = _payment;
@synthesize amountLabel = _amountlabel;
@synthesize centLabel = _centlabel;
@synthesize currencyLabel = _currencyLabel;
@synthesize tipLabel = _tipLabel;

-(id) initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self)
    {
        UIView *view = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 
                                                                self.bounds.size.width,
                                                                self.bounds.size.height)];
        view.contentScaleFactor = [[UIScreen mainScreen] scale];
        view.backgroundColor = [UIColor clearColor];
        view.opaque = YES;
        
        self.contentView = view;
        self.backgroundColor = [UIColor clearColor];
        self.opaque = YES;
        self.deleteButtonIcon = [UIImage imageNamed:@"remove.png"];
        [self addLabels];

    }
    return self;
}

-(void) setPayment:(PushCoinPayment *)payment
{
    _payment = payment;
    self.contentView.backgroundColor = [UIColor colorWithPatternImage:
                                        [self.appDelegate imageForAmountType:payment.amountType]];
    
    //self.contentView.backgroundColor = [UIColor darkGrayColor];
    //self.contentView.layer.borderColor = [UIColor greenColor].CGColor;
    //self.contentView.layer.borderWidth = 1.0f;
    //self.contentView.alpha = 0.75f;
    
    [self setAmount:self.payment.amount andTip:self.payment.tip];
}

- (float) outerMargin
{ return 7.5f; }

-(void) addLabels
{
    // Create label for button
	CGRect frame=CGRectInset(CGRectMake(self.contentView.bounds.origin.x , 
                                        self.contentView.bounds.origin.y,
                                        self.contentView.bounds.size.width, 
                                        self.contentView.bounds.size.height),
                             self.outerMargin, self.outerMargin);
    
    CGRect amountFrame = CGRectMake(frame.origin.x + 20, 
                                    frame.origin.y,
                                    frame.size.width - 65, 
                                    frame.size.height);
    
    CGRect tipFrame = CGRectMake(frame.origin.x, 
                                 frame.origin.y + frame.size.height * 2 / 3,
                                 frame.size.width, 
                                 frame.size.height * 1 / 3);
    
	UILabel *amountLabel;
    amountLabel = [[UILabel alloc] initWithFrame:amountFrame];
	amountLabel.textAlignment = UITextAlignmentRight;
    amountLabel.autoresizingMask = UIViewAutoresizingFlexibleHeight | UIViewAutoresizingFlexibleWidth;
    amountLabel.text = @"";
	amountLabel.backgroundColor = [UIColor clearColor];
	amountLabel.textColor = [UIColor colorWithRed:1.0 green:1.0 blue:1.0 alpha:1.0];
	amountLabel.font = [UIFont fontWithName:@"Helvetica" size:30.0f];
    amountLabel.opaque = YES;
    self.amountLabel = amountLabel;
    
    UILabel *centLabel;
    centLabel = [[UILabel alloc] init];
	centLabel.textAlignment = UITextAlignmentLeft;
    centLabel.autoresizingMask = UIViewAutoresizingFlexibleHeight | UIViewAutoresizingFlexibleWidth;
    centLabel.text = @"";
	centLabel.backgroundColor = [UIColor clearColor];
	centLabel.textColor = [UIColor colorWithRed:1.0 green:1.0 blue:1.0 alpha:1.0];
	centLabel.font = [UIFont fontWithName:@"Helvetica" size:15.0f];
    centLabel.opaque = YES;
    self.centLabel = centLabel;
    
    UILabel *currencyLabel;
    currencyLabel = [[UILabel alloc] init];
	currencyLabel.textAlignment = UITextAlignmentLeft;
    currencyLabel.autoresizingMask = UIViewAutoresizingFlexibleHeight | UIViewAutoresizingFlexibleWidth;
    currencyLabel.text = @"";
	currencyLabel.backgroundColor = [UIColor clearColor];
	currencyLabel.textColor = [UIColor colorWithRed:1.0 green:1.0 blue:1.0 alpha:1.0];
	currencyLabel.font = [UIFont fontWithName:@"Helvetica" size:10.0f];
    currencyLabel.opaque = YES;
    self.currencyLabel = currencyLabel;
    
    UILabel *tipLabel = [[UILabel alloc] initWithFrame:tipFrame];
	tipLabel.textAlignment = UITextAlignmentCenter;
    tipLabel.autoresizingMask = UIViewAutoresizingFlexibleHeight | UIViewAutoresizingFlexibleWidth;
	tipLabel.text = @"";
	tipLabel.backgroundColor = [UIColor clearColor];
	tipLabel.textColor = [UIColor colorWithRed:1.0 green:1.0 blue:1.0 alpha:1.0];
	tipLabel.font = [UIFont fontWithName:@"Helvetica" size:10.0f];
    tipLabel.opaque = YES;
    self.tipLabel = tipLabel;
    
    [[self.contentView subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
    [self.contentView addSubview: amountLabel];
    [self.contentView addSubview: centLabel];
    [self.contentView addSubview: currencyLabel];
    [self.contentView addSubview: tipLabel];

}

- (void) setAmount:(Float32) amount andTip:(Float32)tip
{
    self.amountLabel.text = [NSString stringWithFormat:@"$%d", (int)amount];
    self.centLabel.text = [NSString stringWithFormat:@"%02d", (int)(amount * 100) % 100];
    [self.centLabel sizeToFit];
    
    self.centLabel.frame = CGRectMake(self.amountLabel.frame.origin.x + self.amountLabel.frame.size.width + 3,
                                      26.0f,
                                      self.centLabel.frame.size.width,
                                      self.centLabel.frame.size.height);
    
    self.currencyLabel.text = @"USD";
    [self.currencyLabel sizeToFit];
    
    self.currencyLabel.frame = CGRectMake(self.amountLabel.frame.origin.x + self.amountLabel.frame.size.width + 3,
                                      42.0f,
                                      self.currencyLabel.frame.size.width,
                                      self.currencyLabel.frame.size.height);
    
    if (tip != 0.0f)
        self.tipLabel.text = [NSString stringWithFormat:@"+ %d%% tips", (int) (tip * 100)];
    else
        self.tipLabel.text = @"";
    
}

- (AppDelegate *)appDelegate
{
    return (AppDelegate *)[[UIApplication sharedApplication] delegate];
}

/* Generate Background Image

#define kButtonRadius 6.0

- (void)drawRect:(CGRect)rect
{
    RetinaAwareUIGraphicsBeginImageContext(self.frame.size);
    [self.layer renderInContext:UIGraphicsGetCurrentContext()];
    
    CGContextRef context = UIGraphicsGetCurrentContext();
    
    CGFloat actualBrightness = _brightness;
    
    UIColor * blackColor = [UIColor colorWithRed:0.0 green:0.0 blue:0.0 alpha:1.0];
    UIColor * highlightStart = [UIColor colorWithRed:1.0 green:1.0 blue:1.0 alpha:0.7];
    UIColor * highlightStop = [UIColor colorWithRed:0.0 green:0.0 blue:0.0 alpha:0.0];
    
    
	UIColor * outerTop = [UIColor colorWithHue:_hue 
                                    saturation:_saturation
                                    brightness:1.0 * actualBrightness 
                                         alpha:_alpha];
    
    UIColor * outerBottom = [UIColor colorWithHue:_hue 
                                       saturation:_saturation 
                                       brightness:0.80 * actualBrightness 
                                            alpha:_alpha];
    
    CGFloat outer = self.outerMargin;
    CGRect outerRect = CGRectInset(self.bounds, outer, outer);            
    CGMutablePathRef outerPath = createRoundedRectForRect(outerRect, 6.0);
        
    // Draw gradient for outer path
	CGContextSaveGState(context);
	CGContextAddPath(context, outerPath);
	CGContextClip(context);
	drawLinearGradient(context, outerRect, outerTop, outerBottom);
    
	CGContextRestoreGState(context);

    CGRect highlightRect = CGRectInset(outerRect, 1.0f, 1.0f);
    

	CGMutablePathRef highlightPath = createRoundedRectForRect(highlightRect, 6.0);
        
	CGContextSaveGState(context);
	CGContextAddPath(context, outerPath);
	CGContextAddPath(context, highlightPath);
	CGContextEOClip(context);
        
	drawLinearGradient(context, CGRectMake(outerRect.origin.x, outerRect.origin.y, outerRect.size.width, outerRect.size.height/3), highlightStart, highlightStop);
	CGContextRestoreGState(context);
        
	drawCurvedGloss(context, outerRect, 180);
	CFRelease(highlightPath);
     
     
	//bottom highlight
	CGRect highlightRect2 = CGRectInset(self.bounds, 6.5f, 6.5f);
    
	CGMutablePathRef highlightPath2 = createRoundedRectForRect(highlightRect2, 6.0);
        
	CGContextSaveGState(context);
	CGContextSetLineWidth(context, 0.5);
	CGContextAddPath(context, highlightPath2);
	CGContextAddPath(context, outerPath);
	CGContextEOClip(context);
	drawLinearGradient(context, CGRectMake(self.bounds.origin.x, self.bounds.size.height-self.bounds.size.height/3, self.bounds.size.width, self.bounds.size.height/3), highlightStop, highlightStart);
        
	CGContextRestoreGState(context);
	CFRelease(highlightPath2);
    
    
    // Stroke outer path
    CGContextSaveGState(context);
    CGContextSetLineWidth(context, 0.5);
    CGContextSetStrokeColorWithColor(context, blackColor.CGColor);
    CGContextAddPath(context, outerPath);
    CGContextStrokePath(context);
    CGContextRestoreGState(context);
    
    CFRelease(outerPath);
    
    UIImage * viewImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    NSString * toPath = [self.appDelegate keyFilePath];
    NSString* pathToCreate = [toPath stringByAppendingPathComponent:[NSString stringWithFormat:@"%.2f.png", self.payment.amount]];
    
    NSData *imageData = [NSData dataWithData:UIImagePNGRepresentation(viewImage)];
    [imageData writeToFile:pathToCreate atomically:YES];
}
*/                           
                              



@end