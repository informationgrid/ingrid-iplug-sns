/*
 * **************************************************-
 * Ingrid iPlug SNS
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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
public class SNSControllerPerformanceTestLocal extends TestCase {

    private SNSControllerTestLocal fSNSControllerTest;
    
    private SNSIndexingInterfaceTestLocal fSNSIndexingInterfaceTest;

    private final int fMaxLoops = 10;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        this.fSNSIndexingInterfaceTest = new SNSIndexingInterfaceTestLocal();
        this.fSNSIndexingInterfaceTest.setSNSIndexingInterface(new SNSIndexingInterface("ms", "m3d1asyl3", "de"));
        
        this.fSNSControllerTest = new SNSControllerTestLocal();
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
        for (int i = 0; i < this.fMaxLoops; i++) {
            this.fSNSControllerTest.testTopicsForTerm();
        }
        final long stop = System.currentTimeMillis();

        final String output = "Time for " + this.fMaxLoops + " calls of testTopicsForTerm(): " + ((stop - start) / 1000) + " ("
                + (this.fMaxLoops / ((stop - start) / 1000)) + " calls/second)";
        System.out.println(output);
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

        final String output = "Time for " + this.fMaxLoops + " calls of testGetAssociatedTopics(): " + ((stop - start) / 1000) + " ("
                + (this.fMaxLoops / ((stop - start) / 1000)) + " calls/second)";
        System.out.println(output);
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

        final String output = "Time for " + this.fMaxLoops + " calls of testGetDocumentRelatedTopics(): " + ((stop - start) / 1000) + " ("
        + (this.fMaxLoops / ((stop - start) / 1000)) + " calls/second)"; 
        System.out.println(output);
    }

    /**
     * @throws Exception
     */
    public void testGetBuzzword100() throws Exception {
        final long start = System.currentTimeMillis();
        for (int i = 0; i < this.fMaxLoops; i++) {
            this.fSNSIndexingInterfaceTest.testGetBuzzword();
        }
        final long stop = System.currentTimeMillis();

        final String output = "Time for " + this.fMaxLoops + " calls of testGetBuzzword(): " + ((stop - start) / 1000) + " ("
                + (this.fMaxLoops / ((stop - start) / 1000)) + " calls/second)";
        System.out.println(output);
    }
}
