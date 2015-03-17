package com.xrci.standup.utility;

import java.io.Serializable;

/**
 * Created by q4KV89ZB on 12-03-2015.
 */
public class NotificationModel implements Serializable {
    public String getMessage() {
        return message;
    }

    public int getActivity() {
        return activity;
    }

    public String getTimeString() {
        return timeString;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setActivity(int activity) {
        this.activity = activity;
    }

    public void setTimeString(String timeString) {
        this.timeString = timeString;
    }

    String message;
    int activity;
    String timeString;

    public NotificationModel(String message, int activity, String timeString) {
        this.message = message;
        this.activity = activity;
        this.timeString = timeString;
    }



}
