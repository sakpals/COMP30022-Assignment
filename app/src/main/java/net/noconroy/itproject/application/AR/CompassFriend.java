package net.noconroy.itproject.application.AR;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import net.noconroy.itproject.application.NetworkHelper;
import net.noconroy.itproject.application.R;
import net.noconroy.itproject.application.callbacks.NetworkCallback;
import net.noconroy.itproject.application.models.Friends;
import net.noconroy.itproject.application.models.Orientation;

import java.util.ArrayList;

/**
 * Created by Mattias on 17/09/2017.
 */

public class CompassFriend extends Thread {

    private static final String TAG = CompassFriend.class.getSimpleName();

    private Context context;
    private ArrayList<FriendDrawing> friendDrawings;
    private boolean running;

    /***************************************************************************************/
    /*********************************** Class Methods *************************************/
    /***************************************************************************************/

    public CompassFriend(ArrayList<FriendDrawing> friendDrawings, Context context) {
        this.context = context;
        this.friendDrawings = friendDrawings;
        setRunning(false);
    }

    @Override
    public void run() {

        while (running) {
            RetrieveAllFriendLocations();

            try {
                Thread.sleep(10000);            // Every 10 seconds retrieve friend location
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void RetrieveAllFriendLocations() {
        NetworkHelper.GetFriends(new NetworkCallback<Friends>(Friends.class, null) {
            @Override
            public void onSuccess(Friends friends) {
                Log.i(TAG, "Retrieving all friends from server is successful.");

                if (friends != null) {
                    for (Friends.Friend friend : friends.friends) {
                        GetFriendLocation(friend);
                    }
                }
            }

            @Override
            public void onFailure(Failure f) {
                Log.i(TAG, "Failed to retrieve all friend from server!");
            }
        });
    }

    /**
     * @param friend
     *
     * NOTE: There may be an issue with this, i'm not sure if the correct parameter "friend" will be
     * referenced when retrieving our friends name -- need to test with multiple friends.
     */
    private void GetFriendLocation(final Friends.Friend friend) {
        NetworkHelper.RetrieveLocation(friend.profile.username, new NetworkCallback<Orientation>(Orientation.class, null) {

            @Override
            public void onSuccess(Orientation orientation) {
                Log.i(TAG, "Retrieving a friend location is successful!");

                if (!checkIfFriendExists(friend.profile.username, orientation.direction)) {
                    FriendDrawing newFriend = new FriendDrawing();
                    newFriend.setRotationFromNorth(orientation.direction);
                    newFriend.setName(friend.profile.username);
                    newFriend.setId(friend.profile.username);

                    // Set the friends image on the AR view
                    Resources res = context.getResources();
                    Bitmap map = BitmapFactory.decodeResource(res, R.drawable.default_image_png);
                    Bitmap resized_map = reduceBitmapSize(map, 100);
                    newFriend.setBitmap(resized_map);

                    friendDrawings.add(newFriend);
                }
            }

            @Override
            public void onFailure(Failure f) {
                Log.i(TAG, "Retrieving a friend location has failed!");
            }
        });
    }

    private boolean checkIfFriendExists(String username, Double rotation) {
        for (FriendDrawing friend : friendDrawings) {
            if (friend.getName().equals(username)) {
                friend.setRotationFromNorth(rotation);
                return true;
            }
        }
        return false;
    }

    private Bitmap reduceBitmapSize(Bitmap map, int maxSize) {
        int width = map.getWidth();
        int height = map.getHeight();

        float bitmapRatio = ((float) width / (float) height);
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(map, width, height, true);
    }

    public boolean isRunning() {return running;}
    public void setRunning(boolean running) {this.running = running;}
}
