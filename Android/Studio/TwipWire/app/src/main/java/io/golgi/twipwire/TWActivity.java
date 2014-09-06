package io.golgi.twipwire;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.openmindnetworks.golgi.api.GolgiAPI;
import com.openmindnetworks.golgi.api.GolgiException;
import com.openmindnetworks.golgi.api.GolgiTransportOptions;

import io.golgi.twipwire.gen.TweetFilter;
import io.golgi.twipwire.gen.TwipWireService;


public class TWActivity extends Activity {
    private static Object syncObj = new Object();
    private static TWActivity theInstance;

    private static final String NEWTWEET = "NEWTWEET";
    public static final int SETTINGS_COMPLETE = 1;


    private GolgiTransportOptions stdGto;
    private TweetListAdapter tweetListAdapter;
    private CheckBox enabledCb;
    private boolean inFg;

    public static TWActivity getInstance(){
        return theInstance;
    }

    private static SharedPreferences getSharedPrefs(Context context) {
        return context.getSharedPreferences("TwipWire", Context.MODE_PRIVATE);
    }


    public static String getGolgiId(Context context) {
        SharedPreferences sharedPrefs = getSharedPrefs(context);
        String golgiId = sharedPrefs.getString("GOLGI-ID", "");

        if (golgiId.length() == 0) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < 20; i++) {
                sb.append((char) ('A' + (int) (Math.random() * ('z' - 'A'))));
            }

            golgiId = sb.toString();
            sharedPrefs.edit().putString("GOLGI-ID", golgiId).commit();
        }
        return golgiId;
    }

    public static String getCurrentQuery(Context context) {
        SharedPreferences sharedPrefs = getSharedPrefs(context);
        return sharedPrefs.getString("TW-QUERY", "").trim();
    }

    public static void setCurrentQuery(Context context, String query){
        SharedPreferences sharedPrefs = getSharedPrefs(context);
        sharedPrefs.edit().putString("TW-QUERY", query).commit();
    }

    public static boolean getQueryEnabled(Context context) {
        SharedPreferences sharedPrefs = getSharedPrefs(context);
        return sharedPrefs.getBoolean("TW-ENABLED", true);
    }

    public static void setQueryEnabled(Context context, boolean value){
        SharedPreferences sharedPrefs = getSharedPrefs(context);
        sharedPrefs.edit().putBoolean("TW-ENABLED", value).commit();
    }

    private Handler regCompleteHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            DBG.write("regCompleteHandler()");
            if (msg.what != 0) {
                Toast.makeText(TWActivity.this, "Started Golgi Service", Toast.LENGTH_LONG).show();
                // registerForAlerts();
            } else {
                Toast.makeText(TWActivity.this, "Failed to start Golgi Service", Toast.LENGTH_LONG).show();
            }
        }
    };

    public void golgiRegistrationComplete(boolean success) {
        DBG.write("golgiRegistrationComplete: " + success);
        regCompleteHandler.sendEmptyMessage(success ? 1 : 0);
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle d = msg.getData();

            if (d.containsKey(NEWTWEET)) {
                if(inFg){
                    tweetListAdapter.reload();
                }
            }
        }
    };


    private static void maybeSendMessage(String msgKey, String msgValue){
        TWActivity activity = TWActivity.getInstance();
        if(activity != null){
            Bundle bundle = new Bundle();
            bundle.putString(msgKey, msgValue);
            Message msg = new Message();
            msg.setData(bundle);
            activity.handler.sendMessage(msg);
        }
        else{
            DBG.write("Activity is NULL for '" + msgKey + "': '" + msgValue + "'");
        }
    }

    public static void tweetArrived(){
        maybeSendMessage(NEWTWEET, "");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tw);
        stdGto = new GolgiTransportOptions();
        stdGto.setValidityPeriod(60);
        synchronized(syncObj){
            theInstance = this;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tw, menu);
        DBG.write("Hello");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsActivity = new Intent(getBaseContext(), TWSettings.class);
            startActivityForResult( settingsActivity, SETTINGS_COMPLETE);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showActiveQuery() {
        TextView tv = (TextView) findViewById(R.id.activeQueryTv);
        tv.setText("Active Query: " + getCurrentQuery(this));
    }

    private void startCurrentQuery(){
        TweetFilter f = new TweetFilter();
        f.setGolgiId(getGolgiId(TWActivity.this));
        f.setQuery(getCurrentQuery(TWActivity.this));
        TwipWireService.startStreaming.sendTo(new TwipWireService.startStreaming.ResultReceiver() {
            @Override
            public void failure(GolgiException ex) {
                DBG.write("startStreaming() fail: " + ex.getErrText());
            }

            @Override
            public void success() {
                DBG.write("startStreaming() success");
            }
        }, stdGto, "SERVER", f);

    }

    private void stopCurrentQuery(){
        TwipWireService.stopStreaming.sendTo(new TwipWireService.stopStreaming.ResultReceiver() {
            @Override
            public void failure(GolgiException ex) {
                DBG.write("stopStreaming() fail: " + ex.getErrText());
            }

            @Override
            public void success() {
                DBG.write("stopStreaming() success");
            }
        }, stdGto, "SERVER", getGolgiId(TWActivity.this));
    }


    @Override
    public void onResume() {
        DBG.write("onResume()");
        super.onResume();
        inFg = true;
        GolgiAPI.usePersistentConnection();
        TWService.setQWActivity(this);

        if (!TWService.isRunning(this)) {
            DBG.write("Start the service");
            TWService.startService(this.getApplicationContext());
        } else {
            DBG.write("Service already started");
        }

        ListView lv = (ListView)findViewById(R.id.tweetLv);
        tweetListAdapter = new TweetListAdapter(this, lv);
        lv.setAdapter(tweetListAdapter);

        showActiveQuery();

        enabledCb = (CheckBox)findViewById(R.id.enabledCb);

        boolean enabled = getQueryEnabled(this);

        if(enabled){
            startCurrentQuery();
        }
        else{
            stopCurrentQuery();
        }

        enabledCb.setChecked(getQueryEnabled(this));
        enabledCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setQueryEnabled(TWActivity.this, isChecked);
                String query = getCurrentQuery(TWActivity.this);
                if(isChecked && query.length() > 0){
                    startCurrentQuery();
                }
                else{
                    stopCurrentQuery();
                }
            }
        });

        Button b = (Button)findViewById(R.id.applyButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText queryEt = (EditText)findViewById(R.id.queryEt);
                String text = queryEt.getText().toString().trim();
                if(text.length() > 0) {
                    (new TweetDb(TWActivity.this)).delAllTweets();
                    tweetListAdapter.reload();
                    queryEt.setText("");
                    setCurrentQuery(TWActivity.this, text);
                    showActiveQuery();
                    DBG.write("apply Pressed: '" + queryEt.getText() + "'");
                    setQueryEnabled(TWActivity.this, true);
                    startCurrentQuery();
                    enabledCb.setChecked(true);
                }
            }
        });

        tweetListAdapter.reload();


    }

    @Override
    public void onPause() {
        DBG.write("onPause()");
        super.onPause();
        inFg = false;
        TWService.setQWActivity(null);
        GolgiAPI.useEphemeralConnection();
        // logHandler = null;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        synchronized(syncObj){
            if(theInstance == this){
                theInstance = null;
            }
        }
    }

}
