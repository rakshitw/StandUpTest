package com.xrci.standup;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.location.DetectedActivity;
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
    LinearLayout linearLayoutTimelineArea;
    Context context;
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

        Context context = getActivity();
        currentCircle = (CircleView) view.findViewById(R.id.circleViewCurrent);
        linearLayoutTimelineArea = (LinearLayout) view.findViewById(R.id.linearLayoutTimelineArea);
        dbHandler = new DatabaseHandler(context);

        Date daysBeforeDate = Calendar.getInstance().getTime();
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(new Date());
        int steps = 0;
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(WeeklyFragment.intentFromWeekly)) {
            int position = intent.getIntExtra(WeeklyFragment.intentFromWeekly, 0);
            cal.add(Calendar.DAY_OF_YEAR, -position);
            steps = intent.getIntExtra(WeeklyFragment.intentFromWeeklySteps, 0);
            daysBeforeDate = cal.getTime();
        }

        try {
            currentCircle.setTextLine1(Integer.toString(steps));
            refreshTimeLine(context, dbHandler.fetchAllActivitiesToday(daysBeforeDate));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //System.out.println("view created");

        return view;
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
                    i = i + j -1;
                }
                j = 1;
                nextActivityDetail = userActivities.get(i + j);
                while (activityDetail.activityType == nextActivityDetail.activityType
                        && (i + j + 1) < userActivities.size() - 1) {
                    activityDetail.end = nextActivityDetail.end;
                    activityDetail.timePeriod = activityDetail.timePeriod + nextActivityDetail.timePeriod;
                    j++;
                    nextActivityDetail = userActivities.get(i + j);
                }
                i = i + j - 1;

                if(activityDetail.activityType != DetectedActivity.ON_FOOT  && activityDetail.timePeriod < 180*1000 ) {
                    if(userActivities.get(i + 1).activityType != DetectedActivity.ON_FOOT){
                        ActivityDetails nextActivityDetails = userActivities.get(i + 1);
                        activityDetail.end = nextActivityDetails.end;
                        activityDetail.activityType = nextActivityDetails.activityType;
                        activityDetail.timePeriod = activityDetail.timePeriod + nextActivityDetails.timePeriod;
                        i++;
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

    private long getActivityTime(ActivityDetails activity) {
        return activity.end.getTime() - activity.start.getTime();
    }
//
//    private ArrayList<ActivityDetails> getToPlotActivities(ArrayList<ActivityDetails> userActivities ){
//
//
//
//    }


}


