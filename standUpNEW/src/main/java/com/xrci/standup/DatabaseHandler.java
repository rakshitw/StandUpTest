package com.xrci.standup;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


// Teime period now not kept calculated in the DB, calculated at the runtime.

public class DatabaseHandler extends SQLiteOpenHelper {

    // Database Name
    private static final String DATABASE_NAME = "db_standuo";
    private final static int DB_VERSION = 2;

    // CLUSTER table name
    private static final String TABLE_ACTIVITY_LOG = "tbl_activity_log";
    private static final String TABLE_NOTIFICATION_ACTIVITY_LOG = "tbl_notification_log";
    //SAMPLER table name

    private static final String TABLE_SAMPLES = "tbl_samples";

    // CLUSTER Table Columns names

    private static final String KEY_ACTIVITY = "activity";
    private static final String KEY_START = "start";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_END = "end";
    private static final String NO_OF_STEPS = "nofsteps";
    private static final String TIME_PERIOD = "timeperiod"; // discard time period value and calculate dynamically
    private static final String SYNCED = "synced";


    private static final String ROWID = "ROWID";
    private static final String DISCARDED = "discarded";
    private static final String SITTINGTIME = "sittingtime";
    private static final String MAXSITTINGTIME = "maxsittingtime";
    //Context cont;


    public DatabaseHandler(Context context) {
        //cont=context;
        super(context, DATABASE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {


            // TODO Auto-generated method stub
            String CREATE_LOG_TABLE = "CREATE TABLE " + TABLE_ACTIVITY_LOG + "("
                    + ROWID + " INTEGER PRIMARY KEY," + KEY_ACTIVITY + " INTEGER,"
                    + KEY_START + " TEXT," + KEY_END + " TEXT," + NO_OF_STEPS + " INTEGER," + TIME_PERIOD + " INTEGER," + SYNCED + " INTEGER, " + KEY_TIMESTAMP + " TEXT," + DISCARDED + " INTEGER )";
            db.execSQL(CREATE_LOG_TABLE);


            String CREATE_NOTIFICATION_LOG_TABLE = "CREATE TABLE " + TABLE_NOTIFICATION_ACTIVITY_LOG + "("
                    + ROWID + " INTEGER PRIMARY KEY," + SITTINGTIME + " INTEGER," + MAXSITTINGTIME + " INTEGER," + SYNCED + " INTEGER, " + KEY_TIMESTAMP + " TEXT)";
            db.execSQL(CREATE_NOTIFICATION_LOG_TABLE);


            //db.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        // Drop older table if existed
        // db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLUSTERS);

        // Create tables again
        //onCreate(db);
        if (oldVersion < 2) {
            String CREATE_NOTIFICATION_LOG_TABLE = "CREATE TABLE " + TABLE_NOTIFICATION_ACTIVITY_LOG + "("
                    + ROWID + " INTEGER PRIMARY KEY," + SITTINGTIME + " INTEGER," + MAXSITTINGTIME + " INTEGER," + SYNCED + " INTEGER, " + KEY_TIMESTAMP + " TEXT)";
            db.execSQL(CREATE_NOTIFICATION_LOG_TABLE);
            System.out.println("Came in upgrade");
        }
    }


//	public void addUserActivity(int activity,UserActivity ua,int synced)
//	{
//		SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		SQLiteDatabase db = this.getWritableDatabase();
//
//	    ContentValues values = new ContentValues();
//	    values.put(KEY_ACTIVITY, activity);
//	    values.put(KEY_START, sf.format(ua.startTime));
//	    values.put(KEY_END, sf.format(ua.endTime));
//	    values.put(NO_OF_STEPS, ua.noOfStepsEnd-ua.noOfStepsStart);
//	    values.put(TIME_PERIOD, ua.endTime.getTime()-ua.startTime.getTime());
//	    //values.put(KEY_TIMESTAMP, );
//
//	    values.put(SYNCED, synced);
//	    values.put(DISCARDED, 0);
//	    values.put(KEY_TIMESTAMP, sf.format(Calendar.getInstance().getTime()));
//	    long rowinserted= db.insert(TABLE_ACTIVITY_LOG, null, values);
//		   System.out.println("Row inserted: "+rowinserted);
//		    db.close();
//	}


    public void addUserActivity(int activity, ActivityDetails ua, int synced) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ACTIVITY, activity);
        values.put(KEY_START, sf.format(ua.start));
        values.put(KEY_END, sf.format(ua.end));
        values.put(NO_OF_STEPS, ua.noOfSteps);
        values.put(TIME_PERIOD, ua.timePeriod);
        //values.put(KEY_TIMESTAMP, );

        values.put(SYNCED, synced);
        values.put(DISCARDED, 0);
        values.put(KEY_TIMESTAMP, sf.format(Calendar.getInstance().getTime()));
        long rowinserted = db.insert(TABLE_ACTIVITY_LOG, null, values);
        System.out.println("Row inserted: " + rowinserted);
        db.close();
    }

    public void addNotification(Date notificationTimestamp, int sittingPeriod, int maxsittingPeriod) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_TIMESTAMP, sf.format(notificationTimestamp));

        values.put(SITTINGTIME, sittingPeriod);
        values.put(MAXSITTINGTIME, maxsittingPeriod);
        //values.put(KEY_TIMESTAMP, );

        values.put(SYNCED, 0);
        long rowinserted = db.insert(TABLE_NOTIFICATION_ACTIVITY_LOG, null, values);
        System.out.println("Row inserted: " + rowinserted);
        db.close();
    }


    public void removeOverlappingEntries(Date startTime) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SQLiteDatabase db = this.getReadableDatabase();
        //db.rawQuery("Update tbl_activity_log set synced =1 where ROWID <= "+rowid,null);


        ContentValues values = new ContentValues();
        values.put(SYNCED, 0);
        values.put(DISCARDED, 1);
        values.put(KEY_TIMESTAMP, sf.format(Calendar.getInstance().getTime()));
        //values.put(KEY_PH_NO, contact.getPhoneNumber());

        // updating row
        System.out.println(db.update(TABLE_ACTIVITY_LOG, values, KEY_START + " >= ?",
                new String[]{sf.format(startTime)}));
        db.close();
        updateEndTimeOfOverlappingEntries(startTime);

    }


    public int removeEntriesInWorkingPeriod(Date startTime, Date endTime) {
        int rowsUpdated = 0;
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SQLiteDatabase db = this.getReadableDatabase();
        //db.rawQuery("Update tbl_activity_log set synced =1 where ROWID <= "+rowid,null);


        ContentValues values = new ContentValues();
        values.put(SYNCED, 0);
        values.put(DISCARDED, 1);
        values.put(KEY_TIMESTAMP, sf.format(Calendar.getInstance().getTime()));
        //values.put(KEY_PH_NO, contact.getPhoneNumber());

        // updating row
        rowsUpdated = db.update(TABLE_ACTIVITY_LOG, values, KEY_START + " >= ? and " + KEY_END + " <= ?",
                new String[]{sf.format(startTime), sf.format(endTime)});

        db.close();
        System.out.println("Came in remove Entries in Working period");
        return (rowsUpdated);

    }


    public int removeOverlaps(Date startTime, Date endTime) {
        int rowsUpdated = 0;
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SQLiteDatabase db = this.getReadableDatabase();
        //db.rawQuery("Update tbl_activity_log set synced =1 where ROWID <= "+rowid,null);

        System.out.println("Came in remove Overlaps");

        ContentValues values = new ContentValues();
        values.put(SYNCED, 0);
        values.put(KEY_END, sf.format(startTime));
        values.put(KEY_TIMESTAMP, sf.format(Calendar.getInstance().getTime()));
        //values.put(KEY_PH_NO, contact.getPhoneNumber());

        // updating row
        System.out.println("Start time:" + sf.format(startTime));
        rowsUpdated = db.update(TABLE_ACTIVITY_LOG, values, KEY_START + " <= ? and " + KEY_END + " >= ?",
                new String[]{sf.format(startTime), sf.format(startTime)});
        System.out.println("Rows updated:" + rowsUpdated);
        ContentValues values2 = new ContentValues();
        values2.put(SYNCED, 0);
        values2.put(KEY_START, sf.format(endTime));
        values2.put(KEY_TIMESTAMP, sf.format(Calendar.getInstance().getTime()));
        //values.put(KEY_PH_NO, contact.getPhoneNumber());
        System.out.println("End time:" + sf.format(endTime));
        // updating row
        rowsUpdated += db.update(TABLE_ACTIVITY_LOG, values2, KEY_START + " <= ? and " + KEY_END + " >= ?",
                new String[]{sf.format(endTime), sf.format(endTime)});
        System.out.println("Rows updated:" + rowsUpdated);
        db.close();
        return (rowsUpdated);


    }


    public boolean addWorkingPeriodBetweenExistingEntry(Date startTime, Date endTime) {
        int rowId = 0, activity = 0;
        String start, end = null;
        boolean suchCaseExists = false;
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SQLiteDatabase db = this.getReadableDatabase();
        //db.rawQuery("Update tbl_activity_log set synced =1 where ROWID <= "+rowid,null);
        System.out.println("Came in add Working period");


        Cursor logCursor = db.query(true, TABLE_ACTIVITY_LOG, new String[]{ROWID,
                        KEY_ACTIVITY, KEY_START, KEY_END, TIME_PERIOD, KEY_TIMESTAMP, DISCARDED}, KEY_START + " <= ? and " + KEY_END + " >= ? and " + DISCARDED + " = ?",
                new String[]{sf.format(startTime), sf.format(endTime), String.valueOf(0)}, null, null, null, null);
        if (logCursor != null) {
            if (logCursor.moveToFirst()) {
                rowId = logCursor.getInt(0);
                activity = logCursor.getInt(1);
                start = logCursor.getString(2);
                end = logCursor.getString(3);
                suchCaseExists = true;
            }
        }
        logCursor.close();

        if (suchCaseExists) {
            // Update the endtime of existing entry with the starttime of working interval
            ContentValues values = new ContentValues();
            values.put(SYNCED, 0);
            values.put(KEY_END, sf.format(startTime));
            values.put(KEY_TIMESTAMP, sf.format(Calendar.getInstance().getTime()));
            //values.put(KEY_PH_NO, contact.getPhoneNumber());

            // updating row
            System.out.println(db.update(TABLE_ACTIVITY_LOG, values, ROWID + " = ?",
                    new String[]{String.valueOf(rowId)}));

            // Insert a new row with starttime as workingtimestam endtime and its orignal endtime

            ContentValues values2 = new ContentValues();
            values2.put(KEY_ACTIVITY, activity);
            values2.put(KEY_START, sf.format(endTime));
            values2.put(KEY_END, end);
            values2.put(NO_OF_STEPS, 0);
            values2.put(TIME_PERIOD, 0);
            //values.put(KEY_TIMESTAMP, );

            values2.put(SYNCED, 0);
            values2.put(DISCARDED, 0);
            values2.put(KEY_TIMESTAMP, sf.format(Calendar.getInstance().getTime()));
            long rowinserted = db.insert(TABLE_ACTIVITY_LOG, null, values2);
            System.out.println("Row inserted: " + rowinserted);

        }


        db.close();
        return suchCaseExists;

    }


    public void updateEndTimeOfOverlappingEntries(Date startTime) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SQLiteDatabase db = this.getReadableDatabase();
        //db.rawQuery("Update tbl_activity_log set synced =1 where ROWID <= "+rowid,null);


        ContentValues values = new ContentValues();
        values.put(SYNCED, 0);
        values.put(KEY_END, sf.format(startTime));
        values.put(KEY_TIMESTAMP, sf.format(Calendar.getInstance().getTime()));
        //values.put(KEY_PH_NO, contact.getPhoneNumber());

        // updating row
        System.out.println(db.update(TABLE_ACTIVITY_LOG, values, KEY_END + " >= ?",
                new String[]{sf.format(startTime)}));
        db.close();
    }


    public String fetchUserActivityJSONToBeSynced(String fbid) {
        //ArrayList<String> cells=new ArrayList<String>();
        String jsonString = "";
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONArray jarray = new JSONArray();
        JSONArray jNotificationArray = new JSONArray();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor logCursor = db.query(true, TABLE_ACTIVITY_LOG, new String[]{ROWID,
                        KEY_ACTIVITY, KEY_START, KEY_END, NO_OF_STEPS, TIME_PERIOD, KEY_TIMESTAMP, DISCARDED}, SYNCED + "=?",
                new String[]{String.valueOf(0)}, null, null, null, null);
        if (logCursor != null) {
            if (logCursor.moveToFirst()) {
                do {
                    JSONObject jobj = new JSONObject();
                    try {
                        jobj.put("rowid", logCursor.getInt(0));

                        jobj.put("activity", logCursor.getString(1));
                        String start = logCursor.getString(2);
                        jobj.put("start", start);
                        String end = logCursor.getString(3);
                        jobj.put("end", end);
                        jobj.put("noofsteps", logCursor.getInt(4));
                        jobj.put("timeperiod", sf.parse(end).getTime() - sf.parse(start).getTime());
                        jobj.put("timestamp", logCursor.getString(6));
                        jobj.put("discarded", logCursor.getInt(7));
                        jarray.put(jobj);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }


                } while (logCursor.moveToNext());

            }
        }
        logCursor.close();

        Cursor notificationCursor = db.query(true, TABLE_NOTIFICATION_ACTIVITY_LOG, new String[]{ROWID,
                        SITTINGTIME, MAXSITTINGTIME, KEY_TIMESTAMP,}, SYNCED + "=?",
                new String[]{String.valueOf(0)}, null, null, null, null);
        if (notificationCursor != null) {
            if (notificationCursor.moveToFirst()) {
                do {
                    JSONObject jobj = new JSONObject();
                    try {
                        jobj.put("rowid", notificationCursor.getInt(0));

                        jobj.put("source", "mobile");
                        int sittingtime = notificationCursor.getInt(1);
                        jobj.put("sittingtime", sittingtime);
                        int maxsittingtime = notificationCursor.getInt(2);
                        jobj.put("maxsittingtime", maxsittingtime);
                        String notificationtimestamp = notificationCursor.getString(3);
                        jobj.put("notificationtimestamp", notificationtimestamp);

                        jNotificationArray.put(jobj);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }


                } while (notificationCursor.moveToNext());

            }
        }
        notificationCursor.close();


        db.close();
        if (fbid.length() > 0 && jarray.length() > 0) {
            try {
                JSONObject jO = new JSONObject();
                jO.put("fbid", fbid);
                jO.put("rows", jarray);
                jO.put("notifications", jNotificationArray);
                jsonString = jO.toString();

            } catch (JSONException ex) {

            }
        }

        return jsonString;
    }


    public void updateSyncedStatus(String jsonArrayOfUpdatedIds) {

        SQLiteDatabase db = this.getReadableDatabase();
        db.rawQuery("Update tbl_activity_log set synced =1 where ROWID IN " + jsonArrayOfUpdatedIds.replace("{", "(").replace("}", ")"), null);
        db.close();
    }


    public void deleteNotificationTable() {

        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("delete from " + TABLE_NOTIFICATION_ACTIVITY_LOG);
        System.out.println("deleted");
        db.close();
    }

    public void updateSyncedStatusUptoRowId(int rowid, String lastSynced) {

        SQLiteDatabase db = this.getReadableDatabase();
        //db.rawQuery("Update tbl_activity_log set synced =1 where ROWID <= "+rowid,null);


        ContentValues values = new ContentValues();
        values.put(SYNCED, 1);
        //values.put(KEY_PH_NO, contact.getPhoneNumber());

        // updating row
        System.out.println("In Updation");
        System.out.println(db.update(TABLE_ACTIVITY_LOG, values, ROWID + " <= ? and " + KEY_TIMESTAMP + " <= ?",
                new String[]{String.valueOf(rowid), lastSynced}));
        db.close();
    }

    public void updateSyncedStatusUptoNotificationRowId(int rowid) {

        SQLiteDatabase db = this.getReadableDatabase();
        //db.rawQuery("Update tbl_activity_log set synced =1 where ROWID <= "+rowid,null);


        ContentValues values = new ContentValues();
        values.put(SYNCED, 1);
        //values.put(KEY_PH_NO, contact.getPhoneNumber());

        // updating row
        System.out.println("In Updation");
        System.out.println(db.update(TABLE_NOTIFICATION_ACTIVITY_LOG, values, ROWID + " <= ? ",
                new String[]{String.valueOf(rowid)}));
        db.close();
    }

    public long[] getTimeOfEachActivityToday(Date date) {

        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);


        String startTime = sf.format(date);
        date.setHours(23);
        date.setMinutes(59);
        date.setSeconds(59);
        String endTime = sf.format(date);
        long[] timePeriods = new long[5];
        SQLiteDatabase db = this.getReadableDatabase();
        //Cursor logCursor=db.rawQuery("Select activity,start,end,noofsteps,timeperiod, from tbl_activity_log where end  between \""+startTime+"\" and \""+endTime+"\"",null );
        Cursor logCursor = db.rawQuery("Select activity,sum(strftime('%s',end) - strftime('%s',start)) from tbl_activity_log where end  between \"" + startTime + "\" and \"" + endTime + "\" and discarded=0 group by activity", null);

        if (logCursor != null) {
            if (logCursor.moveToFirst()) {
                do {
                    int activity = logCursor.getInt(0);
                    timePeriods[activity] = logCursor.getLong(1);


                    System.out.println("this" + logCursor.getString(0) + "   " + logCursor.getLong(1));

                } while (logCursor.moveToNext());

            }
        }
        logCursor.close();
        db.close();
        return timePeriods;

    }


    public ArrayList<ActivityDetails> fetchAllActivitiesToday(Date date) throws ParseException {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);


        String startTime = sf.format(date);
        date.setHours(23);
        date.setMinutes(59);
        date.setSeconds(59);
        String endTime = sf.format(date);
        ArrayList<ActivityDetails> userActivities = new ArrayList<ActivityDetails>();
        SQLiteDatabase db = this.getReadableDatabase();
        //Cursor logCursor=db.rawQuery("Select activity,sum(timeperiod) from tbl_activity_log where end  between \""+startTime+"\" and \""+endTime+"\" group by activity",null );
        Cursor logCursor = db.rawQuery("Select activity,start,end,nofsteps,timeperiod from tbl_activity_log where end  between \"" + startTime + "\" and \"" + endTime + "\" and discarded=0 order by end", null);
        //SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (logCursor != null) {
            if (logCursor.moveToFirst()) {
                do {
                    ActivityDetails ad = new ActivityDetails();
                    ad.activityType = logCursor.getInt(0);
                    ad.start = sf.parse(logCursor.getString(1));
                    ad.end = sf.parse(logCursor.getString(2));
                    ad.noOfSteps = logCursor.getInt(3);
                    ad.timePeriod = ad.end.getTime() - ad.start.getTime();
                    userActivities.add(ad);

                    System.out.println(logCursor.getString(1) + "-" + logCursor.getString(2));


                    //System.out.println(logCursor.getString(0)+"   "+ logCursor.getLong(1));

                } while (logCursor.moveToNext());

            }
        }
        logCursor.close();
        db.close();


        return userActivities;

    }

    public int getDayDataFromActivityLog(Date date) {
        int stepCount;
        //Make sure that date format and column format is consistent
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = simpleDateFormat.format(date);
        String fetchDateOnly = dateString.substring(0,10);
        Log.i("cursor_db", fetchDateOnly);
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = new String[] { "%" + fetchDateOnly + "%" };
        Cursor cursor = db.rawQuery("SELECT " + "SUM (" + NO_OF_STEPS + ")" + " FROM "  + TABLE_ACTIVITY_LOG + " WHERE " + KEY_START + " LIKE  ?", selectionArgs);
//        db.close();
        if(cursor.moveToFirst()) {
            Log.i("cursor_log", "today steps are " +  Integer.toString(cursor.getInt(0)));
             stepCount = cursor.getInt(0);
        }
        else
            stepCount = - 1;

        cursor.close();
        db.close();
        return  stepCount;
    }
}
	
