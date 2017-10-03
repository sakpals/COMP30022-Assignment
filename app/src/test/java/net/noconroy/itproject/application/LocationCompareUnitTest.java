package com.example.mattias.devicelocation;

import android.location.Location;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
//import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by Mattias on 4/09/2017.
 */

//@RunWith(MockitoJUnitRunner.class)
public class LocationCompareUnitTest {

    LocationCompare compare;
    Location source;
    Location dest;

    @Before
    public void setUp() {
        compare = new LocationCompare();
        source = mock(Location.class);
        when(source.getLatitude()).thenReturn(19.217803);
        when(source.getLongitude()).thenReturn(24.433594);
    }

    @Test
    public void getLocationCompassBearing_SimpleReturnNorthEast_isTrue() {

        dest = mock(Location.class);
        when(dest.getLatitude()).thenReturn(24.322071);
        when(dest.getLongitude()).thenReturn(28.564453);

        assertEquals(compare.getLocationCompassBearing(source, dest), compare.NORTH_EAST_STRING);
    }

    @Test
    public void getLocationCompassBearing_SimpleReturnNorthWest_isTrue() {
        dest = mock(Location.class);
        when(dest.getLatitude()).thenReturn(25.279471);
        when(dest.getLongitude()).thenReturn(20.654297);

        assertEquals(compare.getLocationCompassBearing(source, dest), compare.NORTH_WEST_STRING);
    }

    @Test
    public void getLocationCompassBearing_SimpleReturnSouthEast_isTrue() {
        dest = mock(Location.class);
        when(dest.getLatitude()).thenReturn(15.744676);
        when(dest.getLongitude()).thenReturn(29.091797);

        assertEquals(compare.getLocationCompassBearing(source, dest), compare.SOUTH_EAST_STRING);
    }

    @Test
    public void getLocationCompassBearing_SimpleReturnSouthWest_isTrue() {
        dest = mock(Location.class);
        when(dest.getLatitude()).thenReturn(16.420278);
        when(dest.getLongitude()).thenReturn(19.072266);

        assertEquals(compare.getLocationCompassBearing(source, dest), compare.SOUTH_WEST_STRING);
    }

    @Test
    public void getLocationCompassBearing_SimpleReturnNull() {
        assertEquals(compare.getLocationCompassBearing(null, null), null);
    }

    @Test
    public void getLocationDistance_SimpleReturn_SomeValue() {
        dest = mock(Location.class);
        when(dest.getLatitude()).thenReturn(16.420278);
        when(dest.getLongitude()).thenReturn(19.072266);

        assertEquals(compare.getLocationDistance(source, dest), (float)10, 1);
    }
}
