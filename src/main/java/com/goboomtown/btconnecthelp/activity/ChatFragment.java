package com.goboomtown.btconnecthelp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.BoolRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.goboomtown.btconnecthelp.R;
import com.goboomtown.btconnecthelp.api.BTConnectAPI;
import com.goboomtown.btconnecthelp.model.BTConnectChat;
import com.goboomtown.btconnecthelp.model.BTConnectIssue;
import com.goboomtown.btconnecthelp.view.BTConnectHelpButton;
import com.goboomtown.chat.BoomtownChat;
import com.goboomtown.chat.BoomtownChatMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Larry Borsato on 2016-07-12.
 */

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public BTConnectHelpButton      mHelpButton     = null;
    public  BTConnectIssue          mIssue          = null;
    private String                  mCommId         = null;
    private Boolean                 mCommEntered    = false;
    private BTConnectChat           mChatRecord     = null;
    private JSONObject              mXmppInfo       = null;

    private String                  mJid 		    = null;
    private String                  mPassword 	    = null;
    private String                  mHost 	  	    = null;
    private String                  mPort 	  	    = null;
    private String                  mResource 	    = null;

    public static final int UPLOAD_TYPE_NONE                = 0;
    public static final int UPLOAD_TYPE_AVATAR              = 1;
    public static final int UPLOAD_TYPE_CHAT                = 2;

    public static final int REQUEST_CAMERA                  = 1;
    public static final int SELECT_FILE                     = 2;
    public static final int LOAD_MASKED_WALLET_REQUEST_CODE = 1000;
    public static final int LOAD_FULL_WALLET_REQUEST_CODE   = 1001;

    //    public static final int mAndroidPayEnvironment = WalletConstants.ENVIRONMENT_TEST;
//    public GoogleApiClient mGoogleApiClient;
    public String          mPurchasePrice;
    public String          mPurchaseDescription;

    public int      mType;
    public Bitmap   mImage;
    public Bitmap   mOriginalImage;
    public int      mImageType;
    public int      mUploadType;
    public Boolean  mChatUpload = false;

    private ProgressDialog  mProgress;

    private OnFragmentInteractionListener mListener;

    private EditText messageET;
    private ListView messagesContainer;
    private Button sendBtn;
    private ChatAdapter adapter;
    private ArrayList<BoomtownChatMessage> chatHistory;
    private ImageButton chatUploadButton;
    private Button chatSendButton;
    private EditText messageEdit;
    private TextView titleView;
    private ListView            mAutocompleteListView;
    private ArrayList<String>   mAutocompleteEntries;
    private ArrayList<String>   mMentions;
    private String[]            mAutocompleteTokens;
    private int                 mAutocompleteTokensCount;
    public ChatFragment activity;
    private AlertDialog alertDialog;
    public View    mView;
    private Boolean             mInSetup = false;

    public Activity     mParent;
    public Boolean      mUploadRequested;
    public WebView webView = null;
    public Boolean  webViewShowing = false;
    private RelativeLayout webViewFrame = null;
    public MenuItem    mMenuItemActionDone;

    private Button  mBtnGetVideoChatHelp;

    public Boolean      mInRoom;

    public String   senderId;
    public String   senderDisplayName;
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
//        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mAutocompleteEntries    = new ArrayList<String>();
        mMentions               = new ArrayList<String>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        mView = view;

        chatUploadButton = (ImageButton) view.findViewById(R.id.chatUploadButton);
        chatSendButton   = (Button) view.findViewById(R.id.chatSendButton);

        chatUploadButton.setEnabled(true);
        chatSendButton.setEnabled(false);

        mUploadRequested = false;

        String title = String.format("%s #%s", getString(R.string.ticket), mIssue.reference_num);
//        getActivity().setTitle(title);
        mHelpButton.setChatTitle(title);

        mBtnGetVideoChatHelp = (Button) view.findViewById(R.id.btn_get_video_chat_help);
        mBtnGetVideoChatHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = String.format("goboomtownconnect://prod/member/issue/read?issue_id=%s", mIssue.id);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                getContext().startActivity(intent);

            }
        });

//        mXmppInfo = BTConnectAPI.extractXmppInformation(mIssue.xmpp_data);
        mXmppInfo = BoomtownChat.extractXmppInformation(mIssue.xmpp_data, BTConnectAPI.sharedInstance().getKey());
        if ( mXmppInfo != null )
        {
            setXmppInfo(mXmppInfo);
            if ( mCommId != null )
            {
                commGet(mCommId);
            }
        }

        initControls(view);

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
        if ( id == R.id.action_done ) {
            if ( webViewShowing ) {
                hideWebView();
                return true;
            }
        }
        if ( id == R.id.action_resolve ) {
            if ( webViewShowing ) {
                hideWebView();
            }
            cancelIssue();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void showProgressBar(final String message, final boolean show)
    {
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

    public void showProgressWithMessage(final String message)
    {
        mParent.runOnUiThread(new Runnable() {
            public void run() {
                if ( mProgress != null ) {
                    mProgress.dismiss();
                    mProgress = null;
                }
                if ( !mParent.isFinishing() )
                    mProgress = ProgressDialog.show(mParent, null, message, true);
            }
        });
    }

    public void hideProgress()
    {
        mParent.runOnUiThread(new Runnable() {
            public void run() {
                if ( mProgress != null ) {
                    mProgress.dismiss();
                    mProgress = null;
                }
            }
        });
    }

    private void setXmppInfo(JSONObject info)
    {
        mCommId 	= info.optString("comm_id");
        mJid 		= info.optString("jid");
        mPassword 	= info.optString("password");
        mHost 	  	= info.optString("host");
        mPort 	  	= info.optString("port");
        mResource 	= info.optString("resource");
    }

    public void setup() {
        if ( mInSetup )
            return;

        mInSetup = true;
//        mChatRecord = new BTConnectChat();

//        setTitle("Boomtown Chat: " + chatRecord.title);
//        titleView.setText(chatRecord.title);

        String me = mResource;
        String[] tokens = me.split(";");
        senderId = tokens[0];
        Map<String, String> participantInfo = (Map<String, String>) mChatRecord.participants_eligible.get(senderId);
        if ( participantInfo != null )
            senderDisplayName = (String) participantInfo.get("name");
        if ( senderDisplayName == null )
            senderDisplayName = senderId;

        mMentions.add("@all");
        for ( String key : mChatRecord.participants_eligible.keySet() )
        {
            Map<String, String> participant = (Map<String, String>) mChatRecord.participants_eligible.get(key);
            mMentions.add(participant.get("alias"));
        }
        Collections.sort(mMentions);

        BoomtownChat.sharedInstance().context = mParent;
        BoomtownChat.sharedInstance().participants_eligible = mChatRecord.participants_eligible;
        BoomtownChat.sharedInstance().setListener(new BoomtownChat.BoomtownChatListener() {
            @Override
            public void onConnect() {
                hideProgress();
                showProgressWithMessage("Joining room");
            }

            @Override
            public void onTimeoutConnect() {
                Log.d("ChatFragment", "onTimeoutConnect");
            }

            @Override
            public void onNotAuthenticate() {
                Log.d("ChatFragment", "onNotAuthenticate");
            }

            @Override
            public void onDisconnect() {
                Log.d("ChatFragment", "onDisconnect");

            }

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
//                        chatUploadButton.setEnabled(true);
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

        });

        chatHistory = new ArrayList<BoomtownChatMessage>();
        adapter = new ChatAdapter(mParent, new ArrayList<BoomtownChatMessage>());
//        adapter.chatFrament = this;
        adapter.chatFragment = this;
        messagesContainer.setAdapter(adapter);

        commEnter();

        if ( mChatRecord!=null && mChatRecord.external_id!=null && !mChatRecord.external_id.isEmpty() )
            BoomtownChat.sharedInstance().roomJid = mChatRecord.external_id;

        connect();

    }

    private void initControls(View view) {
        titleView = (TextView) view.findViewById(R.id.title);
        messagesContainer = (ListView) view.findViewById(R.id.messagesContainer);
        mAutocompleteListView = (ListView) view.findViewById((R.id.autocompleteList));

//        RelativeLayout container = (RelativeLayout) view.findViewById(R.id.chatContainer);
        messageEdit = (EditText) view.findViewById(R.id.messageEdit);
        messageEdit.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                if ( s.toString().length() == 0 ) {
                    dismissKeyboard();
                    messagesContainer.setEnabled(true);
                    mAutocompleteListView.setVisibility(View.GONE);
                    chatSendButton.setEnabled(false);
                }
                else {
                    chatSendButton.setEnabled(true);
                    showAutocompleteList(s.toString());
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
//                if ( mInRoom ) {
//                    ((BaseActivity) getActivity()).selectImage(getActivity(), 0, BaseActivity.UPLOAD_TYPE_NONE);
//                    if (messageEdit != null) {
//                        messageEdit.setText("");
//                        scroll();
//                    }
//                } else {
//                    ((BaseActivity)mParent).showErrorMessage(null, mParent.getString(R.string.msg_not_in_room));
//                }
            }
        });

        chatSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                dismissKeyboard();
                if ( mInRoom ) {
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
        if (webView == null)
        {
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


    private void connect()
    {
        if ( BoomtownChat.sharedInstance().isConnected() )
            showProgressWithMessage(getString(R.string.joining_room));
        else
            showProgressWithMessage(getString(R.string.connecting_to_chat_server));
        new Thread(new Runnable() {
            @Override
            public void run() {
                if ( !BoomtownChat.sharedInstance().isConnected() ) {
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


    private void joinRoom()
    {
        BoomtownChat.sharedInstance().joinRoom(mChatRecord.external_id, mResource);
    }


    private void warn(String title, String message) {
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

    public void addMention(String mention)
    {
        for ( String key : mChatRecord.participants_eligible.keySet() )
        {
            Map<String, String> participant = (Map<String, String>) mChatRecord.participants_eligible.get(key);
            if ( mention.toLowerCase().startsWith(participant.get("name").toLowerCase())) {
                final String alias = participant.get("alias");
                if (alias != null)
                {
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


    public void dismissKeyboard()
    {
        InputMethodManager inputManager = (InputMethodManager)
                mParent.getSystemService(Context.INPUT_METHOD_SERVICE);
        if ( inputManager == null )
            return;
        if ( mParent==null || mParent.getCurrentFocus()==null )
            return;
        inputManager.hideSoftInputFromWindow(mParent.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void showAutocompleteList(String text)
    {
        mAutocompleteTokens = text.trim().split(" ");
        mAutocompleteTokensCount = mAutocompleteTokens.length;

        createAutocompleteList(mAutocompleteTokens[mAutocompleteTokensCount - 1]);

        if ( mAutocompleteEntries.size() == 0 )
        {
            messagesContainer.setEnabled(true);
            return;
        }

        int height = mAutocompleteEntries.size()*44;
        if ( height > 240 )
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


    private void createAutocompleteList(String text)
    {
        mAutocompleteEntries.clear();
        if ( !text.startsWith("@") )
            return;

        for ( String mention : mMentions )
        {
            if ( mention.toLowerCase().startsWith(text.toLowerCase()) )
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
        if ( mUploadRequested )
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
        if ( mMenuItemActionDone != null )
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


    private void cancelIssue()
    {
        if ( mIssue == null )
            return;

        showProgressWithMessage(getString(R.string.cancelling_issue));

        String uri = String.format("%s/issues/cancel/%s", BTConnectAPI.kEndpoint, mIssue.id);

        JSONObject params = new JSONObject();
        try {
            params.put("members_users_id", BTConnectAPI.sharedInstance().membersUsersId);
        } catch (JSONException e) {
            e.printStackTrace();
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
     *
     */
    private void commGet(final String comm_id)
    {
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
                    if ( resultsArray instanceof JSONArray ) {
                        JSONObject chatJSON = resultsArray.optJSONObject(0);
                        if ( chatJSON instanceof JSONObject )
                        {
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
     *
     */
    private void commEnter()
    {
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
                    if ( resultsArray instanceof JSONArray ) {
                        JSONObject chatJSON = resultsArray.optJSONObject(0);
                        if ( chatJSON instanceof JSONObject )
                        {
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
     *
     */
    private void commExit()
    {
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
                    if ( resultsArray instanceof JSONArray ) {
                        JSONObject chatJSON = resultsArray.optJSONObject(0);
                        if ( chatJSON instanceof JSONObject )
                        {
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
        switch( requestCode ) {
//            case REQUEST_CAMERA:
//                if (resultCode == RESULT_OK)
//                    handlePhotoFromCamera(data);
//                break;
//            case SELECT_FILE:
//                if (resultCode == RESULT_OK)
//                    handlePhotoFromFile(data);
//                break;
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
        mImage = getclip(thumbnail);
        upload();
    }


    public void handlePhotoFromFile(Intent data) {
//        mImage = null;
//        Uri selectedImageUri = data.getData();
//        String[] projection = {MediaStore.MediaColumns.DATA};
//        CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null,
//                null);
//        Cursor cursor = cursorLoader.loadInBackground();
//        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
//        cursor.moveToFirst();
//
//        String selectedImagePath = cursor.getString(column_index);
//
//        Bitmap bm;
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(selectedImagePath, options);
//        final int REQUIRED_SIZE = 200;
//        int scale = 1;
//        while (options.outWidth / scale / 2 >= REQUIRED_SIZE
//                && options.outHeight / scale / 2 >= REQUIRED_SIZE)
//            scale *= 2;
//        options.inSampleSize = scale;
//        options.inJustDecodeBounds = false;
//        bm = BitmapFactory.decodeFile(selectedImagePath, options);
//
//        mOriginalImage = ExifUtil.rotateBitmap(selectedImagePath, bm);
//
//        mImage = getclip(mOriginalImage);
//        upload();
    }


    public void upload() {
//        BoomtownAPI.sharedInstance().sendNotification(this, BoomtownAPI.kImageCaptured);
        switch( mUploadType )
        {
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
//        mImageType  = imageType;
//        mUploadType = uploadType;
//        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };
//        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
//        builder.setTitle("Add Photo!");
//        builder.setItems(items, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int item) {
//                if (items[item].equals("Take Photo")) {
//                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    startActivityForResult(intent, Settings.REQUEST_CAMERA);
//                } else if (items[item].equals("Choose from Library")) {
//                    Intent intent = new Intent(
//                            Intent.ACTION_PICK,
//                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                    intent.setType("image/*");
//                    startActivityForResult(
//                            Intent.createChooser(intent, "Select File"),
//                            Settings.SELECT_FILE);
//                } else if (items[item].equals("Cancel")) {
//                    dialog.dismiss();
//                }
//            }
//        });
//        builder.show();
    }



    public RoundedBitmapDrawable roundBitmap(Bitmap drawable)
    {
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

        newWidth = (sourceWidth<=sourceHeight) ? sourceWidth : sourceHeight;
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



}
