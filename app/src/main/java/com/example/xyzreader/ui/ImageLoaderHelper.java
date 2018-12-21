package com.example.xyzreader.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class ImageLoaderHelper {
    private static ImageLoaderHelper sInstance;
    private ImageLoaderCacheWaiter listner;

    public static ImageLoaderHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ImageLoaderHelper(context.getApplicationContext());
        }

        return sInstance;
    }

    public ImageLoaderHelper requestFrom(Activity activity){
        if(activity instanceof ArticleDetailActivity){
            listner = (ImageLoaderCacheWaiter) activity;
        }
        return sInstance;
    }

    private final LruCache<String, Bitmap> mImageCache = new LruCache<String, Bitmap>(20);
    private ImageLoader mImageLoader;

    private ImageLoaderHelper(Context applicationContext) {
        RequestQueue queue = Volley.newRequestQueue(applicationContext);
        ImageLoader.ImageCache imageCache = new ImageLoader.ImageCache() {
            @Override
            public void putBitmap(String key, Bitmap value) {
                mImageCache.put(key, value);
                if (listner != null){
                    listner.onAddedToCache(key,value);
                }
            }

            @Override
            public Bitmap getBitmap(String key) {
                return mImageCache.get(key);
            }
        };
        mImageLoader = new ImageLoader(queue, imageCache);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public Bitmap getBitmap(String key){
        return mImageCache.get(key);
    }

    public interface ImageLoaderCacheWaiter {
        void onAddedToCache(String key,Bitmap bitmap);
    }
}
