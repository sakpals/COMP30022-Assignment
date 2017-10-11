package net.noconroy.itproject.application;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import net.noconroy.itproject.application.Chat.ChatActivity;
import net.noconroy.itproject.application.callbacks.NetworkCallback;
import net.noconroy.itproject.application.models.Friends;

import java.util.ArrayList;

/**
 * Displays the friends page, including the user's friend list, and the options
 * to navigate to other pages to accept new incoming friend requests and add users as friends.
 */
public class FriendsActivity extends AppCompatActivity {

    private static final String TAG = "FriendsActivity";

    // UI references
    private Button mAddFriendButton;
    private Button mFriendRequestsButton;
    private ListView mFriendsList;

    // row adapter for ListView (friendsList)
    public static FriendsListAdapter mFriendsListAdapter = null;

    /* Sets up the view for the FriendsActivity page */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mFriendsList = (ListView) findViewById(R.id.FriendsList);

        displayFriendsList();


        mAddFriendButton = (Button) findViewById(R.id.AddFriendButton);
        mAddFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend(view);
            }
        });

        mFriendRequestsButton = (Button) findViewById(R.id.FriendRequestButton);
        mFriendRequestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewFriendRequests(view);
                //mFriendsListAdapter.notifyDataSetChanged();
            }
        });
    }

    /* Takes user to AddFriend activity. Access token is passed as reference
    * to user to access NetworkHelper method(s). */
    public void addFriend(View view) {
        Intent intent = new Intent(FriendsActivity.this, AddFriendActivity.class);
        startActivity(intent);
    }

    /* Takes user to FriendRequests activity (displaying incoming friend requests). Access
     * token is passed as reference to user to access NetworkHelper method(s). */
    public void viewFriendRequests(View view) {
        Intent intent = new Intent(FriendsActivity.this, FriendRequestsActivity.class);
        startActivity(intent);
    }

    /* Displays user's friend list within this current activity. */
    public void displayFriendsList() {
        NetworkHelper.GetFriends(new NetworkCallback<Friends>(Friends.class, this) {
            @Override
            public void onSuccess(Friends friends) {
                mFriendsListAdapter = new FriendsListAdapter(friends, getApplicationContext(), FriendsActivity.this, new FriendsListAdapter.OpenChat() {
                    @Override
                    public void user(String username) {
                        Intent chatIntent = new Intent(FriendsActivity.this, ChatActivity.class);
                        chatIntent.putExtra(ChatActivity.INTENT_NAME, username);
                        startActivity(chatIntent);
                    }
                });
                mFriendsList.setAdapter(mFriendsListAdapter);
            }

            @Override
            public void onFailure(Failure f) {

            }
        });
    }



    public void updateFriendsList() {
        mFriendsListAdapter.notifyDataSetChanged();
    }

}