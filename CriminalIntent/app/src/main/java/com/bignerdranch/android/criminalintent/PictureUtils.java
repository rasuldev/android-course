package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

/**
 * Created by Rasul on 26.04.2016.
 */
public class PictureUtils {
    public static Bitmap getScaledBitmap(String path, int destWidth, int destHeight) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
        float srcHeight = opts.outHeight;
        float srcWidth = opts.outWidth;

        int sampleSize = Math.round(Math.max(srcHeight / destHeight, srcWidth / destWidth));
        opts = new BitmapFactory.Options();
        opts.inSampleSize = sampleSize;
        return BitmapFactory.decodeFile(path, opts);
    }

    public static Bitmap getScaledBitmap(String path, Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);

        return getScaledBitmap(path, size.x, size.y);
    }
}
