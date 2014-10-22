//
//  AppData.h
//  Quake Watch
//
//  Created by Brian Kelly on 8/29/14.
//  Copyright (c) 2014 Golgi. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface AppData : NSObject

+ (NSString *)getInstanceId;
+ (void)setInstanceId:(NSString *)instanceId;

+ (BOOL)getEnabled;
+ (void)setEnabled:(BOOL)enabled;

+ (NSString *)getCurrentQuery;
+ (void)setCurrentQuery:(NSString *)query;
@end
