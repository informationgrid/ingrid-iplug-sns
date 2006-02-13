/*
 * Copyright (c) 1997-2006 by media style GmbH
 */

package de.ingrid.iplug.sns;

import junit.framework.TestCase;

/**
 * 
 */
public class SNSIndexingInterfaceTest extends TestCase {

    private SNSIndexingInterface fSnsInterface;

    protected void setUp() throws Exception {
        super.setUp();

        this.fSnsInterface = new SNSIndexingInterface("ms", "portalu2006", "de");
    }

    /**
     * @throws Exception 
     */
    public void testGetReferencesToSpace() throws Exception {
        this.fSnsInterface.getBuzzwords("Halle", 1000);

        final Wgs84Box[] result = this.fSnsInterface.getReferencesToSpace();
        assertNotNull(result);
        assertEquals(4, result.length);
    }

    /**
     * @throws Exception
     */
    public void testGetReferencesToTime() throws Exception {
        this.fSnsInterface.getBuzzwords("Tschernobyl Ohio", 1000);

        final String[] result = this.fSnsInterface.getReferencesToTime();
        assertNotNull(result);
        assertEquals(3, result.length);
    }

    /**
     * @throws Exception
     */
    public void testGetBuzzword() throws Exception {
        final String[] result = this.fSnsInterface.getBuzzwords("Tschernobyl Ohio", 1000);
        assertNotNull(result);
        assertEquals(4, result.length);
    }
}
