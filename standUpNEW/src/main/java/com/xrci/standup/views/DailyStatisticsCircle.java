package com.xrci.standup.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.google.android.gms.location.DetectedActivity;
import com.xrci.standup.utils;

public class DailyStatisticsCircle extends AbstractBaseView {
    public void setmRadius(int mRadius) {
        this.mRadius = mRadius;
    }

    //int circleRadius;

    int screenWidth = getResources().getDisplayMetrics().widthPixels;


	int mRadius=screenWidth/11;
	int strokeWidth=mRadius/5;

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    int textSize=mRadius/4;

    public void setCentertextColor(int centertextColor) {
        this.centertextColor = centertextColor;
    }

    int centertextColor = Color.BLACK;
	boolean drawStill=false,drawWalk=false,drawVehicle=false,drawOnBike=false,drawWork=false;
	float stillStartAngle,stillEndAngle,walkStartAngle,walkEndAngle,vehicleStartAngle,vehicleEndAngle,bikeStartAngle,bikeEndAngle,workStartAngle,workEndAngle;
	private String centerText = "Activ-O-Meter";


    public DailyStatisticsCircle(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	
	 public DailyStatisticsCircle(Context context, AttributeSet attrs) {
	        super(context, attrs);

	        init();
	    }


	@Override
	protected int hGetMaximumHeight() {
		// TODO Auto-generated method stub
		return mRadius*2 +strokeWidth;
	}

	@Override
	protected int hGetMaximumWidth() {
		// TODO Auto-generated method stub
		return mRadius *2 + strokeWidth;
	}

	
	
	public DailyStatisticsCircle(Context context,int stillTime,int walkingTime, int vehicleTime, int onBikeTime, int workingTime) {
		super(context);
		setArcStartEndAngles(stillTime,walkingTime,vehicleTime,onBikeTime,workingTime);
	}

	
	
	
	
     
     public void setArcStartEndAngles(int stillTime, int walkingTime,
			int vehicleTime, int onBikeTime,int workingTime) {
    	 drawStill=drawWalk=drawVehicle=drawOnBike=false;
    	 int totalTime = stillTime+walkingTime+vehicleTime+onBikeTime+workingTime;
    	 //System.out.println("total:"+totalTime);
    	 float prevAngle=0;
    	 if(stillTime>0)
    	 {
    		 drawStill=true;
    		 stillStartAngle=prevAngle;
    		 //System.out.println("Still tIme:"+stillTime);
    		 stillEndAngle=(stillTime*360)/totalTime;
    		// System.out.println("Still end angle:"+stillEndAngle);
    		 prevAngle=prevAngle+stillEndAngle; 
    	 }
    	 if(workingTime>0)
    	 {
    		 drawWork=true;
 			workStartAngle=prevAngle;
 			workEndAngle=(workingTime*360)/totalTime;
 			prevAngle=prevAngle+workEndAngle; 
    		 
    	 }
    	 if(vehicleTime>0)
 		{
 			drawVehicle=true;
 			vehicleStartAngle=prevAngle;
 			vehicleEndAngle=(vehicleTime*360)/totalTime;
 			prevAngle=prevAngle+vehicleEndAngle; 
 		}
		if(walkingTime>0)
		{
			drawWalk=true;
			walkStartAngle=prevAngle;
			walkEndAngle=(walkingTime*360)/totalTime;
			prevAngle=prevAngle+walkEndAngle; 
		}
		
		if(onBikeTime>0)
			
		{
			drawOnBike=true;
			bikeStartAngle=prevAngle;
			bikeEndAngle=(onBikeTime*360)/totalTime;
			prevAngle=bikeEndAngle; 
		}

    	 
	}

	public void init() {
         this.setMinimumHeight((mRadius*2)+strokeWidth);
         this.setMinimumWidth((mRadius*2)+strokeWidth);
        		 
         //this.setSaveEnabled(true);
         invalidate(); 
     }
     
     public void setCenterText(String text) {
         this.centerText = text;
     }
     
     
     @Override
     public void onDraw(Canvas canvas) {
         super.onDraw(canvas);

        // int w = this.getWidth();
         //int h = this.getHeight();
         
         final RectF rect = new RectF();
         
         //Example values
         rect.set(0+strokeWidth,0+strokeWidth,mRadius * 2,mRadius * 2); 
        if(drawStill)
        {
        	//System.out.println("Came in drawStill  " + stillStartAngle +"   " +stillEndAngle);
        	canvas.drawArc(rect, stillStartAngle, stillEndAngle, false, getStrokePaint(DetectedActivity.STILL));
       
        }
        if(drawWalk)
        {
        	canvas.drawArc(rect, walkStartAngle, walkEndAngle, false, getStrokePaint(DetectedActivity.ON_FOOT));
        }
        if(drawVehicle)
        {
        	canvas.drawArc(rect, vehicleStartAngle, vehicleEndAngle, false, getStrokePaint(DetectedActivity.IN_VEHICLE));
        }
        if(drawOnBike)
        {
        	canvas.drawArc(rect, bikeStartAngle, bikeEndAngle, false, getStrokePaint(DetectedActivity.ON_BICYCLE));
            
        }
        if(drawWork)
        {
        	canvas.drawArc(rect, workStartAngle, workEndAngle, false, getStrokePaint(utils.ACTIVITY_WORKING));
            	
        }
        
        canvas.drawText(centerText, mRadius+10,mRadius+10 ,getTextPaint() );
        
     }




     Paint  getStrokePaint(int activity)
     {
    	 int strokeColor = 0xff000000;
    	 switch(activity)
    	 {
    	 case DetectedActivity.STILL:
             strokeColor=utils.COLOR_STILL;
             break;
    	 case utils.ACTIVITY_WORKING:
    		 strokeColor=utils.COLOR_UNKNOWN;
    		 break;
    	 case DetectedActivity.ON_FOOT:
    		 strokeColor=utils.COLOR_WALK;
    		 break;
    	 case DetectedActivity.ON_BICYCLE:
    		 strokeColor=utils.COLOR_BIKE;
    		 break;
    	 case DetectedActivity.IN_VEHICLE:
    		 strokeColor=utils.COLOR_VEHICLE;
             break;
    	 }
    	 
    	Paint paint=new Paint();
    	paint.setColor(strokeColor);
    	paint.setStrokeWidth(strokeWidth);
    	paint.setAntiAlias(true);
    	paint.setStrokeCap(Paint.Cap.BUTT);
    	paint.setStyle(Paint.Style.STROKE);
    	return paint;
    		 
    	 
    	 
    	 
     }
     
     
     private Paint getTextPaint()
     {
         Paint p = new Paint();
        
         p.setColor(centertextColor);
         p.setStyle(Style.STROKE);
         p.setTextAlign(Align.CENTER);
         p.setTextSize(textSize);
         return p;
     }

}
