package net.noconroy.itproject.application;

/**
 * Created by matt on 16/08/17.
 */
import android.content.Context;
import android.net.Uri;
import android.util.ArrayMap;

import org.matrix.androidsdk.HomeserverConnectionConfig;
import org.matrix.androidsdk.MXDataHandler;
import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.data.store.MXFileStore;
import org.matrix.androidsdk.rest.client.LoginRestClient;
import org.matrix.androidsdk.rest.model.login.Credentials;
import org.matrix.androidsdk.rest.model.login.RegistrationParams;
import org.matrix.androidsdk.util.Log;

import java.util.HashMap;
import java.util.Map;

public class Matrix {
    private final static String LOG_TAG = "Matrix";
    private static MXSession mxSession = null;
    private static State state = State.LOGGED_OUT;
    private static Uri uri = Uri.parse("https://itproject.noconroy.net");
    private static Context context = null;
    private static Matrix instance;
    private static HomeserverStorage hsStorage;
    private static HomeserverConnectionConfig hsConfig;

    private enum State {
        LOGGED_OUT,
        LOGGING_IN,
        LOGGED_IN,
    }

    private Matrix(Context appContext) {
        context = appContext.getApplicationContext();
        hsStorage = new HomeserverStorage(context);
        hsConfig = hsStorage.getConfig();
        instance = this;
    }

    public synchronized static Matrix getInstance(Context appContext) {
        if ((instance == null) && (appContext != null)) {
            instance = new Matrix(appContext);
        }
        return instance;
    }

    public boolean isLoggingIn() {
        return state == State.LOGGING_IN;
    }

    public void logout(final Callback<Void> cb) {
        MXSession _s = getSession();
        if(_s == null)
            return;

        _s.logout(context, new Callback<Void>(){
            @Override
            public void onGood(Void _) {
                state = State.LOGGED_OUT;
                hsStorage.clearConfig();
                hsConfig = null;
                mxSession = null;
                Log.d(LOG_TAG, "## logout() : success");
                if(cb != null)
                    cb.onGood(_);
            }

            @Override
            public void onBad(Exception e) {
                Log.e(LOG_TAG, "## logout() : error: " + e.getMessage());
                if(cb != null)
                    cb.onBad(e);
            }
        });
    }

    public void login(String username, String password, final Callback<Credentials> cb) {
        LoginRestClient rc = new LoginRestClient(new HomeserverConnectionConfig(uri));
        state = State.LOGGING_IN;
        rc.loginWithUser(username, password, new Callback<Credentials>() {
            @Override
            public void onGood(Credentials credentials) {
                state = State.LOGGED_IN;
                hsConfig = new HomeserverConnectionConfig(uri, credentials);
                hsStorage.setConfig(hsConfig);
                if(cb != null)
                    cb.onGood(credentials);
            }

            @Override
            public void onBad(Exception e) {
                state = State.LOGGED_OUT;
                if(cb != null)
                    cb.onBad(e);
            }
        });
    }

    public void register(String username, String password, final Callback<Credentials> cb) {
        LoginRestClient rc = new LoginRestClient(new HomeserverConnectionConfig(uri));
        state = State.LOGGING_IN;
        RegistrationParams rp = new RegistrationParams();
        rp.username = username;
        rp.password = password;
        rp.auth = new HashMap<String, Object>();
        rp.auth.put("type", LoginRestClient.LOGIN_FLOW_TYPE_DUMMY);

        rc.register(rp, new Callback<Credentials>() {
            @Override
            public void onGood(Credentials credentials) {
                state = State.LOGGED_IN;
                hsConfig = new HomeserverConnectionConfig(uri, credentials);
                hsStorage.setConfig(hsConfig);
                if(cb != null)
                    cb.onGood(credentials);
            }

            @Override
            public void onBad(Exception e) {
                state = State.LOGGED_OUT;
                if(cb != null)
                    cb.onBad(e);
            }
        });

    }

    public MXSession getSession() {

        if(hsConfig == null)
            return null;

        if(mxSession == null) {

            HomeserverConnectionConfig config = hsStorage.getConfig();

            MXFileStore fs = new MXFileStore(config, context);

            MXDataHandler dh = new MXDataHandler(fs, config.getCredentials(), new MXDataHandler.InvalidTokenListener() {
                @Override
                public void onTokenCorrupted() {
                    Log.e(LOG_TAG, "## getSession() : onTokenCorrupted");
                    state = State.LOGGED_OUT;
                }
            });

            mxSession = new MXSession(config, dh, context);

            Log.d(LOG_TAG, "Logged in as " + mxSession.getMyUserId());

        }

        return mxSession;
    }
}
