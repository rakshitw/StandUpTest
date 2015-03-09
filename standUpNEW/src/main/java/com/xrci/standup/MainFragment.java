package com.xrci.standup;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.DetectedActivity;
import com.xrci.standup.views.BlankTimeLineElement;
import com.xrci.standup.views.CircleView;
import com.xrci.standup.views.CompositeTimeLineElement;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainFragment extends Fragment {

    CircleView currentCircle;
    CircleView stepCircle;
    TextView currentActivity, currentActivityTimePeriod;
    LinearLayout linearLayoutTimelineArea;
    Context context;
    DatabaseHandler dbHandler;
    Timer timer;
    private boolean isTimerRunning;
    Date workingSinceTimestamp;
    static int previousActivity = -1;

    public MainFragment() {

    }

    public void setMainFragmentContext(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_fragment, container,
                false);
        stepCircle = (CircleView) view.findViewById(R.id.circleViewTodayStep);
        currentCircle = (CircleView) view.findViewById(R.id.circleViewCurrent);
        currentActivity = (TextView) view.findViewById(R.id.textViewCurrentActivity);
        currentActivityTimePeriod = (TextView) view.findViewById(R.id.textViewCurrentActivityTimePeriod);
        linearLayoutTimelineArea = (LinearLayout) view.findViewById(R.id.linearLayoutTimelineArea);
        dbHandler = new DatabaseHandler(context);

        try {
            refreshTimeLine(context, dbHandler.fetchAllActivitiesToday(Calendar.getInstance().getTime()));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //System.out.println("view created");

        return view;
    }


    void updateSteps(int steps) {

        currentCircle.setFillColor(utils.COLOR_WALK);
        currentCircle.setTextLine1(steps + "");
        currentCircle.setTextLine2("steps"); // Color for Walk
        currentCircle.invalidate();
        currentActivity.setText("On Foot");
//        Calendar cal = GregorianCalendar.getInstance();
//        cal.setTime(new Date());
//        Date today = cal.getTime();
//        int stepCount = dbHandler.getDayDataFromActivityLog(today);
//        stepCircle.setTextLine1(stepCount + "");

    }

    void updateCurrentActivity(int activity, long timePeriod,
                               String since) {
        if (activity == DetectedActivity.STILL) {
            currentCircle.setFillColor(utils.COLOR_STILL); // Color for still
            currentCircle.setTextLine1(timePeriod / (1000 * 60) + "");
            currentCircle.setTextLine2("mins");
            currentActivity.setText("Still");
        } else if (activity == DetectedActivity.ON_FOOT) {
            currentCircle.setFillColor(utils.COLOR_WALK);
            currentCircle.setTextLine2("steps");
            currentActivity.setText("On Foot");

        } else if (activity == DetectedActivity.ON_BICYCLE) {
            currentCircle.setFillColor(utils.COLOR_BIKE); // Color for still
            currentCircle.setTextLine1(timePeriod / (1000 * 60) + "");
            currentCircle.setTextLine2("mins");
            currentActivity.setText("On Bike");

        } else if (activity == DetectedActivity.IN_VEHICLE) {
            currentCircle.setFillColor(utils.COLOR_VEHICLE); // Color for still
            currentCircle.setTextLine1(timePeriod / (1000 * 60) + "");
            currentCircle.setTextLine2("mins");
            currentActivity.setText("In Vehicle");

        }else if(activity== DetectedActivity.UNKNOWN)
        {
            currentCircle.setFillColor(utils.COLOR_UNKNOWN); // Color for still
            currentCircle.setTextLine1(timePeriod / (1000 * 60) + "");
            currentCircle.setTextLine2("mins");
            currentActivity.setText("Unknown");

        }
        currentActivityTimePeriod.setText("Since: " + since);
        currentCircle.invalidate();


    }


    void refreshTimeLine(Context context, ArrayList<ActivityDetails> userActivities) {
        linearLayoutTimelineArea.removeAllViews();
        Date lastActivityEnd = null;
        boolean showStartTime = true;
        for (int i = 0; i < userActivities.size(); i++) {

            ActivityDetails activityDetail = userActivities.get(i);
//            Log.i("Data:"+userActivities.)
            if (lastActivityEnd == null) {
                showStartTime = true;
            }
            else {
                if (activityDetail.end.getTime() == lastActivityEnd.getTime()) {
                    continue;
                }

                if (activityDetail.start.getTime() - lastActivityEnd.getTime() > 50 * 1000 /*&& previousActivity != DetectedActivity.UNKNOWN*/ ) // insert a blanktimeline element if more than one minute missing

                {
                    Log.i("Blank activity", "I am here");
                    BlankTimeLineElement blank = new BlankTimeLineElement(context);
                    linearLayoutTimelineArea.addView(blank, 0);
                    showStartTime = true;

                } else
                    showStartTime = false;
            }

            if (activityDetail.activityType == DetectedActivity.UNKNOWN) {

//                if(previousActivity != DetectedActivity.UNKNOWN) {
//                    Log.i("Blank activity 2", "I am there");

                    BlankTimeLineElement blank = new BlankTimeLineElement(context);
                    linearLayoutTimelineArea.addView(blank, 0);
//                }
            } else {
                CompositeTimeLineElement element = new CompositeTimeLineElement(context, activityDetail.activityType, activityDetail.timePeriod, activityDetail.noOfSteps, activityDetail.start, activityDetail.end, showStartTime);
                linearLayoutTimelineArea.addView(element, 0);
            }
            lastActivityEnd = activityDetail.end;
            previousActivity = activityDetail.activityType;
            //System.out.println("Camsdin refreshtimeline");

        }


    }


    void showWorking(Date workingSinceTimestamp) {
        SimpleDateFormat sf = new SimpleDateFormat("HH:mm");
        this.workingSinceTimestamp = workingSinceTimestamp;
        currentCircle.setFillColor(utils.COLOR_STILL); // Color for still
        currentCircle.setTextLine1("0");
        currentCircle.setTextLine2("mins");
        currentActivity.setText("Working");
        currentActivityTimePeriod.setText("Since:" + sf.format(workingSinceTimestamp));
        currentCircle.init();
        startTimer();
    }


    protected void startTimer() {
        isTimerRunning = true;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                mHandler.obtainMessage(1).sendToTarget();

            }
        }, 0, 60000);
        //timer.
    }

    ;

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            int timePeriod = (int) (Calendar.getInstance().getTime().getTime() - workingSinceTimestamp.getTime()) / 60000;
            currentCircle.setTextLine1(timePeriod + "");
            currentCircle.init();
        }
    };


    void stopShowingWorking() {
        try {
            if (timer != null) {
                timer.cancel();
            }
            currentActivity.setText("Unknown");
            currentCircle.setTextLine1("0");
            currentCircle.init();


        } catch (Exception ex) {

        }
    }
}
