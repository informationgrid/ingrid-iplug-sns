/*
 * **************************************************-
 * Ingrid iPlug SNS
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
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

    private String fGemeindekennziffer;

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
     * @param gemeindekennziffer
     *            The so called Gemeindekennziffer (community code of the German Bureau of Statistics).
     * 
     */
    public Wgs84Box(final String baseName, final double x1, final double x2, final double y1, final double y2,
            final String gemeindekennziffer) {
        this.fTopicName = baseName;
        this.fX1 = x1;
        this.fX2 = x2;
        this.fY1 = y1;
        this.fY2 = y2;
        this.fGemeindekennziffer = gemeindekennziffer;
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
     * Sets the name of a topic.
     * 
     * @param topicName
     * 			The name of the topic.
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
     * Sets longitude of the point left down. 
     * 
     * @param x1 
     * 			A double coordinate.
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
     * Sets the latitude of the point left down.
     * 
     * @param x2 
     * 			A double coordinate.
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
     * Sets longitude of the point right up.
     * 
     * @param y1 
     * 			A double coordinate.
     * 
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
     * Sets latitude of the point right up.
     * 
     * @param y2 
     * 			A double coordinate.
     */
    public void setY2(double y2) {
        this.fY2 = y2;
    }

    /**
     * Returns the so called Gemeindekennziffer. It is the community code of the German Bureau of Statistics.
     * 
     * @return A string representing the Gemeindekennziffer.
     */
    public String getGemeindekennziffer() {
        return this.fGemeindekennziffer;
    }

    /**
     * Sets the so called Gemeindekennziffer. It is the community code of the German Bureau of Statistics.
     * 
     * @param gemeindekennziffer
     *            The so called Gemeindekennziffer.
     */
    public void setGemeindekennziffer(String gemeindekennziffer) {
        this.fGemeindekennziffer = gemeindekennziffer;
    }

	public String toString() {
		String result = "[";
		result += "Name: "+this.fTopicName;
		result += ", Gemeindekennziffer: "+this.fGemeindekennziffer;
		result += ", WGS84Box: "+this.fX1+","+this.fY1+" "+this.fX2+","+this.fY2;
		result += "]";
		return result;
	}
}
