package com.xrci.standup.utility;

import java.util.Date;

/**
 * Created by q4KV89ZB on 17-03-2015.
 */
public class DatedResponse {
    Date date;
    String response;

    public DatedResponse(Date date, String response) {
        this.date = date;
        this.response = response;
    }

    public Date getDate() {
        return date;
    }

    public String getResponse() {
        return response;
    }
}
