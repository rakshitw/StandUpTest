package com.xrci.standup;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterScreenActivity extends Activity {
	String gender="M";
	String name,email;
	EditText editTextName,editTextEmail;
	



@Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.register_screen);
	editTextName=(EditText)findViewById(R.id.editTextName);
	editTextEmail=(EditText)findViewById(R.id.editTextEmail);
	
}


public void registerUser(View v)
{
	name=editTextName.getText().toString();
	email=editTextEmail.getText().toString();
	if(name.length()<2)
		Toast.makeText(this, "Enter valid name", Toast.LENGTH_SHORT).show();
	else if (email.length()<3)
		Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show();
	else
	{
		//sendRegistrationRequestToServer(name,email,gender);
		registerInBackground();
	}
		
	
}
public void selectMale(View v)
{
	gender="M";
}

public void selectFemale(View v)
{
	gender="F";
}


private void sendRegistrationRequestToServer(String name2, String email2,
		String gender2) {
	// TODO Auto-generated method stub
	
}


private void registerInBackground() {
    new AsyncTask<Void, Void, String>() {
    	
    	boolean success=false;
    	private ProgressDialog pd1;

		@Override
		protected void onPreExecute() {
			pd1 = new ProgressDialog(RegisterScreenActivity.this);
			pd1.setTitle("Processing...");
			
			pd1.setMessage("Initializing");
			pd1.setCancelable(false);
			pd1.setIndeterminate(true);
			pd1.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pd1.show(); 
			
		}
        @Override
        protected String doInBackground(Void... params) {
            String msg = "";
            try {
               
                success=sendRegistrationToBackend();
            	//registerUserAtBackend();

               
                // Persist the regID - no need to register again.
                //storeRegistrationId(context, regid);
                //success=true;
            } catch (Exception ex) {
                msg = "Error :" + ex.getMessage();
                
                // If there is an error, don't just keep trying to register.
                // Require the user to click a button again, or perform
                // exponential back-off.
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String msg) {
            //mDisplay.append(msg + "\n");
        	pd1.dismiss();
        	if(success)
        	{
        		//storeRegistrationId(regid);
        		System.out.println("Came in success");
        		launchNewActivity();
        	}
        	else
        	{
        		Toast.makeText(getApplicationContext(), "Cant Register right now, try Later", Toast.LENGTH_LONG).show();
        	}
        }
    }.execute(null,null,null);
}





void launchNewActivity()
{
	Intent intent=new Intent(this,MainActivity.class);
	finish();
	startActivity(intent);
}


protected boolean sendRegistrationToBackend() {
	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	//String fbid=preferences.getString("fbid", "");
	//String name=preferences.getString("name", "");
	if(!email.isEmpty() && !name.isEmpty())
	{
		try
		{
			JSONObject obj=new JSONObject();
			obj.put("email", email);
			obj.put("name", name);
			obj.put("gender", gender);
			//obj.put("regid",regid);
			obj.put("timestamp", getCurrentTime() );
			System.out.println(obj.toString());
			String response=PostData.postContent(utils.SERVER_REGISTER_URI_WO_FB, obj.toString());
			System.out.println(response);
			if(response.contentEquals("exception"))
				return false;
			else
			{
				return parseRegistrationJSONRespone(response);
				
				
			}
					
			
		}
		catch(Exception ex)
		{
			return false;
		}
	}
	
	
	
	return false;
	
	
	
	
	
}
boolean  parseRegistrationJSONRespone(String response) {
	// TODO Auto-generated method stub
	try {
		JSONObject obj=new JSONObject(response);
		String name=obj.getString("name");
		String id=obj.getString("id");
		
		 SharedPreferences prefs =PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //int appVersion = getAppVersion(getApplicationContext());
        //Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("name", name);
        editor.putString("fbid", id);
        editor.putBoolean("withoutFB",true );
        editor.commit();
        return true;
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return false;
	}
	
	
}


String getCurrentTime()
{
	SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	return sf.format(Calendar.getInstance().getTime());
}


}