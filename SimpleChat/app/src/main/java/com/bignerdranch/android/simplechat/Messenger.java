package com.bignerdranch.android.simplechat;

import java.util.List;

/**
 * Created by Admin on 09.07.2016.
 */
public interface Messenger {
    void SendMessage(Message message);
    List<Message> ReceiveMessages(String id);
}

class Message {
    String id;
    String text;

    public Message(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

