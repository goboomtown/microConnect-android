package com.goboomtown.microconnect.btconnecthelp.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;

import com.goboomtown.microconnect.btconnecthelp.api.BTConnectAPI;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WebImageView extends AppCompatImageView {

	public static final String TAG = WebImageView.class.getSimpleName();

	public static final int DEFAULT_TIMEOUT_HTTP_CONNECTION = 30000;

	private Drawable mPlaceholder;
	private DownloadTask mTask;
	public Boolean mRectangular;

	private final Map<String, Drawable> sImageCache;

	public WebImageView(Context context) {
		this(context, null);
	}

	public WebImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WebImageView(Context context, AttributeSet attrs, int defaultStyle) {
		super(context, attrs, defaultStyle);
		sImageCache = new HashMap<>();
		mRectangular = false;
	}

	public void setPlaceholderImage(Drawable drawable) {
		mPlaceholder = drawable;
		setImageDrawable(mPlaceholder);
	}

	public void setPlaceholderImage(int resid) {
		setPlaceholderImage(getResources().getDrawable(resid));
	}

	public void setImageUrl(String url) {
		if (mTask != null) {
			mTask.cancel(true);
		}
		if (sImageCache.containsKey(url)) {
			setImageDrawable(sImageCache.get(url));
			return;
		}
		mTask = new DownloadTask(new WeakReference<>(getContext()), new WeakReference<>(this), new WeakReference<>(sImageCache), mRectangular);
		mTask.execute(url);
	}

	public Drawable getDrawableFromBitmap(Activity activity, Bitmap bitmap) {
		Bitmap bmp = bitmap;
		Drawable d;
		DisplayMetrics metrics = new DisplayMetrics();
		if (activity != null) {
			activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
			float scaledDensity = metrics.density;
			int width = bmp.getWidth();
			int height = bmp.getHeight();
			if (scaledDensity < 1) {
				width = (int) (width * scaledDensity);
				height = (int) (height * scaledDensity);
			} else {
				width = (int) (width + width * (scaledDensity - 1));
				height = (int) (height + height * (scaledDensity - 1));
			}
			bmp = Bitmap.createScaledBitmap(bmp, width, height, true);
			d = new BitmapDrawable(getResources(), bmp);
		} else {
			d = new BitmapDrawable(bmp);
		}
		return d;
	}

	public static class DownloadTask extends AsyncTask<String, Void, Bitmap> {
		public static final String TAG = WebImageView.class.getSimpleName()
				+ "~" + DownloadTask.class.getSimpleName();
		public static final String PROTOCOL_HTTP = "http://";
		public static final String PROTOCOL_HTTPS = "https://";
		public static final String PROTOCOL_FILE = "file://";

		private final Object lockGetBitmap;
		private final WeakReference<Context> ctx;
		private final WeakReference<AppCompatImageView> imageView;
		private final WeakReference<Map<String, Drawable>> imageCache;
		private final boolean rectangular;
		private String url;

		public DownloadTask(WeakReference<Context> ctx, WeakReference<AppCompatImageView> imageView, WeakReference<Map<String, Drawable>> imageCache, boolean rectangular) {
			lockGetBitmap = new Object();
			this.ctx = ctx;
			this.imageView = imageView;
			this.imageCache = imageCache;
			this.rectangular = rectangular;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			url = params[0];
			return getBitmap(params[0]);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (result == null) {
				return;
			}
			if (ctx.get() == null && !isCancelled()) {
				this.cancel(true);
				return;
			}
			RoundedBitmapDrawable roundDrawable;
			Drawable drawable;
			if (!rectangular) {
				roundDrawable = RoundedBitmapDrawableFactory.create(ctx.get().getResources(), result);
				roundDrawable.setCircular(true);
				drawable = roundDrawable;
			} else {
				drawable = new BitmapDrawable(ctx.get().getResources(), result);
			}
			if (imageCache.get() != null) {
				imageCache.get().put(url, drawable);
			}
			if (imageView.get() != null) {
				imageView.get().setImageDrawable(drawable);
			}
		}

		protected Bitmap getBitmap(final String urlWithProtocolPrefix) {
			String url = urlWithProtocolPrefix;
			final Bitmap[] img = {null};
			try {
				if (url == null || url.length() == 0
						|| (!url.toLowerCase().startsWith(PROTOCOL_HTTP) && !url.toLowerCase().startsWith(PROTOCOL_HTTPS) && !url.toLowerCase().startsWith(PROTOCOL_FILE))) {
					return img[0];
				}
				if (url.toLowerCase().startsWith(PROTOCOL_FILE)) {
					// remove file protocol prefix
					url = url.substring(PROTOCOL_FILE.length(), url.length());
					img[0] = BitmapFactory.decodeFile(url);
					return img[0];
				}
				if (ctx.get() == null && !isCancelled()) {
					this.cancel(true);
					return null;
				}
				BTConnectAPI.getImage(ctx.get(), url, new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
						synchronized (lockGetBitmap) {
							lockGetBitmap.notify();
						}
					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (response == null) {
							Log.w(TAG, "unexpected null response received from GET image");
						} else {
							if (response.code() == 200 && response.body() != null) {
								InputStream is = response.body().byteStream();
								if (is != null) {
									img[0] = BitmapFactory.decodeStream(is);
								}
							}
						}
						synchronized (lockGetBitmap) {
							lockGetBitmap.notify();
						}
					}
				});
				synchronized (lockGetBitmap) {
					lockGetBitmap.wait(DEFAULT_TIMEOUT_HTTP_CONNECTION);
				}
			} catch (InterruptedException e) {
				Log.w(TAG, "interrupted getting image data from " + url + ", the error stack was:\n" + Log.getStackTraceString(e));
			} catch (Exception e) {
				Log.w(TAG, "error getting image data from " + url + ", the error stack was:\n" + Log.getStackTraceString(e));
			}
			return img[0];
		}
	}

}
