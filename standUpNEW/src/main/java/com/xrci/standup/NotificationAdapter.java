package com.xrci.standup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.DetectedActivity;
import com.xrci.standup.utility.NotificationModel;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by q4KV89ZB on 13-03-2015.
 */
public class NotificationAdapter extends ArrayAdapter<NotificationModel> {
    public NotificationAdapter(Context context, int resource, List<NotificationModel> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        View view = convertView;

        if (view == null) {

            LayoutInflater viewInflator;
            viewInflator = LayoutInflater.from(getContext());
            view = LayoutInflater.from(getContext()).inflate(R.layout.notification_list_item, null);
        }

        NotificationModel notificationModel = getItem(position);
        if(notificationModel != null) {
            if(notificationModel.getActivity() == DetectedActivity.STILL) {
                ImageView imageView = (ImageView) view.findViewById(R.id.list_item_icon_notification);
                imageView.setImageResource(R.drawable.still);
            }
            else if (notificationModel.getActivity() == DetectedActivity.ON_FOOT){
                {
                    ImageView imageView = (ImageView) view.findViewById(R.id.list_item_icon_notification);
                    imageView.setImageResource(R.drawable.foot);
                }
            }
                TextView messageView = (TextView) view.findViewById(R.id.list_item_message_textview);
            messageView.setText(notificationModel.getMessage());
            TextView timeView = (TextView) view.findViewById(R.id.list_item_time);
            timeView.setText(notificationModel.getTimeString().substring(11,16));
        }

        return view;

    }
}
