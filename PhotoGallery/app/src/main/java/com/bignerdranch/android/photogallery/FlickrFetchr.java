package com.bignerdranch.android.photogallery;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rasul on 18.06.2016.
 */
public class FlickrFetchr {
    public static final String TAG = "PhotoGallery";
    public static final String API_KEY = "056fa81f1d4383fef787ae49601468b3";

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + " with " + urlSpec);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();

        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchItems(){
        String url = Uri.parse("https://api.flickr.com/services/rest/")
                .buildUpon()
                .appendQueryParameter("method", "flickr.photos.getRecent")
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("format", "json")
                .appendQueryParameter("nojsoncallback", "1")
                .appendQueryParameter("extras", "url_s")
                .build().toString();

        List<GalleryItem> items = new ArrayList<>();
        try {
            String jsonResponse = getUrlString(url);
            Log.i(TAG,"Fetched items: " + jsonResponse);
            items = parseItemsGson(jsonResponse);
        } catch (IOException e) {
            Log.e(TAG, "Failed to fetch items", e);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON", e);
        }


        return items;
    }

    private List<GalleryItem> parseItems(String json) throws JSONException {
        List<GalleryItem> items = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(json);
        JSONObject photos = jsonObject.getJSONObject("photos");
        JSONArray photosJSONArray = photos.getJSONArray("photo");
        for (int i = 0; i < photosJSONArray.length(); i++) {
            JSONObject photo = photosJSONArray.getJSONObject(i);
            GalleryItem galleryItem = new GalleryItem();
            galleryItem.setCaption(photo.getString("title"));
            galleryItem.setId(photo.getString("id"));
            if (!photo.has("url_s")) {
                continue;
            }
            galleryItem.setUrl(photo.getString("url_s"));
            items.add(galleryItem);
        }
        return items;
    }

    private List<GalleryItem> parseItemsGson(String json) throws JSONException {
        Gson gson = new GsonBuilder()
//                    .setFieldNamingStrategy(new FieldNamingStrategy() {
//                        @Override
//                        public String translateName(Field f) {
//                            f.getName()
//
//                        }
//                    })
                .create();
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(json);
        JsonElement photoArr = jsonElement.getAsJsonObject().get("photos").getAsJsonObject().get("photo");

        Type type = new TypeToken<List<GalleryItem>>() {}.getType();
        List<GalleryItem> items = gson.fromJson(photoArr, type);
        return items;
    }
}
