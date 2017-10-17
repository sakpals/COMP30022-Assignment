package net.noconroy.itproject.application;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import net.noconroy.itproject.application.AR.CompassActivity;
import net.noconroy.itproject.application.AR.LocationService;
import net.noconroy.itproject.application.AR.LocationServiceProvider;
import net.noconroy.itproject.application.callbacks.EmptyCallback;

import static net.noconroy.itproject.application.HomeActivity.AT_PREFS;
import static net.noconroy.itproject.application.HomeActivity.AT_PREFS_KEY;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String ACCESS_TOKEN_INTENT_KEY = "access_token_intent_key";

    private static final int MY_CAMERA_REQUEST_CODE = 100;

    /***************************************************************************************/
    /******************************** Pop Up/Logcat Messages *******************************/
    /***************************************************************************************/

    // Pop Up/Logcat Messages
    public static final String EXTENDING_LOCATION_SERVICES_SUCCESS_MESSAGE =
            "Extending location services for 10 minutes!";
    public static final String LOCATION_SHARING_FAILURE_MESSAGE =
            "Must turn location sharing on first (use switch on top right of screen)";
    public static final String LOCATION_SHARING_DISABLED_MESSAGE =
            "Location sharing is disabled, location will not be updated";
    public static final String LOCATION_PERMISSION_GRANTED_MESSAGE=
            "Location permission granted, updates requested, starting location updates";
    public static final String LOCATION_PERMISSION_DENIED_MESSAGE=
            "Permission denied, will need to tell user that app is unable to run " +
                    "without appropriate location settings.";
    public static final String CAMERA_PERMISSION_GRANTED_MESSAGE=
            "Camera permissions was granted";
    public static final String CAMERA_PERMISSION_DENIED_MESSAGE=
            "Camera permissions wasn't granted";
    public static final String USER_INTERACTION_CANCELLED_MESSAGE =
            "User interaction was cancelled.";
    public static final String USER_LOCATION_SETING_CHANGE_SUCCESS_MESSAGE =
            "User agreed to make required location settings changes.";
    public static final String USER_LOCATION_SETING_CHANGE_FAILURE_MESSAGE =
            "User chose not to make required location settings changes.";

    /***************************************************************************************/
    /********************************** Location Variables *********************************/
    /***************************************************************************************/

    private Intent mLocationServiceIntent;
    private LocationService mLocationService;
    private boolean locationServiceBound;

    /***************************************************************************************/
    /*****************************  Class Methods  *****************************************/
    /***************************************************************************************/


    private DataStorage ds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ds = DataStorage.getInstance();
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String token = intent.getStringExtra(ACCESS_TOKEN_INTENT_KEY);
        if(token != null) ds.setAccessToken(token);

        // Set all location parameters
        mLocationServiceIntent = null;
        mLocationService = null;
        locationServiceBound = false;

        // Set switch parameters
        addOnSwitchListener();

        // Bind this activity to the location service, register it and start it
        createLocationServices();

        // Set this main activity as the main activity of our app lifecycle handler
        AppLifecycleHandler.setMainActivity(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    // Called when the user clicks the compass button
    public void GetCompass(View view) {
        Intent intent = new Intent(this, CompassActivity.class);
        startActivity(intent);
    }

    public void logout(View view) {
        final Intent intent = new Intent(this, HomeActivity.class);
        SharedPreferences settings = getSharedPreferences(AT_PREFS, 0);
        settings.edit().remove(AT_PREFS_KEY).commit();

        stopLocationService();
        // TODO only logout on successful logout;
        NetworkHelper.Logout(new EmptyCallback(null) {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(Failure f) {

            }
        });
        startActivity(intent);
        finish();
    }

    private void addOnSwitchListener() {
        Switch locationSharing = (Switch)findViewById(R.id.LocationShare);
        locationSharing.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            LocationServiceProvider.sharingLocation = true;
        }

        else {
            LocationServiceProvider.sharingLocation = false;
            Log.i(TAG, LOCATION_SHARING_DISABLED_MESSAGE);

            NetworkHelper.ResetLocation(ds.me.username, new EmptyCallback(null) {
                @Override
                public void onSuccess() {}
                @Override
                public void onFailure(Failure f) {}
            });
        }
    }

    public void extendLocationService(View view) {
        if (LocationServiceProvider.sharingLocation) {
            Log.i(TAG, EXTENDING_LOCATION_SERVICES_SUCCESS_MESSAGE);
            Toast t = Toast.makeText(getApplicationContext(),
                    EXTENDING_LOCATION_SERVICES_SUCCESS_MESSAGE, Toast.LENGTH_SHORT);
            t.show();
            LocationServiceProvider.extendLocationUpdates(10);
        }
        else{
            Toast t = Toast.makeText(getApplicationContext(),
                    LOCATION_SHARING_FAILURE_MESSAGE, Toast.LENGTH_SHORT);
            t.show();
        }
    }

    public void friends(View view) {
        Intent intent = new Intent(MainActivity.this, FriendsActivity.class);
        startActivity(intent);
    }

    public void doBindLocationService() {
        if (mLocationService != null) {
            if (!locationServiceBound && !mLocationService.mRequestingLocationUpdates) {
                createLocationServices();
            }
        }
        else {
            createLocationServices();
        }
    }

    private void createLocationServices() {
        mLocationServiceIntent = new Intent(MainActivity.this, LocationService.class);
        bindService(mLocationServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        locationServiceBound = true;
    }

    public void doUnbindLocationService() {
        if (locationServiceBound) {
            unbindService(mConnection);
            locationServiceBound = false;
        }
    }

    public void stopLocationService() {
        stopService(mLocationServiceIntent);
        mLocationService = null;
        mLocationServiceIntent = null;
    }


    /***************************************************************************************/
    /***************************** Binding a location service ******************************/
    /***************************************************************************************/


    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've binded to LocalService, cast the IBinder and get LocalService instance
            // Note this is asynchronous -- so have to start the service within this task
            mLocationService = ((LocationService.LocationBinder) service).getService();
            mLocationService.registerClient(MainActivity.this);
            startService(mLocationServiceIntent);
;
            // We also add this location service to LocationServiceProvider
            LocationServiceProvider.createLocationService(mLocationService);

            // Once we've started the service we no longer want it binded to this activity
            // so unbind it
            doUnbindLocationService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            // This is called when the connection with the service is unexpectedly disconnected --
            // that is, the process crashed. We should never typically see this occur.
            mLocationService = null;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkCameraPermissions() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }
    }

    /***************************************************************************************/
    /***************************** Location Callbacks **************************************/
    /***************************************************************************************/

    /**
     * @param requestCode
     * @param permissions
     * @param grantResults
     *
     * Callback method that is implemented by LocationService. It is neceesary to keep this callback
     * method within an activity, so that the user can be presented a dialogue box if they happen to
     * deny permission requests.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == mLocationService.REQUEST_PERMISSIONS_REQUEST_CODE) {

            // If user interaction was interrupted, the permission request is cancelled and you
            // receive empty arrays.
            if (grantResults.length <= 0) {
                Log.i(TAG, USER_INTERACTION_CANCELLED_MESSAGE);
            }

            // Permission was granted
            else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mLocationService.mRequestingLocationUpdates) {
                    Log.i(TAG, LOCATION_PERMISSION_GRANTED_MESSAGE);
                    mLocationService.startLocationUpdates();

                    // We now try to get permissions for the camera
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        checkCameraPermissions();
                    }
                }
            }

            // Permission denied - assume this never occurs for now
            else {
                Log.i(TAG, LOCATION_PERMISSION_DENIED_MESSAGE);
            }
        }

        else if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Log.i(TAG, USER_INTERACTION_CANCELLED_MESSAGE);
            }
            else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, CAMERA_PERMISSION_GRANTED_MESSAGE);
            } else {
                Log.d(TAG, CAMERA_PERMISSION_DENIED_MESSAGE);
            }
        }
    }

    /**
     * @param requestCode
     * @param resultCode
     * @param data
     *
     * Similar to above, this is a callback method for LocationServices that is implemented when the
     * user makes the relevant location setting updates.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            // Check for the integer request code originally supplied to startResolutionForResult().
            case LocationService.REQUEST_CHECK_SETTINGS:
                switch (resultCode) {

                    case Activity.RESULT_OK:
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        Log.i(TAG, USER_LOCATION_SETING_CHANGE_SUCCESS_MESSAGE);
                        break;

                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, USER_LOCATION_SETING_CHANGE_FAILURE_MESSAGE);
                        mLocationService.mRequestingLocationUpdates = false;
                        mLocationService.updateLocation();
                        break;
                }
                break;
        }

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
