package net.noconroy.itproject.application.AR;

import android.graphics.Bitmap;

/**
 * Created by Mattias on 14/09/2017.
 */

public class FriendDrawing {

    private String id;
    private String name;
    private Bitmap userImage;
    private double rotationFromNorth;

    public FriendDrawing() {
        ;
    }

    public double getRotationFromNorth() {
        return rotationFromNorth;
    }
    public void setRotationFromNorth(double rotationFromNorth) { this.rotationFromNorth = rotationFromNorth; }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Bitmap getBitmap() { return userImage; }
    public void setBitmap(Bitmap userImage) { this.userImage = userImage; }
}
