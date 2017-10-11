package net.noconroy.itproject.application.Chat;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import net.noconroy.itproject.application.DataStorage;
import net.noconroy.itproject.application.NetworkHelper;
import net.noconroy.itproject.application.R;
import net.noconroy.itproject.application.callbacks.EmptyCallback;
import net.noconroy.itproject.application.callbacks.NetworkCallback;
import net.noconroy.itproject.application.models.Message;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Mattias on 5/10/2017.
 * Basic functionality for ChatActivity was sourced from:
 *      https://www.codeproject.com/Tips/897826/Designing-Android-Chat-Bubble-Chat-UI
 */

public class ChatActivity extends AppCompatActivity {

    private ChatAdapter chatAdapter;
    public static final String INTENT_NAME = "name";

    private Button sendButton;
    private EditText textInput;
    private ListView messageList;

    // Refers to the ID and NAME of the current user we're interacting with in the chat
    private String userClickedOnName;

    Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            chatAdapter.notifyDataSetChanged();
        }
    };

    NetworkHelper.Receiver receiver = new NetworkHelper.Receiver() {
        @Override
        public void process(Message message) {
            chatAdapter.Add(message);
            ChatActivity.this.runOnUiThread(updateRunnable);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Retrieve information from extra bundle
        userClickedOnName = getIntent().getExtras().getString(INTENT_NAME);

        setView();

        // Create a new chat adapter and backlog and load history
        DataStorage.getInstance().notifications.chatHelper.addReceiver(receiver);
        chatAdapter = new ChatAdapter(this);
        messageList.setAdapter(chatAdapter);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void setView() {
        messageList = (ListView)findViewById(R.id.chatListView);
        textInput = (EditText)findViewById(R.id.textInput);
        sendButton = (Button)findViewById(R.id.messageBtn);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputtedText = textInput.getText().toString();

                if (TextUtils.isEmpty(inputtedText)) {
                    return;
                }


                textInput.setText("");
                DataStorage.getInstance().notifications.chatHelper.sendMessage(userClickedOnName, inputtedText, new NetworkCallback<Message>(Message.class, null) {
                    @Override
                    public void onSuccess(Message object) {

                    }

                    @Override
                    public void onFailure(Failure f) {

                    }
                });
            }
        });
    }

    private void scrollListView() {
        messageList.setSelection(messageList.getCount() - 1);
    }
}
