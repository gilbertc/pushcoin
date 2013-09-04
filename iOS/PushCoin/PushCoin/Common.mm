//
//  Common.m
//  CoolTable
//
//  Created by Ray Wenderlich on 9/29/10.
//  Copyright 2010 Ray Wenderlich. All rights reserved.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

#import "Common.h"
#import "NSDate+Utilities.h"

//NSNumberFormatter *standardNumberFormatter();


void RetinaAwareUIGraphicsBeginImageContext(CGSize size) {
    static CGFloat scale = -1.0;
    if (scale<0.0) {
        UIScreen *screen = [UIScreen mainScreen];
        if ([[[UIDevice currentDevice] systemVersion] floatValue] >= 4.0) {
            scale = [screen scale];
        }
        else {
            scale = 0.0;    // mean use old api
        }
    }
    if (scale>0.0) {
        UIGraphicsBeginImageContextWithOptions(size, NO, scale);
    }
    else {
        UIGraphicsBeginImageContext(size);
    }   
}

/*
 void logRect(NSString *prefix, CGRect rect) {
 NSLog(@"%@: %f %f %f %f", prefix, rect.origin.x, rect.origin.y, rect.size.width, rect.size.height);
 }
 
 CGRect rectFor1PxStroke(CGRect rect) {
 return CGRectMake(rect.origin.x + 0.5, rect.origin.y + 0.5, rect.size.width - 1, rect.size.height - 1);
 }
 */
void drawLinearGradient(CGContextRef context, CGRect rect, UIColor * startColor, UIColor * endColor) {

    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
    CGFloat locations[] = { 0.0, 1.0 };
    
    NSArray *colors = [NSArray arrayWithObjects:(__bridge id)startColor.CGColor, (__bridge id)endColor.CGColor, nil];
    
    CGGradientRef gradient = CGGradientCreateWithColors(colorSpace, (__bridge CFArrayRef) colors, locations);

    CGPoint startPoint = CGPointMake(CGRectGetMidX(rect), CGRectGetMinY(rect));
    CGPoint endPoint = CGPointMake(CGRectGetMidX(rect), CGRectGetMaxY(rect));

    CGContextSaveGState(context);
    CGContextAddRect(context, rect);
    CGContextClip(context);
    CGContextDrawLinearGradient(context, gradient, startPoint, endPoint, 0);
    CGContextRestoreGState(context);
    
    CGColorSpaceRelease(colorSpace);
    CGGradientRelease(gradient);
}
/*
 void draw1PxStroke(CGContextRef context, CGPoint startPoint, CGPoint endPoint, CGColorRef color) {
 
 CGContextSaveGState(context);
 CGContextSetLineCap(context, kCGLineCapSquare);
 CGContextSetStrokeColorWithColor(context, color);
 CGContextSetLineWidth(context, 1.0);
 CGContextMoveToPoint(context, startPoint.x + 0.5, startPoint.y + 0.5);
 CGContextAddLineToPoint(context, endPoint.x + 0.5, endPoint.y + 0.5);
 CGContextStrokePath(context);
 CGContextRestoreGState(context);        
 
 }
 */

void drawLinearGloss(CGContextRef context, CGRect rect, BOOL reverse) {
    
	UIColor * highlightStart = [UIColor colorWithRed:1.0 green:1.0 blue:1.0 alpha:0.35];
	UIColor * highlightEnd = [UIColor colorWithRed:1.0 green:1.0 blue:1.0 alpha:0.1];
    
    if (reverse) {
        
		CGRect half = CGRectMake(rect.origin.x, rect.origin.y+rect.size.height/2, rect.size.width, rect.size.height/2);    
		drawLinearGradient(context, half, highlightEnd, highlightStart);
	}
	else {
		CGRect half = CGRectMake(rect.origin.x, rect.origin.y, rect.size.width, rect.size.height/2);    
		drawLinearGradient(context, half, highlightStart, highlightEnd);
	}
    
}

void drawCurvedGloss(CGContextRef context, CGRect rect, CGFloat radius) {
    
	UIColor * glossStart = [UIColor colorWithRed:1.0 green:1.0 blue:1.0 alpha:0.6];
	UIColor * glossEnd = [UIColor colorWithRed:1.0 green:1.0 blue:1.0 alpha:0.1];
    
	//CGFloat radius = 60.0f; //radius of gloss
    
	CGMutablePathRef glossPath = CGPathCreateMutable();
    
	CGContextSaveGState(context);
    CGPathMoveToPoint(glossPath, NULL, CGRectGetMidX(rect), CGRectGetMinY(rect)-radius+rect.size.height/2);
	CGPathAddArc(glossPath, NULL, CGRectGetMidX(rect), CGRectGetMinY(rect)-radius+rect.size.height/2, radius, 0.75f*M_PI, 0.25f*M_PI, YES);	
	CGPathCloseSubpath(glossPath);
	CGContextAddPath(context, glossPath);
	CGContextClip(context);
    
	CGMutablePathRef buttonPath=createRoundedRectForRect(rect, 6.0f);
    
	CGContextAddPath(context, buttonPath);
	CGContextClip(context);
    
	CGRect half = CGRectMake(rect.origin.x, rect.origin.y, rect.size.width, rect.size.height/2);    
    
	drawLinearGradient(context, half, glossStart, glossEnd);
	CGContextRestoreGState(context);
    
	CGPathRelease(buttonPath);
	CGPathRelease(glossPath);
}

/*
 CGMutablePathRef createArcPathFromBottomOfRect(CGRect rect, CGFloat arcHeight) {
 
 CGRect arcRect = CGRectMake(rect.origin.x, rect.origin.y + rect.size.height - arcHeight, 
 rect.size.width, arcHeight);
 
 CGFloat arcRadius = (arcRect.size.height/2) + (pow(arcRect.size.width, 2) / (8*arcRect.size.height));
 CGPoint arcCenter = CGPointMake(arcRect.origin.x + arcRect.size.width/2, arcRect.origin.y + arcRadius);
 
 CGFloat angle = acos(arcRect.size.width / (2*arcRadius));
 CGFloat startAngle = radians(180) + angle;
 CGFloat endAngle = radians(360) - angle;
 
 CGMutablePathRef path = CGPathCreateMutable();
 CGPathAddArc(path, NULL, arcCenter.x, arcCenter.y, arcRadius, startAngle, endAngle, 0);
 CGPathAddLineToPoint(path, NULL, CGRectGetMaxX(rect), CGRectGetMinY(rect));
 CGPathAddLineToPoint(path, NULL, CGRectGetMinX(rect), CGRectGetMinY(rect));
 CGPathAddLineToPoint(path, NULL, CGRectGetMinX(rect), CGRectGetMaxY(rect));
 return path;    
 
 }
 */
CGMutablePathRef createRoundedRectForRect(CGRect rect, CGFloat radius) {
    
    CGMutablePathRef path = CGPathCreateMutable();
    CGPathMoveToPoint(path, NULL, CGRectGetMidX(rect), CGRectGetMinY(rect));
    CGPathAddArcToPoint(path, NULL, CGRectGetMaxX(rect), CGRectGetMinY(rect), CGRectGetMaxX(rect), CGRectGetMaxY(rect), radius);
    CGPathAddArcToPoint(path, NULL, CGRectGetMaxX(rect), CGRectGetMaxY(rect), CGRectGetMinX(rect), CGRectGetMaxY(rect), radius);
    CGPathAddArcToPoint(path, NULL, CGRectGetMinX(rect), CGRectGetMaxY(rect), CGRectGetMinX(rect), CGRectGetMinY(rect), radius);
    CGPathAddArcToPoint(path, NULL, CGRectGetMinX(rect), CGRectGetMinY(rect), CGRectGetMaxX(rect), CGRectGetMinY(rect), radius);
    CGPathCloseSubpath(path);
    
    return path;        
}

CGMutablePathRef createRoundedRectForRectCCW(CGRect rect, CGFloat radius) {
    
    CGMutablePathRef path = CGPathCreateMutable();
    CGPathMoveToPoint(path, NULL, CGRectGetMidX(rect), CGRectGetMinY(rect));
    CGPathAddArcToPoint(path, NULL, CGRectGetMinX(rect), CGRectGetMinY(rect), CGRectGetMinX(rect), CGRectGetMaxY(rect), radius);
    CGPathAddArcToPoint(path, NULL, CGRectGetMinX(rect), CGRectGetMaxY(rect), CGRectGetMaxX(rect), CGRectGetMaxY(rect), radius);
    CGPathAddArcToPoint(path, NULL, CGRectGetMaxX(rect), CGRectGetMaxY(rect), CGRectGetMaxX(rect), CGRectGetMinY(rect), radius);
    CGPathAddArcToPoint(path, NULL, CGRectGetMaxX(rect), CGRectGetMinY(rect), CGRectGetMinX(rect), CGRectGetMinY(rect), radius);
    CGPathCloseSubpath(path);
    
    return path;        
}

NSNumberFormatter * standardNumberFormatter() {
    NSNumberFormatter *formatter = [[NSNumberFormatter alloc] init];
    [formatter setMaximumSignificantDigits:3];
	[formatter setUsesSignificantDigits:YES];
    //[formatter setMinimumSignificantDigits:2];
    //[formatter setMaximumFractionDigits:4];
    //[formatter setMinimumIntegerDigits:1];
    return formatter;
}

NSString * UtcTimestampToString(uint64_t utc, NSString * format)
{
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:format];
    
    NSTimeInterval interval= utc;
    NSDate * date = [NSDate dateWithTimeIntervalSince1970:interval];
    return [dateFormatter stringFromDate:date];
}

NSString * UtcTimestampToPrettyDate(uint64_t utc)
{
    NSTimeInterval interval= utc;
    NSDate * date = [NSDate dateWithTimeIntervalSince1970:interval];
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setAMSymbol:@"am"];
    [dateFormatter setPMSymbol:@"pm"];
    
    if (date.isToday)
    {
        [dateFormatter setDateFormat:@"h:mma"];
        return [NSString stringWithFormat:@"Today at %@",[dateFormatter stringFromDate: date]];
    }
    
    if (date.isYesterday)
    {
        [dateFormatter setDateFormat:@"h:mma"];
        return [NSString stringWithFormat:@"Yesterday at %@",[dateFormatter stringFromDate: date]];
    }
    
    if (date.isLastWeek)
    {
        [dateFormatter setDateFormat:@"EEE 'at' h:mma"];
        return [NSString stringWithFormat:@"Last %@",[dateFormatter stringFromDate: date]];
    }
    
    if (date.isThisYear)
    {
        [dateFormatter setDateFormat:@"MMM dd 'at' h:mma"];
        return [dateFormatter stringFromDate: date];
    }
    
    [dateFormatter setDateFormat:@"yyyy-MM-dd 'at' h:mma"];
    return [dateFormatter stringFromDate: date];
}

NSString * TxContextToString(NSString * txContext)
{
    if ([txContext compare:@"P"] == 0)
        return @"Payment";
    else if ([txContext compare:@"D"] == 0)
        return @"Deposit";
    else
        return @"Unknown";
}

NSString * TxTypeToString(NSString * txType)
{
    if ([txType compare:@"C"] == 0)
        return @"Credit";
    else if ([txType compare:@"D"] == 0)
        return @"Debit";
    else
        return @"Unknown";
}

NSString * fileAtDocumentDirectory(NSString * fileName)
{
    NSString * applicationDocumentsDir = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    return [applicationDocumentsDir stringByAppendingPathComponent:fileName];
}

BOOL fileExistsInDocumentDirectory(NSString * fileName)
{
    NSString * storePath = fileAtDocumentDirectory(fileName);
    return [[NSFileManager defaultManager] fileExistsAtPath:storePath];
}


