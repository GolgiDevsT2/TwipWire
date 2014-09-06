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



@interface _StartStreamingResultReceiver: NSObject <TwipWireStartStreamingResultReceiver>
@end
@implementation _StartStreamingResultReceiver
- (void)success
{
    NSLog(@"startStreaming: SUCCESS");
}

- (void)failureWithGolgiException:(GolgiException *)golgiException
{
    NSLog(@"startStreaming: FAILED '%@'", [golgiException getErrText]);
}

@end
@interface _StopStreamingResultReceiver: NSObject <TwipWireStopStreamingResultReceiver>
@end
@implementation _StopStreamingResultReceiver
- (void)success
{
    NSLog(@"stopStreaming: SUCCESS");
}

- (void)failureWithGolgiException:(GolgiException *)golgiException
{
    NSLog(@"stopStreaming: FAILED '%@'", [golgiException getErrText]);
}

@end

@interface CombinedRequestReceiver: NSObject <TwipWireNewTweetRequestReceiver>
{
    ViewController *viewController;
    GolgiStuff *golgiStuff;
}
- (CombinedRequestReceiver *)initWithViewController:(ViewController *)viewController andGolgiStuff:(GolgiStuff *)golgiStuff;
@end
@implementation CombinedRequestReceiver

- (void)newTweetWithResultSender:(id<TwipWireNewTweetResultSender>)resultSender andTweetDetails:(TweetDetails *)tweetDetails
{
    NSLog(@"Received tweet from: '%@' Content: '%@'", [tweetDetails getName], [tweetDetails getText]);
    [golgiStuff.tweetDb addTweet:tweetDetails];
    [golgiStuff.viewController newTweets];
    [resultSender success];
    AudioServicesPlaySystemSound(golgiStuff.popId);
}


- (CombinedRequestReceiver *)initWithViewController:(ViewController *)_viewController andGolgiStuff:(GolgiStuff *)_golgiStuff
{
    self = [self init];
    
    viewController = _viewController;
    golgiStuff = _golgiStuff;
    
    return self;
}

@end



@implementation GolgiStuff
@synthesize tweetDb;
@synthesize viewController;
@synthesize popId;

- (void)startStreaming:(NSString *)query
{
    TweetFilter *tweetFilter = [[TweetFilter alloc]init];
    
    [tweetFilter setGolgiId:[AppData getInstanceId]];
    [tweetFilter setQuery:query];
    
    [TwipWireSvc sendStartStreamingUsingResultReceiver:[[_StartStreamingResultReceiver alloc] init] withTransportOptions:stdGto andDestination:@"SERVER" withFilter:tweetFilter];
}

- (void)stopStreaming
{
    [TwipWireSvc sendStopStreamingUsingResultReceiver:[[_StopStreamingResultReceiver alloc]init] withTransportOptions:stdGto andDestination:@"SERVER" withGolgiId:[AppData getInstanceId]];
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
    
    CombinedRequestReceiver *crr = [[CombinedRequestReceiver alloc] initWithViewController:viewController andGolgiStuff:self];
    
    [TwipWireSvc registerNewTweetRequestReceiver:crr];

    
    [Golgi registerWithDevId:GOLGI_DEV_KEY
                       appId:GOLGI_APP_KEY
                      instId:ourId
                  andAPIUser:self];
}

//
// Registration worked
//

- (void)golgiRegistrationSuccess
{
    NSLog(@"Golgi Registration: PASS");
    
    /*
    qf = [[QuakeFilter alloc] initWithIsSet:true];
    
    [qf setGolgiId: ourId];
    [qf setLat:0.0];
    [qf setLng:0.0];
    [qf setRadius:0.0];
    
    

    [QuakeWatchSvc sendAddMeUsingResultReceiver:[[_AddMeResultReceiver alloc]init]
                           withTransportOptions:stdGto
                                 andDestination:@"SERVER"
                                     withFilter:qf];
     */

}

//
// Registration failed
//

- (void)golgiRegistrationFailure:(NSString *)errorText
{
    NSLog(@"Golgi Registration: FAIL => '%@'", errorText);
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
