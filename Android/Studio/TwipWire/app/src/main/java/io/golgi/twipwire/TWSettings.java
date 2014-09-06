package io.golgi.twipwire;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by brian on 9/3/14.
 */
public class TWSettings extends PreferenceActivity {
    private static void DBG(String str){
        DBG.write(str);
    }

    protected void onDestroy(){
        super.onDestroy();
        DBG.write("Destroy Preference Activity");
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}
