package net.noconroy.itproject.application;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import net.noconroy.itproject.application.AR.LocationServiceProvider;

import java.util.ArrayList;

/**
 * Displays the friends page, including the user's friend list, and the options
 * to navigate to other pages to accept new incoming friend requests and add users as friends.
 */
public class Friends extends AppCompatActivity {

    private static final String TAG = "Friends";
    private String access_token = null;
    private Friends.FriendsListTask mTask = null;

    // UI references
    private Button mAddFriendButton;
    private Button mFriendRequestsButton;
    private ListView mFriendsList;

    // row adapter for ListView (friendsList)
    public static FriendsListAdapter mFriendsListAdapter = null;

    /* Sets up the view for the Friends page */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        Intent intent = getIntent();
        this.access_token = intent.getStringExtra(RegisterActivity.ACCESS_TOKEN_MESSAGE);

        mFriendsList = (ListView) findViewById(R.id.FriendsList);
        if (this.access_token != null) {
            displayFriendsList();
        }

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
        Intent intent = new Intent(Friends.this, AddFriendActivity.class);
        intent.putExtra(RegisterActivity.ACCESS_TOKEN_MESSAGE, access_token);
        startActivity(intent);

    }

    /* Takes user to FriendRequests activity (displaying incoming friend requests). Access
     * token is passed as reference to user to access NetworkHelper method(s). */
    public void viewFriendRequests(View view) {
        Intent intent = new Intent(Friends.this, FriendRequestsActivity.class);
        intent.putExtra(RegisterActivity.ACCESS_TOKEN_MESSAGE, access_token);
        startActivity(intent);
    }

    /* Displays user's friend list within this current activity. */
    public void displayFriendsList() {
        if (mTask != null) {
            return;
        } else {
            mTask = new FriendsListTask(this.access_token);
            mTask.execute((Void) null);
        }
    }

    /* Asynchronous method which retrieves user's friend list from the database using NetworkHelper
    * method 'GetFriends'. */
    private class FriendsListTask extends AsyncTask<Void, Void, Boolean> {

        private String access_token = null;
        private ArrayList<ArrayList<String>> mFriends = null;

        public FriendsListTask(String access_token) {
            this.access_token = access_token;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (access_token != null) {
                mFriends = NetworkHelper.GetFriends(access_token);
            }
            if (mFriends != null && mFriends.size() != 0) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mTask = null;
            if (success) {
                mFriendsListAdapter = new FriendsListAdapter(mFriends, getApplicationContext(), access_token, Friends.this);
                mFriendsList.setAdapter(mFriendsListAdapter);
            }
            else {
                Toast.makeText(getApplicationContext(), "You have no friends!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {

        }
    }

    public void updateFriendsList() {
        mFriendsListAdapter.notifyDataSetChanged();
    }



    public void ShareLocation(View view) {
        if (!LocationServiceProvider.extendLocationUpdates(0.5f)) {
            Log.d(TAG, "can't extend location updates");
        } else {
            try {
                AsyncTask shareLocation = new ShareLocationWithFriends(access_token);
                shareLocation.execute();
            } catch (Exception e) {
                Log.i(TAG, "Issue with error");
            }
        }
    }

    public class ShareLocationWithFriends extends AsyncTask<Object, Void, Boolean> {

        private String access_token;
        private String status;

        public ShareLocationWithFriends(String access_token) {
            this.access_token = access_token;
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            status = NetworkHelper.UpdateLocationSettings("bob111", access_token);
            if(!status.equals("200")) {
                return false;
            }
            else {
                return true;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success) {
                Log.i(TAG, "Location has been shared with friends.");
            }
            else {
                ;
            }
        }

        @Override
        protected void onCancelled() {
            ;
        }
    }


}