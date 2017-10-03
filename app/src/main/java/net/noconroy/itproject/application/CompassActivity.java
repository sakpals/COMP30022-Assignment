package net.noconroy.itproject.application;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Mattias on 5/09/2017.
 */

// NOTE: When running the CompassActivity, have to make sure that the device is not in
// power-saver mode, otherwise there is an extreme lack of response from the app.

// NOTE: Cannot set orientation within the activity, as that causes the camera.open()
// in Android 4.3 to fail
@SuppressWarnings("deprecation")
public class CompassActivity extends AppCompatActivity {

    private static final String TAG = CompassActivity.class.getSimpleName();

    private CompassSurfaceView compassSurfaceView;
    private CompassManager compassManager;



    /***************************************************************************************/
    /*****************************  Class Methods  *****************************************/
    /***************************************************************************************/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        // This messes with android version 4.3 (jelly beans)
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        // Set our surface view - can't do this in constructor
        setCompassSurfaceView();
        startCompassManagerThread();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!compassManager.getRunning()) {
            startCompassManagerThread();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onPause() {
        super.onPause();

        if (compassManager.getRunning()) {
            compassManager.stopThread();
        }
    }

    private void startCompassManagerThread() {
        compassManager = new CompassManager(this, compassSurfaceView, compassSurfaceView.getSurfaceHolder());
        compassManager.initialiseThread();
        compassManager.start();
    }

    private void setCompassSurfaceView() {
        compassSurfaceView = (CompassSurfaceView)findViewById(R.id.compassSurfaceView);
    }
}
