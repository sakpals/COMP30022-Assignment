package net.noconroy.itproject.application.callbacks;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by matt on 10/10/17.
 *
 * Generic
 */

public abstract class NetworkCallback<T> implements Callback {
    public abstract void onSuccess(T object);
    public abstract void onFailure(Failure f);

    Class<T> type;
    Activity ctx;

    public NetworkCallback(Class<T> _type, Activity _ctx) {
        type = _type;
        ctx = _ctx;
    }

    private class SuccessRunnable implements Runnable {
        Call call;
        Response response;
        SuccessRunnable(Call _call, Response _response) {
            call = _call;
            response = _response;
        }

        @Override
        public void run(){
            String body = "";
            try {
                body = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(response.isSuccessful()) {
                Gson gson = new Gson();
                if(type != null) {
                    T obj = gson.fromJson(body, type);
                    onSuccess(obj);
                } else {
                    onSuccess(null);
                }
            } else {
                Gson gson = new Gson();
                Failure f = gson.fromJson(body, Failure.class);
                f.code = response.code();
                onFailure(f);
            }
            done();
        }
    }

    private class FailureRunnable implements Runnable {

        Call call;
        IOException e;

        FailureRunnable(Call _call, IOException _e) {
            call = _call;
            e = _e;
        }

        @Override
        public void run() {
            Failure f = new Failure();
            f.code = -1;
            f.msg = "Network error";
            onFailure(f);
            done();
        }
    }

    public class Failure {
        public int code;
        public String msg;

        public String toString() {
            return "ERR: " + code + ", MSG: " + msg;
        }
    }

    @Override
    public void onFailure(Call call, IOException e) {
        FailureRunnable r = new FailureRunnable(call, e);

        if(ctx == null)
            r.run();
        else
            ctx.runOnUiThread(r);
    }

    @Override
    public void onResponse(Call call, Response response) {
        SuccessRunnable r = new SuccessRunnable(call, response);
        if(ctx == null)
            r.run();
        else
            ctx.runOnUiThread(r);
    }

    /* Poor mans thread wait below
     * TODO: Use proper thread signaling routines
     */
    private boolean finished = false;

    private void done() {
        finished = true;
    }

    public void waitDone() {
        int counter = 50;
        while(!finished && counter-- > 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
