package net.noconroy.itproject.application;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
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
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Created by James on 8/09/2017.
 */

public final class ChatHelper {
    private static final String SERVER_SCHEME = "http";
    private static final String SERVER_HOST ="127.0.0.1";
    private static final Integer SERVER_PORT = 5000;
    private static final String ACCESS_TOKEN_NAME = "access_token";
    private static final String SERVER_ADDRESS = "http://127.0.0.1:5000";
    private static final String CHANNEL = "channel/";
    private static final String JOIN = "/join";
    private static final String LEAVE = "/leave";
    private static final String MESSAGE = "/message";
    private static final String FROM = "?from=";
    private static final String TO = "&to=";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static String CreateChannel(String name, String access_token) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        final OkHttpClient client = new OkHttpClient();

        HttpUrl url = constructURL(CHANNEL + name);

        access_token = access_token.replaceAll("^\"|\"$", "");

        JSONObject json = new JSONObject();
        try {
            json.put("persistent", true);
            json.put("access_token", access_token);
        } catch (JSONException e) {
            e.printStackTrace();
            return e.getMessage();
        }

        RequestBody body = RequestBody.create(JSON, json.toString());

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

    public static String DeleteChannel(String name, String access_token) {
        final OkHttpClient client = new OkHttpClient();

        HttpUrl url = constructURL(CHANNEL + name);

        access_token = access_token.replaceAll("^\"|\"$", "");

        RequestBody body = new FormBody.Builder()
                .add("access_token", access_token)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .delete(body)
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

    public static String SubscribeChannel(String name, String access_token) {
        final OkHttpClient client = new OkHttpClient();

        HttpUrl url = constructURL(CHANNEL + name + JOIN);

        access_token = access_token.replaceAll("^\"|\"$", "");

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

    public static String LeaveChannel(String name, String access_token) {
        final OkHttpClient client = new OkHttpClient();

        HttpUrl url = constructURL(CHANNEL + name + LEAVE);

        access_token = access_token.replaceAll("^\"|\"$", "");

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

    public static String MessageChannel(String name, String user, String message, String access_token) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String type = "text/plain; charset=utf-8";
        final OkHttpClient client = new OkHttpClient();

        HttpUrl url = constructURL(CHANNEL + name + MESSAGE,
                access_token);

        access_token = access_token.replaceAll("^\"|\"$", "");

        JSONObject message_data = new JSONObject();
        try {
            message_data.put("channel", name);
            message_data.put("message_type", type);
            message_data.put("user", user);
            message_data.put("message", message);


        } catch (JSONException e) {
            e.printStackTrace();
            return e.getMessage();
        }

        JSONObject json = new JSONObject();
        try {
            json.put("data", message_data);
            json.put("type", type);
            json.put("access_token", access_token);
        } catch (JSONException e) {
            e.printStackTrace();
            return e.getMessage();
        }

        RequestBody body = RequestBody.create(JSON, json.toString());

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

    public static String GetMessages(String channel, String from, String to, String access_token) {
        final OkHttpClient client = new OkHttpClient();

        access_token = access_token.replaceAll("^\"|\"$","");

        HttpUrl url = constructURL(CHANNEL + channel + MESSAGE,
                from, to, access_token);

        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            String res = response.body().string();

            return res;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String GetAllMessages(String channel, String access_token) {
        final OkHttpClient client = new OkHttpClient();

        access_token = access_token.replaceAll("^\"|\"$","");

        HttpUrl url = constructURL(CHANNEL + channel + MESSAGE, access_token);

        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            String res = response.body().string();

            return res;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


/*
    public static void ListenChannels(String access_token) {
        final OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(SERVER_ADDRESS)
                .build();
        WebSocket socket = client.newWebSocket(request, new WebSocketListener() {
            public void onOpen() {
                System.out.println("open");
            }
            public void onMessage() {
                System.out.println("message");
            }
            public void onFailure() {
                System.out.println("fail");
            }
            public void onClosing() {
                System.out.println("closing");
            }
            public void onClosed() {
                System.out.println("closed");
            }
        });
    }
*/

    private static HttpUrl constructURL(String segment){
        HttpUrl url = new HttpUrl.Builder()
                .scheme(SERVER_SCHEME)
                .host(SERVER_HOST)
                .port(SERVER_PORT)
                .addPathSegments(segment)
                .build();
        return url;
    }

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

    private static HttpUrl constructURL(String segment, String from, String to, String token){
        HttpUrl url = new HttpUrl.Builder()
                .scheme(SERVER_SCHEME)
                .host(SERVER_HOST)
                .port(SERVER_PORT)
                .addPathSegments(segment)
                .addQueryParameter("from", from)
                .addQueryParameter("to", to)
                .addQueryParameter(ACCESS_TOKEN_NAME, token)
                .build();
        return url;
    }
}
