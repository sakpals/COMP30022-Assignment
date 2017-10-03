package net.noconroy.itproject.application;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Mattias on 24/09/2017.
 *
 * Resourced from: https://stackoverflow.com/questions/3667022/checking-
 * if-an-android-application-is-running-in-the-background/5862048#5862048
 */

// Note: ActivtyLifecycleCallbacks only works with Android 4.0 and above
public class AppLifecycleHandler implements Application.ActivityLifecycleCallbacks {

    private static MainActivity mainActivity = null;

    private static int resumed;
    private static int paused;
    private static int started;
    private static int stopped;

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {}

    @Override
    public void onActivityStarted(Activity activity) {
        ++started;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        ++resumed;
        mainActivity.doBindLocationService();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        ++paused;
        Log.d("AppLifecycleHandler", "application in foreground: " + (resumed > paused));
    }

    @Override
    public void onActivityStopped(Activity activity) {
        ++stopped;
        Log.d("AppLifecycleHandler", "application is visible: " + (started > stopped));
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}

    public static boolean setMainActivity(MainActivity main) {
        if (mainActivity == null) {
            mainActivity = main;
            return true;
        }
        return false;
    }

    public static boolean isApplicationVisible() {
        Log.d("AppLifecycleHandler", "Application visible: " + (started > stopped));
        return started > stopped;
    }

    public static boolean isApplicationInForeground() {
        Log.d("AppLifecycleHandler", "Application in foreground: " + (resumed > paused));
        return resumed > paused;
    }
}
