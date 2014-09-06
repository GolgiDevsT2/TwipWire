//
//  ViewController.h
//  TwipWire
//
//  Created by Brian Kelly on 9/4/14.
//  Copyright (c) 2014 Golgi. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TweetCell.h"
#import "GolgiStuff.h"

@class GolgiStuff;

@interface ViewController : UIViewController <UITableViewDataSource, UITableViewDelegate>
{
    GolgiStuff *golgiStuff;
    TweetCell *offScreenCell;
    NSArray *allTweets;
}

@property IBOutlet UISwitch *enabledSwitch;
@property IBOutlet UILabel *currentQueryLabel;
@property IBOutlet UITextField *queryText;
@property IBOutlet UITableView *tableView;

- (IBAction)enabledValueChanged:(id)sender;
- (IBAction)applyPressed:(UIButton *)sender;
- (void)newTweets;
@end
