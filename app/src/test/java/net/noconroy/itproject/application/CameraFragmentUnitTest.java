package com.example.mattias.devicelocation;


import org.junit.Before;
import org.junit.Test;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
/**
 * Created by sampadasakpal on 18/9/17.
 */

@SuppressWarnings("deprecation")
@RunWith(PowerMockRunner.class)
@PrepareForTest({Camera.class, CameraFragment.class})
public class CameraFragmentUnitTest {

    public Camera camera = null;

    /* Mocks for testCheckCameraHardware() */
    public Context context = Mockito.mock(Context.class);
    public Context context1 = Mockito.mock(Context.class);

    public  PackageManager pkg1 = Mockito.mock(PackageManager.class);
    public PackageManager pkg = Mockito.mock(PackageManager.class);

    public CameraFragment activity = new CameraFragment();

    /* Initialises the output of some methods */
    @Before
    public void setUp() {

        /* for testGetNumberOfCameras() */
        PowerMockito.mockStatic(Camera.class);
        Mockito.when(Camera.getNumberOfCameras()).thenReturn(1);


        /* for testFindBackFacingCamera() */
        PowerMockito.mockStatic(CameraFragment.class);
        Mockito.when(CameraFragment.findBackFacingCamera()).thenReturn(0);

        /* for testGetCameraInstance() */
        Mockito.when(CameraFragment.getCameraInstance()).thenReturn(null);

        /* for testCheckCameraHardware() */
        Mockito.when(context.getPackageManager()).thenReturn(pkg);
        Mockito.when(context1.getPackageManager()).thenReturn(pkg1);

        Mockito.when(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)).thenReturn(true);
        Mockito.when(context1.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)).thenReturn(false);


        Mockito.when(activity.checkCameraHardware(context)).thenReturn(true);
        Mockito.when(activity.checkCameraHardware(context1)).thenReturn(false);




    }

    /* Test to check number of cameras using powermock (for static methods) */
    @Test
    public void testGetNumberOfCameras() {
        assertEquals(Camera.getNumberOfCameras(), 1);
        PowerMockito.verifyStatic(); // checks to make sure mock static method has been called
    }

    /* Test to check there is a camera available*/
    @Test
    public void testCheckCameraHardware() {
        // case when there is camera hardware
        assertEquals(activity.checkCameraHardware(context), true);

        // case when there is no camera hardware
        assertEquals(activity.checkCameraHardware(context1), false);
    }


    /* Test for checking that there is back facing camera with id 0 */
    @Test
    public void testFindBackFacingCamera() {
        int backCameraId = CameraFragment.findBackFacingCamera();
        assertEquals(backCameraId, 0);
    }


    /* Test for case when there is no camera or it fails to open */
    @Test
    public void testGetCameraInstance() {
        camera = CameraFragment.getCameraInstance();
        assertEquals(camera,  null);
    }


}
