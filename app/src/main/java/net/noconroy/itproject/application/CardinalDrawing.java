package net.noconroy.itproject.application;

/**
 * Created by Mattias on 14/09/2017.
 */

public class CardinalDrawing {

    private String cardinalDirection;
    private float starting_x;
    private float starting_y;
    private float end_x;
    private float end_y;

    public CardinalDrawing(String cardinalDirection, float starting_x, float starting_y,
                           float end_x, float end_y) {
        this.cardinalDirection = cardinalDirection;
        this.starting_x = starting_x;
        this.starting_y = starting_y;
        this.end_x = end_x;
        this.end_y = end_y;

    }

    public String getCardinalDirection() {
        return cardinalDirection;
    }

    public void setCardinalDirection(String cardinalDirection) {
        this.cardinalDirection = cardinalDirection;
    }

    public float getStarting_x() {
        return starting_x;
    }

    public void setStarting_x(float starting_x) {
        this.starting_x = starting_x;
    }

    public float getStarting_y() {
        return starting_y;
    }

    public void setStarting_y(float starting_y) {
        this.starting_y = starting_y;
    }

    public float getEnd_x() {
        return end_x;
    }

    public void setEnd_x(float end_x) {
        this.end_x = end_x;
    }

    public float getEnd_y() {
        return end_y;
    }

    public void setEnd_y(float end_y) {
        this.end_y = end_y;
    }
}
