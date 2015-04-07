package com.xrci.standup.utility;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by q4KV89ZB on 03-04-2015.
 */

public class OrganizationModel {
    String  orgURL = "http://64.49.234.131:8080/standup/rest/organisation/getList";
//    [{"id":1,"name":"XRCI"},{"id":2,"name":"XIL"}]
    public ArrayList<String> getOrgaizationList(){
        ArrayList<String> orgList = new ArrayList<>();
        String response = GetData.getContent(orgURL);
        if (response.equals(GetData.INVALID_RESPONSE) ||
                response.equals(GetData.EXCEPTION) || response.equals(GetData.EXCEPTION))
            return orgList;
        else {
            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0 ; i < jsonArray.length() ; i++) {
                    orgList.add(jsonArray.getJSONObject(i).getString("name"));
                }
            } catch (JSONException e) {
                e.printStackTrace();

            }
            return  orgList;
        }


    }
}
