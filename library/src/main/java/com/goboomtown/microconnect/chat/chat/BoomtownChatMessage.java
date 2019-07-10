package com.goboomtown.microconnect.chat.chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jxmpp.jid.Jid;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by larry on 2015-12-16.
 */
public class BoomtownChatMessage {
    public static final String TAG = BoomtownChatMessage.class.getSimpleName();

    public enum BTMessageType {
        kTypeVideoChat(0),
        kTypeAttachment(1),
        kTypeKB(2),
        kTypeOther(3);

        private int type = 0;

        BTMessageType(int type) {
            this.type = type;
        }
    }

    public static final String UNDERSCORE = "_";
    public static final String SLASH = "/";
    public static final String NULLSTR = "null";
    public static final String BLANK = "";

    public static final String ASSET_PATH_EMOTICONS = "emoticons";
    public static final String EMOTICON = "emoticon";

    public static final TimeZone TZ_UTC = TimeZone.getTimeZone("UTC");

    public static final String JSON_KEY_LBL = "lbl";
    public static final String JSON_KEY_KEY = "key";
    public static final String JSON_KEY_URI = "uri";
    public static final String JSON_KEY_SECRET = "secret";
    public static final String JSON_KEY_MESSAGE = "message";
    public static final String JSON_KEY_CONTEXT = "context";
    public static final String JSON_KEY_FROM = "from";
    public static final String JSON_KEY_FROM_NAME = "from_name";
    public static final String JSON_KEY_ATTACHMENT = "attachment";
    public static final String JSON_KEY_ACTIONS = "actions";
    public static final String JSON_KEY_KB = "kb";
    public static final String JSON_KEY_ID = "id";
    public static final String JSON_KEY_HASH = "hash";
    public static final String JSON_KEY_TYPE = "type";
    public static final String JSON_KEY_FROMNAME = "fromName";
    public static final String JSON_KEY_FROMMENTION = "fromMention";
    public static final String JSON_KEY_FROMNICKNAME = "fromNickname";
    public static final String JSON_KEY_CALLID = "callId";
    public static final String JSON_KEY_CHANNEL = "channel";
    public static final String JSON_KEY_SERVICE = "service";
    public static final String JSON_KEY_TEXT = "text";
    public static final String JSON_KEY_XML = "xml";
    public static final String JSON_KEY_PREVIEW = "preview";
    public static final String JSON_KEY_URL = "url";
    public static final String JSON_KEY_DATESTRING = "dateString";
    public static final String JSON_KEY_FROM_USER = "from_user";
    public static final String JSON_KEY_FROM_AVATAR = "from_avatar";
    public static final String JSON_KEY_KB_TITLE = "title"; // (always first 2k characters/converted from HTML)
    public static final String JSON_KEY_KB_PREVIEW = "preview"; // (always first 2k characters/converted from HTML)
    public static final String JSON_KEY_KB_PREVIEW_LENGTH = "preview_length"; // (user-specified # of preview characters)
    public static final String JSON_KEY_KB_ID = "id";

    /*
    secretPlain         ac7938d40cfc2307e2bf325d28e7884e
    secretSms           18b43c6a536a8fe1362f7a3887936be6
    secretNotification  0cfd653d5d3e1e9fdbb644523d77971d
    secretAttachment    44290cefe42924d04a92d99428a95f27
    secretVideoChat     231012598c936a405fb8c62e295222e8
     */
    public static final Map<PayloadType, String> PAYLOAD_SECRETS = new HashMap<>();
    public static final String REGEX_HTTP_URL = "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)";

    static {
        for (PayloadType pt : PayloadType.values()) {
            Log.d(TAG, pt + ": " + pt.generateSecret());
            PAYLOAD_SECRETS.put(pt, pt.generateSecret());
        }
    }

    public String id;
    public String hash;
    public String type;
    public String from;
    public String fromName;
    public String fromMention;
    public String fromNickname;
    public String callId;
    public String channel;
    public String service;
    public String text;
    public Spannable dispMessage;
    public Date date;
    public String dateString;
    public String xml;
    public String avatar;
    public String preview;
    public String url;
    public boolean self;
    public DelayInformation delayInformation;
    private JSONArray actions;

    public String   kb_title; // (always first 2k characters/converted from HTML)
    public String   kb_preview; // (always first 2k characters/converted from HTML)
    public int      kb_preview_length; // (user-specified # of preview characters)
    public String   kb_id;

    public BTMessageType messageType;

    public BoomtownChatMessage() {
    }

    public JSONArray getActions() {
        return actions;
    }

    public void populateFromMessage(Message message) {
        if (message.hasStanzaIdSet()) {
            this.id = message.getStanzaId();
            this.hash = message.getStanzaId();
        } else {
        }

        String body = message.getBody();
        Jid from = message.getFrom();
        String[] tokens = from.asUnescapedString().split("/");
        String resource;
        if (tokens.length > 1)
            resource = tokens[1];
        else
            resource = tokens[0];
        tokens = resource.split(";");
        this.from = tokens[0];

        this.type = message.getType().toString();
        this.text = body;
        this.dispMessage = new SpannableString(body);
        this.xml = message.toString();

        //  <delay xmlns='urn:xmpp:delay' stamp='2016-04-14T18:02:05.443+00:00' from='comm.5unzbf@conference.xmpp.dev.gizmocreative.com/users:RMG;jko4v2'></delay>
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        if (message.hasExtension("delay", "urn:xmpp:delay")) {
            delayInformation = (DelayInformation) message.getExtension("delay", "urn:xmpp:delay");
            this.date = delayInformation.getStamp();
        } else {
            this.date = new Date();
        }
        this.dateString = sdf.format(this.date);
    }

    public void populateFromString(JSONObject message) {
        if (message == null) {
            return;
        }
        Map<String, Object> entry;
        try {
            entry = JSONHelper.toMap(message);
            this.id = (String) entry.get(JSON_KEY_ID);
            this.hash = (String) entry.get(JSON_KEY_HASH);
            this.type = (String) entry.get(JSON_KEY_TYPE);
            this.from = (String) entry.get(JSON_KEY_FROM);
            this.fromName = (String) entry.get(JSON_KEY_FROMNAME);
            this.fromMention = (String) entry.get(JSON_KEY_FROMMENTION);
            this.fromNickname = (String) entry.get(JSON_KEY_FROMNICKNAME);
            this.callId = (String) entry.get(JSON_KEY_CALLID);
            this.channel = (String) entry.get(JSON_KEY_CHANNEL);
            this.service = (String) entry.get(JSON_KEY_SERVICE);
            this.text = (String) entry.get(JSON_KEY_TEXT);
            this.dispMessage = new SpannableString(this.text);
            this.xml = (String) entry.get(JSON_KEY_XML);
            this.preview = (String) entry.get(JSON_KEY_PREVIEW);
            this.url = (String) entry.get(JSON_KEY_URL);
            this.dateString = (String) entry.get(JSON_KEY_DATESTRING);
            this.kb_title = (String) entry.get(JSON_KEY_KB_TITLE);
            this.kb_preview = (String) entry.get(JSON_KEY_KB_PREVIEW);
            ArrayList actions = (ArrayList) entry.get(JSON_KEY_ACTIONS);
            if ( actions != null ) {
                this.actions = new JSONArray(actions);
            }
//            this.kb_preview_length = (int) entry.get(JSON_KEY_KB_PREVIEW_LENGTH);
            this.kb_id = (String) entry.get(JSON_KEY_KB_ID);

            if (this.dateString == null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
                sdf.setTimeZone(TZ_UTC);
                this.date = new Date();
                this.dateString = sdf.format(this.date);
            }

            determineMessageType();
        } catch (JSONException e) {
            Log.w(TAG, Log.getStackTraceString(e));
        }
    }

    /*
D/BoomtownChatMessage: PLAIN: ac7938d40cfc2307e2bf325d28e7884e
D/BoomtownChatMessage: SMS: 18b43c6a536a8fe1362f7a3887936be6
D/BoomtownChatMessage: SMS_ATTACHMENT: 06baebc7a6683f977a8c9bc191456a57
D/BoomtownChatMessage: NOTIFICATION: 0cfd653d5d3e1e9fdbb644523d77971d
D/BoomtownChatMessage: ATTACHMENT: 44290cefe42924d04a92d99428a95f27
D/BoomtownChatMessage: VIDEO_CHAT: 231012598c936a405fb8c62e295222e8
D/BoomtownChatMessage: EXTERNAL_CHAT: 5c51f890b2d0ae2888686f7a3c2ddb85
D/BoomtownChatMessage: EMAIL_MESSAGE: eb9b31d134455b695a77f4e88095f355
D/BoomtownChatMessage: EMAIL_MESSAGE_KNOWN: 4ddae1c8d316f8199fd9f4c6f71a3203
D/BoomtownChatMessage: DATA: 8d777f385d3dfec8815d20f7496026dc
D/BoomtownChatMessage: EXTENDED: d3e78e3d3b68cb0fbd9f66dcaef93cea
D/BoomtownChatMessage: FACEBOOK: 26cae7718c32180a7a0f8e19d6d40a59
     */

    public Boolean postprocessMessage() {
        Boolean retCode = false;

        JSONObject object = null;
        String secret = null;
        String text = this.text;
        JSONObject context = null;
        String from = null;
        String fromName = null;
        String callId = null;
        JSONObject attachment = null;
        String preview = null;
        String url = null;
        JSONArray actions = null;
        JSONObject kb = null;
        if (text == null) {
            return false;
        }
        try {
            object = new JSONObject(text);
        } catch (JSONException e) {
            retCode = true;
            // Log.d(TAG, "error marshalling chat message  (" + text + "): " + e.getMessage());
        }
        if (object instanceof JSONObject) {
            secret = object.optString(JSON_KEY_SECRET);
            text = object.optString(JSON_KEY_MESSAGE);
            context = object.optJSONObject(JSON_KEY_CONTEXT);
            actions = object.optJSONArray(JSON_KEY_ACTIONS);
            kb = object.optJSONObject(JSON_KEY_KB);
            if (context != null) {
                from = context.optString(JSON_KEY_FROM);
                fromName = context.optString(JSON_KEY_FROM_NAME);
            }
            attachment = object.optJSONObject(JSON_KEY_ATTACHMENT);
            if (attachment != null) {
                preview = attachment.optString(JSON_KEY_PREVIEW);
                url = attachment.optString(JSON_KEY_URL);
            }
            if (secret == null) {
                retCode = false;
            } else if (PAYLOAD_SECRETS.get(PayloadType.VIDEO_CHAT).equals(secret)) {
                from = context.optString(JSON_KEY_FROM_USER);
                callId = context.optString(JSON_KEY_FROM);
                this.dispMessage = new SpannableString("");
                if (text != null && !text.trim().equals(NULLSTR)) {
                    this.dispMessage = new SpannableString(text);
                }
                if (context != null) {
                    if (from != null && !from.trim().equalsIgnoreCase(NULLSTR)) {
                        this.from = from;
                    }
                    if (fromName != null) {
                        this.fromName = fromName;
                    }
                    if (callId != null && !callId.trim().equalsIgnoreCase(NULLSTR)) {
                        this.callId = callId;
                    }
                    String avatar = context.optString(JSON_KEY_FROM_AVATAR);
                    if (avatar != null) {
                        this.avatar = avatar;
                    }
                }
                if (attachment != null) {
                    if (preview != null && !preview.trim().equalsIgnoreCase(NULLSTR)) {
                        this.preview = preview;
                    }
                    if (url != null && !url.trim().equalsIgnoreCase(NULLSTR)) {
                        this.url = url;
                    }
                }
                retCode = true;
            } else if (secret.equals(PAYLOAD_SECRETS.get(PayloadType.PLAIN)) ||
                    secret.equals(PAYLOAD_SECRETS.get(PayloadType.KB)) ||
                    secret.equals(PAYLOAD_SECRETS.get(PayloadType.NOTIFICATION)) ||
                    secret.equals(PAYLOAD_SECRETS.get(PayloadType.ATTACHMENT)) ||
                    secret.equals(PAYLOAD_SECRETS.get(PayloadType.EXTERNAL_CHAT)) ||
                    secret.equals(PAYLOAD_SECRETS.get(PayloadType.EXTENDED))) {
                // f.e.: {"message":"@fbeachler4 - Please select which Poynt tax-related item you'd like to learn more about.","timestamp":"2017-09-28T20:50:30.166Z","channel":"chat","secret":"d3e78e3d3b68cb0fbd9f66dcaef93cea","context":{},"attachment":{},"email":{},"actions":[{"key":"65c319da57bb149cbf7a411983d7f8bc","lbl":"Custom Item Taxes","uri":"bt-bot:\/\/A3C-37E?key=65c319da57bb149cbf7a411983d7f8bc"},{"key":"31ef931599be39d1a84bf3d05e95bf6e","lbl":"Creating Taxes","uri":"bt-bot:\/\/A3C-37E?key=31ef931599be39d1a84bf3d05e95bf6e"},{"key":"93251e257652394d4b840c8ac95f9bac","lbl":"Applying Taxes, Fees, and Discounts","uri":"bt-bot:\/\/A3C-37E?key=93251e257652394d4b840c8ac95f9bac"},{"key":"9fe59e6554efeb272934a637938551fa","lbl":"Associating Taxes w\/ Individual Items","uri":"bt-bot:\/\/A3C-37E?key=9fe59e6554efeb272934a637938551fa"},{"key":"17f956b3682068879cc1aec7576f0874","lbl":"Nevermind","uri":"bt-bot:\/\/A3C-37E?key=17f956b3682068879cc1aec7576f0874"}]}
                this.dispMessage = new SpannableString(BLANK);
                if (text != null && !text.equals(NULLSTR)) {
                    this.dispMessage = new SpannableString(text);
                }
                if (context != null) {
                    if (from != null && !from.trim().equalsIgnoreCase(NULLSTR)) {
                        this.from = from;
                    }
                    if (fromName != null && !from.trim().equalsIgnoreCase(NULLSTR)) {
                        this.fromName = fromName;
                    }
                    String avatar = context.optString(JSON_KEY_FROM_AVATAR);
                    if (avatar != null) {
                        this.avatar = avatar;
                    }
                }
                if (attachment != null) {
                    if (preview != null && !preview.trim().equalsIgnoreCase(NULLSTR)) {
                        this.preview = preview;
                    }
                    if (url != null && !url.trim().equalsIgnoreCase(NULLSTR)) {
                        this.url = url;
                    }
                }
                if (actions != null) {
                    this.actions = new JSONArray();
                    for ( int n=0; n<actions.length(); n++ ) {
                        try {
                            JSONObject action = actions.getJSONObject(n);
                            Object perms = action.opt("perms");
                            if ( isPermitted(perms) ) {
                                this.actions.put(action);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
//                    this.actions = actions;
                }
                if ( kb != null ) {
                    kb_preview = null;
                    kb_title = kb.optString(JSON_KEY_KB_TITLE);
                    kb_preview = kb.optString(JSON_KEY_KB_PREVIEW);
                    kb_preview_length = kb.optInt(JSON_KEY_KB_PREVIEW_LENGTH);
                    kb_id = kb.optString(JSON_KEY_KB_ID);
                    if ( kb_preview!=null && !kb_preview.isEmpty() ) {
                        try {
                            kb_preview = URLDecoder.decode(kb_preview, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    } else {
                        kb_preview = BoomtownChat.sharedInstance().context.getString(com.goboomtown.microconnect.R.string.kb_empty_preview_message);
                    }
                }
                retCode = true;
            } else {
                retCode = false;
            }
        }
        Log.d(TAG, "#postprocessMessage " + this.from + ":" + text);
        return retCode;
    }

    public Boolean isPermitted(Object buttonPerms) {
        Object userPerms = BoomtownChat.sharedInstance().userPermissions;
        if ( buttonPerms == null ) {
            return true;
        }
        else if ( buttonPerms instanceof String ) {
            if ( ((String) buttonPerms).length() == 0 ) {
                return true;
            }
        }
        else if ( buttonPerms instanceof Integer &&
                  userPerms instanceof Integer ) {
//            if ((int) buttonPerms && !((int) buttonPerms & (int) userPerms)) {
//                return false;
//            } else {
//                return true;
//            }
            return true;
        }
        else if ( buttonPerms instanceof JSONArray &&
                  userPerms instanceof JSONArray ) {
            ArrayList<String> permsArray = arrayListFromJSONArray((JSONArray)buttonPerms);
            ArrayList<String> userPermsArray = arrayListFromJSONArray((JSONArray)userPerms);
            List<String> resultArray = intersection(permsArray, userPermsArray);
            if ( resultArray.size() > 0 ) {
                return true;
            }
        }
        return false;
    }


    public <T> List<T> intersection(List<T> list1, List<T> list2) {
        List<T> list = new ArrayList<T>();

        for (T t : list1) {
            if(list2.contains(t)) {
                list.add(t);
            }
        }

        return list;
    }


    public ArrayList<String> arrayListFromJSONArray(JSONArray arrayJSON) {
        ArrayList<String> array = new ArrayList<String>();
        if ( arrayJSON != null ) {
            for ( int n=0; n<arrayJSON.length(); n++ ) {
                String entry = arrayJSON.optString(n);
                if ( entry != null ) {
                    array.add(entry);
                }
            }
        }
        return  array;
    }


    public void determineMessageType() {
        if ( callId!=null && !callId.isEmpty() ) {
            messageType = BTMessageType.kTypeVideoChat;
        }
        else if ( url!=null && !url.isEmpty() ) {
            messageType = BTMessageType.kTypeAttachment;
        }
        else if ( kb_preview!=null && !kb_preview.isEmpty() && kb_id!=null && !kb_id.isEmpty() ) {
            messageType = BTMessageType.kTypeKB;
        }
        else {
            messageType = BTMessageType.kTypeOther;
        }
    }

    /**
     * Replace known emoticons in a message with their corresponding {@link ImageSpan}s.
     *
     * @param emoticonResources a map of known emoticons
     * @param msg               message to process
     * @param strategy          strategy for building emoticon drawable spans
     * @return
     */
    public static Spannable processTxtWithEmoticons(Spannable msg, Map<String, Emoticon> emoticonResources, EmoticonSpannableBuilderStrategy strategy) {
        SpannableStringBuilder ret = new SpannableStringBuilder(msg);
        Pattern pattern = Pattern.compile("\\([\\w]+\\)");
        Matcher matcher = pattern.matcher(msg);
        while (matcher.find()) {
            Log.d(TAG, "found text " + matcher.group() + " starting at "
                    + "index " + matcher.start() + " and ending at index " + matcher.end());
            String m = matcher.group().replace("(", "").replace(")", "");
            if (emoticonResources.containsKey(m)) {
                Emoticon emot = emoticonResources.get(m);
                DynamicDrawableSpan span = strategy.buildDynamicDrawableSpan(emot);
                if (span != null) {
                    ret.setSpan(span, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        return ret;
    }

    /**
     * Parse text and insert clickable links.
     *
     * @param msg
     * @return msg with links spanned with {@link android.text.style.URLSpan}s.
     */
    public static Spannable processTxtWithLinks(final Context context, final Spannable msg) {
        if (msg == null) {
            return null;
        }
        SpannableStringBuilder ret = new SpannableStringBuilder(msg);
        String model = Build.MODEL.toLowerCase();
        Pattern pattern = Pattern.compile(REGEX_HTTP_URL, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        final Matcher matcher = pattern.matcher(msg);
        while (matcher.find()) {
            Log.d(TAG, "found text " + matcher.group() + " starting at "
                    + "index " + matcher.start() + " and ending at index " + matcher.end());
            if (!model.contains("verifone") && !model.contains("carbon")) {
                ClickableSpan cs = new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "link inside chat message clicked - launching URL " + matcher.group());
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(matcher.group()));
                        context.startActivity(intent);
                    }
                };
                ret.setSpan(cs, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return ret;
    }

    /**
     * Build a drawable span with bounds set for its container.
     *
     * @param ctx contex for display density
     * @param container container to invalidate for animated gifs
     * @param emot emoticon to span
     * @param emotBoundWidth suggested width for emoticon; if 0 then height or line-height of container is used
     * @param emotBoundHeight suggested height for emoticon; if 0 then height or line-height of container is used
     * @return
     */
    public static DynamicDrawableSpan buildDynamicDrawableSpan(Context ctx, final View container, Emoticon emot, Integer emotBoundWidth, Integer emotBoundHeight) {
        DynamicDrawableSpan span = null;
        Integer resId;
        Drawable emotDrawable;
        String assetPath;
        InputStream asset;
        float displayDensity = ctx.getResources().getDisplayMetrics().density;
        int adjW = emotBoundWidth, adjH = emotBoundHeight;
        // set bounds
        if (emotBoundWidth < 1) {
            if (TextView.class.isAssignableFrom(container.getClass())) {
                adjW = ((TextView) container).getLineHeight();
            } else if (Button.class.isAssignableFrom(container.getClass())) {
                adjW = ((Button) container).getLineHeight();
            } else {
                adjW = container.getHeight();
            }
        }
        if (emotBoundHeight < 1) {
            if (TextView.class.isAssignableFrom(container.getClass())) {
                adjH = ((TextView) container).getLineHeight();
            } else if (Button.class.isAssignableFrom(container.getClass())) {
                adjH = ((Button) container).getLineHeight();
            } else {
                adjH = container.getHeight();
            }
        }
        switch (emot.getType()) {
            case RESOURCE:
                resId = (Integer) emot.getId();
                Log.d(TAG, "loading resource with id " + resId);
                emotDrawable = ResourcesCompat.getDrawable(ctx.getResources(), resId, null);
                // set bounds
                emotDrawable.setBounds(0, 0, adjW, adjH);
                // set image span
                span = new ImageSpan(emotDrawable);
                break;
            case ASSET_PNG:
                assetPath = (String) emot.getId();
                Log.d(TAG, "loading asset PNG with id " + assetPath);
                try {
                    asset = ctx.getAssets().open(assetPath);
                    emotDrawable = Drawable.createFromStream(asset, emot.getName());
                    // set bounds
                    emotDrawable.setBounds(0, 0, adjW, adjH);
                    // set image span
                    span = new ImageSpan(emotDrawable);
                } catch (IOException e1) {
                    Log.e(TAG, Log.getStackTraceString(e1));
                }
                break;
            case ASSET_GIF:
                assetPath = (String) emot.getId();
                Log.d(TAG, "loading asset GIF with id " + assetPath);
                try {
                    GifDrawable gd = new GifDrawable(ctx.getAssets(), assetPath);
                    adjW = (int) ((float) gd.getIntrinsicWidth() * displayDensity);
                    adjH = (int) ((float) gd.getIntrinsicHeight() * displayDensity);
                    gd.setBounds(0, 0, adjW, adjH);
                    gd.setCallback(new Drawable.Callback() {
                        @Override
                        public void invalidateDrawable(@NonNull Drawable who) {
                            container.invalidate();
                        }

                        @Override
                        public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
                            container.postDelayed(what, when);
                        }

                        @Override
                        public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
                            container.removeCallbacks(what);
                        }
                    });
                    span = new ImageSpan(gd);
                    gd.start();
                } catch (IOException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
                break;
            default:
                span = null;
                Log.w(TAG, "unknown emoticon type in resource mapping: " + emot.toString());
                break;
        }
        return span;
    }

    public void calculateId() {
        Log.d(TAG, "calculateId id: " + id);
//        try {
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            String idString = String.format("%s%s%s", this.from, this.text, sdf.format(this.date));
//            id = md5(idString);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public static final String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest
                    .getInstance("MD5");
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
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return BLANK;
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", this.id);
            jsonObject.put("hash", this.hash);
            jsonObject.put("type", this.type);
            jsonObject.put("from", this.from);
            jsonObject.put("fromName", this.fromName);
            jsonObject.put("fromMention", this.fromMention);
            jsonObject.put("fromNickname", this.fromNickname);
            jsonObject.put("callId", this.callId);
            jsonObject.put("service", this.service);
            jsonObject.put("channel", this.channel);
            jsonObject.put("text", this.dispMessage == null ? "null" : this.dispMessage.toString());
            jsonObject.put("dateString", this.dateString);
            jsonObject.put("xml", this.xml);
            jsonObject.put("preview", this.preview);
            jsonObject.put("url", this.url);
            jsonObject.put(JSON_KEY_ACTIONS, this.actions);
            jsonObject.put(JSON_KEY_KB_TITLE, this.kb_title);
            jsonObject.put(JSON_KEY_KB_PREVIEW, this.kb_preview);
            jsonObject.put(JSON_KEY_KB_PREVIEW_LENGTH, this.kb_preview_length);
            jsonObject.put(JSON_KEY_KB_ID, this.kb_id);
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return jsonObject.toString();
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (!(BoomtownChatMessage.class.isAssignableFrom(o.getClass()))) return false;

        BoomtownChatMessage that = (BoomtownChatMessage) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (from != null ? !from.equals(that.from) : that.from != null) return false;
        if (fromName != null ? !fromName.equals(that.fromName) : that.fromName != null)
            return false;
        if (fromMention != null ? !fromMention.equals(that.fromMention) : that.fromMention != null)
            return false;
        if (fromNickname != null ? !fromNickname.equals(that.fromNickname) : that.fromNickname != null)
            return false;
        if (callId != null ? !callId.equals(that.callId) : that.callId != null) return false;
        if (channel != null ? !channel.equals(that.channel) : that.channel != null) return false;
        if (service != null ? !service.equals(that.service) : that.service != null) return false;
        if (text != null ? !text.equals(that.text) : that.text != null) return false;
        if (dispMessage != null ? !dispMessage.toString().equals(that.dispMessage.toString()) : that.dispMessage != null)
            return false;
        // if (date != null ? !date.equals(that.date) : that.date != null) return false;
        if (dateString != null ? !dateString.equals(that.dateString) : that.dateString != null)
            return false;
        // if (xml != null ? !xml.equals(that.xml) : that.xml != null) return false;
        // if (avatar != null ? !avatar.equals(that.avatar) : that.avatar != null) return false;
        // if (preview != null ? !preview.equals(that.preview) : that.preview != null) return false;
        // if (url != null ? !url.equals(that.url) : that.url != null) return false;
        // if (delayInformation != null ? !delayInformation.equals(that.delayInformation) : that.delayInformation != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (from != null ? from.hashCode() : 0);
        result = 31 * result + (fromName != null ? fromName.hashCode() : 0);
        result = 31 * result + (fromMention != null ? fromMention.hashCode() : 0);
        result = 31 * result + (fromNickname != null ? fromNickname.hashCode() : 0);
        result = 31 * result + (callId != null ? callId.hashCode() : 0);
        result = 31 * result + (channel != null ? channel.hashCode() : 0);
        result = 31 * result + (service != null ? service.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (dispMessage != null ? dispMessage.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (dateString != null ? dateString.hashCode() : 0);
        result = 31 * result + (xml != null ? xml.hashCode() : 0);
        result = 31 * result + (avatar != null ? avatar.hashCode() : 0);
        result = 31 * result + (preview != null ? preview.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (delayInformation != null ? delayInformation.hashCode() : 0);
        return result;
    }

    public String getDateString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    /**
     * trim {@link this#dispMessage} - beware - removes any spans already set
     */
    public void trimText() {
        if (dispMessage != null) {
            dispMessage = new SpannableString(dispMessage.toString().trim());
        }
    }

//    public String getMessage() {
//        return dispMessage;
//    }

    public Spannable getMessage() {
        return dispMessage;
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
        TimeZone timeZone = TimeZone.getDefault();
        if (timeZone == null) {
            timeZone = TimeZone.getTimeZone("UTC");
        }
        sdf.setTimeZone(timeZone);
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

    /**
     * Simple POJO to encapsulate an emoticon image.
     *
     * @author fbeachler
     */
    public static class Emoticon {
        public static final String EXT_GIF = ".gif";
        public static final String EXT_PNG = ".png";

        public static enum EmoticonType {
            UNKNOWN(-1), RESOURCE(1), ASSET_GIF(1), ASSET_PNG(2);

            private int code;

            private EmoticonType(int code) {
                this.code = code;
            }

            public static EmoticonType fromEmoticonObjectId(Object objId) {
                if (String.class.isAssignableFrom(objId.getClass())) {
                    String id = (String) objId;
                    if (id.endsWith(EXT_GIF)) {
                        return ASSET_GIF;
                    }
                    if (id.endsWith(EXT_PNG)) {
                        return ASSET_PNG;
                    }
                }
                if (Integer.class.isAssignableFrom(objId.getClass())) {
                    return RESOURCE;
                }
                return UNKNOWN;
            }
        }

        private String name;
        private Object id;
        private EmoticonType type;

        public Emoticon(String name, Object id) {
            setName(name);
            setId(id);
        }

        @Override
        public String toString() {
            return name + "-" + type.name() + "-" + (id == null ? NULLSTR : id.toString());
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            if (name == null) {
                throw new IllegalArgumentException("name must be non-null");
            }
            this.name = name;
        }

        public Object getId() {
            return id;
        }

        public void setId(Object id) {
            if (id == null) {
                throw new IllegalArgumentException("id must be non-null");
            }
            this.id = id;
            this.type = EmoticonType.fromEmoticonObjectId(id);
        }

        public EmoticonType getType() {
            return type;
        }

    }

    /**
     * @author fbeachler
     */
    public enum PayloadType {
        UNKNOWN(-1), PLAIN(1), SMS(2), SMS_ATTACHMENT(3), NOTIFICATION(4),
        ATTACHMENT(5), VIDEO_CHAT(6), EXTERNAL_CHAT(7), EMAIL_MESSAGE(8),
        EMAIL_MESSAGE_KNOWN(9), DATA(10), EXTENDED(11), FACEBOOK(12), KB(13);

        private int code;

        private PayloadType(int code) {
            this.code = code;
        }

        public static PayloadType fromString(String type) {
            if (type == null) {
                return UNKNOWN;
            }
            switch (type.trim().toLowerCase()) {
                case "plain":
                    return PLAIN;
                case "sms":
                    return SMS;
                case "sms_attachment":
                    return SMS_ATTACHMENT;
                case "notification":
                    return NOTIFICATION;
                case "attachment":
                    return ATTACHMENT;
                case "video_chat":
                    return VIDEO_CHAT;
                case "external_chat":
                    return EXTERNAL_CHAT;
                case "email_message":
                    return EMAIL_MESSAGE;
                case "email_message_known":
                    return EMAIL_MESSAGE_KNOWN;
                case "data":
                    return DATA;
                case "extended":
                    return EXTENDED;
                case "facebook":
                    return FACEBOOK;
                case "kb":
                    return KB;
                default:
                    // noop
            }
            return UNKNOWN;
        }

        /**
         * Generate a payload type secret.
         *
         * @return secret hash for tis payload type
         */
        public String generateSecret() {
            return md5(this.name().toLowerCase());
        }
    }

    /**
     * Strategy for building emoticon spannables.
     *
     * @author fbeachler
     */
    public abstract static class EmoticonSpannableBuilderStrategy {
        /**
         * Build a span for an emoticon drawable.
         *
         * @param emot
         * @return
         */
        public abstract DynamicDrawableSpan buildDynamicDrawableSpan(BoomtownChatMessage.Emoticon emot);
    }

}
