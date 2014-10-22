//
//  MsgDb.m
//  SuperMail
//
//  Created by Brian Kelly on 03/04/2012.
//  Copyright (c) 2012 Openmind Networks. All rights reserved.
//

#import "TweetDb.h"


@implementation TweetDb
@synthesize db;


- (NSArray *)getAllTweets
{
    NSMutableArray *tweets = [[NSMutableArray alloc]init];
    sqlite3_stmt *statement;
    
    NSString *qry = [NSString stringWithFormat:@"SELECT "
                     "ENCODING "
                     "FROM TWEETS ORDER BY DBKEY DESC;"];
    
    NSLog(@"Get All Query: '%@'\n", qry);
    if(sqlite3_prepare_v2(db, [qry UTF8String], -1, &statement, nil) == SQLITE_OK){
        NSLog(@"Get All Query success");
        while(sqlite3_step(statement) == SQLITE_ROW){
            NSString *str = [NSString stringWithUTF8String:(const char *) sqlite3_column_text(statement, 0)];
            TweetDetails *deets = [TweetDetails deserialiseFromString:str];
            if(deets != nil){
                [tweets addObject:deets];
            }
        }
    }
    else{
        NSLog(@"FAILED(1) to get all tweets");
        NSLog(@"Error: '%s'", sqlite3_errmsg(db));
    }

    
    NSLog(@"There are %ld tweets", (unsigned long)tweets.count);
    
    return tweets;
}

- (NSString *)addTweet:(TweetDetails *)deets
{
    NSString *key, *serData;
    sqlite3_stmt *statement;   
    NSString *insQry = @"INSERT INTO TWEETS (DBKEY,ENCODING) VALUES(?,?);";
        
    if(sqlite3_prepare_v2(db, [insQry UTF8String], -1, &statement, nil) == SQLITE_OK){
        key = [NSString stringWithFormat:@"%ld:%@", (long)[deets getTimestamp], [deets getUsername]];
        serData = [deets serialise];
        sqlite3_bind_text(statement, 1, [key UTF8String], -1, NULL);
        sqlite3_bind_text(statement, 2, [serData UTF8String], -1, NULL);
        
        if(sqlite3_step(statement) != SQLITE_DONE){
            NSLog(@"'%@' FAILED(1) to insert\n", key);                    
        }
        else{
            NSLog(@"'%@' inserted OK", key);
        }
        sqlite3_finalize(statement);
    }
    else{
        NSLog(@"'%@' FAILED(2) to insert int %p\n", [deets getName], db);
        NSLog(@"Error: '%s'", sqlite3_errmsg(db));
        key = NULL;
    }
    
    [self pruneTweets];
        
    return key;
}

- (void)deleteTweet:(NSString *)key
{
    NSString *delQry = [NSString stringWithFormat:@"DELETE FROM TWEETS where DBKEY like '%@';", key];
    sqlite3_stmt *statement;
    
    if(sqlite3_prepare_v2(db, [delQry UTF8String], -1, &statement, nil) == SQLITE_OK){
        if(sqlite3_step(statement) != SQLITE_DONE){
            NSLog(@"FAILED(1) to delete a tweet\n");
        }
        else{
            NSLog(@"delete OK");
        }
        sqlite3_finalize(statement);
    }
    else{
        NSLog(@"FAILED(2) to delete a tweet");
        NSLog(@"Error: '%s'", sqlite3_errmsg(db));
    }
}

- (void)pruneTweets
{
    NSMutableArray *forPrunage = [[NSMutableArray alloc]init];
    sqlite3_stmt *statement;
    NSInteger i;
    
    NSString *qry = [NSString stringWithFormat:@"SELECT "
                     "DBKEY "
                     "FROM TWEETS ORDER BY DBKEY DESC;"];
    
    NSLog(@"Get All Query: '%@'\n", qry);
    if(sqlite3_prepare_v2(db, [qry UTF8String], -1, &statement, nil) == SQLITE_OK){
        NSLog(@"Get All Query success");
        i = 0;
        while(sqlite3_step(statement) == SQLITE_ROW){
            if(i >= 200){
                NSString *str = [NSString stringWithUTF8String:(const char *) sqlite3_column_text(statement, 0)];
                [forPrunage addObject:str];
            }
            i++;
        }
    }
    else{
        NSLog(@"FAILED(1) to get all tweets");
        NSLog(@"Error: '%s'", sqlite3_errmsg(db));
    }
    
    for(i = 0; i < forPrunage.count; i++){
        [self deleteTweet:[forPrunage objectAtIndex:i]];
    }
}

- (void)delAllTweets
{
    sqlite3_stmt *statement;
    NSString *insQry = @"DELETE FROM TWEETS;";
    
    if(sqlite3_prepare_v2(db, [insQry UTF8String], -1, &statement, nil) == SQLITE_OK){
        if(sqlite3_step(statement) != SQLITE_DONE){
            NSLog(@"FAILED(1) to delete all tweets\n");
        }
        else{
            NSLog(@"delete OK");
        }
        sqlite3_finalize(statement);
    }
    else{
        NSLog(@"FAILED(2) to delete all tweets");
        NSLog(@"Error: '%s'", sqlite3_errmsg(db));
    }
}

- (NSString *)dbFilePath
{
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *docsDir = [paths objectAtIndex:0];
    return [docsDir stringByAppendingPathComponent:@"tweetdb-v1.sql"];
}

- (TweetDb *)init
{
    char *err;
    self = [super init];
    
    if(sqlite3_open([[self dbFilePath] UTF8String], &db) != SQLITE_OK){
        sqlite3_close(db);
        NSAssert(0, @"Failed to open Database");
    }
    
    NSString *sql = @"CREATE TABLE IF NOT EXISTS TWEETS"
    "("
    "DBKEY TEXT PRIMARY KEY,"
    "ENCODING TEXT"
    ");";
    
    if(sqlite3_exec(db, [sql UTF8String], NULL, NULL, &err) != SQLITE_OK){
        NSLog(@"Failed to create the table: %s", err);
    }
    NSLog(@"Created the SQLite table\n");
    
    startTime = (int)time(NULL);
    nextKey = 0;
    
    return self;
}


@end
