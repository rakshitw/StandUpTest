package com.xrci.standup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;




public class FirstScreenActivity extends Activity {
	//String registerUrl="";
	LoginButton login_button;
	boolean showLogout=false;
	//String gender="M";
	//String name,email;
	//EditText editTextName,editTextEmail;
	
	 public static final String EXTRA_MESSAGE = "message";
	    public static final String PROPERTY_REG_ID = "registration_id";
	    private static final String PROPERTY_APP_VERSION = "appVersion";
	    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	    String SENDER_ID = "288201432386";

	  

	    TextView mDisplay;
	    GoogleCloudMessaging gcm;
	    AtomicInteger msgId = new AtomicInteger();
	    Context context;

	    String regid;
	// private boolean isResumed = false;
	// private boolean userSkippedLogin = false;
	 private Session.StatusCallback callback = new Session.StatusCallback() {
	        @Override
	        public void call(Session session, SessionState state, Exception exception) {
	        
	            onSessionStateChange(session, state, exception);
	        }
	    };
	 private UiLifecycleHelper uiHelper;
	private boolean nextActivityInitiated=false;
	private Button registerButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.firstscreen_layout);
		login_button=(LoginButton)findViewById(R.id.login_button);
		registerButton=(Button)findViewById(R.id.buttonRegisterFirstScreen);
		uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
        Intent intent=getIntent();
        showLogout=intent.getBooleanExtra("showLogout", false);
        //savedInstanceState.get
		if(showLogout )
		{
			System.out.println("In if");
			login_button.setVisibility(View.VISIBLE);
			registerButton.setVisibility(View.INVISIBLE);
		}
		else
		{
			if(Session.getActiveSession().isOpened())
			{
				goToNextActivity();
				//System.out.println("Came in if of session open");
			}
			else
			{
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
   		       	if(preferences.getBoolean("withoutFB", false))
   		       	{
   		       		login_button.setVisibility(View.INVISIBLE);
   		       		registerButton.setVisibility(View.INVISIBLE);
   		       	new Handler().postDelayed(new Runnable(){
  		          @Override
  		          public void run() {
  		        	
  		        	  
  		        	  launchNewActivity();
  		        	  
  		          }
  				}, 3000);
   		       		
   		       	}
				
			}
			
		}
			
        
        
			
			
		 //uiHelper = new UiLifecycleHelper(, sessionCallback);
	        //uiHelper.onCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);

        //outState.putBoolean(USER_SKIPPED_LOGIN_KEY, userSkippedLogin);
    }
	
	 private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	        
	            // check for the OPENED state instead of session.isOpened() since for the
	            // OPENED_TOKEN_UPDATED state, the selection fragment should already be showing.
	            if (state.equals(SessionState.OPENED)) {
	            	
	               System.out.println(" Successfull login!");
	              // if(!showLogout)
	              // 	{
	            	   Request.newMeRequest(session, new Request.GraphUserCallback() {

	       	            // callback after Graph API response with user object
	       	            @Override
	       	            public void onCompleted(GraphUser user, Response response) {
	       	              if (user != null) {
	       	            	 // System.out.println("User ID="+user.getId());
	       	            	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	       		       		Editor editor=preferences.edit();
	       		       		// User changed !!
	       		       		if(!preferences.getString("fbid", "").contentEquals(user.getId()))
	       		       				{
	       		       					editor.putString("fbid", user.getId());
	       		       					editor.putString("name", user.getName());
	       		       					editor.putString(PROPERTY_REG_ID, "");
	       		       					editor.commit();
	       		       				}
	       		       		//editor.putString("medical", medicalHistory);
	       	            	  //user.
	       	                //TextView welcome = (TextView) findViewById(R.id.welcome);
	       	                //welcome.setText("Hello " + user.getName() + "!");
	       	              }
	       	            }
	       	          }).executeAsync();
	           	
	            	   goToNextActivity();
	            	   
	              // 	}
	            	   
	               
	            }
	            
	            if (state.equals(SessionState.CLOSED)) 
	            {
	            	// Reset the fbid
	            	
	            	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
   		       		Editor editor=preferences.edit();
   		       		editor.putString("fbid","");
  					editor.putString("name", "");
  					editor.putString(PROPERTY_REG_ID, "");
  					editor.commit();
	            	
	            	
	            }
	            
	        
	    }
	


    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
       // isResumed = true;

        // Call the 'activateApp' method to log an app event for use in analytics and advertising reporting.  Do so in
        // the onResume methods of the primary Activities that an app may be launched into.
        AppEventsLogger.activateApp(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //uiHelper.onPause();
          }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }
    
    public void goToNextActivity()
    {
    	
    	if(!nextActivityInitiated)
    	{
    	login_button.setVisibility(View.INVISIBLE);
    	registerButton.setVisibility(View.INVISIBLE);
    	nextActivityInitiated=true;
			
			new Handler().postDelayed(new Runnable(){
		          @Override
		          public void run() {
		        	  
		        	  
		        	  getGCMRegistrationId();
		        	  
		          }
				}, 3000);
    	}
    	
    }
    
    
    void launchNewActivity()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean isFormFilled = preferences.getBoolean(BasicInformationForm.registrationFormFilled, false);
        if (isFormFilled){
            Intent intent = new Intent(this, MainActivity.class);
            finish();
            startActivity(intent);
        }
        else {
            Intent intent = new Intent(this, BasicInformationForm.class);
            finish();
            startActivity(intent);
        }
    }

    void saveUserInformation()
    {
    	
    }
    
    
    
    void getGCMRegistrationId()
    {
    	 context = getApplicationContext();

         // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
         if (checkPlayServices()) {
             gcm = GoogleCloudMessaging.getInstance(this);
             regid = getRegistrationId();
             System.out.println(regid);

             if (regid.isEmpty()) {
                //registerInBackground();
                 launchNewActivity();
             }
             else
             {
            	 System.out.println("Here in else");
            	 launchNewActivity();
             }
         } else {
            // Log.i(TAG, "No valid Google Play Services APK found.");
        	 Toast.makeText(getApplicationContext(), "Install google play Services first", Toast.LENGTH_LONG).show();
        	 finish();
         }
    }
    
    
   
    
    
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    
    
    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
        	
        	boolean success=false;
        	private ProgressDialog pd1;

			@Override
			protected void onPreExecute() {
				pd1 = new ProgressDialog(FirstScreenActivity.this);
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
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    System.out.println("regid"+regid);

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    success=sendRegistrationIdToBackend(regid);

                   
                    // Persist the regID - no need to register again.
                    //storeRegistrationId(context, regid);
                    //success=true;
                } catch (IOException ex) {
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
            		storeRegistrationId(regid);
            		System.out.println("Came in success");
            		launchNewActivity();
            	}
            	else
            	{
            		Toast.makeText(getApplicationContext(), "Cant Register right now, try Later", Toast.LENGTH_LONG).show();
                   // TODO: Remove launchNewActivity
                    launchNewActivity();
                }
            }
        }.execute(null,null,null);
    }

    protected boolean sendRegistrationIdToBackend(String regid) {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	String fbid=preferences.getString("fbid", "");
    	String name=preferences.getString("name", "");
    	if(!fbid.isEmpty() && !name.isEmpty())
    	{
    		try
    		{
    			JSONObject obj=new JSONObject();
    			obj.put("fbid", fbid);
    			obj.put("name", name);
    			obj.put("regid",regid);
    			obj.put("timestamp", getCurrentTime() );
    			System.out.println(obj.toString());
    			String response=PostData.postContent(utils.SERVER_REGISTER_URI, obj.toString());
    			if(response.contentEquals("exception"))
    				return false;
    			else if (response.contentEquals("SUCCESS"))
    			{
    				return true;
    			}
    					
    			return false;
    		}
    		catch(Exception ex)
    		{
    			return false;
    		}
    	}
    	
    	
    	
		return false;
		
    	
    	
    	
		
	}

	private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                //Log.i(TAG, "This device is not supported.");
            	Toast.makeText(getApplicationContext(), "This device is not supported", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param regId registration ID
     */
    private void storeRegistrationId( String regId) {
        final SharedPreferences prefs =PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int appVersion = getAppVersion(getApplicationContext());
        //Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            //Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
           // Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }
    
    // Time zone not used, can be used for local deployment
    String getCurrentTime()
    {
    	SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	return sf.format(Calendar.getInstance().getTime());
    }
    
    public void openRegisterScreen (View v)
    {
    	
    	Intent intent=new Intent(this,RegisterScreenActivity.class);
    	finish();
    	startActivity(intent);
    	
    	
    }
    
    

  

    	}