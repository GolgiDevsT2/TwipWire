//
//  AppData.m
//  Quake Watch
//
//  Created by Brian Kelly on 8/29/14.
//  Copyright (c) 2014 Golgi. All rights reserved.
//

#import "AppData.h"

static AppData *instance = nil;


@implementation AppData

+ (AppData *)getInstance
{
    if(instance == nil){
        instance = [[AppData alloc] init];
    }
    
    return instance;
}

+ (NSString *)getInstanceId
{
    return [[AppData getInstance] _getInstanceId];
}

+ (void)setInstanceId:(NSString *)_instanceId
{
    [[AppData getInstance] _setInstanceId:_instanceId];
}

+ (BOOL)getEnabled
{
    return [[AppData getInstance] _getEnabled];
}

+ (void)setEnabled:(BOOL)enabled
{
    [[AppData getInstance] _setEnabled:enabled];
}

+ (NSString *)getCurrentQuery
{
    return [[AppData getInstance] _getCurrentQuery];
}


+ (void)setCurrentQuery:(NSString *)query
{
    [[AppData getInstance] _setCurrentQuery:query];
}



/*********************************************************************/

- (NSString *)_getInstanceId
{
    return instanceId;
}

- (void)_setInstanceId:(NSString *)_instanceId
{
    instanceId = _instanceId;
    [self save];
}

- (NSString *)_getCurrentQuery
{
    return [currentQuery stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
}

- (void)_setCurrentQuery:(NSString *)_currentQuery
{
    currentQuery = _currentQuery;
    [self save];
}

- (BOOL)_getEnabled
{
    return enabled;
}

- (void)_setEnabled:(BOOL)_enabled
{
    enabled = _enabled;
    [self save];
}


- (void)save
{
    NSString *error = nil;
    NSString *rootPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSString *plistPath = [rootPath stringByAppendingPathComponent:@"AppData.plist"];
    
    NSDictionary *plistDict = [NSDictionary dictionaryWithObjects:
                               [NSArray arrayWithObjects:
                                [NSString stringWithString:instanceId],
                                [NSString stringWithString:currentQuery],
                                [NSString stringWithFormat:@"%d", enabled],
                                nil]
                               
                                                          forKeys:[NSArray arrayWithObjects:
                                                                   @"instanceId",
                                                                   @"currentQuery",
                                                                   @"enabled",
                                                                   nil]
                               ];
    
    NSData *plistData = [NSPropertyListSerialization dataFromPropertyList:plistDict
                                                                   format:NSPropertyListXMLFormat_v1_0
                                                         errorDescription:&error];
    if(plistData) {
        [plistData writeToFile:plistPath atomically:YES];
    }
    else {
        NSLog(@"Error Writing GameData: %@", error);
    }
}

- (AppData *)init
{
    self = [super init];
    
    instanceId = @"";
    currentQuery = @"";
    enabled = YES;
    
    NSString *str;
    
    NSString *errorDesc = nil;
    NSPropertyListFormat format;
    NSString *plistPath;
    NSString *rootPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory,
                                                              NSUserDomainMask, YES) objectAtIndex:0];
    plistPath = [rootPath stringByAppendingPathComponent:@"AppData.plist"];
    if (![[NSFileManager defaultManager] fileExistsAtPath:plistPath]) {
        plistPath = [[NSBundle mainBundle] pathForResource:@"AppData" ofType:@"plist"];
    }
    NSData *plistXML = [[NSFileManager defaultManager] contentsAtPath:plistPath];
    NSDictionary *temp = (NSDictionary *)[NSPropertyListSerialization
                                          propertyListFromData:plistXML
                                          mutabilityOption:NSPropertyListMutableContainersAndLeaves
                                          format:&format
                                          errorDescription:&errorDesc];
    if (!temp) {
        NSLog(@"Error reading plist: %@, format: %d", errorDesc, (int)format);
    }
    else {
        if((str = [temp objectForKey:@"instanceId"]) != nil){
            instanceId = str;
        }
        if((str = [temp objectForKey:@"currentQuery"]) != nil){
            currentQuery = str;
        }
        if((str = [temp objectForKey:@"enabled"]) != nil){
            enabled = (atol([str UTF8String]) ? YES : NO);
        }
    }
    
    NSLog(@"  Instance Id: '%@'", instanceId);
    
    return self;
}



@end
