package net.noconroy.itproject.application;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

/**
 * Displays the friends page, including the user's friend list, and the options
 * to navigate to other pages to accept new incoming friend requests and add users as friends.
 */
public class Friends extends AppCompatActivity {

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
                mFriendsListAdapter = new FriendsListAdapter(mFriends, getApplicationContext(), access_token);
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

    /* Adapter class that is used to configure each row within the user's friends list. */
    private class FriendsListAdapter extends BaseAdapter {

        private ArrayList<ArrayList<String>> mFriendProfiles;
        private ArrayList<String> mFriendUsernamesList = new ArrayList<String>();
        private Context mContext;
        private Button mRemoveButton;
        private String access_token;
        private FriendsListAdapter.RemoveFriendTask mRemoveTask = null;

        public FriendsListAdapter(ArrayList<ArrayList<String>> profiles, Context context, String access_token) {
            mContext = context;
            this.mFriendProfiles = profiles;
            for(int i=0 ; i < mFriendProfiles.size(); i++) {
                if(mFriendProfiles.get(i) != null) {
                    mFriendUsernamesList.add(mFriendProfiles.get(i).get(0));
                }
            }
            this.access_token = access_token;
        }

        @Override
        public int getCount() {
            return mFriendUsernamesList.size();
        }

        @Override
        public Object getItem(int pos) {
            return mFriendUsernamesList.get(pos);
        }

        @Override
        public long getItemId(int pos) {
            return 0;
        }

        /* Sets up the view for a particular row within the friends list. */
        @Override
        public View getView(final int pos, View convertView, ViewGroup parent) {
            View view = convertView;
            if(view == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.friends_list_row, null);
            }

            // sets the username for a row
            final TextView username = (TextView) view.findViewById(R.id.FriendUsername);
            username.setText(mFriendUsernamesList.get(pos));

            // button to remove friend
            mRemoveButton = (Button) view.findViewById(R.id.RemoveButton);
            mRemoveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   removeFriend(username.getText().toString(), access_token, pos);
                }
            });

            return view;
        }

        /**
         * Removes a particular friend from the user's friend list
         * @param username belonging to the friend to remove
         * @param access_token belonging to the user
         * @param pos index position of the row within the friends list
         */
        public void removeFriend(String username, String access_token, int pos) {
            if(mRemoveTask != null) {
                return;
            }
            else {
                mRemoveTask = new RemoveFriendTask(username, access_token, pos);
                mRemoveTask.execute((Void) null);
            }
        }

        /**
         * Asynchronous task responsible for removing a friend from the user's friend list.
         * Calls NetworkHelper method 'RemoveFriend'.
         */
        public class RemoveFriendTask extends AsyncTask<Void, Void, Boolean> {
            private String mUsername;
            private String mAccessToken;
            private String response;
            private int index;

            public RemoveFriendTask(String user, String access_token, int pos) {
                this.mUsername = user;
                this.mAccessToken = access_token;
                this.index = pos;
            }

            @Override
            protected  Boolean doInBackground(Void... params) {
                response = NetworkHelper.RemoveFriend(mUsername, mAccessToken);
                if(response.equals("200")) {
                    return true;
                }
                else {
                    runOnUiThread(remove_friend_error);
                    return false;
                }
            }

            @Override
            protected void onPostExecute(final Boolean success) {
                mRemoveTask = null;
                if(success) {
                    Toast.makeText(getApplicationContext(), "Removed Friend!", Toast.LENGTH_SHORT).show();
                    mFriendUsernamesList.remove(index);
                    mFriendsListAdapter.notifyDataSetChanged();
                }
                // errors handled in main thread
            }

            @Override
            protected void onCancelled() {
                mRemoveTask = null;
            }

        }

        /**
         * Handles the case when there is a network error in attempting to remove a friend.
         * To be run on the main thread.
         */
        private Runnable remove_friend_error = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Attempt to remove friend failed!", Toast.LENGTH_SHORT).show();
            }
        };
    }
}