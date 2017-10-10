package net.noconroy.itproject.application;

import com.google.gson.Gson;

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
    private static final String SERVER_HOST ="10.0.2.2";//"127.0.0.1";
    private static final Integer SERVER_PORT = 5000;
   // private static final String SERVER_ADDRESS = "http://127.0.0.1:5000";
    private static final String CHANNEL = "channel/";
    private static final String JOIN = "/join";
    private static final String LEAVE = "/leave";
    private static final String MESSAGE = "/message";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static String CreateChannel(String name, String access_token, Boolean persistent) {
        final OkHttpClient client = new OkHttpClient();

        HttpUrl url = new HttpUrl.Builder()
                .scheme(SERVER_SCHEME)
                .host(SERVER_HOST)
                .port(SERVER_PORT)
                .addPathSegments(CHANNEL+name)
                .build();
        //String url = SERVER_ADDRESS + CHANNEL + name;
        access_token = access_token.replaceAll("^\"|\"$", "");

        RequestBody body = new FormBody.Builder()
                .add("persistent", persistent.toString())
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

    public static String DeleteChannel(String name, String access_token) {
        final OkHttpClient client = new OkHttpClient();

        HttpUrl url = new HttpUrl.Builder()
                .scheme(SERVER_SCHEME)
                .host(SERVER_HOST)
                .port(SERVER_PORT)
                .addPathSegments(CHANNEL+name)
                .build();
        //String url = SERVER_ADDRESS + CHANNEL + name;
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

        HttpUrl url = new HttpUrl.Builder()
                .scheme(SERVER_SCHEME)
                .host(SERVER_HOST)
                .port(SERVER_PORT)
                .addPathSegments(CHANNEL+name+JOIN)
                .build();
       // String url = SERVER_ADDRESS + CHANNEL + name + JOIN;
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

        HttpUrl url = new HttpUrl.Builder()
                .scheme(SERVER_SCHEME)
                .host(SERVER_HOST)
                .port(SERVER_PORT)
                .addPathSegments(CHANNEL+name+LEAVE)
                .build();
        //String url = SERVER_ADDRESS + CHANNEL + name + LEAVE;
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

    public static String MessageChannel(String name, String message, String access_token) {
        final OkHttpClient client = new OkHttpClient();

        HttpUrl url = new HttpUrl.Builder()
                .scheme(SERVER_SCHEME)
                .host(SERVER_HOST)
                .port(SERVER_PORT)
                .addPathSegments(CHANNEL+name+MESSAGE)
                .build();
        //String url = SERVER_ADDRESS + CHANNEL + name + MESSAGE;
        access_token = access_token.replaceAll("^\"|\"$", "");

        JSONObject jsonMessage = new JSONObject();
        try {
            jsonMessage.put("channel", name);
            jsonMessage.put("message_type", "text/plain");
            jsonMessage.put("message", message);

        } catch (JSONException e) {
            e.printStackTrace();
            return e.getMessage();
        }

        String msgString = jsonMessage.toString();

        RequestBody body = new FormBody.Builder()
                .add("data", msgString)
                .add("type", "text/plain")
                .add("access_token", access_token)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            System.out.println(Integer.toString(response.code()));
            return Integer.toString(response.code());
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public static void ListenChannels(String access_token) {
        final OkHttpClient client = new OkHttpClient();

        HttpUrl url = new HttpUrl.Builder()
                .scheme(SERVER_SCHEME)
                .host(SERVER_HOST)
                .port(SERVER_PORT)
                .build();

        Request request = new Request.Builder()
                .url(url)
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
}
