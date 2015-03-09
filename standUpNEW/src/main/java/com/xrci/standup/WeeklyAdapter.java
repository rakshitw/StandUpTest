package com.xrci.standup;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xrci.standup.utility.DayDetails;
import com.xrci.standup.views.DailyStatisticsCircle;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by q4KV89ZB on 25-02-2015.
 */

public class WeeklyAdapter extends ArrayAdapter<DayDetails> {


    public WeeklyAdapter(Context context, int resource, List<DayDetails> items) {
        super(context, resource, items);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        View view = convertView;

//        if (view == null) {

//            LayoutInflater viewInflator;
//            viewInflator = LayoutInflater.from(getContext());
            view = getInflatedLayout(position);

//        }

        DayDetails day = getItem(position);


        if (day != null) {
            DailyStatisticsCircle dailyStatistics = (DailyStatisticsCircle) view.findViewById(R.id.dailyStatisiticsCircle);
            TextView dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            TextView achieveView = (TextView) view.findViewById(R.id.list_item_achieved_textview);
            ImageView rowImage = (ImageView) view.findViewById(R.id.list_item_icon);
            dailyStatistics.setCenterText(Integer.toString(day.getStepsTaken()) + "  steps");
            if (dateView != null) {
                if (day.getDate() != null) {
                    Calendar cal = GregorianCalendar.getInstance();
                    cal.setTime(new Date());
                    cal.add(Calendar.DAY_OF_YEAR, -1);
                    Date dayBeforeDate = cal.getTime();

                    String dateString = simpleDateFormat.format(day.getDate());
                    String compareDate = simpleDateFormat.format(dayBeforeDate);
                    if (dateString.compareTo(compareDate) == 0)
                        dateView.setText("Yesterday");
                    else
                        dateView.setText(dateString);
                } else
                    dateView.setText("Week Statistics");
            }
            if (achieveView != null) {
                if (day.getGoalAchieved() != null)
                    if (day.getGoalAchieved())
                        achieveView.setText("Woohoo");
                    else {
                        if (day.getStepsTaken() == 0)
                            achieveView.setText("Steps unavailable");
                        else

                            achieveView.setText("Try harder");
                    }
            }
            if(position == 0) {
                dailyStatistics.setmRadius(120);
                dailyStatistics.setTextSize(28);
                dailyStatistics.setCentertextColor(Color.WHITE);
                achieveView.setText("Daily Goal: " + WeeklyActivity.goal);


            }
            else
                dailyStatistics.setTextSize(22);



            if (day.getStepsTaken() != 0) {
                dailyStatistics.setArcStartEndAngles(day.getStepsRemained(), day.getStepsTaken(), 0, 0, 0);
                dailyStatistics.init();

            }

//            if (remainingView != null) {
//                if (day.getStepsTaken() != 0)
//                    remainingView.setText(Integer.toString(day.getStepsRemained()));
//                else
//                    remainingView.setText("NA");
//            }
////
//            dailyStatistics.setArcStartEndAngles(day.getStepsRemained(), day.getStepsTaken(), 0, 0, 0);
//            dailyStatistics.init();
            /**
             * Change images
             */

            if (day.getStepsTaken() != 0){
                if (day.getGoalAchieved())
                    rowImage.setImageResource(R.drawable.happyman);
                else
                    rowImage.setImageResource(R.drawable.eldersad);
            }
            else
                rowImage.setImageResource(R.drawable.confusedman);
        }

        return view;

    }

    private View getInflatedLayout(int position) {
        if (position == 0) {
            return LayoutInflater.from(getContext()).inflate(R.layout.list_item_today, null);
        } else
            return LayoutInflater.from(getContext()).inflate(R.layout.list_item_weekly, null);


    }


}
