package com.goboomtown.btconnecthelp.model;

import org.json.JSONObject;

/**
 * Created by larry on 2016-07-21.
 */
public class BTConnectIssue {
    public String  arrival_time;
    public String  category;
    public String  created;
    public String  departure_time;
    public String  details;
    public String  enroute_time;
    public String  id;
    public String  job;
    public String  members_email;
    public String  members_id;
    public String  members_locations_id;
    public String  members_locations_name;
    public String  members_name;
    public String  members_users_email;
    public String  members_users_id;
    public String  members_users_name;
    public String  reference_num;
    public String  remote_id;
    public String  resolution;
    public String  scheduled_time;
    public String  status;
    public String  type;
    public String  updated;
    public String  xmpp_data;


    public BTConnectIssue() {
        clear();
    }

    public BTConnectIssue(JSONObject issueJSON) {
        clear();
        populateFromJSON(issueJSON);
    }

    private void populateFromJSON(JSONObject issueJSON) {
        arrival_time 			=   issueJSON.optString("arrival_time");
        category 				=   issueJSON.optString("category");;
        created 				=   issueJSON.optString("created");;
        departure_time 		    =   issueJSON.optString("departure_time");;
        details 				=   issueJSON.optString("details");;
        enroute_time			=   issueJSON.optString("enroute_time");;
        id 					    =   issueJSON.optString("id");;
        job 					=   issueJSON.optString("job");;
        members_email 			=   issueJSON.optString("members_email");;
        members_id				=   issueJSON.optString("members_id");;
        members_locations_id 	=   issueJSON.optString("members_locations_id");;
        members_locations_name	=   issueJSON.optString("members_locations_name");;
        members_name 			=   issueJSON.optString("members_name");;
        members_users_email 	=   issueJSON.optString("members_users_email");;
        members_users_id 		=   issueJSON.optString("members_users_id");;
        members_users_name 	    =   issueJSON.optString("members_users_name");;
        reference_num 			=   issueJSON.optString("reference_num");;
        remote_id 				=   issueJSON.optString("remote_id");;
        resolution 			    =   issueJSON.optString("resolution");;
        scheduled_time			=   issueJSON.optString("scheduled_time");;
        status 				    =   issueJSON.optString("status");;
        type 					=   issueJSON.optString("type");;
        updated 				=   issueJSON.optString("updated");;
        xmpp_data				=   issueJSON.optString("xmpp_data");;
    }

    private void clear() {
        arrival_time 			=   null;
        category 				=   null;
        created 				=   null;
        departure_time 		    =   null;
        details 				=   null;
        enroute_time			=   null;
        id 					    =   null;
        job 					=   null;
        members_email 			=   null;
        members_id				=   null;
        members_locations_id 	=   null;
        members_locations_name	=   null;
        members_name 			=   null;
        members_users_email 	=   null;
        members_users_id 		=   null;
        members_users_name 	    =   null;
        reference_num 			=   null;
        remote_id 				=   null;
        resolution 			    =   null;
        scheduled_time			=   null;
        status 				    =   null;
        type 					=   null;
        updated 				=   null;
        xmpp_data				=   null;
    }
}


