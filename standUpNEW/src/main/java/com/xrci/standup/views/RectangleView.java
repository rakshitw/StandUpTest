package com.xrci.standup.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.xrci.standup.R;

public class RectangleView extends AbstractBaseView
{ 
	
    
    private int fillColor = Color.GRAY;
    private int rectangleWidth=0;
    private int rectangleHeight=0;
    
    public RectangleView(Context context) {
        super(context);

        init();
    }

    public RectangleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

        init();
    }

    public RectangleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray aTypedArray = context.obtainStyledAttributes(attrs, R.styleable.RectangleView, defStyleAttr, 0);

        try
        {
        	  this.fillColor = aTypedArray.getColor(R.styleable.RectangleView_rectanglefillColor, fillColor);
              this.rectangleHeight=	aTypedArray.getDimensionPixelSize(R.styleable.RectangleView_rectangleHeight, rectangleHeight);
              this.rectangleWidth=	aTypedArray.getDimensionPixelSize(R.styleable.RectangleView_rectangleWidth, rectangleWidth);
              
        }
        
        finally
        {
        aTypedArray.recycle();
        }

        init();
    }

    public RectangleView(Context context, int fillColor) {
        super(context);
        
        this.fillColor=fillColor;
        init();
    }
    
   /* public CircleView(Context context, int circleType, int timePeriod, int totalPeriod)
    {
    	super(context);
    	
    	
    }
*/
    void init() {
        this.setMinimumHeight(rectangleHeight);
        this.setMinimumWidth(rectangleWidth);
        //this.setSaveEnabled(true);
        invalidate(); 
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = rectangleWidth;
        int h = rectangleHeight;
        
        //this.getBackground()
        //int ox = w/2;
        //int oy = h/2;
       // System.out.println(" Color:"+getFill().getColor());
        //System.out.println("W:"+rectangleWidth+"W:" +rectangleHeight);
       // canvas.drawCircle(ox, oy, circleRadius, getStroke());
        //canvas.drawCircle(ox, oy, circleRadius - circleGap, getFill());
        canvas.drawRect(new Rect(0,0,w,h), getFill());
    }

   

    private Paint getFill()
    {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        //System.out.println("Color in Fill:"+fillColor);
        p.setColor(fillColor);
        p.setStyle(Paint.Style.FILL);
        return p;
    }

   

    public int getFillColor() {
        return fillColor;
    }

    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
    }
    
    
    public int getRectangleWidth() {
        return rectangleWidth;
    }

    
    public void setRectangleWidth(int rectangleWidth) {
        this.rectangleWidth=rectangleWidth;
    }
    
    
    public int getRectangleHeight() {
        return rectangleHeight;
    }

    
    public void setRectangleHeight(int rectangleHeight) {
        this.rectangleHeight=rectangleHeight;
    }


	@Override
	protected int hGetMaximumHeight() {
		// TODO Auto-generated method stub
		return rectangleHeight;
	}

	@Override
	protected int hGetMaximumWidth() {
		// TODO Auto-generated method stub
		return rectangleWidth;
	}

    }
