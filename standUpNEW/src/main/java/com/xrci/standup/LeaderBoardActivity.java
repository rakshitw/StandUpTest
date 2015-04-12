package com.xrci.standup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.xrci.standup.utility.GetData;
import com.xrci.standup.utility.LeaderBoardModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


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
            try {

                ShowLeaders showLeaders = new ShowLeaders(getActivity(), getActivity().getApplicationContext());
                showLeaders.execute();
            } catch (Exception e) {
                Log.i("LeaderBoardActivity", "exception in leaderboardActivity " + e.getMessage());
            }
            return rootView;
        }

        public class ShowLeaders extends AsyncTask<Void, Void, String> {
            ArrayList<LeaderBoardModel> leaderBoardModels = new ArrayList<LeaderBoardModel>();
            Context activityContext;
            Context applicationContext;
            DatabaseHandler databaseHandler;
            ListView mListView = (ListView) rootView.findViewById(R.id.listview_leaderBoard);
            LeaderBoardAdapter leaderBoardAdapter;

            public ShowLeaders(Context activityContext, Context applicationContext) {
                this.activityContext = activityContext;
                this.applicationContext = applicationContext;
                databaseHandler = new DatabaseHandler(applicationContext);
            }

            @Override
            protected void onPreExecute() {

                super.onPreExecute();
                ArrayList<LeaderBoardModel> leaderModelsDb = databaseHandler.getLeaderBoardModelFromDB();
                Log.i("LEADERBOARD", "leader from db length is " + leaderModelsDb.size());
                leaderBoardAdapter = new LeaderBoardAdapter(activityContext
                        , R.layout.leaderboard_list_item, leaderModelsDb);
                mListView.setAdapter(leaderBoardAdapter);
                pd1 = new ProgressDialog(activityContext);
                pd1.setTitle("Fetching leaders");

                pd1.setMessage("Getting data");
                pd1.setCancelable(false);
                pd1.setIndeterminate(true);
                pd1.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pd1.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
//                        Intent intent = NavUtils.getParentActivityIntent(getActivity());
//                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                        NavUtils.navigateUpTo(getActivity(), intent);
                    }
                });
                pd1.show();

            }

            @Override
            protected String doInBackground(Void... params) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
                int userId = sharedPreferences.getInt("userId", 0);
                String response = GetData.getContent(LeaderBoardModel.getUrlForLeaderBoard(userId));

                if (!response.equals(PostData.INVALID_RESPONSE) && !response.equals(PostData.EXCEPTION)) {

//                [ { "name" : "Kuldeep Yadav", "avgStepsPerDay" : 3701.5000
//                        , "authType" : "facebook", "userId" : 4, "authId" : "10203786457483639" },
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String name = jsonObject.getString("name");
                            if (name.contains(" ")) {
                                name = name.substring(0, name.indexOf(" "));
                            }
                            int steps;
                            if (jsonObject.isNull("avgStepsPerDay")) {
                                steps = 0;
                            } else {
                                steps = (int) jsonObject.getDouble("avgStepsPerDay");
                            }
                            String authType = jsonObject.getString("authType");
                            String authId = jsonObject.getString("authId");
                            int compliance;
                            if (jsonObject.isNull("Compliance")) {
                                compliance = 0;
                            } else
                                compliance = jsonObject.getInt("Compliance");
                            LeaderBoardModel leaderBoardModel = new LeaderBoardModel(authId, authType, name, steps, compliance);
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
                try {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activityContext);
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
                    String lastUpdate = sharedPreferences.getString("last_updated", "unknown time");

                    if (response.equals(PostData.INVALID_RESPONSE) || response.equals(PostData.EXCEPTION)) {

                        alertDialogBuilder.setTitle("Internet Connection Unavailable");

                        alertDialogBuilder
                                .setMessage("Check your internet connection and try again. Leaderboard last updated at " + lastUpdate + ".")
                                .setCancelable(true)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // if this button is clicked, just close
                                        // the dialog box and do nothing

                                        dialog.cancel();
//                                        Intent intent = NavUtils.getParentActivityIntent(getActivity());
//                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                                        NavUtils.navigateUpTo(getActivity(), intent);
                                    }
                                });

                        // create alert dialog
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        // show it
                        alertDialog.show();


                    } else if (response.equals(PostData.INVALID_PAYLOAD)) {


                        alertDialogBuilder.setTitle("Oops");

                        alertDialogBuilder
                                .setMessage("This is embarrassing . Something went wrong. Leaderboard last updated at" + lastUpdate + ".")
                                .setCancelable(true)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // if this button is clicked, just close
                                        // the dialog box and do nothing

                                        dialog.cancel();
//                                        Intent intent = NavUtils.getParentActivityIntent(getActivity());
//                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                                        NavUtils.navigateUpTo(getActivity(), intent);
                                    }
                                });

                        // create alert dialog
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        // show it
                        alertDialog.show();

                    } else {
                        Log.i("Leaderboard", "leaderboard model size is" + leaderBoardModels.size());
                        if (leaderBoardModels != null && leaderBoardModels.size() > 0) {
                            databaseHandler.clearLeaderBoard();
                            leaderBoardAdapter.clear();
                            int i = 0;
                            for (LeaderBoardModel leaderBoardModel : leaderBoardModels) {
                                databaseHandler.addLeaderBoardRow(leaderBoardModel);
                                leaderBoardAdapter.insert(leaderBoardModel, i);
                                i++;
                            }

//                            LeaderBoardAdapter leaderBoardAdapter = new LeaderBoardAdapter(context
//                                    , R.layout.leaderboard_list_item, leaderBoardModels);
                            leaderBoardAdapter.notifyDataSetChanged();
//                            mListView.setAdapter(leaderBoardAdapter);
//                            SharedPreferences sharedPreferences2 = PreferenceManager.getDefaultSharedPreferences(applicationContext);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy, HH:mm");
                            Log.i("check", "update date is " + sdf.format(Calendar.getInstance().getTime()));
                            editor.putString("last_updated", sdf.format(Calendar.getInstance().getTime()));
                            editor.commit();
                        }
                    }
                    databaseHandler.close();
                } catch (Exception e) {
                    Log.i("check", "Error in LeaderBoardActivity  = " + e.getMessage());
                }
            }
        }
    }
}
