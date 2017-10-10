package net.noconroy.itproject.application.callbacks;

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

    public NetworkCallback(Class<T> _type) {
        type = _type;
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
        Failure f = new Failure();
        f.code = -1;
        f.msg = "Network error";
        onFailure(f);
        done();
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if(response.isSuccessful()) {
            Gson gson = new Gson();
            if(type != null) {
                T obj = gson.fromJson(response.body().string(), type);
                onSuccess(obj);
            } else {
                onSuccess(null);
            }
        } else {
            Gson gson = new Gson();
            Failure f = gson.fromJson(response.body().string(), Failure.class);
            f.code = response.code();
            onFailure(f);
        }
        done();
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
