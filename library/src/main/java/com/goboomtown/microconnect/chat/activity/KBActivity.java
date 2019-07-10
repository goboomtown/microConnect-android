package com.goboomtown.microconnect.chat.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.goboomtown.microconnect.R;
import com.goboomtown.microconnect.chat.fragment.WebViewFragment;

//import com.goboomtown.R;
//import com.goboomtown.api.BoomtownAPI;

public class KBActivity extends BaseActivity {

    public static final String ARG_URL = "url";
    public static final String ARG_TITLE = "title";

    private WebViewFragment webViewFragment;
    public  String          url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kb);
        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getIntent().getStringExtra(KBActivity.ARG_TITLE));
        }
        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
//            arguments.putString(ServiceHistoryDetailFragment.ARG_ITEM_ID,
//                    getIntent().getStringExtra(ServiceHistoryDetailFragment.ARG_ITEM_ID));
            webViewFragment = new WebViewFragment();
            webViewFragment.url = getIntent().getStringExtra(KBActivity.ARG_URL);
            webViewFragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.web_view_container, webViewFragment)
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BoomtownAPI.kApplicationStateChangedAction));

        webViewFragment.loadUrl();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void startVideoSession(BoomtownComm call) {

    }

    @Override
    protected void onAppMsgStartVideoSession(String callId) {

    }

    @Override
    protected void onAppMsgCommPutSuccess(int commType, BoomtownComm call, String kAlert) {

    }

    @Override
    protected void onAppMsgCommGetSuccess(int kTypeVideo, BoomtownComm call, String stringExtra) {

    }

    @Override
    protected void onAppMsgCommEnterSuccess(int commType, BoomtownComm call, int duration) {

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void loadUrl(String url) {
        webViewFragment.url = url;
        webViewFragment.loadUrl();
    }



}
