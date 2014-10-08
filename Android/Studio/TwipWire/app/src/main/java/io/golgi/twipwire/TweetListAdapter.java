package io.golgi.twipwire;

/**
 * Created by brian on 9/3/14.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.golgi.twipwire.gen.TweetDetails;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Created by brian on 8/9/13.
 */
public class TweetListAdapter extends ArrayAdapter<TweetDetails>
        implements Comparator<TweetDetails> {
    private TweetDb tweetDb;
    private Object syncObj = new Object();
    private TWActivity activity;
    private ListView listView;
    private String ourUid;
    private boolean isDebugDev;
    private Bitmap ourBitmap;

    @Override
    public int compare(TweetDetails m1, TweetDetails m2){
        return (int)(m2.getTimestamp() - m1.getTimestamp());
    }

    public static class ViewHolder{
        public View self;
        public ImageView userIv;
        public TextView nameTv;
        public TextView timestampTv;
        public TextView contentTv;
        public TweetDetails deets;
        public int position;
    }

    private View initRowView(TweetDetails deets){
        View rowView;
        ViewHolder viewHolder = new ViewHolder();

        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        rowView = layoutInflater.inflate(R.layout.tw_deets, null);
        viewHolder.userIv = (ImageView)rowView.findViewById(R.id.userIv);
        viewHolder.nameTv = (TextView)rowView.findViewById(R.id.nameTv);
        viewHolder.timestampTv = (TextView)rowView.findViewById(R.id.timestampTv);
        viewHolder.contentTv = (TextView)rowView.findViewById(R.id.contentTv);
        viewHolder.deets = deets;

        rowView.setTag(viewHolder);

        return rowView;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        TweetDetails deets = getItem(position);
        ViewHolder holder;
        View rowView = convertView;
        // DBG.write("***************************** Get: " + position);

        if(rowView == null){
            rowView = initRowView(deets);
        }

        holder = (ViewHolder)rowView.getTag();
        holder.self = rowView;
        holder.position = position;
        holder.deets = deets;
        displayMessage(holder);

        return rowView;
    }

    private void displayMessage(ViewHolder holder){
        RelativeLayout.LayoutParams rlp;
        TweetDetails deets = holder.deets;

        holder.userIv.setImageResource(R.drawable.user_blank);

        byte[] imgData = deets.getImage();
        Bitmap b = null;

        if(imgData.length > 0){
            b = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
        }

        if(b == null){
            holder.userIv.setImageResource(R.drawable.user_blank);
        }
        else{
            holder.userIv.setImageBitmap(b);
        }

        holder.timestampTv.setText(DateHelper.dateToNowRelativeString(activity, (long)deets.getTimestamp() * 1000));
        holder.nameTv.setText(deets.getName());
        holder.contentTv.setText(deets.getText());
        holder.self.invalidate();
    }

    public int reload(){
        boolean markedSome = false;


        setNotifyOnChange(false);
        clear();

        TweetDetails[] allTweets = tweetDb.getAllTweets();
        DBG.write("Reloading messages from DB: " + allTweets.length);
        for(int i = 0; i < allTweets.length; i++){
            add(allTweets[i]);
        }

        sort(this);

        notifyDataSetChanged();

        return allTweets.length;

        // reset();
    }

    public TweetListAdapter(TWActivity activity, ListView listView){
        super(activity, R.layout.tw_deets);
        this.activity = activity;
        this.listView = listView;
        this.tweetDb = new TweetDb(activity);
    }
}
