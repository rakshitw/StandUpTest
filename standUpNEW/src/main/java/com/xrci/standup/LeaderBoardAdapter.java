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

    public LeaderBoardAdapter(Context context, int resource, List<LeaderBoardModel> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


            View view = convertView;

            if (view == null) {

                LayoutInflater viewInflator;
                viewInflator = LayoutInflater.from(getContext());
                view = getInflatedLayout(position);

            }
        try {
            LeaderBoardModel leaderBoardModel = getItem(position);

            if (leaderBoardModel != null) {
                if (leaderBoardModel.getAuthType().equalsIgnoreCase("facebook")) {
                    if (position != 0) {
                        ProfilePictureView profilePictureView = (ProfilePictureView) view.findViewById(R.id.leader_profile_pic);
                        profilePictureView.setCropped(true);
                        profilePictureView.setProfileId(leaderBoardModel.getAuthId());
                    }

                }
                if (position != 0) {
                    TextView nameView = (TextView) view.findViewById(R.id.list_leader_name_textview);
                    nameView.setText(leaderBoardModel.getName());
                }
//            TextView stepView = (TextView) view.findViewById(R.id.list_item_steps_textview);
                DailyStatisticsCircle dailyStatisticsCircleSteps = (DailyStatisticsCircle) view.findViewById(R.id.StatisiticsStepCircle);
                DailyStatisticsCircle dailyStatisticsCircleCompliance = (DailyStatisticsCircle) view.findViewById(R.id.ComplianceCircle);

                int steps = leaderBoardModel.getSteps();
                if (steps >= 1000) {

                    dailyStatisticsCircleSteps.setCenterText(round((float) leaderBoardModel.getSteps() / 1000, 1) + "K");
                } else
                    dailyStatisticsCircleSteps.setCenterText(Integer.toString(leaderBoardModel.getSteps()));

                DisplayMetrics metrics = new DisplayMetrics();
                ((Activity) view.getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
                int screenWidth = metrics.widthPixels;
                int radius;
                if (position == 0) {
                    radius = screenWidth / 7;
                    dailyStatisticsCircleSteps.setCentertextColor(Color.WHITE);
                    dailyStatisticsCircleCompliance.setCentertextColor(Color.WHITE);

                } else
                    radius = screenWidth / 11;
                int textSize = radius / 2;
                //            dailyStatisticsCircleSteps.setmRadius(radius);

                dailyStatisticsCircleSteps.setmRadius(radius);
                dailyStatisticsCircleSteps.setTextSize(textSize);
                dailyStatisticsCircleSteps.setArcStartEndAngles(0, 100, 0, 0, 0);
                dailyStatisticsCircleSteps.init();
                dailyStatisticsCircleCompliance.setmRadius(radius);
                dailyStatisticsCircleCompliance.setTextSize(textSize);
                dailyStatisticsCircleCompliance.setArcStartEndAngles(100, 0, 0, 0, 0);
                dailyStatisticsCircleCompliance.setCenterText("0%");
                dailyStatisticsCircleCompliance.init();

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
}
