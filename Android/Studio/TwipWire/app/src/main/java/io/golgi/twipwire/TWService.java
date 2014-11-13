package io.golgi.twipwire;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.openmindnetworks.golgi.api.GolgiAPI;
import com.openmindnetworks.golgi.api.GolgiAPIHandler;
import io.golgi.apiimpl.android.GolgiAbstractService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import io.golgi.twipwire.gen.GolgiKeys;
import io.golgi.twipwire.gen.TweetDetails;
import io.golgi.twipwire.gen.TwipWireService;

/**
 * Created by brian on 9/2/14.
 */
public class TWService extends GolgiAbstractService {
    private static Object syncObj = new Object();
    private static TWActivity twActivity = null;
    private TweetDb tweetDb;

    /*
    public static class GolgiGCMBroadcastReceiver extends io.golgi.apiimpl.android.GolgiGCMBroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            DBG("Hello from GolgiGCMBroadcastReceiver in TWService  **********");
            super.onReceive(context, intent);

        }
    }
    */



    private static void DBG(String str){
        DBG.write("SVC", str);
    }

    public static void setQWActivity(TWActivity activity){
        synchronized(syncObj) {
            twActivity = activity;
        }
    }

    public void playTweetSound(TweetDetails deets){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String uriStr;
        uriStr = sharedPrefs.getString("newTweetSound", "");
        if(uriStr.length() == 0){
            uriStr = Settings.System.DEFAULT_NOTIFICATION_URI.toString();
        }

        NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(context, TWActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this.context, 0, intent, 0);

        NotificationCompat.Builder builder;



        builder = new NotificationCompat.Builder(this.context).setContentTitle(deets.getName()).
                setContentText(deets.getText()).
                setSmallIcon(R.drawable.ic_stat_twitter_logo_blue).
                setContentIntent(contentIntent);

        builder.setSound(Uri.parse(uriStr));


        Notification notification = builder.getNotification();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(1234, notification);
    }


    @Override
    public void readyForRegister() {

        TwipWireService.newTweet.registerReceiver(new TwipWireService.newTweet.RequestReceiver() {
            @Override
            public void receiveFrom(TwipWireService.newTweet.ResultSender resultSender, TweetDetails tweetDetails) {
                // DBG.write("Received a tweet: " + tweetDetails.getText());
                tweetDb.addTweet(tweetDetails);
                resultSender.success();

                ArrayList<TweetDetails> al = new ArrayList<TweetDetails>();
                TweetDetails[] allTweets = tweetDb.getAllTweets();
                if(allTweets.length > 200) {
                    for (int i = 0; i < allTweets.length; i++) {
                        al.add(allTweets[i]);
                    }

                    Collections.sort(al, new Comparator<TweetDetails>() {
                        @Override
                        public int compare(TweetDetails lhs, TweetDetails rhs) {
                            return rhs.getTimestamp() - lhs.getTimestamp();
                        }
                    });
                    for (int i = 200; i < al.size(); i++) {
                        // DBG.write("Delete " + al.get(i).getText());
                        tweetDb.delTweet(al.get(i));
                    }
                }
                playTweetSound(tweetDetails);
                TWActivity.tweetArrived();

            }
        });

        registerGolgi(
                new GolgiAPIHandler() {
                    @Override
                    public void registerSuccess() {
                        DBG("Golgi registration Success");
                        if (twActivity != null) {
                            twActivity.golgiRegistrationComplete(true);
                        }
                    }

                    @Override
                    public void registerFailure() {
                        DBG("Golgi registration Failure");
                        if (twActivity != null) {
                            twActivity.golgiRegistrationComplete(false);
                        }
                    }
                },
                GolgiKeys.DEV_KEY,
                GolgiKeys.APP_KEY,
                TWActivity.getGolgiId(this));


    }

    @Override
    public void onCreate(){
        super.onCreate();
        tweetDb = new TweetDb(this);
    }


}
