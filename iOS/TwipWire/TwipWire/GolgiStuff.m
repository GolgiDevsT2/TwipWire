//
//  GolgiStuff.m
//  Quake Watch
//
//  Created by Brian Kelly on 8/29/14.
//  Copyright (c) 2014 Golgi. All rights reserved.
//

#import <AudioToolbox/AudioToolbox.h>
#import "GolgiStuff.h"
#import "AppData.h"
#import "GOLGI_KEYS.h"
#import "TwipWireSvcWrapper.h"


@implementation GolgiStuff
@synthesize tweetDb;
@synthesize viewController;
@synthesize popId;

- (void)startStreaming:(NSString *)query
{
    TweetFilter *tweetFilter = [[TweetFilter alloc]init];
    
    [tweetFilter setGolgiId:[AppData getInstanceId]];
    [tweetFilter setQuery:query];
    
    [TwipWireSvc sendStartStreamingUsingResultHandler:^(TwipWireStartStreamingExceptionBundle *exBundle)
                                                     {
                                                         if(exBundle == nil){
                                                             NSLog(@"startStreaming: SUCCESS");
                                                         }
                                                         else{
                                                             NSLog(@"startStreaming: FAILED '%@'", [exBundle.golgiException getErrText]);
                                                         }
                                                     }
                                 withTransportOptions:stdGto
                                       andDestination:@"SERVER"
                                           withFilter:tweetFilter];
    
}

- (void)stopStreaming
{
    [TwipWireSvc sendStopStreamingUsingResultHandler:^(TwipWireStopStreamingExceptionBundle *exBundle)
                                                    {
                                                        if(exBundle == nil){
                                                            NSLog(@"stopStreaming: SUCCESS");
                                                        }
                                                        else{
                                                            NSLog(@"stopStreaming: FAILED '%@'", [exBundle.golgiException getErrText]);
                                                        }
                                                    }
                                withTransportOptions:stdGto
                                      andDestination:@"SERVER"
                                         withGolgiId:[AppData getInstanceId]];
}



// GOLGI
//********************************* Registration ***************************
//
// Setup handling of inbound SendMessage methods and then Register with Golgi
//
- (void)doGolgiRegistration
{
    //
    // Do this before registration because on registering, there may be messages queued
    // up for us that would arrive and be rejected because there is no handler in place
    //
    
    // [TapTelegraphSvc registerSendMessageRequestReceiver:self];
    
    //
    // and now do the main registration with the service
    //
    NSLog(@"Registering with golgiId: '%@'", ourId);
    // [Golgi setOption:@"USE_DEV_CLUSTER" withValue:@"0"];

    
    [TwipWireSvc registerNewTweetRequestHandler:^(id<TwipWireNewTweetResultSender> resultSender, TweetDetails *tweetDetails) {
        NSLog(@"Received tweet from: '%@' Content: '%@'", [tweetDetails getName], [tweetDetails getText]);
        [tweetDb addTweet:tweetDetails];
        [viewController newTweets];
        [resultSender success];
        AudioServicesPlaySystemSound(popId);
        
        UILocalNotification* localNotification = [[UILocalNotification alloc] init];
        localNotification.alertBody = [NSString stringWithFormat:@"Tweet by %@", [tweetDetails getName]];
        [[UIApplication sharedApplication] cancelAllLocalNotifications];
        [[UIApplication sharedApplication] scheduleLocalNotification:localNotification];
        NSLog(@"D");
        AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
        NSLog(@"E");
    }];

    [Golgi registerWithDevId:GOLGI_DEV_KEY appId:GOLGI_APP_KEY instId:ourId andResultHandler:^(NSString *errorText) {
        if(errorText == nil){
            NSLog(@"Golgi Registration: PASS");
        }
        else{
            NSLog(@"Golgi Registration: FAIL => '%@'", errorText);
        }
    }];
}

- (void)setPushId:(NSString *)_pushId
{
    if([pushId  compare:_pushId] != NSOrderedSame){
        pushId = _pushId;
        [self doGolgiRegistration];
    }
}

- (NSString *)pushTokenToString:(NSData *)token
{
    NSMutableString *hexStr = [[NSMutableString alloc]init];
    
    for(int i = 0; i < token.length; i++){
        [hexStr appendFormat:@"%02x", ((unsigned char *)[token bytes])[i]];
    }
    
    return [NSString stringWithString:hexStr];
}

- (GolgiStuff *)initWithViewController:(ViewController *)_viewController
{
    self = [self init];
    viewController = _viewController;
    
    NSURL *popUrl = [NSURL fileURLWithPath:[[NSBundle mainBundle] pathForResource:@"FB-pop" ofType:@"mp3"]];
    AudioServicesCreateSystemSoundID((__bridge CFURLRef)popUrl, &popId);

    
    stdGto = [[GolgiTransportOptions alloc] init];
    [stdGto setValidityPeriodInSeconds:300];
    
    tweetDb = [[TweetDb alloc] init];

    
    ourId = [AppData getInstanceId];
    if(ourId.length == 0){
        NSMutableString *str = [[NSMutableString alloc]init];
        
        srand48((long)CACurrentMediaTime());
        for(int i = 0; i < 20; i++){
            char ch;
            ch = lrand48() % 62;
            if(ch < 26){
                ch = 'A' + ch;
            }
            else if(ch < 52){
                ch = 'a' + (ch - 26);
            }
            else{
                ch = '0' + (ch - 52);
            }
            [str appendFormat:@"%c", ch];
        }
        
        ourId = [NSString stringWithString:str];
        [AppData setInstanceId:ourId];
    }
    
    NSLog(@"Instance Id: '%@'", ourId);
    
    pushId = @"";
    
    
    [self doGolgiRegistration];
    
    return self;
}

@end
