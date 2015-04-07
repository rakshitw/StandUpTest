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

import com.xrci.standup.utility.DayDetails;
import com.xrci.standup.views.DailyStatisticsCircle;

import java.math.BigDecimal;
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

    public static class ViewHolder {
        DailyStatisticsCircle dailyStatistics;
        DailyStatisticsCircle complianceCircle;
        TextView dateView;
        TextView achieveView;

        public ViewHolder(View view) {
            dailyStatistics = (DailyStatisticsCircle) view.findViewById(R.id.dailyStatisiticsCircle);
            complianceCircle = (DailyStatisticsCircle) view.findViewById(R.id.dailyComplianceCircle);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            achieveView = (TextView) view.findViewById(R.id.list_item_achieved_textview);

        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        View view = convertView;

        view = getInflatedLayout(position);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        try {
            DayDetails day = getItem(position);


            if (day != null) {
                if (position == 0) {
                    if (day.getStepsTaken() >= 1000) {
                        float displpaySteps = round((float) day.getStepsTaken() / 1000, 1);
                        viewHolder.dailyStatistics.setCenterText(displpaySteps + "K ");
                    } else
                        viewHolder.dailyStatistics.setCenterText(Integer.toString(day.getStepsTaken()));
                } else if (day.getDayGoal() != 0) {
                    int goalPercent = ((day.getStepsTaken() * 100) / day.getDayGoal());
                    viewHolder.dailyStatistics.setCenterText(" " + goalPercent + "%");
                }

                if (viewHolder.complianceCircle == null) {
                    Log.i("check", "compliance circle is null");
                } else
                    viewHolder.complianceCircle.setCenterText(" " + Integer.toString(day.getCompliance()) + "%");

                if (viewHolder.dateView != null) {
                    if (day.getDate() != null) {
                        Calendar cal = GregorianCalendar.getInstance();
                        cal.setTime(new Date());
                        cal.add(Calendar.DAY_OF_YEAR, -1);
                        Date dayBeforeDate = cal.getTime();

                        String dateString = simpleDateFormat.format(day.getDate());
                        String compareDate = simpleDateFormat.format(dayBeforeDate);
                        if (dateString.compareTo(compareDate) == 0)
                            viewHolder.dateView.setText("Yesterday");
                        else
                            viewHolder.dateView.setText(dateString);
                    } else
                        viewHolder.dateView.setText("Week Statistics");
                }
                if (viewHolder.achieveView != null) {
                    if (day.getGoalAchieved() != null)
                        if (day.getGoalAchieved())
                            viewHolder.achieveView.setText("Goal: " + day.getDayGoal() + " Steps: " + day.getStepsTaken());
                        else {
                            if (day.getStepsTaken() == 0)
                                viewHolder.achieveView.setText("Information Not Available");
                            else

                                viewHolder.achieveView.setText("Goal: " + day.getDayGoal() + "  Steps: " + day.getStepsTaken());
                        }
                }

                DisplayMetrics metrics = new DisplayMetrics();
                ((Activity) view.getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
                int screenWidth = metrics.widthPixels;

                if (position == 0) {
                    int radius = screenWidth / 7;
                    int textSize = radius / 2;
                    viewHolder.dailyStatistics.setmRadius(radius);
                    viewHolder.dailyStatistics.setTextSize(textSize);
                    viewHolder.dailyStatistics.setCentertextColor(Color.WHITE);
                    viewHolder.complianceCircle.setmRadius(radius);
                    viewHolder.complianceCircle.setTextSize(textSize);
                    viewHolder.complianceCircle.setCentertextColor(Color.WHITE);

                } else {
                    int radius = screenWidth / 11;
                    int textSize = radius / 2;
                    viewHolder.dailyStatistics.setmRadius(radius);
                    viewHolder.complianceCircle.setmRadius(radius);
                    viewHolder.dailyStatistics.setTextSize(textSize);
                    viewHolder.complianceCircle.setTextSize(textSize);
                }


                if (day.getStepsTaken() != 0) {
                    viewHolder.dailyStatistics.setArcStartEndAngles(day.getStepsRemained(), day.getStepsTaken(), 0, 0, 0);
                    viewHolder.dailyStatistics.init();
                    viewHolder.complianceCircle.setArcStartEndAngles(100 - day.getCompliance(), 0, 0, 0, day.getCompliance());
                    viewHolder.complianceCircle.init();

                } else {
//                dailyStatistics.setArcStartEndAngles(100, 0, 0, 0, 0);
                    viewHolder.dailyStatistics.init();
//                complianceCircle.setArcStartEndAngles(100, 0, 0, 0, 0);
                    viewHolder.complianceCircle.init();


                }
            }
        } catch (Exception e) {
            Log.i(StepService.TAG, "exception in weekly adapter " + e.getMessage());
        }
        return view;
    }


    private View getInflatedLayout(int position) {
        if (position == 0) {
            return LayoutInflater.from(getContext()).inflate(R.layout.list_item_today, null);
        } else
            return LayoutInflater.from(getContext()).inflate(R.layout.list_item_weekly, null);
    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
}
