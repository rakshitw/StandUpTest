package com.xrci.standup;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SettingsActivity extends Activity {
    TimePicker workplaceStartTimePicker;
    TimePicker workplaceEndTimePicker;
    //	CheckBox checkBoxOfficeTimeOnly;
    EditText editTextStartTime, editTextEndTime, editTextWeight;
    int maximumSittingTime;
    int workplaceStartHour, workplaceStartMin, workplaceEndHour, workplaceEndMin;
    boolean alertAtOffice;
    public static boolean settingsUpdated = false;
    public static String stopPingTimePeriodStart = "stopPingTimePeriodStart";
    public static String stopPingTimePeriodEnd = "stopPingTimePeriodEnd";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        workplaceStartTimePicker = (TimePicker) findViewById(R.id.timePickerStart);
        workplaceEndTimePicker = (TimePicker) findViewById(R.id.timePickerEnd);
//		checkBoxOfficeTimeOnly=(CheckBox)findViewById(R.id.checkBoxAlert);
//		editTextMaxSittingTime=(EditText)findViewById(R.id.editTextMaxSittingtime);
        editTextStartTime = (EditText) findViewById(R.id.editTextStartTime);
        editTextEndTime = (EditText) findViewById(R.id.editTextEndTime);
        editTextWeight = (EditText) findViewById(R.id.editTextWeight);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editTextStartTime.setText(sharedPreferences.getString(stopPingTimePeriodStart, "22:00"));
        editTextEndTime.setText(sharedPreferences.getString(stopPingTimePeriodEnd, "7:00"));
        editTextWeight.setText(sharedPreferences.getString(BasicInformationForm.registrationWeight,"65"));


//        workplaceStartTimePicker.setIs24HourView(true);
//        workplaceEndTimePicker.setIs24HourView(true);
//        updateView();
    }

//    void updateView() {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        workplaceStartHour = preferences.getInt("workplaceStartHour", 10);
//        workplaceStartMin = preferences.getInt("workplaceStartMin", 0);
//
//        workplaceStartTimePicker.setCurrentHour(workplaceStartHour);
//        workplaceStartTimePicker.setCurrentMinute(workplaceStartMin);
//
//        workplaceEndHour = preferences.getInt("workplaceEndHour", 18);
//        workplaceEndMin = preferences.getInt("workplaceEndMin", 0);
//
//        workplaceEndTimePicker.setCurrentHour(workplaceEndHour);
//        workplaceEndTimePicker.setCurrentMinute(workplaceEndMin);
//
//        alertAtOffice = preferences.getBoolean("alertAtOffice", true);
////		checkBoxOfficeTimeOnly.setChecked(alertAtOffice);
//
////		maximumSittingTime=preferences.getInt("maximumSittingTime", 40);
////		editTextMaxSittingTime.setText(maximumSittingTime+"");
//
//
//    }


    public void saveSettings(View v) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String weight_orig = sharedPreferences.getString(BasicInformationForm.registrationWeight,"65");
        int userId = sharedPreferences.getInt("userId", 0);
        String weight_new = editTextWeight.getText().toString();
//        {: 1,"weight": 80,"modificationDate" : "22-02-2015-23-55-22"}
        if(!weight_new.equals(weight_orig)){
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("userId", userId);
                jsonObject.put("weight", Integer.parseInt(weight_new));
                SimpleDateFormat sf = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
                String date = sf.format(Calendar.getInstance().getTime());
                jsonObject.put("modificationDate", date );
                String data = jsonObject.toString();
                ChangeWeight  changeWeight = new ChangeWeight();
                changeWeight.execute(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        settingsUpdated = true;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Editor editor = preferences.edit();
        editor.putString(stopPingTimePeriodEnd, editTextEndTime.getText().toString());
        editor.putString(stopPingTimePeriodStart,editTextStartTime.getText().toString() );
        editor.putString(BasicInformationForm.registrationWeight, editTextWeight.getText().toString());
//	    try
//	    {
////		maximumSittingTime=Integer.parseInt(editTextMaxSittingTime.getText().toString());
//	    }
//	    catch(Exception ex)
//	    {
//	    	Toast.makeText(this, "Enter sitting time in minutes!", Toast.LENGTH_SHORT).show();
//	        return;
////	    }
//        Log.i("check ", "start time" + editTextStartTime.getText());
//        Log.i("check ", "end time" + editTextEndTime.getText());

//        editor.putInt("", maximumSittingTime);
//	    editor.putInt("workplaceStartHour", workplaceStartTimePicker.getCurrentHour());
//		editor.putInt("workplaceStartMin", workplaceStartTimePicker.getCurrentMinute());
//		editor.putInt("workplaceEndHour", workplaceEndTimePicker.getCurrentHour());
//		editor.putInt("workplaceEndMin", workplaceEndTimePicker.getCurrentMinute());
//        Log.i("check", "start time is " +  workplaceStartTimePicker.getCurrentHour() + ":" +  workplaceStartTimePicker.getCurrentMinute());
//        Log.i("check", "end time is " +  workplaceEndTimePicker.getCurrentHour() + ":" +  workplaceEndTimePicker.getCurrentMinute());

//		editor.putBoolean("alertAtOffice",checkBoxOfficeTimeOnly.isChecked() );

//


        editor.commit();

        finish();
    }

    public void onEditTextStartClick(View v) {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(SettingsActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                if (selectedHour < 10 && selectedMinute < 10) {
                    editTextStartTime.setText("0" + selectedHour + ":" + "0" + selectedMinute);
                } else if (selectedHour < 10)
                    editTextStartTime.setText("0" + selectedHour + ":" + selectedMinute);
                else if (selectedMinute < 10)
                    editTextStartTime.setText(  selectedHour + ":" + "0" + selectedMinute);
                else
                    editTextStartTime.setText(selectedHour + ":" + selectedMinute);
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    public void onEditTextEndClick(View v) {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(SettingsActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                if (selectedHour < 10 && selectedMinute < 10) {
                    editTextEndTime.setText("0" + selectedHour + ":" + "0" + selectedMinute);
                } else if (selectedHour < 10)
                    editTextEndTime.setText("0" + selectedHour + ":" + selectedMinute);
                else if (selectedMinute < 10)
                    editTextEndTime.setText(  selectedHour + ":" + "0" + selectedMinute);
                else
                    editTextEndTime.setText(selectedHour + ":" + selectedMinute);
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    public class ChangeWeight extends AsyncTask<String, Void, Void> {


        @Override
        protected Void doInBackground(String... params) {
            String url = "http://64.49.234.131:8080/standup/rest/user/updateUserFeatures";
            String response = PostData.postContent(url, params[0]);

            Log.i("check", "response of weight update is " +  response + " request is " + params[0]);

            return null;
        }
    }


}
