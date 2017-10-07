package net.noconroy.itproject.application;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


public class AddFriendActivity extends AppCompatActivity {

    private String access_token = null;

    // UI
    private EditText mSearchUser;
    private Button mFindUserButton;

    private AddFriendActivity.AddUserAsFriendTask mTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        Intent i = getIntent();
        String access_token = i.getStringExtra(RegisterActivity.ACCESS_TOKEN_MESSAGE);
        this.access_token = access_token;

        mSearchUser = (EditText) findViewById(R.id.username_search);
        mFindUserButton = (Button) findViewById(R.id.find_friend_button);

        if (access_token != null) {
            mFindUserButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptFindUser();
                }
            });
        }
    }

    /**
     * Attempts to find a user via asynchronous task.
     */
    public void attemptFindUser() {
        if(mTask != null) {
            return;
        }
        String username = null;
        username = mSearchUser.getText().toString();

        if(TextUtils.isEmpty(username) || username == null) {
            mSearchUser.setError("No username entered");
            mSearchUser.requestFocus();
        }

        else {
            Toast t = Toast.makeText(getApplicationContext(), "Attempting to add friend: "+username, Toast.LENGTH_SHORT);
            t.show();
            // begin background asynchronous task of adding friend
            mTask = new AddUserAsFriendTask(username, access_token);
            mTask.execute((Void) null);
        }
    }

    /**
     * Asynchronous task of adding user as friend, which sends a friend request to future friend.
     * Calls NetworkHelper method 'AddFriend'.
     */
    public class AddUserAsFriendTask extends AsyncTask<Void, Void, Boolean> {

        private String mUsername;
        private String access_token;
        private String response;

        public AddUserAsFriendTask(String username, String access_token) {
            this.mUsername = username;
            this.access_token = access_token;
        }

        @Override
        protected  Boolean doInBackground(Void... params) {
            response = NetworkHelper.AddFriend(mUsername, access_token);
            if(response.equals("400")) {
                runOnUiThread(already_sent_request_error);
                return false;
            }
            else if(response.equals("404")) {
                runOnUiThread(user_does_not_exist);
                return false;
            }
            else if(response.equals("500")) {
                runOnUiThread(general_error);
                return false;
            }
            else {
                return true;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mTask = null;
            if(success) {
                Toast t = Toast.makeText(getApplicationContext(), "Sent friend request!", Toast.LENGTH_SHORT);
                t.show();
            }
            else {
                // do nothing, handle errors from Runnable objects (in main thread)
            }
        }

        @Override
        protected void onCancelled() {
            mTask = null;
        }

    }

    /* Handles server errors. To be run in the main thread. */

    /**
     * Handles the case when a friend request has already been sent, in case a user tries
     * to add the same person twice.
     */
    private Runnable already_sent_request_error = new Runnable() {
        @Override
        public void run() {
            mSearchUser.setError("Already sent friend request to this user!");
            mSearchUser.requestFocus();
        }
    };

    /**
     * Handles the case when a user attempts to search for a username that does not exist.
     */
    private Runnable user_does_not_exist = new Runnable() {
        @Override
        public void run() {
            mSearchUser.setError("This user does not exist!");
            mSearchUser.requestFocus();
        }
    };

    /**
     * Handles the case when the server fails to send a friend request.
     */
    private Runnable general_error = new Runnable() {
        @Override
        public void run() {
            Toast t = Toast.makeText(getApplicationContext(), "Failed to send friend request!", Toast.LENGTH_SHORT);
            t.show();
        }
    };

}
