//
//  MsgDb.h
//  SuperMail
//
//  Created by Brian Kelly on 03/04/2012.
//  Copyright (c) 2012 Openmind Networks. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "sqlite3.h"
#import "TwipWireSvcWrapper.h"

@interface TweetDb : NSObject
{
    sqlite3 *db;
    int startTime;
    int nextKey;
}

@property (readonly) sqlite3 *db;

- (NSArray *)getAllTweets;
- (NSString *)addTweet:(TweetDetails *)msg;
- (void)delAllTweets;



@end
