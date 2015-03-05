package com.xrci.standup.utility;

import android.util.Log;

import com.xrci.standup.PostData;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by q4KV89ZB on 05-03-2015.
 */
public class AuthenticationModel {
    public static String TAG = "Authentication Model";
    public static final String parameterUserName = "userName";
    public static final String parameterEmail = "email";
    public static final String parameterAuthType = "authType";
    public static final String parameterAuthID = "authId";
    public static final String parameterTypeID = "typeId";

    private String valueUserName;
    private String valueEmail;
    private int valueAuthType;
    private int valueAuthID;
    private int valueTypeID;

    public String getValueUserName() {
        return valueUserName;
    }

    public void setValueUserName(String valueUserName) {
        this.valueUserName = valueUserName;
    }

    public String getValueEmail() {
        return valueEmail;
    }

    public void setValueEmail(String valueEmail) {
        this.valueEmail = valueEmail;
    }

    public int getValueAuthType() {
        return valueAuthType;
    }

    public void setValueAuthType(int valueAuthType) {
        this.valueAuthType = valueAuthType;
    }

    public int getValueAuthID() {
        return valueAuthID;
    }

    public void setValueAuthID(int valueAuthID) {
        this.valueAuthID = valueAuthID;
    }

    public int getValueTypeID() {
        return valueTypeID;
    }

    public void setValueTypeID(int valueTypeID) {
        this.valueTypeID = valueTypeID;
    }


    static final public String authenticationUri = "http://64.49.234.131:8080/standup/rest/user/authenticate";

    public AuthenticationModel(String valueUserName, String valueEmail, int valueAuthType, int valueAuthID, int valueTypeID) {
        this.valueUserName = valueUserName;
        this.valueEmail = valueEmail;
        this.valueAuthType = valueAuthType;
        this.valueAuthID = valueAuthID;
        this.valueTypeID = valueTypeID;
    }

    public String verifyAuthentication() {
        JSONObject authenticateObject = new JSONObject();
//    //     {"userName": "amandeep","email": "amandeep@abc.com","authType" : "Facebook" , "authId": 1212432142, "typeId": 1}
        try {
            authenticateObject.put(parameterUserName, valueUserName);
            authenticateObject.put(parameterAuthID, valueAuthID);
            authenticateObject.put(parameterAuthType, valueAuthType);
            authenticateObject.put(parameterEmail, valueEmail);
            authenticateObject.put(parameterTypeID, valueTypeID);


        } catch (JSONException e) {
            Log.i(TAG, "JSON Exception");
        }
        String authenticationString = authenticateObject.toString();
        String response = PostData.postContent(authenticationUri, authenticationString);
        return response;
    }

}