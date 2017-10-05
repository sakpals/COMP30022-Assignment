package net.noconroy.itproject.application.Chat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import net.noconroy.itproject.application.R;

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
    private ArrayList<ChatMessage> chatMessages;

    private Button sendButton;
    private EditText textInput;
    private ListView messageList;

    // Refers to the ID and NAME of the current user we're interacting with in the chat
    private String userClickedOnId;
    private String userClickedOnName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Retrieve information from extra bundle
        userClickedOnName = getIntent().getExtras().getString("name");
        userClickedOnId = getIntent().getExtras().getString("id");

        setView();

        // Create a new chat adapter and backlog and load history
        chatMessages = new ArrayList<ChatMessage>();
        loadServerHistory(chatMessages);
        chatAdapter = new ChatAdapter(ChatActivity.this, chatMessages);
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

                ChatMessage message = new ChatMessage(
                        "userId",       // a unique id for this chat message
                        textInput.getText().toString(),
                        DateFormat.getDateTimeInstance().format(new Date()),
                        true
                );

                // Remove whatever we had in our edit text box
                textInput.setText("");
                sendMessage(message);
                dummyData();
            }
        });
    }

    // Would make a request to server to get all previous messages sent between the two users
    private void loadServerHistory(ArrayList<ChatMessage> chatMessages) {
        ;
    }

    private void sendMessage(ChatMessage message) {
        chatAdapter.add(message);
        chatAdapter.notifyDataSetChanged();
        scrollListView();
    }

    private void dummyData() {
        ChatMessage m1 = new ChatMessage(
                "2",
                "Hello darkness my old friend",
                DateFormat.getDateTimeInstance().format(new Date()),
                false
        );
        ChatMessage m2 = new ChatMessage(
                "2",
                "are you well?",
                DateFormat.getDateTimeInstance().format(new Date()),
                false
        );

        sendMessage(m1);
        sendMessage(m2);
    }

    private void scrollListView() {
        messageList.setSelection(messageList.getCount() - 1);
    }
}
