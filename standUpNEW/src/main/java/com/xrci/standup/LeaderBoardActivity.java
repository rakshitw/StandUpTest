package com.xrci.standup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.xrci.standup.utility.GetData;
import com.xrci.standup.utility.LeaderBoardModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class LeaderBoardActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new LeaderBoardFragment())
                    .commit();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_leader_board, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class LeaderBoardFragment extends Fragment {
        View rootView;
        Context context;
        private ProgressDialog pd1;

        public LeaderBoardFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            context = getActivity();
            rootView = inflater.inflate(R.layout.fragment_leader_board, container, false);

            TextView dayView = (TextView) rootView.findViewById(R.id.leaderBoard_day);
            String day = Calendar.getInstance().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
            dayView.setText(day);
            TextView dateView = (TextView) rootView.findViewById(R.id.leaderBoard_date);
            SimpleDateFormat sf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            dateView.setText(sf.format(Calendar.getInstance().getTime()));
            ShowLeaders showLeaders = new ShowLeaders();
            showLeaders.execute();
            return rootView;
        }

        public class ShowLeaders extends AsyncTask<Void, Void, String> {
            ArrayList<LeaderBoardModel> leaderBoardModels = new ArrayList<LeaderBoardModel>();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pd1 = new ProgressDialog(context);
                pd1.setTitle("Fetching leaders");

                pd1.setMessage("Getting data");
                pd1.setCancelable(false);
                pd1.setIndeterminate(true);
                pd1.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pd1.show();

            }

            @Override
            protected String doInBackground(Void... params) {
                String response = GetData.getContent(LeaderBoardModel.uri);

                if (!response.equals(PostData.INVALID_RESPONSE) && !response.equals(PostData.EXCEPTION)) {

//                [ { "name" : "Kuldeep Yadav", "avgStepsPerDay" : 3701.5000
//                        , "authType" : "facebook", "userId" : 4, "authId" : "10203786457483639" },
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String name = jsonObject.getString("name");
                            int steps = (int) jsonObject.getDouble("avgStepsPerDay");
                            String authType = jsonObject.getString("authType");
                            String authId = jsonObject.getString("authId");
                            LeaderBoardModel leaderBoardModel = new LeaderBoardModel(authId, authType, name, steps);
                            leaderBoardModels.add(leaderBoardModel);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                return response;

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

                } else {
                    if (leaderBoardModels != null && leaderBoardModels.size() > 0) {
                        ListView mListView = (ListView) rootView.findViewById(R.id.listview_leaderBoard);

                        LeaderBoardAdapter leaderBoardAdapter = new LeaderBoardAdapter(context
                                , R.layout.leaderboard_list_item, leaderBoardModels);

                        mListView.setAdapter(leaderBoardAdapter);
                    }


                }


            }
        }
    }



}