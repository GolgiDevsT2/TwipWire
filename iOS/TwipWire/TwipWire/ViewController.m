//
//  ViewController.m
//  TwipWire
//
//  Created by Brian Kelly on 9/4/14.
//  Copyright (c) 2014 Golgi. All rights reserved.
//

#import "ViewController.h"
#import "TweetCell.h"
#import "AppData.h"
#import "TwipWireSvcWrapper.h"


@interface ViewController ()

@end

@implementation ViewController
@synthesize queryText;
@synthesize enabledSwitch;
@synthesize currentQueryLabel;
@synthesize tableView;

- (void)scrollViewWillBeginDragging:(UIScrollView *)scrollView
{
    NSLog(@"Start dragging");
    [queryText resignFirstResponder];
}


- (void)showCurrentQuery:(NSString *)query
{
    [currentQueryLabel setText:[NSString stringWithFormat:@"Query: %@", query]];
}

- (IBAction)enabledValueChanged:(UISwitch *)s
{
    NSLog(@"Switch Cganged %d", s.on);
    [AppData setEnabled:s.on];
    if(!s.on){
        //
        // Call stopStreaming
        //
        [golgiStuff stopStreaming];
    }
    else{
        //
        // Call startStreaming *if* current query non-empty
        //
        NSString *str = [AppData getCurrentQuery];
        
        if(str.length > 0){
            [golgiStuff startStreaming:str];
        }
    }
}

- (IBAction)applyPressed:(UIButton *)sender
{
    NSLog(@"Apply Pressed: '%@'", queryText.text);
    NSString *str = [queryText.text stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    [queryText resignFirstResponder];
    
    if(str.length > 0){
        //
        // Only do something if the query is non-empty
        //
        [golgiStuff.tweetDb delAllTweets];
        [tableView reloadData];
        [AppData setCurrentQuery:str];
        [self showCurrentQuery:str];
        [golgiStuff startStreaming:str];
        enabledSwitch.on = YES;
        
    }
}

- (TweetDetails *)tweetForRow:(NSInteger) row
{
    TweetDetails *deets;
    
    if(row < allTweets.count){
        deets = [allTweets objectAtIndex:row];
    }
    else{
        deets = [[TweetDetails alloc] initWithIsSet:YES];
    }
    
    return deets;
}

- (NSString *)textForRow:(NSInteger) row
{
    return [[self tweetForRow:row] getText];
}
- (CGFloat) tableView:(UITableView *)tableView estimatedHeightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 64.0;
}

- (CGFloat)tableView:(UITableView *)tView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if(offScreenCell == nil){
        offScreenCell = [tView dequeueReusableCellWithIdentifier:@"XXX"];
    }
    
    [offScreenCell setNeedsUpdateConstraints];
    [offScreenCell updateConstraintsIfNeeded];
    
    offScreenCell.bounds = CGRectMake(0.0f, 0.0f, CGRectGetWidth(tableView.bounds), 1000.f);
    
    offScreenCell.contentTv.selectable = YES;
    [offScreenCell.contentTv setText:[self textForRow:indexPath.row]];
    offScreenCell.contentTv.selectable = NO;
    
    [offScreenCell sizeToFit];
    [offScreenCell setNeedsLayout];
    [offScreenCell layoutIfNeeded];
    
    
    
    [offScreenCell setNeedsLayout];
    [offScreenCell layoutIfNeeded];
    
    // Get the actual height required for the cell's contentView
    CGFloat height = [offScreenCell.contentView systemLayoutSizeFittingSize:UILayoutFittingCompressedSize].height;

    height += 1.0;
    
    // NSLog(@"Row %ld height: %f", indexPath.row, height);
    CGSize s = [offScreenCell.contentTv sizeThatFits:offScreenCell.contentTv.frame.size];
    NSLog(@"Content %ld Size: %f", (long)indexPath.row, s.height);
    
    height = s.height + 20.0 + 1.0;
    
    return height;
}


#pragma mark - Table view data source


- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if(golgiStuff != nil){
        allTweets = [golgiStuff.tweetDb getAllTweets];
    }
    else{
        allTweets = [[NSArray alloc] init];
    }
    
    NSLog(@"There are %ld tweets", (long)allTweets.count);
    return allTweets.count;
}

- (void)newTweets
{
    if([self isViewLoaded]){
        [tableView reloadData];
    }
}

- (UITableViewCell *)tableView:(UITableView *)tView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    TweetCell *cell = [tView dequeueReusableCellWithIdentifier:@"XXX" forIndexPath:indexPath];
    TweetDetails *deets = [self tweetForRow:indexPath.row];
 
    // Configure the cell...
    cell.contentTv.selectable = YES;
    [cell.contentTv setText:[deets getText]];
    cell.contentTv.selectable = NO;
    [cell.nameLabel setText:[deets getName]];
    [cell.userIv setImage:[UIImage imageWithData:[deets getImage]]];
    
    int now = (int)time(NULL);
    int delay = now - [deets getTimestamp];
    
    NSDate *date = [NSDate dateWithTimeIntervalSince1970:(double)[deets getTimestamp]];
    NSString *dstr;

    if(delay > 86400){
        dstr = [NSDateFormatter localizedStringFromDate:date dateStyle:NSDateFormatterShortStyle timeStyle:NSDateFormatterShortStyle];
        
    }
    else if(delay > 900){
        dstr = [NSDateFormatter localizedStringFromDate:date dateStyle:NSDateFormatterShortStyle timeStyle:NSDateFormatterShortStyle];
    }
    else if(delay > 60){
        dstr = [NSString stringWithFormat:@"%d minutes ago", (delay / 60)];
    }
    else{
        dstr = [NSString stringWithFormat:@"%d seconds ago", delay];
    }
    
    
    [cell.timestampLabel setText:dstr];

    
    
    [cell setNeedsUpdateConstraints];
    [cell updateConstraintsIfNeeded];
    [cell sizeToFit];
    [cell setNeedsLayout];
    [cell layoutIfNeeded];

    
    return cell;
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    if(golgiStuff == nil){
        golgiStuff = [[GolgiStuff alloc] initWithViewController:self];
    }
    [self showCurrentQuery:[AppData getCurrentQuery]];
    [enabledSwitch setOn:[AppData getEnabled]];
    
    [tableView reloadData];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
