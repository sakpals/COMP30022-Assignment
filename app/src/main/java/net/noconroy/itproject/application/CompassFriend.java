package net.noconroy.itproject.application;

import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Mattias on 17/09/2017.
 */

public class CompassFriend extends Thread {

    private static final String TAG = CompassFriend.class.getSimpleName();

    private ArrayList<FriendDrawing> friendDrawings;
    private boolean running;

    // Temporary variable used to create a fake array of friends -- testing purposes
    private JSONArray friends;


    /***************************************************************************************/
    /*********************************** Class Methods *************************************/
    /***************************************************************************************/

    public CompassFriend(ArrayList<FriendDrawing> friendDrawings) {
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

            // once we have friends position - either update friends drawing with that friends
            // position or create a new object inside of it

            try {
                loopThroughJSONArrayAddToFriends(friends);
            } catch (JSONException e) {
                e.printStackTrace();
            }

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

            friend2.put("id", "2");
            friend2.put("name", "james");
            friend2.put("lat", "-37.813628");
            friend2.put("long", "144.963058");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray array = new JSONArray();
        array.put(friend1);
        array.put(friend2);
        return array;
    }

    public boolean isRunning() {return running;}
    public void setRunning(boolean running) {this.running = running;}
}
