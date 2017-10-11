package net.noconroy.itproject.application;

/**
 * This class provides a RESTful interface in order for the server to easily
 * interact with the client
 */

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.noconroy.itproject.application.callbacks.AuthenticationCallback;
import net.noconroy.itproject.application.callbacks.EmptyCallback;
import net.noconroy.itproject.application.callbacks.NetworkCallback;
import net.noconroy.itproject.application.models.Friends;
import net.noconroy.itproject.application.models.IncomingFriendRequests;
import net.noconroy.itproject.application.models.Message;
import net.noconroy.itproject.application.models.Orientation;
import net.noconroy.itproject.application.models.OutgoingFriendRequests;
import net.noconroy.itproject.application.models.Profile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Created by James on 7/09/2017.
 * Created by Bryce
 */

public final class NetworkHelper {

    /************************************************************************/
    /********************* Constants ****************************************/
    /************************************************************************/

    private static final String SERVER_SCHEME = "https";
    private static final String SERVER_HOST ="itproject.noconroy.net";
    private static final Integer SERVER_PORT = 443;
    private static final String JSON_HEADER_NAME = "content-type";
    private static final String ACCESS_TOKEN_NAME = "access_token";
    private static final String JSON_HEADER_VALUE = "application/json; " +
            "charset=utf-8";
    private static final MediaType JSON = MediaType.parse("Content-Type: " +
            "application/json");
    private static final String USER_LOGIN = "user/login";
    private static final String USER_LOGOUT = "user/logout";
    private static final String USER_REGISTER = "user/register";
    private static final String USER_PROFILE = "profile";
    private static final String USER_SPECIFIC_PROFILE = "profile/";

    private static final String FRIEND_LIST = "friends";  // removed an unecessary extra '/' for URL building
    private static final String FRIEND_ADD = "friends/add/";
    private static final String FRIEND_ACCEPT = "friends/accept/";
    private static final String FRIEND_REMOVE = "friends/remove/";
    private static final String FRIEND_REQUESTS_IN = "friends/requests/in";
    private static final String FRIEND_REQUESTS_OUT = "friends/requests/out";
    private static final String LOCATION = "location/";

    private static final String CHANNEL = "channel/";
    private static final String JOIN = "/join";
    private static final String LEAVE = "/leave";
    private static final String MESSAGE = "/message";
    private static final String LISTEN_CHANNELS = "sync";

    private static final OkHttpClient client = new OkHttpClient();

    /************************************************************************/
    /********************* User Profile Methods *****************************/
    /************************************************************************/

    /**
     * Registers a user in the database, for now the user MUST enter all
     * paramaters
     *
     * @param username The username that the user wishes to use, must not
     *                 already be registered
     *                 and
     * @param password The user chooses a password
     * @param avatar_url The user chooses an avatar_url
     * @param description The user chooses a description
     * @return The http message the server sends in response to this request
     */
    public static void Register(String username, String password,
                                  String avatar_url, String description, AuthenticationCallback cb) {

        RequestBody body =  createBodyRequest(new String []
                        {"password", password},
                new String [] {"avatar_url", avatar_url},
                new String [] {"description", description},
                new String [] {"username", username});

        HttpUrl url = new HttpUrl.Builder()
                .scheme(SERVER_SCHEME)
                .host(SERVER_HOST)
                .port(SERVER_PORT)
                .addPathSegments(USER_REGISTER)
                .build();

        Request request = new Request.Builder()
                .addHeader(JSON_HEADER_NAME, JSON_HEADER_VALUE)
                .url(url)
                .post(body)
                .build();


        Call call = client.newCall(request);
        call.enqueue(cb);
    }


    /**
     * Logs the user into the server and returns an access token, which
     * they can then use to access more priviliged methods
     *
     * @param username The username the user inputted when they registered
     * @param password The password the user inputted when they registered
     * @return The access token assoicated with the user, or if the request
     * http message was invalid, return the servers http message response code
     */
    public static void Login(String username, String password, AuthenticationCallback cb) {
        RequestBody body =  createBodyRequest(new String []
                        {"username", username},
                new String [] {"password", password});

        HttpUrl url = new HttpUrl.Builder()
                .scheme(SERVER_SCHEME)
                .host(SERVER_HOST)
                .port(SERVER_PORT)
                .addPathSegments(USER_LOGIN)
                .build();

        Request request = new Request.Builder()
                .addHeader(JSON_HEADER_NAME, JSON_HEADER_VALUE)
                .url(url)
                .post(body)
                .build();

        Call call = client.newCall(request);
        call.enqueue(cb);
    }


    /**
     * Logs the user out, which thus makes the current access token invalid
     *
     * @return The http message the server sends in response to this request
     */
    public static void Logout(EmptyCallback cb) {
        // Create an empty body
        RequestBody body = RequestBody.create(null, new byte[0]);

        HttpUrl url = constructURL(USER_LOGOUT);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Call call = client.newCall(request);
        call.enqueue(cb);
    }


    /**
     * Allows the user to update their own profle
     *
     * @param username The users username
     * @param password The users password
     * @param avatar_url NOT CURRENTLY WORKING, but is the url the user wants
     *                   to change it to
     * @param description The new description the user wants on their profile
     * @return The http message the server sends in response to this request
     */
    public static void UpdateProfile(String username, String password,
                                     String avatar_url, String description,
                                     EmptyCallback cb) {
        RequestBody body =  createBodyRequest(new String []
                {"description", description});


        if(username == null)
            username = DataStorage.getInstance().me.username;

        HttpUrl url = constructURL(USER_SPECIFIC_PROFILE + username);

        Request request = new Request.Builder()
                .addHeader(JSON_HEADER_NAME, JSON_HEADER_VALUE)
                .url(url)
                .put(body)
                .build();

        Call call = client.newCall(request);
        call.enqueue(cb);
    }

    /**
     * Allows a user to get the profile of any other registered user
     *
     * @param username Any users username in the database
     * @return The description of the profile for the user inputted as the
     * username paramater
     */
    public static void GetProfile(String username, NetworkCallback<Profile> cb) {

        HttpUrl url;
        if(username == null)
            url = constructURL(USER_PROFILE);
        else
            url = constructURL(USER_SPECIFIC_PROFILE + username);

        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = client.newCall(request);
        call.enqueue(cb);
    }


    /************************************************************************/
    /********************* Location System Methods **************************/
    /************************************************************************/


    /**
     * Update the location of the current user
     *
     * @param username Current users username
     * @param lat Current users current latitude
     * @param lon Current users current longitude
     * @return The http message the server sends in response to this request
     */
    public static void UpdateLocation(String username, Double lat, Double lon,
                                      EmptyCallback cb) {
        RequestBody body = createBodyRequest(new String [] {"lat", lat.toString()},
                new String [] {"lon", lon.toString()});

        username = username == null ? DataStorage.getInstance().me.username : username;

        HttpUrl url = constructURL(LOCATION + username);

        Request request = new Request.Builder()
                .addHeader(JSON_HEADER_NAME, JSON_HEADER_VALUE)
                .url(url)
                .post(body)
                .build();

        Call call = client.newCall(request);
        call.enqueue(cb);
    }

    /**
     * Make it so that the user is sharing his/her location with all friends.
     *
     * @param username
     * @return
     */
    public static void ResetLocation(String username, EmptyCallback cb) {
        UpdateLocation(username, (double) 0, (double) 0, cb);
    }

    /**
     * Get the distance and direction of a user
     *
     * @param username Current users username
     * @return A 2 element string array containing distance and direction
     * respectively
     */
    public static void RetrieveLocation(String username, NetworkCallback<Orientation> cb) {

        HttpUrl url = constructURL(LOCATION + username);

        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = client.newCall(request);
        call.enqueue(cb);
    }


    /************************************************************************/
    /********************* Friend System Methods ****************************/
    /************************************************************************/


    /**
     *
     * @param username The username of the user you want to add
     * @return The http message the server sends in response to this request
     */
    public static void AddFriend(String username, EmptyCallback cb) {
        // Create an empty body
        RequestBody body = RequestBody.create(null, new byte[0]);

        HttpUrl url = constructURL(FRIEND_ADD + username);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Call call = client.newCall(request);
        call.enqueue(cb);
    }

    /**
     *
     *
     * @param friendship_token The friendship_token of the user who sent the
     *                         friend request
     * @return The http message the server sends in response to this request
     */
    public static void AcceptFriend(String friendship_token, EmptyCallback cb) {
        // Create an empty body
        RequestBody body = RequestBody.create(null, new byte[0]);

        HttpUrl url = constructURL(FRIEND_ACCEPT + friendship_token);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Call call = client.newCall(request);
        call.enqueue(cb);
    }


    /**
     * Gets the profile of all a users friends
     * @return A 2d arraylist, the outer arraylist being each profile, and
     * th inner arraylist displaying the profile attribute values, which is
     * currently only username and description
     */
    public static void GetFriends(NetworkCallback<Friends> cb) {

        HttpUrl url = constructURL(FRIEND_LIST);

        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = client.newCall(request);
        call.enqueue(cb);
    }


    /**
     *
     * @param username The username of the friend you wish to remove
     * @return The http message the server sends in response to this request
     */
    public static void RemoveFriend(String username, EmptyCallback cb) {
        // Create an empty body
        RequestBody body = RequestBody.create(null, new byte[0]);

        HttpUrl url = constructURL(FRIEND_REMOVE + username);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Call call = client.newCall(request);
        call.enqueue(cb);
    }


    /**
     * Gets a users incoming friend requests
     *
     * @return If there are no incoming friend requests, null is returned. If
     * there is, a hashamp is returned, with the key:username, value:
     * friendship request token
     */
    public static void GetIncomingFriendRequests(NetworkCallback<IncomingFriendRequests> cb) {

        HttpUrl url = constructURL(FRIEND_REQUESTS_IN);

        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = client.newCall(request);
        call.enqueue(cb);
    }


    /**
     * Gets a users outgoing friend requests
     *
     * @return If there are no outgoing friend requests, null is returned.
     * If there is, a 2d arraylist is returned, the outer arraylist containing
     * the profiles, the inner array containg profile information (which is
     * only usernamesso far)
     */
    public static void GetOutgoingFriendRequests(NetworkCallback<OutgoingFriendRequests> cb) {

        HttpUrl url = constructURL(FRIEND_REQUESTS_OUT);

        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = client.newCall(request);
        call.enqueue(cb);
    }

    /************************************************************************/
    /********************* Channel Methods ****************************/
    /************************************************************************/


    public static void ChannelCreate(String name, Boolean persistent, EmptyCallback cb) {

        HttpUrl url = constructURL(CHANNEL + name);

        RequestBody body = new FormBody.Builder()
                .add("persistent", persistent.toString())
                .build();

        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();

        Call call = client.newCall(request);
        call.enqueue(cb);
    }

    public static void ChannelDelete(String name, EmptyCallback cb) {

        HttpUrl url = constructURL(CHANNEL + name);

        Request request = new Request.Builder()
                .url(url)
                .delete(null)
                .build();

        Call call = client.newCall(request);
        call.enqueue(cb);
    }

    public static void ChannelSubscribe(String name, EmptyCallback cb) {
        HttpUrl url = constructURL(CHANNEL + name + JOIN);

        Request request = new Request.Builder()
                .url(url)
                .post(null)
                .build();

        Call call = client.newCall(request);
        call.enqueue(cb);
    }

    public static void ChannelLeave(String name, EmptyCallback cb) {

        HttpUrl url = constructURL(CHANNEL + name + LEAVE);

        Request request = new Request.Builder()
                .url(url)
                .post(null)
                .build();

        Call call = client.newCall(request);
        call.enqueue(cb);
    }

    public class Messages { public List<Message> messages; };

    public static void ChannelMessages(String name, NetworkCallback<Messages> cb) {
        HttpUrl url = constructURL(CHANNEL + name + MESSAGE);

        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = client.newCall(request);
        call.enqueue(cb);
    }

    public static void ChannelMessage(String name, String type, JsonObject message, NetworkCallback<Message> cb) {

        HttpUrl url = constructURL(CHANNEL + name + MESSAGE);

        Gson gson = new Gson();

        RequestBody body = new FormBody.Builder()
                .add("type", type)
                .add("data", message.toString())
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Call call = client.newCall(request);
        call.enqueue(cb);
    }

    // Websocket parts

    private static final class ReceiverWebSocket extends WebSocketListener {

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            System.out.println("WebSocket Listening");
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Message m = new Gson().fromJson(text, Message.class);
            Receiver r = listeners.get(m.type);
            if(r != null)
                r.process(m);
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(code, null);
            System.out.println("Closing: " + code + " " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            t.printStackTrace();
        }
    }

    private static WebSocket webSocket = null;

    public static void ChannelListen() {

        if(webSocket != null)
            return;

        HttpUrl url = constructURL(LISTEN_CHANNELS);

        Request request = new Request.Builder()
                .url(url.toString().replace(SERVER_SCHEME, "ws"))
                .build();

        webSocket = client.newWebSocket(request, new ReceiverWebSocket());
    }

    public static void ChannelStopListen() {
        webSocket.cancel();
        webSocket = null;
    }

    /* Listener interface */

    public interface Receiver {
        void process(Message message);
    }

    public static HashMap<String,Receiver> listeners = new HashMap<>();

    public static void ChannelAddListener(String type, Receiver listener) {
        listeners.put(type, listener);
    }

    public static void ChannelRemoveListener(String type) {
        listeners.remove(type);
    }

    /************************************************************************/
    /************************* Helper Methods *******************************/
    /************************************************************************/


    /**
     * Create a string in json format usuing an arbitary number of arguments
     *
     * @param args Arguments must be String arrays of length 2
     * @return A RequestBody containing the json data
     */
    private static RequestBody createBodyRequest(String[]...args) {

        JSONObject jsonobj = new JSONObject();

        for (String[] arg : args) {
            try {
                jsonobj.put(arg[0], arg[1]);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return RequestBody.create(JSON, jsonobj.toString());
    }



    /**
     * Constructs a url, as most urls only differ by segment and paramater
     *
     * @param segment Strings of the segments wanted in the URL
     * @return A constructed url
     */
    private static HttpUrl constructURL(String segment){
        HttpUrl url = new HttpUrl.Builder()
                .scheme(SERVER_SCHEME)
                .host(SERVER_HOST)
                .port(SERVER_PORT)
                .addPathSegments(segment)
                .addQueryParameter(ACCESS_TOKEN_NAME, DataStorage.getInstance().getAccessToken())
                .build();
        return url;
    }
}









