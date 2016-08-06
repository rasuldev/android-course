package com.bignerdranch.android.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

/**
 * Created by Rasul on 06.08.2016.
 */
public class NotificationReceiver extends BroadcastReceiver {
    public static final String TAG = "PhotoNotifReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received result: " + getResultCode());
        if (getResultCode() != Activity.RESULT_OK) {
            return;
        }

        int reqCode = intent.getIntExtra(PollService.EXTRA_REQUEST_CODE, 0);
        Notification notification = (Notification) intent.getParcelableExtra(PollService.EXTRA_NOTIFICATION);
        NotificationManagerCompat.from(context).notify(reqCode, notification);

        //sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION), PERM_PRIVATE);
    }
}
