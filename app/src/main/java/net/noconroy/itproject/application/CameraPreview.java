package net.noconroy.itproject.application;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * Created by sampadasakpal on 15/9/17.
 */
@SuppressWarnings( "deprecation")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder holder = null;
    private Camera camera;
    private Camera.Parameters parameters;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.camera = camera;

        holder = getHolder();
        holder.addCallback(this);

    }

    /* creates surface telling camera where to draw preview */
    public void surfaceCreated(SurfaceHolder holder) {

        try {

            camera.setPreviewDisplay(holder);
            camera.startPreview();
        }
        catch (IOException e){
            Log.d("ERROR", "Error setting camera preview : " + e.getMessage());
        }
    }


    public void surfaceDestroyed(SurfaceHolder holder) {
        // deferred to CameraFragment, since the app will have more screens
    }

    /* adjusts the preview according to any rotations */
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if(holder.getSurface() == null) {
            return;
        }

        try {
            // stop preview first!
            camera.stopPreview();

        }
        catch (Exception e) {
            Log.d("ERROR", "Error stopping camera preview: " + e.getMessage());
        }
        // gets and sets the correct preview size for device
        parameters = camera.getParameters();
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size previewSize = previewSizes.get(0);
        for(int i=0; i < previewSizes.size(); i ++) {
            if(previewSizes.get(i).width <= w && previewSizes.get(i).height <= h) {
                previewSize = previewSizes.get(i);
                break;
            }
        }
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        camera.setParameters(parameters);
        // makes any changes here
        // set display orientation so that there is no rotation (i.e upright)
        camera.setDisplayOrientation(0);


        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        }
        catch (Exception e) {
            Log.d("ERROR", "Camera error on surface changed: "+ e.getMessage());
        }
    }

}






    /*
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null)
        {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }


    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int width, int height)
    {
        Camera.Size optimalSize = null;

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) height / width;

        // Try to find a size match which suits the whole screen minus the menu on the left.
        for (Camera.Size size : sizes)
        {
            if (size.height != width) continue;
            double ratio = (double) size.width / size.height;
            if (ratio <= targetRatio + ASPECT_TOLERANCE && ratio >= targetRatio - ASPECT_TOLERANCE)
            {
                optimalSize = size;
            }
        }

        // If we cannot find the one that matches the aspect ratio, ignore the requirement.
        if (optimalSize == null)
        {
            // TODO : Backup in case we don't get a size.
        }

        return optimalSize;
    }
    */