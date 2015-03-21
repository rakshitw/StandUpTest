package com.xrci.standup;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.xrci.standup.utility.NotificationModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class NotificationLogActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_log);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notification_log, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {


            DatabaseHandler dbHandler = new DatabaseHandler(getActivity());

            ArrayList<NotificationModel> notificationModels = dbHandler.getDayNotification(Calendar
                    .getInstance().getTime());

                for(NotificationModel notificationModel : notificationModels){
                Log.i("Notification", "notification models length is "
                        + notificationModel.getActivity() + " message : "
                        + notificationModel.getMessage() + " time: "
                        + notificationModel.getTimeString());
            }
            View rootView = inflater.inflate(R.layout.fragment_notification_log, container, false);
            TextView dayView = (TextView) rootView.findViewById(R.id.notification_day);
            String day = Calendar.getInstance().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
            dayView.setText(day);
            TextView dateView = (TextView) rootView.findViewById(R.id.notification_date);
            SimpleDateFormat sf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            dateView.setText(sf.format(Calendar.getInstance().getTime()));

            ListView mListView = (ListView) rootView.findViewById(R.id.listview_notification);
            NotificationAdapter notificationAdapter = new NotificationAdapter(getActivity()
                    , R.layout.notification_list_item,notificationModels);

            mListView.setAdapter(notificationAdapter);



            return rootView;
        }
    }
}
