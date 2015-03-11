package com.xrci.standup.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.xrci.standup.R;

/**
 * Created by Shahab on 3/20/14.
 */
public class CircleView
        extends AbstractBaseView {

    private int circleType;
    private int circleRadius = 20;
    private int strokeColor = Color.WHITE;
    private int strokeWidth = 0;
    private int fillColor = Color.RED;
    private int circleGap = 0;
    private String text_line1 = " abc", text_line2 = " abc";
    int textSize = 20;

    public CircleView(Context context) {
        super(context);

        init();
    }

    public CircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

        init();
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray aTypedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleView, defStyleAttr, 0);

        try {
            strokeColor = aTypedArray.getColor(R.styleable.CircleView_strokeColor, strokeColor);
            strokeWidth = aTypedArray.getDimensionPixelSize(R.styleable.CircleView_strokeWidth, strokeWidth);
            fillColor = aTypedArray.getColor(R.styleable.CircleView_fillColor, fillColor);
            circleRadius = aTypedArray.getDimensionPixelSize(R.styleable.CircleView_circleRadius, circleRadius);
            circleGap = aTypedArray.getDimensionPixelSize(R.styleable.CircleView_circleGap, circleGap);
            text_line1 = aTypedArray.getString(R.styleable.CircleView_text_line1);
            text_line2 = aTypedArray.getString(R.styleable.CircleView_text_line2);
            // System.out.println(" Color:"+strokeColor+ " fillColor:"+fillColor);
            //System.out.println("circleRadius" +circleRadius);
            textSize = circleRadius / 2;
        } finally {
            aTypedArray.recycle();
        }

        init();
    }

    public CircleView(Context context, int strokeColor, int strokeWidth, int fillColor, int circleRadius, int circleGap) {
        super(context);
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.fillColor = fillColor;
        this.circleRadius = circleRadius;
        this.circleGap = circleGap;
        textSize = circleRadius / 2;

        init();
    }

    public CircleView(Context context, int circleType, int timePeriod, int totalPeriod) {
        super(context);


    }

    public void init() {
        this.setMinimumHeight(circleRadius * 2 + strokeWidth);
        this.setMinimumWidth(circleRadius * 2 + strokeWidth);
        //this.setSaveEnabled(true);
        invalidate();
    }

    public float convertToDP(float px){
//        Resources resources = getResources().getSystem();
//        DisplayMetrics metrics = resources.getDisplayMetrics();
//        float dp = px / (metrics.densityDpi / 160f);
//        return dp;

        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, getResources().getDisplayMetrics());


    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = this.getWidth();
        int h = this.getHeight();

        int ox = w / 2;
        int oy = h / 2;
        //System.out.println(" Radius:"+circleRadius);

        canvas.drawCircle(convertToDP(ox), convertToDP(oy), convertToDP(circleRadius), getStroke());
        canvas.drawCircle(convertToDP(ox), convertToDP(oy), convertToDP(circleRadius - circleGap), getFill());
        canvas.drawText(text_line1, convertToDP(ox), convertToDP(oy), getTextPaint());
        canvas.drawText(text_line2, convertToDP(ox), convertToDP(oy + textSize), getTextPaint());

    }

    private Paint getStroke() {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStrokeWidth(strokeWidth);
        p.setColor(strokeColor);
        p.setStyle(Paint.Style.STROKE);
        return p;
    }

    private Paint getFill() {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        //System.out.println("Color in Fill:"+fillColor);
        p.setColor(fillColor);
        p.setStyle(Paint.Style.FILL);
        return p;
    }

    private Paint getTextPaint() {
        Paint p = new Paint();

        p.setColor(Color.WHITE);
        p.setStyle(Style.STROKE);
        p.setTextAlign(Align.CENTER);
        p.setTextSize(textSize);
        return p;
    }

    @Override
    protected int hGetMaximumHeight() {
        return circleRadius * 2 + strokeWidth;
    }

    @Override
    protected int hGetMaximumWidth() {
        return circleRadius * 2 + strokeWidth;
    }

    public int getCircleRadius() {
        return circleRadius;
    }

    public void setCircleRadius(int circleRadius) {
        this.circleRadius = circleRadius;
        textSize = circleRadius / 2;
        //invalidate();
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public void setTextLine1(String text) {
        this.text_line1 = text;
    }

    public String getTextLine1(){
        return this.text_line1;
    }

    public void setTextLine2(String text) {
        this.text_line2 = text;
    }


    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
        //invalidate();
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public int getFillColor() {
        return fillColor;
    }

    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
    }

    public int getCircleGap() {
        return circleGap;
    }

    public void setCircleGap(int circleGap) {
        this.circleGap = circleGap;
    }
}
