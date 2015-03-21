package com.xrci.standup;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;
import com.google.android.gms.location.DetectedActivity;
import com.xrci.standup.utility.PostActivityDetailsModel;
import com.xrci.standup.utility.PostNotificationModel;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


//import com.google.android.gms.fit.samples.common.logger.Log;

public class StepService extends Service implements SensorEventListener {
    public static final String TAG = "BasicSensorsApi";
    // [START auth_variable_references]

    private Date start_time = Calendar.getInstance().getTime();
    private Date end_time = Calendar.getInstance().getTime();
    private Date curr_time = Calendar.getInstance().getTime();
    private Date prev_time = Calendar.getInstance().getTime();
    private int initial_count = 0;
    private int final_count = 0;
    private int intermediate_count = 0;
    private int delta_value = 0;
    private static int delta_intermediate_value = 0;
    private int sampling_rate = 5;  //Sample Rate for Sensor
    private static AtomicBoolean hasStepsStarted = new AtomicBoolean(false);
    private static AtomicBoolean hasStepRecordingStarted = new AtomicBoolean(false);


    private Timer myTimer;
    private int timer_interval = 15000; //Keep timer_interval to the maximum time
    // for which step count loss is not an issue

    private int step_gap_time = 15;

    private Value cumulative_val;
    private static final int REQUEST_OAUTH = 1;

    private static AtomicBoolean isStill = new AtomicBoolean(true);
    private Date stillStartTime = Calendar.getInstance().getTime();
    private Date stillEndTime = Calendar.getInstance().getTime();
    private static AtomicBoolean hasStillStarted = new AtomicBoolean(false);
    private static AtomicBoolean hasSittingEndedByUnknown = new AtomicBoolean(false);

    private static AtomicBoolean isUnknown = new AtomicBoolean(false);
    private Date unknownStartTime = Calendar.getInstance().getTime();
    private Date unknownEndTime = Calendar.getInstance().getTime();
    private Date pseudoStartUnknownTime = Calendar.getInstance().getTime();
    private Date pseudoEndUnknownTime = Calendar.getInstance().getTime();
    private static AtomicBoolean hasUnknownRecordingStarted = new AtomicBoolean(false);
    private static AtomicBoolean hasUnknownStarted = new AtomicBoolean(false);


    static final public String UPDATE_CURRENT_FRAGMENT = "com.xrci.standup.update_fragment";
    static final public String REFRESH_TIMELINE = "refresh";
    static final public String REFRESH_TIMELINE_ONLY_STOP_LISTENING = "refresh_only";
    static final public String ACTIVITY = "activity";
    static final public String SINCE = "since";
    static final public String UPDATE_STEPS_ONLY = "updatestepsonly";
    static final public String STEPS = "steps";
    static final public String TIME_PERIOD = "timeperiod";
    public static final String TODAY = "today";
    public static final String STEPS_TODAY = "steps_today";
    public static final String STEPS_FUSE = "steps_fuse";
    private LocalBroadcastManager broadcaster;
    private DatabaseHandler dbHandler;
    private static final String GOALSETDAY = "goalsetday";
    private boolean isUserValidated = false;
    private JSONArray entityArray;
    private JSONArray notificationArray;
    private int userId;
    private Date lastNotificationTime = Calendar.getInstance().getTime();
    private long sittingNotificationTime = 40 * 60 * 1000; //40 minutes
    private long minNotificationGapTime = 10 * 60 * 1000;
    private boolean goalAchievedNotification = false;
    private Date lastFusedTime = Calendar.getInstance().getTime();
    private long fuseTimeGap = 10 * 60 * 1000;
//    private long noNotificationRange =
    /**
     * Track whether an authorization activity is stacking over the current activity, i.e. when
     * a known auth error is being resolved, such as showing the account chooser or presenting a
     * consent dialog. This avoids common duplications as might happen on screen rotations, etc.
     */
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;

    private GoogleApiClient mClient = null;
//    private int samplingRate = 5;
    // [END auth_variable_references]

    // [START mListener_variable_reference]
    // Need to hold a reference to this listener, as it's passed into the "unregister"
    // method in order to stop all sensors from sending data to this listener.
    private OnDataPointListener mListener;
    // [END mListener_variable_reference]
    static final public String STEP_MESSAGE_INTENT = "step_service_intent";
    static final public String FIT_EXTRA_NOTIFY_FAILED_STATUS_CODE = "fit_error_code";
    static final public String FIT_EXTRA_NOTIFY_FAILED_INTENT = "fit_notify_failed_intent";
    static final public String FIT_EXTRA_IS_STATUS = "is_status";
    static final public String FIT_EXTRA_IS_RESOLUTION = "is_resolution";
    static final public String FIT_EXTRA_REQUEST_OAUTH = "request_oauth";

    private SharedPreferences preferences;
    private SharedPreferences.Editor dateEditor;
    private NotificationManager mNotificationManager;

    BroadcastReceiver fitResolutionBroadcastReceiver;

    private PowerManager.WakeLock mWakeLock;


    public StepService() {

    }

    /**
     * Broadcast intent to UI Thread (Main Activity)
     * to resolve issues like sign in or show status
     *
     * @param result
     * @param isResolve
     * @param isStatus
     */
    private void notifyUiFailedConnection(ConnectionResult result, Boolean isResolve, Boolean isStatus) {
        Intent intent = new Intent(STEP_MESSAGE_INTENT);
        intent.putExtra(FIT_EXTRA_NOTIFY_FAILED_STATUS_CODE, result.getErrorCode());
        intent.putExtra(FIT_EXTRA_NOTIFY_FAILED_INTENT, result.getResolution());
        intent.putExtra(FIT_EXTRA_IS_RESOLUTION, isResolve);
        intent.putExtra(FIT_EXTRA_IS_STATUS, isStatus);
        intent.putExtra(FIT_EXTRA_REQUEST_OAUTH, REQUEST_OAUTH);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {

        if (mClient.isConnected()) {
            unregisterFitnessDataListener();
            mClient.disconnect();
        }

        if (mWakeLock != null)
            mWakeLock.release();

        if (mAccelerometer != null)
            mSensorManager.unregisterListener(this);

        if (mNotificationManager != null)
            mNotificationManager.cancel(1);


    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Authenticate
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
//        String fbid = preferences.getString("fbid", "1");
        String userName = preferences.getString("name", "default");
        Log.i(TAG, "name is " + userName);

        Date cal = Calendar.getInstance().getTime();
//        Date today =
//        cal.add(Calendar.DAY_OF_YEAR, 0);
        dbHandler = new DatabaseHandler(this);
        int stepsToday = dbHandler.getDayDataFromActivityLog(cal);
        Log.i(TAG, "date is " + cal.toString() + "steps are " + stepsToday);
        editor.putLong(TODAY, cal.getTime());
        editor.putInt(STEPS, stepsToday);
//
//        //TODO check what to do when not authenticated, though a rare chance.
//        if (response.equals(PostData.INVALID_RESPONSE)) {
////                 Toast.makeText(getApplicationContext(), "Make sure you are connected to internet"
////                            ,Toast.LENGTH_SHORT).show();
//        } else if (response.equals(PostData.INVALID_PAYLOAD)) {
////            Toast.makeText(getApplicationContext(), "Unable to validate user"
////                ,Toast.LENGTH_SHORT).show();
//        } else
//        } else
//            isUserValidated = true;
//
//        if (isUserValidated == true) {
//            Log.i(TAG, "user id throught setUserId" + setUserId(response));
//            editor.putInt("userId", setUserId(response));
//            Log.i(TAG, "user is validated " + userId);
//
//        } else {
//            Log.i(TAG, "user not validated" + userId);
//        }
        editor.commit();


        //initialize entityArray and notification array
        entityArray = new JSONArray();
        notificationArray = new JSONArray();

//        Log.i(TAG, "is user validated " + isUserValidated );
//        Log.i(TAG, "authentication response is " + response);


        Log.i(TAG, "service started");
        /**
         * Initialize sensor manager for proximity and
         * accelerometer to find if the phone is on table or not
         */
        initializeSensor();

        /**
         * Initialize On foot end and start time
         */

        start_time = Calendar.getInstance().getTime();
        end_time = Calendar.getInstance().getTime();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showAlert("service started at " + Calendar.getInstance().getTime());
        //Set step goal
        setTodayGoal();
//        Logger.appendLog("service started", true);
        myTimer = new Timer();
        //Regularly checks if end time for
        // a step session has reached or not
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkInterval();
                Log.i(TAG, "in timer");
            }
        }, 0, timer_interval);

        setmWakeLock();
        // [START auth_connection_flow_in_activity_lifecycle_methods]
        //Recieve and listen for any google API Resolution
        // Set local broadcast manager

        broadcaster = LocalBroadcastManager.getInstance(this);
        setReceiveFitResolutionReceiver();
        startListeningFromMainActivity();
        buildFitnessClient();
        Log.i(TAG, "Connecting...");
        mClient.connect();
    }

    /**
     * If google service issue is resolved
     * then the client is reconnected.
     */
    public void setReceiveFitResolutionReceiver() {
        fitResolutionBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "in receiver for resolution");
                //notifyUiFailedConnection is blocked till authInProgress != false
                authInProgress = false;
                // Make sure the app is not already connected or attempting to connect
                if (!mClient.isConnecting() && !mClient.isConnected()) {
                    Log.i(TAG, "trying to connect again in receiver");
                    mClient.connect();
                }
            }

        };
    }

    /**
     * Listener for resolution of Google Service issues
     */
    void startListeningFromMainActivity() {
        LocalBroadcastManager.getInstance(this).registerReceiver((fitResolutionBroadcastReceiver),
                new IntentFilter(MainActivity.MAIN_ACTIVITY_INTENT));
    }

    /**
     * Initialize the proximity and accelerometer sensor
     */
    private void initializeSensor() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        accel = 0.0f;
        accelLast = SensorManager.GRAVITY_EARTH;
        accelNow = SensorManager.GRAVITY_EARTH;
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);

    }

    /**
     *
     */

    private void setmWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
        mWakeLock.acquire();
    }

    /**
     * To find the start-end interval for number of steps.
     */
    public void checkInterval() {
        Calendar c = Calendar.getInstance();
        curr_time = c.getTime();

        if (isStill.get()) {
            if (!hasStillStarted.get()) {
                hasStillStarted.set(true);
                stillStartTime = end_time;
                Log.i(TAG, "still time started at " + stillStartTime);
            }
            if (hasUnknownRecordingStarted.get()) {
//                Log.i(TAG, "has unknown started set to true");
//                isUnknown.set(true);
                stillEndTime = unknownStartTime;

                Log.i(TAG, "hasSittingEndedByUnknown = true");

            }
            Log.i(TAG, "isUnknown.get = " + isUnknown.get());

            if (isUnknown.get()) {
                //Do nothing
                if (curr_time.getTime() - lastFusedTime.getTime() > fuseTimeGap) {
                    Log.i(TAG, "doing fusion in unknown");
                    updateActivityUI(DetectedActivity.UNKNOWN, unknownStartTime
                            , curr_time.getTime() - unknownStartTime.getTime(), false, false, 0, false, getTotalStepsToday(0), true);
                    lastFusedTime = curr_time;
                } else
                    updateActivityUI(DetectedActivity.UNKNOWN, unknownStartTime
                            , curr_time.getTime() - unknownStartTime.getTime(), false, false, 0, false, getTotalStepsToday(0), false);


//
// PostData.postContent()
            } else {
                Log.i(TAG, "updating sitting UI");
//                String responseFromGet = FusedDataModel.getFusedData(2, Calendar.getInstance().getTime(), Calendar.getInstance().getTime());
//                Log.i(TAG, "response of getfuseddata is " + responseFromGet);
                Date notificationTime = Calendar.getInstance().getTime();
                int notificationHour = notificationTime.getHours();
                int notificationMinute = notificationTime.getMinutes();
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String startTimeToCompare = sharedPreferences.getString(SettingsActivity.stopPingTimePeriodStart, "22:00");
                String endTimeToCompare = sharedPreferences.getString(SettingsActivity.stopPingTimePeriodEnd, "07:00");

                int startCompareHours = Integer.parseInt(startTimeToCompare.substring(0,2));
                int startCompareMins = Integer.parseInt(startTimeToCompare.substring(3,5));
                long notificationHourMin =  notificationHour*60 + notificationMinute;
                long compareStartHourMin = startCompareHours*60 + startCompareMins;

                int endCompareHours = Integer.parseInt(endTimeToCompare.substring(0,2));
                int endCompareMins = Integer.parseInt(endTimeToCompare.substring(3,5));

                long compareEndHourMin = endCompareHours*60 + endCompareMins;


                /**
                 * Interesting logic: if do not ping start time < do not ping end time
                 * Then notification can be send when notification time is on either side of time line
                 * OR
                 * If start time > end time then notification can be send when
                 * hour+min of notification is between end and start time
                 */
                if (((compareStartHourMin < compareEndHourMin) && ((notificationHourMin < compareStartHourMin)
                        ||(notificationHourMin > compareEndHourMin)))
                        || ((compareStartHourMin > compareEndHourMin) && (notificationHourMin > compareEndHourMin)
                        && (notificationHourMin < compareStartHourMin))){

                    if ((curr_time.getTime() - stillStartTime.getTime()) > sittingNotificationTime
                            && (curr_time.getTime() - lastNotificationTime.getTime()) > minNotificationGapTime) {
                        long timePeriod = curr_time.getTime() - stillStartTime.getTime();
                        String displayText = "Stand up! You have been " +
                                "still for " + (int) timePeriod / 60000 + " minutes now";
                        lastNotificationTime = curr_time;
                        dbHandler.setTableNotificationActivityRecords(displayText, DetectedActivity.STILL, curr_time);

                        showAlert(displayText);
                        sendNotificationToServer(curr_time, displayText, DetectedActivity.STILL, 0);
                    }


                }
                updateActivityUI(DetectedActivity.STILL, stillStartTime, curr_time.getTime()
                        - stillStartTime.getTime(), false, false, 0, false, getTotalStepsToday(0), false);

            }


        }

        if (isStill.get() && hasUnknownRecordingStarted.get() && !isUnknown.get()) {
            Log.i(TAG, "unknownRecording Stopped");
            hasUnknownRecordingStarted.set(false);
            stillStartTime = unknownEndTime;
            ActivityDetails activityDetails = new ActivityDetails(DetectedActivity.UNKNOWN
                    , unknownEndTime.getTime() - unknownStartTime.getTime(), unknownStartTime, unknownEndTime, 0);
            dbHandler.addUserActivity(DetectedActivity.UNKNOWN, activityDetails, 0);

            //TODO check the parameters
            updateActivityUI(DetectedActivity.UNKNOWN, unknownStartTime, 0, true, false, 0, true, getTotalStepsToday(0), false);
            if (unknownStartTime.getTime() >= stillEndTime.getTime() && unknownStartTime.getTime() >= end_time.getTime() && (unknownEndTime.getTime() > unknownStartTime.getTime())) {

                //Send to server too
                sendActivityDetailToServer(unknownStartTime, unknownEndTime, DetectedActivity.UNKNOWN, 0);

                Log.i(TAG, "end time for unknown is " + unknownEndTime);
            }

        } else if (!isStill.get() && hasStillStarted.get()) {
            hasStillStarted.set(false);
            stillEndTime = start_time;


//            stillStartTime = end_time;
            ActivityDetails activityDetails = new ActivityDetails(DetectedActivity.STILL, stillEndTime.getTime() - stillStartTime.getTime(), stillStartTime, stillEndTime, 0);
            dbHandler.addUserActivity(DetectedActivity.STILL, activityDetails, 0);
            updateActivityUI(DetectedActivity.STILL, stillStartTime, 0, true, false, 0, true, getTotalStepsToday(0), false);
            if (stillStartTime.getTime() >= unknownEndTime.getTime() && stillStartTime.getTime() >= end_time.getTime() && stillEndTime.getTime() > stillStartTime.getTime()) {

                //Send to server too
                sendActivityDetailToServer(stillStartTime, stillEndTime, DetectedActivity.STILL, 0);
                Log.i(TAG, "end time for still is " + stillEndTime);
            }
//            }
        } else if (isStill.get() && hasSittingEndedByUnknown.get()) {
            hasSittingEndedByUnknown.set(false);
//            if( (stillEndTime.getTime() - stillStartTime.getTime()) > 15000 ) {
            Log.i(TAG, "hasSittingendedbyunknown");
            //TODO check to comment previous line or not.
//            stillEndTime = unknownStartTime;
//            stillStartTime = end_time;
            ActivityDetails activityDetails = new ActivityDetails(DetectedActivity.STILL, stillEndTime.getTime() - stillStartTime.getTime(), stillStartTime, stillEndTime, 0);
            dbHandler.addUserActivity(DetectedActivity.STILL, activityDetails, 0);
            updateActivityUI(DetectedActivity.STILL, stillStartTime, 0, true, false, 0, true, getTotalStepsToday(0), false);
            if (stillEndTime.getTime() > stillStartTime.getTime() && stillStartTime.getTime() >= unknownEndTime.getTime() && stillStartTime.getTime() >= end_time.getTime()) {

                //Send to server too
                sendActivityDetailToServer(stillStartTime, stillEndTime, DetectedActivity.STILL, 0);
                Log.i(TAG, "end time for still is " + stillEndTime);
//            }
            }

        }


        //Avoid 0 step count
        if (!hasStepRecordingStarted.get() && hasStepsStarted.get()) {
            intermediate_count = Integer.parseInt(cumulative_val.toString());
            delta_intermediate_value = intermediate_count - initial_count;
            Log.i(TAG, "intermediate total count in check interval " + intermediate_count + "intermediate  delta = " + delta_intermediate_value);
            if (delta_intermediate_value > 5) {
                hasStepRecordingStarted.set(true);
//                logStartTime = curr_time;
                isStill.set(false);
                //TODO check this code performance
                if (hasUnknownRecordingStarted.get()) {
                    unknownEndTime = start_time;
                    hasSittingEndedByUnknown.set(false);
                    isUnknown.set(false);
                    Log.i(TAG, "is unknown set to false at line 479");
                }

                ActivityDetails activityDetails = new ActivityDetails(DetectedActivity.UNKNOWN,
                        unknownEndTime.getTime() - unknownStartTime.getTime(), unknownStartTime
                        , unknownEndTime, 0);
                dbHandler.addUserActivity(DetectedActivity.UNKNOWN, activityDetails, 0);

                //TODO check the parameters

                updateActivityUI(DetectedActivity.UNKNOWN, unknownStartTime, 0, true, false, 0, true, getTotalStepsToday(0), false);
                if (unknownEndTime.getTime() > unknownStartTime.getTime() && unknownStartTime.getTime() >= stillEndTime.getTime() && unknownStartTime.getTime() >= end_time.getTime()) {
                    //Send to server too
                    sendActivityDetailToServer(unknownStartTime, unknownEndTime, DetectedActivity.UNKNOWN, 0);

//                }


                    Log.i(TAG, "started at in check interval " + start_time);
                }
            } else {

                //initial_count = intermediate_count;
                Log.i(TAG, " not started at in check interval " + start_time);
                hasStepsStarted.set(false);
                isStill.set(true);
            }
        }


        if ((hasStepRecordingStarted.get() && hasStepsStarted.get() && (Math.abs(curr_time.getTime() - prev_time.getTime()) / 1000) > step_gap_time)) {
            end_time = prev_time;
            final_count = Integer.parseInt(cumulative_val.toString());
            delta_value = final_count - initial_count;

            Log.i(TAG, "ended at in check interval " + end_time + " started at " + start_time);
            Log.i(TAG, "Steps taken in the interval = " + delta_value + "    " + "final_count =  " + final_count);
            stillStartTime = end_time;
            ActivityDetails activityDetails = new ActivityDetails(DetectedActivity.ON_FOOT, end_time.getTime() - start_time.getTime(), start_time, end_time, delta_value);
            dbHandler.addUserActivity(DetectedActivity.ON_FOOT, activityDetails, 0);
            //update step circle here
//            Log.i(TAG, "set total steps today " + setTotalStepsToday());
//            if(las)
            updateActivityUI(DetectedActivity.STILL, stillStartTime, 0, true, false, 0, false, getTotalStepsToday(0), false);
            if (end_time.getTime() > start_time.getTime() && start_time.getTime() >= unknownEndTime.getTime() && start_time.getTime() >= stillEndTime.getTime()) {

                //Send to server too
                sendActivityDetailToServer(start_time, end_time, DetectedActivity.ON_FOOT, delta_value);
            }
            setTotalStepsToday(delta_value);

            hasStepsStarted.set(false);
            hasStepRecordingStarted.set(false);
            isStill.set(true);

        }
    }

    /**
     * Build a {@link com.google.android.gms.common.api.GoogleApiClient} that will authenticate the user and allow the application
     * to connect to Fitness APIs. The scopes included should match the scopes your app needs
     * (see documentation for details). Authentication will occasionally fail intentionally,
     * and in those cases, there will be a known resolution, which the OnConnectionFailedListener()
     * can address. Examples of this include the user never having signed in before, or having
     * multiple accounts on the device and needing to specify which account to use, etc.
     */
    private void buildFitnessClient() {
        // Create the Google API Client
        Log.i(TAG, "Create the Google API Client");
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
//                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {

                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i(TAG, "Connected!!!");
                                // Now you can make calls to the Fitness APIs.
                                // Put application specific code here.
                                // [END auth_build_googleapiclient_beginning]
                                //  What to do? Find some data sources!
                                findFitnessDataSources();

                                // [START auth_build_googleapiclient_ending]
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                // If your connection to the sensor gets lost at some point,
                                // you'll be able to determine the reason and react to it here.
                                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Log.i(TAG, "Connection lost.  Reason: Service Disconnected");
                                }
                            }
                        }
                )
                .addOnConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener() {
                            // Called whenever the API client fails to connect.
                            @Override
                            public void onConnectionFailed(ConnectionResult result) {
                                Log.i(TAG, "Connection failed. Cause: " + result.toString());
                                if (!result.hasResolution()) {
                                    // Show the localized error dialog at Activity
                                    notifyUiFailedConnection(result, false, true);

                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                if (!authInProgress) {
                                    Log.i(TAG, "Attempting to resolve failed connection");
                                    authInProgress = true;
                                    //broadcast Activity to handle Google API failure
                                    notifyUiFailedConnection(result, true, false);
                                }
                            }
                        }
                )
                .build();
    }

    /**
     * Check if step counter sensor exists or not.
     *
     * @return
     */

    public int stepCounterSensorExists() {
        SensorManager mSensorManager;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            // Success! There's a step counter.
            return DataSource.TYPE_RAW;
        } else {
            // Failure! No step counter.
            return DataSource.TYPE_DERIVED;
        }
    }

    /**
     * Find available data sources and attempt to register on a specific {@link com.google.android.gms.fitness.data.DataType}.
     * If the application cares about a data type but doesn't care about the source of the data,
     * this can be skipped entirely, instead calling
     * {@link com.google.android.gms.fitness.SensorsApi
     * #register(GoogleApiClient, SensorRequest, DataSourceListener)},
     * where the {@link com.google.android.gms.fitness.request.SensorRequest} contains the desired data type.
     */
    private void findFitnessDataSources() {
        // [START find_data_sources]
        Fitness.SensorsApi.findDataSources(mClient, new DataSourcesRequest.Builder()
                // At least one datatype must be specified.
                .setDataTypes(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                        // Can specify whether data type is raw or derived
                        // dependent upon if step count sensor exists or otherwise.
                .setDataSourceTypes(stepCounterSensorExists())

                .build())
                .setResultCallback(new ResultCallback<DataSourcesResult>() {
                    @Override
                    public void onResult(DataSourcesResult dataSourcesResult) {

                        Log.i(TAG, "Result: " + dataSourcesResult.getStatus().toString() + dataSourcesResult.toString());

                        for (DataSource dataSource : dataSourcesResult.getDataSources()) {
                            Log.i(TAG, "Data source found: " + dataSource.toString());
                            Log.i(TAG, "Data Source type: " + dataSource.getDataType().getName());

                            //Let's register a listener to receive Activity data!
                            if (dataSource.getDataType().equals(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                                    && mListener == null) {
                                Log.i(TAG, "Data source for step count found!  Registering.");
                                registerFitnessDataListener(dataSource,
                                        DataType.TYPE_STEP_COUNT_CUMULATIVE);
                            }
                        }
                    }
                });
        // [END find_data_sources]
    }


    /**
     * Register a listener with the Sensors API for the provided {@link DataSource} and
     * {@link DataType} combo.
     */
    private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {
        // [START register_data_listener]
        mListener = new OnDataPointListener() {
            @Override
            public void onDataPoint(DataPoint dataPoint) {
                for (Field field : dataPoint.getDataType().getFields()) {
                    cumulative_val = dataPoint.getValue(field);
                    int intermediateStepCount = Integer.parseInt(cumulative_val.toString())
                            - initial_count;
                    Log.i(TAG, "dataPoint cummulative value?? ");
                    Log.i(TAG, "dataPoint cummulative value " + Integer.parseInt(cumulative_val.toString()));

                    if (hasStepsStarted.get() == false) {
                        Calendar c = Calendar.getInstance();
                        start_time = c.getTime();
                        curr_time = start_time;
                        initial_count = Integer.parseInt(cumulative_val.toString());
                        Log.i(TAG, "is started set to true in datasource initial count:" + initial_count);

                        hasStepsStarted.set(true);

                    }
                    if (hasStepRecordingStarted.get()) {
                        Log.i(TAG, "data point??" + intermediateStepCount);
                        long intermediate_timeperiod = Calendar.getInstance().getTime().getTime()
                                - start_time.getTime();
                        //update step circle here
                        int todaysteps = getTotalStepsToday(intermediateStepCount);
                        int todaygoal = getTodayGoal();
                        if (todaysteps > todaygoal && !goalAchievedNotification) {
                            goalAchievedNotification = true;
                            String message = "Goal Achieved: " + todaygoal + " steps taken";
                            showAlert(message);
                            dbHandler.setTableNotificationActivityRecords(message, DetectedActivity.ON_FOOT, curr_time);
                            sendNotificationToServer(curr_time, message, DetectedActivity.ON_FOOT, todaygoal);
                        }
                        updateActivityUI(DetectedActivity.ON_FOOT, start_time
                                , intermediate_timeperiod, false, true, intermediateStepCount, false, todaysteps, false);
                        Log.i(TAG, "get Total Steps Today = " + getTotalStepsToday(intermediateStepCount));


                    }

                }
                Calendar c = Calendar.getInstance();

                prev_time = curr_time;
                curr_time = c.getTime();

//                checkInterval();

            }
        };

        Fitness.SensorsApi.add(
                mClient,
                new SensorRequest.Builder()
                        .setDataSource(dataSource) // Optional but recommended for custom data sets.
                        .setDataType(dataType) // Can't be omitted.
                                //TODO check how sampling rate affects?
                        .setSamplingRate(sampling_rate, TimeUnit.SECONDS)
                        .build(),
                mListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Listener registered!");
                        } else {
                            Log.i(TAG, "Listener not registered.");
                        }
                    }
                });
        // [END register_data_listener]
    }

    /**
     * Unregister the listener with the Sensors API.
     * UNUSED FOR NOW
     */
    private void unregisterFitnessDataListener() {
        if (mListener == null) {
            // This code only activates one listener at a time.  If there's no listener, there's
            // nothing to unregister.
            return;
        }

        // [START unregister_data_listener]
        // Waiting isn't actually necessary as the unregister call will complete regardless,
        // even if called from within onStop, but a callback can still be added in order to
        // inspect the results.
        Fitness.SensorsApi.remove(
                mClient,
                mListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Listener was removed!");
                        } else {
                            Log.i(TAG, "Listener was not removed.");
                        }
                    }
                });
        // [END unregister_data_listener]
    }


    void updateActivityUI(int activity2, Date startTime, long timePeriod, boolean refreshTimeline
            , boolean updateStepsOnly, int steps, boolean refreshtimeLineOnly, int steps_today, boolean fuseTimeline) {
        try {
            SimpleDateFormat sf = new SimpleDateFormat("HH:mm");
            Intent intent = new Intent(UPDATE_CURRENT_FRAGMENT);
            intent.putExtra(REFRESH_TIMELINE, refreshTimeline);
            intent.putExtra(ACTIVITY, activity2);

            if (startTime != null)
                intent.putExtra(SINCE, sf.format(startTime));
            intent.putExtra(TIME_PERIOD, (long) timePeriod);
            intent.putExtra(UPDATE_STEPS_ONLY, updateStepsOnly);
            intent.putExtra(REFRESH_TIMELINE_ONLY_STOP_LISTENING, refreshtimeLineOnly); // Sent when Monitoring stops, deactivate listener
            intent.putExtra(STEPS, steps);
            intent.putExtra(STEPS_TODAY, steps_today);
            intent.putExtra(STEPS_FUSE, fuseTimeline);


            //System.out.println("TimePeriod"+timePeriod);

            broadcaster.sendBroadcast(intent);
        } catch (Exception e) {
            Logger.appendLog("Exception in updateActivityUI(ActivityMonitoringService)" + e.getMessage(), true);

        }


    }


    private SensorManager mSensorManager;
    private Sensor mProximity;
    private Sensor mAccelerometer;

    private boolean onTable = false;
    //    private boolean isDark = false;
//    private boolean isMoving = false;
    private boolean isFaceUp = true;
    private float accelLast, accelNow, accel;

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float axis[];// = new float[3];
            //float linear_acceleration[] = new float[3];
            axis = event.values.clone();

            float norm = (float) Math.sqrt(axis[0] * axis[0] +
                    axis[1] * axis[1] + axis[2] * axis[2]);

            accelLast = accelNow;
            accelNow = norm;
            float delta = accelNow - accelLast;
            accel = accel * 0.9f + delta;
//            isMoving = accel > 0.25f;
            axis[0] /= norm;
            axis[1] /= norm;
            axis[2] /= norm;
            int inclination = (int) Math.round(Math.toDegrees(Math.acos(axis[2])));


            if (inclination < 10 || inclination > 170) {
//                Log.i(TAG, "FLAT inclination=" + inclination);
                onTable = true;
                pseudoEndUnknownTime = Calendar.getInstance().getTime();
                if (!hasUnknownStarted.get() && isStill.get()) {
                    pseudoStartUnknownTime = Calendar.getInstance().getTime();

                    hasUnknownStarted.set(true);
                }
                if (!hasUnknownRecordingStarted.get() && hasUnknownStarted.get() && ((Calendar.getInstance().getTime().getTime() - pseudoStartUnknownTime.getTime()) > 8000)) {
//                    Log.i(TAG, "unknownstarttime = " +  pseudoStartUnknownTime);
                    unknownStartTime = pseudoStartUnknownTime;

                    hasSittingEndedByUnknown.set(true);
                    stillEndTime = unknownStartTime;
                    Log.i(TAG, "unknown start time = " + unknownStartTime);
                    hasUnknownRecordingStarted.set(true);
                    isUnknown.set(true);
                    Log.i(TAG, "osUnknown set to true at line 856 ");

                }

//                isFaceUp = inclination < 25;
//                if (!isDark && isFaceUp) {
//                    Log.i(TAG, "TABLE face up");
//                }
            } else {
                onTable = false;
                pseudoStartUnknownTime = Calendar.getInstance().getTime();
                if (hasUnknownStarted.get() && isStill.get()) {
                    pseudoEndUnknownTime = Calendar.getInstance().getTime();
                    hasUnknownStarted.set(false);
                }

                if (!hasUnknownStarted.get() && (Calendar.getInstance().getTime().getTime() - pseudoEndUnknownTime.getTime()) > 8000) {
                    if (hasUnknownRecordingStarted.get()) {
                        unknownEndTime = Calendar.getInstance().getTime();
                        Log.i(TAG, "unknown end time = " + unknownEndTime);
                        hasSittingEndedByUnknown.set(false);

                        stillStartTime = unknownEndTime;
                        isUnknown.set(false);
                        Log.i(TAG, "isUnknown set to false");
//                        hasUnknownRecordingStarted.set(false);


                    }
                }
            }

//                int rotation = (int) Math.round(Math.toDegrees(Math.atan2(axis[0], axis[1])));
//                Log.i(TAG, "NOT FLAT");
//                Log.i(TAG, "Rotated : " + rotation + " degrees");
//                if (!isDark) {
//                    Log.i(TAG, "In Hand");
//                }

//        }
// else if (sensor.getType() == Sensor.TYPE_PROXIMITY) {
//            float distance = event.values[0];
//            Log.d(TAG, "Proximity: " + distance);
//            isDark = distance == 0.0f;
//
//            if (isDark) {
//                if (onTable && !isMoving && !isFaceUp) {
//                    Log.i(TAG, "Face down on table");
//                } else {
//                    Log.i(TAG, "POCKET");
//                }
//            } else {
//                if (onTable && !isMoving && isFaceUp) {
//                    Log.i(TAG, "TABLE face up");
//                } else {
//                    Log.i(TAG, "In hand");
//                }
//            }
//        }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * @param activityStartTime
     * @param activityEndTime
     * @param typeId
     * @param steps             makes a post call to store entity data to server
     *                          and updates entity Array accordingly
     */

    private void sendActivityDetailToServer(Date activityStartTime, Date activityEndTime, int typeId, int steps) {
        //TODO change this format at server
        Log.i(TAG, "entity length before send  " + entityArray.length());
//        Toast.makeText(,"entity length before send is "
//                + entityArray.length(),Toast.LENGTH_SHORT).show();
        //TODO remove the below type id assignment
        userId = getUserId();
        Log.i(TAG, "userId is " + userId);
        PostActivityDetailsModel postActivityDetailsModel = new PostActivityDetailsModel(
                activityStartTime, activityEndTime, 1, userId, typeId, steps);

        postActivityDetailsModel.getPostActivityJSON(entityArray);
//        Toast.makeText(getApplicationContext(),"entity length inside send is "
//                + entityArray.length(),Toast.LENGTH_SHORT).show();
        Log.i(TAG, "length inside send " + entityArray.length());
        String activityPayload = entityArray.toString();
        Log.i(TAG, "post activity payload is " + activityPayload);
        String result = PostData.postContent(PostActivityDetailsModel.postActivityDetailURI, activityPayload);
        Log.i(TAG, "postActivity result is " + result);
        if (result.equals(PostData.INVALID_PAYLOAD)) {
            //TODO: get more than just error from server, discarding array for now
            entityArray = new JSONArray();

        } else if (result.equals(PostData.INVALID_RESPONSE) || result.equals(PostData.EXCEPTION)) {
            //Do nothing
        } else {
            Log.i(TAG, "sent activity result is " + result);
            entityArray = new JSONArray();
        }
        Log.i(TAG, "entity length after send is " + entityArray.length());
//        Toast.makeText(getApplicationContext(),"entity length after send is "
//                + entityArray.length(),Toast.LENGTH_SHORT).show();

    }

    private void sendNotificationToServer(Date notificationTime, String message, int typeId, int steps) {
        //TODO change this format at server
        Log.i(TAG, "entity length before send  " + notificationArray.length());
//        Toast.makeText(,"entity length before send is "
//                + entityArray.length(),Toast.LENGTH_SHORT).show();
        //TODO remove the below type id assignment
        userId = getUserId();

        Log.i(TAG, "userId is " + userId);
        PostNotificationModel postNotificationModel = new PostNotificationModel(
                notificationTime, message, 1, userId, typeId, steps);

        postNotificationModel.getPostNotificationJSON(notificationArray);
//        Toast.makeText(getApplicationContext(),"entity length inside send is "
//                + entityArray.length(),Toast.LENGTH_SHORT).show();
        Log.i(TAG, "length inside notification send " + notificationArray.length());
        String notificationPayload = notificationArray.toString();
        Log.i(TAG, "post notification payload is " + notificationPayload);
        String result = PostData.postContent(PostNotificationModel.postNotificationURI, notificationPayload);
        Log.i(TAG, "postActivity notification is " + result);
        if (result.equals(PostData.INVALID_PAYLOAD)) {
            //TODO: get more than just error from server, discarding array for now
            notificationArray = new JSONArray();

        } else if (result.equals(PostData.INVALID_RESPONSE) || result.equals(PostData.EXCEPTION)) {
            //Do nothing
        } else {
            Log.i(TAG, "sent activity result is " + result);
            notificationArray = new JSONArray();
        }
        Log.i(TAG, "entity length after send is " + notificationArray.length());
//        Toast.makeText(getApplicationContext(),"entity length after send is "
//                + entityArray.length(),Toast.LENGTH_SHORT).show();

    }


    public int getUserId() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return preferences.getInt("userId", 0);

    }

    public int getTotalStepsToday(int intermediateStepCount) {
        Date storedDate = new Date(preferences.getLong(TODAY, 0));
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(new Date());
        Date today = cal.getTime();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        if (fmt.format(storedDate).equals(fmt.format(today))) {
            return preferences.getInt(STEPS, 0) + intermediateStepCount;
        } else {
//            SharedPreferences.Editor editor = preferences.edit();
//            editor.putLong(TODAY, today.getTime());
//
            return intermediateStepCount;
//            editor.putInt()


        }

    }

    public int setTotalStepsToday(int delta_value) {
        Date storedDate = new Date(preferences.getLong(TODAY, 0));
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(new Date());
        Date today = cal.getTime();
//        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
//        if (fmt.format(storedDate).equals(fmt.format(today))) {
//            int stepBase = preferences.getInt(STEPS, 0);
//            SharedPreferences.Editor editor = preferences.edit();
//            editor.putLong(STEPS, stepBase + delta_value);
//            editor.commit();
//            return stepBase + delta_value;
//        } else {

        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(TODAY, today.getTime());
        int steps = dbHandler.getDayDataFromActivityLog(today);
        editor.putInt(STEPS, steps);
        editor.commit();
        return steps;
//        }


    }

    /**
     * Set goal for today, updates preference manager to the current date
     * if goal is not set on current day...
     * <p/>
     * Use this at start of service, and notification send time.
     */

    public void setTodayGoal() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Date storedDate = new Date(preferences.getLong(GOALSETDAY, 0));
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        Calendar cal1 = GregorianCalendar.getInstance();
        cal1.setTime(new Date());
        Date today = cal1.getTime();

        if (!fmt.format(storedDate).equals(fmt.format(today))) {
            goalAchievedNotification = false;
            Calendar cal2 = GregorianCalendar.getInstance();
            cal2.setTime(new Date());
            cal2.add(Calendar.DAY_OF_YEAR, -1);
            Date yesterday = cal2.getTime();
            String message;
            int goal;
            int steps = dbHandler.getDayDataFromActivityLog(yesterday);
            int prevGoal = dbHandler.getDayGoal(yesterday);
            if (steps < 4000) {
                goal = 4000;
                if (prevGoal >= goal){
                    goal = (goal + prevGoal)/2;
                }
                float goalRound = goal/1000;
                goal = (int)WeeklyAdapter.round(goalRound, 0)*1000;
                message = "Go for more than " + goal + " steps today";
            } else if (steps < 5000) {
                goal = 5000;
                if (prevGoal > goal){
                    goal = (goal + prevGoal)/2;
                }
                float goalRound = goal/1000;
                goal = (int)WeeklyAdapter.round(goalRound, 0)*1000;
                message = "Go for more than " + goal + " steps today";
            } else if (steps < 6000) {
                goal = 6000;
                if (prevGoal > goal){
                    goal = (goal + prevGoal)/2;
                }
                float goalRound = goal/1000;
                goal = (int)WeeklyAdapter.round(goalRound, 0)*1000;
                message = "Go for more than " + goal + " steps today";
            } else if (steps < 7000) {
                goal = 7000;

                if (prevGoal > goal){
                    goal = (goal + prevGoal)/2;
                }
                float goalRound = goal/1000;
                goal = (int)WeeklyAdapter.round(goalRound, 0)*1000;
                message = "Go for more than " + goal + " steps today";
            } else if (steps < 8000) {
                goal = 8000;
                if (prevGoal > goal){
                    goal = (goal + prevGoal)/2;
                }
                float goalRound = goal/1000;
                goal = (int)WeeklyAdapter.round(goalRound, 0)*1000;
                message = "Doing great! Now strive for " + goal + " steps today";
            } else if (steps < 9000) {
                goal = 9000;

                if (prevGoal > goal){
                    goal = (goal + prevGoal)/2;
                }
                float goalRound = goal/1000;
                goal = (int)WeeklyAdapter.round(goalRound, 0)*1000;
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
            dbHandler.setTableNotificationActivityRecords(message, 0, Calendar.getInstance().getTime());
            showAlert(message);
            sendNotificationToServer(today, message, DetectedActivity.ON_FOOT, goal);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(GOALSETDAY, today.getTime());
            editor.commit();

        }

    }

    public int getTodayGoal() {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(new Date());
        Date today = cal.getTime();
        return dbHandler.getDayGoal(today);
    }

    //
    protected void showAlert(String displayText) {
        // TODO Auto-generated method stub
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, 2, intent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Stand up and Move!!")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(displayText))
                        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                        .setContentText(displayText)
                        .setAutoCancel(true);


        mBuilder.setContentIntent(pIntent);
        mNotificationManager.notify(2, mBuilder.build());
    }


}