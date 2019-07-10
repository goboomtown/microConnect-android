package com.goboomtown.microconnect.chat.widget;

import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.InputStream;

public class AnimatedGifDrawable extends AnimationDrawable {

    private int mCurrentIndex = 0;
    private UpdateListener mListener;

    public AnimatedGifDrawable(InputStream source, UpdateListener listener, float displayDensity) {
        mListener = listener;
        GifDecoder decoder = new GifDecoder();
        decoder.read(source, 0);
        decoder.advance();
        // Iterate through the gif frames, add each as animation frame
        for (int i = 0; i < decoder.getFrameCount() - 1; i++) {
            Bitmap bitmap = decoder.getNextFrame();
            if (bitmap == null) {
                continue;
            }
            BitmapDrawable drawable = new BitmapDrawable(bitmap);
            // Explicitly set the bounds in order for the frames to display
            int adjW = (int) ((float) bitmap.getWidth() * displayDensity);
            int adjH = (int) ((float) bitmap.getHeight() * displayDensity);
            drawable.setBounds(0, 0, adjW, adjH);
            addFrame(drawable, decoder.getDelay(i));
            if (i == 0) {
                // Also set the bounds for this container drawable
                setBounds(0, 0, adjW, adjH);
            }
            decoder.advance();
        }
    }

    /**
     * Naive method to proceed to next frame. Also notifies listener.
     */
    public void nextFrame() {
        mCurrentIndex = (mCurrentIndex + 1) % getNumberOfFrames();
        if (mListener != null) mListener.update();
    }

    /**
     * Return display duration for current frame
     */
    public int getFrameDuration() {
        return getDuration(mCurrentIndex);
    }

    /**
     *
     * @return current frame index
     */
    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    /**
     * Return drawable for current frame
     */
    public Drawable getDrawable() {
        return getFrame(mCurrentIndex);
    }

    /**
     * Interface to notify listener to update/redraw
     * Can't figure out how to invalidate the drawable (or span in which it sits) itself to force redraw
     */
    public interface UpdateListener {
        void update();
    }

}