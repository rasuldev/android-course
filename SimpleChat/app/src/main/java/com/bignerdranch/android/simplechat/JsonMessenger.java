package com.bignerdranch.android.simplechat;

import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class JsonMessenger implements Messenger {

    public static final String BASE_URL="https://brave-weasel.hyperdev.space/";

    @Override
    public void SendMessage(Message message) {
        String url = Uri.parse(BASE_URL + "/message")
                .buildUpon()
                .appendQueryParameter("text", message.getText())
                .appendQueryParameter("id", message.getId())
                .build().toString();
        try {
            IOUtils.toString(new URL(url).openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public List<Message> ReceiveMessages(String id) {
        try {
            return getMessages(BASE_URL + "/chat");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


    private List<Message> getMessages(String url) throws JSONException, IOException {
        String json = IOUtils.toString(new URL(url).openStream());
        Gson gson = new GsonBuilder()
                .create();
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(json);
        JsonElement photoArr = jsonElement.getAsJsonObject().get("messages");

        Type type = new TypeToken<List<Message>>() {}.getType();
        List<Message> items = gson.fromJson(photoArr, type);
        return items;
    }
}
