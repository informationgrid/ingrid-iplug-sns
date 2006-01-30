/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: $
 */

package de.ingrid.iplug.sns;

import junit.framework.TestCase;

/**
 * 
 */
public class SNSControllerPerformanceTest extends TestCase {

    private SNSControllerTest fSNSControllerTest;

    private final int fMaxLoops = 100;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        this.fSNSControllerTest = new SNSControllerTest();
        this.fSNSControllerTest.setSNSClient(new SNSClient("ms", "portalu2006", "de"));
    }

    /**
     * @throws Exception
     * 
     */
    public void testTopicsForTerm100() throws Exception {
        final long start = System.currentTimeMillis();
        for (int i = 0; i < this.fMaxLoops; i++) {
            this.fSNSControllerTest.testTopicsForTerm();
        }
        final long stop = System.currentTimeMillis();

        System.out.println("Time for 100 calls of testTopicsForTerm(): " + ((stop - start) / 1000) + " ("
                + (100.0 / ((stop - start) / 1000)) + " calls/second)");
    }

    /**
     * @throws Exception
     */
    public void testGetAssociatedTopics100() throws Exception {
        final long start = System.currentTimeMillis();
        for (int i = 0; i < this.fMaxLoops; i++) {
            this.fSNSControllerTest.testGetAssociatedTopics();
        }
        final long stop = System.currentTimeMillis();

        System.out.println("Time for 100 calls of testGetAssociatedTopics(): " + ((stop - start) / 1000) + " ("
                + (100.0 / ((stop - start) / 1000)) + " calls/second)");
    }

    /**
     * @throws Exception
     */
    public void testGetDocumentRelatedTopics100() throws Exception {
        final long start = System.currentTimeMillis();
        for (int i = 0; i < this.fMaxLoops; i++) {
            this.fSNSControllerTest.testGetDocumentRelatedTopics();
        }
        final long stop = System.currentTimeMillis();

        System.out.println("Time for 100 calls of testGetDocumentRelatedTopics(): " + ((stop - start) / 1000) + " ("
                + (100.0 / ((stop - start) / 1000)) + " calls/second)");
    }
}
