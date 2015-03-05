package com.xrci.standup.views;

import java.util.logging.Logger;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Shahab on 3/20/14.
 * Abstract Base view class
 * Root class for all of our custom views
 */
public abstract class AbstractBaseView
        extends View {
    public AbstractBaseView(Context context) {
        super(context);
    }


    /**
     * called by the layout inflater.
     * use {@link android.content.res.TypedArray} to read custom attributes
     *
     * @param context current context
     * @param attrs   xml attributes
     */
    public AbstractBaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    /**
     * This is not called by the layout inflater but a derived class.
     * A derived view when invoked by the layout inflater may choose to
     * pass another attribute set reference from its theme whose
     * resource id is defStyle.
     *
     * @param context      current context
     * @param attrs        xml attributes
     * @param defStyleAttr style resource
     */
    public AbstractBaseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     * Called by measure() of the base view class.
     * This method is called only if a layout is requested on this child.
     * Return after setting the required size through setMeasuredDimension
     * The default onMeasure from View may be sufficient for you.
     * But if you allow using wrap_content for this view you may want to
     * override the wrap_content behavior which by default takes the entire space
     * supplied. The following logic implements this properly by overriding
     * onMeasure. This should be suitable for a number of cases, otherwise
     * you have all the logic here.
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getImprovedDefaultWidth(widthMeasureSpec),
                getImprovedDefaultHeight(heightMeasureSpec));
    }


    /**
     * {@link android.view.View.MeasureSpec#UNSPECIFIED}
     * used for scrolling
     * means you can be as big as you would like to be
     * return the maximum comfortable size
     * it is ok if you are bigger because scrolling may be on
     *
     * {@link android.view.View.MeasureSpec#EXACTLY}
     * You have indicated your explicit size in the layout
     * or you said match_parent with the parents exact size
     * return back the passed in size
     *
     * {@link android.view.View.MeasureSpec#AT_MOST}
     * I have this much space to spare
     * sent when wrap_content
     * Take as much as you think your natural size is
     * this is a bit misleading
     * you are advised not to take all the size
     * you should be smaller and return a preferred size
     * if you don't and take all the space, the other siblings will get lost.
     * In this implementation I used the minimum size to satisfy at-most.
     * @param measureSpec
     * @return
     */
    private int getImprovedDefaultHeight(int measureSpec) {

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                return hGetMaximumHeight();

            case MeasureSpec.EXACTLY:
                return specSize;

            case MeasureSpec.AT_MOST:
                return hGetMinimumHeight();
        }

        // you shouldn't come here
        //Logger.e("Unknown spec mode");

        return specSize;
    }

    private int getImprovedDefaultWidth(int measureSpec) {

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                return hGetMaximumWidth();

            case MeasureSpec.EXACTLY:
                return specSize;

            case MeasureSpec.AT_MOST:
                return hGetMinimumWidth();
        }

        // you shouldn't come here
        //Logger.e("Unknown spec mode");

        return specSize;
    }

    /**
     * Override these methods to provide a maximum size
     * "h" stands for hook pattern
     * @return
     */
    abstract protected int hGetMaximumHeight();

    abstract protected int hGetMaximumWidth();

    protected int hGetMinimumHeight() {
        return this.getSuggestedMinimumHeight();
    }

    protected int hGetMinimumWidth() {
        return this.getSuggestedMinimumWidth();
    }
}
