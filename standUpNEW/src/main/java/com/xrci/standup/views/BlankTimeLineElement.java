package com.xrci.standup.views;

import com.xrci.standup.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

public class BlankTimeLineElement extends RelativeLayout {

	public BlankTimeLineElement(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		addView(inflater.inflate(R.layout.blank_timeslot, null));
		// TODO Auto-generated constructor stub
	}

}
