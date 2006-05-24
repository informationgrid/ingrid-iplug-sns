/*
 * Copyright (c) 1997-2006 by media style GmbH
 */

package de.ingrid.iplug.sns;

import java.util.Date;

/**
 * 
 */
public class Temporal {

    private Date fFrom = null;

    private Date fAt = null;

    private Date fTo = null;

    /**
     * The date at which something occured.
     * 
     * @return A date.
     */
    public Date getAt() {
        return this.fAt;
    }

    /**
     * The date from which something occured.
     * 
     * @return A date.
     */
    public Date getFrom() {
        return this.fFrom;
    }

    /**
     * The date to which something occured.
     * 
     * @return A date.
     */
    public Date getTo() {
        return this.fTo;
    }

    /**
     * @param javaDate
     */
    public void setFrom(Date javaDate) {
        this.fFrom = javaDate;
    }

    /**
     * @param javaDate
     */
    public void setTo(Date javaDate) {
        this.fTo = javaDate;
    }

    /**
     * @param javaDate
     */
    public void setAt(Date javaDate) {
        this.fAt = javaDate;
    }

    /**
     * Tests whether one of the dates are set.
     * 
     * @return True if no date is set otherwise false.
     */
    public boolean isEmpty() {
        return ((this.fAt == null) && (this.fFrom == null) && (this.fTo == null));
    }
}
