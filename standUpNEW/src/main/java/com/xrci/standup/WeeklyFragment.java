package com.xrci.standup;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.xrci.standup.utility.DayDetails;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * A placeholder fragment containing a simple view.
 */
public class WeeklyFragment extends Fragment {
    private ListView mListView;
    public static final String intentFromWeekly = "intentFromWeekly";
    public static final String intentFromWeeklySteps = "weeklyIntentSteps";
    public WeeklyFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_weekly, container, false);
        mListView = (ListView) rootView.findViewById(R.id.listview_weekly);
        final ArrayList<DayDetails> days = findWeeklySteps();
        WeeklyAdapter weeklyAdapter = new WeeklyAdapter(getActivity(), R.layout.list_item_weekly, findWeeklySteps());

        mListView.setAdapter(weeklyAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    Intent startMain = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                    startActivity(startMain);
                }
                else {
                Intent intent = new Intent(getActivity().getApplicationContext(),
                        DayDetailActivity.class).putExtra(intentFromWeekly, position)
                        .putExtra(intentFromWeeklySteps, days.get(position).getStepsTaken());

                startActivity(intent);
                }

            }
        });
        return rootView;

    }

    public ArrayList<DayDetails> findWeeklySteps() {
        ArrayList<DayDetails> days = new ArrayList<DayDetails>();
        int validDays = 0;
        int weeklyCount = 0;

        for (int i = 1; i <= 7; i++) {
            Calendar cal = GregorianCalendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, -i);
            Date daysBeforeDate = cal.getTime();

            DayDetails day = new DayDetails();
            DatabaseHandler dbHandler = new DatabaseHandler(getActivity().getApplicationContext());
            int stepCount = dbHandler.getDayDataFromActivityLog(daysBeforeDate);
//            Log.i("cursor_log", "table count is " + Integer.toString(cursor.getCount()));

            day.setStepsTaken(stepCount);

            if (day.getStepsTaken() > 0)
                validDays++;

            day.setDate(daysBeforeDate);
            day.setStepsRemained((WeeklyActivity.goal - day.getStepsTaken()));
            if (day.getStepsRemained() <= 0)
                day.setGoalAchieved(true);
            else
                day.setGoalAchieved(false);

            days.add(day);
            weeklyCount += day.getStepsTaken();
        }
        DayDetails dayAvg = new DayDetails();

        if (validDays != 0) {
            dayAvg.setStepsTaken(weeklyCount / validDays);
            dayAvg.setStepsRemained(WeeklyActivity.goal - weeklyCount / validDays);
        }


        if (dayAvg.getStepsRemained() <= 0)
            dayAvg.setGoalAchieved(true);

        else
            dayAvg.setGoalAchieved(false);

        days.add(0, dayAvg);
//        days.add(dayAvg);

        return days;
    }

}

