package com.xrci.standup;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.location.DetectedActivity;
import com.xrci.standup.utility.DatedResponse;
import com.xrci.standup.utility.FusedDataModel;
import com.xrci.standup.views.BlankTimeLineElement;
import com.xrci.standup.views.CircleView;
import com.xrci.standup.views.CompositeTimeLineElement;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;

/**
 * A placeholder fragment containing a simple view.
 */
public class DayDetailFragment extends Fragment {
    CircleView currentCircle;
    CircleView complianceCircle;
    LinearLayout linearLayoutTimelineArea;
    Context context;
    Context applicationContext;

    DatabaseHandler dbHandler;
    Timer timer;
    private boolean isTimerRunning;
    Date workingSinceTimestamp;

    public DayDetailFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_day_detail, container, false);
        try {
            context = getActivity();
            applicationContext = getActivity().getApplicationContext();
            currentCircle = (CircleView) view.findViewById(R.id.circleViewCurrent);
            complianceCircle = (CircleView) view.findViewById(R.id.daydetailCompliance);
            linearLayoutTimelineArea = (LinearLayout) view.findViewById(R.id.linearLayoutTimelineArea);
            dbHandler = new DatabaseHandler(context);


            Date daysBeforeDate = Calendar.getInstance().getTime();
            Calendar cal = GregorianCalendar.getInstance();
            cal.setTime(new Date());
            int steps = 0;
            int compliance = 0;
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(WeeklyFragment.intentFromWeekly)) {
                int position = intent.getIntExtra(WeeklyFragment.intentFromWeekly, 0);
                cal.add(Calendar.DAY_OF_YEAR, -position);
                steps = intent.getIntExtra(WeeklyFragment.intentFromWeeklySteps, 0);
                compliance = intent.getIntExtra(WeeklyFragment.intentFromWeeklyCompliance, 0);
                daysBeforeDate = cal.getTime();


                try {
                    currentCircle.setTextLine1(Integer.toString(steps));
                    complianceCircle.setTextLine1(Integer.toString(compliance) + "%");
                    if (compliance <= 50)
                        complianceCircle.setFillColor(utils.COLOR_STILL);
                    else
                        complianceCircle.setFillColor(utils.COLOR_WALK);

                    refreshFusedTimeLine(daysBeforeDate);

                    ShowFusedTimeline showFusedTimeline = new ShowFusedTimeline(context);
                    showFusedTimeline.execute(daysBeforeDate);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            //System.out.println("view created");
        } catch (Exception e) {
            Log.i("DayDetailFragment", "Daydetailfragment exception " + e.getMessage());
        }
        return view;
    }

    public class ShowFusedTimeline extends AsyncTask<Date, Void, DatedResponse> {
        Context context;

        public ShowFusedTimeline(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected DatedResponse doInBackground(Date... date) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
            int userid = sharedPreferences.getInt("userId", 1);
            String response = FusedDataModel.getFusedData(date[0], userid, dbHandler);
            DatedResponse datedResponse = new DatedResponse(date[0], response);
            return datedResponse;
        }

        @Override
        protected void onPostExecute(DatedResponse datedResponse) {
            super.onPostExecute(datedResponse);
            String response = datedResponse.getResponse();
            Date date = datedResponse.getDate();
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

//            = new AlertDialog.Builder(DayDetailActivity.this);

            if (response.equals(PostData.INVALID_RESPONSE) || response.equals(PostData.EXCEPTION)) {

                alertDialogBuilder.setTitle("Internet Connection Unavailable");

                alertDialogBuilder
                        .setMessage("Internet Unavailable, timeline may be stale.")
                        .setCancelable(true)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();


            } else if (response.equals(PostData.INVALID_PAYLOAD)) {

                alertDialogBuilder.setTitle("Oops");

                alertDialogBuilder
                        .setMessage("This is embarrassing. Something went wrong.")
                        .setCancelable(true)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();

            }

            refreshFusedTimeLine(date);
        }
    }

    void refreshTimeLine(Context context, ArrayList<ActivityDetails> userActivities) {
        linearLayoutTimelineArea.removeAllViews();
        Date lastActivityEnd = null;
        boolean showStartTime = true;
        for (int i = 0; i < userActivities.size(); i++) {
            int j = 1;
            ActivityDetails activityDetail = userActivities.get(i);

            if (i == userActivities.size() - 1) {
                //continue
            } else {
                ActivityDetails nextActivityDetail = userActivities.get(i + j);

                if (activityDetail.activityType != DetectedActivity.ON_FOOT) {
                    while (nextActivityDetail.activityType != DetectedActivity.ON_FOOT
                            && getActivityTime(nextActivityDetail) < 180 * 1000
                            && (i + j + 1) < userActivities.size() - 1) {
                        activityDetail.end = nextActivityDetail.end;
                        activityDetail.timePeriod = activityDetail.timePeriod + nextActivityDetail.timePeriod;
                        j++;
                        nextActivityDetail = userActivities.get(i + j);
                    }
                    i = i + j - 1;
                }
                j = 1;
                nextActivityDetail = userActivities.get(i + j);
                while (activityDetail.activityType == nextActivityDetail.activityType && (nextActivityDetail.start.getTime() - activityDetail.end.getTime() <= 180 * 1000)
                        && (i + j + 1) < userActivities.size() - 1) {
                    activityDetail.end = nextActivityDetail.end;
                    activityDetail.timePeriod = activityDetail.timePeriod + nextActivityDetail.timePeriod;
                    j++;
                    nextActivityDetail = userActivities.get(i + j);
                }
                i = i + j - 1;

                if (activityDetail.activityType != DetectedActivity.ON_FOOT && activityDetail.timePeriod < 180 * 1000) {
                    if (userActivities.get(i + 1).activityType != DetectedActivity.ON_FOOT) {
                        ActivityDetails nextActivityDetails = userActivities.get(i + 1);
                        if (nextActivityDetail.start.getTime() - activityDetail.end.getTime() <= 240 * 1000) {
                            activityDetail.end = nextActivityDetails.end;
                            activityDetail.activityType = nextActivityDetails.activityType;
                            activityDetail.timePeriod = activityDetail.timePeriod + nextActivityDetails.timePeriod;
                            i++;
                        }
                    }
                }
            }


            if (lastActivityEnd == null) {
                showStartTime = true;
            } else {

                if (activityDetail.end.getTime() == lastActivityEnd.getTime()) {
                    continue;
                }

                if ((activityDetail.start.getTime() - lastActivityEnd.getTime() > 300 * 1000)) // insert a blanktimeline element if more than one minute missing

                {
                    Log.i("Blank activity", "I am here");
                    BlankTimeLineElement blank = new BlankTimeLineElement(context);
                    linearLayoutTimelineArea.addView(blank, 0);
                    showStartTime = true;

                } else
                    showStartTime = false;
            }
//            if (activityDetail.timePeriod > 180 * 1000 || activityDetail.activityType == DetectedActivity.ON_FOOT) {

            if (activityDetail.activityType == DetectedActivity.UNKNOWN) {

//                if(previousActivity != DetectedActivity.UNKNOWN) {
//                    Log.i("Blank activity 2", "I am there");

                BlankTimeLineElement blank = new BlankTimeLineElement(context);
                linearLayoutTimelineArea.addView(blank, 0);
//                }
            } else {
                CompositeTimeLineElement element = new CompositeTimeLineElement(context, activityDetail.activityType, activityDetail.end.getTime() - activityDetail.start.getTime(), activityDetail.noOfSteps, activityDetail.start, activityDetail.end, showStartTime);
                linearLayoutTimelineArea.addView(element, 0);
            }
//            }
            lastActivityEnd = activityDetail.end;
//            previousActivity = activityDetail.activityType;
            //System.out.println("Camsdin refreshtimeline");


        }
    }

    protected void refreshFusedTimeLine(Date date) {

        ArrayList<ActivityDetails> userActivities;
        try {
            userActivities = dbHandler.fetchAllFusedActivitiesToday(date);

            Log.i("check ", "user activities to be printed = " + userActivities.size() + "date is " + date);
            if (userActivities.size() > 0)
                refreshTimeLine(context, userActivities);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.i("check", "Exception in refreshTimeLine(MainActivity):" + e.getMessage());

        }
    }

    private long getActivityTime(ActivityDetails activity) {
        return activity.end.getTime() - activity.start.getTime();
    }
}


