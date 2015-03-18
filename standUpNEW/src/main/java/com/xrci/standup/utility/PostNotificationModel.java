package com.xrci.standup.utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by q4KV89ZB on 18-03-2015.
 */
public class PostNotificationModel {
    //[{"time":"15-03-2015-18-24-47","message":"abc","sourceId":1,"steps":0,"typeId":3,"userId":1}]

    public static final String parameterTime = "time";
    public static final String parameterMessage = "message";
    public static final String parameterSourceId = "sourceId";
    public static final String parameterUserId = "userId";
    public static final String parameterTypeID = "typeId";
    public static final String parameterSteps = "steps";
    //    public static String postActivityDetailURI = "http://13.218.150.162:8080/standup/rest/activity/postActivity";
    public static String postNotificationURI = "http://64.49.234.131:8080/standup/rest/notification/postNotification";

    private Date valueTime;
    private String valueMessage;
    private int valueSourceId;
    private int valueUserId;
    private int valueTypeId;
    private int valueSteps;

    public PostNotificationModel(Date valueTime, String valueMessage , int valueSourceId, int valueUserId, int valueTypeId, int valueSteps) {
        this.valueTime = valueTime;
        this.valueMessage = valueMessage;
        this.valueSourceId = valueSourceId;
        this.valueUserId = valueUserId;
        this.valueTypeId = valueTypeId;
        this.valueSteps = valueSteps;
    }

    public JSONArray getPostNotificationJSON(JSONArray jsonArray) {
        JSONObject jsonObject = new JSONObject();
        try {
            SimpleDateFormat sf = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
            jsonObject.put(parameterTime, sf.format(valueTime));
            jsonObject.put(parameterMessage,valueMessage);
            jsonObject.put(parameterSourceId, valueSourceId);
            jsonObject.put(parameterSteps, valueSteps);
            jsonObject.put(parameterTypeID, valueTypeId);
            jsonObject.put(parameterUserId, valueUserId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        jsonArray.put(jsonObject);
        return jsonArray;
    }
}
