package com.goboomtown.btconnecthelp.widget;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

//import org.apache.http.util.ByteArrayBuffer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.AttributeSet;
import android.widget.ImageView;

//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.entity.BufferedHttpEntity;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.util.ByteArrayBuffer;

public class WebImageView extends ImageView {
	
	private Drawable 				mPlaceholder, mImage;
	private RoundedBitmapDrawable 	mRoundImage;
    private DownloadTask 			mTask;
	public  Boolean					mRectangular = false;

    private static final HashMap<String, Drawable> sImageCache = new HashMap<String, Drawable>();
	
	public WebImageView(Context context) {
		this(context, null);
	}
	
	public WebImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public WebImageView(Context context, AttributeSet attrs, int defaultStyle) {
		super(context, attrs, defaultStyle);
	}
	
	public void setPlaceholderImage(Drawable drawable) {
		mPlaceholder = drawable;
		if(mImage == null) {
			setImageDrawable(mPlaceholder);
		}
	}
	public void setPlaceholderImage(int resid) {
		mPlaceholder = getResources().getDrawable(resid);
		if(mImage == null) {
			setImageDrawable(mPlaceholder);
		}
	}
	
	public void setImageUrl(String url) {
        if(mTask != null) mTask.cancel(true);
        if(sImageCache.containsKey(url)) {
            setImageDrawable(sImageCache.get(url));
            return;
        }

        mTask = new DownloadTask();
        mTask.execute(url);
	}

	private Bitmap getBitmap(String urlStr)
	{
		Bitmap img = null;
		if ( urlStr==null || urlStr.length()==0 || !urlStr.toLowerCase().startsWith("http") )
			return img;

//		HttpClient client = new DefaultHttpClient();
//		HttpGet request = new HttpGet(urlStr);
//		HttpResponse response;
//		try {
//			response = (HttpResponse)client.execute(request);
//			HttpEntity entity = response.getEntity();
//			BufferedHttpEntity bufferedEntity = new BufferedHttpEntity(entity);
//			InputStream inputStream = bufferedEntity.getContent();
//			img = BitmapFactory.decodeStream(inputStream);
//		} catch (ClientProtocolException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return img;

	}

	class DownloadTask extends AsyncTask<String, Void, Bitmap> {
        private String url;
		@Override
		protected Bitmap doInBackground(String... params) {
//			return getBitmap(params[0]);
			return null;
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
			if ( result != null ) {
				mImage = new BitmapDrawable(result);
				if ( !mRectangular) {
					mRoundImage = RoundedBitmapDrawableFactory.create(getResources(), result);
					if (mRoundImage != null) {
						mRoundImage.setCircular(true);
						sImageCache.put(url, mRoundImage);
						setImageDrawable(mRoundImage);
					}
				} else {
					setImageDrawable(mImage);
				}
			}
		}
	};
}
