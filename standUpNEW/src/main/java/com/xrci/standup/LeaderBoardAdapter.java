package com.xrci.standup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.facebook.widget.ProfilePictureView;
import com.xrci.standup.utility.LeaderBoardModel;

import java.util.List;

/**
 * Created by q4KV89ZB on 23-03-2015.
 */
public class LeaderBoardAdapter extends ArrayAdapter<LeaderBoardModel> {

    public LeaderBoardAdapter(Context context, int resource, List<LeaderBoardModel> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {

            LayoutInflater viewInflator;
            viewInflator = LayoutInflater.from(getContext());
            view = LayoutInflater.from(getContext()).inflate(R.layout.leaderboard_list_item, null);
        }

        LeaderBoardModel leaderBoardModel = getItem(position);
        if(leaderBoardModel != null) {
            if(leaderBoardModel.getAuthType().equalsIgnoreCase("facebook")) {

                ProfilePictureView profilePictureView = (ProfilePictureView) view.findViewById(R.id.leader_profile_pic);
                profilePictureView.setCropped(true);
                profilePictureView.setProfileId(leaderBoardModel.getAuthId());

            }
            TextView nameView = (TextView) view.findViewById(R.id.list_leader_name_textview);
            nameView.setText(leaderBoardModel.getName());
            TextView stepView = (TextView) view.findViewById(R.id.list_item_steps_textview);
            stepView.setText(Integer.toString(leaderBoardModel.getSteps()));
        }

        return view;

    }
}
