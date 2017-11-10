package com.ayvengoza.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by ang on 08.11.17.
 */

public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private static final int CACHE_SIZE = 200;

    private boolean mHasQuit = false;
    private Handler mRequestHandler;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    private Handler mResponseHandler;
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;
    private LruCache<String, Bitmap> mBitmapCache;

    public interface ThumbnailDownloadListener<T> {
        void onThumbnaiDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
        mThumbnailDownloadListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
        mBitmapCache = new LruCache<>(CACHE_SIZE);
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

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    private void handleRequest(final T target) {

        final String url = mRequestMap.get(target);
        if (url == null) {
            return;
        }
        final Bitmap bitmap = downloadBitmap(url);
        mResponseHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mRequestMap.get(target) != url || mHasQuit) {
                    return;
                }
                mRequestMap.remove(target);
                mThumbnailDownloadListener.onThumbnaiDownloaded(target, bitmap);
            }
        });

    }

    private Bitmap downloadBitmap(String url) {
        Bitmap bitmap = mBitmapCache.get(url);
        try {
            if (bitmap == null) {
                byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
                bitmap = BitmapFactory
                        .decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                mBitmapCache.put(url, bitmap);
                Log.i(TAG, "Bitmap created");
            } else {
                Log.i(TAG, "Take Bitmap from cache " + url);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error downloading image", e);
        }
        return bitmap;
    }

    public void clearQueue() {
        mRequestMap.remove(MESSAGE_DOWNLOAD);
        mRequestMap.clear();
    }

    public void queueThumbnail(T target, String url) {
        Log.i(TAG, "Got a URL: " + url);

        if (url == null) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
        }
    }
}
