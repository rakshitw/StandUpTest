package com.xrci.standup;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.widget.ProfilePictureView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.xrci.standup.utility.ComplianceModel;
import com.xrci.standup.utility.FusedDataModel;
import com.xrci.standup.utility.GetData;
import com.xrci.standup.views.CircleView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends Activity {


    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private ProfilePictureView profilePictureView;
    private TextView textViewUserName, textViewPcSyncId, textViewOrg;
    //    private DailyStatisticsCircle dSc;
    static private boolean userIsWorking = false;
    static private Date workingSinceTimeStamp;
    static public String CURRENT_STATUS = "CURRENT_STATUS";
    LocalBroadcastManager broadcaster;
    DatabaseHandler dbHandler;


    public static final int SWIPE_MIN_DISTANCE = 120;
    public static final int SWIPE_MAX_OFF_PATH = 250;
    public static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector mGesture;

    private PendingIntent pIntent;
    private BroadcastReceiver receiver;
    private BroadcastReceiver receiverGCM;
    private MainFragment fragment;

    public static ProgressDialog progressDialog;



    // Rakshit 23-02-2015

    public static final String TAG = "MainActivity";
    private static final int REQUEST_OAUTH = 1;
    private BroadcastReceiver mFitStatusAndResolveReceiver;
    public static String MAIN_ACTIVITY_INTENT = "main_activity_intent";
    public static boolean isGoalCircle = false;
    private OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener() {


        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //Log.v("fling", "Flinged.");
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
                    return false;
                }

                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    //Start week log

                    Intent startWeeklyLog = new Intent(getApplicationContext(), WeeklyActivity.class);
                    startActivity(startWeeklyLog);

//                    Toast.makeText(getApplicationContext(), "Flip Right to Left", Toast.LENGTH_SHORT).show();


                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                    Toast.makeText(getApplicationContext(), "Flip Left to Right", Toast.LENGTH_SHORT).show();

                }
            } catch (Exception e) {
                Logger.appendLog("Exception in onFling(MainActivity):" + e.getMessage(), true);
            }

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.side_menu);
            Log.i(TAG, "main activity created");
            // Initializing
            //dataList = new ArrayList<DrawerItem>();
            mTitle = mDrawerTitle = getTitle();
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            //mDrawerList = (ListView) findViewById(R.id.left_drawer);

            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                    GravityCompat.START);

            progressDialog = new ProgressDialog(this);
            dbHandler = new DatabaseHandler(this);

//            dSc = (DailyStatisticsCircle) findViewById(R.id.dailyStatisiticsCircle);


            getActionBar().setDisplayHomeAsUpEnabled(true);
            //getActionBar().setHomeButtonEnabled(true);

            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                    R.drawable.ic_drawer, R.string.drawer_open,
                    R.string.drawer_close) {
                public void onDrawerClosed(View view) {
                    getActionBar().setTitle(mTitle);
                    invalidateOptionsMenu(); // creates call to
                    // onPrepareOptionsMenu()
                }

                public void onDrawerOpened(View drawerView) {
                    getActionBar().setTitle(mDrawerTitle);
                    invalidateOptionsMenu();
                    //refreshStatisticsCircle();
                    // creates call to
                    // onPrepareOptionsMenu()
                }
            };

            mDrawerLayout.setDrawerListener(mDrawerToggle);
            displayUserDetails();
            //setUpActivityRecognitionClient();


            displayFragment();
            setUpReceiverFromStepService();
            //setUpReceiverFromGCMService();

            //startListeningFromGCMService();
//            refreshStatisticsCircle();
            //refreshTimeLine();

            mGesture = new GestureDetector(this, mOnGesture);

            // Rakshit

            if (!isMyServiceRunning(StepService.class)) {
                Intent startStepService = new Intent(getApplicationContext(), StepService.class);
                startService(startStepService);
            }



            setGoogleFitHandler();
            startListeningFromStepService();
            startListeningFromMonitoringService();

            /**
             * Immediately propagate circleViews from StepService
             */
//            Intent currentDetail = new Intent(CURRENT_STATUS);
//            broadcaster = LocalBroadcastManager.getInstance(this);
//            broadcaster.sendBroadcast(currentDetail);


            //Starting tracking alarm manager if not running
//            checkAndStartTrackingAlarm();

        } catch (Exception e) {
            Logger.appendLog("Exception in onCreate(MainActivity):" + e.getMessage(), true);
        }


    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    //Added for tracking alarm manager

    /**
     * make sure that time for alarm is same in BootCompleteIntentReceiver.class also
     */
//    private void checkAndStartTrackingAlarm() {
//        try {
//            Context context = getApplicationContext();
//            Intent intent = new Intent(context, TrackStepService.class);
//            boolean isStarted = PendingIntent.getBroadcast(context, 0, intent,
//                    PendingIntent.FLAG_NO_CREATE) != null;
//            if (!isStarted) {
//                Log.i(TAG, "tracking alarm not running , starting from main activity");
//                Calendar cal = Calendar.getInstance();
//                cal.add(Calendar.SECOND, 10);
//                Intent trackServiceIntent = new Intent(context, TrackStepService.class);
//
//                PendingIntent pendingIntent = PendingIntent.getService(context, 0, trackServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//                AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//                alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
//                        3 * 60 * 1000, pendingIntent);
//            } else
//                Log.i(TAG, "tracking alarm already running");
//        } catch (Exception e) {
//            Log.i(TAG, "Exception in alarm tracking in main activity");
//            e.printStackTrace();
//        }
//
//    }

    // rakshit

    /**
     * Listener for google fit handler
     */
    void startListeningFromStepService() {
        LocalBroadcastManager.getInstance(this).registerReceiver((mFitStatusAndResolveReceiver),
                new IntentFilter(StepService.STEP_MESSAGE_INTENT));
    }

    /**
     * Receiver for handling google fit authentication, like sign in
     * show the status etc
     */

    private void setGoogleFitHandler() {
        mFitStatusAndResolveReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Get extra data included in the Intent
                if (intent.hasExtra(StepService.FIT_EXTRA_NOTIFY_FAILED_STATUS_CODE) &&
                        intent.hasExtra(StepService.FIT_EXTRA_NOTIFY_FAILED_STATUS_CODE)) {
                    //Recreate the connection result
                    int statusCode = intent
                            .getIntExtra(StepService.FIT_EXTRA_NOTIFY_FAILED_STATUS_CODE, 0);
                    PendingIntent pendingIntent = intent
                            .getParcelableExtra(StepService.FIT_EXTRA_NOTIFY_FAILED_INTENT);
                    ConnectionResult result = new ConnectionResult(statusCode, pendingIntent);
                    if (intent.getBooleanExtra(StepService.FIT_EXTRA_IS_STATUS, false) == true) {
                        fitShowConnectionStatus(result);
                    }
                    if (intent.getBooleanExtra(StepService.FIT_EXTRA_IS_RESOLUTION, false) == true) {
                        Log.i(TAG, "Attempting to resolve failed connection");
                        fitHandleFailedConnection(result, intent.
                                getIntExtra(StepService.FIT_EXTRA_REQUEST_OAUTH, 1));
                    }
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        StepService.isMainInForeground = true;
        refreshTimeLine();


    }

    @Override
    protected void onStop() {
        super.onStop();
        StepService.isMainInForeground = false;

    }

    /**
     * @param stepCircle
     */
    public void switchStepCircle(View stepCircle) {
        CircleView switchCircle = (CircleView) stepCircle;
//        switchCircle.init();
        if (!isGoalCircle) {
            setTodayGoal();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("tempSteps", switchCircle.getTextLine1());
            editor.commit();
            int steps = getTodayGoal();
            switchCircle.setTextLine1(steps + "");
            switchCircle.setFillColor(utils.COLOR_STILL);
            switchCircle.invalidate();
            isGoalCircle = true;

        } else {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String tempSteps = preferences.getString("tempSteps", "0");
            switchCircle.setTextLine1(tempSteps);
            switchCircle.setFillColor(utils.COLOR_WALK);
            switchCircle.invalidate();
            isGoalCircle = false;
        }
    }

    public void setTodayGoal() {
        String GOALSETDAY = "goalsetday"; //same as in step service
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Date storedDate = new Date(preferences.getLong(GOALSETDAY, 0));
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        Calendar cal1 = GregorianCalendar.getInstance();
        cal1.setTime(new Date());
        Date today = cal1.getTime();

        if (!fmt.format(storedDate).equals(fmt.format(today))) {
            Calendar cal2 = GregorianCalendar.getInstance();
            cal2.setTime(new Date());
            cal2.add(Calendar.DAY_OF_YEAR, -1);
            Date yesterday = cal2.getTime();
            int prevGoal = dbHandler.getDayGoal(yesterday);
            int steps = dbHandler.getDayDataFromActivityLog(yesterday);
            String message;
            int goal;
            if (steps < 4000) {
                goal = 4000;
                if (prevGoal > goal) {
                    goal = (goal + prevGoal) / 2;
                }
                float goalRound = goal / 1000;
                goal = (int) WeeklyAdapter.round(goalRound, 0) * 1000;
                message = "Go for more than " + goal + " steps today";
            } else if (steps < 5000) {
                goal = 5000;
                if (prevGoal > goal) {
                    goal = (goal + prevGoal) / 2;
                }
                float goalRound = goal / 1000;
                goal = (int) WeeklyAdapter.round(goalRound, 0) * 1000;
                message = "Go for more than " + goal + " steps today";
            } else if (steps < 6000) {
                goal = 6000;
                if (prevGoal > goal) {
                    goal = (goal + prevGoal) / 2;
                }
                float goalRound = goal / 1000;
                goal = (int) WeeklyAdapter.round(goalRound, 0) * 1000;
                message = "Go for more than " + goal + " steps today";
            } else if (steps < 7000) {
                goal = 7000;

                if (prevGoal > goal) {
                    goal = (goal + prevGoal) / 2;
                }
                float goalRound = goal / 1000;
                goal = (int) WeeklyAdapter.round(goalRound, 0) * 1000;
                message = "Go for more than " + goal + " steps today";
            } else if (steps < 8000) {
                goal = 8000;
                if (prevGoal > goal) {
                    goal = (goal + prevGoal) / 2;
                }
                float goalRound = goal / 1000;
                goal = (int) WeeklyAdapter.round(goalRound, 0) * 1000;
                message = "Doing great! Now strive for " + goal + " steps today";
            } else if (steps < 9000) {
                goal = 9000;

                if (prevGoal > goal) {
                    goal = (goal + prevGoal) / 2;
                }
                float goalRound = goal / 1000;
                goal = (int) WeeklyAdapter.round(goalRound, 0) * 1000;
                message = "Doing great! Now strive for " + goal + " steps today";
            } else {
                goal = 10000;

                if (prevGoal > goal) {
                    goal = (goal + prevGoal) / 2;
                }
                float goalRound = goal / 1000;
                goal = (int) WeeklyAdapter.round(goalRound, 0) * 1000;
                message = "You are doing awesome, let us strive for " + goal + " steps today";
            }


            dbHandler.setDayGoal(today, goal);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(GOALSETDAY, today.getTime());
            editor.commit();
        }

    }

//    public void setTodayGoal() {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//        Date storedDate = new Date(preferences.getLong(GOALSETDAY, 0));
//        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
//        Calendar cal1 = GregorianCalendar.getInstance();
//        cal1.setTime(new Date());
//        Date today = cal1.getTime();
//
//        if (!fmt.format(storedDate).equals(fmt.format(today))) {
//            goalAchievedNotification = false;
//            Calendar cal2 = GregorianCalendar.getInstance();
//            cal2.setTime(new Date());
//            cal2.add(Calendar.DAY_OF_YEAR, -1);
//            Date yesterday = cal2.getTime();
//            String message;
//            int goal;
//            int steps = dbHandler.getDayDataFromActivityLog(yesterday);
//            if (steps < 4000) {
//                goal = 4000;
//                message = "Go for more than 4000 steps today";
//            } else if (steps < 5000) {
//                goal = 5000;
//                message = "Go for more than 5000 steps today";
//            } else if (steps < 6000) {
//                goal = 6000;
//                message = "Go for more than 6000 steps today";
//            } else if (steps < 7000) {
//                goal = 7000;
//                message = "Go for more than 7000 steps today";
//            } else if (steps < 8000) {
//                goal = 8000;
//                message = "Doing great! Now strive for 8000 steps today";
//            } else if (steps < 9000) {
//                goal = 9000;
//                message = "Doing great! Now strive for 9000 steps today";
//            } else {
//                goal = 10000;
//                message = "You are doing awesome, let us strive for 10000 steps today";
//            }
//
//            if (prevGoal > goal){
//                goal = (goal + prevGoal)/2;
//            }
//            float goalRound = goal/1000;
//            goal = (int)WeeklyAdapter.round(goalRound, 0)*1000;
//
//            dbHandler.setDayGoal(today, goal);
//            dbHandler.setTableNotificationActivityRecords(message, 0, Calendar.getInstance().getTime());
//            showAlert(message);
//            sendNotificationToServer(today, message, DetectedActivity.ON_FOOT, 4000);
//
//            SharedPreferences.Editor editor = preferences.edit();
//            editor.putLong(GOALSETDAY, today.getTime());
//            editor.commit();
//
//        }
//
//    }


    private int getTodayGoal() {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(new Date());
        Date today = cal.getTime();
        return dbHandler.getDayGoal(today);
    }

    /**
     * Choose from multiple google accounts
     *
     * @param result
     */
    private void fitShowConnectionStatus(ConnectionResult result) {
        GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                MainActivity.this, 0).show();
    }

    /**
     * Sign into account to authorize this has to be on activity
     *
     * @param result
     * @param REQUEST_OAUTH
     */
    private void fitHandleFailedConnection(ConnectionResult result, int REQUEST_OAUTH) {
        try {
            result.startResolutionForResult(MainActivity.this, REQUEST_OAUTH);
//            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(MAIN_ACTIVITY_INTENT));

        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    /**
     * Send intent when resolved i.e. after account attached
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            if (resultCode == RESULT_OK) {
                LocalBroadcastManager.getInstance(getApplicationContext())
                        .sendBroadcast(new Intent(MAIN_ACTIVITY_INTENT));
            }
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean handled = super.dispatchTouchEvent(ev);
        handled = mGesture.onTouchEvent(ev);
        return handled;
    }


//    protected void refreshStatisticsCircle() {
//        // TODO Auto-generated method stub
//        try {
//            long timePeriods[] = dbHandler.getTimeOfEachActivityToday(Calendar.getInstance().getTime());
//            int stillTime = (int) timePeriods[DetectedActivity.STILL];
//            int walkTime = (int) timePeriods[DetectedActivity.ON_FOOT];
//            int vehicleTime = (int) timePeriods[DetectedActivity.IN_VEHICLE];
//            int bikeTime = (int) timePeriods[DetectedActivity.ON_BICYCLE];
//
//            int workingTime = (int) timePeriods[utils.ACTIVITY_WORKING];
////            dSc.setArcStartEndAngles(stillTime, walkTime, vehicleTime, bikeTime, workingTime);
////            dSc.init();
//        } catch (Exception e) {
//            Logger.appendLog("Exception in refreshStatisticsCircle(MainActivity):" + e.getMessage(), true);
//        }
//
//
//    }


    void setUpReceiverFromStepService() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // String s = intent.getStringExtra(ActivityMonitoringService.UPDATE_CURRENT_FRAGMENT);
                // do something here.
                try {

                    boolean refreshTimeLineOnly = intent.getBooleanExtra(StepService.REFRESH_TIMELINE_ONLY_STOP_LISTENING, false);
                    boolean refreshFusedTimeLine = intent.getBooleanExtra(StepService.STEPS_FUSE, false);
                    Log.i(TAG, "receiving intent onReceive");
                    boolean updateStepsOnly = intent.getBooleanExtra(StepService.UPDATE_STEPS_ONLY, false);
                    Log.i(TAG, "UPDATE_STEPS_ONLY" + updateStepsOnly);
                    if (updateStepsOnly) {
                        int steps = intent.getIntExtra(StepService.STEPS, 0);
                        int todaySteps = intent.getIntExtra(StepService.STEPS_TODAY, 0);
                        updateSteps(steps, todaySteps);
                        Log.i(TAG, "No of steps:" + steps);
                        Log.i(TAG, "intent today steps " + todaySteps);
                    }
                    if (refreshFusedTimeLine) {
                        Context passContext = getApplicationContext();
                        UpdateTableForFusedTimelineForMain updateTableForFusedTimelineForMain = new UpdateTableForFusedTimelineForMain(context);
                        updateTableForFusedTimelineForMain.execute();

                    }
                    if (refreshTimeLineOnly) {
                        //if(workingSinceTimeStamp!=null)
                        //removeOverlappingEntries(workingSinceTimeStamp);
                        //sendUpdateToServer();
                        refreshTimeLine();
                        //stopListeningFromMonitoringService();
                    } else {
                        long timePeriod = intent.getLongExtra(StepService.TIME_PERIOD, 0);
                        //System.out.println("tp:"+timePeriod);
                        String since = intent.getStringExtra(StepService.SINCE);
                        int activity = intent.getIntExtra(StepService.ACTIVITY, 0);
                        int todaySteps = intent.getIntExtra(StepService.STEPS_TODAY, 0);
                        updateCurrentActivityLayout(activity, timePeriod, since, todaySteps);

                        boolean refreshTimeline = intent.getBooleanExtra(StepService.REFRESH_TIMELINE, false);
                        if (refreshTimeline) {
                            refreshTimeLine();
                        }

                    }
                } catch (Exception e) {
                    Log.i(TAG, "Exception in onReceive MonitoringService(MainActivity):" + e.getMessage());

                    Logger.appendLog("Exception in onReceive MonitoringService(MainActivity):" + e.getMessage(), true);
                }

            }
        };

    }

	
	/*
    protected void sendUpdateToServer() {
		// TODO Auto-generated method stub
		
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			
			String response ;
			int successTag=0;
			
				
			@Override
			protected Void doInBackground(Void... arg0) {
				
					//System.out.println("obj="+obj.toString());
				try
				{
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());	
				String fbid = preferences.getString("fbid","");
					
				String jsonBody=dbHandler.fetchUserActivityJSONToBeSynced(fbid);
				System.out.println(jsonBody);
				response = PostData.postContent(utils.SERVER_UPDATE_URI,jsonBody);
				Date lastSynced=Calendar.getInstance().getTime();
				
				System.out.println("Tag:"+response);
				if(response.contains("exception"))
					successTag=0;
				else if(response.contains("successupto")) 
				{	
					Editor editor=preferences.edit();
					SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String lastSyncedString=sf.format(lastSynced);
					editor.putString("lastSynced",lastSyncedString); 
					successTag=1;
					JSONObject obj=new JSONObject(response);
					int rowsupdatedupto=obj.getInt("successupto");
					dbHandler.updateSyncedStatusUptoRowId(rowsupdatedupto,lastSyncedString);
				}
				} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Logger.appendLog("Exception in sendUpdatetoServer(MainActivity):"+e.getMessage(), true);
	        	   
				
				successTag=0;
				
				//jobUuid="exception";
			}
			return null;
		}
			@Override
			protected void onPostExecute(Void result) {
				
					if(successTag==1)
					{
					//updateSuggestStatsLayouts(response)	;
					
					
						
						
					}
					else
					{
						
						Toast.makeText(getApplicationContext(), "Unable to contact Server", Toast.LENGTH_SHORT).show();
					}
					
					
					
					//b.setEnabled(true);
				
				}
			
				
		};
		task.execute((Void[])null);
		
	}
*/




/*
    protected void removeOverlappingEntries(Date workingSinceTimeStamp2) {
		// TODO Auto-generated method stub
		try
		{
		dbHandler.removeOverlappingEntries(workingSinceTimeStamp2);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			Logger.appendLog("Exception in removeOverlappingEntries(MainActivity):"+e.getMessage(), true);
     	   
		}
		
	}
	*/


    public void onBackPressed() {

    }


/*
    private void setUpReceiverFromGCMService() {
		// TODO Auto-generated method stub
		receiverGCM=new  BroadcastReceiver()
		{

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String messageFromGCM=intent.getStringExtra(GcmIntentService.GCM_MESSAGE);
				parseMessageFromGCM(messageFromGCM);
				
			}
		};
		
		
		
		
		
	}
	*/


    protected void refreshTimeLine() {
        // TODO Auto-generated method stub
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean isFused = preferences.getBoolean("isFusedTimeLine", false);
        if (!isFused) {
            ArrayList<ActivityDetails> userActivities;
            try {
                userActivities = dbHandler.fetchAllActivitiesToday(Calendar.getInstance().getTime());
                if (userActivities.size() > 0)
                    fragment.refreshTimeLine(this, userActivities);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                Logger.appendLog("Exception in refreshTimeLine(MainActivity):" + e.getMessage(), true);

            }
//            refreshStatisticsCircle();
        }


    }

    protected void refreshFusedTimeLine() {
        // TODO Auto-generated method stub
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isFusedTimeLine", true);
        editor.commit();

        ArrayList<ActivityDetails> userActivities;
        try {
            Date date = Calendar.getInstance().getTime();
            userActivities = dbHandler.fetchAllFusedActivitiesToday(date);
            Log.i(TAG, "user activities to be printed = " + userActivities.size());
            if (userActivities.size() > 0)
                fragment.refreshTimeLine(this, userActivities);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.i(TAG, "Exception in refreshTimeLine(MainActivity):" + e.getMessage());

        }


    }


    protected void updateCurrentActivityLayout(int activity, long timePeriod,
                                               String since, int todaySteps) {
        // TODO Auto-generated method stub
        try {
            fragment.updateCurrentActivity(activity, timePeriod, since, todaySteps);
        } catch (Exception e) {
            Logger.appendLog("Exception in updateCurrentActivityLayout(MainActivity):" + e.getMessage(), true);
        }


    }


    protected void updateSteps(int steps, int todaySteps) {
        // TODO Auto-generated method stub
        try {
            if (fragment != null) {
                fragment.updateSteps(steps, todaySteps);
            }
        } catch (Exception e) {
            Logger.appendLog("Exception in updateSteps(MainActivity):" + e.getMessage(), true);
        }

    }


    void startListeningFromMonitoringService() {
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver), new IntentFilter(StepService.UPDATE_CURRENT_FRAGMENT));

    }


    void stopListeningFromMonitoringService() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }
    /*
    void startListeningFromGCMService()
	{
		LocalBroadcastManager.getInstance(this).registerReceiver((receiverGCM), new IntentFilter(GcmIntentService.GCM_MESSAGE_INTENT));

	}
	*/


    private void displayFragment() {
        // TODO Auto-generated method stub
        try {
            fragment = new MainFragment();
            fragment.setMainFragmentContext(this);
            FragmentManager frgManager = getFragmentManager();
            frgManager.beginTransaction().replace(R.id.content_frame, fragment)
                    .commit();
        } catch (Exception e) {
            Logger.appendLog("Exception in displayFragment(MainActivity):" + e.getMessage(), true);
        }


    }


//    private void startActivityMonitoringService() {
//        // TODO Auto-generated method stub
//        try {
//            if (!ActivityMonitoringService.isMonitoring) {
//                Intent monitoringServiceIntent = new Intent(this, ActivityMonitoringService.class);
//                startService(monitoringServiceIntent);
//            }
//        } catch (Exception e) {
//            Logger.appendLog("Exception in startActivityMonitoringService(MainActivity):" + e.getMessage(), true);
//        }
//
//
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
//        if (item.getItemId() == R.id.action_settings) {
//            Intent i = new Intent(this, SettingsActivity.class);
//            startActivity(i);

//        }
        if (mDrawerToggle.onOptionsItemSelected(item)) {

            return true;
        }

        return false;
    }

    public void onLogout(View v) {
        try {
            Intent monitoringServiceIntent = new Intent(this, StepService.class);
            stopService(monitoringServiceIntent);
            Intent intent = new Intent(getApplicationContext(), FirstScreenActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("showLogout", true);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(BasicInformationForm.registrationFormFilled, false);
            editor.commit();
            finish();
            startActivity(intent);
        } catch (Exception e) {
            Logger.appendLog("Exception in onLogOut(MainActivity):" + e.getMessage(), true);

        }
    }

    void displayUserDetails() {
        String userName = "", fbid = "";
        try {
            textViewPcSyncId = (TextView) findViewById(R.id.textViewPCSyncId);
            textViewOrg = (TextView) findViewById(R.id.textViewOrgName);
            textViewUserName = (TextView) findViewById(R.id.textViewUserName);
            profilePictureView = (ProfilePictureView) findViewById(R.id.selection_profile_pic);
            profilePictureView.setCropped(true);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String email = preferences.getString("registrationEmail", "");
            fbid = preferences.getString("fbid", "");
            userName = preferences.getString("name", "");
            String org = preferences.getString(BasicInformationForm.registrationOrganization, "Unavailable");
//            int userId = preferences.getInt("userId", 0);
            boolean withoutFB = preferences.getBoolean("withoutFB", false);
//            String firstName = userName;
//            if (userName.contains(" ")) {
//                firstName = userName.substring(0, userName.indexOf(" "));
//            }
            if (!userName.isEmpty())
                textViewUserName.setText(userName);
            if (!fbid.isEmpty()) {
                if (!withoutFB)
                    profilePictureView.setProfileId(fbid);
                textViewPcSyncId.setText(email);
            }
            if(!org.isEmpty()) {
                textViewOrg.setText(org);
            }

        } catch (Exception e) {
            Logger.appendLog("Exception in displayUserDetails(MainActivity):" + e.getMessage(), true);

        }
    }

	/*
     private void parseMessageFromGCM(String string) {
			// TODO Auto-generated method stub
		 SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		 System.out.println("message from gcm:"+string);
	    		try
	    		{
	    			JSONObject jobj=new JSONObject(string);
	    			String type=jobj.getString("type");
	    			JSONObject data=jobj.getJSONObject("data");
	    			if(type.contentEquals("start"))
	    			{
	    				Date lastWorkingStartTime=sf.parse(data.getString("starttime"));
	    				Date lastWorkingEndTime=sf.parse(data.getString("endtime"));
	    				//startMonitoringAfterWorking(lastWorkingStartTime,lastWorkingEndTime);
	    			}
	    			else if(type.contentEquals("stop"))
	    			{
	    				Date workingStartTime=sf.parse(data.getString("starttime"));
	    				stopMonitoringAndShowWorking(workingStartTime);
	    			}
	    			else if(type.contentEquals("alert"))
	    			{
	    				Date workingSinceTimeStamp=sf.parse(data.getString("starttime"));
	    				int notificationNumber=data.getInt("notificationnumber");
	    				showAlertAndUpdateNotification(workingSinceTimeStamp,notificationNumber);
	    			}
	    			
	    			
	    		}
	    		catch(Exception ex)
	    		{
	    			System.out.println(ex.getMessage());
	    		}
		}

*/


    private void showAlertAndUpdateNotification(Date workingSinceTimeStamp,
                                                int notificationNumber) {
        // TODO Auto-generated method stub

    }

    public void onNotifications(View view) {
        Intent intent = new Intent(this, NotificationLogActivity.class);
        startActivity(intent);
    }

    public void onWeek(View view) {

        Intent intent = new Intent(this, WeeklyActivity.class);
        startActivity(intent);

    }

    public void onCustomize(View view) {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    public void onLeaderBoard(View view) {
        Intent intent = new Intent(this, LeaderBoardActivity.class);
        startActivity(intent);
    }

    public void onFeedBack(View view) {
        Intent intent = new Intent(this, FeedbackActivity.class);
        startActivity(intent);

    }

    //TODO remove hard coded stuff
//    public void onFusedClick(View view) {
//        TextView fusedTimeLine = (TextView) findViewById(R.id.textFusedTimeline);
//
//        if (fusedTimeLine.getText().equals("Show Fused Timeline")) {
//            ShowFusedTimeline showFusedTimeline = new ShowFusedTimeline();
//            showFusedTimeline.execute();
//        } else if (fusedTimeLine.getText().equals("Back to Live Timeline")) {
//            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putBoolean("isFusedTimeLine", false);
//            editor.commit();
//
//            refreshTimeLine();
//            fusedTimeLine.setText("Show Fused Timeline");
//            TextView timelineStatus = (TextView) findViewById(R.id.timelineStatusTextView);
//            timelineStatus.setText("Currently Showing Live Timeline");
//
//        }
//
//    }

    public class UpdateTableForFusedTimelineForMain extends AsyncTask<Void, Void, String> {
        Context context;

        public UpdateTableForFusedTimelineForMain(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(Void... params) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            int userid = sharedPreferences.getInt("userId", 1);
            Log.i(TAG, "updating table for timeline");
            String response = FusedDataModel.getFusedDataForMainTimeline(Calendar.getInstance().getTime(), userid, dbHandler);
            ComplianceModel complianceModel = new ComplianceModel(Calendar.getInstance().getTime(), userid);
            String complianceResponse = complianceModel.getCompliance();
            Log.i(TAG, "compliance response is " + complianceResponse);

            return complianceResponse;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try {
                if (!response.equals(GetData.INVALID_RESPONSE)
                        && !response.equals(GetData.INVALID_PAYLOAD) && !response.equals(GetData.EXCEPTION)) {

                    CircleView circleView = (CircleView) findViewById(R.id.complianceCircle);
                    circleView.setTextLine1(response + "%");
                    if (Integer.parseInt(response) >= 50) {
                        circleView.setFillColor(utils.COLOR_WALK);
                    } else
                        circleView.setFillColor(utils.COLOR_STILL);

                    circleView.invalidate();
                }
                refreshTimeLine();
            } catch (Exception e) {
                Log.i(TAG, "exception in postExecute " + e.getMessage());
            }
        }
    }


//    public class ShowFusedTimeline extends AsyncTask<Void, Void, String> {
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            TextView fusedTimeLine = (TextView) findViewById(R.id.textFusedTimeline);
//            fusedTimeLine.setText("Fusing Timeline...");
//        }
//
//        @Override
//        protected String doInBackground(Void... params) {
//            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//            int userid = sharedPreferences.getInt("userId", 1);
//            String response = FusedDataModel.getFusedData(Calendar.getInstance().getTime(),userid, dbHandler);
//            return response;
//        }
//
//        @Override
//        protected void onPostExecute(String response) {
//            super.onPostExecute(response);
//
//            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
//
//            if (response.equals(PostData.INVALID_RESPONSE) || response.equals(PostData.EXCEPTION)) {
//
//                alertDialogBuilder.setTitle("Internet Connection Unavailable");
//
//                alertDialogBuilder
//                        .setMessage("Check your internet connection and try again.")
//                        .setCancelable(true)
//                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                // if this button is clicked, just close
//                                // the dialog box and do nothing
//                                dialog.cancel();
//                            }
//                        });
//
//                // create alert dialog
//                AlertDialog alertDialog = alertDialogBuilder.create();
//                // show it
//                alertDialog.show();
//
//
//            } else if (response.equals(PostData.INVALID_PAYLOAD)) {
//
//
//                alertDialogBuilder.setTitle("Oops");
//
//                alertDialogBuilder
//                        .setMessage("This is embarrassing . Something went wrong.")
//                        .setCancelable(true)
//                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                // if this button is clicked, just close
//                                // the dialog box and do nothing
//                                dialog.cancel();
//                            }
//                        });
//
//                // create alert dialog
//                AlertDialog alertDialog = alertDialogBuilder.create();
//                // show it
//                alertDialog.show();
//
//            }
//
//            refreshFusedTimeLine();
//            TextView timelineStatus = (TextView) findViewById(R.id.timelineStatusTextView);
//            timelineStatus.setText("Currently Showing Fused Timeline");
//            TextView fusedTimeLine = (TextView) findViewById(R.id.textFusedTimeline);
//            fusedTimeLine.setText("Back to Live Timeline");
//        }
//    }



/*

	private void stopMonitoringAndShowWorking(Date workingStartTime) {
		// TODO Auto-generated method stub
		if(ActivityMonitoringService.isMonitoring)
		{
			Intent intent =new Intent(this,ActivityMonitoringService.class);
			stopService(intent);
			//stopListeningFromMonitoringService();
			
		}
		userIsWorking=true;
		workingSinceTimeStamp=workingStartTime;
		System.out.println(workingSinceTimeStamp.toString());
		//refreshTimeLine();
		
		showWorking(workingStartTime);
		// update UI
		
	}
	
	
	
	void stopShowingWorking()
	{
		fragment.stopShowingWorking();	
	}


	private void showWorking(Date workingStartTime) {
		// TODO Auto-generated method stub
		fragment.showWorking(workingStartTime);
		
	}



*/
/*
	private void startMonitoringAfterWorking(Date lastWorkingStartTime,
			Date lastWorkingEndTime) {
		// TODO Auto-generated method stub
		
		storeWorkingActivity(lastWorkingStartTime,lastWorkingEndTime);
		refreshTimeLine();
		System.out.println("Came here in start");
		userIsWorking=false;
		workingSinceTimeStamp=null;
		startActivityMonitoringService();
		startListeningFromMonitoringService();
		stopShowingWorking();
		
		
	}





	private void storeWorkingActivity(Date lastWorkingStartTime,
			Date lastWorkingEndTime) {
		// TODO Auto-generated method stub
		UserActivity ua=new UserActivity();
		ua.startTime=lastWorkingStartTime;
		ua.endTime=lastWorkingEndTime;
		
		dbHandler.addUserActivity(utils.ACTIVITY_WORKING, ua,0);
		
	}
	
	
	*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(receiver != null) {
            stopListeningFromMonitoringService();
        }
    }
}
