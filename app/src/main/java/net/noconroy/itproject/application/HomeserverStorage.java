package net.noconroy.itproject.application;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;
import org.matrix.androidsdk.HomeserverConnectionConfig;
import org.matrix.androidsdk.util.Log;

/**
 * Created by matt on 8/21/17.
 */

public class HomeserverStorage {
    private static final String LOG_TAG = "HomeserverStorage";

    private static final String PREFERENCES_HS = "HomeserverStorage.hs";

    private static final String PREFERENCES_CONFIG = "HomeserverStorage.config";

    private final Context context;

    public HomeserverStorage(Context _c) {
        context = _c.getApplicationContext();
    }

    public HomeserverConnectionConfig getConfig() {

        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_HS, Context.MODE_PRIVATE);

        String configString = preferences.getString(PREFERENCES_CONFIG, null);

        if (configString == null) {
            return null;
        }

        try {

            JSONObject configJSON = new JSONObject(configString);

            return HomeserverConnectionConfig.fromJson(configJSON);

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to load config " + e.getMessage(), e);
            throw new RuntimeException("Failed to deserialize homeserver config");
        }
    }

    public void setConfig(HomeserverConnectionConfig config) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_HS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();


        try {

            String s = config.toJson().toString();
            editor.putString(PREFERENCES_CONFIG, s);
            editor.apply();

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to save config " + e.getMessage(), e);
            throw new RuntimeException("Failed to serialize homeserver config");
        }
    }

    public void clearConfig() {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_HS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PREFERENCES_CONFIG);
        editor.apply();
    }
}
