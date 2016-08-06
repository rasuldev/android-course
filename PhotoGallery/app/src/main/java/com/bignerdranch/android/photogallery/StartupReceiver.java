package com.bignerdranch.android.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Rasul on 06.08.2016.
 */
public class StartupReceiver extends BroadcastReceiver {
    private static final String TAG = "PhotoStartup";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received broadcast intent: " + intent.getAction());
        boolean isOn = QueryPrefs.isAlarmOn(context);
        PollService.setAlarm(context, isOn);
    }
}
