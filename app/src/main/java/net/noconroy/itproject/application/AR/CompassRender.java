package net.noconroy.itproject.application.AR;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.view.SurfaceHolder;

import java.util.ArrayList;

/**
 * Created by Mattias on 14/09/2017.
 */

public class CompassRender extends Thread {

    private static final String TAG = CompassRender.class.getSimpleName();


    /***************************************************************************************/
    /*********************************** Constants *****************************************/
    /***************************************************************************************/


    private static final int textSize = 40;         // for cardinal direction text

    private int iteration;                          // current frame iteration
    private static final int animationdelay = 10;   // number of frames necessary to render a canvas

    private CompassSurfaceView compassSurfaceView;
    private SurfaceHolder compassSurfaceHolder;
    private CompassDrawing currentObjectBeingDrawn;
    public ArrayList<CompassDrawing> objectsToDraw;

    private boolean currentlyAnimating = false;
    private boolean running = false;

    Paint emptyPaint;
    Paint rectPaint;
    Paint textPaint;


    /***************************************************************************************/
    /*********************************** Class Methods *************************************/
    /***************************************************************************************/

    public CompassRender(SurfaceHolder compassSurfaceHolder, CompassSurfaceView compassSurfaceView) {

        // Initialise variables
        this.compassSurfaceView = compassSurfaceView;
        this.compassSurfaceHolder = compassSurfaceHolder;
        objectsToDraw = new ArrayList<CompassDrawing>();
        currentObjectBeingDrawn = null;

        setRunning(false);
        iteration = 0;

        // Initialise our various paints
        emptyPaint = new Paint();
        emptyPaint.setColor(Color.TRANSPARENT);
        emptyPaint.setStyle(Paint.Style.FILL);

        rectPaint = new Paint();
        rectPaint.setColor(Color.BLACK);
        rectPaint.setStrokeWidth(2);

        textPaint = new Paint();
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(textSize);
    }

    @Override
    public void run() {

        while (running) {

            if (compassSurfaceView.getDrawOk()) {

                Canvas canvas = compassSurfaceHolder.lockCanvas();

                if (canvas != null) {
                    synchronized (compassSurfaceHolder) {

                        if (currentObjectBeingDrawn != null) {

                            // empty the view
                            // canvas.drawPaint(emptyPaint);
                            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                            // draw the rect
                            canvas.drawRect(currentObjectBeingDrawn.canvasRect, rectPaint);

                            // draw cardinal directions && friend markers
                            for (CardinalDrawing cardinalObject : currentObjectBeingDrawn.cardinalDirections) {
                                if (cardinalObject.getImage() != null) {
                                    canvas.drawBitmap(
                                            cardinalObject.getImage(),
                                            cardinalObject.getStarting_x() + ((cardinalObject.getEnd_x() - cardinalObject.getStarting_x()) / animationdelay) * iteration,
                                            cardinalObject.getEnd_y() - 30,
                                            textPaint
                                    );
                                }
                                else {
                                    canvas.drawText(cardinalObject.getCardinalDirection(),
                                            cardinalObject.getStarting_x() + ((cardinalObject.getEnd_x() - cardinalObject.getStarting_x()) / animationdelay) * iteration,
                                            cardinalObject.getEnd_y(),
                                            textPaint);
                                }
                            }

                            // update iteration
                            if (iteration < animationdelay) iteration++;

                            // if we've finished animating our cardinal directions - this is based on
                            // how fast we want our cardinal directions to move to our end destination
                            // set our animationdelay to e.g. 30 means we want our animation to be
                            // completed in 30 frames
                            if (iteration >= animationdelay) currentlyAnimating = false;

                        }
                    }
                }

                compassSurfaceHolder.unlockCanvasAndPost(canvas);
            }

            // if we've finished animating the current object
            // then pop another off the objects to draw pile and begin animating that
            if (!currentlyAnimating) {
                synchronized (objectsToDraw) {
                    if (!objectsToDraw.isEmpty()) {
                        currentObjectBeingDrawn = objectsToDraw.remove(0);
                        currentlyAnimating = true;
                        iteration = 0;
                    }
                }
            }
        }
    }

    public void stopThread() {
        setRunning(false);
    }

    public void setRunning(boolean value) {running = value;}
    public boolean getRunning() {return running;}
    public boolean getCurrentlyAnimating() {return currentlyAnimating;}
}
