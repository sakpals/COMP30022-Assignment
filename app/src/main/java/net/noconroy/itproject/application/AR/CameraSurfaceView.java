package net.noconroy.itproject.application.AR;

/**
 * Created by Mattias on 26/09/2017.
 */

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/** A basic Camera preview class */
@SuppressWarnings( "deprecation")
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "testcameriew";
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public boolean cameraOpen = false;
    public boolean cameraInPreview = false;


    private Camera.Size mPreviewSize;
    private List<Camera.Size> mSupportedPreviewSizes;
    private Context mContext;
    private SurfaceView mSurfaceView;

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        openCamera();

        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHolder.setKeepScreenOn(true);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            cameraOpen = false;
            cameraInPreview = false;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        if (mCamera != null)
        {
            previewCamera();
        }
        else {
            openCamera();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        try
        {
            if (mCamera != null)
            {
                mCamera.setPreviewDisplay(holder);
            }
            else {
                openCamera();
            }
        }
        catch (IOException exception)
        {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
    }

    public void openCamera() {
        try {
            releaseCameraAndPreview();
            mCamera = Camera.open();
            setCamera(mCamera);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCamera(Camera camera)
    {
        mCamera = camera;
        try {
            if (mCamera != null) {
                mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
                mCamera.setPreviewDisplay(mHolder);
            }
        } catch (Exception e) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", e);
        }

        requestLayout();
    }

    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mHolder.removeCallback(this);
            mCamera.release();
            mCamera = null;
            cameraInPreview = false;
            cameraOpen = false;
        }
    }

    public void restartCamera() {
        if (!cameraInPreview && !cameraOpen) {
            openCamera();
            previewCamera();
        }
    }

    private void previewCamera() {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        if (mCamera != null)
        {
            cameraOpen = true;
            Camera.Parameters parameters = mCamera.getParameters();

            int width = 320;      // base width
            int height = 240;     // base height

            for (Camera.Size sizes : mSupportedPreviewSizes) {
                width = sizes.width;
                height = sizes.height;
                break;
            }

            // NOTE: If you want this to work on e  mulators then restrict the
            // width and height to 320 and 240 respectively

            parameters.setPreviewSize(width, height);

            mCamera.setParameters(parameters);

            try {
                mCamera.startPreview();
                cameraInPreview = true;
            } catch (Exception e) {
                parameters.setPreviewSize(320, 240);
                mCamera.setParameters(parameters);
                mCamera.startPreview();
                cameraInPreview = true;
            }
        } else {
            restartCamera();
        }
    }
}
