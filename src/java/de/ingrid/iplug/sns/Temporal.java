/*
 * Copyright (c) 1997-2006 by media style GmbH
 */

package de.ingrid.iplug.sns;

/**
 * 
 */
public class Temporal {

    private String fFrom = null;

    private String fAt = null;

    private String fTo = null;

    /**
     * @return
     */
    public String getAt() {
        return this.fAt;
    }

    /**
     * @return
     */
    public String getFrom() {
        return this.fFrom;
    }

    /**
     * @return
     */
    public String getTo() {
        return this.fTo;
    }

    /**
     * @param value
     */
    public void setFrom(String value) {
        this.fFrom = value;
    }

    /**
     * @param value
     */
    public void setTo(String value) {
        this.fTo = value;
    }

    /**
     * @param value
     */
    public void setAt(String value) {
        this.fAt = value;
    }

    /**
     * @return
     */
    public boolean isEmpty() {
        return ((this.fAt == null) && (this.fFrom == null) && (this.fTo == null));
    }
}
