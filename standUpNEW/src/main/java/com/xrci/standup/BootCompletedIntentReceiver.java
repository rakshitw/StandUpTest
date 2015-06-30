package com.xrci.standup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedIntentReceiver extends BroadcastReceiver {
    String TAG = "StepServiceTracking";
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent pushIntent = new Intent(context, StepService.class);
            context.startService(pushIntent);

            //Tracking service for step service
//            checkAndStartTrackingAlarm(context);
        }


    }

    /**
     *
     /**
     * make sure that time for alarm is same in MainActivity.class also
     * @param context
     */
//    private void checkAndStartTrackingAlarm(Context context) {
//        try {
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

}