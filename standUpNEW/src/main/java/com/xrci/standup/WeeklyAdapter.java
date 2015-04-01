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

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        View view = convertView;

        if (view == null) {

            LayoutInflater viewInflator;
            viewInflator = LayoutInflater.from(getContext());
            view = getInflatedLayout(position);

        }
        try {
            DayDetails day = getItem(position);


            if (day != null) {
                DailyStatisticsCircle dailyStatistics = (DailyStatisticsCircle) view.findViewById(R.id.dailyStatisiticsCircle);
                DailyStatisticsCircle complianceCircle = (DailyStatisticsCircle) view.findViewById(R.id.dailyComplianceCircle);
                TextView dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
                TextView achieveView = (TextView) view.findViewById(R.id.list_item_achieved_textview);
//            ImageView rowImage = (ImageView) view.findViewById(R.id.list_item_icon);
                if (position == 0) {
                    if (day.getStepsTaken() >= 1000) {
                        float displpaySteps = round((float) day.getStepsTaken() / 1000, 1);
                        dailyStatistics.setCenterText(displpaySteps + "K ");
                    } else
                        dailyStatistics.setCenterText(Integer.toString(day.getStepsTaken()));
                } else if (day.getDayGoal() != 0) {
                    int goalPercent = ((day.getStepsTaken() * 100) / day.getDayGoal()) ;
                    dailyStatistics.setCenterText(" " + goalPercent + "%");
                }

                if (complianceCircle == null) {
                    Log.i("check", "compliance circle is null");
                } else
                    complianceCircle.setCenterText(" " + Integer.toString(day.getCompliance()) + "%");

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
                            achieveView.setText("Goal: " + day.getDayGoal() + " Steps: " + day.getStepsTaken());
                        else {
                            if (day.getStepsTaken() == 0)
                                achieveView.setText("Information Not Available");
                            else

                                achieveView.setText("Goal: " + day.getDayGoal() + "  Steps: " + day.getStepsTaken());
                        }
                }

                DisplayMetrics metrics = new DisplayMetrics();
                ((Activity) view.getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
                int screenWidth = metrics.widthPixels;

                if (position == 0) {
                    int radius = screenWidth / 7;
                    int textSize = radius / 2;
                    dailyStatistics.setmRadius(radius);
                    dailyStatistics.setTextSize(textSize);
                    dailyStatistics.setCentertextColor(Color.WHITE);
                    complianceCircle.setmRadius(radius);
                    complianceCircle.setTextSize(textSize);
                    complianceCircle.setCentertextColor(Color.WHITE);

                } else {
                    int radius = screenWidth / 11;
                    int textSize = radius / 2;
                    dailyStatistics.setmRadius(radius);
                    complianceCircle.setmRadius(radius);
                    dailyStatistics.setTextSize(textSize);
                    complianceCircle.setTextSize(textSize);
                }


                if (day.getStepsTaken() != 0) {
                    dailyStatistics.setArcStartEndAngles(day.getStepsRemained(), day.getStepsTaken(), 0, 0, 0);
                    dailyStatistics.init();
                    complianceCircle.setArcStartEndAngles(100 - day.getCompliance(), 0, 0, 0, day.getCompliance());
                    complianceCircle.init();

                } else {
//                dailyStatistics.setArcStartEndAngles(100, 0, 0, 0, 0);
                    dailyStatistics.init();
//                complianceCircle.setArcStartEndAngles(100, 0, 0, 0, 0);
                    complianceCircle.init();


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

//            if (day.getStepsTaken() != 0){
//                if (day.getGoalAchieved())
//                    rowImage.setImageResource(R.drawable.happyman);
//                else
//                    rowImage.setImageResource(R.drawable.eldersad);
//            }
//            else
//                rowImage.setImageResource(R.drawable.confusedman);
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
