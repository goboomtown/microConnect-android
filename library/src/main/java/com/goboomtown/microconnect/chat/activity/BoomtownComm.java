package com.goboomtown.microconnect.chat.activity;

import android.os.Parcel;
import android.os.Parcelable;

import com.goboomtown.microconnect.chat.chat.JSONHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

//import com.goboomtown.util.JSONHelper;

/**
 * @author larry on 2015-12-11
 * @author fbeachler
 */
public class BoomtownComm implements Parcelable {

    public static final int kTypeVideo = 1;
    public static final int kTypeChat = 2;

    public int comm_type;
    public String comm_id;
    public String created;
    public String external_id;
    public String title;
    public Map<String, Object> participants_eligible;

    public String call_id;
    public String oovoo_conference_id;
    public String video_conference_id;
    public int duration;
    public String subject;
    public String status;
    public String entity_of;
    public String enable_video;
    public String entity_name;
    public String entity_id;
    public String related_to;
    public String related_id;
    public String related_name;
    public String preamble;
    public String preamble_url;

    public BoomtownComm(JSONObject jsonObject) {
        populateCallFromJSONObject(jsonObject);
    }

    protected BoomtownComm(Parcel in) {
        comm_type = in.readInt();
        comm_id = in.readString();
        created = in.readString();
        external_id = in.readString();
        title = in.readString();
        call_id = in.readString();
        oovoo_conference_id = in.readString();
        video_conference_id = in.readString();
        duration = in.readInt();
        subject = in.readString();
        status = in.readString();
        entity_of = in.readString();
        enable_video = in.readString();
        entity_name = in.readString();
        entity_id = in.readString();
        related_to = in.readString();
        related_id = in.readString();
        related_name = in.readString();
        preamble = in.readString();
        preamble_url = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(comm_type);
        dest.writeString(comm_id);
        dest.writeString(created);
        dest.writeString(external_id);
        dest.writeString(title);
        dest.writeString(call_id);
        dest.writeString(oovoo_conference_id);
        dest.writeString(video_conference_id);
        dest.writeInt(duration);
        dest.writeString(subject);
        dest.writeString(status);
        dest.writeString(entity_of);
        dest.writeString(enable_video);
        dest.writeString(entity_name);
        dest.writeString(entity_id);
        dest.writeString(related_to);
        dest.writeString(related_id);
        dest.writeString(related_name);
        dest.writeString(preamble);
        dest.writeString(preamble_url);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BoomtownComm> CREATOR = new Creator<BoomtownComm>() {
        @Override
        public BoomtownComm createFromParcel(Parcel in) {
            return new BoomtownComm(in);
        }

        @Override
        public BoomtownComm[] newArray(int size) {
            return new BoomtownComm[size];
        }
    };

    public void populateCallFromJSONObject(JSONObject jsonObject) {
        if (jsonObject instanceof JSONObject) {
            created = jsonObject.optString("created");
            external_id = jsonObject.optString("external_id");
//            participants_eligible = jsonObject.optJSONObject("participants_eligible");
            JSONObject objComm = jsonObject.optJSONObject("comm");
            if (objComm instanceof JSONObject) {
                comm_id = objComm.optString("id");
                title = objComm.optString("title");
                comm_type = kTypeChat;

                call_id = objComm.optString("call_id");
                if (!call_id.isEmpty()) {
                    comm_id = call_id;
                }
                oovoo_conference_id = objComm.optString("oovoo_conference_id");
                video_conference_id = objComm.optString("video_conference_id");
                if (!video_conference_id.isEmpty()) {
                    comm_type = kTypeVideo;
                }
                created = objComm.optString("created");
                duration = objComm.optInt("duration");
                subject = objComm.optString("subject");
                status = objComm.optString("status");
                entity_of = objComm.optString("entity_of");
                enable_video = objComm.optString("enable_video");
                entity_id = objComm.optString("entity_id");
                entity_name = objComm.optString("entity_name");
                related_to = objComm.optString("related_to");
                related_id = objComm.optString("related_id");
                related_name = objComm.optString("related_name");
                preamble = objComm.optString("preamble");
                preamble_url = objComm.optString("preamble_url");
            }

            JSONObject objcall = jsonObject.optJSONObject("call");
            if (objcall instanceof JSONObject) {
                call_id = objcall.optString("call_id");
                if (!call_id.isEmpty()) {
                    comm_id = call_id;
                }
                oovoo_conference_id = objcall.optString("oovoo_conference_id");
                video_conference_id = objcall.optString("video_conference_id");
                created = objcall.optString("created");
                duration = objcall.optInt("duration");
                subject = objcall.optString("subject");
                status = objcall.optString("status");
                entity_of = objcall.optString("entity_of");
                enable_video = objcall.optString("enable_video");
                entity_id = objcall.optString("entity_id");
                entity_name = objcall.optString("entity_name");
                related_to = objcall.optString("related_to");
                related_id = objcall.optString("related_id");
                related_name = objcall.optString("related_name");
                preamble = objcall.optString("preamble");
                preamble_url = objcall.optString("preamble_url");
            }

            try {
                JSONObject participantsEligibleList = jsonObject.optJSONObject("participants_eligible");
                if (participantsEligibleList instanceof JSONObject) {
                    participants_eligible = JSONHelper.toMap(participantsEligibleList);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public Boolean isChat() {
        return comm_type == kTypeChat;
    }

    public Boolean isVideo() {
        return comm_type == kTypeVideo;
    }

}
