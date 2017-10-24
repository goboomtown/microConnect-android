package com.goboomtown.microconnect.sample.plainview;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.goboomtown.microconnect.btconnecthelp.activity.ChatFragment;
import com.goboomtown.microconnect.btconnecthelp.view.BTConnectHelpButton;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements BTConnectHelpButton.BTConnectHelpButtonListener {

    public static final String TAG = "MainActivity";

    public static final String CRED_TOKEN = "042F0714883D378313F8";
    public static final String CRED_SECRET = "22d245384b8279f1c9cc3ccfa51d8d0a5a7b0161";

    private FrameLayout         mFragmentContainer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragmentContainer  = findViewById(R.id.fragment_container);

        BTConnectHelpButton mHelpButton = findViewById(R.id.helpButton);
        mHelpButton.setListener(this);

        mHelpButton.memberID 			= "WA3QMJ";
        mHelpButton.memberUserID 		= "WA3QMJ-5XK"; //@"WA3QMJ-2QE";
        mHelpButton.memberLocationID 	= "WA3QMJ-FYH"; //@"WA3QMJ-JVE";

        mHelpButton.supportWebsiteURL 	= Uri.parse("http://example.com");
        mHelpButton.supportEmailAddress  = "support@example.com";
        mHelpButton.supportPhoneNumber 	= "1-888-555-2368";

        mHelpButton.setCredentials(CRED_TOKEN, CRED_SECRET);

        Map<String, String> myPubData = new HashMap<>();
        myPubData.put("public", "fooData");
        Map<String, String> myPrivData = new HashMap<>();
        myPrivData.put("private", "someEncryptedData");

        mHelpButton.advertiseServiceWithPublicData(myPubData, myPrivData);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "#onResume complete");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "#onPause complete");
    }

    @Override
    public void helpButtonDidFailWithError(final String description, final String reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                builder.setTitle(description);
                builder.setMessage(reason);
                builder.setCancelable(false);

                builder.setPositiveButton("OK", null);

                final AlertDialog dlg = builder.create();
                if (dlg != null) {
                    dlg.show();
                }
            }
        });
    }

    @Override
    public void helpButtonDidSetCredentials() {
        Log.d(TAG, "#helpButtonDidSetCredentials");
    }

    @Override
    public void helpButtonDisplayChatFragment(final ChatFragment chatFragment) {
        Log.d(TAG, "#helpButtonDisplayChatFragment");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, chatFragment)
                        .addToBackStack(null)
                        .commit();
                mFragmentContainer.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void helpButtonSetChatTitle(String title) {
        setTitle(title);
    }

    @Override
    public void helpButtonRemoveChatFragment() {
        getSupportFragmentManager().popBackStack();
        mFragmentContainer.setVisibility(View.GONE);
        setTitle(getString(R.string.app_name));
    }

    @Override
    public void helpButtonDidAdvertiseService() {
        Log.i(TAG, "service advertised successfully");
    }

    @Override
    public void helpButtonDidFailToAdvertiseService() {
        Log.i(TAG, "error when advertising service");
    }

}
