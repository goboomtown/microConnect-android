package com.goboomtown.chat;

import android.util.Log;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by larry on 2015-12-16.
 */
public class BoomtownChatMessage {
    public String id;
    public String type;
    public String from;
    public String fromName;
    public String fromMention;
    public String text;
    public Date date;
    public String dateString;
    public String xml;
    public String avatar;
    public String preview;
    public String url;
    public boolean self;
    public DelayInformation delayInformation;

    public BoomtownChatMessage() {

    }

    public void populateFromMessage(Message message) {
        if (message.hasStanzaIdSet())
            this.id = message.getStanzaId();
        else {
//            this.id = java.util.UUID.randomUUID().toString();
        }

        String body = message.getBody();
        String from = message.getFrom();
        if (from == null)
            from = "";
        String[] tokens = from.split("/");
        String resource;
        if (tokens.length > 1)
            resource = tokens[1];
        else
            resource = tokens[0];
        tokens = resource.split(";");
        this.from = tokens[0];
        this.type = message.getType().toString();
        this.text = body;
        this.xml = message.toString();
        //  <delay xmlns='urn:xmpp:delay' stamp='2016-04-14T18:02:05.443+00:00' from='comm.5unzbf@conference.xmpp.dev.goboomtown.com/users:RMG;jko4v2'></delay>
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        if (message.hasExtension("delay", "urn:xmpp:delay")) {
            delayInformation = (DelayInformation) message.getExtension("delay", "urn:xmpp:delay");
            this.date = delayInformation.getStamp();
        } else
            this.date = new Date();
        this.dateString = sdf.format(this.date);
    }

    public void populateFromString(String message) {
        Map<String, Object> entry;
        try {
            JSONObject jsonObject = new JSONObject(message);
            entry = JSONHelper.toMap(jsonObject);
            this.id = (String) entry.get("id");
            this.type = (String) entry.get("type");
            this.from = (String) entry.get("from");
            this.fromName = (String) entry.get("fromName");
            this.fromMention = (String) entry.get("fromMention");
            this.text = (String) entry.get("text");
            this.xml = (String) entry.get("xml");
            this.preview = (String) entry.get("preview");
            this.url = (String) entry.get("url");
            this.dateString = (String) entry.get("dateString");

            if (this.dateString == null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                this.date = new Date();
                this.dateString = sdf.format(this.date);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static final String kPayloadTypePlain = "plain";
    private static final String kPayloadTypeSms = "sms";
    private static final String kPayloadTypeNotification = "notification";
    private static final String kPayloadTypeAttachment = "attachment";

    public Boolean postprocessMessage() {
        String secretPlain = md5(kPayloadTypePlain);
        String secretSms = md5(kPayloadTypeSms);
        String secretNotification = md5(kPayloadTypeNotification);
        String secretAttachment = md5(kPayloadTypeAttachment);
        Boolean rc = false;

        JSONObject object = null;
        try {
            object = new JSONObject(text);
            if (object instanceof JSONObject) {
                String secret = object.optString("secret");
                if (secret != null && (secret.equals(secretPlain) || secret.equals(secretNotification) || secret.equals(secretAttachment))) {
                    this.text = "";
                    String text = object.optString("message");
                    if (text != null && !text.equals("null"))
                        this.text = text;
                    else
                        this.text = "";
                    JSONObject context = object.optJSONObject("context");
                    if (context != null) {
                        String from = context.optString("from");
                        if (from != null && !from.equalsIgnoreCase("null"))
                            this.from = from;

                        String fromName = context.optString("from_name");
                        if (fromName != null)
                            this.fromName = fromName;

                        String avatar = context.optString("from_avatar");
                        if (avatar != null)
                            this.avatar = avatar;
                    }
                    JSONObject attachment = object.optJSONObject("attachment");
                    if (attachment != null) {
                        String preview = attachment.optString("preview");
                        if (preview != null && !preview.equalsIgnoreCase("null"))
                            this.preview = preview;

                        String url = attachment.optString("url");
                        if (url != null && !url.equalsIgnoreCase("null"))
                            this.url = url;
                    }
                    Log.d("postprocessMessage", this.from + ":" + this.text);
                    rc = true;
                    return rc;

                }
                rc = false;
                return rc;
            }
            rc = true;
            return rc;
        } catch (JSONException e) {
            rc = true;
        }
        return rc;
    }


    public void calculateId() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String idString = String.format("%s%s%s", this.from, this.text, sdf.format(this.date));
            id = md5(idString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", this.id);
            jsonObject.put("type", this.type);
            jsonObject.put("from", this.from);
            jsonObject.put("fromName", this.fromName);
            jsonObject.put("fromMention", this.fromMention);
            jsonObject.put("text", this.text);
            jsonObject.put("dateString", this.dateString);
            jsonObject.put("xml", this.xml);
            jsonObject.put("preview", this.preview);
            jsonObject.put("url", this.url);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public String getDateString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    public void trimText() {
        if (text != null)
            text = text.trim();
    }

    public String getMessage() {
        return text;
    }

    public String getName() {
        return fromName;
    }

    public String getDate() {
        if (dateString != null)
            return dateString;
        else {
            date = new Date();
            return DateFormat.getDateTimeInstance().format(date);
        }
    }


    public String getHumanDate() {
        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        if (dateString != null) {
            try {
                date = sdf.parse(dateString);
                sdf.applyPattern("LLL d, yyyy KK:mm a");
                return sdf.format(date);
            } catch (ParseException ex) {
            }
        } else {
            date = new Date();
            return DateFormat.getDateTimeInstance().format(date);
        }
        if (date == null)
            date = new Date();
        sdf.applyPattern("LLL d, yyyy KK:mm a");
        return sdf.format(date);
    }

    public boolean isMe() {
        return self;
    }
}

