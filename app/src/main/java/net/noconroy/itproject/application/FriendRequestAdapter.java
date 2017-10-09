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
import java.util.HashMap;

/**
 * Created by sampadasakpal on 9/10/17.
 */

public class FriendRequestAdapter extends BaseAdapter {


        private ArrayList<String> mUsernamesList;
        private String access_token;
        private ArrayList mFriendshipTokensList;
        private HashMap<String, String> mRequests;
        private Context mContext;
        public Button mAcceptButton;
        private FriendRequestsActivity mActivity;
        private FriendRequestAdapter.AcceptFriendRequestTask mAcceptTask = null;

        /**
         * Constructor
         * @param requests a particular request by another user
         * @param context application context
         * @param access_token user (who is accepting requests) access_token
         */
        public FriendRequestAdapter(HashMap<String, String> requests, Context context, String access_token, FriendRequestsActivity friendRequestsActivity) {
            this.mRequests = requests;
            this.mContext = context;
            mUsernamesList = new ArrayList<String>();
            mUsernamesList.addAll(mRequests.keySet());
            mFriendshipTokensList = new ArrayList<String>();
            mFriendshipTokensList.addAll(mRequests.values());
            this.access_token = access_token;
            mActivity = friendRequestsActivity;
        }

        @Override
        public int getCount() {
            return mUsernamesList.size();
        }

        @Override
        public Object getItem(int pos) {
            return mUsernamesList.get(pos);
        }

        @Override
        public long getItemId(int pos) {
            return 0;
        }

        /**
         * Sets up the view for a particular row within the friend requests list
         * @param pos the index position of a particular row
         * @param convertView
         * @param parent
         * @return view
         */
        @Override
        public View getView(final int pos, View convertView, ViewGroup parent) {
            View view = convertView;
            if(view == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.friend_request_row, null);
            }

            // sets the username for a row
            final TextView username = (TextView) view.findViewById(R.id.Username);
            username.setText(mUsernamesList.get(pos));

            // button to accept
            mAcceptButton = (Button) view.findViewById(R.id.AcceptRequestButton);

            // on click listener for accept button
            mAcceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    acceptFriendRequest(mRequests.get(username.getText()), access_token);
                }
            });
            return view;
        }

        /**
         * Calls an asynchronous task to accept a friend request. This method is called when
         * the user clicks on the accept button for a particular friend request.
         * @param friendship_token belonging to the future friend to be accepted
         * @param access_token belonging to the user
         */
        public void acceptFriendRequest(String friendship_token, String access_token) {

            if(mAcceptTask != null) {
                return;
            }
            else {
                mAcceptTask = new AcceptFriendRequestTask(friendship_token, access_token);
                mAcceptTask.execute((Void) null);
            }
        }

        /**
         * Asynchronous task that accepts a friend request by calling upon NetworkHelper
         * method 'AcceptFriend'. Handles errors in the main thread.
         */
        public class AcceptFriendRequestTask extends AsyncTask<Void, Void, Boolean> {

            private String mFriendship_token;
            private String access_token;
            private String response;

            public AcceptFriendRequestTask(String friendship_token, String access_token) {
                this.mFriendship_token = friendship_token;
                this.access_token = access_token;
            }

            @Override
            protected  Boolean doInBackground(Void... params) {
                response = NetworkHelper.AcceptFriend(mFriendship_token, access_token);
                if(response.equals("404")) {
                    mActivity.runOnUiThread(already_accepted_request_error);
                    return false;
                }
                else if(response.equals("400")) {
                    mActivity.runOnUiThread(already_friends_error);
                    removeRequest(mFriendship_token);
                    mActivity.updateRequests();
                    return false;
                }
                else if(response.equals("500")) {
                    mActivity.runOnUiThread(accept_request_error);
                    return false;
                }
                else {
                    return true;
                }
            }

            @Override
            protected void onPostExecute(final Boolean success) {
                mAcceptTask = null;
                if(success) {
                    Toast.makeText(mActivity.getApplicationContext(), "ACCEPTED!", Toast.LENGTH_SHORT).show();
                    removeRequest(mFriendship_token);
                    mActivity.updateRequests();
                }
                // errors handled on main thread
            }

            @Override
            protected void onCancelled() {
                mAcceptTask = null;
            }

            /**
             * Updates the friend requests list in the current view
             * @param friendship_token
             */
            public void removeRequest(String friendship_token) {
                for(int pos = 0; pos < mFriendshipTokensList.size(); pos++) {
                    if(mFriendshipTokensList.get(pos).equals(friendship_token)) {
                        mUsernamesList.remove(pos);
                        break;
                    }
                }
            }
        }

        /* Handles errors generated when accepting friends. To be run on the main thread. */

        /**
         * Handles the case when a user has already accepted a friend.
         * NOTE: This is never really called as the list view is modified as soon
         * as a user accepts a friend so that they cannot accept twice. Just a precaution.
         */
        private Runnable already_accepted_request_error = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mActivity.getApplicationContext(), "Already accepted this friend!", Toast.LENGTH_SHORT).show();
            }
        };

        /**
         * Handles the case when there may be some network error disabling a user
         * from accepting a friend request correctly.
         */
        private Runnable accept_request_error = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mActivity.getApplicationContext(), "Error in accepting friend request!", Toast.LENGTH_SHORT).show();

            }
        };

        /**
         * Handles the case when a user is trying to accept a friend request from
         * someone who is already their friend.
         * NOTE: Again, this shouldn't get called, but included in an effort to handle all server
         * error messages.
         */
        private Runnable already_friends_error = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mActivity.getApplicationContext(), "Already friends with this user!", Toast.LENGTH_SHORT).show();
            }
        };

}