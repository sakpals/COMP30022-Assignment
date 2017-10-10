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

import java.util.ArrayList;

/**
 * Created by sampadasakpal on 9/10/17.
 */

public class FriendsListAdapter extends BaseAdapter {

    private ArrayList<ArrayList<String>> mFriendProfiles;
    private ArrayList<String> mFriendUsernamesList = new ArrayList<String>();
    private Context mContext;
    private Button mRemoveButton;
    private String access_token;
    private Friends mActivity;
    private RemoveFriendTask mRemoveTask = null;

    public FriendsListAdapter(ArrayList<ArrayList<String>> profiles, Context context, String access_token, Friends friendsActivity) {
        mContext = context;
        mActivity = friendsActivity;
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
                mActivity.runOnUiThread(remove_friend_error);
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mRemoveTask = null;
            if(success) {
                Toast.makeText(mActivity.getApplicationContext(), "Removed Friend!", Toast.LENGTH_SHORT).show();
                mFriendUsernamesList.remove(index);
                mActivity.updateFriendsList();
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
            Toast.makeText(mActivity.getApplicationContext(), "Attempt to remove friend failed!", Toast.LENGTH_SHORT).show();
        }
    };
}
