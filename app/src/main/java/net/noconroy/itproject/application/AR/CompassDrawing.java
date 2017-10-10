package net.noconroy.itproject.application.AR;

import android.graphics.Rect;

import java.util.ArrayList;

/**
 * Created by Mattias on 14/09/2017.
 */

// Represents an object that will be drawn to the surface view representing the camera
public class CompassDrawing {

    public ArrayList<CardinalDrawing> cardinalDirections;
    public ArrayList<FriendDrawing> friendDrawings;
    public Rect canvasRect;

    public CompassDrawing(Rect canvasRect) {
        this.canvasRect = canvasRect;
        cardinalDirections = new ArrayList<CardinalDrawing>();
        friendDrawings = new ArrayList<FriendDrawing>();
    }
}
