//
//  AppData.m
//  Quake Watch
//
//  Created by Brian Kelly on 8/29/14.
//  Copyright (c) 2014 Golgi. All rights reserved.
//

#import "AppData.h"
#import "libGolgi.h"

#define INSTANCE_ID_KEY @"INSTANCE_ID"
#define CURRENT_QUERY_KEY @"CURRENT_QUERY"
#define ENABLED_KEY @"ENABLED"

@implementation AppData


+ (NSString *)getInstanceId
{
    return [GolgiStore getStringForKey:INSTANCE_ID_KEY withDefault:@""];
}

+ (void)setInstanceId:(NSString *)_instanceId
{
    [GolgiStore deleteStringForKey:INSTANCE_ID_KEY];
    [GolgiStore putString:_instanceId forKey:INSTANCE_ID_KEY];
}

+ (BOOL)getEnabled
{
    return ([GolgiStore getIntegerForKey:ENABLED_KEY withDefault:0] != 0) ? true : false;
}

+ (void)setEnabled:(BOOL)enabled
{
    [GolgiStore deleteIntegerForKey:ENABLED_KEY];
    [GolgiStore putInteger:(enabled ? 1 : 0) forKey:ENABLED_KEY];
}

+ (NSString *)getCurrentQuery
{
    return [GolgiStore getStringForKey:CURRENT_QUERY_KEY withDefault:@""];
}


+ (void)setCurrentQuery:(NSString *)query
{
    [GolgiStore deleteStringForKey:CURRENT_QUERY_KEY];
    [GolgiStore putString:query forKey:CURRENT_QUERY_KEY];
    
}

@end
