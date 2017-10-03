package net.noconroy.itproject.application;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import net.noconroy.itproject.application.AR.CameraPreview;

/**
 * Created by sampadasakpal on 15/9/17.
 */
@SuppressWarnings( "deprecation")
public class CameraFragment extends Fragment {

    private Camera camera = null;
    private CameraPreview preview = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.camera_preview_fragment, container, false);
    }


    @Override
    public void onStart() {
        super.onStart();

        // if there is no camera available for AR view
        if(!checkCameraHardware(getActivity().getApplicationContext())) {
            Toast t = Toast.makeText(getActivity().getApplicationContext(), "No camera available", Toast.LENGTH_SHORT);
            t.show();
        }

        // creates camera object
        try {
            camera = getCameraInstance();

        }
        catch (Exception e) {
            Log.d("ERROR", "Failed to get camera instance: " + e.getMessage());
        }
        if(camera != null) {
            preview = new CameraPreview(getActivity(), camera);
            FrameLayout ARView = getView().findViewById(R.id.camera_preview_fragment);
            ARView.addView(preview);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        preview = null;
        releaseCamera();
    }


    @Override
    public void onPause() {
        super.onPause();
        preview = null;
        releaseCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(camera == null) {
            getCameraInstance();
        }
        if(preview == null) {
            preview = new CameraPreview(getActivity(), camera);
            FrameLayout ARView =  getView().findViewById(R.id.camera_preview_fragment);
            ARView.removeAllViews();
            ARView.addView(preview);
        }

    }

    /* if there is a camera object, release it*/
    private void releaseCamera() {
        if(camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }


    /* checks to see if device has a camera */
    public boolean checkCameraHardware(Context context) {
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        }
        else {
            return false;
        }
    }

    public static int findBackFacingCamera() {
        int num = Camera.getNumberOfCameras();
        int backCameraId = 0;
        for(int i = 0; i < num; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if(info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                backCameraId = i;
                break;
            }

        }
        return backCameraId;
    }

    /* gets instance of camera object */
    public static Camera getCameraInstance() {
        int backCameraId;
        Camera cam = null;
        try {
            backCameraId = findBackFacingCamera();// COMMENTED OUT FOR THE SAKE OF TESTING
            cam = Camera.open(backCameraId);
        }
        catch (Exception e) {
            Log.d("ERROR", "Failed to get camera: "+ e.getMessage());
        }
        return cam;
    }
}
