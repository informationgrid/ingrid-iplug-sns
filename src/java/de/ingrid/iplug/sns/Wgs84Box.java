/*
 * Copyright (c) 1997-2006 by media style GmbH
 */

package de.ingrid.iplug.sns;

/**
 * Bounding box coordinates.
 */
public class Wgs84Box {

    private String fTopicName;

    private double fX1;

    private double fX2;

    private double fY1;

    private double fY2;

    /**
     * Sets the required values of the bounding box.
     * 
     * @param baseName
     *            The name of the topic.
     * @param x1
     *            Longitude of the point left down.
     * @param x2
     *            Latitude of the point left down.
     * @param y1
     *            Longitude of the point right up.
     * @param y2
     *            Latitude of the point right up.
     */
    public Wgs84Box(String baseName, double x1, double x2, double y1, double y2) {
        this.fTopicName = baseName;
        this.fX1 = x1;
        this.fX2 = x2;
        this.fY1 = y1;
        this.fY2 = y2;
    }

    /**
     * The name of the topic. In the language of the SNS it is the basename.
     * 
     * @return The name of the topic.
     */
    public String getTopicName() {
        return this.fTopicName;
    }

    /**
     * @param topicName
     */
    public void setTopicName(String topicName) {
        this.fTopicName = topicName;
    }

    /**
     * This is the longitude of the point left down.
     * 
     * @return A double coordinate.
     */
    public double getX1() {
        return this.fX1;
    }

    /**
     * @param x1
     *            Longitude of the point left down.
     */
    public void setX1(double x1) {
        this.fX1 = x1;
    }

    /**
     * This is the latitude of the point left down.
     * 
     * @return A double coordinate.
     */
    public double getX2() {
        return this.fX2;
    }

    /**
     * @param x2
     *            Latitude of the point left down.
     */
    public void setX2(double x2) {
        this.fX2 = x2;
    }

    /**
     * This is the longitude of the point right up.
     * 
     * @return A double coordinate.
     */
    public double getY1() {
        return this.fY1;
    }

    /**
     * @param y1
     *            Longitude of the point right up.
     */
    public void setY1(double y1) {
        this.fY1 = y1;
    }

    /**
     * This is the latitude of the point right up.
     * 
     * @return A double coordinate.
     */
    public double getY2() {
        return this.fY2;
    }

    /**
     * @param y2
     *            Latitude of the point right up.
     */
    public void setY2(double y2) {
        this.fY2 = y2;
    }
}
