//
//  TweetCell.h
//  TwipWire
//
//  Created by Brian Kelly on 9/4/14.
//  Copyright (c) 2014 Golgi. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface TweetCell : UITableViewCell

@property IBOutlet UIImageView *userIv;
@property IBOutlet UILabel *nameLabel;
@property IBOutlet UILabel *timestampLabel;
@property IBOutlet UILabel *contentLabel;

@end
