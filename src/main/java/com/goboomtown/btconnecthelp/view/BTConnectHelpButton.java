package com.goboomtown.btconnecthelp.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.appcompat.BuildConfig;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.goboomtown.btconnecthelp.R;
import com.goboomtown.btconnecthelp.R.*;
import com.goboomtown.btconnecthelp.activity.ChatFragment;
import com.goboomtown.btconnecthelp.api.BTConnectAPI;
import com.goboomtown.btconnecthelp.model.BTConnectIssue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * TODO: document your custom view class.
 */
public class BTConnectHelpButton extends View {

    private static final String TAG = BTConnectHelpButton.class.getSimpleName();

    private static final String BTConnectHelpErrorDomain  = "com.goboomtown.btconnecthelp";
    private static final String BTConnectHelpName         = "microConnect";

    private String mExampleString; // TODO: use a default from R.string...
    private int mExampleColor = Color.RED; // TODO: use a default from R.color...
    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
    private Drawable mExampleDrawable;

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;

    private String  providerId;
    private String  microConnectVersion;
    private String  osVersion;
    private Context mContext;

    public interface BTConnectHelpButtonListener {
        public void helpButtonDidFailWithError(String description, String reason);
        public void helpButtonDidSetCredentials();
        public void helpButtonDisplayChatFragment(ChatFragment chatFragment);
        public void helpButtonRemoveChatFragment();
        public void helpButtonSetChatTitle(String title);
    }

    private BTConnectHelpButtonListener mListener;

    /**
 Website URL the help button will take the user to.

 If not null this will be populated by the setCredentialsWithToken:secret: method from the current provider information
 */
    public  Uri     supportWebsiteURL;

/**
 Email address the help button will send email to.

 If not null this will be populated by the setCredentialsWithToken:secret: method from the current provider information
 */
    public  String  supportEmailAddress;

/**
 Phone number the help button will call.

 If not null this will be populated by the setCredentialsWithToken:secret: method from the current provider information
 */
    public  String  supportPhoneNumber;

/**
 The ID of the member (also referred to as teh merchant)
 */
    public  String  memberID;

/**
 The ID of the member user
 */
    public  String	memberUserID;

/**
 The ID of the member user's location
 */
    public  String	memberLocationID;


    public BTConnectHelpButton(Context context) {
        super(context);
        mContext = context;
        init(null, 0);
    }

    public BTConnectHelpButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(attrs, 0);
    }

    public BTConnectHelpButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init(attrs, defStyle);
    }

    private void clicked() {
        Log.d("getHelp", "Clicked");
        showHelpDialog();
    }

    /**
     * Set credentials for API requests
     * @param token     the API token provided by Boomtown
     * @param secret    the API secret provided by Boomtown
     */
    public void setCredentials(String token, String secret)
    {
        BTConnectAPI.setCredentials(token, secret);

        if ( memberID==null || memberUserID==null ||  memberLocationID==null )
        {
            if ( mListener != null ) {
                mListener.helpButtonDidFailWithError(
                        mContext.getString(R.string.error_unable_to_create_issue),
                        mContext.getString(string.reason_member_info_missing)
                );
            }
        }

        BTConnectAPI.sharedInstance().membersId             = memberID;
        BTConnectAPI.sharedInstance().membersUsersId        = memberUserID;
        BTConnectAPI.sharedInstance().membersLocationsId    = memberLocationID;

        int versionCode     = BuildConfig.VERSION_CODE;
        String versionName  = BuildConfig.VERSION_NAME;
        PackageManager manager =  getContext().getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(getContext().getPackageName(), 0);
            microConnectVersion = String.format("%s Build %d", info.versionName, info.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        osVersion = String.format("Android %s (API %d)", Build.VERSION.RELEASE,Build.VERSION.SDK_INT);

        getProvider();
    }

    private void displayChat(BTConnectIssue issue) {
        ChatFragment chatFragment = new ChatFragment();
        chatFragment.mIssue = issue;
        chatFragment.mHelpButton = this;
        if (mListener != null) {
            mListener.helpButtonDisplayChatFragment(chatFragment);
        }
    }

    public void setChatTitle(String title) {
        if (mListener != null) {
            mListener.helpButtonSetChatTitle(title);
        }
    }

    public void removeChat() {
        if ( mListener != null ) {
            mListener.helpButtonRemoveChatFragment();
        }
    }


    private String clientAppIdentifier()
    {
        //  microConnect 1.11, iOS 9.7.1 [ABC-123]
        return String.format("%s %s, %s", BTConnectHelpName, microConnectVersion, osVersion);
    }


    private void init(AttributeSet attrs, int defStyle) {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clicked();
            }
        });

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.BTConnectHelpButton, defStyle, 0);

        mExampleString = a.getString(
                R.styleable.BTConnectHelpButton_exampleString);
        if ( mExampleString == null )
            mExampleString = "";
        mExampleColor = a.getColor(
                R.styleable.BTConnectHelpButton_exampleColor,
                mExampleColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mExampleDimension = a.getDimension(
                R.styleable.BTConnectHelpButton_exampleDimension,
                mExampleDimension);

        if (a.hasValue(R.styleable.BTConnectHelpButton_exampleDrawable)) {
            mExampleDrawable = a.getDrawable(
                    R.styleable.BTConnectHelpButton_exampleDrawable);
            mExampleDrawable.setCallback(this);
        }
        else {
            mExampleDrawable = getResources().getDrawable(drawable.help_slider_gear_light);
            mExampleDrawable.setCallback(this);
        }

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }

    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize(mExampleDimension);
        mTextPaint.setColor(mExampleColor);
        mTextWidth = mTextPaint.measureText(mExampleString);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        // Draw the text.
        canvas.drawText(mExampleString,
                paddingLeft + (contentWidth - mTextWidth) / 2,
                paddingTop + (contentHeight + mTextHeight) / 2,
                mTextPaint);

        // Draw the example drawable on top of the text.
        if (mExampleDrawable != null) {
            mExampleDrawable.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            mExampleDrawable.draw(canvas);
        }
    }

    /**
     * Gets the example string attribute value.
     *
     * @return The example string attribute value.
     */
    public String getExampleString() {
        return mExampleString;
    }

    /**
     * Sets the view's example string attribute value. In the example view, this string
     * is the text to draw.
     *
     * @param exampleString The example string attribute value to use.
     */
    public void setExampleString(String exampleString) {
        mExampleString = exampleString;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example color attribute value.
     *
     * @return The example color attribute value.
     */
    public int getExampleColor() {
        return mExampleColor;
    }

    /**
     * Sets the view's example color attribute value. In the example view, this color
     * is the font color.
     *
     * @param exampleColor The example color attribute value to use.
     */
    public void setExampleColor(int exampleColor) {
        mExampleColor = exampleColor;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example dimension attribute value.
     *
     * @return The example dimension attribute value.
     */
    public float getExampleDimension() {
        return mExampleDimension;
    }

    /**
     * Sets the view's example dimension attribute value. In the example view, this dimension
     * is the font size.
     *
     * @param exampleDimension The example dimension attribute value to use.
     */
    public void setExampleDimension(float exampleDimension) {
        mExampleDimension = exampleDimension;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example drawable attribute value.
     *
     * @return The example drawable attribute value.
     */
    public Drawable getExampleDrawable() {
        return mExampleDrawable;
    }

    /**
     * Sets the view's example drawable attribute value. In the example view, this drawable is
     * drawn above the text.
     *
     * @param exampleDrawable The example drawable attribute value to use.
     */
    public void setExampleDrawable(Drawable exampleDrawable) {
        mExampleDrawable = exampleDrawable;
    }


    public void setListener(BTConnectHelpButtonListener listener)
    {
        mListener = listener;
    }


    private void showHelpDialog()
    {
        final CharSequence[] items = { "Chat with Us", "Web Support", "Email Support", "Phone Support", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                switch( item ) {
                    case 0:
                        createIssue(memberID, memberUserID, memberLocationID);
                        break;
                    case 1:
                        visitWebsite();
                        break;
                    case 2:
                        sendEmail();
                        break;
                    case 3:
                        phone();
                        break;
                    case 4:
                        dialog.dismiss();
                        break;
                    default:
                        break;
                }
            }
        });
        AlertDialog dialog = builder.show();
//        AppCompatTextView messageText = (AppCompatTextView)dialog.findViewById(android.R.id.message);
//        messageText.setGravity(Gravity.CENTER);
//        dialog.setView(messageText);
    }


    private void visitWebsite() {
        Intent intent = new Intent(Intent.ACTION_VIEW, supportWebsiteURL);
        getContext().startActivity(intent);
    }

    private void sendEmail() {
        if ( supportEmailAddress.isEmpty() )
            return;

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{ supportEmailAddress});

        //need this to prompts email client only
        emailIntent.setType("message/rfc822");

        getContext().startActivity(Intent.createChooser(emailIntent, "Select an Email Client:"));
    }


    private void phone() {
        String permission = "android.permission.CALL_PHONE";
        int res = getContext().checkCallingOrSelfPermission(permission);
        if (res == PackageManager.PERMISSION_GRANTED ) {
            Intent intent = new Intent(Intent.ACTION_CALL);

            intent.setData(Uri.parse("tel:" + supportPhoneNumber));
            getContext().startActivity(intent);
        }
    }


    private void createIssue(String members_id, String members_users_id, String members_locations_id) {
        String clientAppIdentifier = clientAppIdentifier();
        JSONObject params = new JSONObject();
        JSONObject issuesJSON = new JSONObject();
        try {
            issuesJSON.put("members_id", members_id);
            issuesJSON.put("members_users_id", members_users_id);
            issuesJSON.put("members_locations_id", members_locations_id);
            issuesJSON.put("user_agent", clientAppIdentifier);

            params.put("issues", issuesJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = String.format("%s/issues/create", BTConnectAPI.kEndpoint);

        BTConnectAPI.post(getContext(), uri, params, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                if (mListener != null) {
                    mListener.helpButtonDidFailWithError("Unable to create issue", e.getLocalizedMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                boolean success = false;
                BTConnectIssue issue = null;
                String message = "";

                JSONObject jsonObject = BTConnectAPI.successJSONObject(response.body().string());
                if (jsonObject instanceof JSONObject) {
                    JSONArray results = jsonObject.optJSONArray("results");
                    if (results instanceof JSONArray && results.length() > 0) {
                        Log.d(TAG, "onResponse");
                        JSONObject issueJSON = null;
                        try {
                            issueJSON = results.getJSONObject(0);
                            issue = new BTConnectIssue(issueJSON);
                            success = true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (issue != null) {
                            displayChat(issue);
                        }
                    }
                }

                if (!success) {
                    if (mListener != null) {
                        mListener.helpButtonDidFailWithError(mContext.getString(R.string.error_unable_to_create_issue), message);
                    }
                }
            }
        });
    }

     private void getProvider()
     {
         String uri = String.format("%s/providers/get", BTConnectAPI.kEndpoint);

         BTConnectAPI.get(getContext(), uri, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                if ( mListener != null )
                    mListener.helpButtonDidFailWithError(mContext.getString(string.error_unable_to_get_provider_info), e.getLocalizedMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                boolean success = false;
                String message = "";

                try {
                    JSONObject jsonObject = BTConnectAPI.successJSONObject(response.body().string());
                    if (jsonObject instanceof JSONObject) {
                        JSONArray resultsArray = jsonObject.optJSONArray("results");
                        if ( resultsArray instanceof JSONArray ) {
                            JSONObject provider = resultsArray.optJSONObject(0);
                            if ( provider instanceof JSONObject )
                            {
                                success = true;
                                providerId = provider.optString("id");
                                if ( provider.has("website") ) {
                                    try {
                                        String url = provider.getString("website");
                                        if ( !url.startsWith("http") )
                                            url = "http://" + url;
                                        supportWebsiteURL = Uri.parse(url);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if ( provider.has("email") ) {
                                    supportEmailAddress = provider.getString("email");
                                }
                                if ( provider.has("phone") ) {
                                        supportPhoneNumber = provider.getString("phone");
                                }
                            }
                        }
                    } else {
                        message = jsonObject.optString("message");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (success) {
                    if ( mListener != null )
                        mListener.helpButtonDidSetCredentials();
                } else {
                    if ( mListener != null )
                        mListener.helpButtonDidFailWithError(mContext.getString(string.error_unable_to_get_provider_info), message);
                }
            }
         });
    }



}
