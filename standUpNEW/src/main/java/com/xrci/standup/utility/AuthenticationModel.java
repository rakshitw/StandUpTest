package com.xrci.standup.utility;

import android.util.Log;

import com.xrci.standup.PostData;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

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
    public static final String parameterSignUpdate = "signUpDate";
    public static final String parameterOrganization = "organisation";
    public static final String parameterAge = "age";
    public static final String parameterSex = "sex";
    public static final String parameterWeight = "weight";


    private String valueUserName;
    private String valueEmail;
    private String valueAuthType;
    private String valueAuthID;
    private int valueTypeID;
    private Date valueSignUpDate;
    private String valueSex;
    private String valueOrganization;
    private String valueAge;
    private String valueWeight;


    static final public String authenticationUri = "http://64.49.234.131:8080/standup/rest/user/authenticate";
//    static final public String authenticationUri = "http://13.218.150.162:8080/standup/rest/user/authenticate";

//    Authentication:
//    {"userName": "amandeep",
//            "email": "amandeep@abc.com","authType" : "email"
//            , "authId": "amandeep@abc.com", "signUpDate" : "22-02-2015-23-55"
//            , "sex": "M", "organisation" : "xrci", "age" : 27}


    /**
     * @param valueUserName
     * @param valueEmail
     * @param valueAuthType
     * @param valueAuthID
     * @param valueSignUpDate
     * @param valueSex
     * @param valueOrganization
     * @param valueAge
     */
    public AuthenticationModel(String valueUserName, String valueEmail
            , String valueAuthType, String valueAuthID, Date valueSignUpDate
            , String valueSex, String valueOrganization, String valueAge, String valueWeight) {
        this.valueUserName = valueUserName;
        this.valueEmail = valueEmail;
        this.valueAuthType = valueAuthType;
        this.valueAuthID = valueAuthID;
        this.valueSignUpDate = valueSignUpDate;
        this.valueSex = valueSex;
        this.valueOrganization = valueOrganization;
        this.valueAge = valueAge;
        this.valueWeight = valueWeight;

    }

    public String verifyAuthentication() {
        JSONObject authenticateObject = new JSONObject();
//    //     {"userName": "amandeep","email": "amandeep@abc.com","authType" : "Facebook" , "authId": 1212432142, "typeId": 1}
        try {
            authenticateObject.put(parameterUserName, valueUserName);
            authenticateObject.put(parameterAuthID, valueAuthID);
            authenticateObject.put(parameterAuthType, valueAuthType);
            authenticateObject.put(parameterEmail, valueEmail);
            authenticateObject.put(parameterSex, valueSex);
            authenticateObject.put(parameterOrganization, valueOrganization);
            authenticateObject.put(parameterAge, Integer.parseInt(valueAge));
            SimpleDateFormat sf = new SimpleDateFormat("dd-MM-yyyy-HH-mm");
            authenticateObject.put(parameterSignUpdate, sf.format(valueSignUpDate));
            authenticateObject.put(parameterWeight, Integer.parseInt(valueWeight));
        } catch (JSONException e) {
            Log.i(TAG, "JSON Exception");
        }
        String authenticationString = authenticateObject.toString();
        Log.i(TAG, "authString is " + authenticationString);
        String response = PostData.postContent(authenticationUri, authenticationString);
        return response;
    }

}