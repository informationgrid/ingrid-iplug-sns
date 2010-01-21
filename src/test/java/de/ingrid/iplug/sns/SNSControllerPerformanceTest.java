/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: $
 */

package de.ingrid.iplug.sns;

import junit.framework.TestCase;
import de.ingrid.external.sns.SNSClient;

/**
 * 
 */
public class SNSControllerPerformanceTest extends TestCase {

    private SNSControllerTest fSNSControllerTest;
    
    private SNSIndexingInterfaceTest fSNSIndexingInterfaceTest;

    private static final int fMaxLoops = 3;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        this.fSNSIndexingInterfaceTest = new SNSIndexingInterfaceTest();
        this.fSNSIndexingInterfaceTest.setSNSIndexingInterface(new SNSIndexingInterface("ms", "m3d1asyl3", "de"));
        
        this.fSNSControllerTest = new SNSControllerTest();
        SNSClient snsClient = new SNSClient("ms", "m3d1asyl3", "de");
        snsClient.setTimeout(180000);
        this.fSNSControllerTest.setSNSClient(snsClient);
    }

    /**
     * @throws Exception
     * 
     */
    public void testTopicsForTerm100() throws Exception {
        final long start = System.currentTimeMillis();
        for (int i = 0; i < SNSControllerPerformanceTest.fMaxLoops; i++) {
            this.fSNSControllerTest.testTopicsForTerm();
        }
        final long stop = System.currentTimeMillis();

        final String output = "Time for " + SNSControllerPerformanceTest.fMaxLoops + " calls of testTopicsForTerm(): " + ((stop - start) / 1000) + " ("
                + (SNSControllerPerformanceTest.fMaxLoops / ((stop - start) / 1000)) + " calls/second)";
        System.out.println(output);
    }

    /**
     * @throws Exception
     */
    public void testGetAssociatedTopics100() throws Exception {
        final long start = System.currentTimeMillis();
        for (int i = 0; i < SNSControllerPerformanceTest.fMaxLoops; i++) {
            this.fSNSControllerTest.testGetAssociatedTopics();
        }
        final long stop = System.currentTimeMillis();

        final String output = "Time for " + SNSControllerPerformanceTest.fMaxLoops + " calls of testGetAssociatedTopics(): " + ((stop - start) / 1000) + " ("
                + (SNSControllerPerformanceTest.fMaxLoops / ((stop - start) / 1000)) + " calls/second)";
        System.out.println(output);
    }

    /**
     * @throws Exception
     */
    public void testGetDocumentRelatedTopics100() throws Exception {
        final long start = System.currentTimeMillis();
        for (int i = 0; i < SNSControllerPerformanceTest.fMaxLoops; i++) {
            this.fSNSControllerTest.testGetDocumentRelatedTopics();
        }
        final long stop = System.currentTimeMillis();

        final String output = "Time for " + SNSControllerPerformanceTest.fMaxLoops + " calls of testGetDocumentRelatedTopics(): " + ((stop - start) / 1000) + " ("
        + (SNSControllerPerformanceTest.fMaxLoops / ((stop - start) / 1000)) + " calls/second)"; 
        System.out.println(output);
    }

    /**
     * @throws Exception
     */
    public void testGetBuzzword100() throws Exception {
        final long start = System.currentTimeMillis();
        for (int i = 0; i < SNSControllerPerformanceTest.fMaxLoops; i++) {
            this.fSNSIndexingInterfaceTest.testGetBuzzword();
        }
        final long stop = System.currentTimeMillis();

        final String output = "Time for " + SNSControllerPerformanceTest.fMaxLoops + " calls of testGetBuzzword(): " + ((stop - start) / 1000) + " ("
                + (SNSControllerPerformanceTest.fMaxLoops / ((stop - start) / 1000)) + " calls/second)";
        System.out.println(output);
    }
}
