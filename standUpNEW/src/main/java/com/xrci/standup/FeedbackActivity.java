package com.xrci.standup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class FeedbackActivity extends Activity {
    private ProgressDialog pd1;
    String uri = "http://64.49.234.131:8080/standup/rest/feedback/postDetails";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feedback, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
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
            View rootView = inflater.inflate(R.layout.fragment_feedback, container, false);

            return rootView;
        }
    }

    public void onSendFeedback(View view) {
        EditText editText = (EditText) findViewById(R.id.textFeedback);
        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        String rating = String.valueOf(ratingBar.getRating());
        String feedback = editText.getText().toString();
        SendFeedBack sendFeedBack = new SendFeedBack(this);
        sendFeedBack.execute(feedback, rating);

    }

    public class SendFeedBack extends AsyncTask<String, Void, String> {
        Context context;

        public SendFeedBack(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd1 = new ProgressDialog(context);
            pd1.setTitle("Feedback");

            pd1.setMessage("Sending feedback");
            pd1.setCancelable(false);
            pd1.setIndeterminate(true);
            pd1.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd1.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String url = "http://64.49.234.131:8080/standup/rest/feedback/postDetails";
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            int userId = sharedPreferences.getInt("userId", 0);
            Log.i("check", "string is blah ");
            if(params[0].length() > 0) {
                //Make JSON and Post Data to the
                //[{"userId": 1,"comment": "abc","time" : "22-02-2015-23-55-22"}]
                Date date = Calendar.getInstance().getTime();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
                JSONArray jsonArray = new JSONArray();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("userId", userId);
                    jsonObject.put("comment", params[0]);
                    jsonObject.put("time", simpleDateFormat.format(date) );
                    jsonObject.put("rating", params[1]);
                    jsonArray.put(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String response = PostData.postContent(url,jsonArray.toString() );
                return  response;
            }
            else
                return  params[0];
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            pd1.dismiss();
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);


            if (response.equals(PostData.INVALID_RESPONSE) || response.equals(PostData.EXCEPTION)) {

                alertDialogBuilder.setTitle("Internet Connection Unavailable");

                alertDialogBuilder
                        .setMessage("Check your internet connection and try again.")
                        .setCancelable(true)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                                Intent intent = NavUtils.getParentActivityIntent(FeedbackActivity.this);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                NavUtils.navigateUpTo(FeedbackActivity.this, intent);
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();


            } else if (response.equals(PostData.INVALID_PAYLOAD)) {


                alertDialogBuilder.setTitle("Oops");

                alertDialogBuilder
                        .setMessage("This is embarrassing . Something went wrong.")
                        .setCancelable(true)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();
            } else if (response.length() == 0) {
                alertDialogBuilder.setTitle("Oops");

                alertDialogBuilder
                        .setMessage("You forget to enter the feedback")
                        .setCancelable(true)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();
            } else {
                alertDialogBuilder.setTitle("Feedback sent");

                alertDialogBuilder
                        .setMessage("Thank you!")
                        .setCancelable(true)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                                Intent intent = NavUtils.getParentActivityIntent(FeedbackActivity.this);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                NavUtils.navigateUpTo(FeedbackActivity.this, intent);
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();
            }
        }
    }
}
