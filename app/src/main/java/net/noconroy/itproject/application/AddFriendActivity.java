package net.noconroy.itproject.application;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.noconroy.itproject.application.callbacks.EmptyCallback;

public class AddFriendActivity extends AppCompatActivity {

    // UI
    private EditText mSearchUser;
    private Button mFindUserButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        Intent i = getIntent();

        mSearchUser = (EditText) findViewById(R.id.username_search);
        mFindUserButton = (Button) findViewById(R.id.find_friend_button);

        mFindUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptFindUser();
            }
        });
    }

    /**
     * Attempts to find a user via asynchronous task.
     */
    public void attemptFindUser() {

        String username = mSearchUser.getText().toString();

        if(TextUtils.isEmpty(username) || username == null) {
            mSearchUser.setError("No username entered");
            mSearchUser.requestFocus();
        }

        else {
            Toast t = Toast.makeText(getApplicationContext(), "Attempting to add friend: "+username, Toast.LENGTH_SHORT);
            t.show();

            NetworkHelper.AddFriend(username, new EmptyCallback(this) {
                @Override
                public void onSuccess(Void object) {
                    Toast t = Toast.makeText(getApplicationContext(), "Sent friend request!", Toast.LENGTH_SHORT);
                    t.show();
                }

                @Override
                public void onFailure(Failure f) {
                    if(f.code == 400) {
                        runOnUiThread(already_sent_request_error);
                    }
                    else if(f.code == 404) {
                        runOnUiThread(user_does_not_exist);
                    }
                    else if(f.code == 500) {
                        runOnUiThread(general_error);
                    }
                }
            });
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
