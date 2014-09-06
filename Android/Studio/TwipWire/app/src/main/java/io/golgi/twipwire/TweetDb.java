package io.golgi.twipwire;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.golgi.twipwire.gen.TweetDetails;


public class TweetDb extends SQLiteOpenHelper{
	private  Object syncObject = new Object();

	private static void DBG(String str){
		DBG.write(str);
	}

    private String tableName = null;
	private static final int DATABASE_VERSION = 1;
	private static final String MSGID = "MSGID";
	private static final String ENCODING = "ENCODING";
    private static final String FILENAME = "tweet-db";
    private static final String TABLENAME = "tweets";

	private long fetchStartTime = 0;
	private boolean fetchInProgress;
	private boolean fetchNeeded;

	public TweetDb(Context context){
		super(context, FILENAME, null, DATABASE_VERSION);
        this.tableName = TABLENAME;
	}

	private void nukeWorker(SQLiteDatabase db) {

		// DBG("nuke() called in DraftMessageSchema");
		db.execSQL("DROP TABLE IF EXISTS " + tableName);
		db.execSQL("CREATE TABLE " + tableName +
                "(" +
                MSGID        + " TEXT PRIMARY KEY, " +
                ENCODING     + " TEXT " +
                ");");
	}

	public void nuke() {
		SQLiteDatabase db = getWritableDatabase();
		nukeWorker(db);
		db.close();
	}

	public void onOpen(SQLiteDatabase db) {
		// DBG("onOpen() called in DraftMessageSchema");
		// nukeWorker(db);
		// DBG("Back from nuke");
	}

	public void onCreate(SQLiteDatabase db) {
		DBG("onCreate() called");
		nukeWorker(db);
		DBG("Back from nuke");
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + tableName);
		onCreate(db);
	}

    public TweetDetails[] getAllTweets(){
        ArrayList<TweetDetails> al = new ArrayList<TweetDetails>();
        getAllTweets(al);

        return al.toArray(new TweetDetails[0]);
    }

    public void getAllTweets(ArrayList<TweetDetails> dst){
		Cursor c;
        DBG("Asked to get all messages");
        synchronized(syncObject){
            SQLiteDatabase db = getReadableDatabase();
            try{
                c = db.query(
                        tableName,
                        new String[] { ENCODING},
                        null,
                        null,
                        null,
                        null,
                        null);

                if(c != null && c.getCount() > 0){
                    DBG("Cursor has " + c.getColumnCount() + " entries");
                    for(int i = 0; i < c.getCount(); i++){
                        c.moveToPosition(i);
                        // DBG("Tweet " + i + " by '" + c.getString(1));

                        TweetDetails deets = new TweetDetails(c.getString(0));
                        if(!deets.isCorrupt()){
                            DBG("Decoded tweet " + i + " text: '" + deets.getText() + "'");
                            dst.add(deets);
                        }
                        else{
                            DBG("Failed to decode message " + i);
                        }
                    }
                }
                else{
                    DBG("Cussor broken or empty");
                }
                if(c != null){
                    c.close();
                }
            }
            catch(NullPointerException npe){
            }
            db.close();
        }
	}

    private String msgIdFromDeets(TweetDetails deets){
        return "" + deets.getTimestamp() + ":" + deets.getUsername();
    }

	public void delTweet(TweetDetails deets){
		long res;

        synchronized(syncObject){
            SQLiteDatabase db = getWritableDatabase();
            res = db.delete(tableName, MSGID + " = '" + msgIdFromDeets(deets) + "'", null);
            db.close();
        }

		DBG("Delete resulted: " + res);
	}

    public void delAllTweets(){
        long res;

        synchronized(syncObject){
            SQLiteDatabase db = getWritableDatabase();
            res = db.delete(tableName, null, null);
            db.close();
        }

        DBG("Delete resulted: " + res);
    }

	public void addTweet(TweetDetails deets){
        ContentValues values = new ContentValues();
        long res;
        DBG("Asked to add tweet: '" + deets.getText() + "'");
        values.put(MSGID, msgIdFromDeets(deets));
        values.put(ENCODING, deets.serialise().toString());

        synchronized(syncObject){
            SQLiteDatabase db = getWritableDatabase();
            res = db.insert(tableName, null, values);
            db.close();
        }
        DBG("addMessage result: " + res);
	}
}
