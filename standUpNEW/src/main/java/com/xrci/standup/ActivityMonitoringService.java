//package com.xrci.standup;
//
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import android.app.Notification;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.SharedPreferences;
//import android.content.SharedPreferences.Editor;
//import android.hardware.SensorManager;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.os.PowerManager;
//import android.os.PowerManager.WakeLock;
//import android.preference.PreferenceManager;
//import android.support.v4.app.NotificationCompat;
//import android.support.v4.content.LocalBroadcastManager;
//import android.widget.Toast;
//
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GooglePlayServicesClient;
//import com.google.android.gms.location.ActivityRecognitionClient;
//import com.google.android.gms.location.DetectedActivity;
//
//public class ActivityMonitoringService extends Service implements GooglePlayServicesClient.ConnectionCallbacks,GooglePlayServicesClient.OnConnectionFailedListener,StepListener  {
//
//	static final public String UPDATE_CURRENT_FRAGMENT = "com.xrci.standup.update_fragment";
//	static final public String REFRESH_TIMELINE = "refresh";
//	static final public String REFRESH_TIMELINE_ONLY_STOP_LISTENING = "refresh_only";
//	static final public String ACTIVITY = "activity";
//	static final public String SINCE="since";
//	static final public String UPDATE_STEPS_ONLY = "updatestepsonly";
//	static final public String STEPS = "steps";
//	static final public String TIME_PERIOD = "timeperiod";
//	public static boolean isMonitoring=false;
//	private ActivityRecognitionClient arclient;
//	 private PendingIntent pIntent;
//	 private BroadcastReceiver receiver;
//	 private SensorManager mSensorManager;
//	 private StepDetector mStepDetector;
//	 private BroadcastReceiver receiverGCM;
//
//
//
//	 boolean stepDetectionOn=false;
//	 int steps=0;
//	 String fbid;
//	 protected final long stillthreshhold=30*60*1000;
//	 static long oneminuteWindowStart=0;
//
//	 DatabaseHandler dbHandler;
//	 UserActivity[] userActivities=new UserActivity[4];
//
//	  Date last_notification=null;
//
//	 static int timeCounter=0;
//	 int prevActivity=3;// Still
//	// Date lastTimestamp;
//	 private int activity;
//	 Date prevTimeStamp;
//	private LocalBroadcastManager broadcaster;
//	protected UserActivity currentActivity;
//	private NotificationManager mNotificationManager;
//	private WakeLock mWakeLock;
//	private int workplaceStartHour;
//	private int workplaceStartMin;
//	private int workplaceEndHour;
//	private int workplaceEndMin;
//	private boolean alertAtOffice;
//	private int maximumSittingTimeInMilliSeconds;
//	private int workplaceStartTotalMins;
//	private int workplaceEndTotalMins;
//	private static boolean mutexLock=false,updateFromGcmLock=false;
//
//
//	@Override
//	public void onCreate() {
//		// TODO Auto-generated method stub
//		try
//		{
//		super.onCreate();
//		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//		//userName=preferences.getString("name", "");
//		last_notification=Calendar.getInstance().getTime();
//		fbid=preferences.getString("fbid","");
//		if(fbid.isEmpty())
//		{
//			isMonitoring=false;
//			stopSelf();
//
//		}
//		  PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//		  mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
//		  mWakeLock.acquire();
//
//		broadcaster = LocalBroadcastManager.getInstance(this);
//		isMonitoring=true;
//		Logger.appendLog("Service started", true);
//
//		dbHandler=new DatabaseHandler(this);
//		String pendingActivityJson=preferences.getString("activitypending", "");
//		if(pendingActivityJson.length()>1)
//		handlePendingActivity(pendingActivityJson);
//		SharedPreferences.Editor editor = preferences.edit();
//        editor.putString("activitypending", "");
//        editor.commit();
//        fetchWorkplaceSettings();
//		  prevTimeStamp=new Date();
//		  prevTimeStamp=Calendar.getInstance().getTime();
//		  for(int i=0;i<4;i++)
//		  {
//			  userActivities[i]=new UserActivity();
//			  userActivities[i].startTime=new Date();
//			  userActivities[i].endTime=new Date();
//		  }
//		  arclient = new ActivityRecognitionClient(this, this, this);
//		   arclient.connect();
//		   mStepDetector = new StepDetector();
//		   mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//		   mStepDetector.addStepListener(this);
//
//		   final SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		   receiver = new BroadcastReceiver() {
//
//
//
//
//		 	@Override
//		       public void onReceive(Context context, Intent intent) {
//		 		try
//		 		{
//		 			if(SettingsActivity.settingsUpdated)
//		 			{
//		 				fetchWorkplaceSettings();
//		 				SettingsActivity.settingsUpdated=false;
//		 			}
//
//		 		 long timePeriod = 0;
//		 		// boolean sittingPeriodRedColor=false;
//		     	   activity=intent.getIntExtra("Activity",DetectedActivity.UNKNOWN);
//		     	   boolean timelineRefresh=false;
//		     	  //int confidence=intent.getExtras().getInt("Confidence");
//		     	  if(activity==DetectedActivity.IN_VEHICLE)
//		     	  {
//		     		  activity=DetectedActivity.STILL;
//		     	  }
//		     	  else if (activity==DetectedActivity.TILTING || activity==DetectedActivity.UNKNOWN) // Consider the previos activity if detected activity is unlnown or tiliting
//		     		  activity=prevActivity;
//		     	  prevActivity=activity;
//		     	  Date currentTimeStamp=Calendar.getInstance().getTime();
//		     	 // System.out.println("DETECTION");
//
//
//
//
//		     	  currentActivity=userActivities[activity];
//		     	 if(!currentActivity.isActive)
//		     	 {
//		     		 currentActivity.startTime=currentTimeStamp;
//		     		 currentActivity.endTime=currentTimeStamp;
//		     		 currentActivity.isActive=true;
//		     		 currentActivity.noOfStepsStart=steps;
//		     		 currentActivity.previousActivitiesLogged=false;
//		     		 if(activity==DetectedActivity.ON_FOOT )
//		        	  	{
//		     			 if(!stepDetectionOn)
//		        		  {
//
//		        			  mSensorManager.registerListener(mStepDetector,
//		        		          SensorManager.SENSOR_ACCELEROMETER |
//		        		          SensorManager.SENSOR_MAGNETIC_FIELD |
//		        		          SensorManager.SENSOR_ORIENTATION,
//		        		          SensorManager.SENSOR_DELAY_FASTEST);
//		        			  stepDetectionOn=true;
//		        		  }
//		        	  	}
//
//
//
//
//		     	 }
//		     	 else
//		     	 {
//		     		 currentActivity.endTime=currentTimeStamp;
//		     		 currentActivity.noOfStepsEnd=steps;
//		     		  timePeriod=currentActivity.endTime.getTime()-currentActivity.startTime.getTime();
//
//		     		// if(!currentActivity.previousActivitiesLogged &&  timePeriod> 30*1000 )
//		     		  if(timePeriod> 30*1000)
//		     		 {
//		     			 timelineRefresh=logPreviouslyActiveActivity(activity);
//		     			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//		     			JSONObject jobj=new JSONObject();
//		     			jobj.put("activity", activity);
//		     			jobj.put("starttime",sf.format(currentActivity.startTime));
//		     			jobj.put("endtime",sf.format(currentActivity.endTime));
//		     			jobj.put("noOfStepsEnd", currentActivity.noOfStepsEnd);
//		     			jobj.put("noOfStepsStart", currentActivity.noOfStepsStart);
//		     			//System.out.println("Written"+jobj.toString());
//		     			SharedPreferences.Editor editor = preferences.edit();
//		     	        editor.putString("activitypending", jobj.toString());
//		     	        editor.commit();
//
//		     			 //currentActivity.previousActivitiesLogged=true;
//		     		 }
//		     		 if(activity==DetectedActivity.STILL && timePeriod>maximumSittingTimeInMilliSeconds )
//		     		 {
//
//		     		 if(alertAtOffice)
//		     		 {
//		     			int currentTimeHours=currentTimeStamp.getHours();
//			     		 int currentTimeMins=currentTimeStamp.getMinutes();
//			     		 int currentTimeTotalMinutes=(currentTimeHours*60)+currentTimeMins;
//			     		 //System.out.println("Came here outside "+currentTimeTotalMinutes+ " "+ workplaceStartTotalMins+" ");
//		     			 if(currentTimeTotalMinutes>=workplaceStartTotalMins && currentTimeTotalMinutes<=workplaceEndTotalMins)
//		     			 {
//		     				//System.out.println("Came here inside");
//		     				 if(currentTimeStamp.getTime()-last_notification.getTime()>maximumSittingTimeInMilliSeconds)
//		     					 {
//		     					last_notification=currentTimeStamp;
//		     					 showAlert(timePeriod);
//		     					 //System.out.println("ading notification");
//		     					 dbHandler.addNotification(currentTimeStamp, (int)timePeriod/1000, maximumSittingTimeInMilliSeconds/60000);
//		     					 }
//		     			 }
//		     		 }else
//		     		 {
//
//		     			 //System.out.println("timePeriod:"+timePeriod);
//		     			 //showAlert();
//		     			 //sittingPeriodRedColor=true;
//		     		 if(currentTimeStamp.getTime()-last_notification.getTime()>maximumSittingTimeInMilliSeconds)
//		     		 	{last_notification=currentTimeStamp;
//		     			 showAlert(timePeriod);
//		     			 dbHandler.addNotification(currentTimeStamp, (int)timePeriod/1000, maximumSittingTimeInMilliSeconds/60000);
//
//		     		 	}
//		     		 }
//		     		 }
//		     	 }
//		     	 //System.out.println("red"+sittingPeriodRedColor);
//		     	 //updateCurrentFragment(activity,confidence,timePeriod,sittingPeriodRedColor);
//		     	 //System.out.println("Current Activity:"+ActivityRecognitionService.getType(activity));
//		     	updateActivityUI(activity,currentActivity.startTime,timePeriod,timelineRefresh,false,steps,false);
//
//
//
//
//
//
//
//
//
//		     	if(!userActivities[DetectedActivity.ON_FOOT].isActive && stepDetectionOn )
//		     	{
//		     		mSensorManager.unregisterListener(mStepDetector);
//		     		stepDetectionOn=false;
//
//		     	}
//		 		}catch(Exception e)
//		 		{
//		 			e.printStackTrace();
//		 			Logger.appendLog("Exception in onReceive(ActivityMonitoringService)"+e.getMessage(), true);
//		 		}
//		       }
//		     };
//
//		    IntentFilter filter = new IntentFilter();
//		    filter.addAction("com.xrci.standup.ACTIVITY_RECOGNITION_DATA");
//		    registerReceiver(receiver, filter);
//		    //setUpReceiverFromGCMService();
//		    //startListeningFromGCMService();
//		    mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//		    showRecordingNotification();
//		}
//		catch(Exception e)
//		{
//			Logger.appendLog("Exception in onCreate(ActivityMonitoringService)"+e.getMessage(), true);
//		}
//	}
//
//
//	protected void showAlert(long timePeriod) {
//		// TODO Auto-generated method stub
//		Intent intent = new Intent(this, MainActivity.class);
//		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//		PendingIntent pIntent = PendingIntent.getActivity(this, 2, intent, 0);
//	    String displayText="Still for "+(int)timePeriod/60000+" minutes";
//
//	    NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(this)
//        .setSmallIcon(R.drawable.ic_launcher)
//        .setContentTitle("Stand up and Move!!")
//        .setStyle(new NotificationCompat.BigTextStyle()
//        .bigText(displayText))
//        .setDefaults(Notification.DEFAULT_SOUND| Notification.DEFAULT_VIBRATE)
//        .setContentText(displayText)
//        .setAutoCancel(true);
//
//
//        mBuilder.setContentIntent(pIntent);
//        mNotificationManager.notify(2, mBuilder.build());
//
//
//
//	}
//
//
//	void fetchWorkplaceSettings()
//	{
//		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//
//		 workplaceStartHour=preferences.getInt("workplaceStartHour",10);
//		 workplaceStartMin=preferences.getInt("workplaceStartMin",0);
//		 workplaceStartTotalMins=(workplaceStartHour*60)+workplaceStartMin;
//
//		 workplaceEndHour=preferences.getInt("workplaceEndHour",18);
//		 workplaceEndMin=preferences.getInt("workplaceEndMin",0);
//		 workplaceEndTotalMins=(workplaceEndHour*60)+workplaceEndMin;
//		 alertAtOffice=preferences.getBoolean("alertAtOffice", true);
//		//checkBoxOfficeTimeOnly.setChecked(alertAtOffice);
//
//		int  maximumSittingTime=preferences.getInt("maximumSittingTime", 40);
//		maximumSittingTimeInMilliSeconds=maximumSittingTime*60000;
//	}
//
//
//
//	 private void handlePendingActivity(String pendingActivityJson) {
//		// TODO Auto-generated method stub
//		 System.out.println("in handlepending"+pendingActivityJson);
//		 SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		 try {
//			JSONObject obj=new JSONObject(pendingActivityJson);
//			UserActivity ua=new UserActivity();
//			int activity=obj.getInt("activity");
//			ua.startTime=sf.parse(obj.getString("starttime"));
//			ua.endTime=sf.parse(obj.getString("endtime"));
//			ua.noOfStepsStart=obj.getInt("noOfStepsStart");
//			ua.noOfStepsEnd=obj.getInt("noOfStepsEnd");
//			dbHandler.addUserActivity(activity, ua,0);
//			updateActivityUI(0,null,0,false,false,0,true);
//
//
//
//
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//
//
//
//
//	protected boolean logPreviouslyActiveActivity(int currentActivity) {
//			// TODO Auto-generated method stub
//		 boolean updateRequired=false;
//		 if(!mutexLock && !updateFromGcmLock)
//		 {
//		 try
//		 {
//
//			mutexLock=true;
//		   SimpleDateFormat sf=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
//			 for(int i=1;i<4;i++) // 0 is Vehicle which will not occur now
//			 {
//				 if(i!=currentActivity)
//				 {
//					 if(userActivities[i].isActive)
//					 {
//						 if(userActivities[currentActivity].startTime.before(userActivities[i].endTime))
//							 userActivities[i].endTime=userActivities[currentActivity].startTime;
//						 else
//							 userActivities[currentActivity].startTime=userActivities[i].endTime;
//
//						 if(userActivities[i].endTime.getTime()-userActivities[i].startTime.getTime()>30 *1000)
//						 {
//							dbHandler.addUserActivity(i, userActivities[i],0);
//							//String toBeUpdated=dbHandler.fetchUserActivityJSONToBeSynced(fbid);
//							//System.out.println(dbHandler.fetchUserActivityJSONToBeSynced(mobileNo));
//							updateRequired=true;
//							SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//
//			     			SharedPreferences.Editor editor = preferences.edit();
//			     	        editor.putString("activitypending", "");
//			     	        editor.commit();
//							sendUpdateToServer();
//							//Logger.appendLog(dbHandler.fetchUserActivityJSONToBeSynced(mobileNo),true);
//
//						 }
//
//
//
//					 }
//					 userActivities[i].isActive=false;
//
//				 }
//			 }
//		 }
//		 catch(Exception e)
//		 {
//			 Logger.appendLog("Exception in logPreviouslyActiveActivity(ActivityMonitoringService)"+e.getMessage(), true);
//		 }
//		 finally
//		 {
//			//return lastTimeStamp;
//			 mutexLock=false;
//		 }
//
//			 return updateRequired;
//		 }
//		 else
//			 return false;
//
//		}
//	@Override
//	public IBinder onBind(Intent intent) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	@Override
//	public void onStep() {
//		// TODO Auto-generated method stub
//		try
//		{
//		steps++;
//		updateActivityUI(0,null,0,false,true,steps-userActivities[DetectedActivity.ON_FOOT].noOfStepsStart,false);
//		}
//		catch(Exception e)
//		{
//			 Logger.appendLog("Exception in onStep(ActivityMonitoringService)"+e.getMessage(), true);
//
//		}
//	}
//	@Override
//	public void passValue() {
//		// TODO Auto-generated method stub
//
//	}
//	@Override
//	public void onConnectionFailed(ConnectionResult arg0) {
//		// TODO Auto-generated method stub
//
//	}
//	@Override
//	public void onConnected(Bundle arg0) {
//		// TODO Auto-generated method stub
//		try
//		{
//		Intent intent = new Intent(this, ActivityRecognitionService.class);
//		pIntent = PendingIntent.getService(this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
//		arclient.requestActivityUpdates(1000, pIntent);
//		}
//		catch(Exception e)
//		{
//			Logger.appendLog("Exception in onConnected(ActivityMonitoringService)"+e.getMessage(), true);
//
//		}
//
//	}
//	@Override
//	public void onDisconnected() {
//		// TODO Auto-generated method stub
//
//	}
//
//
//	@Override
//	public void onDestroy() {
//		// TODO Auto-generated method stub
//		try
//		{
//		super.onDestroy();
//
//		// Insert code to log the current activity
//		if(arclient.isConnected())
//			{
//			arclient.removeActivityUpdates(pIntent);
//			pIntent.cancel();
//			arclient.disconnect();
//
//			}
//		isMonitoring=false;
//		if(receiver!=null)
//		unregisterReceiver(receiver);
//
//
//		if(mNotificationManager!=null)
//		mNotificationManager.cancel(1);
//
//		if (stepDetectionOn )
//			mSensorManager.unregisterListener(mStepDetector);
//
//		if(mWakeLock!=null)
//			mWakeLock.release();
//		}catch(Exception e)
//		{
//			Logger.appendLog("Exception in onDestroy(ActivityMonitoringService)"+e.getMessage(), true);
//
//		}
//
//		//handleActiveActivities();
//
//
//			}
//	/*
//	private void handleActiveActivities() {
//		// TODO Auto-generated method stub
//		boolean updateRequired=false;
//		 SimpleDateFormat sf=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
//		 for(int i=0;i<4;i++)
//		 {
//
//
//
//					 if(userActivities[i].endTime.getTime()-userActivities[i].startTime.getTime()>30 *1000)
//					 {
//						dbHandler.addUserActivity(i, userActivities[i],0);
//						String toBeUpdated=dbHandler.fetchUserActivityJSONToBeSynced(fbid);
//						//System.out.println(dbHandler.fetchUserActivityJSONToBeSynced(mobileNo));
//						updateRequired=true;
//						System.out.println(toBeUpdated);
//						//updateServer(toBeUpdated);
//						//Logger.appendLog(dbHandler.fetchUserActivityJSONToBeSynced(mobileNo),true);
//
//					 }
//
//
//
//
//				 userActivities[i].isActive=false;
//
//			 }
//		// if(updateRequired)
//			 updateActivityUI(0,null,0,false,false,0,true);
//
//
//
//
//	}
//
//
//*/
//
//	void updateActivityUI(int activity2, Date startTime, long timePeriod, boolean refreshTimeline,boolean updateStepsOnly, int steps,boolean refreshtimeLineOnly)
//	{
//		try{
//			SimpleDateFormat sf=new SimpleDateFormat("HH:mm");
//		    Intent intent = new Intent(UPDATE_CURRENT_FRAGMENT);
//		    intent.putExtra(REFRESH_TIMELINE, refreshTimeline);
//		    intent.putExtra(ACTIVITY, activity2);
//		    if(startTime!=null)
//		    intent.putExtra(SINCE,sf.format(startTime));
//		    intent.putExtra(TIME_PERIOD,(long) timePeriod);
//		    intent.putExtra(UPDATE_STEPS_ONLY, updateStepsOnly);
//		    intent.putExtra(REFRESH_TIMELINE_ONLY_STOP_LISTENING, refreshtimeLineOnly); // Sent when Monitoring stops, deactivate listener
//		    intent.putExtra(STEPS, steps);
//		    //System.out.println("TimePeriod"+timePeriod);
//
//		    broadcaster.sendBroadcast(intent);
//		}
//		catch(Exception e)
//		{
//			Logger.appendLog("Exception in updateActivityUI(ActivityMonitoringService)"+e.getMessage(), true);
//
//		}
//
//
//	}
//
//
//	private void showRecordingNotification(){
//	    //Notification not = new Notification(R.drawable.ic_launcher, "Standup Monitoring", System.currentTimeMillis());
//	    //PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), Notification.FLAG_ONGOING_EVENT);
//	    //not.flags = Notification.FLAG_ONGOING_EVENT|Notification.FLAG_NO_CLEAR;
//	    //not.defaults=Notification.DEFAULT_SOUND| Notification.DEFAULT_VIBRATE;
//	    //not.setLatestEventInfo(this, "Stand Up", "Sensors Monitoring your Activity !", contentIntent);
//	    Intent intent = new Intent(this, MainActivity.class);
//		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//		PendingIntent pIntent = PendingIntent.getActivity(this, 1, intent, 0);
//
//
//	    NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(this)
//        .setSmallIcon(R.drawable.ic_launcher)
//        .setContentTitle("Stand up")
//        .setStyle(new NotificationCompat.BigTextStyle()
//        .bigText("Sensors Monitoring your Activity !"))
//        //.setDefaults(Notification.DEFAULT_SOUND| Notification.DEFAULT_VIBRATE)
//        .setContentText("Sensors Monitoring your Activity !")
//        .setAutoCancel(false)
//        .setOngoing(true);
//
//        mBuilder.setContentIntent(pIntent);
//        mNotificationManager.notify(1, mBuilder.build());
//	    //mNotificationManager.notify(1, not);
//	}
//
//
//	/*
//	private void setUpReceiverFromGCMService() {
//		// TODO Auto-generated method stub
//		receiverGCM=new  BroadcastReceiver()
//		{
//
//			@Override
//			public void onReceive(Context context, Intent intent) {
//				// TODO Auto-generated method stub
//				String messageFromGCM=intent.getStringExtra(GcmIntentService.GCM_MESSAGE);
//				parseMessageFromGCM(messageFromGCM);
//
//			}
//		};
//
//	}*/
//
//	/*
//	 private void parseMessageFromGCM(String string) {
//			// TODO Auto-generated method stub
//		 SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//		 System.out.println("message from gcm: in Service now"+string);
//	    		try
//	    		{
//	    			JSONObject jobj=new JSONObject(string);
//	    			String type=jobj.getString("type");
//	    			JSONObject data=jobj.getJSONObject("data");
//	    			if(type.contentEquals("start"))
//	    			{
//	    				Date lastWorkingStartTime=sf.parse(data.getString("starttime"));
//	    				Date lastWorkingEndTime=sf.parse(data.getString("endtime"));
//	    				//startMonitoringAfterWorking(lastWorkingStartTime,lastWorkingEndTime);
//	    			   storeWorkingTime(lastWorkingStartTime,lastWorkingEndTime);
//
//
//	    			}
//	    			else if(type.contentEquals("stop"))
//	    			{
//	    				Date workingStartTime=sf.parse(data.getString("starttime"));
//	    				//stopMonitoringAndShowWorking(workingStartTime);
//	    			}
//	    			else if(type.contentEquals("alert"))
//	    			{
//	    				Date workingSinceTimeStamp=sf.parse(data.getString("starttime"));
//	    				int notificationNumber=data.getInt("notificationnumber");
//	    				//showAlertAndUpdateNotification(workingSinceTimeStamp,notificationNumber);
//	    			}
//
//
//	    		}
//	    		catch(Exception ex)
//	    		{
//	    			System.out.println(ex.getMessage());
//	    		}
//		}
//*/
//	void storeWorkingTime(Date lastWorkingStartTime,Date lastWorkingEndTime)
//	{
//		try
//		{
//		updateFromGcmLock=true;
//
//		for(int i=1;i<4;i++) // 0 is Vehicle which will not occur now
//		 {
//			if(userActivities[i].isActive)
//			{
//				if(userActivities[i].startTime.getTime()>lastWorkingEndTime.getTime())
//					break;
//				 if(userActivities[i].endTime.getTime()-userActivities[i].startTime.getTime()>30 *1000)
//				 {
//					dbHandler.addUserActivity(i, userActivities[i],0);
//				 }
//				 userActivities[i].isActive=false;
//			}
//
//		 }
//
//		if (dbHandler.addWorkingPeriodBetweenExistingEntry(lastWorkingStartTime, lastWorkingEndTime))
//		    //{
//		{
//			// addWorkingToDB;
//		}
//		else
//		{
//			dbHandler.removeEntriesInWorkingPeriod(lastWorkingStartTime, lastWorkingEndTime);
//			dbHandler.removeOverlaps(lastWorkingStartTime, lastWorkingEndTime);
//		}
//		storeWorkingActivity(lastWorkingStartTime,lastWorkingEndTime);
//		updateFromGcmLock=false;
//		//updateActivityUI(0,null,0,false,false,0,true);
//		//sendUpdateToServer();
//		}
//		catch(Exception e)
//		{
//			Logger.appendLog("Exception in storeWorkingTime(ActivityMonitoringService)"+e.getMessage(), true);
//
//		}
//
//	}
//
//
//	private void storeWorkingActivity(Date lastWorkingStartTime,
//			Date lastWorkingEndTime) {
//		// TODO Auto-generated method stub
//		try
//		{
//		UserActivity ua=new UserActivity();
//		ua.startTime=lastWorkingStartTime;
//		ua.endTime=lastWorkingEndTime;
//
//		//dbHandler.addUserActivity(utils.ACTIVITY_WORKING, ua,0);
//		dbHandler.addUserActivity(utils.ACTIVITY_WORKING, ua,1);// add the activity but it need not be synced as it is already coming from server
//		}
//		catch(Exception e)
//		{
//			Logger.appendLog("Exception in storeWorkingActivity(ActivityMonitoringService)"+e.getMessage(), true);
//
//		}
//
//	}
//
//	/*
//	 void startListeningFromGCMService()
//		{
//			LocalBroadcastManager.getInstance(this).registerReceiver((receiverGCM), new IntentFilter(GcmIntentService.GCM_MESSAGE_INTENT));
//
//		}
//		*/
//
//	 protected void sendUpdateToServer() {
//			// TODO Auto-generated method stub
//
//			AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
//
//				String response ;
//				int successTag=0;
//
//
//				@Override
//				protected Void doInBackground(Void... arg0) {
//
//						//System.out.println("obj="+obj.toString());
//					try
//					{
//					SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//					String fbid = preferences.getString("fbid","");
//
//					String jsonBody=dbHandler.fetchUserActivityJSONToBeSynced(fbid);
//					System.out.println(jsonBody);
//
//					if(jsonBody.length()>1)
//					{
//					response = PostData.postContent(utils.SERVER_UPDATE_URI,jsonBody);
//					Date lastSynced=Calendar.getInstance().getTime();
//
//					System.out.println("Tag:"+response);
//					if(response.contains("exception"))
//						successTag=0;
//					else if(response.contains("successupto"))
//					{
//						Editor editor=preferences.edit();
//						SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//						String lastSyncedString=sf.format(lastSynced);
//						editor.putString("lastSynced",lastSyncedString);
//						successTag=1;
//						JSONObject obj=new JSONObject(response);
//						int rowsupdatedupto=obj.getInt("successupto");
//						dbHandler.updateSyncedStatusUptoRowId(rowsupdatedupto,lastSyncedString);
//						dbHandler.deleteNotificationTable();
//						JSONArray rowsFromPc=obj.getJSONArray("rowsfrompc");
//						if(rowsFromPc.length()>0)
//							processRowsFromPc(rowsFromPc);
//					}
//					}
//					else
//						successTag=1;
//					} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					successTag=0;
//					Logger.appendLog("Exception in sendUpdateToServer(ActivityMonitoringService)"+e.getMessage(), true);
//
//
//					//jobUuid="exception";
//				}
//				return null;
//			}
//				@Override
//				protected void onPostExecute(Void result) {
//
//						if(successTag==1)
//						{
//						//updateSuggestStatsLayouts(response)	;
//
//
//
//
//						}
//						else
//						{
//
//							Toast.makeText(getApplicationContext(), "Unable to contact Server", Toast.LENGTH_SHORT).show();
//						}
//
//
//
//						//b.setEnabled(true);
//
//					}
//
//
//			};
//			task.execute((Void[])null);
//
//		}
//
//
//
//
//	protected void processRowsFromPc(JSONArray rowsFromPc) {
//		SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		for(int i=0;i<rowsFromPc.length();i++)
//		{
//			try {
//				JSONObject obj=rowsFromPc.getJSONObject(i);
//				Date workingStartTime=sf.parse(obj.getString("start"));
//				Date workingEndTime=sf.parse(obj.getString("end"));
//				storeWorkingTime(workingStartTime,workingEndTime);
//
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		}
//
//		updateActivityUI(0,null,0,false,false,0,true);
//		sendUpdateToServer();
//
//	}
//}
