package net.noconroy.itproject.application;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import net.noconroy.itproject.application.AR.CompassActivity;
import net.noconroy.itproject.application.AR.LocationService;
import net.noconroy.itproject.application.AR.LocationServiceProvider;

import net.noconroy.itproject.application.Chat.ChatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int MY_CAMERA_REQUEST_CODE = 100;

    /***************************************************************************************/
    /********************************** Location Variables *********************************/
    /***************************************************************************************/

    private Intent mLocationServiceIntent;
    private LocationService mLocationService;
    private boolean locationServiceBound;

    /***************************************************************************************/
    /*****************************  Class Methods  *****************************************/
    /***************************************************************************************/

    private String access_token = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocationServiceIntent = null;
        mLocationService = null;
        locationServiceBound = false;

        // Bind this activity to the location service, register it and start it
        createLocationServices();

        // Set this main activity as the main activity of our app lifecycle handler
        AppLifecycleHandler.setMainActivity(this);
      
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String access_token = intent.getStringExtra(RegisterActivity.ACCESS_TOKEN_MESSAGE);
        this.access_token = access_token;
        if (access_token != null) {

            // Hide register button
            Button registerButton = (Button) findViewById(R.id.Registerbutton);
            registerButton.setVisibility(View.GONE);


            // change this to a logout button

            // Change text of Login button
            Button loginButton = (Button) findViewById(R.id.LoginButton);
            //loginButton.setText("Logged in");
            loginButton.setText("Log out");

            // Disable logging in, as you're already logged
            //loginButton.setEnabled(false);

            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    logout(view);
                }
            });

            Button friendsButton = (Button) findViewById(R.id.FriendsButton);
            friendsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    friends(view);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    // Called when the user clicks the send button
    public void GetDeviceLocation(View view) {
        Intent intent = new Intent(this, DeviceLocationActivity.class);
        startActivity(intent);

        // Using this as temporary example to extend location timer
        // extend timer for 5 minutes
        if (LocationServiceProvider.extendLocationUpdates(0.5f)) {
            Log.d(TAG, "can't extend location updates");
        }
    }

    // Called when the user clicks the compass button
    public void GetCompass(View view) {
        Intent intent = new Intent(this, CompassActivity.class);
        startActivity(intent);
    }

    public void logout(View view) {
        Intent intent = new Intent(this, LogoutActivity.class);
        intent.putExtra(RegisterActivity.ACCESS_TOKEN_MESSAGE, access_token);
        startActivity(intent);
    }
    public void register(View view){
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void login(View view){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void camera(View view){
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    public void friends(View view) {
        Intent intent = new Intent(MainActivity.this, Friends.class);
        intent.putExtra(RegisterActivity.ACCESS_TOKEN_MESSAGE, access_token);
        startActivity(intent);
    }


    public void startChat(View view) {
        Intent intent = new Intent(this, ChatActivity.class);

        // Change this in order to add the users id and name from server
        Bundle userClickedOn = new Bundle();
        userClickedOn.putString("id", "1");         // replace with proper id
        userClickedOn.putString("name", "bob");     // replace with proper name etc.
        intent.putExtras(userClickedOn);

        startActivity(intent);


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
                Log.i(TAG, "User interaction was cancelled.");
            }

            // Permission was granted
            else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mLocationService.mRequestingLocationUpdates) {
                    Log.i(TAG, "Location permission granted, updates requested, starting location updates");
                    mLocationService.startLocationUpdates();

                    // We now try to get permissions for the camera
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        checkCameraPermissions();
                    }
                }
            }

            // Permission denied - assume this never occurs for now
            else {
                Log.i(TAG, "Permission denied, will need to tell user that app is unable to run " +
                        "without appropriate location settings.");
            }
        }

        else if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Log.i(TAG, "User interaction was cancelled.");
            }
            else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permissions was granted");
            } else {
                Log.d(TAG, "Camera permissions wasn't granted");
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
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        break;

                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        mLocationService.mRequestingLocationUpdates = false;
                        mLocationService.updateLocation();
                        break;
                }
                break;
        }

    }
}
