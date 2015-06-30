package com.xrci.standup.utility;

import android.util.Log;

import com.xrci.standup.ActivityDetails;
import com.xrci.standup.DatabaseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by q4KV89ZB on 15-03-2015.
 */
public class FusedDataModel {

    public static String baseURI = "http://64.49.234.131:8080/standup/rest/fusedActivity/getTimeLine";


    public static String getFusedData(Date startDate, int userId, DatabaseHandler dbHandler) {
        startDate.setHours(0);
        startDate.setMinutes(0);
        startDate.setSeconds(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
        String startTimeString = sdf.format(startDate);
        String endTimeString = sdf.format(Calendar.getInstance().getTime());
        String url = baseURI +"/" + userId     + "/" + startTimeString + "/" + endTimeString;
        Log.i("check", "fused url is " + url);
        JSONArray jsonArray;
        String response = GetData.getContent(url);

        try {
            jsonArray = new JSONArray(response);
            if (jsonArray.length() > 0){
                dbHandler.clearFusedUserActivity(startDate);

            }
            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                parseAndStoreDate(jsonObject, dbHandler);
            }


            Log.i("check", "JSON array length is " + jsonArray.length());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return response;

    }

    public static String getFusedDataForMainTimeline(Date startDate, int userId, DatabaseHandler dbHandler) {
        startDate.setHours(0);
        startDate.setMinutes(0);
        startDate.setSeconds(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
        String startTimeString = sdf.format(startDate);
        String endTimeString = sdf.format(Calendar.getInstance().getTime());
        String url = baseURI +"/" + userId     + "/" + startTimeString + "/" + endTimeString;
        Log.i("check", "fused url for mainTimeline is " + url);
        JSONArray jsonArray;
        String response = GetData.getContent(url);

        try {
             jsonArray = new JSONArray(response);
             Log.i("check", "json array length for main is " + jsonArray.length());
             if (jsonArray.length() > 0){
                 JSONObject jsonObject = jsonArray.getJSONObject(0);
                 String lastTimeForFuseData =  jsonObject.getString("endTime");
                 SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
                 try {
                     Date lastDate = simpleDateFormat.parse(lastTimeForFuseData);
                     Log.i("check", "main timeline to be deleted" + "last date is " + lastDate);
                     dbHandler.deleteFromActivityTable(lastDate);
                 } catch (ParseException e) {
                     e.printStackTrace();
                 }


             }
             for (int i = 0; i < jsonArray.length(); i++){
                 JSONObject jsonObject = jsonArray.getJSONObject(i);
                 Log.i("check", "parsing and storing for main timeline");
                 parseAndStoreDateForMainTimeline(jsonObject, dbHandler);
             }


            Log.i("check", "JSON array length is " + jsonArray.length());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return response;

    }




    public static void parseAndStoreDateForMainTimeline(JSONObject jsonObject , DatabaseHandler dbHandler){
        try {

            String endTimeString =  jsonObject.getString("endTime");
            String startTimeString =  jsonObject.getString("startTime");
            int steps = jsonObject.getInt("steps");
            int type = jsonObject.getJSONObject("type").getInt("id");

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
            try {
                Date endTime = simpleDateFormat.parse(endTimeString);
                Date startTime = simpleDateFormat.parse(startTimeString);
                ActivityDetails activityDetails = new ActivityDetails(type, endTime.getTime() - startTime.getTime()
                        , startTime, endTime,steps);
                dbHandler.addUserActivity(type, activityDetails, 0);
                //Store in fusion table and update UI

            } catch (ParseException e) {
                e.printStackTrace();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void parseAndStoreDate(JSONObject jsonObject, DatabaseHandler dbHandler){
        try {

            String endTimeString =  jsonObject.getString("endTime");
            String startTimeString =  jsonObject.getString("startTime");
            int steps = jsonObject.getInt("steps");
            int type = jsonObject.getJSONObject("type").getInt("id");

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
            try {
                Date endTime = simpleDateFormat.parse(endTimeString);
                Date startTime = simpleDateFormat.parse(startTimeString);
                ActivityDetails activityDetails = new ActivityDetails(type, endTime.getTime() - startTime.getTime()
                        , startTime, endTime,steps);
                dbHandler.addFusedUserActivity(type, activityDetails, 0);
                //Store in fusion table and update UI

            } catch (ParseException e) {
                e.printStackTrace();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
