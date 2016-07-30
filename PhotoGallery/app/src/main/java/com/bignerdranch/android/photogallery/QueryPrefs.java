package com.bignerdranch.android.photogallery;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by Rasul on 26.07.2016.
 */
public class QueryPrefs {
    private static final String PREF_SEARCH_KEY = "searchQuery";
    private static final String PREF_LAST_ID = "lastId";

    public static String getStoredQuery(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_SEARCH_KEY, null);
    }

    public static void setStoredQuery(Context context, String query) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putString(PREF_SEARCH_KEY, query).apply();
    }

    public static String getLastId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_LAST_ID, null);
    }

    public static void setLastId(Context context, String id) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putString(PREF_LAST_ID, id).apply();
    }
}
