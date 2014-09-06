package io.golgi.twipwire;

import android.content.Context;
import android.content.Intent;

/**
 * Created by brian on 9/2/14.
 */
public class GolgiGCMBroadcastReceiver extends io.golgi.apiimpl.android.GolgiGCMBroadcastReceiver{

    public GolgiGCMBroadcastReceiver(){
        super("io.golgi.twipwire", "io.golgi.twipwire.GolgiGCMIntentService");
        DBG.write("Received PUSH");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        DBG.write("Received PUSH(2)");
        TWService.startService(context);
        super.onReceive(context, intent);
        DBG.write("Received PUSH(3)");
    }

}

