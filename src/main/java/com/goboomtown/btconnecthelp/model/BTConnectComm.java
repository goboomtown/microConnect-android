/**
 * Created by larry on 2016-07-25.
 */

package com.goboomtown.btconnecthelp.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;


import com.goboomtown.btconnecthelp.api.JSONHelper;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by larry on 2015-12-11.
 */
public class BTConnectComm {

    public String       comm_id;
    public String       created;
    public String       external_id;
    public String       title;
    public Map<String, Object> participants_eligible;
//    public ArrayList<BoomtownUser> participants;
//
//    public BoomtownCall(){
//        participants = new ArrayList<BoomtownUser>();
//    }

    public BTConnectComm( JSONObject jsonObject ){
//        participants = new ArrayList<BoomtownUser>();
        populateCallFromJSONObject(jsonObject);
    }


    public void populateCallFromJSONObject( JSONObject jsonObject ) {
        if (jsonObject instanceof JSONObject)
        {
            created = jsonObject.optString("created");
            external_id = jsonObject.optString("external_id");
//            participants_eligible = jsonObject.optJSONObject("participants_eligible");
            JSONObject objComm = jsonObject.optJSONObject("comm");
            if (objComm instanceof JSONObject) {
                title = objComm.optString("title");
            }
            try {
                participants_eligible = JSONHelper.toMap(jsonObject.optJSONObject("participants_eligible"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

