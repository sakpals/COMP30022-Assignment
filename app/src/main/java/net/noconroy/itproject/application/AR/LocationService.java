package net.noconroy.itproject.application.AR;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import net.noconroy.itproject.application.AppLifecycleHandler;
import net.noconroy.itproject.application.DataStorage;
import net.noconroy.itproject.application.MainActivity;
import net.noconroy.itproject.application.NetworkHelper;
import net.noconroy.itproject.application.callbacks.EmptyCallback;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Mattias on 9/09/2017.
 */

public class LocationService extends Service {

    private static final String TAG = LocationService.class.getSimpleName();


    /***************************************************************************************/
    /*********************************** Service Binding ***********************************/
    /***************************************************************************************/

    private final Binder locationBinder = new LocationBinder();

    /**
     * Class for clients to access. For this application, we'll only have MainActivity bind
     * with LocationServices, and instead create a singleton, to allow global access to the
     * users current location.
     */
    public class LocationBinder extends Binder {
        public LocationService getService(){
            return LocationService.this;
        }
    }

    /***************************************************************************************/
    /*********************************** Constants *****************************************/
    /***************************************************************************************/

    // Used for callback binding to the given activity
    private MainActivity currentActivity;

    // Code used to request location runtime permission
    public static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // Constant used in to the location settings dialog
    public static final int REQUEST_CHECK_SETTINGS = 0x1;

    // Interval for requesting location updates
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;  // 10 seconds

    // Fastest interval for requesting active location updates
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";
    private final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";

    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;

    // Stores the types of location services the client is interested in using. Used for checking
    // settings to determine if the device has optimal location settings
    private LocationSettingsRequest mLocationSettingsRequest;

    // Tracks the current status of the location updates request. If True, then we are requesting
    // updates, otherwise if False then we aren't requesting location updates
    public Boolean mRequestingLocationUpdates;

    // Time when the location was last updated, represented as a string
    private String mLastUpdateTime;

    // Time when the location was last updated, represented as a string
    public Date clickedLastUpdateTime;

    // Time used to extend the location updating - represented in seconds
    public float extendedLocationTimer = 0;

    private DataStorage ds = null;


    /***************************************************************************************/
    /**************************** Overrideable Class Methods  ******************************/
    /***************************************************************************************/

    @Override
    public void onCreate() {
        super.onCreate();
        ds = DataStorage.getInstance();

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        // Kick off the process of building the LocationCallback, LocationRequest, and LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
    }

    @Override
    public int onStartCommand(Intent mIntent, int flags, int startId) {
        super.onStartCommand(mIntent, flags, startId);

        mRequestingLocationUpdates = true;

        if (checkPermissions()) {
            startLocationUpdates();
        }
        else if (!checkPermissions()) {
            requestPermissions();
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return locationBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }


    /***************************************************************************************/
    /**************************** Setting up Location Service  *****************************/
    /***************************************************************************************/

    // Sets up the location request
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    // Sets up the callback for receiving location events
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

                // If we currently have a timer active -- extending location updates
                if (extendedLocationTimer > 0) {
                    Date currentDate = new Date();
                    extendedLocationTimer -= ((currentDate.getTime() - clickedLastUpdateTime.getTime()) / 1000);
                    clickedLastUpdateTime = currentDate;
                    updateLocation();
                }

                else {
                    // The application is in the background and there is no timer active extending the location updates
                    if (!AppLifecycleHandler.isApplicationInForeground() && !AppLifecycleHandler.isApplicationVisible()) {
                        currentActivity.stopLocationService();
                    }
                    // The application is in the foreground -- keep updating
                    else {
                        updateLocation();
                    }
                }
            }
        };
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }


    /***************************************************************************************/
    /**************************** Start/Stop/Update GPS ************************************/
    /***************************************************************************************/


    // Requests location updates from the FusedLocationAPI
    // We don't call this unless location runtime permission has been granted
    public void startLocationUpdates() {
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest).addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.i(TAG, "All location settings are satisfied.");

                //noinspection MissingPermission
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade location settings.");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the
                            // result in onActivityResult().
                            ResolvableApiException rae = (ResolvableApiException) e;
                            rae.startResolutionForResult(currentActivity, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sie) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        String errorMessage = "Location settings are inadequate, and cannot be " +
                                "fixed here. Fix in Settings.";
                        Log.e(TAG, errorMessage);
                        Toast.makeText(currentActivity, errorMessage, Toast.LENGTH_LONG).show();
                        mRequestingLocationUpdates = false;
                }

                updateLocation();
            }
        });
    }

    /**
     * Method to be called when we want to do something with our newly updated location data
     * e.g. send that data to the server
     */
    public void updateLocation() {

        if (mCurrentLocation == null) {
            Log.i(TAG, "mCurrentLocation is currently null.");
            return;
        }

        if (!mRequestingLocationUpdates) {
            Log.i(TAG, "User is currently not requesting and location updates.");
            return;
        }

        if (ds.me != null) {
            if (LocationServiceProvider.sharingLocation) {
                Log.i(TAG, "Location sharing is enabled, location will be updated.");
                NetworkHelper.UpdateLocation(ds.me.username,
                        mCurrentLocation.getLatitude(),
                        mCurrentLocation.getLongitude(), new EmptyCallback(null){
                            @Override
                            public void onSuccess() {}
                            @Override
                            public void onFailure(Failure f) {}
                        });
            }
        }
    }



    /**
     * Stops location updates from the FusedLocationAPI
     */
    public void stopLocationUpdates() {
        Log.i(TAG, "Stopping GPS updates for user.");

        if (!mRequestingLocationUpdates) {
            Log.i(TAG, "stopLocationUpdatess: User was never requesting GPS data, no-op.");
            return;
        }

        mRequestingLocationUpdates = false;
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);

        Log.i(TAG, "Resetting users location when they close the application -- or location services");
        LocationServiceProvider.sharingLocation = false;
        NetworkHelper.ResetLocation(ds.me.username, new EmptyCallback(null) {
            @Override
            public void onSuccess() {}
            @Override
            public void onFailure(Failure f) {}
        });
    }


    /***************************************************************************************/
    /**************************** Setting up GPS Permissions *******************************/
    /***************************************************************************************/


    // Returns the current state of the location permissions
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    // Requests permission from the device
    // The result of this method will be called in MainActivity: onPermissionsResult
    private void requestPermissions() {
        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                currentActivity, Manifest.permission.ACCESS_FINE_LOCATION
        );

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        // For now we'll ignore this for development purposes, as all users will provide
        // permissions to the application
        if (shouldProvideRationale) {
            ;
        } else {
            Log.i(TAG, "Requesting permission for GPS.");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(currentActivity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);

        }
    }

    public void registerClient(MainActivity activity) {
        this.currentActivity = activity;
    }
    public Location getLocation() {return mCurrentLocation;}
}
