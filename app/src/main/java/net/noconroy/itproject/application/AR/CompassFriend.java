package net.noconroy.itproject.application.AR;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import net.noconroy.itproject.application.NetworkHelper;
import net.noconroy.itproject.application.R;
import net.noconroy.itproject.application.RegisterActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Mattias on 17/09/2017.
 */

public class CompassFriend extends Thread {

    private static final String TAG = CompassFriend.class.getSimpleName();

    private Context context;
    private ArrayList<FriendDrawing> friendDrawings;
    private boolean running;

    // Temporary variable used to create a fake array of friends -- testing purposes
    private JSONArray friends;


    /***************************************************************************************/
    /*********************************** Class Methods *************************************/
    /***************************************************************************************/

    public CompassFriend(ArrayList<FriendDrawing> friendDrawings, Context context) {
        this.context = context;
        this.friendDrawings = friendDrawings;
        setRunning(false);

        // Create a fake array of friends
        friends = createFakeFriendLocationUpdates();
    }

    @Override
    public void run() {

        while (running) {

            // create a synchronous request to get friends position - e.g.
            // okHttp.getAllFriendsLocations) returns JSONArray
            // loop through JSONArray

            // for now we create an example location - friends
            AsyncTask getFriendLocation = new RetrieveFriendLocation(RegisterActivity.ACCESS_TOKEN_MESSAGE);
            getFriendLocation.execute();
            // once we have friends position - either update friends drawing with that friends
            // position or create a new object inside of it
            /*
            try {
                loopThroughJSONArrayAddToFriends(friends);
            } catch (JSONException e) {
                e.printStackTrace();
            }*/

            // note - we'll probably have to introduce a timer for how long we can get a friends
            // location - so probably add a timer for each object i.e. 10 minutes after 10 minutes
            // delete that object


            try {
                Thread.sleep(5000);     // sleep for 5 second - don't need friend updates that often
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



    public class RetrieveFriendLocation extends AsyncTask<Object, Void, Boolean> {

        private String access_token;
        private String[] status = null;

        public RetrieveFriendLocation(String access_token) {
            this.access_token = access_token;
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            status = NetworkHelper.RetrieveLocation("bob111", access_token);

            if (status == null) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                Log.i(TAG, "Received friend locations.");
                try {
                    retrieveFriendLocation(status);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                ;
            }
        }

        @Override
        protected void onCancelled() {
            ;
        }
    }

    private void retrieveFriendLocation(String[] friends) {
        if (!friendExists(friends)) {
            FriendDrawing newFriend = new FriendDrawing();

            // Set the friends rotation from north
            newFriend.setRotationFromNorth(Double.valueOf(friends[1]));

            // Create a random id value
            newFriend.setId("1");

            // Set the friends image on the AR view
            Resources res = context.getResources();
            Bitmap map = BitmapFactory.decodeResource(res, R.drawable.default_image_png);
            Bitmap resized_map = reduceBitmapSize(map, 100);
            newFriend.setBitmap(resized_map);

            friendDrawings.add(newFriend);
        }
    }

    private boolean friendExists(String[] friends) {
        if (!friendDrawings.isEmpty()) {
            for (FriendDrawing friend : friendDrawings) {


                // Have to hard code this for now -- as we're not getting any id values from the server
                // IF we dont get ID values from server we have to periodically clear the friendsdrawing
                if (friend.getId().equals("1")) {
                    friend.setRotationFromNorth(Double.valueOf(friends[0]));
                    return true;
                }
            }
        }
        return false;
    }







    private void loopThroughJSONArrayAddToFriends(JSONArray friends) throws JSONException {

        for (int i = 0; i < friends.length(); i++) {

            JSONObject friend = friends.getJSONObject(i);

            // If true -- no need to do anything as it will be updated
            // otherwise we need to add the new friend
            if (!checkIfFriendExistsAndUpdate(friend)) {

                // create a new friend drawing
                FriendDrawing newFriend = new FriendDrawing();
                newFriend.setId(friend.getString("id"));
                newFriend.setName(friend.getString("name"));
                newFriend.setLat(Double.valueOf(friend.getString("lat")));
                newFriend.setLong_(Double.valueOf(friend.getString("long")));

                if (friend.getString("image").equals("na")) {
                    Resources res = context.getResources();
                    Bitmap map = BitmapFactory.decodeResource(res, R.drawable.default_image_png);
                    Bitmap resized_map = reduceBitmapSize(map, 100);
                    newFriend.setBitmap(resized_map);
                }

                // newFriend.setRotationFromNorth(
                updateBearingFromNorth(newFriend);

                // add that friend to the list of friends to render
                friendDrawings.add(newFriend);
            }
        }
    }

    private boolean checkIfFriendExistsAndUpdate(JSONObject friendObject) throws JSONException {
        if (!friendDrawings.isEmpty()) {
            for (FriendDrawing friend : friendDrawings) {

                // If the friend already exists within our updates
                if (friend.getId().equals(friendObject.getString("id"))) {

                    // Update friends lat and long
                    friend.setLat(Double.valueOf(friendObject.getString("lat")));
                    friend.setLong_(Double.valueOf(friendObject.getString("long")));
                    updateBearingFromNorth(friend);

                    return true;
                }
            }
        }
        return false;
    }


    // NOTE: Probably have to do something in case LocationServiceProvider is actually null --
    // though in normal cases it shouldn't be
    private void updateBearingFromNorth(FriendDrawing friend) {

        // Set the friends location of bearing from north
        if (LocationServiceProvider.retrieveUserLocation() != null) {
            Log.d(TAG, "LocationServiceProvider location not null.");

            Location currentLocation = LocationServiceProvider.retrieveUserLocation();
            friend.setRotationFromNorth(LocationCompare.bearing(
                    currentLocation.getLatitude(), currentLocation.getLongitude(),
                    friend.getLat(), friend.getLong_()
            ));
        }

        else {
            Log.d(TAG, "LocationServiceProvider location is null.");
        }
    }


    // Used to create a test friend location update for now
    private JSONArray createFakeFriendLocationUpdates() {
        JSONObject friend1 = new JSONObject();
        JSONObject friend2 = new JSONObject();
        try {
            friend1.put("id", "1");
            friend1.put("name", "bob");
            friend1.put("lat", "-37.84391867784434");
            friend1.put("long", "145.062575340271");
            friend1.put("image", "na");

            friend2.put("id", "2");
            friend2.put("name", "james");
            friend2.put("lat", "-37.813628");
            friend2.put("long", "144.963058");
            friend2.put("image", "na");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray array = new JSONArray();
        array.put(friend1);
        array.put(friend2);
        return array;
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
