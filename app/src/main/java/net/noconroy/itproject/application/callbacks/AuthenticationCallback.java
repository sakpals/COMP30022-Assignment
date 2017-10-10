package net.noconroy.itproject.application.callbacks;

import com.google.gson.JsonObject;

import net.noconroy.itproject.application.DataStorage;

/**
 * Created by matt on 10/10/17.
 */

public abstract class AuthenticationCallback extends NetworkCallback<AuthenticationCallback.AccessToken>{
    public abstract void onAuthenticated(String access_token);
    public abstract void onFailure(Failure f);

    public class AccessToken {
        public String access_token;
    }

    public AuthenticationCallback() {
        super(AccessToken.class);
    }

    @Override
    public void onSuccess(AccessToken o) {
        DataStorage.getInstance().setAccessToken(o);
        onAuthenticated(o.access_token);
    }
}
