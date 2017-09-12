package net.noconroy.itproject.application;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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
    private static final String SERVER_SCHEME = "http";
    private static final String SERVER_HOST = "127.0.0.1";
    private static final Integer SERVER_PORT = 5000;
    private static final String SERVER_ADDRESS = "http://127.0.0.1:5000";
    private static final String USER_LOGIN = "/user/login";
    private static final String USER_LOGOUT = "/user/logout";
    private static final String USER_REGISTER = "/user/register";
    private static final String USER_UPDATE_PROFILE = "/profile/";
    private static final String FRIEND_LIST = "/friends";
    private static final String FRIEND_ADD = "/friends/add/";
    private static final String FRIEND_ACCEPT = "/friends/accept/";
    private static final String FRIEND_REMOVE = "/friends/remove/";
    private static final String FRIEND_REQUESTS_IN = "/friends/requests/in";
    private static final String FRIEND_REQUESTS_OUT = "/friends/requests/out";
    private static final String LOCATION = "/location/";

    public static String Register(String username, String password, String avatar_url, String description) {
        String url = SERVER_ADDRESS + USER_REGISTER;
        final OkHttpClient client = new OkHttpClient();

        RequestBody body = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("avatar_url", avatar_url)
                .add("description", description)
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

    public static String Login(String username, String password) {
        final OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();

        String url = SERVER_ADDRESS + USER_LOGIN;

        RequestBody body = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
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
            return token.get("access_token").toString();
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    // TODO: Unit test fails unauthorised, check
    public static String Logout(String access_token, String username) {
        final OkHttpClient client = new OkHttpClient();

        final MediaType JSON
                = MediaType.parse("Content-Type: application/json");


        // Create a string in the JSON format
        String json = null;
        try {
            json = new JSONObject()
                    .put("username", username)
                    .toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(JSON, json);

        // Remove quotation marks so it is in the correct format for okhttp3
        access_token = access_token.replaceAll("^\"|\"$", "");

        HttpUrl url = new HttpUrl.Builder()
                .scheme(SERVER_SCHEME)
                .host(SERVER_HOST)
                .port(SERVER_PORT)
                .addPathSegments(USER_LOGOUT)
                .addQueryParameter("access_token", access_token)
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

    public static String UpdateProfile(String username, String password, String avatar_url, String description, String access_token) {
        final OkHttpClient client = new OkHttpClient();

        String url = SERVER_ADDRESS + USER_UPDATE_PROFILE + username;

        RequestBody body = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("avatar_url", avatar_url)
                .add("description", description)
                .add("access_token", access_token)
                .build();

        Request request = new Request.Builder()
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

    public static String GetProfile(String username, String access_token) {
        return "200";
    }

    public static String UpdateLocation(String username, String lat, String lon, String access_token) {
        final OkHttpClient client = new OkHttpClient();

        String url = SERVER_ADDRESS + LOCATION + username;

        RequestBody body = new FormBody.Builder()
                .add("lat", lat)
                .add("lon", lon)
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

    public static String RetrieveLocation(String username, String access_token) {
        return "200";
    }

    public static String AddFriend(String username, String access_token) {
        final OkHttpClient client = new OkHttpClient();

        String url = SERVER_ADDRESS + FRIEND_ADD + username;

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

    // TODO: Clarify origin of request_id
    public static String AcceptFriend(String request_id, String access_token) {
        final OkHttpClient client = new OkHttpClient();

        String url = SERVER_ADDRESS + FRIEND_ACCEPT + request_id;

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

    // TODO: Clarify - response?
    public static String GetFriends(String access_token) {
        final OkHttpClient client = new OkHttpClient();

        String url = SERVER_ADDRESS + FRIEND_LIST;

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
}
