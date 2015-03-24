package com.xrci.standup.utility;

/**
 * Created by q4KV89ZB on 23-03-2015.
 */
public class LeaderBoardModel {
    public static String uri = "http://64.49.234.131:8080/standup/rest/user/getLeadershipBoard";

    public String getAuthId() {
        return authId;
    }

    public String getAuthType() {
        return authType;
    }

    public String getName() {
        return name;
    }

    public int getSteps() {
        return steps;
    }

    private String authId;
    private String authType;
    private String name;
    private int steps;

    public LeaderBoardModel(String authId, String authType, String name, int steps) {
        this.authId = authId;
        this.authType = authType;
        this.name = name;
        this.steps = steps;
    }



}
