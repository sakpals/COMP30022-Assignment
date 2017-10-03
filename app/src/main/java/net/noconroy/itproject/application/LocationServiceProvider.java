package net.noconroy.itproject.application;

import android.location.Location;

import com.google.android.gms.location.LocationRequest;

import java.util.Date;

/**
 * Created by Mattias on 23/09/2017.
 */

// Provides a global access point to our current location
public class LocationServiceProvider {

    private static LocationService locationService = null;

    public static LocationService createLocationService(LocationService mLocationService) {
        locationService = mLocationService;
        return null;
    }

    public static Location retrieveUserLocation() {
        if (locationService == null) {
            return null;
        }
        return locationService.getLocation();
    }

    /**
     * @param timer - represents the time in minutes we want to extend updating our location
     *
     * Extends the length of location updates -- should be used for when the user
     * wants to send their location to others and then closes his app. Returns true if successful,
     * otherwise returns false.
     */
    public static boolean extendLocationUpdates(float timer) {
        if (locationService != null) {

            // convert minutes to seconds
            locationService.extendedLocationTimer = timer * 60;
            locationService.clickedLastUpdateTime = new Date();
            return true;
        }
        return false;
    }

    /**
     * @param interval
     * @param fastestInterval
     * @param expirationDuration
     * @param priority
     *
     * Currently have not implemented this side of code within LocationService. Trying to think
     * of an appropriate plan of attack for dealing with the multiple issues that arise when
     * implementing this -- stems from the lack of callback option after this update expires.
     */
    public static void changeLocationUpdateInterval(long interval, long fastestInterval,
                                                    long expirationDuration, int priority) {
        LocationRequest lRequest = new LocationRequest();
        lRequest.setInterval(interval);
        lRequest.setFastestInterval(fastestInterval);
        lRequest.setExpirationDuration(expirationDuration);
        lRequest.setPriority(priority);
    }
}
