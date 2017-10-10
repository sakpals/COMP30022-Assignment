package net.noconroy.itproject.application.callbacks;

import android.app.Activity;

import com.google.gson.Gson;

import java.io.IOException;

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

    /**
     *
     * @param _type T class eg. (Friends.class)
     * @param a Activity to perform UI in. Can be null
     */
    public NetworkCallback(Class<T> _type, Activity a) {
        type = _type;
        ctx = a;
    }

    /**
     *  Runnable to run in thread. Can be run with Runnable.run()
     */
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

    /**
     *  Runnable to run in thread. Can be run with Runnable.run()
     */
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
            f.message = "Network error";
            onFailure(f);
            done();
        }
    }

    public class Failure {
        public int code;
        public String message;

        public String toString() {
            return "ERR: " + code + ", MSG: " + message;
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
