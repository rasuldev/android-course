package com.bignerdranch.android.photogallery;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by Rasul on 26.07.2016.
 */
public class QueryPrefs {
    private static final String PREF_SEARCH_KEY = "searchQuery";

    public static String getStoredQuery(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_SEARCH_KEY, null);
    }

    public static void setStoredQuery(Context context, String query) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putString(PREF_SEARCH_KEY, query).apply();
    }
}
