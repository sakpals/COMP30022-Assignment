package net.noconroy.itproject.application;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;

import java.util.ArrayList;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by Mattias on 14/09/2017.
 */

// Responsible for taking in sensor events, then identifying where to render directions
// then passes this info to CompassRender
public class CompassManager extends Thread implements SensorEventListener {

    private static final String TAG = CompassManager.class.getSimpleName();


    /***************************************************************************************/
    /*********************************** Constants *****************************************/
    /***************************************************************************************/


    private boolean running = false;
    private boolean haveGravitySensor = false;
    private boolean haveAccelSensor = false;
    private boolean haveGeoSensor = false;
    private boolean haveGyroSensor = false;

    private CompassRender compassRenderThread;
    private CompassFriend compassFriendThread;
    private ArrayList<FriendDrawing> friendDrawings;
    private HandlerThread mCompassManagerThread;
    private Handler mCompassManagerHandler;
    private SurfaceHolder compassSurfaceHolder;
    private CompassSurfaceView compassSurfaceView;

    private SensorManager mSensorManager;
    private Sensor mGravity;
    private Sensor mAccelerometer;
    private Sensor mMagneticField;
    private Sensor mGyroscope;

    private float[] geomagneticMatrix = new float[3];
    private float[] gyroscopeMatrix = new float[3];
    private float azimuth;

    // Represents the rotation from north converted into range [0..360]
    private float prevConvertedRotation;
    private float currConvertedRotation;

    // Represents the previous canvas state
    CompassDrawing previousCanvasState;

    // Screen height and width
    private DisplayMetrics lDisplayMetrics;
    private int deviceScreenHeight;
    private int deviceScreenWidth;

    // Rectangle components - drawing the compass
    private Rect canvasRect;
    private int rectLeft;
    private int rectRight;
    private int rectTop;
    private int rectHeight;
    private int rectBottom;
    private int rectWidth;

    // Represents an interval
    private int INTERVAL;

    // Represents how many degrees we want an interval to be expressed in
    private int compassDegreeInterval;

    // Every rectInterval (MAX of INTERVAL) will be equivalent to a compassDegreeInterval
    // E.g. if compassDegreeInterval = 10, and INTERVAL = 10, then every tenth of the rectangle will be 10 degrees
    private float rectIntervals;

    private void initialiseRectVariables() {
        rectLeft = 50;
        rectRight = deviceScreenWidth - 50;
        rectTop = 0;
        rectHeight = 100;
        rectBottom = rectTop + rectHeight;
        rectWidth = rectRight - rectLeft;

        INTERVAL = 10;
        compassDegreeInterval = 10;
        rectIntervals = rectWidth / INTERVAL;
    }

    // Compass cardinal directions -> between [0, 360]
    private static final int NORTH = 0;
    private static final int NORTH_EAST = 45;
    private static final int EAST = 90;
    private static final int SOUTH_EAST = 135;
    private static final int SOUTH = 180;
    private static final int SOUTH_WEST = 225;
    private static final int WEST = 270;
    private static final int NORTH_WEST = 315;
    private static final int ROTATION = 360;


    /***************************************************************************************/
    /*********************************** Class Methods *************************************/
    /***************************************************************************************/

    public CompassManager(Context mContext, CompassSurfaceView compassSurfaceView,
                          SurfaceHolder compassSurfaceHolder) {

        // Instantiate all relevant sensors for this device
        mSensorManager = (SensorManager)mContext.getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // Get the surface holder for our view - will pass it to compass render thread
        this.compassSurfaceHolder = compassSurfaceHolder;
        this.compassSurfaceView = compassSurfaceView;

        // Set the current height and width of the device
        lDisplayMetrics = mContext.getResources().getDisplayMetrics();
        deviceScreenHeight = lDisplayMetrics.heightPixels;
        deviceScreenWidth = lDisplayMetrics.widthPixels;

        // Instantiate our canvas rect - only need 1 object, so instantiate on creation
        initialiseRectVariables();
        canvasRect = createRect();
        previousCanvasState = null;

        // Instantiate our compass rendering thread
        compassRenderThread = new CompassRender(compassSurfaceHolder, compassSurfaceView);
        compassRenderThread.setRunning(true);
        compassRenderThread.start();

        // Instantiate our compass friend retriever
        friendDrawings = new ArrayList<FriendDrawing>();

        // Whenever we add to friendDrawings in CompassFriend it will also add to our
        // local object here
        compassFriendThread = new CompassFriend(friendDrawings);
        compassFriendThread.setRunning(true);
        compassFriendThread.start();
    }

    public void initialiseThread() {

        // Allow this thread to run
        setRunning(true);

        // Instantiate handler threads - necessary as they handle our sensor listener
        mCompassManagerThread = new HandlerThread("Compass Manager Thread", Thread.MAX_PRIORITY);
        mCompassManagerThread.start();
        mCompassManagerHandler = new Handler(mCompassManagerThread.getLooper());

        // Register all sensor listeners
        registerListeners();

        prevConvertedRotation = -1000;
    }

    /**
     * This method will make us render a new set of objects when the previous set of objects have
     * been successfully rendered. (Therefore instead of implementing an arbitrary FPS value,
     * we base our rendering time on the completion of the objects -- as the time to do this can
     * vary immensely e.g. 200ms ~ 600ms.
     */
    @Override
    public void run() {

        while(running) {

            // If we're no longer animating a set of objects anymore and our objectsToDraw is empty
            // -- we implement our render in this manner so as to prevent any potential of delay
            // when rendering our objects
            if (!compassRenderThread.getCurrentlyAnimating() && compassRenderThread.objectsToDraw.isEmpty()) {

                // Observations: whenever the device is stationary i.e. user is holding it still (albeit
                // slight tremors), the x value of the gyroscope will be quite minimal -- in this
                // case less than 0.1 or less than -0.1 (negative values indicate seperate orientation
                // therefore they're 2 different scenarios). We are using this observation so as
                // to limit the movement of the compass when the user holds it still, otherwise
                // due to the constant fluctuations in the gravity sensor and magnetic field sensors --
                // when holding the device still, the compass moves quite a bit, which is quite
                // undesirable. For now we will implement our solution in this manner, until
                // we can create something more effective.
                if (haveGyroSensor && (gyroscopeMatrix[0] > 0.1 || gyroscopeMatrix[0] < -0.1)) {
                    createCanvas(currConvertedRotation);
                    prevConvertedRotation = currConvertedRotation;
                }

                // Just in case the device doesn't have a gyro sensor -- though most devices that
                // have Android 4.0 should have some piece of software in their device that
                // calculates it
                else if (!haveGyroSensor) {
                    createCanvas(currConvertedRotation);
                    prevConvertedRotation = currConvertedRotation;
                }
            }
        }
    }

    public void registerListeners() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI, mCompassManagerHandler);
        mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_UI, mCompassManagerHandler);
        mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_UI, mCompassManagerHandler);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_UI, mCompassManagerHandler);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void stopThread() {
        unregisterListeners();
        compassFriendThread.setRunning(false);
        compassRenderThread.stopThread();

        // Stop the handler thread for this current thread
        mCompassManagerThread.quitSafely();
        setRunning(false);
    }

    private void unregisterListeners() {
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagneticField);
        mSensorManager.unregisterListener(this, mGravity);
        mSensorManager.unregisterListener(this, mGyroscope);
    }

    /**
     * @param event
     *
     * This is the method that will listen to new changes by our device sensors and pass that
     * through the 'event' parameter.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        final float alpha = 0.8f;

        // We place the gravity value on the x-axis so as to represent the way the
        // camera will be held during AR view
        float[] gravityMatrix = {9.81f, 0, 0};

        switch (event.sensor.getType()) {
            case Sensor.TYPE_GRAVITY:
                gravityMatrix[0] = event.values[0];
                gravityMatrix[1] = event.values[1];
                gravityMatrix[2] = event.values[2];
                // Log.d(TAG, String.valueOf(gravityMatrix[2]));
                haveGravitySensor = true;
                break;

            case Sensor.TYPE_ACCELEROMETER:
                if (haveGravitySensor) break;
                /// Isolate the force of gravity acting on the device
                gravityMatrix[0] = alpha * gravityMatrix[0] + (1 - alpha) * event.values[0];
                gravityMatrix[1] = alpha * gravityMatrix[1] + (1 - alpha) * event.values[1];
                gravityMatrix[2] = alpha * gravityMatrix[2] + (1 - alpha) * event.values[2];
                haveAccelSensor = true;
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                geomagneticMatrix[0] = event.values[0];
                geomagneticMatrix[1] = event.values[1];
                geomagneticMatrix[2] = event.values[2];
                haveGeoSensor = true;
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroscopeMatrix[0] = event.values[0];
                gyroscopeMatrix[1] = event.values[1];
                gyroscopeMatrix[2] = event.values[2];
                haveGyroSensor = true;
                break;
            default:
                break;
        }

        if ((haveGravitySensor || haveAccelSensor) && haveGeoSensor) {
            float R[] = new float[9];
            float I[] = new float[9];
            float RM[] = new float[9];

            if (SensorManager.getRotationMatrix(R, I, gravityMatrix, geomagneticMatrix)) {

                // NOTE: If we tilt the device to far forwards, the coordinate axis of the device will flip
                // resulting in N becoming S. This is only an issue, if the user tilts it too far forward,
                // which for now we'll ignore.
                // SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Z, RM);
                SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Z, RM);

                float orientation[] = new float[3];
                SensorManager.getOrientation(RM, orientation);

                azimuth = orientation[0];
                float rotationFromNorth = azimuth * 360 / (2 * 3.14159f);
                currConvertedRotation = convertRotation(Float.valueOf(rotationFromNorth));
            }
        }
    }



    /***************************************************************************************/
    /**************************** Canvas Rendering Classes *********************************/
    /***************************************************************************************/



    /**
     * @param rotationFromNorth
     *
     * This method will generate a simple canvasDrawing object, in which we will add
     * our cardinal drawing objects and friend drawing objects -- that will then be rendered
     * onto the UI
     */
    private void createCanvas(float rotationFromNorth) {

        CompassDrawing canvasDrawing = new CompassDrawing(canvasRect);
        getCardinalObjectsToDraw(canvasDrawing, rotationFromNorth);

        if (previousCanvasState == null) {
            compassRenderThread.objectsToDraw.add(canvasDrawing);
        } else{
            reassembleCanvasState(canvasDrawing);
            compassRenderThread.objectsToDraw.add(canvasDrawing);
        }

        previousCanvasState = canvasDrawing;
    }

    /**
     * @param canvasDrawing
     *
     * This method will compare the previousCanvasState and currentCanvasState and sets the
     * starting_x position of a cardinal drawing/friend marker as it's previous end_x value
     */
    private void reassembleCanvasState(CompassDrawing canvasDrawing) {
        for (CardinalDrawing currentDrawingObject : canvasDrawing.cardinalDirections) {
            for (CardinalDrawing prevDrawingObject : previousCanvasState.cardinalDirections) {
                if (currentDrawingObject.getCardinalDirection().equals(prevDrawingObject.getCardinalDirection())) {
                    currentDrawingObject.setStarting_x(prevDrawingObject.getEnd_x());
                }
            }

        }
    }


    /**
     * @param canvasDrawing
     * @param rotationFromNorth
     *
     * This method will check the boundaries of a given compass (360 degrees), and will either
     * increment it be 360 degrees -- if it's close to either 0 or 360, or leave it be
     */
    private void getCardinalObjectsToDraw(CompassDrawing canvasDrawing, float rotationFromNorth) {

        int[] direction_values = {NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST};
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};

        // If the boundaries are close to 0 or close to 360 -- then increase their values by 360
        if (rotationFromNorth + (INTERVAL * compassDegreeInterval) / 2 > ROTATION ||
                rotationFromNorth - (INTERVAL * compassDegreeInterval) / 2 < NORTH) {

            for (int i = 0; i < direction_values.length; i++) {
                addCardinalDirections(canvasDrawing, rotationFromNorth, ROTATION, directions[i], direction_values[i]);
            }
            for (int i = 0; i < friendDrawings.size(); i++) {
                addCardinalDirections(canvasDrawing, rotationFromNorth, ROTATION, friendDrawings.get(i).getId(), (int)friendDrawings.get(i).getRotationFromNorth());
            }

        }

        // Otherwise the boundaries are fine
        else {
            for (int i = 0; i < direction_values.length; i++) {
                addCardinalDirections(canvasDrawing, rotationFromNorth, 0, directions[i], direction_values[i]);
            }
            for (int i = 0; i < friendDrawings.size(); i++) {
                addCardinalDirections(canvasDrawing, rotationFromNorth, 0, friendDrawings.get(i).getId(), (int)friendDrawings.get(i).getRotationFromNorth());
            }
        }
    }

    /**
     * @param canvasDrawing
     * @param rotationFromNorth
     * @param boundaryAddition
     * @param direction
     * @param direction_value
     *
     * This method will run through every cardinal direction and friend marker, and determine
     * whether they can be added based on the compasses rotation.
     */
    private void addCardinalDirections(CompassDrawing canvasDrawing, float rotationFromNorth,
                                       int boundaryAddition, String direction, int direction_value) {
        float starting_x;

        if (drawCardinalDirection(direction_value + boundaryAddition, rotationFromNorth) ||
                drawCardinalDirection(direction_value, rotationFromNorth)) {

            float position = calculateCardinalPosition(direction_value + boundaryAddition, rotationFromNorth);
            starting_x = closerToStartOfRect(position) ? rectLeft : rectRight;

            canvasDrawing.cardinalDirections.add(new CardinalDrawing(
                    direction,
                    starting_x,
                    rectTop + (rectHeight / 2),
                    position,
                    rectTop + (rectHeight / 2)
            ));
        }
    }

    /**
     * @param cardinalDegree
     * @param rotationFromNorth
     * @return
     *
     * This method determines whether we can draw the cardinal direction/friend marking
     * to the canvas
     */
    public boolean drawCardinalDirection(int cardinalDegree, float rotationFromNorth) {
        float upperBoundary = rotationFromNorth + (INTERVAL * compassDegreeInterval) / 2;
        float lowerBoundary = rotationFromNorth - (INTERVAL * compassDegreeInterval) / 2;

        if (cardinalDegree > lowerBoundary && cardinalDegree < upperBoundary) {
            return true;
        }
        return false;
    }

    /**
     * @param cardinalDegree
     * @param rotationFromNorth
     * @return
     *
     * This will return the location on the canvas in which we can draw the cardinal direction/
     * friend marker
     */
    public float calculateCardinalPosition(int cardinalDegree, float rotationFromNorth) {
        float upperBoundary = rotationFromNorth + (INTERVAL * compassDegreeInterval) / 2;
        float lowerBoundary = rotationFromNorth - (INTERVAL * compassDegreeInterval) / 2;

        // Range is [360, 720]
        if (cardinalDegree > lowerBoundary && cardinalDegree < upperBoundary) {
            float percentage = 1 - ((upperBoundary - cardinalDegree) / (upperBoundary - lowerBoundary));
            return ((percentage * INTERVAL) * rectIntervals) + rectLeft;
        }

        // Range is [0, 360]
        if (cardinalDegree - ROTATION > lowerBoundary && cardinalDegree - ROTATION < upperBoundary) {
            float degree = cardinalDegree - ROTATION;
            float percentage = 1 - ((upperBoundary - degree) / (upperBoundary - lowerBoundary));
            return ((percentage * INTERVAL) * rectIntervals) + rectLeft;
        }

        return 0;
    }

    /**
     * @param rotationFromNorth
     * @return
     *
     * This method will convert a given return to a value between the range of [0..360] if
     * it's negative
     */
    public float convertRotation(float rotationFromNorth) {
        if (rotationFromNorth > 0) return rotationFromNorth;
        return rotationFromNorth + ROTATION;
    }

    /**
     * @param position
     * @return
     *
     * This method will determine from which side of the canvas that we should begin a newly
     * animated marker
     */
    private boolean closerToStartOfRect(float position) {
        if (position > (rectLeft + rectWidth / 2)) {
            return false;
        }
        return true;
    }

    /**
     * @param intendedPosition
     * @return
     *
     * This method corrects how we render our markers onto the compass. This fixes the issue --
     * that East appears on the left hand side of North whilst West appears on the right hand side
     * of North -- creating an incorrect compass
     */
    private float normalisePosition(float intendedPosition) {
        float difference = intendedPosition - (rectWidth / 2);

        // The intended position is on the right hand side of the middle point
        if (difference > 0) {
            return (rectLeft + rectWidth / 2) - difference;
        }
        // The intended position is on the left hand side of the middle point
        else {
            return (rectLeft + rectWidth / 2) + difference;
        }
    }




    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}
    private Rect createRect() {
        return new Rect(rectLeft, rectTop, rectRight, rectBottom);
    }
    public void setRunning(boolean running) {
        this.running = running;
    }
    public boolean getRunning() {
        return this.running;
    }
}
