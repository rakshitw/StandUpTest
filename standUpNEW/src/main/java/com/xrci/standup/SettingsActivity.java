package com.xrci.standup;

import java.util.Calendar;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

public class SettingsActivity extends Activity{
	TimePicker workplaceStartTimePicker;
	TimePicker workplaceEndTimePicker;
	CheckBox checkBoxOfficeTimeOnly; 
	EditText editTextMaxSittingTime,editTextStartTime,editTextEndTime;
	int maximumSittingTime;
	int workplaceStartHour,workplaceStartMin,workplaceEndHour,workplaceEndMin;
	boolean alertAtOffice;
	public static boolean   settingsUpdated=false;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_layout);
		workplaceStartTimePicker=(TimePicker)findViewById(R.id.timePickerStart);
		workplaceEndTimePicker=(TimePicker)findViewById(R.id.timePickerEnd);
		checkBoxOfficeTimeOnly=(CheckBox)findViewById(R.id.checkBoxAlert);
		editTextMaxSittingTime=(EditText)findViewById(R.id.editTextMaxSittingtime);
		editTextStartTime= (EditText)findViewById(R.id.editTextStartTime);
		editTextEndTime= (EditText)findViewById(R.id.editTextEndTime);
		workplaceStartTimePicker.setIs24HourView(true);
		workplaceEndTimePicker.setIs24HourView(true);
		updateView();
	}
	
	void updateView()
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		workplaceStartHour=preferences.getInt("workplaceStartHour",10);
		workplaceStartMin=preferences.getInt("workplaceStartMin",0);
		
		workplaceStartTimePicker.setCurrentHour(workplaceStartHour);
		workplaceStartTimePicker.setCurrentMinute(workplaceStartMin);
		
		workplaceEndHour=preferences.getInt("workplaceEndHour",18);
		workplaceEndMin=preferences.getInt("workplaceEndMin",0);
	
		workplaceEndTimePicker.setCurrentHour(workplaceEndHour);
		workplaceEndTimePicker.setCurrentMinute(workplaceEndMin);
		
		alertAtOffice=preferences.getBoolean("alertAtOffice", true);
		checkBoxOfficeTimeOnly.setChecked(alertAtOffice);
		
		maximumSittingTime=preferences.getInt("maximumSittingTime", 40);
		editTextMaxSittingTime.setText(maximumSittingTime+"");
		
		
		
		
	}
	
	
	public void saveSettings(View v)
	{
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Editor editor=preferences.edit();
	    try
	    {
		maximumSittingTime=Integer.parseInt(editTextMaxSittingTime.getText().toString());
	    }
	    catch(Exception ex)
	    {
	    	Toast.makeText(this, "Enter sitting time in minutes!", Toast.LENGTH_SHORT).show();
	        return;
	    }
	    editor.putInt("maximumSittingTime", maximumSittingTime);
	    editor.putInt("workplaceStartHour", workplaceStartTimePicker.getCurrentHour());
		editor.putInt("workplaceStartMin", workplaceStartTimePicker.getCurrentMinute());
		editor.putInt("workplaceEndHour", workplaceEndTimePicker.getCurrentHour());
		editor.putInt("workplaceEndMin", workplaceEndTimePicker.getCurrentMinute());
		editor.putBoolean("alertAtOffice",checkBoxOfficeTimeOnly.isChecked() );
		editor.commit();
		Toast.makeText(this,"Saved Successfully", Toast.LENGTH_LONG).show();
		settingsUpdated=true;
		finish();
	
	
	}
	
	
	public void onEditTextStartClick(View v)
	{
		Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(SettingsActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
            	editTextStartTime.setText( selectedHour + ":" + selectedMinute);
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
	}
	
	public void onEditTextEndClick(View v)
	{
		Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(SettingsActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
            	editTextEndTime.setText( selectedHour + ":" + selectedMinute);
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
	}
	
	

	
	

}
