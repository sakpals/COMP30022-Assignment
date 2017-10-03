package net.noconroy.itproject.application;

import android.location.Location;

/**
 * Created by Mattias on 4/09/2017.
 */

// Should make this Location class static - no point making it an object

public class LocationCompare {

    private static final int NORTH = 0;
    private static final int EAST = 90;
    private static final int SOUTH = 180;
    private static final int WEST = 270;

    public static final String EAST_STRING = "EAST";
    public static final String WEST_STRING = "WEST";
    public static final String NORTH_STRING = "NORTH";
    public static final String SOUTH_STRING = "SOUTH";
    public static final String NORTH_EAST_STRING = "NORTH-EAST";
    public static final String NORTH_WEST_STRING = "NORTH-WEST";
    public static final String SOUTH_EAST_STRING = "SOUTH-EAST";
    public static final String SOUTH_WEST_STRING = "SOUTH-WEST";

    public LocationCompare() {
        ;
    }

    // Returns the bearing of firstDevice in comparison to secondDevice
    // If ERROR: then return null
    public String getLocationCompassBearing(Location firstDevice, Location secondDevice) {
        if (firstDevice != null && secondDevice != null) {
            float bearing = bearing(firstDevice.getLatitude(), firstDevice.getLongitude(),
                    secondDevice.getLatitude(), secondDevice.getLongitude());

            return identifyLocationBearingToAnAngle(bearing);
        }
        return null;
    }

    // Returns the bearing angle between device 1 and device 2
    // (0, 180) (East represents 90)
    // (0, -180) (West represents -90)
    // If ERROR: return -1
    public float getLocationCompassAngle(Location firstDevice, Location secondDevice) {
        if (firstDevice != null && secondDevice != null) {
            return firstDevice.bearingTo(secondDevice);
        }
        return -1;
    }

    // Returns a String representing the compass bearing from device 1 to device 2
    // If ERROR: then return null;
    private String identifyLocationBearingToAnAngle(float angle) {

        // Round angle to a whole integer - not going to make a huge difference in regards to bearing
        // We use angleInt as a simplified way to identfiy N,E,W,S
        int angleInt = Math.round(angle);

        if (angleInt == NORTH) return NORTH_STRING;
        if (angleInt == SOUTH) return SOUTH_STRING;
        if (angleInt == EAST) return EAST_STRING;
        if (angleInt == WEST) return WEST_STRING;
        if (angleInt > NORTH && angleInt < EAST) return NORTH_EAST_STRING;
        if (angleInt > EAST && angleInt < SOUTH) return SOUTH_EAST_STRING;
        if (angleInt > SOUTH && angleInt < WEST) return SOUTH_WEST_STRING;
        if (angleInt > WEST && angleInt < WEST + EAST) return NORTH_WEST_STRING;

        // ERROR
        return null;
    }

    // Return distance between devices
    // Similarly this is also returning 0.0
    // If ERROR: then return -1
    public float getLocationDistance(Location firstDevice, Location secondDevice) {
        if (firstDevice != null && secondDevice != null) {
            return distance(firstDevice.getLatitude(), firstDevice.getLongitude(),
                    secondDevice.getLatitude(), secondDevice.getLongitude());

        }
        return -1;
    }


    // Use a basic function to calculate bearing as testing via Android when using Mock Locations
    // is time-consuming and involves Mock Locations Injections
    public static float bearing(double lat1, double long1, double lat2, double long2) {
        double longitude1 = long1;
        double longitude2 = long2;
        double latitude1 = Math.toRadians(lat1);
        double latitude2 = Math.toRadians(lat2);
        double longDiff= Math.toRadians(longitude2-longitude1);
        double y= Math.sin(longDiff)*Math.cos(latitude2);
        double x=Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);

        return (float)(Math.toDegrees(Math.atan2(y, x))+360)%360;
    }

    // Calculates distance between 2 lat and long coordinates
    // Ignores altitude differences
    public static float distance(double lat1, double lat2, double lon1, double lon2) {
        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = 0;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return (float)Math.sqrt(distance);
    }
}
