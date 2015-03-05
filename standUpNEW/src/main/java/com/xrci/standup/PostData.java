package com.xrci.standup;


import android.os.StrictMode;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PostData
{
	String tag= this.getClass().getName();
	
	
	
	public static String postContent(String path, String data)
	{
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

		StrictMode.setThreadPolicy(policy);
		try
		{
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httpost = new HttpPost(path);
			/*
			// convert parameters into JSON object
			JSONObject holder = getJsonObjectFromMap(params);
			JSONObject holder=new JSONObject(data);
			*/
			//Passes the results to a string builder/entity
			StringEntity se = new StringEntity(data);
			Logger.appendLog(data, true);
			//Sets the post request as the resulting string
			httpost.setEntity(se);
			//Sets a request header 
			httpost.setHeader("Accept", "application/json");
			httpost.setHeader("Content-type", "application/json");
			//Handles what is returned from the page 
			HttpResponse reply= httpclient.execute(httpost);
			//Thread.sleep(7000);
			BufferedReader BuffRead = new BufferedReader( new InputStreamReader(reply.getEntity().getContent(),"UTF-8") );
			String response = BuffRead.readLine();
			Logger.appendLog(response, true);
			return response;
		}
		catch(Exception e)
		{
			Log.e("PostData","Error in posting data: ");
			Logger.appendLog("Exception in posting data:"+e.getMessage() , true);
			//AppLog.logger(" Error in posting for " +path +" with data:" + data+" :"+ e.getMessage());
			e.printStackTrace();
			return "exception";
		}
		
	}

}
