package net.noconroy.itproject.application;

import android.app.Application;

/**
 * Created by Mattias on 24/09/2017.
 */

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new AppLifecycleHandler());
    }
}
