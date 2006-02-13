/*
 * Copyright (c) 1997-2006 by media style GmbH
 */

package de.ingrid.iplug.sns;

/**
 * 
 */
public class Wgs84Box {

    private String fTopicName;

    private String fX1;

    private String fX2;

    private String fY1;

    private String fY2;

    /**
     * @param baseName
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     */
    public Wgs84Box(String baseName, String x1, String x2, String y1, String y2) {
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
    public String getX1() {
        return this.fX1;
    }

    /**
     * @param x1
     */
    public void setX1(String x1) {
        this.fX1 = x1;
    }

    /**
     * @return
     */
    public String getX2() {
        return this.fX2;
    }

    /**
     * @param x2
     */
    public void setX2(String x2) {
        this.fX2 = x2;
    }

    /**
     * @return
     */
    public String getY1() {
        return this.fY1;
    }

    /**
     * @param y1
     */
    public void setY1(String y1) {
        this.fY1 = y1;
    }

    /**
     * @return
     */
    public String getY2() {
        return this.fY2;
    }

    /**
     * @param y2
     */
    public void setY2(String y2) {
        this.fY2 = y2;
    }
}
