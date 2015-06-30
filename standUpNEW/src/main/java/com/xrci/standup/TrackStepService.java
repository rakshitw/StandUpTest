//package com.xrci.standup;
//
//import android.app.ActivityManager;
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Environment;
//import android.os.IBinder;
//import android.util.Log;
//
//import java.io.BufferedOutputStream;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//
//public class TrackStepService extends Service {
//    String TAG = "TrackStepService";
//    public TrackStepService() {
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.i("TrackStepService" , "Starting at " + Calendar.getInstance().getTime());
//        return START_STICKY;
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        Log.i("TrackStepService" , "tracking step service at " + Calendar.getInstance().getTime());
//        trackAndLogStepService();
//        stopSelf();
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        Log.i("TrackStepService" , "Destroying at " + Calendar.getInstance().getTime());
//    }
//
//    private void trackAndLogStepService() {
//        try {
//            String fname = "xrci_track_stepService";
//            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
//                    + "/XrciStandUpFiles");
//            dir.mkdirs();
//            String filename = Environment.getExternalStorageDirectory().getAbsolutePath() +
//                    "/XrciStandUpFiles/" + fname + ".csv";
//
//            BufferedOutputStream bos = null;
//            try {
//                bos = new BufferedOutputStream(new FileOutputStream(filename, true));
//            } catch (FileNotFoundException e) {
//                Log.e(TAG, e.getMessage());
//                e.printStackTrace();
//            }
//
//
//            String string = null;
//            Boolean isStepServiceOn = isMyServiceRunning(StepService.class);
//            if (isStepServiceOn) {
//                string = sf.format(Calendar.getInstance().getTime()) + "," + "1" + "\n";
//            } else {
//                string = sf.format(Calendar.getInstance().getTime()) + "," + "0" + "\n";
//                Log.i(TAG, "starting step service via tracking service");
//                Intent startStepService = new Intent(getApplicationContext(), StepService.class);
//                startService(startStepService);
//            }
//
//
////        Log.d(TAG, "Writing: " + string);
//
//            try {
//                bos.write(string.getBytes());
//                bos.flush();
//                bos.close();
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private boolean isMyServiceRunning(Class<?> serviceClass) {
//        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        boolean flag = false;
//        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            if (serviceClass.getName().equals(service.service.getClassName())) {
//                flag = true;
//            }
//        }
//        return flag;
//    }
//}
