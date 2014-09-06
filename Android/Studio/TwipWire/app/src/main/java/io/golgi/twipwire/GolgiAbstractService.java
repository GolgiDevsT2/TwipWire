package io.golgi.twipwire;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import com.openmindnetworks.golgi.api.GolgiAPI;
import com.openmindnetworks.golgi.api.GolgiAPIHandler;

import java.text.DateFormat;
import java.util.Date;

import io.golgi.apiimpl.android.GolgiService;

/**
 * Created by brian on 9/2/14.
 */
public abstract class GolgiAbstractService extends GolgiService {
    private static Object syncObj = new Object();

    private static String getServiceClassName(Context context){
        Resources r = context.getResources();
        String str;
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            str = ai.metaData.getString("GolgiServiceClassName");
        }
        catch(PackageManager.NameNotFoundException nnf){
            str = "";
        }
        return str;
    }

    private static String getServicePackageName(Context context){
        String str = getServiceClassName(context);
        int idx;
        if(str != null && (idx = str.lastIndexOf('.')) > 0){
            str = str.substring(0, idx);
        }
        else{
            str = "";
        }

        return str;
    }

    public static boolean isRunning(Context context) {
        return GolgiService.isRunning(context, getServiceClassName(context));
    }


    public static void startService(Context context){
        GolgiService.startService(context, getServicePackageName(context), getServiceClassName(context));
    }

    public static void stopService(Context context){
        GolgiService.stopService(context, getServicePackageName(context), getServiceClassName(context));
    }

    public void registerGolgi(GolgiAPIHandler apiHandler, String devKey, String appKey, String id) {
        GolgiAPI.getInstance().register(
                devKey,
                appKey,
                id,
                apiHandler);
    }


    public abstract void registerGolgi();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        registerGolgi();

        return START_STICKY;

    }

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }
}
