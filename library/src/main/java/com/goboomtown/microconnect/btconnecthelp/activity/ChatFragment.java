package com.goboomtown.microconnect.btconnecthelp.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.DynamicDrawableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.goboomtown.chat.BoomtownChat;
import com.goboomtown.chat.BoomtownChatMessage;
import com.goboomtown.activity.BaseActivity;
import com.goboomtown.activity.KBActivity;
import com.goboomtown.microconnect.btconnecthelp.api.BTConnectAPI;
import com.goboomtown.microconnect.btconnecthelp.model.BTConnectChat;
import com.goboomtown.microconnect.btconnecthelp.model.BTConnectIssue;
import com.goboomtown.microconnect.btconnecthelp.util.ExifUtil;
import com.goboomtown.microconnect.btconnecthelp.view.BTConnectHelpButton;
import com.goboomtown.microconnect.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * A simple {@link android.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p>
 * Created by Larry Borsato on 2016-07-12.
 */
public class ChatFragment extends Fragment implements ChatAdapter.ChatAdapterClickListener, BoomtownChat.BoomtownChatListener {

    public static final String TAG = ChatFragment.class.getSimpleName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public BTConnectHelpButton mHelpButton = null;
    public BTConnectIssue mIssue = null;
    private String mCommId = null;
    private Boolean mCommEntered = false;
    private BTConnectChat mChatRecord = null;
    private JSONObject mXmppInfo = null;

    private String mJid = null;
    private String mPassword = null;
    private String mHost = null;
    private String mPort = null;
    private String mResource = null;

    public static final int UPLOAD_TYPE_NONE = 0;
    public static final int UPLOAD_TYPE_AVATAR = 1;
    public static final int UPLOAD_TYPE_CHAT = 2;

    public static final int REQUEST_CAMERA = 1;
    public static final int SELECT_FILE = 2;
    public static final int LOAD_MASKED_WALLET_REQUEST_CODE = 1000;
    public static final int LOAD_FULL_WALLET_REQUEST_CODE = 1001;

    //    public static final int mAndroidPayEnvironment = WalletConstants.ENVIRONMENT_TEST;
//    public GoogleApiClient mGoogleApiClient;
    public String mPurchasePrice;
    public String mPurchaseDescription;

    public int mType;
    public Bitmap mImage;
    public Bitmap mOriginalImage;
    public int mImageType;
    public int mUploadType;
    public Boolean mChatUpload = false;

    private ProgressDialog mProgress;

    private OnFragmentInteractionListener mListener;

    private EditText messageET;
    private ListView messagesContainer;
    private Button sendBtn;
//    private OldChatAdapter adapter;
    private ChatAdapter adapter;
    private ArrayList<BoomtownChatMessage> chatHistory;
    private ImageButton chatUploadButton;
    private Button chatSendButton;
    private EditText messageEdit;
    private TextView titleView;
    private ListView mAutocompleteListView;
    private ArrayList<String> mAutocompleteEntries;
    private ArrayList<String> mMentions;
    private String[] mAutocompleteTokens;
    private int mAutocompleteTokensCount;
    public ChatFragment activity;
    private AlertDialog alertDialog;
    public View mView;
    private Boolean mInSetup = false;
    public String kbTitle;

    public Activity mParent;
    public Boolean mUploadRequested;
    public WebView webView = null;
    public Boolean webViewShowing = false;
    private RelativeLayout webViewFrame = null;
    public MenuItem mMenuItemActionDone;

    public Button mBtnGetVideoChatHelp;

    public Boolean mInRoom;

    public String senderId;
    public String senderDisplayName;
    private AutocompletePredictor acPredictor;
    private Map<String, BoomtownChatMessage.Emoticon> emoticonsMap;
    private ChatMessageEditWatcher cmew;

    protected Map<String, BoomtownChatMessage.Emoticon> chatEmoticons;

    public ChatFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mParent = getActivity();
        mInRoom = false;
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        BoomtownChat.sharedInstance().context = mParent;
        BoomtownChat.sharedInstance().avatarRetrievalAPIBaseURL = "https://gnfktoh224eg-api.goboomtown.com";
        BoomtownChat.sharedInstance().buildEmoticonsIndexFromAssets(BoomtownChatMessage.ASSET_PATH_EMOTICONS);

        mAutocompleteEntries = new ArrayList<>();
        mMentions = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        mView = view;

        chatUploadButton = view.findViewById(R.id.chatUploadButton);
        chatSendButton = view.findViewById(R.id.chatSendButton);

        chatUploadButton.setEnabled(true);
        chatSendButton.setEnabled(false);

        mUploadRequested = false;

        mBtnGetVideoChatHelp = (Button) view.findViewById(R.id.btn_get_video_chat_help);
        mBtnGetVideoChatHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mParent.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String url = String.format("goboomtownconnect://prod/member/issue/read?issue_id=%s&call", mIssue.id);
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        mParent.startActivity(intent);
                    }
                });
            }
        });

        if ( mIssue!=null && mIssue.reference_num!=null ) {
            String title = String.format("%s #%s", getString(R.string.ticket), mIssue.reference_num);
            mHelpButton.setChatTitle(title);
        }

//        mXmppInfo = BTConnectAPI.extractXmppInformation(mIssue.xmpp_data);
        mXmppInfo = BoomtownChat.extractXmppInformation(mIssue.xmpp_data, BTConnectAPI.sharedInstance().getKey());
        if (mXmppInfo != null) {
            setXmppInfo(mXmppInfo);
            if (mCommId != null) {
                commGet(mCommId);
            }
        } else {
            warn(getString(R.string.app_name), getString(R.string.warn_unable_to_obtain_chat_server_information));
        }

        initControls(view);
//        initLayoutListeners();

//        emoticonsMap = getChatEmoticons();
//        emoticonsMap =  BoomtownChat.sharedInstance().chatEmoticons;
        emoticonsMap = new HashMap<String, BoomtownChatMessage.Emoticon>(BoomtownChat.sharedInstance().chatEmoticons);
        acPredictor = new AutocompletePredictor(new WeakReference<Activity>(mParent),
                new WeakReference<ListView>(mAutocompleteListView),
                new WeakReference<ListView>(messagesContainer),
                new WeakReference<EditText>(messageEdit),
                new WeakReference<Map<String, BoomtownChatMessage.Emoticon>>(emoticonsMap));

        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_chat, menu);
        mMenuItemActionDone = menu.findItem(R.id.action_done);
        mMenuItemActionDone.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_done) {
            if (webViewShowing) {
                hideWebView();
                return true;
            }
        }
        if (id == R.id.action_resolve) {
            if (webViewShowing) {
                hideWebView();
            }
            cancelIssue();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClickChatAttachment(View v, String imageUrl) {
        final String iu = imageUrl;
        getActivity().runOnUiThread(() -> showWebView(iu));
    }

    @Override
    public void onClickKB(View v, String kbUrl, String kbTitle) {
        this.kbTitle = kbTitle;
        retrieveKB(kbUrl);
    }

    @Override
    public void onClickChatBtnInfo(View v, String mention) {
        addMention(mention);
    }

    @Override
    public void onClickChatAvatarLeft(View v, String mention) {
        addMention(mention);
    }

    @Override
    public void onClickChatAvatarRight(View v, String mention) {
        addMention(mention);
    }

    @Override
    public void onClickChatActionButton(View v, String actionUrl) {
        JSONObject actMsg = new JSONObject();
        try {
            actMsg.put(BoomtownChatMessage.JSON_KEY_SECRET, BoomtownChatMessage.PAYLOAD_SECRETS.get(BoomtownChatMessage.PayloadType.DATA));
            actMsg.put(BoomtownChatMessage.JSON_KEY_MESSAGE, actionUrl);
            actMsg.put(BoomtownChatMessage.JSON_KEY_CHANNEL, "chat");
            actMsg.put(BoomtownChatMessage.JSON_KEY_CONTEXT, new JSONObject());
            actMsg.put(BoomtownChatMessage.JSON_KEY_FROM, senderId);
            String msgString = actMsg.toString();
            BoomtownChat.sharedInstance().sendGroupchatMessage(msgString, true);
        } catch (JSONException e) {
            Log.w(TAG, "unable to send action msg, exception was:\n" + Log.getStackTraceString(e));
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }

//        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver, new IntentFilter(BoomtownAPI.kApplicationStateChangedAction));
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        hideProgress();
        commExit();
        BoomtownChat.sharedInstance().disconnect();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void showProgressBar(final String message, final boolean show) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
//                ProgressBar progressBar = (ProgressBar) mView.findViewById(R.id.progressBar);
//                TextView progressMessage = (TextView) mView.findViewById(R.id.progressMessage);
//                progressMessage.setText(message);
//                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
//                progressMessage.setVisibility(show ? View.VISIBLE : View.GONE);
//                progressMessage.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void enableChatButtons() {
        mParent.runOnUiThread(new Runnable() {
            public void run() {
                chatUploadButton.setEnabled(true);
                chatSendButton.setEnabled(true);
            }
        });
    }

    private void disableChatButtons() {
        mParent.runOnUiThread(new Runnable() {
            public void run() {
                chatUploadButton.setEnabled(false);
                chatSendButton.setEnabled(false);
            }
        });
    }

    public void showProgressWithMessage(final String message) {
        mParent.runOnUiThread(new Runnable() {
            public void run() {
                if (mProgress != null) {
                    mProgress.dismiss();
                    mProgress = null;
                }
                if (!mParent.isFinishing())
                    mProgress = ProgressDialog.show(mParent, null, message, true);
            }
        });
    }

    public void hideProgress() {
        mParent.runOnUiThread(new Runnable() {
            public void run() {
                if (mProgress != null) {
                    mProgress.dismiss();
                    mProgress = null;
                }
            }
        });
    }

    private void setXmppInfo(JSONObject info) {
        mCommId = info.optString("comm_id");
        mJid = info.optString("jid");
        mPassword = info.optString("password");
        mHost = info.optString("host");
        mPort = info.optString("port");
        mResource = info.optString("resource");
    }

    public void setup() {
        if (mInSetup)
            return;

        mInSetup = true;
//        mChatRecord = new BTConnectChat();

//        setTitle("Boomtown Chat: " + chatRecord.title);
//        titleView.setText(chatRecord.title);

        String me = mResource;
        String[] tokens = me.split(";");
        senderId = tokens[0];
        BoomtownChat.sharedInstance().me = senderId;

        Map<String, String> participantInfo = (Map<String, String>) mChatRecord.participants_eligible.get(senderId);
        if (participantInfo != null)
            senderDisplayName = (String) participantInfo.get("name");
        if (senderDisplayName == null)
            senderDisplayName = senderId;

//        mMentions.add("@all");
//        for (String key : mChatRecord.participants_eligible.keySet()) {
//            Map<String, String> participant = (Map<String, String>) mChatRecord.participants_eligible.get(key);
//            mMentions.add(participant.get("alias"));
//        }
//        Collections.sort(mMentions);

        acPredictor.addMention("@all");
        if (mChatRecord != null ) {
            for (String key : mChatRecord.participants_eligible.keySet()) {
                Map<String, String> participant = (Map<String, String>) mChatRecord.participants_eligible.get(key);
                acPredictor.addMention(participant.get("alias"));
            }
            Collections.sort(acPredictor.getMentions());

            BoomtownChat.sharedInstance().participants_eligible = mChatRecord.participants_eligible;
        }

        BoomtownChat.sharedInstance().setListener(this);
//        BoomtownChat.sharedInstance().listener = this;

//        BoomtownChat.sharedInstance().setListener(new BoomtownChat.BoomtownChatListener() {
//            @Override
//            public void onConnect() {
//                hideProgress();
//                showProgressWithMessage("Joining room");
//            }
//
//            @Override
//            public void onConnectError() {
//
//            }
//
////            @Override
////            public void onTimeoutConnect() {
////                Log.d("ChatFragment", "onTimeoutConnect");
////            }
//
//            @Override
//            public void onNotAuthenticate() {
//                Log.d("ChatFragment", "onNotAuthenticate");
//            }
//
//            @Override
//            public void onDisconnect(Exception e) {
//                Log.d("ChatFragment", "onDisconnect");
//            }
//
////            @Override
////            public void onDisconnect() {
////                Log.d("ChatFragment", "onDisconnect");
////
////            }
//
//            @Override
//            public void onReceiveMessage(final BoomtownChatMessage message) {
//                if (message.from != null && message.from.equalsIgnoreCase(senderId))
//                    message.self = true;
//                mParent.runOnUiThread(new Runnable() {
//                    public void run() {
//                        displayMessage(message);
//                    }
//                });
////                scroll();
//            }
//
//            @Override
//            public void onJoinRoom() {
////                alertDialog.dismiss();
//                hideProgress();
//                Log.d("ChatFragment", "onJoinRoom");
//                mParent.runOnUiThread(new Runnable() {
//                    public void run() {
//                        mInRoom = true;
////                        chatUploadButton.setEnabled(true);
////                        chatSendButton.setEnabled(false);
//                    }
//                });
//            }
//
//            @Override
//            public void onJoinRoomNoResponse() {
//                hideProgress();
//                Log.d("ChatFragment", "onJoinRoomNoResponse");
//            }
//
//            @Override
//            public void onJoinRoomFailed(String reason) {
//                hideProgress();
//                Log.d("ChatFragment", "onJoinRoomNoResponse");
//            }
//
//        });

        chatHistory = new ArrayList<BoomtownChatMessage>();
//        adapter = new OldChatAdapter(mParent, new ArrayList<BoomtownChatMessage>());
//        adapter.chatFrament = this;
        adapter = new ChatAdapter(mParent, this, chatHistory, emoticonsMap);
//        adapter.chatFragment = this;
        messagesContainer.setAdapter(adapter);

        commEnter();

        if (mChatRecord != null && mChatRecord.external_id != null && !mChatRecord.external_id.isEmpty())
            BoomtownChat.sharedInstance().roomJid = mChatRecord.external_id;

        connect();

    }

        @Override
        public void onConnect() {
            hideProgress();
            showProgressWithMessage("Joining room");
        }

        @Override
        public void onConnectError() {

        }

//            @Override
//            public void onTimeoutConnect() {
//                Log.d("ChatFragment", "onTimeoutConnect");
//            }

        @Override
        public void onNotAuthenticate() {
            Log.d("ChatFragment", "onNotAuthenticate");
        }

        @Override
        public void onDisconnect(Exception e) {
            Log.d("ChatFragment", "onDisconnect");
        }

//            @Override
//            public void onDisconnect() {
//                Log.d("ChatFragment", "onDisconnect");
//
//            }

        @Override
        public void onReceiveMessage(final BoomtownChatMessage message) {
            if (message.from != null && message.from.equalsIgnoreCase(senderId))
                message.self = true;
            mParent.runOnUiThread(new Runnable() {
                public void run() {
                    displayMessage(message);
                }
            });
//                scroll();
        }

        @Override
        public void onJoinRoom() {
//                alertDialog.dismiss();
            hideProgress();
            Log.d("ChatFragment", "onJoinRoom");
            mParent.runOnUiThread(new Runnable() {
                public void run() {
                    mInRoom = true;
                        chatUploadButton.setEnabled(true);
//                        chatSendButton.setEnabled(false);
                }
            });
        }

        @Override
        public void onJoinRoomNoResponse() {
            hideProgress();
            Log.d("ChatFragment", "onJoinRoomNoResponse");
        }

        @Override
        public void onJoinRoomFailed(String reason) {
            hideProgress();
            Log.d("ChatFragment", "onJoinRoomNoResponse");
        }



    private void initControls(View view) {
        titleView = (TextView) view.findViewById(R.id.title);
        messagesContainer = (ListView) view.findViewById(R.id.messagesContainer);
        mAutocompleteListView = (ListView) view.findViewById((R.id.autocompleteList));

//        RelativeLayout container = (RelativeLayout) view.findViewById(R.id.chatContainer);
        messageEdit = (EditText) view.findViewById(R.id.messageEdit);
        messageEdit.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
//                if (s.toString().length() == 0) {
//                    dismissKeyboard();
//                    messagesContainer.setEnabled(true);
//                    mAutocompleteListView.setVisibility(View.GONE);
//                    chatSendButton.setEnabled(false);
//                } else {
//                    chatSendButton.setEnabled(true);
//                    showAutocompleteList(s.toString());
//                }
                if (s.toString().length() == 0) {
                    dismissKeyboard();
                    messagesContainer.setEnabled(true);
                    mAutocompleteListView.setVisibility(View.GONE);
                    chatSendButton.setEnabled(false);
                } else {
                    chatSendButton.setEnabled(true);
                    acPredictor.showAutoCompleteList(s);
                }

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                String text = s.toString();
//                if ( text.length() == 0 ) {
//                    chatSendButton.setEnabled(false);
//                }
//                else {
//                    chatSendButton.setEnabled(true);
//                    showAutocompleteList(s.toString());
//                }
            }
        });

//        chatUploadButton = (ImageButton) view.findViewById(R.id.chatUploadButton);
//        chatUploadButton.setEnabled(false);
        chatUploadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                dismissKeyboard();
                if ( mInRoom ) {
//                    ((BaseActivity) getActivity()).selectImage(getActivity(), 0, BaseActivity.UPLOAD_TYPE_NONE);
                    selectImage(getActivity(), 0, BaseActivity.UPLOAD_TYPE_NONE);
                    if (messageEdit != null) {
                        messageEdit.setText("");
                        scroll();
                    }
                } else {
                    ((BaseActivity)mParent).showErrorMessage(null, mParent.getString(R.string.msg_not_in_room));
                }
            }
        });

        chatSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                dismissKeyboard();
                if (mInRoom) {
                    if (messageEdit != null) {
                        BoomtownChat.sharedInstance().sendGroupchatMessage(messageEdit.getText().toString(), false);
                        messageEdit.setText("");
                        scroll();
                    } else {
//                        ((BaseActivity)mParent).showErrorMessage(null, mParent.);
                        warn(getString(R.string.app_name), getString(R.string.msg_not_in_room));
                    }
                }
            }
        });

        webViewFrame = (RelativeLayout) view.findViewById(R.id.chatContainer);
        if (webView == null) {
            webView = new WebView(getActivity());
            webView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            webView.getSettings().setSupportZoom(true);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            webView.setScrollbarFadingEnabled(true);
            webView.getSettings().setLoadsImagesAutomatically(true);
            webView.getSettings().setJavaScriptEnabled(false);

            webView.setWebViewClient(new SimpleWebViewClient());

//            if (savedInstanceState != null) {
//                // our onRestoreInstanceState will call webView.restoreState()
//                // webView.restoreState(savedInstanceState);
//            } else {
//                webView.loadUrl(url);
//            }
        }

        webView.setVisibility(View.GONE);
        webViewShowing = false;
        webViewFrame.addView(webView);

    }

    protected void initLayoutListeners() {
        final InputMethodManager imm = (InputMethodManager) mParent.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (cmew == null) {
            cmew = new ChatMessageEditWatcher(new WeakReference<>(imm));
            messageEdit.addTextChangedListener(cmew);
        }
//        chatUploadButton.setEnabled(true);
        chatUploadButton.setOnClickListener(v -> {
            // Perform action on click
            dismissKeyboard(imm);
            if (mInRoom) {
                Activity activity = getActivity();
                if ( mParent instanceof BTConnectHelpBaseActivity ) {
//                    ((BTConnectHelpBaseActivity) getActivity()).selectImage(getActivity(), 0, BaseActivity.UPLOAD_TYPE_NONE);
                    selectImage(mParent, 0, UPLOAD_TYPE_NONE);
                    if (messageEdit != null) {
                        messageEdit.setText("");
                        scroll();
                    }
                } else {
//                    Toast.makeText(mParent, "Your activity must extent BTConnectHelpBaseActivity to support attachments.", Toast.LENGTH_LONG).show();
                    warn(getString(R.string.app_name), "Your activity must extent BTConnectHelpBaseActivity to support attachments.");
                }
            } else {
//                Toast.makeText(mParent, mParent.getString(R.string.msg_not_in_room), Toast.LENGTH_LONG).show();
//                ((BaseActivity) mParent).showErrorMessage(null, mParent.getString(R.string.msg_not_in_room));
                warn(getString(R.string.app_name), getString(R.string.msg_not_in_room));
            }
        });

        chatSendButton.setOnClickListener(v -> {
            // Perform action on click
            dismissKeyboard(imm);
            if (mInRoom) {
                if (messageEdit != null) {
                    BoomtownChat.sharedInstance().sendGroupchatMessage(messageEdit.getText().toString(), false);
                    messageEdit.setText("");
                    scroll();
                } else {
                    Toast.makeText(mParent, mParent.getString(R.string.msg_not_in_room), Toast.LENGTH_LONG).show();
//                    ((BaseActivity) mParent).showErrorMessage(null, mParent.getString(R.string.msg_not_in_room));
                }
            }
        });
    }



    private void connect() {
        if (BoomtownChat.sharedInstance().isConnected())
            showProgressWithMessage(getString(R.string.joining_room));
        else
            showProgressWithMessage(getString(R.string.connecting_to_chat_server));
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!BoomtownChat.sharedInstance().isConnected()) {
                    BoomtownChat.sharedInstance().connectToServerWithJid(
                            mJid,
                            mPassword,
                            mHost,
                            Integer.parseInt(mPort),
                            30
                    );
                }
                joinRoom();
            }
        }).start();
    }


    private void joinRoom() {
        BoomtownChat.sharedInstance().joinRoom(mChatRecord.external_id, mResource);
    }


    private void warn(String title, String message) {
        mParent.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_LIGHT);
                builder.setTitle(title);
                builder.setMessage(message);
                builder.setCancelable(false);

                builder.setPositiveButton("OK", null);
//        builder.setNegativeButton("CANCEL", null);

                final AlertDialog dlg = builder.create();
                if (dlg != null) {
                    dlg.show();
                }
            }
        });
    }

    public void addMention(String mention) {
        for (String key : mChatRecord.participants_eligible.keySet()) {
            Map<String, String> participant = (Map<String, String>) mChatRecord.participants_eligible.get(key);
            if (mention.toLowerCase().startsWith(participant.get("name").toLowerCase())) {
                final String alias = participant.get("alias");
                if (alias != null) {
                    mParent.runOnUiThread(new Runnable() {
                        public void run() {
                            String text = messageEdit.getText().toString();
                            messageEdit.setText(text + alias + " ");
                            messageEdit.setSelection(messageEdit.getText().length());
                        }
                    });
                    break;
                }
            }
        }
    }


    public void dismissKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                mParent.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager == null)
            return;
        if (mParent == null || mParent.getCurrentFocus() == null)
            return;
        inputManager.hideSoftInputFromWindow(mParent.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void dismissKeyboard(InputMethodManager imm) {
        if (mParent == null || mParent.getCurrentFocus() == null) {
            return;
        }
        if (imm.isAcceptingText()) { // verify if the soft keyboard is open
            if (mParent.getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(mParent.getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    public void showKeyboard(InputMethodManager inputManager, EditText textFieldForKeyboardIn) {
        if (inputManager == null) {
            return;
        }
        if (mParent == null || mParent.getCurrentFocus() == null || textFieldForKeyboardIn == null) {
            return;
        }
        textFieldForKeyboardIn.requestFocus();
        inputManager.showSoftInput(textFieldForKeyboardIn, 0);
    }

    public void showAutocompleteList(String text) {
        mAutocompleteTokens = text.trim().split(" ");
        mAutocompleteTokensCount = mAutocompleteTokens.length;

        createAutocompleteList(mAutocompleteTokens[mAutocompleteTokensCount - 1]);

        if (mAutocompleteEntries.size() == 0) {
            messagesContainer.setEnabled(true);
            return;
        }

        int height = mAutocompleteEntries.size() * 44;
        if (height > 240)
            height = 240;
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mAutocompleteListView.getLayoutParams();
        lp.height = height;
        mAutocompleteListView.setLayoutParams(lp);

        mAutocompleteListView.setVisibility(View.VISIBLE);
        mAutocompleteListView.bringToFront();
        messagesContainer.setEnabled(false);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mParent,
                android.R.layout.simple_list_item_1, android.R.id.text1, mAutocompleteEntries);

        // Assign adapter to ListView
        mAutocompleteListView.setAdapter(adapter);

        // ListView Item Click Listener
        mAutocompleteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition = position;

                // ListView Clicked item value
                String itemValue = (String) mAutocompleteListView.getItemAtPosition(position);

                mAutocompleteTokens[mAutocompleteTokensCount - 1] = itemValue;
                String newText = strJoin(mAutocompleteTokens, " ");
                messageEdit.setText(newText);
                mAutocompleteListView.setVisibility(View.INVISIBLE);
                messagesContainer.setEnabled(true);
            }

        });
    }


    private void createAutocompleteList(String text) {
        mAutocompleteEntries.clear();
        if (!text.startsWith("@"))
            return;

        for (String mention : mMentions) {
            if (mention.toLowerCase().startsWith(text.toLowerCase()))
                mAutocompleteEntries.add(mention);
        }
    }


    public static String strJoin(String[] aArr, String sSep) {
        StringBuilder sbStr = new StringBuilder();
        for (int i = 0, il = aArr.length; i < il; i++) {
            if (i > 0)
                sbStr.append(sSep);
            sbStr.append(aArr[i]);
        }
        return sbStr.toString();
    }

    public void displayMessage(BoomtownChatMessage message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scroll();
    }

    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }


    public void selectUpload() {
        if (mUploadRequested)
            return;

        mUploadRequested = true;

        ImageView image = new ImageView(mParent);
//        image.setImageDrawable(new BitmapDrawable(getResources(), ((BaseActivity)mParent).mOriginalImage));

//        AlertDialog.Builder builder =
//                new AlertDialog.Builder(mParent).
//                        setMessage(mParent.getString(R.string.msgUpload)).
//                        setPositiveButton(mParent.getString(R.string.promptUpload), new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(final DialogInterface dialog, int which) {
//                                mParent.runOnUiThread(new Runnable() {
//                                    public void run() {
//                                        dialog.dismiss();
//                                        String message = messageEdit.getText().toString();
//                                        if ( message==null || message.isEmpty() )
//                                            message = "";
//                                        ((BaseActivity) mParent).showProgressWithMessage(mParent.getString(R.string.msgUploading));
//                                         BoomtownAPI.sharedInstance().apiMembersCommPutfile(mParent.getApplicationContext(),
//                                                ((BaseActivity)mParent).mOriginalImage,
//                                                BoomtownAPI.sharedInstance().currentComm().comm_id,
//                                                message);
//                                        mUploadRequested = false;
//                                    }
//                                });
//                            }
//                        }).
//                        setNegativeButton(mParent.getString(R.string.promptCancel), new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                                mUploadRequested = false;
//                            }
//                        }).
//                        setView(image);
//        builder.create().show();
    }

    public void enableDone(Boolean enable) {
        if (mMenuItemActionDone != null)
            mMenuItemActionDone.setVisible(enable);
    }

    public void showWebView(String url) {
        enableDone(true);

        webView.loadUrl(url);
    }

    public void hideWebView() {
        webView.setVisibility(View.GONE);
        webViewShowing = false;
        enableDone(false);
    }


    private class SimpleWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {

            mParent.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webView.setVisibility(View.VISIBLE);
                    enableDone(true);
                    webViewShowing = true;
                }
            });
        }
    }


    private void cancelIssue() {
        if (mIssue == null)
            return;

        showProgressWithMessage(getString(R.string.cancelling_issue));

        String uri = String.format("%s/issues/cancel/%s", BTConnectAPI.kEndpoint, mIssue.id);

        JSONObject params = new JSONObject();
        try {
            params.put("members_users_id", BTConnectAPI.sharedInstance().membersUsersId);
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        BTConnectAPI.post(getContext(), uri, params, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                hideProgress();
                warn(getString(R.string.app_name), getString(R.string.cancel_failed));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                boolean success = false;

                hideProgress();
                JSONObject jsonObject = BTConnectAPI.successJSONObject(response.body().string());
                if (jsonObject instanceof JSONObject) {
                    mIssue = null;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mHelpButton.removeChat();
                        }
                    });
                }
            }
        });
    }


    /**
     * Example of fetching a provider, and dumping key information associated with the provider.
     */
    private void commGet(final String comm_id) {
        String uri = String.format("%s/comm/get/%s", BTConnectAPI.kEndpoint, comm_id);

        BTConnectAPI.get(getContext(), uri, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
//                if ( mListener != null )
//                    mListener.helpButtonDidFailWithError("", "");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                boolean success = false;

                JSONObject jsonObject = BTConnectAPI.successJSONObject(response.body().string());
                if (jsonObject instanceof JSONObject) {
                    JSONArray resultsArray = jsonObject.optJSONArray("results");
                    if (resultsArray instanceof JSONArray) {
                        JSONObject chatJSON = resultsArray.optJSONObject(0);
                        if (chatJSON instanceof JSONObject) {
                            mChatRecord = new BTConnectChat(chatJSON);
                            success = true;
                        }
                    }
                }

                if (success) {
//                    if ( mListener != null )
//                        mListener.helpButtonDidSetCredentials();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setup();
                        }
                    });
                } else {
//                    if ( mListener != null )
//                        mListener.helpButtonDidFailWithError("", "");
                }
            }
        });
    }


    /**
     * Example of fetching a provider, and dumping key information associated with the provider.
     */
    private void commEnter() {
        String uri = String.format("%s/comm/enter/%s", BTConnectAPI.kEndpoint, mCommId);

        JSONObject params = new JSONObject();
        try {
            params.put("members_users_id", BTConnectAPI.sharedInstance().membersUsersId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        BTConnectAPI.post(getContext(), uri, params, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
//                if ( mListener != null )
//                    mListener.helpButtonDidFailWithError("", "");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                boolean success = false;

                JSONObject jsonObject = BTConnectAPI.successJSONObject(response.body().string());
                if (jsonObject instanceof JSONObject) {
                    JSONArray resultsArray = jsonObject.optJSONArray("results");
                    if (resultsArray instanceof JSONArray) {
                        JSONObject chatJSON = resultsArray.optJSONObject(0);
                        if (chatJSON instanceof JSONObject) {
                            mCommEntered = true;
                            success = true;
                        }
                    }
                }
            }
        });
    }


    /**
     * Example of fetching a provider, and dumping key information associated with the provider.
     */
    private void commExit() {
        String uri = String.format("%s/comm/exit/%s", BTConnectAPI.kEndpoint, mCommId);

        JSONObject params = new JSONObject();
        try {
            params.put("members_users_id", BTConnectAPI.sharedInstance().membersUsersId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        BTConnectAPI.post(getContext(), uri, params, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
//                if ( mListener != null )
//                    mListener.helpButtonDidFailWithError("", "");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                boolean success = false;

                JSONObject jsonObject = BTConnectAPI.successJSONObject(response.body().string());
                if (jsonObject instanceof JSONObject) {
                    JSONArray resultsArray = jsonObject.optJSONArray("results");
                    if (resultsArray instanceof JSONArray) {
                        JSONObject chatJSON = resultsArray.optJSONObject(0);
                        if (chatJSON instanceof JSONObject) {
                            mCommEntered = false;
                            success = true;
                        }
                    }
                }
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (resultCode == Activity.RESULT_OK)
                    handlePhotoFromCamera(data);
                break;
            case SELECT_FILE:
                if (resultCode == Activity.RESULT_OK)
                    handlePhotoFromFile(data);
                break;
            case LOAD_MASKED_WALLET_REQUEST_CODE:
                loadMaskedWallet(data);
                break;
            case LOAD_FULL_WALLET_REQUEST_CODE:
                loadFullWallet(data);
                break;
            default:
                break;
        }
    }


    public void loadMaskedWallet(Intent data) {
//        MaskedWallet maskedWallet = data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);
//        FullWalletRequest fullWalletRequest = FullWalletRequest.newBuilder()
//                .setCart(Cart.newBuilder()
//                        .setCurrencyCode("USD")
//                        .setTotalPrice("20.00")
//                        .addLineItem(LineItem.newBuilder() // Identify item being purchased
//                                .setCurrencyCode("USD")
//                                .setQuantity("1")
//                                .setDescription("Premium Llama Food")
//                                .setTotalPrice("20.00")
//                                .setUnitPrice("20.00")
//                                .build())
//                        .build())
//                .setGoogleTransactionId(maskedWallet.getGoogleTransactionId())
//                .build();
//        Wallet.Payments.loadFullWallet(mGoogleApiClient, fullWalletRequest, LOAD_FULL_WALLET_REQUEST_CODE);
    }


    public void loadFullWallet(Intent data) {
//        FullWallet fullWallet = data.getParcelableExtra(WalletConstants.EXTRA_FULL_WALLET);
//        String tokenJSON = fullWallet.getPaymentMethodToken().getToken();
//
//        //A token will only be returned in production mode,
//        //i.e. WalletConstants.ENVIRONMENT_PRODUCTION
//        if (mAndroidPayEnvironment == WalletConstants.ENVIRONMENT_PRODUCTION)
//        {
//            com.stripe.model.Token token = com.stripe.model.Token.GSON.fromJson(
//                    tokenJSON, com.stripe.model.Token.class);
//
//            // TODO: send token to your server
//        }
    }


    public void handlePhotoFromCamera(Intent data) {
        mImage = null;
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mOriginalImage = thumbnail;
        mImage = mOriginalImage; // getclip(thumbnail);
        upload();
    }


    public void handlePhotoFromFile(Intent data) {
        mImage = null;
        Uri selectedImageUri = data.getData();
        String[] projection = {MediaStore.MediaColumns.DATA};
        CursorLoader cursorLoader = new CursorLoader(mParent, selectedImageUri, projection, null, null,
                null);
        Cursor cursor = cursorLoader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();

        String selectedImagePath = cursor.getString(column_index);

        Bitmap bm;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(selectedImagePath, options);
        final int REQUIRED_SIZE = 200;
        int scale = 1;
        while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                && options.outHeight / scale / 2 >= REQUIRED_SIZE)
            scale *= 2;
        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(selectedImagePath, options);

        mOriginalImage = ExifUtil.rotateBitmap(selectedImagePath, bm);

        mImage = mOriginalImage; // getclip(mOriginalImage);
        upload();
    }


    public void upload() {
//        BoomtownAPI.sharedInstance().sendNotification(this, BoomtownAPI.kImageCaptured);
        switch (mUploadType) {
            case UPLOAD_TYPE_NONE:
//                retrieveKB("39e163ce-68f2-40b8-977e-dc72f5c33163/8f7f4990-e5e9-4ffc-b3ff-1dd300844512");
                retrieveKB("8745d074-cc4f-48b8-9ec8-278483ced3e8/e91d67cf-d1c1-4c8f-9b84-68f49aa143c2");
                commPutFile(mCommId, mImage);
                break;
            case UPLOAD_TYPE_AVATAR:
                showProgressWithMessage("Uploading photo");
//                BoomtownAPI.sharedInstance().apiImageUpload(getApplicationContext(), mImage, mImageType);
                break;

            case UPLOAD_TYPE_CHAT:
                break;

            default:
                break;
        }
    }


    public void selectImage(Activity activity, int imageType, int uploadType) {
        mImageType  = imageType;
        mUploadType = uploadType;
        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    if (ContextCompat.checkSelfPermission(mParent, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, REQUEST_CAMERA);
                    } else {
                        Toast.makeText(mParent, "You do not have permission to use the camera.", Toast.LENGTH_LONG).show();
                    }
                } else if (items[item].equals("Choose from Library")) {
                    if (ContextCompat.checkSelfPermission(mParent, Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                    } else {
                        Toast.makeText(mParent, "You do not have permission to use the gallery.", Toast.LENGTH_LONG).show();
                    }

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    public RoundedBitmapDrawable roundBitmap(Bitmap drawable) {
        RoundedBitmapDrawable d =
                RoundedBitmapDrawableFactory.create(getResources(), drawable);
        d.setCircular(true);
        return d;
    }

    public Bitmap getclip(Bitmap bitmapIn) {
        Bitmap bitmap = scaleCenterCrop(bitmapIn);
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public Bitmap scaleCenterCrop(Bitmap source) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        int newHeight;
        int newWidth;

        newWidth = (sourceWidth <= sourceHeight) ? sourceWidth : sourceHeight;
        newHeight = newWidth;

        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the bigger
        // of these two.
        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        // Let's find out the upper left coordinates if the scaled bitmap
        // should be centered in the new size give by the parameters
        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        // The target rectangle for the new, scaled version of the source bitmap will now
        // be
        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        // Finally, we create a new bitmap of the specified size and draw our new,
        // scaled bitmap onto it.
        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, null);

        return dest;
    }


    public void commPutFile(String commId, Bitmap image) {
        String url = BTConnectAPI.kV3Endpoint + "/chat/microFilePut";

        JSONObject params = new JSONObject();
        try {
            params.put("id", commId);
            params.put("customers_users_id",BTConnectAPI.sharedInstance().membersUsersId);
            params.put("file_tag", "attachment");
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        BTConnectAPI.post(getContext(), url, params, image, "image", new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                hideProgress();
                warn(getString(R.string.app_name), getString(R.string.warn_unable_to_add_attachmemt));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                boolean success = false;

                hideProgress();
                String responseBody = response.body().string();
                JSONObject jsonObject = BTConnectAPI.successJSONObject(responseBody);
                if (jsonObject instanceof JSONObject) {
                    if ( jsonObject.has("results") ) {
                        try {
                            JSONArray resultsArray = jsonObject.getJSONArray("results");
                            if ( resultsArray.length() > 0 ) {
                                JSONObject resultJSON = resultsArray.getJSONObject(0);

                                String payload = resultJSON.toString();
                                if (payload != null) {
                                    Log.d(TAG, payload);
                                    if (mInRoom) {
                                        BoomtownChat.sharedInstance().sendGroupchatMessage(payload, true);
                                    }

                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        warn(getString(R.string.app_name), getString(R.string.warn_unable_to_add_attachmemt));
                    }
                }
            }
        });
    }

    public void retrieveKB(String id) {
//        String query = "";
//        try {
//            query = "/" + URLEncoder.encode(id, "utf-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        String url = BTConnectAPI.kV3Endpoint + "/kb/get?id=" + id;

        BTConnectAPI.get(getContext(), url, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                warn(getString(R.string.app_name), getString(R.string.warn_unable_to_retrieve_kb));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                boolean success = false;

                JSONObject jsonObject = BTConnectAPI.successJSONObject(response.body().string());
                if (jsonObject instanceof JSONObject) {
                    if ( jsonObject.has("results")) {
//                        success = true;
//                        Intent intent = new Intent(mParent, KBActivity.class);
//                        intent.putExtra(KBActivity.ARG_URL, url);
//                        intent.putExtra(KBActivity.ARG_TITLE, this.kbTitle);
//                        startActivity(intent);
                    }
                }
                if ( success == false ) {
                    warn(getString(R.string.app_name), getString(R.string.warn_unable_to_retrieve_kb));
                }
            }
        });
    }


    public static class AutocompletePredictor {

        public static final String BLANK = "";
        public static final String OPEN_PARENS = "(";
        public static final String CLOSED_PARENS = ")";
        public static final String AT_MENTION = "@";
        public static final String SPACE = " ";
        public static final String MAP_KEY_TXT = "txt";
        public static final String MAP_KEY_IMG = "img";

        private List<String> mentions;
        private List<String> acListEntriesMention;
        private WeakReference<Activity> act;
        private WeakReference<ListView> acListView;
        private WeakReference<ListView> msgContainerListView;
        private WeakReference<EditText> msgEditText;
        private WeakReference<Map<String, BoomtownChatMessage.Emoticon>> emoticonsMap;
        private int lastOpenParensIndex;
        private int lastSpaceIndex;
        private int lastATMentionIndex;
        private String mentionToken;
        private String emoticonToken;

        /**
         * @param autoCompleteListView
         * @param messagesContainer
         */
        public AutocompletePredictor(WeakReference<Activity> parent,
                                     WeakReference<ListView> autoCompleteListView,
                                     WeakReference<ListView> messagesContainer,
                                     WeakReference<EditText> messageEdit,
                                     WeakReference<Map<String, BoomtownChatMessage.Emoticon>> emoticonsMap) {
            if (parent == null) {
                throw new IllegalArgumentException("parent cannot be null");
            }
            if (autoCompleteListView == null) {
                throw new IllegalArgumentException("listview cannot be null");
            }
            if (messagesContainer == null) {
                throw new IllegalArgumentException("container listview cannot be null");
            }
            if (messageEdit == null) {
                throw new IllegalArgumentException("edittext cannot be null");
            }
            this.acListView = autoCompleteListView;
            this.msgContainerListView = messagesContainer;
            this.msgEditText = messageEdit;
            this.act = parent;
            this.emoticonsMap = emoticonsMap;
            if (this.emoticonsMap == null || this.emoticonsMap.get() == null) {
                Map<String, BoomtownChatMessage.Emoticon> noop = new HashMap<>();
                this.emoticonsMap = new WeakReference<Map<String, BoomtownChatMessage.Emoticon>>(noop);
            }
            mentions = new ArrayList<>();
            initPos();
        }

        /**
         * reset {@link this#lastOpenParensIndex} and {@link this#lastSpaceIndex}
         * that track parsing positions; also set {@link this#mentionToken}
         * and {@link this#emoticonToken} to {@link this#BLANK}.
         */
        protected void initPos() {
            this.lastOpenParensIndex = -1;
            this.lastSpaceIndex = -1;
            this.lastATMentionIndex = -1;
            this.mentionToken = BLANK;
            this.emoticonToken = BLANK;
        }

        /**
         * @return the mentions list.
         */
        public List<String> getMentions() {
            return mentions;
        }

        /**
         * @param mention mention to add
         */
        public void addMention(String mention) {
            if (mention == null) {
                return;
            }
            mentions.add(mention);
        }

        /**
         * Show auto complete list.  The list to be shown is based on the latest character typed in text.
         * Supports display of emoticons and mention lists, shown distinctly and not simultaneously.
         * Multiple emoticons and/or mentions in one text are supported.  Any existing
         * replacements/spans will remain intact.
         *
         * @param fullText entire text field used to create an autocomplete list
         */
        public void showAutoCompleteList(final Editable fullText) {
            if (fullText == null) {
                return;
            }
            final SpannableStringBuilder text = new SpannableStringBuilder(fullText);
            if (fullText.toString().endsWith(OPEN_PARENS)) {
                lastOpenParensIndex = text.length() - 1;
            } else if (fullText.toString().endsWith(CLOSED_PARENS)) {
                lastOpenParensIndex = -1;
            } else if (fullText.toString().endsWith(AT_MENTION)) {
                lastATMentionIndex = text.length() - 1;
            } else if (fullText.toString().endsWith(SPACE)) {
                lastSpaceIndex = text.length() - 1;
                lastATMentionIndex = -1;
                lastOpenParensIndex = -1;
            }
            if (lastOpenParensIndex != -1 && !fullText.toString().endsWith(OPEN_PARENS)) {
                if (lastOpenParensIndex >= text.length()) {
                    lastOpenParensIndex = text.length() - 1;
                }
                emoticonToken = text.toString().substring(lastOpenParensIndex, text.length());
                buildEmoticonsListViewAdapter(text, acListView, msgContainerListView, emoticonToken, emoticonsMap, act.get().getApplicationContext());
            } else if (lastATMentionIndex != -1 && !fullText.toString().endsWith(AT_MENTION)) {
                int tokenStart = lastATMentionIndex + 1;
                mentionToken = text.toString().substring(tokenStart, text.length());
                buildMentionsListViewAdapter(text, acListView, msgContainerListView, mentionToken, act.get().getApplicationContext());
            }
        }

        /**
         * Build the emoticons autocomplete list view adapter.
         *
         * @param text
         * @param acListView
         * @param msgContainerListView
         * @param emoticonToken
         * @param emoticonsMap
         * @param ctx
         */
        public void buildEmoticonsListViewAdapter(final SpannableStringBuilder text,
                                                  final WeakReference<ListView> acListView,
                                                  final WeakReference<ListView> msgContainerListView,
                                                  final String emoticonToken,
                                                  final WeakReference<Map<String, BoomtownChatMessage.Emoticon>> emoticonsMap,
                                                  final Context ctx) {
            if (emoticonToken == null || emoticonToken.length() < 1) {
                acListView.get().setAdapter(null);
                acListView.get().setVisibility(View.INVISIBLE);
                msgContainerListView.get().setEnabled(true);
                return;
            }
            final List<BoomtownChatMessage.Emoticon> acListEntriesEmoticon = buildAutocompleteEmoticonsList(emoticonToken
                            .replace(OPEN_PARENS, BLANK).replace(CLOSED_PARENS, BLANK),
                    emoticonsMap.get());

            setACListLayoutAttibutes(ctx, acListView.get(), acListEntriesEmoticon);
            msgContainerListView.get().setEnabled(false);

            final EmoticonListAdapter acAdapter = new EmoticonListAdapter(act.get().getApplicationContext(),
                    R.layout.emoticon_list_row, acListEntriesEmoticon);
            // Assign adapter to ListView
            acListView.get().setAdapter(acAdapter);
            acListView.get().setClickable(true);
            // ListView Item Click Listener
            acListView.get().setOnItemClickListener((parent, view, position, id) -> {
                // ListView Clicked item index
                int itemPosition = position;
                // ListView Clicked item value
                BoomtownChatMessage.Emoticon chosenValue = acAdapter.getItem(position);
                // delete start of emoticon from orig string
                text.delete(lastOpenParensIndex, text.length());
                // rebuild text value
                text.append(OPEN_PARENS + chosenValue.getName() + CLOSED_PARENS);
                // span emoticon text with image
                DynamicDrawableSpan span = BoomtownChatMessage.buildDynamicDrawableSpan(AutocompletePredictor.this.act.get().getApplicationContext(),
                        AutocompletePredictor.this.msgEditText.get(),
                        chosenValue,
                        msgEditText.get().getLineHeight(),
                        msgEditText.get().getLineHeight());
                if (span != null) {
                    text.setSpan(span, lastOpenParensIndex, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                // reset text
                msgEditText.get().setText(text);
                msgEditText.get().setSelection(text.toString().length());
                lastOpenParensIndex = -1;
                msgContainerListView.get().setEnabled(true);
                acListView.get().setVisibility(View.INVISIBLE);
                if (!act.get().isFinishing()) {
                    act.get().runOnUiThread(() -> {
                        acListEntriesEmoticon.clear();
                        acAdapter.notifyDataSetChanged();
                    });
                }
            });
        }

        /**
         * Build the mention autocomplete list view adapter.
         *
         * @param text
         * @param acListView
         * @param msgContainerListView
         * @param mentionToken
         * @param ctx
         */
        public void buildMentionsListViewAdapter(final SpannableStringBuilder text,
                                                 final WeakReference<ListView> acListView,
                                                 final WeakReference<ListView> msgContainerListView,
                                                 final String mentionToken,
                                                 final Context ctx) {
            if (mentionToken == null || mentionToken.length() < 1) {
                acListView.get().setAdapter(null);
                acListView.get().setVisibility(View.INVISIBLE);
                msgContainerListView.get().setEnabled(true);
                return;
            }
            acListEntriesMention = buildAutocompleteMentionsList(mentionToken, mentions);

            setACListLayoutAttibutes(ctx, acListView.get(), acListEntriesMention);
            msgContainerListView.get().setEnabled(false);

            final ArrayAdapter<String> acAdapter = new ArrayAdapter<String>(act.get(),
                    android.R.layout.simple_list_item_1, android.R.id.text1, acListEntriesMention);
            // assign adapter to ListView
            acListView.get().setAdapter(acAdapter);
            // set ListView item click listener
            acListView.get().setOnItemClickListener((parent, view, position, id) -> {
                // ListView clicked item index
                int itemPosition = position;
                // ListView Clicked item value
                String itemValue = (String) acListView.get().getItemAtPosition(position);
                // delete start of mention from orig string
                text.delete(lastATMentionIndex, lastATMentionIndex + mentionToken.length() + 1);
                // rebuild text value
                text.append(itemValue + SPACE);
                msgEditText.get().setText(text.toString());
                msgEditText.get().setSelection(text.toString().length());
                msgContainerListView.get().setEnabled(true);
                acListView.get().setVisibility(View.INVISIBLE);
                if (!act.get().isFinishing()) {
                    act.get().runOnUiThread(() -> {
                        acListEntriesMention.clear();
                        acAdapter.notifyDataSetChanged();
                    });
                }
            });
        }

        /**
         * Set height and visibility for given listview.
         *
         * @param ctx
         * @param acListView
         * @param acListEntries
         */
        public void setACListLayoutAttibutes(Context ctx, ListView acListView, List<?> acListEntries) {
            float density = ctx.getResources().getDisplayMetrics().density;
            int height = (int) ((float) acListEntries.size() * 44f * density + 40f);
            if (height > ((int) 400f * density)) {
                height = (int) (400f * density);
            }
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) acListView.getLayoutParams();
            lp.height = height;
            acListView.setLayoutParams(lp);
            acListView.setVisibility(View.VISIBLE);
            acListView.bringToFront();
        }

        /**
         * Returns a list of all known mentions that start with given text.
         *
         * @param text         the token to search for
         * @param mentionsList a list of known mentions
         * @return an empty list if text does not start with "@";
         * otherwise a list of mentions for the mention autocomplete adapter
         */
        protected List<String> buildAutocompleteMentionsList(final String text, List<String> mentionsList) {
            List<String> ret = new ArrayList<>();
            for (String mention : mentionsList) {
                if (mention.substring(1).toLowerCase().startsWith(text.toLowerCase())) {
                    ret.add(mention);
                }
            }
            return ret;
        }

        /**
         * @param text
         * @return a list of emoticons that start with the given text
         */
        protected List<BoomtownChatMessage.Emoticon> buildAutocompleteEmoticonsList(final String text,
                                                                                    final Map<String, BoomtownChatMessage.Emoticon> emoticonsMap) {
            if (text == null || emoticonsMap == null) {
                return null;
            }
            List<BoomtownChatMessage.Emoticon> ret = new ArrayList<>();
            for (String key : emoticonsMap.keySet()) {
                if (key.startsWith(text)) {
                    ret.add(emoticonsMap.get(key));
                }
            }
            return ret;
        }

    }

    /**
     * @author fbeachler
     */
    private class ChatMessageEditWatcher implements TextWatcher {

        private WeakReference<InputMethodManager> imm;

        public ChatMessageEditWatcher(WeakReference<InputMethodManager> imm) {
            this.imm = imm;
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().length() == 0) {
                dismissKeyboard(imm.get());
                messagesContainer.setEnabled(true);
                mAutocompleteListView.setVisibility(View.GONE);
                chatSendButton.setEnabled(false);
            } else {
                chatSendButton.setEnabled(true);
                acPredictor.showAutoCompleteList(s);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // noop
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // noop
        }
    }

    /**
     * @author fbeachler
     */
    public static class EmoticonListAdapter extends ArrayAdapter<BoomtownChatMessage.Emoticon> {

        public EmoticonListAdapter(Context context, int resource) {
            super(context, resource);
        }

        public EmoticonListAdapter(Context context, int resource, List<BoomtownChatMessage.Emoticon> items) {
            super(context, resource, items);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.emoticon_list_row, null);
            }
            BoomtownChatMessage.Emoticon p = getItem(position);
            if (p != null) {
                TextView tt1 = v.findViewById(R.id.txt);
                WebView iv1 = v.findViewById(R.id.img);
                if (tt1 != null) {
                    tt1.setText(String.format("(%s)", p.getName()));
                }
                if (iv1 != null) {
                    String data = null;
                    switch (p.getType()) {
                        case ASSET_GIF:
                        case ASSET_PNG:
                            data = "<body> <img src = \"" + p.getId() + "\"/></body>";
                            break;
                        default:
                            break;
                    }
                    iv1.loadDataWithBaseURL("file:///android_asset/", data, "text/html", "UTF-8", null);
                }
            }
            return v;
        }

    }

    public Map<String, BoomtownChatMessage.Emoticon> getChatEmoticons() {
        return chatEmoticons;
    }

    public void setChatEmoticons(Map<String, BoomtownChatMessage.Emoticon> chatEmoticons) {
        this.chatEmoticons = chatEmoticons;
    }

    /**
     * Get emoticon images from assets folder.
     *
     * @param path asset path (from root) to index for emoticons
     * @return an empty map if no "emoticon_*" drawables are found, otherwise a map of emoticon names and asset paths
     */
    public Map<String, BoomtownChatMessage.Emoticon> buildEmoticonsIndexFromAssets(String path) {
        Map<String, BoomtownChatMessage.Emoticon> ret = new HashMap<>();
        if (path == null) {
            path = "";
        }
        try {
            String[] list = mParent.getAssets().list(path);
            if (list != null && list.length > 0) {
                for (String f : list) {
                    String n = f.toLowerCase();
                    if (n.startsWith(BoomtownChatMessage.EMOTICON)) {
                        String key = n.replace(BoomtownChatMessage.EMOTICON + BoomtownChatMessage.UNDERSCORE, "")
                                .replace(BoomtownChatMessage.Emoticon.EXT_PNG, "")
                                .replace(BoomtownChatMessage.Emoticon.EXT_GIF, "");
                        BoomtownChatMessage.Emoticon e = new BoomtownChatMessage.Emoticon(key, path + BoomtownChatMessage.SLASH + f);
                        ret.put(key, e);
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return ret;
    }

    /**
     * Use reflection to get all drawables in {@link com.goboomtown.boomtownchat.R.drawable}
     * with a name starting with "emoticon_" and build a map of resource IDs.
     *
     * @return an empty map if no "emoticon_*" drawables are found, otherwise a map of emoticon names and resource IDs
     */
    public Map<String, BoomtownChatMessage.Emoticon> buildEmoticonsIndexFromResources() {
        Map<String, BoomtownChatMessage.Emoticon> ret = new HashMap<>();
//        R.drawable gsDrawables = new R.drawable();
//        Field[] fields = gsDrawables.getClass().getFields();
//        for (Field f : fields) {
//            String n = f.getName().toLowerCase();
//            if (n.startsWith("emoticon")) {
//                String key = n.replace("emoticon_", "");
//                try {
//                    BoomtownChatMessage.Emoticon e = new BoomtownChatMessage.Emoticon(key, f.getInt(gsDrawables));
//                    ret.put(key, e);
//                } catch (IllegalAccessException e) {
//                    Log.w(TAG, Log.getStackTraceString(e));
//                }
//            }
//        }
        return ret;
    }

}
