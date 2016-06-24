package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Rasul on 24.06.2016.
 */
public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "PhotoThumbDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;

    private Handler mRequestHandler;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();

    private Handler mResponseHandler;
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;

    private LruCache<String, Bitmap> mCache = new LruCache<>(100);

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
        mThumbnailDownloadListener = listener;
    }

    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDowload(T target, Bitmap thumbnail);
    }


    public ThumbnailDownloader(Handler responseHandler) {
        super("PhotoThumbDownloader");
        mResponseHandler = responseHandler;
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "Got a request for URL: " + mRequestMap.get(target));
                    handleRequest(target);
                }
            }
        };
    }

    private void handleRequest(final T target) {
        try {
            final String url = mRequestMap.get(target);
            if (url == null) {
                return;
            }

            // try to find in cache first
            Bitmap bmp = mCache.get(url);
            if (bmp == null) {
                byte[] bmpBytes = new FlickrFetchr().getUrlBytes(url);
                bmp = BitmapFactory.decodeByteArray(bmpBytes, 0, bmpBytes.length);
                Log.i(TAG, "Bitmap created");
                mCache.put(url, bmp);
            } else {
                Log.i(TAG, "Used cached bitmap");
            }

            final Bitmap thumbnail = bmp;

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRequestMap.get(target) != url) {
                        return;
                    }

                    mRequestMap.remove(target);

                    if (mThumbnailDownloadListener != null) {
                        mThumbnailDownloadListener.onThumbnailDowload(target, thumbnail);
                    }
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Error downloading image", e);
        }
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    public void queueThumbnail(T target, String url) {
        if (url == null) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
        }
    }
}
