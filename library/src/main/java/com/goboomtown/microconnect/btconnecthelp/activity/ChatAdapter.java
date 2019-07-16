package com.goboomtown.microconnect.btconnecthelp.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.DynamicDrawableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

//import com.goboomtown.BoomtownMember.R;
import com.goboomtown.chat.BoomtownChat;
import com.goboomtown.chat.BoomtownChatMessage;
import com.goboomtown.microconnect.chat.activity.BoomtownComm;
//import com.goboomtown.api.BoomtownAPI;
//import com.goboomtown.api.Constants;
//import com.goboomtown.chat.BoomtownChat;
//import com.goboomtown.chat.BoomtownChatMessage;
//import com.goboomtown.BoomtownChat;
//import com.goboomtown.chat.BoomtownChatMessage;
//import com.goboomtown.core.activity.BaseActivity;
//import com.goboomtown.core.widget.WebImageView;
//import com.goboomtown.model.BoomtownComm;
import com.goboomtown.microconnect.chat.fragment.WebViewFragment;
import com.goboomtown.microconnect.R;
import com.goboomtown.microconnect.btconnecthelp.widget.WebImageView;
import com.wefika.flowlayout.FlowLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Technovibe on 17-04-2015.
 * @author fbeachler
 */
public class ChatAdapter extends BaseAdapter {

    public static final String TAG = ChatAdapter.class.getSimpleName();

    private final List<BoomtownChatMessage> chatMessages;
    private ChatAdapterClickListener adapterListener;
    private Activity context;
    private LayoutInflater vi;
    private Bitmap bitmap;
    private ProgressDialog pDialog;
    private ViewHolder holder;
    private BoomtownChatMessage chatMessage;
    private Bitmap bmp;
    private Bitmap bmpRight;
    private WebViewFragment fragmentWebView;
    private final Map<String, BoomtownChatMessage.Emoticon> emoticonMap;

    public ChatAdapter(Activity context, ChatAdapterClickListener adapterListener, List<BoomtownChatMessage> chatMessages, Map<String, BoomtownChatMessage.Emoticon> emoticonMap) {
        if (context == null) {
            throw new IllegalArgumentException("null context given, aborting");
        }
        if (adapterListener == null) {
            throw new IllegalArgumentException("null listener given, aborting");
        }
        this.context = context;
        this.adapterListener = adapterListener;
        this.chatMessages = chatMessages;
        BoomtownChat.sharedInstance().avatars = new HashMap<String, Object>();
        fragmentWebView = new WebViewFragment();
        this.emoticonMap = emoticonMap;
        vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if (chatMessages != null) {
            return chatMessages.size();
        } else {
            return 0;
        }
    }

    @Override
    public BoomtownChatMessage getItem(int position) {
        if (chatMessages != null) {
            return chatMessages.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final BoomtownChatMessage chatMessage = getItem(position);

        if (convertView == null) {
            convertView = vi.inflate(R.layout.list_item_chat_message, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // check whether msg from me or other sender
        boolean isMe = chatMessage.isMe();
        setAlignment(holder, isMe);
        Spannable message = chatMessage.getMessage();
        if (message != null && !message.toString().isEmpty()) {
            holder.contentWithBG.setVisibility(View.VISIBLE);
            Spannable msgTxt = BoomtownChatMessage.processTxtWithEmoticons(chatMessage.getMessage(), emoticonMap, new BoomtownChatMessage.EmoticonSpannableBuilderStrategy() {
                @Override
                public DynamicDrawableSpan buildDynamicDrawableSpan(BoomtownChatMessage.Emoticon emot) {
                    return BoomtownChatMessage.buildDynamicDrawableSpan(context, holder.txtMessage, emot, holder.txtMessage.getLineHeight(), holder.txtMessage.getLineHeight());
                }
            });
            msgTxt = BoomtownChatMessage.processTxtWithLinks(context, msgTxt);
            holder.txtMessage.setClickable(true);
            holder.txtMessage.setText(msgTxt);
        } else {
            holder.contentWithBG.setVisibility(View.GONE);
        }
//        holder.txtMessage.setMovementMethod(LinkMovementMethod.getInstance());
        holder.txtMessage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                if (chatMessage.callId != null) {
                    callAnswer(chatMessage.callId, holder.txtMessage.getText().toString());
                }
            }
        });

        holder.txtInfo.setText(chatMessage.fromName + "  " + chatMessage.getHumanDate());

        holder.btnInfo.setText(chatMessage.fromName + "  " + chatMessage.getHumanDate());
        holder.btnInfo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (adapterListener != null) {
                    Button btn = (Button) v;
                    adapterListener.onClickChatBtnInfo(v, btn.getText().toString());
                }
            }
        });
        // clear action buttons
        for (int i = holder.actionButtonWrapper.getChildCount(); i >= 0; i--) {
            holder.actionButtonWrapper.removeView(holder.actionButtonWrapper.getChildAt(i));
        }

        JSONArray actions = chatMessage.getActions();
        buildActionButtons(actions, holder.actionButtonWrapper);

        if (!isMe) {
            holder.avatarLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapterListener != null) {
                        adapterListener.onClickChatAvatarLeft(v, holder.btnInfo.getText().toString());
                    }
                }
            });
        } else {
            holder.avatarRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapterListener != null) {
                        adapterListener.onClickChatAvatarRight(v, holder.btnInfo.getText().toString());
                    }
                }
            });
        }
        // load avatar in background
        (new LoadAvatarTask()).execute(chatMessage, holder, new WeakReference<Activity>(context), new WeakReference<ChatAdapter>(this));
        // load attachment preview
        holder.attachment.setVisibility(View.GONE);
        holder.txtMessage.setVisibility(View.GONE);
        holder.actionButtonWrapper.setVisibility(View.GONE);
        holder.htmlMessage.setVisibility(View.GONE);

        switch( chatMessage.messageType ) {
            case kTypeAttachment:
                holder.attachment.setVisibility(View.VISIBLE);
                holder.attachment.mRectangular = true;
                holder.attachment.setImageUrl(chatMessage.preview);
                holder.attachment.invalidate();
                if (chatMessage.url != null) {
                    holder.imageUrl = chatMessage.url;
                    holder.attachment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "chat attachment clicked (" + holder.imageUrl + ")");
                            if (adapterListener != null) {
                                adapterListener.onClickChatAttachment(v, holder.imageUrl);
                            }
                        }
                    });
                }
                break;
            case kTypeKB:
                holder.txtMessage.setVisibility(View.VISIBLE);
                holder.htmlMessage.setVisibility(View.GONE);
                String kbUrl = kbUrl(context, chatMessage.kb_id);
//                holder.htmlMessage.loadUrl(kbUrl);
                String html = chatMessage.kb_preview;
                html = html.replace("<p>", "\n\n");
                html = html.replace("<br>", "\n\n");
//                html = "<html><body" + html + "</body></html>";
                holder.txtMessage.setText(html);
//                holder.htmlMessage.setWebViewClient(new WebViewClient() {
//                                                        @Override
//                                                        public void onPageFinished(WebView webView, String url) {
//                                                            super.onPageFinished(webView, url);
//                                                            holder.contentWithBG.requestLayout();
//                                                        }
//                                                    }
//                );
//                holder.htmlMessage.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
                holder.txtMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "KB clicked (" + chatMessage.kb_title + ")");
                        String kbUrl = kbUrl(context, chatMessage.kb_id);

                        if (adapterListener != null) {
                            adapterListener.onClickKB(v, kbUrl, chatMessage.kb_title);
                        }
                    }
                });
                holder.txtMessage.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Log.d(TAG, "KB clicked (" + chatMessage.kb_title + ")");
                        String kbUrl = kbUrl(context, chatMessage.kb_id);

                        if (adapterListener != null) {
                            adapterListener.onClickKB(v, kbUrl, chatMessage.kb_title);
                        }
                        return true;
                    }
                });
                break;
            case kTypeVideoChat:
            case kTypeOther:
                holder.txtMessage.setVisibility(View.VISIBLE);
                holder.actionButtonWrapper.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
//        if (chatMessage.preview != null && chatMessage.preview.length() > 0) {
//            holder.attachment.setVisibility(View.VISIBLE);
//            holder.attachment.mRectangular = true;
//            holder.attachment.setImageUrl(chatMessage.preview);
//            holder.attachment.invalidate();
//            if (chatMessage.url != null) {
//                holder.imageUrl = chatMessage.url;
//                holder.attachment.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Log.d(TAG, "chat attachment clicked (" + holder.imageUrl + ")");
//                        if (adapterListener != null) {
//                            adapterListener.onClickChatAttachment(v, holder.imageUrl);
//                        }
//                    }
//                });
//            }
//        }

        return convertView;
    }

    public String kbUrl(final Context applicationContext, String kb_id) {
//        AuthenticationStore authStore = AuthenticationStore.getInstance(applicationContext);
//        final String authToken = authStore.getAuthToken();
        String kbUrl = "";//String.format("%s?id=%s&authtoken=%s",BoomtownArticlePage, kb_id, authToken);
        return kbUrl;
    }

    /**
     * Add chat message.
     *
     * @param message
     */
    public void add(BoomtownChatMessage message) {
        chatMessages.add(message);
    }

    /**
     * Add list of chat messages.
     *
     * @param messages
     */
    public void add(List<BoomtownChatMessage> messages) {
        chatMessages.addAll(messages);
    }

    /**
     * Initiate a video chat for the given call id.
     *
     * @param call_id
     * @param alert
     */
    public void callAnswer(String call_id, String alert) {
//        Intent intent = new Intent();
//        if (call_id != null) {
//            intent.putExtra(BoomtownAPI.kCallId, call_id);
//        }
//        ((BaseActivity) context).showProgressWithMessage(context.getString(R.string.msg_starting_video));
//        BoomtownAPI.sharedInstance().apiMembersCommGet(call_id, alert, context, BoomtownComm.kTypeVideo);
    }

    /**
     * Set text message alignment and chat bubble bg.
     *
     * @param holder
     * @param isMe
     */
    protected void setAlignment(ViewHolder holder, boolean isMe) {
        // default !isMe
        int alignGravity = Gravity.LEFT;
        int bgResourceId = R.drawable.out_message_bg;
        int[] holderContentRuleVerbs = {RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_LEFT};
        int[] avatarVisibilities = {View.VISIBLE, View.INVISIBLE};
        if (isMe) {
            alignGravity = Gravity.RIGHT;
            bgResourceId = R.drawable.in_message_bg;
            holderContentRuleVerbs[0] = RelativeLayout.ALIGN_PARENT_LEFT;
            holderContentRuleVerbs[1] = RelativeLayout.ALIGN_PARENT_RIGHT;
            avatarVisibilities[0] = View.INVISIBLE;
            avatarVisibilities[1] = View.VISIBLE;
        }
        // set chat bubble bg
        holder.contentWithBG.setBackgroundResource(bgResourceId);
        // align panel
        LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) holder.contentPanel.getLayoutParams();
        llp.gravity = alignGravity;
        holder.contentPanel.setLayoutParams(llp);
        // align wrapper
        llp = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
        llp.gravity = alignGravity;
        holder.contentWithBG.setLayoutParams(llp);
        // align wrapper
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
        rlp.addRule(holderContentRuleVerbs[0], 0);
        rlp.addRule(holderContentRuleVerbs[1]);
        holder.content.setLayoutParams(rlp);
        // align txt msg + attachment
        llp = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
        llp.gravity = alignGravity;
        holder.txtMessage.setLayoutParams(llp);

        llp = (LinearLayout.LayoutParams) holder.htmlMessage.getLayoutParams();
        llp.gravity = alignGravity;
        holder.htmlMessage.setLayoutParams(llp);

        llp = (LinearLayout.LayoutParams) holder.attachment.getLayoutParams();
        llp.gravity = alignGravity;
        holder.attachment.setLayoutParams(llp);
        // align txtinfo + btninfo
        llp = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
        llp.gravity = alignGravity;
        holder.txtInfo.setLayoutParams(llp);
        llp = (LinearLayout.LayoutParams) holder.btnInfo.getLayoutParams();
        llp.gravity = alignGravity;
        holder.btnInfo.setLayoutParams(llp);
        // align action buttons
        llp = (LinearLayout.LayoutParams) holder.actionButtonWrapper.getLayoutParams();
        llp.gravity = alignGravity;
        holder.actionButtonWrapper.setLayoutParams(llp);
        // avatar visibility
        holder.avatarLeft.setVisibility(avatarVisibilities[0]);
        holder.avatarRight.setVisibility(avatarVisibilities[1]);
    }

    /**
     *
     * @param v
     * @return a view holder hydrated with views for this adapter
     */
    protected ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.txtMessage = v.findViewById(R.id.txtMessage);
        holder.content = v.findViewById(R.id.content);
        holder.contentPanel = v.findViewById(R.id.contentPanel);
        holder.contentWithBG = v.findViewById(R.id.contentWithBackground);
        holder.btnInfo = v.findViewById(R.id.btnInfo);
        holder.txtInfo = v.findViewById(R.id.txtInfo);
        holder.avatarLeft = v.findViewById(R.id.avatarLeft);
        holder.avatarRight = v.findViewById(R.id.avatarRight);
        holder.attachment = v.findViewById(R.id.attachment);
        holder.actionButtonWrapper = v.findViewById(R.id.wrap_chat_msg_act_btns);
        holder.handle = null;
        holder.imageUrl = null;
        holder.htmlMessage = v.findViewById(R.id.htmlMessage);
        return holder;
    }

    public void clearMessages() {
        this.chatMessages.clear();
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        public TextView txtMessage;
        public TextView txtInfo;
        public Button btnInfo;
        public LinearLayout content;
        public LinearLayout contentPanel;
        public LinearLayout contentWithBG;
        public ImageView avatarLeft;
        public ImageView avatarRight;
        public String handle;
        public String imageUrl;
        public WebImageView attachment;
        public FlowLayout actionButtonWrapper;
        public WebView htmlMessage;
    }

    /**
     * Load the user avatar web image.
     *
     * @param chatMessage
     * @return
     */
    public Bitmap getAvatarForUser(BoomtownChatMessage chatMessage) {
        String urlString = null;
        Bitmap avatar = (Bitmap) BoomtownChat.sharedInstance().avatars.get(chatMessage.from);
        if (avatar != null) {
            return avatar;
        }
        if (chatMessage.avatar != null && !chatMessage.avatar.isEmpty()) {
            urlString = chatMessage.avatar;
        } else {
            String resource = chatMessage.from;
            if (resource == null) {
                return null;
            }
            String[] tokens = resource.split(";");
            String from = tokens[0];
            tokens = from.split(":");
            if (tokens.length > 1) {
                urlString = BoomtownChat.sharedInstance().avatarRetrievalAPIBaseURL + "/api/v1/avatar/" + tokens[0] + "/" + tokens[1] + "/50,50";
            }
        }
        Log.d(TAG, "#getAvatarForUser url: " + urlString);
//        if (urlString == null || urlString.isEmpty() || Constants.NULLSTR.equals(urlString)) {
        if ( urlString == null || urlString.isEmpty() ) {
            return null;
        }
        URL url;
        try {
            url = new URL(urlString);
            HttpURLConnection conn =
                    (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            avatar = ChatAdapter.getclip(BitmapFactory.decodeStream(is));
            BoomtownChat.sharedInstance().avatars.put(chatMessage.from, avatar);
        } catch (Throwable e) {
            Log.d(TAG, Log.getStackTraceString(e));
            Log.w(TAG, "unable to load avatar from URL: " + urlString);
        }
        return avatar;
    }

    /**
     * Build BT action buttons dynamically.  Adds new chat buttons to container
     * with click listeners that invoke {@link ChatAdapter.ChatAdapterClickListener#onClickChatActionButton(View, String)}.
     * Actions JSON should look like: [{"key":"65c319da57bb149cbf7a411983d7f8bc","lbl":"Custom Item Taxes","uri":"bt-bot:\/\/A3C-37E?key=65c319da57bb149cbf7a411983d7f8bc"},{"key":"31ef931599be39d1a84bf3d05e95bf6e","lbl":"Creating Taxes","uri":"bt-bot:\/\/A3C-37E?key=31ef931599be39d1a84bf3d05e95bf6e"},{"key":"93251e257652394d4b840c8ac95f9bac","lbl":"Applying Taxes, Fees, and Discounts","uri":"bt-bot:\/\/A3C-37E?key=93251e257652394d4b840c8ac95f9bac"},{"key":"9fe59e6554efeb272934a637938551fa","lbl":"Associating Taxes w\/ Individual Items","uri":"bt-bot:\/\/A3C-37E?key=9fe59e6554efeb272934a637938551fa"},{"key":"17f956b3682068879cc1aec7576f0874","lbl":"Nevermind","uri":"bt-bot:\/\/A3C-37E?key=17f956b3682068879cc1aec7576f0874"}]
     *
     * @param actions
     * @param container
     */
    protected void buildActionButtons(JSONArray actions, ViewGroup container) {
        // set container visibility
        if (actions == null || actions.length() < 1) {
            container.setVisibility(View.INVISIBLE);
            return;
        }
        container.setVisibility(View.VISIBLE);
        // add new buttons
        for (int i = 0; i < actions.length(); i++) {
            try {
                JSONObject action = actions.getJSONObject(i);
                Spannable btnLabel = new SpannableString(action.optString(BoomtownChatMessage.JSON_KEY_LBL));
                final String btnUri = action.optString(BoomtownChatMessage.JSON_KEY_URI);
                if (btnLabel != null && btnUri != null) {
                    final Button btn = new Button(context);
                    FlowLayout.LayoutParams llp = new FlowLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    btn.setLayoutParams(llp);
                    btn.setAllCaps(false);  // override default styling else lose spannable data via #AllCapsTransformationMethod
                    btnLabel = BoomtownChatMessage.processTxtWithEmoticons(btnLabel, emoticonMap, new BoomtownChatMessage.EmoticonSpannableBuilderStrategy() {
                        @Override
                        public DynamicDrawableSpan buildDynamicDrawableSpan(BoomtownChatMessage.Emoticon emot) {
                            return BoomtownChatMessage.buildDynamicDrawableSpan(context, btn, emot, btn.getLineHeight() * 2, btn.getLineHeight() * 2);
                        }
                    });
                    btn.setText(btnLabel);
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (adapterListener != null) {
                                adapterListener.onClickChatActionButton(v, btnUri);
                            }
                        }
                    });
                    container.addView(btn);
                }
            } catch (JSONException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
    }

    /**
     * Clip a bitmap.
     *
     * @param bitmap
     * @return
     */
    public static Bitmap getclip(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, width, height);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(width / 2, height / 2,
                width / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static class LoadAvatarTask extends AsyncTask<Object, Void, Bitmap> {

        private BoomtownChatMessage cm;
        private ViewHolder holder;
        private WeakReference<Activity> activity;
        private WeakReference<ChatAdapter> adapter;

        @Override
        protected Bitmap doInBackground(Object... params) {
            if (params == null || params.length < 1) {
                return null;
            }
            cm = (BoomtownChatMessage) params[0];
            holder = (ViewHolder) params[1];
            activity = (WeakReference<Activity>) params[2];
            adapter = (WeakReference<ChatAdapter>) params[3];
            return adapter.get().getAvatarForUser(cm);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap == null) {
                return;
            }
            final Bitmap bmp = bitmap;
            activity.get().runOnUiThread(new Runnable() {
                public void run() {
                    boolean isMe = cm.isMe();
                    if (bmp != null) {
                        if (!isMe) {
                            holder.avatarLeft.setImageBitmap(bmp);
                        } else {
                            holder.avatarRight.setImageBitmap(bmp);
                        }
                    }
                }
            });
        }
    }

    public interface ChatAdapterClickListener {
        void onClickChatAttachment(View v, String imageUrl);
        void onClickKB(View v, String kbUrl, String kbTitle);
        void onClickChatBtnInfo(View v, String mention);
        void onClickChatAvatarLeft(View v, String mention);
        void onClickChatAvatarRight(View v, String mention);
        void onClickChatActionButton(View v, String actionUrl);
    }
}
