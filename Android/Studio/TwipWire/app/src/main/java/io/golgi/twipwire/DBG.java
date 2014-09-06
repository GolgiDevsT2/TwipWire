package io.golgi.twipwire;

import android.util.Log;

/**
 * Created by brian on 9/2/14.
 */
public class DBG {
    public static void write(String where, String str){
        Log.i(where, str);
    }

    public static void write(String str){
        write("TW", str);
    }
}
