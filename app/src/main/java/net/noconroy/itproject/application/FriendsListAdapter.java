package net.noconroy.itproject.application;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.noconroy.itproject.application.callbacks.EmptyCallback;
import net.noconroy.itproject.application.callbacks.NetworkCallback;
import net.noconroy.itproject.application.models.Friends;
import net.noconroy.itproject.application.models.Profile;

import java.util.ArrayList;

/**
 * Created by sampadasakpal on 9/10/17.
 */

public class FriendsListAdapter extends BaseAdapter {

    private Context mContext;
    private Button mRemoveButton;
    private FriendsActivity mActivity;
    Friends mFriends;

    public FriendsListAdapter(Friends friends, Context context, FriendsActivity friendsActivity) {
        mContext = context;
        mActivity = friendsActivity;
        mFriends = friends;
    }

    @Override
    public int getCount() {
        return mFriends.friends.size();
    }

    @Override
    public Object getItem(int pos) {
        return mFriends.friends.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return 0;
    }

    /* Sets up the view for a particular row within the friends list. */
    @Override
    public View getView(final int pos, View convertView, ViewGroup parent) {
        final Profile friend = mFriends.friends.get(pos);
        View view = convertView;
        if(view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.friends_list_row, null);
        }

        // sets the username for a row
        final TextView username = (TextView) view.findViewById(R.id.FriendUsername);
        username.setText(friend.username);

        // button to remove friend
        mRemoveButton = (Button) view.findViewById(R.id.RemoveButton);
        mRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetworkHelper.RemoveFriend(friend.username, new EmptyCallback(mActivity) {

                    @Override
                    public void onSuccess(Void object) {
                        Toast.makeText(mActivity.getApplicationContext(), "Removed Friend!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Failure f) {
                        mActivity.runOnUiThread(remove_friend_error);
                    }
                });
            }
        });

        return view;
    }

    /**
     * Handles the case when there is a network error in attempting to remove a friend.
     * To be run on the main thread.
     */
    private Runnable remove_friend_error = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(mActivity.getApplicationContext(), "Attempt to remove friend failed!", Toast.LENGTH_SHORT).show();
        }
    };
}
