package net.noconroy.itproject.application;

import net.noconroy.itproject.application.callbacks.AuthenticationCallback;
import net.noconroy.itproject.application.callbacks.NetworkCallback;
import net.noconroy.itproject.application.models.Profile;

/**
 * Created by matt on 10/10/17.
 */

public class DataStorage {
    private String access_token = null;
    public Profile me = null;

    private static DataStorage instance;

    private static DataStorage newInstance() {
        DataStorage i = new DataStorage();
        return i;
    }

    public void setAccessToken(String token) {
        if(!token.equals(access_token)) {
            access_token = token;
            load();
        }
    }

    public void setAccessToken(AuthenticationCallback.AccessToken token) {
        if(!token.access_token.equals(access_token)) {
            access_token = token.access_token;
            load();
        }
    }

    public String getAccessToken() {
        return access_token;
    }

    public static DataStorage getInstance() {
        if(instance == null)
            instance = newInstance();

        return instance;
    }

    public void load() {
        if(access_token == null)
            return;

        NetworkHelper.GetProfile(null, new NetworkCallback<Profile>(Profile.class) {
            @Override
            public void onSuccess(Profile profile) {
                me = profile;
            }

            @Override
            public void onFailure(Failure f) {

            }
        });
    }

}
