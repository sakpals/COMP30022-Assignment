package net.noconroy.itproject.application;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.noconroy.itproject.application.callbacks.NetworkCallback;
import net.noconroy.itproject.application.models.IncomingFriendRequests;

import java.util.ArrayList;
import java.util.HashMap;

/* Displays the user's incoming friend requests. */
public class FriendRequestsActivity extends AppCompatActivity {

    private String access_token = null;
    private ListView requests;
    private FriendRequestAdapter customAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);

        this.requests = (ListView) findViewById(R.id.FriendRequests);

        showFriendRequests();

    }

    /**
     * Calls an asynchronous task to display a user's friend list
     */
    public void showFriendRequests() {
        NetworkHelper.GetIncomingFriendRequests(new NetworkCallback<IncomingFriendRequests>(IncomingFriendRequests.class) {
            @Override
            public void onSuccess(IncomingFriendRequests incoming) {
                customAdapter = new FriendRequestAdapter(incoming, getApplicationContext(), FriendRequestsActivity.this);
                requests.setAdapter(customAdapter);
            }

            @Override
            public void onFailure(Failure f) {

            }
        });
    }

    // DON'T MOVE THIS TO A SEPARATE CLASS (SINCE IT DEPENDS ON A STATIC VARIABLE IN THE ABOVE CLASS)

    public void updateRequests() {
        customAdapter.notifyDataSetChanged();
    }

}
