package net.noconroy.itproject.application;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by James on 8/09/2017.
 */

public final class ChatHelper {
    private static final String SERVER_ADDRESS = "http://127.0.0.1:5000";
    private static final String CHANNEL = "/channel/";
    private static final String JOIN = "/join";
    private static final String LEAVE = "/leave";
    private static final String MESSAGE = "/message";

    public static String CreateChannel(String name, String access_token) {
        final OkHttpClient client = new OkHttpClient();

        String url = SERVER_ADDRESS + CHANNEL + name;
        access_token = access_token.replaceAll("^\"|\"$", "");

        RequestBody body = new FormBody.Builder()
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

        String url = SERVER_ADDRESS + CHANNEL + name;
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

        String url = SERVER_ADDRESS + CHANNEL + name + JOIN;
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

        String url = SERVER_ADDRESS + CHANNEL + name + LEAVE;
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

        String url = SERVER_ADDRESS + CHANNEL + name + MESSAGE;
        access_token = access_token.replaceAll("^\"|\"$", "");

        RequestBody body = new FormBody.Builder()
                .add("channel", name)
                .add("message_type", "text/plain")
                .add("message", message)
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

    public static String PollChannels(String access_token) {
        return "-1";
    }
}
