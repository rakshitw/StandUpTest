package com.xrci.standup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import android.os.Environment;

public class Logger {
	
	
	public static File logFile;
	public static void appendLog(String text, boolean timestamp)
	{       
		//System.out.println("In Logger + check Uploading:"+Sync.uploading);
		
	   logFile = new File("/sdcard/standup/log.dat");
	   Calendar c=Calendar.getInstance();
	    
	   if (!logFile.exists())
	   {
		   try
           {
               File f = new File("/sdcard/standup");
               if(!f.exists()) f.mkdir();
               logFile.createNewFile();
             //  System.out.println("here is whattttt in logger");
           } 
           catch (IOException e)
           {
              // TODO Auto-generated catch block
                 //Toast.makeText(this, "Exception in creating file", Toast.LENGTH_LONG).show(); 
              e.printStackTrace();
           }
	   }
	   try
	   {
	      //BufferedWriter for performance, true to set append to file flag
	      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
	    if(timestamp)
	      buf.append("["+c.getTime()+"]:"+text);
	    else
	    	buf.append(text);
	      buf.append("\n");
	      buf.newLine();
	      buf.close();
	   }
	   catch (IOException e)
	   {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	     // AppLog.logger("Sync:Error in writing System File"+ e.getMessage());
	   }
	}
	

}
