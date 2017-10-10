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
import net.noconroy.itproject.application.models.IncomingFriendRequests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sampadasakpal on 9/10/17.
 */

public class FriendRequestAdapter extends BaseAdapter {

    private IncomingFriendRequests mRequests;
    private Context mContext;
    public Button mAcceptButton;
    private FriendRequestsActivity mActivity;

        /**
         * Constructor
         * @param requests a particular request by another user
         * @param context application context
         */
        public FriendRequestAdapter(IncomingFriendRequests requests, Context context, FriendRequestsActivity friendRequestsActivity) {
            this.mRequests = requests;
            this.mContext = context;
            mActivity = friendRequestsActivity;
        }

        @Override
        public int getCount() {
            return mRequests.requests.size();
        }

        @Override
        public Object getItem(int pos) {
            return mRequests.requests.get(pos);
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
            username.setText(mRequests.requests.get(pos).profile.username);

            // button to accept
            mAcceptButton = (Button) view.findViewById(R.id.AcceptRequestButton);

            // on click listener for accept button
            mAcceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NetworkHelper.AcceptFriend(mRequests.requests.get(pos).token, new EmptyCallback(mActivity) {
                        @Override
                        public void onSuccess(Void object) {
                            Toast.makeText(mActivity.getApplicationContext(), "ACCEPTED!", Toast.LENGTH_SHORT).show();
                            removeRequest();
                        }

                        @Override
                        public void onFailure(Failure f) {
                            if(f.code == 404) {
                                Toast.makeText(mActivity.getApplicationContext(), "Already accepted this friend!", Toast.LENGTH_SHORT).show();
                            }

                            if(f.code == 400) {
                                Toast.makeText(mActivity.getApplicationContext(), "Already friends with this user!", Toast.LENGTH_SHORT).show();
                            }

                            if(f.code == 500) {
                                Toast.makeText(mActivity.getApplicationContext(), "Error in accepting friend request!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
            return view;
        }

        private void removeRequest() {
            mActivity.updateRequests();
        }

}