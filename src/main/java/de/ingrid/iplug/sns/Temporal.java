/*
 * **************************************************-
 * Ingrid iPlug SNS
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
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

import java.util.Date;

/**
 * Class for handling timeline events (from, to and at a given point in time). 
 */
public class Temporal {

    private Date fFrom;

    private Date fAt;

    private Date fTo;

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
     * Set the date from which something occured.
     * 
     * @param javaDate
     */
    public void setFrom(Date javaDate) {
        this.fFrom = javaDate;
    }

    /**
     * Set the date to which something occured.
     * 
     * @param javaDate
     */
    public void setTo(Date javaDate) {
        this.fTo = javaDate;
    }

    /**
     * Set the date at which something occured.
     * 
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

	public String toString() {
		String result = "[";
		result += "At: "+this.fAt;
		result += ", From: "+this.fFrom;
		result += ", To: "+this.fTo;
		result += "]";
		return result;
	}
}
