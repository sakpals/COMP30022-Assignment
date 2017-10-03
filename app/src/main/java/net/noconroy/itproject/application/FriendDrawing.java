package net.noconroy.itproject.application;

/**
 * Created by Mattias on 14/09/2017.
 */

public class FriendDrawing {

    private double lat;
    private double long_;
    private String id;
    private String name;

    private double rotationFromNorth;       // Will be used by CompassManager to render the position of our friend

    public FriendDrawing() {
        ;
    }

    public double getRotationFromNorth() {
        return rotationFromNorth;
    }
    public void setRotationFromNorth(double rotationFromNorth) {
        this.rotationFromNorth = rotationFromNorth;
    }
    public double getLat() {
        return lat;
    }
    public void setLat(double lat) {
        this.lat = lat;
    }
    public double getLong_() {
        return long_;
    }
    public void setLong_(double long_) {
        this.long_ = long_;
    }
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
}
