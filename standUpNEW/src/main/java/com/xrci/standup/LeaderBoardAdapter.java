package com.xrci.standup;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.facebook.widget.ProfilePictureView;
import com.xrci.standup.utility.LeaderBoardModel;
import com.xrci.standup.views.DailyStatisticsCircle;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by q4KV89ZB on 23-03-2015.
 */
public class LeaderBoardAdapter extends ArrayAdapter<LeaderBoardModel> {
    private static final int VIEW_TYPE_COUNT = 2;

    public LeaderBoardAdapter(Context context, int resource, List<LeaderBoardModel> items) {
        super(context, resource, items);
    }

    public static class ViewHolder {
        ProfilePictureView profilePictureView;
        TextView nameView;
        DailyStatisticsCircle dailyStatisticsCircleSteps;
        DailyStatisticsCircle dailyStatisticsCircleCompliance;

        public ViewHolder(View view) {
            profilePictureView = (ProfilePictureView) view.findViewById(R.id.leader_profile_pic);
            nameView = (TextView) view.findViewById(R.id.list_leader_name_textview);
            dailyStatisticsCircleSteps = (DailyStatisticsCircle) view.findViewById(R.id.StatisiticsStepCircle);
            dailyStatisticsCircleCompliance = (DailyStatisticsCircle) view.findViewById(R.id.ComplianceCircle);

        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        LayoutInflater viewInflator;
        viewInflator = LayoutInflater.from(getContext());
        view = getInflatedLayout(position);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        try {
            LeaderBoardModel leaderBoardModel = getItem(position);

            if (leaderBoardModel != null) {
                if (leaderBoardModel.getAuthType().equalsIgnoreCase("facebook")) {
                    if (position != 0) {
                        viewHolder.profilePictureView.setCropped(true);
                        viewHolder.profilePictureView.setProfileId(leaderBoardModel.getAuthId());
                    }
                }
                if (position != 0) {
                    viewHolder.nameView.setText(leaderBoardModel.getName());
                }
                int steps = leaderBoardModel.getSteps();
                if (steps >= 1000) {

                    viewHolder.dailyStatisticsCircleSteps.setCenterText(round((float) leaderBoardModel.getSteps() / 1000, 1) + "K");
                } else
                    viewHolder.dailyStatisticsCircleSteps.setCenterText(Integer.toString(leaderBoardModel.getSteps()));
                viewHolder.dailyStatisticsCircleCompliance.setCenterText(" " + Integer.toString(leaderBoardModel.getCompliance()) + "%");
                DisplayMetrics metrics = new DisplayMetrics();
                ((Activity) view.getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
                int screenWidth = metrics.widthPixels;
                int radius;
                if (position == 0) {
                    radius = screenWidth / 7;
                    viewHolder.dailyStatisticsCircleSteps.setCentertextColor(Color.WHITE);
                    viewHolder.dailyStatisticsCircleCompliance.setCentertextColor(Color.WHITE);

                } else
                    radius = screenWidth / 11;
                int textSize = radius / 2;
                viewHolder.dailyStatisticsCircleSteps.setmRadius(radius);
                viewHolder.dailyStatisticsCircleSteps.setTextSize(textSize);
                viewHolder.dailyStatisticsCircleSteps.setArcStartEndAngles(0, 100, 0, 0, 0);
                viewHolder.dailyStatisticsCircleSteps.init();
                viewHolder.dailyStatisticsCircleCompliance.setmRadius(radius);
                viewHolder.dailyStatisticsCircleCompliance.setTextSize(textSize);
                viewHolder.dailyStatisticsCircleCompliance.setArcStartEndAngles(100 - leaderBoardModel.getCompliance(), 0, 0, 0, leaderBoardModel.getCompliance());
                viewHolder.dailyStatisticsCircleCompliance.init();

            }
        } catch (Exception e) {
            Log.i("LeaderBoard", "exception in leaderboard" + e.getMessage());
        }

        return view;

    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    private View getInflatedLayout(int position) {
        if (position == 0) {
            return LayoutInflater.from(getContext()).inflate(R.layout.list_item_leader_self, null);
        } else
            return LayoutInflater.from(getContext()).inflate(R.layout.leaderboard_list_item, null);


    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
}
