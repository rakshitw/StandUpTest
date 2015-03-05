package com.xrci.standup;

import java.util.Date;

public class ActivityDetails {
	public int activityType;
	public long timePeriod;
	Date start;
	Date end;
	int noOfSteps;

    ActivityDetails()
    {

    }

    public ActivityDetails(int activityType, long timePeriod, Date start, Date end, int noOfSteps) {
        this.activityType = activityType;
        this.timePeriod = timePeriod;
        this.start = start;
        this.end = end;
        this.noOfSteps = noOfSteps;
    }
}
