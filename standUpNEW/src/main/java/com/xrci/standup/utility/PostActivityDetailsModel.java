package com.xrci.standup.utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by q4KV89ZB on 06-03-2015.
 */
public class PostActivityDetailsModel {
    //    {"startTime": "22-02-2015-23-55","endTime": "22-02-2015-23-55","sourceId" : 1 , "userId": 1, "typeId": 1, "steps" : 2}

    public static final String parameterStartTime = "startTime";
    public static final String parameterEndTime = "endTime";
    public static final String parameterSourceId = "sourceId";
    public static final String parameterUserId = "userId";
    public static final String parameterTypeID = "typeId";
    public static final String parameterSteps = "steps";
//    public static String postActivityDetailURI = "http://13.218.150.162:8080/standup/rest/activity/postActivity";
    public static String postActivityDetailURI = "http://64.49.234.131:8080/standup/rest/activity/postActivity";

    private Date valueStartTime;
    private Date valueEndTime;
    private int valueSourceId;
    private int valueUserId;
    private int valueTypeId;
    private int valueSteps;

    public PostActivityDetailsModel(Date valueStartTime, Date valueEndTime, int valueSourceId, int valueUserId, int valueTypeId, int valueSteps) {
        this.valueStartTime = valueStartTime;
        this.valueEndTime = valueEndTime;
        this.valueSourceId = valueSourceId;
        this.valueUserId = valueUserId;
        this.valueTypeId = valueTypeId;
        this.valueSteps = valueSteps;
    }

    public JSONArray getPostActivityJSON(JSONArray jsonArray) {
        JSONObject jsonObject = new JSONObject();
        try {
            SimpleDateFormat sf = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
            jsonObject.put(parameterStartTime, sf.format(valueStartTime));
            jsonObject.put(parameterEndTime, sf.format(valueEndTime));
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
