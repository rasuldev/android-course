package com.bignerdranch.android.simplechat;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Chat";
    private RecyclerView mChat;
    private EditText mMessage;
    private List<Message> mMessages = new ArrayList<>();
    private ChatAdapter mChatAdapter;
    private Messenger mMessenger;
    private String mId = UUID.randomUUID().toString().substring(0, 5);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMessage = (EditText) findViewById(R.id.message);
        mChat = (RecyclerView) findViewById(R.id.messages);
        mMessenger = new JsonMessenger();

        mChat.setLayoutManager(new LinearLayoutManager(this));
        mChatAdapter = new ChatAdapter();
        mChat.setAdapter(mChatAdapter);
        mMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String text =v.getText().toString();
                Log.i(TAG, text);
                //mMessages.add(new Message(mId, text));
                //mChatAdapter.notifyDataSetChanged();
                new SendTask().execute(text);
                v.setText("");
                return true;
            }
        });

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                new ReceiveTask().execute();
            }
        }, 0, 3000);
    }

    private class MessageHolder extends RecyclerView.ViewHolder {
        private TextView mText;

        public MessageHolder(View itemView) {
            super(itemView);
            mText = (TextView) itemView;
        }

        public void bind(Message message) {
            mText.setText(message.getId() + ": " + message.getText());
        }
    }

    private class ChatAdapter extends RecyclerView.Adapter<MessageHolder> {
        @Override
        public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MessageHolder(new TextView(MainActivity.this));
        }

        @Override
        public void onBindViewHolder(MessageHolder holder, int position) {
            holder.bind(mMessages.get(position));
        }

        @Override
        public int getItemCount() {
            return mMessages.size();
        }
    }

    private class SendTask extends AsyncTask<String, Void, Void> {


        @Override
        protected Void doInBackground(String... params) {
            mMessenger.SendMessage(new Message(mId, params[0]));
            return null;
        }
    }

    private class ReceiveTask extends AsyncTask<Void, Void, List<Message>> {

        @Override
        protected List<Message> doInBackground(Void... params) {
            return mMessenger.ReceiveMessages(mId);
        }

        @Override
        protected void onPostExecute(List<Message> messages) {
            mMessages = messages;
            mChatAdapter.notifyDataSetChanged();
        }
    }
}
