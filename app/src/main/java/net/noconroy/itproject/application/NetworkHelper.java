package net.noconroy.itproject.application;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by James on 7/09/2017.
 */

public final class NetworkHelper {

    /************************************************************************/
    /********************* Constants ****************************************/
    /************************************************************************/

    private static final String SERVER_SCHEME = "http";
    private static final String SERVER_HOST = "127.0.0.1";
    private static final Integer SERVER_PORT = 5000;
    private static final String JSON_HEADER_NAME = "content-type";
    private static final String ACCESS_TOKEN_NAME = "access_token";
    private static final String JSON_HEADER_VALUE = "application/json; charset=utf-8";
    private static final MediaType JSON = MediaType.parse("Content-Type: application/json");
    private static final String SERVER_ADDRESS = "http://127.0.0.1:5000";
    private static final String USER_LOGIN = "user/login";
    private static final String USER_LOGOUT = "user/logout";
    private static final String USER_REGISTER = "user/register";
    private static final String USER_UPDATE_PROFILE = "profile/";
    private static final String FRIEND_LIST = "/friends";
    private static final String FRIEND_ADD = "friends/add/";
    private static final String FRIEND_ACCEPT = "friends/accept/";
    private static final String FRIEND_REMOVE = "/friends/remove";
    private static final String FRIEND_REQUESTS_IN = "/friends/requests/in";
    private static final String FRIEND_REQUESTS_OUT = "/friends/requests/out";
    private static final String LOCATION = "location/";


    /************************************************************************/
    /********************* User Profile Methods *****************************/
    /************************************************************************/


    /**
     * Registers a user in the database, for now the user MUST enter all paramaters
     *
     * @param username The username that the user wishes to use, must not already be registered
     *                 and TODO: must make sure the username is of correct format e.g. no invalid chars
     * @param password The user chooses a password
     * @param avatar_url The user chooses an avatar_url
     * @param description The user chooses a description
     * @return The http message the server sends in response to this request
     */
    public static String Register(String username, String password, String avatar_url, String description) {
        final OkHttpClient client = new OkHttpClient();

        RequestBody body =  createBodyRequest(new String [] {"password", password},
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
        try {
            Response response = call.execute();
            return Integer.toString(response.code());
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
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
    public static String Login(String username, String password) {
        final OkHttpClient client = new OkHttpClient();

        RequestBody body =  createBodyRequest(new String [] {"username", username},
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
        try {
            Response response = call.execute();
            String res = response.body().string();

            if (response.code() != 200) {
                return Integer.toString(response.code());
            }

            JsonObject token = new JsonParser().parse(res).getAsJsonObject();
            return token.get(ACCESS_TOKEN_NAME).toString();

        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }


    /**
     * Logs the user out, which thus makes the current access token invalid
     *
     * @param access_token The users access token they received at login
     * @return The http message the server sends in response to this request
     */
    public static String Logout(String access_token) {
        final OkHttpClient client = new OkHttpClient();

        // Remove quotation marks so it is in the correct format for okhttp3
        access_token = removeQuotations(access_token);

        HttpUrl url = constructURL(USER_LOGOUT, access_token);

        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            return Integer.toString(response.code());
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }


    /**
     * Allows the user to update their own profle
     *
     * @param username The users username
     * @param password The users password
     * @param avatar_url NOT CURRENTLY WORKING, but is the url the user wants to change it to
     * @param description The new description the user wants on their profile
     * @param access_token Users access token recieved at login
     * @return The http message the server sends in response to this request
     */
    public static String UpdateProfile(String username, String password, String avatar_url, String description, String access_token) {
        final OkHttpClient client = new OkHttpClient();

        RequestBody body =  createBodyRequest(new String [] {"description", description});

        // Remove quotation marks so it is in the correct format for okhttp3
        access_token = removeQuotations(access_token);

        HttpUrl url = constructURL(USER_UPDATE_PROFILE + username, access_token);

        Request request = new Request.Builder()
                .addHeader(JSON_HEADER_NAME, JSON_HEADER_VALUE)
                .url(url)
                .put(body)
                .build();

        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            return Integer.toString(response.code());
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    /**
     * Allows a user to get the profile of any other registered user
     *
     * @param username Any users username in the database
     * @param access_token The current users username, does not need to be the one
     *                     assoicaed with username
     * @return The description of the profile for the user inputted as the username paramater
     */
    public static String GetProfile(String username, String access_token) {
        final OkHttpClient client = new OkHttpClient();

        // Remove quotation marks so it is in the correct format for okhttp3
        access_token = removeQuotations(access_token);

        HttpUrl url = constructURL(USER_UPDATE_PROFILE + username, access_token);

        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = client.newCall(request);
        String jsonData = null;
        try {
            Response response = call.execute();

            // Raw JSON data below in String format
            jsonData = response.body().string();

        }  catch (IOException e) {
            e.printStackTrace();
        }
        try {
            // Extract description from JSON String
            JSONObject jsonobject = new JSONObject(jsonData);
            return jsonobject.getString("description");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Should never reach here
        return null;
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
     * @param access_token Current user access token
     * @return The http message the server sends in response to this request
     */
    public static String UpdateLocation(String username, String lat, String lon, String access_token) {
        final OkHttpClient client = new OkHttpClient();

        RequestBody body =  createBodyRequest(new String [] {"lat", lat},
                                              new String [] {"lon", lon});

        // Remove quotation marks so it is in the correct format for okhttp3
        access_token = removeQuotations(access_token);

        HttpUrl url = constructURL(LOCATION + username, access_token);

        Request request = new Request.Builder()
                .addHeader(JSON_HEADER_NAME, JSON_HEADER_VALUE)
                .url(url)
                .post(body)
                .build();

        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            return Integer.toString(response.code());
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }


    /**
     * Get the distance and direction of a user
     *
     * @param username Current users username
     * @param access_token Current users access token
     * @return A 2 element string array containing distance and direction
     * respectively
     */
    public static String[] RetrieveLocation(String username, String access_token) {
        final OkHttpClient client = new OkHttpClient();

        // Remove quotation marks so it is in the correct format for okhttp3
        access_token = removeQuotations(access_token);

        HttpUrl url = constructURL(LOCATION + username, access_token);

        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = client.newCall(request);
        String jsonData = null;
        try {
            Response response = call.execute();

            // Raw JSON data below in String format
            jsonData = response.body().string();

        }  catch (IOException e) {
            e.printStackTrace();
        }
        try {
            // Extract description from JSON String
            JSONObject jsonobject = new JSONObject(jsonData);

            // Note that optString is used here, so could return ""
            String dist = jsonobject.optString("distance");
            String dir = jsonobject.optString("direction");
            return new String[] {dist, dir};

        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Should never reach here
        return null;
    }


    /************************************************************************/
    /********************* Friend System Methods ****************************/
    /************************************************************************/


    /**
     *
     * @param username The username of the user you want to add
     * @param access_token The current users access token (not the target users)
     * @return The http message the server sends in response to this request
     */
    public static String AddFriend(String username, String access_token) {
        final OkHttpClient client = new OkHttpClient();

        // Remove quotation marks so it is in the correct format for okhttp3
        access_token = removeQuotations(access_token);

        HttpUrl url = constructURL(FRIEND_ADD + username, access_token);

        Request request = new Request.Builder()
                .url(url)
                //.post(body)
                .build();
        System.out.println(request);

        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            return Integer.toString(response.code());
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    /**
     *
     *
     * @param username The username of the user who sent the friend request
     * @param access_token The access token of the user who is accepting the
     *                     friend request
     * @return The http message the server sends in response to this request
     */
    public static String AcceptFriend(String username, String access_token) {
        final OkHttpClient client = new OkHttpClient();

        // Remove quotation marks so it is in the correct format for okhttp3
        access_token = removeQuotations(access_token);

        HttpUrl url = constructURL(FRIEND_ACCEPT + username, access_token);

        Request request = new Request.Builder()
                .url(url)
                .build();
        System.out.println(request);

        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            return Integer.toString(response.code());
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }


    /**
     * Gets the profile of all a users friends
     *
     * @param access_token The access token of the user who's friends list
     *                     will be displayed
     * @return A 2d arraylist, the outer arraylist being each profile, and
     * th inner arraylist displaying the profile attribute values, which is
     * currently only username and description
     */
    public static ArrayList<ArrayList<String>> GetFriends(String access_token) {
        final OkHttpClient client = new OkHttpClient();

        // Remove quotation marks so it is in the correct format for okhttp3
        access_token = removeQuotations(access_token);

        HttpUrl url = constructURL(FRIEND_LIST, access_token);

        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = client.newCall(request);
        String jsonData = null;
        try {
            Response response = call.execute();

            // Raw JSON data below in String format
            jsonData = response.body().string();

        }  catch (IOException e) {
            e.printStackTrace();
        }
        try {
            // Extract description from JSON String
            JSONObject jsonobject = new JSONObject(jsonData);
            System.out.println(jsonobject);
            return jsonProfileToArrayList(jsonobject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Should never reach here
        return null;
    }

    public static String RemoveFriend(String username, String access_token) {
        final OkHttpClient client = new OkHttpClient();

        String url = SERVER_ADDRESS + FRIEND_REMOVE + username;

        RequestBody body = new FormBody.Builder()
                .add("access_token", access_token)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            return Integer.toString(response.code());
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public static String GetIncomingFriendRequests(String access_token) {
        return "200";
    }

    public static String GetOutgoingFriendRequests(String access_token) {
        return "200";
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


    // Removes quotation marks from a string
    private static String removeQuotations(String str){
        return str.replaceAll("^\"|\"$", "");
    }


    /**
     * Constructs a url, as most urls only differ by segment and paramater
     *
     * @param segment Strings of the segments wanted in the URL
     * @param token String of the access token
     * @return A constructed url
     */
    private static HttpUrl constructURL(String segment, String token){
        HttpUrl url = new HttpUrl.Builder()
                .scheme(SERVER_SCHEME)
                .host(SERVER_HOST)
                .port(SERVER_PORT)
                .addPathSegments(segment)
                .addQueryParameter(ACCESS_TOKEN_NAME, token)
                .build();
        return url;
    }


    /**
     * Converts a json object representing a users profile to a nicely
     * formatted 2d ArrayList
     *
     * NOTE: does not work for avatar_url
     *
     * @param json The json object representing a users profile
     * @return A 2d ArrayList of length equalling the number of friends, each sub array
     * list containing username and description respectively
     * users profile
     */
    private static ArrayList<ArrayList<String>> jsonProfileToArrayList (JSONObject json){

        // The possible names of the attributes in the json file for the profile
        final String[] profileNames = {"username", "description"};

        int outlen = -1;
        try {
            outlen = json.getJSONArray("friends").length();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayList<ArrayList<String>> output = new ArrayList<ArrayList<String>>(outlen);

        try {
            // Get the profile for ONE user
            JSONArray profileArray = json.getJSONArray("friends");

            // Iterate over each profile
            for (int i=0; i< profileArray.length(); i++){

                JSONObject profile = profileArray.getJSONObject(i);
                ArrayList<String> line = new ArrayList<String>(profileNames.length);
                JSONObject profileContents = profile.getJSONObject("profile");

                // Extract values from each attribute from the profile
                for (int j=0; j<profileNames.length; j++){
                    if (profileContents.getString(profileNames[j]) != null){
                        line.add(profileContents.getString(profileNames[j]).toString());
                    }
                }
                output.add(line);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return output;
    }
}









