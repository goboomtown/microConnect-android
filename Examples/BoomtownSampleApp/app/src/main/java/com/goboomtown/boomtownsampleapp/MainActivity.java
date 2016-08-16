package com.goboomtown.boomtownsampleapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.AlertDialog;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.goboomtown.btconnecthelp.activity.ChatFragment;
import com.goboomtown.btconnecthelp.view.BTConnectHelpButton;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity
        implements BTConnectHelpButton.BTConnectHelpButtonListener {

    private BTConnectHelpButton mHelpButton;
    private FrameLayout         mFragmentContainer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHelpButton         = (BTConnectHelpButton) findViewById(R.id.helpButton);
        mFragmentContainer  = (FrameLayout)         findViewById(R.id.fragment_container);

        mHelpButton.setListener(this);

        mHelpButton.memberID 			= "WA3QMJ";
        mHelpButton.memberUserID 		= "WA3QMJ-5XK"; //@"WA3QMJ-2QE";
        mHelpButton.memberLocationID 	= "WA3QMJ-FYH"; //@"WA3QMJ-JVE";

        mHelpButton.supportWebsiteURL 	= Uri.parse("http://example.com");
        mHelpButton.supportEmailAddress  = "support@example.com";
        mHelpButton.supportPhoneNumber 	= "1-888-555-2368";

        mHelpButton.setCredentials("31211E2CC0A30F98ABBD","0a46f159dc5a846d3fa7cf7024adb2248a8bc8ed");
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
        Log.d("MainActivity", "helpButtonDidSetCredentials");
    }

    @Override
    public void helpButtonDisplayChatFragment(final ChatFragment chatFragment) {
        Log.d("MainActivity", "helpButtonDisplayChatFragment");
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
}
