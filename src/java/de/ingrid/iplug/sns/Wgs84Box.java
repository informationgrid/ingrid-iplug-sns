/*
 * Copyright (c) 1997-2006 by media style GmbH
 */

package de.ingrid.iplug.sns;

/**
 * 
 */
public class Wgs84Box {

    private String fTopicName;

    private double fX1;

    private double fX2;

    private double fY1;

    private double fY2;

    /**
     * @param baseName
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     */
    public Wgs84Box(String baseName, double x1, double x2, double y1, double y2) {
        this.fTopicName = baseName;
        this.fX1 = x1;
        this.fX2 = x2;
        this.fY1 = y1;
        this.fY2 = y2;
    }

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
     * @return
     */
    public double getX1() {
        return this.fX1;
    }

    /**
     * @param x1
     */
    public void setX1(double x1) {
        this.fX1 = x1;
    }

    /**
     * @return
     */
    public double getX2() {
        return this.fX2;
    }

    /**
     * @param x2
     */
    public void setX2(double x2) {
        this.fX2 = x2;
    }

    /**
     * @return
     */
    public double getY1() {
        return this.fY1;
    }

    /**
     * @param y1
     */
    public void setY1(double y1) {
        this.fY1 = y1;
    }

    /**
     * @return
     */
    public double getY2() {
        return this.fY2;
    }

    /**
     * @param y2
     */
    public void setY2(double y2) {
        this.fY2 = y2;
    }
}
