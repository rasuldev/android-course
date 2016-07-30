package com.bignerdranch.android.photogallery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.List;

/**
 * Created by Rasul on 27.07.2016.
 */
public class PollService extends IntentService {
    private static final String TAG = "PhotoPollService";
    private static final int POLL_INTERVAL = 60 * 1000; // 60 secs

    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);
    }

    public PollService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!isNetworkAvailableAndConnected()) {
            Log.i(TAG, "No internet connection");
            return;
        }
        Log.i(TAG, "Received an intent: " + intent);

        String query = QueryPrefs.getStoredQuery(this);
        String lastId = QueryPrefs.getLastId(this);
        List<GalleryItem> items;
        items = query == null ? new FlickrFetchr().fetchRecent(1) : new FlickrFetchr().search(query, 1);
        if (items.size() == 0) {
            Log.i(TAG, "No new photos");
            return;
        }

        String id = items.get(0).getId();
        if (id.equals(lastId)) {
            Log.i(TAG, "Got an old result: " + id);
        } else {
            Log.i(TAG, "Got a new result: " + id);

            Resources res = getResources();
            PendingIntent pi = PendingIntent.getService(this, 0, PhotoGalleryActivity.newIntent(this), 0);
            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(res.getString(R.string.new_pic_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(res.getString(R.string.new_pic_title))
                    .setContentText(res.getString(R.string.new_pic_text))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();
            NotificationManagerCompat.from(this).notify(0, notification);
        }
        QueryPrefs.setLastId(this, id);
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isAvailable = cm.getActiveNetworkInfo() != null;
        boolean isConnected = isAvailable && cm.getActiveNetworkInfo().isConnected();
        return isConnected;
    }

    public static void setAlarm(Context context, boolean isOn) {
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (isOn) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                    POLL_INTERVAL, pi);
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    public static boolean isAlarmOn(Context context) {
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }
}
