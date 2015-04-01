/**
 * Immediate circle update problem...
 */


//package com.xrci.standup.utility;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.support.v4.content.LocalBroadcastManager;
//import android.util.Log;
//
//import com.google.android.gms.location.DetectedActivity;
//import com.xrci.standup.MainActivity;
//
///**
// * Created by q4KV89ZB on 30-03-2015.
// */
//public class TOBEDELETED {
//
//    public void startListeningForCurrentStatus() {
//        LocalBroadcastManager.getInstance(this).registerReceiver((currentStatusIntentReceiver), new IntentFilter(MainActivity.CURRENT_STATUS));
//    }
//private BroadcastReceiver currentStatusIntentReceiver;
//    public void setUpReceiverForCurrentActivity() {
//        currentStatusIntentReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                Log.i(TAG, "inside step service listener");
//                //whichever has startTime > endTime, that means such an activity is going on
//                if (start_time.getTime() > end_time.getTime()) {
//                    updateActivityUI(DetectedActivity.ON_FOOT, start_time
//                            , intermediate_timeperiod, false, true, intermediateStepCount, false, todaysteps, false);
//                }
//                else if(unknownStartTime.getTime() > unknownEndTime.getTime()) {
//                    updateActivityUI(DetectedActivity.UNKNOWN, unknownStartTime
//                            , curr_time.getTime() - unknownStartTime.getTime(), false, false, 0, false, getTotalStepsToday(0), false);
//                }
//                else if(stillStartTime.getTime() > stillEndTime.getTime() ) {
//                    updateActivityUI(DetectedActivity.STILL, stillStartTime, curr_time.getTime()
//                            - stillStartTime.getTime(), false, false, 0, false, getTotalStepsToday(0), false);
//                }
//            }
//        };
//    }
//}


