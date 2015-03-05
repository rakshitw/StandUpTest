package com.xrci.standup;

import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class SwipeGestureDetector extends SimpleOnGestureListener {
	final int SWIPE_MIN_DISTANCE = 120;
    final int SWIPE_MAX_OFF_PATH = 250;
    final int SWIPE_THRESHOLD_VELOCITY = 200;
    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        try {
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) return false;

            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
               System.out.println("Detected right to left");

            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
            	System.out.println("left to right");

            }
        } catch (Exception e) {
            // nothing
        }
        return false;
    }
}