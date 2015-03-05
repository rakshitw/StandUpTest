package com.xrci.standup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;


public class WeeklyActivity extends Activity {

    public static final int goal = 7000;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    private GestureDetector mGesture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new WeeklyFragment())
                    .commit();
        }
        mGesture = new GestureDetector(this, mOnGesture);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weekly, menu);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mTitle = mDrawerTitle = getTitle();

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open,
                R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
                //refreshStatisticsCircle();
                // creates call to
                // onPrepareOptionsMenu()
            }
        };


        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean handled = super.dispatchTouchEvent(ev);
        handled = mGesture.onTouchEvent(ev);
        return handled;
    }

    private GestureDetector.OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //Log.v("fling", "Flinged.");
            try {
                if (Math.abs(e1.getY() - e2.getY()) > MainActivity.SWIPE_MAX_OFF_PATH) {
                    return false;
                }

                if (e1.getX() - e2.getX() > MainActivity.SWIPE_MIN_DISTANCE && Math.abs(velocityX) > MainActivity.SWIPE_THRESHOLD_VELOCITY) {
                    //do nothing


                } else if (e2.getX() - e1.getX() > MainActivity.SWIPE_MIN_DISTANCE && Math.abs(velocityX) > MainActivity.SWIPE_THRESHOLD_VELOCITY) {
                    //Start main activity
                    Intent startMainActivity = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(startMainActivity);
//                    Toast.makeText(getApplicationContext(), "Flip Left to Right", Toast.LENGTH_SHORT).show();

                }
            } catch (Exception e) {
                Logger.appendLog("Exception in onFling(MainActivity):" + e.getMessage(), true);
            }

            return true;
        }
    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (item.getItemId() == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);

        } else if (mDrawerToggle.onOptionsItemSelected(item)) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}

