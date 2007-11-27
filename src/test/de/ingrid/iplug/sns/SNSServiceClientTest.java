/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: DispatcherTest.java,v $
 */

package de.ingrid.iplug.sns;

import junit.framework.TestCase;

import com.slb.taxi.webservice.xtm.stubs.FieldsType;
import com.slb.taxi.webservice.xtm.stubs.SearchType;
import com.slb.taxi.webservice.xtm.stubs.TopicMapFragment;

/**
 * created on 21.07.2005
 * 
 * @author hs
 */
public class SNSServiceClientTest extends TestCase {

    private static SNSClient adapter = null;

    static {
        try {
            adapter = new SNSClient("ms", "m3d1asyl3", "de");
            adapter.setTimeout(180000);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * 
     * @throws Exception
     */
    public void testFindTopics() throws Exception {
        String queryTerm = null;
        int offset = -1;

        try {
            adapter.findTopics(queryTerm, null, null, null, offset, "de");
            fail("Should throw an exception");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }

        queryTerm = "xyz";
        try {
            adapter.findTopics(queryTerm, null, null, null, offset, "de");
            fail("Should throw an exception");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
        offset = 1;
        assertNotNull(adapter.findTopics(queryTerm, null, null, null, offset, "de"));
        offset = Integer.MAX_VALUE;
        assertNotNull(adapter.findTopics(queryTerm, null, null, null, offset, "de"));
    }

    /**
     * @throws Exception
     * 
     */
    public void testGetPSI() throws Exception {
        String topicID = null;
        int distance = -1;
        try {
            adapter.getPSI(topicID, distance, null);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
        topicID = "uba_thes_3450";
        try {
            adapter.getPSI(topicID, distance, null);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
        distance = 4;
        try {
            adapter.getPSI(topicID, distance, null);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
        distance = 2;
        assertNotNull(adapter.getPSI(topicID, distance, null));
    }

    /**
     * @throws Exception
     * 
     * 
     */
    public void testAutoClassify() throws Exception {
        String document = null;
        int maxWords = -1;
        try {
            adapter.autoClassify(document, maxWords, null, true, "de");
            fail("Should throw an exception");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
        document = "Die Ozonschicht ist sehr dünn";
        try {
            adapter.autoClassify(document, maxWords, null, true, "de");
            fail("Should throw an exception");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
        maxWords = 0;
        assertNotNull(adapter.autoClassify(document, maxWords, null, true, "de"));
        maxWords = Integer.MAX_VALUE;
        assertNotNull(adapter.autoClassify(document, maxWords, null, true, "de"));
    }

    /**
     * @throws Exception
     */

    public void testGetTypes() throws Exception {
        TopicMapFragment fragment = adapter.getTypes();
        assertNotNull(fragment);
    }

    /**
     * @throws Exception
     */
    public void testAnniversary() throws Exception {
        TopicMapFragment fragment = adapter.anniversary("1976-08-31");
        assertNotNull(fragment);
    }

    /**
     * @throws Exception
     */
    public void testGetSimilarTerms() throws Exception {
        TopicMapFragment fragment = adapter.getSimilarTerms(true, new String[] { "1976-08-31" }, "de");
        assertNotNull(fragment);
    }

    /**
     * @throws Exception
     */
    public void testFindEventsAt() throws Exception {
        TopicMapFragment fragment = adapter.findEvents("query", true, SearchType.contains, new String[] { "/event/" },
                FieldsType.allfields, 0, "1976-08-31", "de", 10);
        assertNotNull(fragment);
    }
}
