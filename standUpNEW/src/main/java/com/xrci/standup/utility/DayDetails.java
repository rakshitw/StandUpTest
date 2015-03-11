package com.xrci.standup.utility;

import java.util.Date;

/**
 * Created by Rakshit on 25-02-2015.
 */
public class DayDetails {
    private Date date;
    private Boolean goalAchieved;
    private int stepsTaken;
    private int stepsRemained;
    //To be used when goals are set dynamically
    private int dayGoal;
    private int compliance;

    public int getCompliance() {
        return compliance;
    }

    public void setCompliance(int compliance) {
        this.compliance = compliance;
    }


    public int getDayGoal() {
        return dayGoal;
    }

    public void setDayGoal(int dayGoal) {
        this.dayGoal = dayGoal;
    }


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Boolean getGoalAchieved() {
        return goalAchieved;
    }

    public void setGoalAchieved(Boolean goalAchieved) {
        this.goalAchieved = goalAchieved;
    }

    public int getStepsTaken() {
        return stepsTaken;
    }

    public void setStepsTaken(int stepsTaken) {
        this.stepsTaken = stepsTaken;
    }

    public int getStepsRemained() {
        return stepsRemained;
    }

    public void setStepsRemained(int stepsRemained) {
        this.stepsRemained = stepsRemained;
    }


}
