/*
 * Copyright (c) 1997-2006 by media style GmbH
 */

package de.ingrid.iplug.sns;

import java.text.ParseException;

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

        System.out.println(result[0].getTopicName());
        System.out.println(result[0].getX1());
        System.out.println(result[0].getX2());
        System.out.println(result[0].getY1());
        System.out.println(result[0].getY2());
    }

    /**
     * @throws Exception
     */
    public void testGetReferencesToTime() throws Exception {
        this.fSnsInterface.getBuzzwords("Tschernobyl Ohio", 1000);

        final Temporal[] result = this.fSnsInterface.getReferencesToTime();
        assertNotNull(result);
        assertEquals(3, result.length);
        System.out.println(result[0].getAt());
    }

    /**
     * @throws Exception
     */
    public void testGetBuzzword() throws Exception {
        final String[] result = this.fSnsInterface.getBuzzwords("Tschernobyl Ohio", 1000);
        assertNotNull(result);
        assertEquals(4, result.length);
    }

    /**
     * @throws Exception
     */
    public void testGetBuzzwordNotExistent() throws Exception {
        final String[] result = this.fSnsInterface.getBuzzwords("blabla", 1000);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    /**
     * @throws Exception
     */
    public void testGetReferencesNotExistent() throws Exception {
        this.fSnsInterface.getBuzzwords("blabla", 1000);

        final Temporal[] result = this.fSnsInterface.getReferencesToTime();
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    /**
     * @throws Exception
     */
    public void testGetReferencesWithOtherDateFormatYYYY() throws Exception {
        this.fSnsInterface.getBuzzwords("wasser magdeburg", 1000);

        try {
            final Temporal[] result = this.fSnsInterface.getReferencesToTime();
            assertNotNull(result);
            assertEquals(1, result.length);
        } catch (ParseException e) {
            fail();
        }
    }
}
