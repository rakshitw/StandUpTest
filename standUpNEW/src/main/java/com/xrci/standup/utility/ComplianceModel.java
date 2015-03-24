package com.xrci.standup.utility;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by q4KV89ZB on 24-03-2015.
 */
public class ComplianceModel {
    public static String baseURI = "http://64.49.234.131:8080/standup/rest/fusedActivity/getCompliance/";
    public static String baseWeekURI = "http://64.49.234.131:8080/standup/rest/fusedActivity/getComplianceWeek/";

    public ComplianceModel(Date date, int userId) {
        this.date = date;
        this.userId = userId;
    }

    Date date;
    int userId;

    public String getCompliance(){
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
        String dateString = sdf.format(date);
        String uri = baseURI + userId + "/" + dateString;
        Log.i("check", "compliance uri is " + uri);
        String response = GetData.getComplianceContent(uri);
        Log.i("check", "compliance response is " + response);

        return  response;
    }

    public String getWeekCompliance(){
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
        String dateString = sdf.format(date);
        String uri = baseWeekURI + userId + "/" + dateString;
        Log.i("check", "compliance uri is " + uri);
        String response = GetData.getComplianceContent(uri);
        Log.i("check", "compliance response is " + response);

        return  response;
    }

}
