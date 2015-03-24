package com.xrci.standup.utility;

import android.os.StrictMode;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by q4KV89ZB on 15-03-2015.
 */
public class GetData {
    String tag = this.getClass().getName();

    public static int RESULT_OK = 200;
    public static String INVALID_RESPONSE = "invalidResponse";
    public static String INVALID_PAYLOAD = "invalidPayload";
    public static String EXCEPTION = "exception";

    public static String getContent(String path) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpGet httpost = new HttpGet(path);
            /*
            // convert parameters into JSON object
			JSONObject holder = getJsonObjectFromMap(params);
			JSONObject holder=new JSONObject(data);
			*/
            //Passes the results to a string builder/entity
//            Logger.appendLog(data, true);
            //Sets the post request as the resulting string
            //Sets a request header
            httpost.setHeader("Accept", "application/json");
            httpost.setHeader("Content-type", "application/json");
            //Handles what is returned from the page
            HttpResponse reply = httpclient.execute(httpost);
            //Thread.sleep(7000);
            Log.i("status code", "status code is " + reply.getStatusLine().getStatusCode());

            if (reply.getStatusLine().getStatusCode() != RESULT_OK) {
                Log.i("check", "status code is " + reply.getStatusLine().getStatusCode());
                return INVALID_RESPONSE;
            } else {
                BufferedReader buffRead = new BufferedReader(new InputStreamReader(reply.getEntity().getContent(), "UTF-8"));
                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = buffRead.readLine()) != null) {
                    sb.append(line);
                }
                String response = sb.toString();

                Log.i("GetData", "response from getData is " + response);
                JSONArray responseJSON = new JSONArray(response);
//                if (responseJSON.has("Result")) {
//                    String responseResult = responseJSON.get("Result").toString();
//
//                    if (responseResult.equals("error")) {
//                        Log.i("check", "postActivity in Post Data " + responseJSON.toString());
//                        return INVALID_PAYLOAD;
//                    }
//                    else {
//                        Log.i("check","I am here");
//                        return response;
//                    }
//
//                } else {
//                    Log.i("check", "result field missing  and status code not 200");
//                    return response;
//                }
                return  responseJSON.toString();
            }
        } catch (Exception e) {
            Log.e("GetData", "Error in getting data: " + e.getMessage());

//            Logger.appendLog("Exception in posting data:" + e.getMessage(), true);
            //AppLog.logger(" Error in posting for " +path +" with data:" + data+" :"+ e.getMessage());
            e.printStackTrace();
            return EXCEPTION;
        }

    }

    public static String getComplianceContent(String path) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpGet httpost = new HttpGet(path);
            /*
            // convert parameters into JSON object
			JSONObject holder = getJsonObjectFromMap(params);
			JSONObject holder=new JSONObject(data);
			*/
            //Passes the results to a string builder/entity
//            Logger.appendLog(data, true);
            //Sets the post request as the resulting string
            //Sets a request header
            httpost.setHeader("Accept", "application/json");
            httpost.setHeader("Content-type", "application/json");
            //Handles what is returned from the page
            HttpResponse reply = httpclient.execute(httpost);
            //Thread.sleep(7000);
            Log.i("status code", "status code is " + reply.getStatusLine().getStatusCode());

            if (reply.getStatusLine().getStatusCode() != RESULT_OK) {
                Log.i("check", "status code is " + reply.getStatusLine().getStatusCode());
                return INVALID_RESPONSE;
            } else {
                BufferedReader buffRead = new BufferedReader(new InputStreamReader(reply.getEntity().getContent(), "UTF-8"));
                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = buffRead.readLine()) != null) {
                    sb.append(line);
                }
                String response = sb.toString();

//                Log.i("GetData", "response from getData is " + response);
//                JSONArray responseJSON = new JSONArray(response);
                return  response;
            }
        } catch (Exception e) {
            Log.e("GetData", "Error in getting data: " + e.getMessage());

//            Logger.appendLog("Exception in posting data:" + e.getMessage(), true);
            //AppLog.logger(" Error in posting for " +path +" with data:" + data+" :"+ e.getMessage());
            e.printStackTrace();
            return EXCEPTION;
        }

    }
}
