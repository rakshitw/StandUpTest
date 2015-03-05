package com.xrci.standup.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.DetectedActivity;
import com.xrci.standup.R;
import com.xrci.standup.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CompositeTimeLineElement extends RelativeLayout {
	
	
	CircleView circle;
	RectangleView rectangleHorizontal,rectangleVertical;
	TextView textViewStartTime,textViewEndTime;
	ImageView imageViewActivityLogo;
	int type;
	String text_line1="",text_line2="";
	int fillColor;
	int maxCircleRadius=100,minHorizontalRectangleWidth=20;
	int minCircleRadius=30,withMinCircleHorizontalRectangleWidth=100;
	
	
	int circleRadius,horizontalRectangleWidth, verticalRectangleHeight;

	public CompositeTimeLineElement(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		addView(inflater.inflate(R.layout.custom_layout, null));
	}
	
	
	//timePeriod in minutes 
	public CompositeTimeLineElement(Context context,int activity,long timePeriod,int steps,Date startTime,Date endTime, boolean showStartTime) {
		super(context);
		// TODO Auto-generated constructor stub
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		addView(inflater.inflate(R.layout.custom_layout, null));
		circle=(CircleView)findViewById(R.id.circleView1);
		rectangleHorizontal=(RectangleView)findViewById(R.id.rectangleViewHorizontal);
		rectangleVertical=(RectangleView)findViewById(R.id.rectangleViewVertical);
		textViewStartTime=(TextView)findViewById(R.id.textViewStartTime);
		textViewEndTime=(TextView)findViewById(R.id.textViewEndTime);
		imageViewActivityLogo=(ImageView)findViewById(R.id.imageViewLogo);
		
		setFillColor(activity);
		setDimensions(timePeriod,activity);
		setText(timePeriod,activity,steps);
		setImage(activity);
		renderDrawings();
		SimpleDateFormat sf=new SimpleDateFormat("HH:mm");
		textViewStartTime.setText(sf.format(startTime));
		textViewEndTime.setText(sf.format(endTime));
		if(!showStartTime)
		{
			textViewStartTime.setVisibility(View.INVISIBLE);
		}
		//rectangleHorizontal.invalidate();
	}
	
	
	
	private void renderDrawings() {
		// TODO Auto-generated method stub
		//System.out.println("")
		circle.setCircleRadius(circleRadius);
		circle.setFillColor(fillColor);
		circle.setTextLine1(text_line1);
		circle.setTextLine2(text_line2);
		rectangleHorizontal.setRectangleWidth(horizontalRectangleWidth);
		rectangleVertical.setRectangleHeight(verticalRectangleHeight);
		rectangleVertical.setFillColor(fillColor);
		rectangleHorizontal.init();
		rectangleVertical.init();
		circle.init();

		
		
		
		
	}
	
	private void setImage(int activity)
	{
		switch(activity)
		{
		case DetectedActivity.STILL:
			imageViewActivityLogo.setImageDrawable(getResources().getDrawable(R.drawable.still));
			break;
		case DetectedActivity.IN_VEHICLE:
			imageViewActivityLogo.setImageDrawable(getResources().getDrawable(R.drawable.vehicle));
			break;
		case DetectedActivity.ON_FOOT:
			imageViewActivityLogo.setImageDrawable(getResources().getDrawable(R.drawable.foot));
			break;
		case DetectedActivity.ON_BICYCLE:
			imageViewActivityLogo.setImageDrawable(getResources().getDrawable(R.drawable.bike));
			break;
		case utils.ACTIVITY_WORKING:
			imageViewActivityLogo.setImageDrawable(null);
			break;
		}
		
	}


	private void setText(long timePeriod, int activity, int steps) {
		// TODO Auto-generated method stub
		if(activity==DetectedActivity.ON_FOOT)
		{
			text_line1=steps+"";
			text_line2="steps";
		}
		else
		{
			text_line1=(int)timePeriod/(60*1000)+"";
			text_line2="mins";
			
		}
		
	}


	void setDimensions(long timePeriod,int activity)
	{
		int timePeriodinMinutes=(int)timePeriod/(60*1000);
		System.out.println("time Period in minutes:"+timePeriodinMinutes);
		
		if(activity==DetectedActivity.STILL||activity==DetectedActivity.IN_VEHICLE||activity==utils.ACTIVITY_WORKING)
			circleRadius=timePeriodinMinutes*3;
		else
			circleRadius=timePeriodinMinutes*15;
		horizontalRectangleWidth=200-circleRadius;
		
		if(circleRadius<=minCircleRadius)
		{
			circleRadius=minCircleRadius;
			horizontalRectangleWidth=withMinCircleHorizontalRectangleWidth;
		}
		else if(circleRadius>=maxCircleRadius)
		{
			circleRadius=maxCircleRadius;
			horizontalRectangleWidth=minHorizontalRectangleWidth;
		}
		verticalRectangleHeight=circleRadius*2;
		System.out.println("Circle radius:"+circleRadius);
		
		
	}
	
	
	
		void setFillColor(int activity)
		{
			switch(activity){
				case DetectedActivity.STILL:
				case DetectedActivity.IN_VEHICLE:
				case utils.ACTIVITY_WORKING:
					 fillColor= utils.COLOR_STILL;
					 break;
					 
				case DetectedActivity.ON_FOOT:
				case DetectedActivity.ON_BICYCLE:
					 fillColor= utils.COLOR_WALK;
					 break;
					
				//case DetectedActivity.ON_BICYCLE:
				//	 fillColor= utils.COLOR_BIKE;
				//	 break;
					
				//case DetectedActivity.IN_VEHICLE:
				//	 fillColor= utils.COLOR_VEHICLE;
				//	 break;

				default:
					 fillColor= utils.COLOR_UNKNOWN;
					 break;
			}
			
			
		}

}
