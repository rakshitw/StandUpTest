package com.xrci.standup;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.xrci.standup.utility.ComplianceModel;
import com.xrci.standup.utility.DayDetails;
import com.xrci.standup.utility.GetData;

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
    public static final String intentFromWeeklyCompliance = "weeklyIntentCompliance";
    WeeklyAdapter weeklyAdapter;
    public WeeklyFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_weekly, container, false);
//        context = getActivity().getApplicationContext();
        mListView = (ListView) rootView.findViewById(R.id.listview_weekly);
        final ArrayList<DayDetails> days = findWeeklySteps(getActivity().getApplicationContext());
        weeklyAdapter = new WeeklyAdapter(getActivity(), R.layout.list_item_weekly, days);

        mListView.setAdapter(weeklyAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
//                    Intent startMain = new Intent(getActivity().getApplicationContext(), MainActivity.class);
//                    startActivity(startMain);
                } else {
                    Intent intent = new Intent(getActivity().getApplicationContext(),
                            DayDetailActivity.class).putExtra(intentFromWeekly, position)
                            .putExtra(intentFromWeeklySteps, days.get(position).getStepsTaken())
                            .putExtra(intentFromWeeklyCompliance, days.get(position).getCompliance());
                    startActivity(intent);
                }

            }
        });


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        int userid = sharedPreferences.getInt("userId", 0);

        ComplianceSettingClass complianceSettingClass = new ComplianceSettingClass(getActivity().getApplicationContext());
        complianceSettingClass.execute(userid);

        return rootView;

    }

    public ArrayList<DayDetails> findWeeklySteps(Context context) {
        ArrayList<DayDetails> days = new ArrayList<DayDetails>();
        int validDays = 0;
        int weeklyCount = 0;
        int totalGoal = 0;
        int totalCompliance = 0;
        for (int i = 1; i <= 7; i++) {

            Calendar cal = GregorianCalendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, -i);
            Date daysBeforeDate = cal.getTime();

            DayDetails day = new DayDetails();
            DatabaseHandler dbHandler = new DatabaseHandler(context);
            int stepCount = dbHandler.getDayDataFromActivityLog(daysBeforeDate);
//            Log.i("cursor_log", "table count is " + Integer.toString(cursor.getCount()));

            day.setStepsTaken(stepCount);

            if (day.getStepsTaken() > 0)
                validDays++;
            int dayGoal = dbHandler.getDayGoal(daysBeforeDate);
            day.setDayGoal(dayGoal);
            int dayCompliance = dbHandler.getDayCompliance(daysBeforeDate);
            day.setCompliance(dayCompliance);


            Log.i("check", "day goal is " + dayGoal);
            day.setDate(daysBeforeDate);
            day.setStepsRemained((dayGoal - day.getStepsTaken()));

            if (day.getStepsRemained() <= 0)
                day.setGoalAchieved(true);
            else
                day.setGoalAchieved(false);

            days.add(day);

            if (day.getStepsTaken() != 0) {
                weeklyCount += day.getStepsTaken();
                totalGoal += dayGoal;
                totalCompliance += dayCompliance;
            }
        }
        DayDetails dayAvg = new DayDetails();

        if (validDays != 0) {
            dayAvg.setStepsTaken(weeklyCount/validDays);
            dayAvg.setDayGoal(totalGoal/validDays);
            dayAvg.setCompliance(totalCompliance / validDays);
            dayAvg.setStepsRemained(totalGoal - weeklyCount);
        }
        totalGoal = 0;

        if (dayAvg.getStepsRemained() <= 0)
            dayAvg.setGoalAchieved(true);

        else
            dayAvg.setGoalAchieved(false);

        days.add(0, dayAvg);
//        days.add(dayAvg);

        return days;
    }

    public class ComplianceSettingClass extends AsyncTask<Integer, Void, String> {
        Context context;
        public ComplianceSettingClass(Context context) {
            this.context = context;
        }
        @Override
        protected String doInBackground(Integer... userid) {
            Log.i("check", "Weekly fragment userid is" +  userid[0]);
            ComplianceModel complianceModel = new ComplianceModel(Calendar.getInstance().getTime(), userid[0]);
            String complianceResponse = complianceModel.getWeekCompliance();
            DatabaseHandler dbHandler = new DatabaseHandler(context);
            dbHandler.flushTablesWeekOrMoreOlder();
            return complianceResponse;
        }


        @Override
        protected void onPostExecute(String complianceResponse) {
            super.onPostExecute(complianceResponse);
            DatabaseHandler dbHandler = new DatabaseHandler(context);
            if (!complianceResponse.equals(GetData.EXCEPTION) && !complianceResponse.equals(GetData.INVALID_PAYLOAD) && !complianceResponse.equals(GetData.INVALID_RESPONSE)) {
                int[] weeklyComplianceArray = getIntArrayFromString(complianceResponse);
                if (weeklyComplianceArray.length > 0) {
                    dbHandler.clearCompliance();
                }
                for (int i = 7, j = 0; i > 0; i--, j++) {
                    Calendar cal = GregorianCalendar.getInstance();
                    cal.setTime(new Date());
                    cal.add(Calendar.DAY_OF_YEAR, -i);
                    Date daysBeforeDate = cal.getTime();
                    dbHandler.setDayCompliance(daysBeforeDate, weeklyComplianceArray[j]);
                    dbHandler.close();
                }
            }
            weeklyAdapter.clear();
            ArrayList<DayDetails> dayDetailses = findWeeklySteps(context);
            int i = 0;
            for (DayDetails dayDetails : dayDetailses) {
                weeklyAdapter.insert(dayDetails, i);
                i++;
            }
            weeklyAdapter.notifyDataSetChanged();
        }

        public int[] getIntArrayFromString(String arr) {
            String[] items = arr.replaceAll("\\[", "").replaceAll("\\]", "").split(",");

            int[] results = new int[items.length];

            for (int i = 0; i < items.length; i++) {
                try {
                    results[i] = Integer.parseInt(items[i]);
                } catch (NumberFormatException nfe) {
                }
                ;
            }
            return results;
        }
    }


}

