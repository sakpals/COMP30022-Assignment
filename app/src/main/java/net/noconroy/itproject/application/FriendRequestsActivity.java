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
import java.util.HashMap;

/* Displays the user's incoming friend requests. */
public class FriendRequestsActivity extends AppCompatActivity {

    private String access_token = null;
    private ListView requests;
    private FriendRequestsActivity.DisplayIncomingFriendsRequestsTask mTask = null;
    private  FriendRequestAdapter customAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);

        Intent i = getIntent();
        this.access_token = i.getStringExtra(RegisterActivity.ACCESS_TOKEN_MESSAGE);

        this.requests = (ListView) findViewById(R.id.FriendRequests);

        if(access_token != null) {
            showFriendRequests();
        }
        else {
            Toast t = Toast.makeText(getApplicationContext(), "Unauthorised access!", Toast.LENGTH_SHORT);
            t.show();
        }
    }

    /**
     * Calls an asynchronous task to display a user's friend list
     */
    public void showFriendRequests() {
        if(mTask != null) {
            return;
        }
        else {
            mTask = new DisplayIncomingFriendsRequestsTask(access_token);
            mTask.execute((Void) null);
        }
    }

    /**
     * Asynchronous method that calls upon NetworkHelper method 'GetIncomingFriendRequests'
     * to display a user's incoming friend requests.
     */
    public class DisplayIncomingFriendsRequestsTask extends AsyncTask<Void, Void, Boolean> {

        private String access_token;
        private HashMap<String, String> mIncoming_requests = null;

        public DisplayIncomingFriendsRequestsTask(String access_token) {
            this.access_token = access_token;
        }

        @Override
        protected  Boolean doInBackground(Void... params) {
            mIncoming_requests = NetworkHelper.GetIncomingFriendRequests(access_token);
            if(mIncoming_requests == null) {
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
                // adapter that is used to display each row of the friend requests list
                customAdapter = new FriendRequestAdapter(mIncoming_requests, getApplicationContext(), access_token, FriendRequestsActivity.this);
                requests.setAdapter(customAdapter);
            }
            else {
                Toast.makeText(getApplicationContext(), "You have no requests!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mTask = null;
        }
    }


    // DON'T MOVE THIS TO A SEPARATE CLASS (SINCE IT DEPENDS ON A STATIC VARIABLE IN THE ABOVE CLASS)

    public void updateRequests() {
        customAdapter.notifyDataSetChanged();
    }

}
