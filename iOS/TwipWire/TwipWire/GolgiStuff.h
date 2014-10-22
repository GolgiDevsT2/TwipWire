//
//  GolgiStuff.h
//  Quake Watch
//
//  Created by Brian Kelly on 8/29/14.
//  Copyright (c) 2014 Golgi. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <AudioToolbox/AudioToolbox.h>
#import <CoreLocation/CoreLocation.h>
#import "TwipWireSvcWrapper.h"
#import "ViewController.h"
#import "TweetDb.h"

@class ViewController;

@interface GolgiStuff : NSObject
{
    NSString *ourId;
    NSString *pushId;
    GolgiTransportOptions *stdGto;
}

@property TweetDb *tweetDb;
@property ViewController *viewController;
@property SystemSoundID popId;

- (void)startStreaming:(NSString *)query;
- (void)stopStreaming;


- (NSString *)pushTokenToString:(NSData *)token;
- (void)setPushId:(NSString *)_pushId;
- (GolgiStuff *)initWithViewController:(ViewController *)viewController;


@end
