package com.goboomtown.microconnect.chat.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

//import com.goboomtown.api.BoomtownAPI;
//import com.goboomtown.api.Constants;
//import com.goboomtown.core.R;
//import com.goboomtown.core.application.BoomtownApplication;
//import com.goboomtown.core.services.BoomtownServices;
//import com.goboomtown.core.util.ExifUtil;
//import com.goboomtown.core.util.Settings;
//import com.goboomtown.core.util.Utils;
//import com.goboomtown.core.view.helper.DialogHelper;
//import com.goboomtown.core.widget.CustomAlertDialogBuilder;
//import com.goboomtown.model.BoomtownComm;

public abstract class BaseActivity extends AppCompatActivity {

    public final static String TAG = BaseActivity.class.getSimpleName();

    public static final String MUC_CONFERENCE_ID_USER_INITIATED = "USER INITIATED";
    public static final String INTENT_TYPE_IMAGE_SLASH_STAR = "image/*";

    public ProgressDialog mProgress = null;

    public static final String OOVOO_INITIALIZED = "com.boomtown.BaseActivity.OOVOO_INITIALIZED";
    public Boolean ooVooInitialized = false;

    public static final int UPLOAD_TYPE_NONE            = 0;
    public static final int UPLOAD_TYPE_AVATAR          = 1;
    public static final int UPLOAD_TYPE_CHAT            = 2;
    public static final int UPLOAD_TYPE_DEVICE_PHOTO    = 3;

    public static final int REQUEST_CAMERA = 1;
    public static final int SELECT_FILE = 2;
    public static final int SELECT_IMAGE_ANY = 20;
    public static final int HANDLE_INCOMING_CALL = 10;
    public static final int LOAD_MASKED_WALLET_REQUEST_CODE = 1000;
    public static final int LOAD_FULL_WALLET_REQUEST_CODE = 1001;

    public String mPurchasePrice;
    public String mPurchaseDescription;

    public int mType;
    public Bitmap mImage;
    public Bitmap mOriginalImage;
    public int mImageType;
    public int mUploadType;
    public Boolean mChatUpload = false;

//    protected BoomtownServices boomtownServices = null;
    private Context mContext = null;

//    protected BoomtownApplication getBTApplication() {
//        return ((BoomtownApplication) getApplication());
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BoomtownAPI.kApplicationStateChangedAction));
//        if (boomtownServices != null) {
//            // start mDNS/DNS-SD
//            boomtownServices.startDNSSDService(getApplicationContext());
//        } else {
//            Log.d(TAG, "boomtown services is empty, cannot start base services - maybe you should subclass this object?");
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if (mMessageReceiver != null) {
//            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
//        }
//        if (boomtownServices != null) {
//            try {
//                boomtownServices.stopDNSSDService();
//            } catch (IllegalStateException e) {
//                Log.d(TAG, Log.getStackTraceString(e));
//            }
//        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onStop() {
        super.onStop();  // Always call the superclass method first
        hideProgress();
//        if (boomtownServices != null) {
//            try {
//                boomtownServices.stopDNSSDService();
//            } catch (IllegalStateException e) {
//                Log.d(TAG, Log.getStackTraceString(e));
//            }
//        }
    }

    public void showProgressWithMessage(final String message) {
//        runOnUiThread(() -> {
//            if (isFinishing()) {
//                Log.w(TAG, "activity already finishing - unable to show progress dialog: '" + message + "'");
//                return;
//            }
//            if (mProgress != null) {
//                mProgress.setMessage(message);
//                if (!mProgress.isShowing()) {
//                    mProgress.show();
//                } else {
////                    mProgress.hide();
//                    mProgress.dismiss();
//                    mProgress.show();
//                }
//            } else {
//                mProgress = ProgressDialog.show(BaseActivity.this, null, message, true);
//            }
//        });
    }

    public void hideProgress() {
//        runOnUiThread(() -> {
//            if (mProgress != null && mProgress.isShowing()) {
////                mProgress.hide();
//                mProgress.dismiss();
//            }
//        });
    }

    public void showNotificationAlert(Intent intent) {
//        String alert = null;
//        if (intent.hasExtra(BoomtownAPI.kStringExtraData)) {
//            alert = intent.getStringExtra(BoomtownAPI.kStringExtraData);
//        } else if (intent.hasExtra(BoomtownAPI.kAlert)) {
//            alert = intent.getStringExtra(BoomtownAPI.kAlert);
//        } else if (intent.hasExtra(BoomtownAPI.kApplicationStateChangedMessage)) {
//            alert = intent.getStringExtra(BoomtownAPI.kApplicationStateChangedMessage);
//        }
//
//        if (alert != null) {
//            Toast.makeText(this, alert, Toast.LENGTH_SHORT).show();
//        }
    }


    public void showErrorMessage(final String titleToShow,
                                 final String msgToShow) {
//        runOnUiThread(() -> {
//            if (titleToShow != null) {
//                DialogHelper.showAlertDialog(BaseActivity.this, titleToShow, msgToShow);
//            } else {
//                DialogHelper.showAlertDialog(BaseActivity.this, titleToShow, msgToShow);
//            }
//        });
    }

    public boolean handleLongClickForClass(String className) {
        return false;
    }


    /**
     * Launch builtin browser.
     *
     * @param url URL to load in browser.
     */
    public void launchBrowser(String url) {
        String model = Build.MODEL.toLowerCase();
        if (!model.contains("verifone") && !model.contains("carbon")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (resultCode == RESULT_OK)
                    handlePhotoFromCamera(data);
                break;
            case SELECT_FILE:
            case SELECT_IMAGE_ANY:
                if (resultCode == RESULT_OK)
                    handlePhotoFromFile(data);
                break;
            case LOAD_MASKED_WALLET_REQUEST_CODE:
                loadMaskedWallet(data);
                break;
            case LOAD_FULL_WALLET_REQUEST_CODE:
                loadFullWallet(data);
                break;
            case HANDLE_INCOMING_CALL:
//                gcNotifications(BoomtownServices.NOTIFICATION_ID_VIDEO);
//                if (resultCode == IncomingCallActivity.ACTIVITY_RESULT_CALL_ACCEPTED
//                        && data != null) {
//                    final String callId = data.getStringExtra(BoomtownAPI.kCallId);
//                    showProgressWithMessage(getString(R.string.msg_joining_call));
//                    BoomtownAPI.sharedInstance().apiTechniciansCommGet(callId, getApplicationContext(), BoomtownComm.kTypeVideo);
//                }
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
        } catch (IOException e) {
        }
//        mOriginalImage = thumbnail;
//        mImage = mUploadType==UPLOAD_TYPE_DEVICE_PHOTO ? mOriginalImage : Utils.getclip(thumbnail);
        upload();
    }

    public void handlePhotoFromFile(Intent data) {
        mImage = null;
        Uri selectedImageUri = data.getData();
        String[] projection = {MediaStore.MediaColumns.DATA};
        CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null,
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

//        mOriginalImage = ExifUtil.rotateBitmap(selectedImagePath, bm);
//
//        mImage = mUploadType==UPLOAD_TYPE_DEVICE_PHOTO ? mOriginalImage : Utils.getclip(mOriginalImage);
        upload();
    }

    public void upload() {
//        BoomtownAPI.sharedInstance().sendNotification(this, BoomtownAPI.kImageCaptured, mUploadType);
//        switch (mUploadType) {
//            case UPLOAD_TYPE_AVATAR:
//                showProgressWithMessage(getString(R.string.label_uploading_photo));
//                BoomtownAPI.sharedInstance().apiMembersUpdateAvatar(getApplicationContext(), mImage, mImageType);
//                break;
//
//            case UPLOAD_TYPE_CHAT:
//                break;
//
//            case UPLOAD_TYPE_DEVICE_PHOTO:
//                handleDevicePhoto(mImage);
//                break;
//
//            default:
//                break;
//        }
    }

    public void handleDevicePhoto(Bitmap image) {

    }

    public void selectImage(Activity activity, int imageType, int uploadType) {
//        mImageType = imageType;
//        mUploadType = uploadType;
//        final CharSequence[] items = {getString(R.string.label_take_photo), getString(R.string.label_choose_from_library), getString(R.string.cancel)};
//        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
//        builder.setTitle(R.string.title_add_photo);
//        builder.setItems(items, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int item) {
//                if (getString(R.string.label_take_photo).equals(items[item])) {
//                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    startActivityForResult(intent, Settings.REQUEST_CAMERA);
//                } else if (getString(R.string.label_choose_from_library).equals(items[item])) {
//                    Intent intent = new Intent(
//                            Intent.ACTION_PICK,
//                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                    intent.setType(INTENT_TYPE_IMAGE_SLASH_STAR);
//                    startActivityForResult(
//                            Intent.createChooser(intent, getString(R.string.title_select_file)),
//                            Settings.SELECT_FILE);
//                } else if (getString(R.string.cancel).equals(items[item])) {
//                    dialog.dismiss();
//                }
//            }
//        });
//        builder.show();
    }

    protected abstract void startVideoSession(BoomtownComm call);

    protected abstract void onAppMsgStartVideoSession(String callId);

    protected abstract void onAppMsgCommPutSuccess(int commType, BoomtownComm call, String kAlert);

    protected abstract void onAppMsgCommGetSuccess(int kTypeVideo, BoomtownComm call, String stringExtra);

    protected abstract void onAppMsgCommEnterSuccess(int commType, BoomtownComm call, int duration);


     /**
      * Default implementation always returns false.  Override for specific impl.
      *
      * @return if this activity's default layout is two-pane aka side-by-side
      */
     public boolean isTwoPane() {
         return false;
     }

     /**
      * Log screen stats to {@link Log#v(String, String)} stream.
      *
      * @return
      */
     protected void debugLogScreenStats() {
         Log.v(TAG, "getResources().getConfiguration()\t::");
         Log.v(TAG, "\t\torientation=" + getResources().getConfiguration().orientation);
         Log.v(TAG, "\t\tscreenWidthDp=" + getResources().getConfiguration().screenWidthDp);
         Log.v(TAG, "\t\tscreenHeightDp=" + getResources().getConfiguration().screenHeightDp);
         Log.v(TAG, "\t::");
     }

    /**
     * Call {@link NotificationManagerCompat#cancel(String, int)} for given notifications.
     *
     * @param notifIds notifIds to cancel
     */
    protected void gcNotifications(Integer notifIds) {
        AsyncTask<Integer, Void, Void> gcNotifsTask = new GCNotifAsyncTask(new WeakReference<Context>(getApplicationContext()));
        gcNotifsTask.execute(notifIds);
    }


    /**
     * @author fbeachler
     */
    public static class GCNotifAsyncTask extends AsyncTask<Integer, Void, Void> {
        private final WeakReference<Context> ctx;

        public GCNotifAsyncTask(WeakReference<Context> ctx) {
            this.ctx = ctx;
        }

        @Override
        protected Void doInBackground(Integer... notifIds) {
            for (Integer notifId : notifIds) {
                NotificationManagerCompat.from(ctx.get()).cancel(null, notifId);
            }
            return null;
        }
    }
}
