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
 * Copyright (c) 1997-2005 by media style GmbH
 *
 * $Source: DispatcherTest.java,v $
 */

package de.ingrid.iplug.sns;

import java.net.URL;
import java.util.ResourceBundle;

import junit.framework.TestCase;

import com.hp.hpl.jena.rdf.model.Resource;

import de.ingrid.external.FullClassifyService.FilterType;
import de.ingrid.external.sns.RDFUtils;
import de.ingrid.external.sns.SNSClient;

/**
 * created on 21.07.2005
 *
 * @author hs
 */
public class SNSServiceClientTest extends TestCase {

    private static SNSClient adapter = null;

    static {
    	ResourceBundle resourceBundle = ResourceBundle.getBundle("sns");
        try {
            adapter = new SNSClient("ms", "m3d1asyl3", "de", new URL(resourceBundle.getString("sns.serviceURL.thesaurus")),
	        		new URL(resourceBundle.getString("sns.serviceURL.gazetteer")),
	        		new URL(resourceBundle.getString("sns.serviceURL.chronicle")));
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
            adapter.findTopics(queryTerm, null, null, null, offset, 500, "de", false);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }

        queryTerm = "xyz";
        try {
            adapter.findTopics(queryTerm, FilterType.ONLY_TERMS, null, null, offset, 500, "de", false);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
        offset = 1;
        assertNotNull(adapter.findTopics(queryTerm, FilterType.ONLY_TERMS, null, null, offset, 500, "de", false));
        offset = Integer.MAX_VALUE;
        assertNotNull(adapter.findTopics(queryTerm, FilterType.ONLY_TERMS, null, null, offset, 500, "de", false));
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
            adapter.autoClassify(document, maxWords, null, false, "de");
            fail("Should throw an exception");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
        document = "Die Ozonschicht ist sehr dünn";
        try {
            adapter.autoClassify(document, maxWords, null, false, "de");
            fail("Should throw an exception");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
        maxWords = 0;
        try {
            assertNotNull(adapter.autoClassify(document, maxWords, null, false, "de"));
            fail("Should throw an exception");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
        // FIXME:
//        assertNotNull(adapter.autoClassify(document, maxWords, FilterType.ONLY_TERMS, false, "de"));
//        maxWords = Integer.MAX_VALUE;
//        assertNotNull(adapter.autoClassify(document, maxWords, FilterType.ONLY_TERMS, false, "de"));
    }

    /**
     * @throws Exception
     */
    public void testAnniversary() throws Exception {
    	SNSClient client = new SNSClient("", "", "de", null, null, new URL("https://sns.uba.de/chronik/"));
        Resource fragment = client.anniversary("1976-08-31", "de");
        assertNotNull(fragment);

        SNSController ctrl = new SNSController(client, "");
        int[] totalSize = new int[1];
        totalSize[0] = 0;
        de.ingrid.iplug.sns.utils.Topic[] result = ctrl.getAnniversaryFromTopic("1976-08-31", "de", 1, "/my-plug", totalSize);
        assertTrue(result.length > 0);

        result = ctrl.getAnniversaryFromTopic("1976-08-31", "en", 1, "/my-plug", totalSize);
        assertTrue(result.length > 0);
    }

    /**
     * @throws Exception
     */
    public void testGetSimilarTerms() throws Exception {
        Resource fragment = adapter.getSimilarTerms(true, new String[] { "1976-08-31" }, "de");
        assertNotNull(fragment);
    }

    /**
     * @throws Exception
     */
    public void testFindEventsAt() throws Exception {
        Resource eventsRes = adapter.findEvents("query", "contains",
        		null, 0, "1976-08-31", "de", 10);
        assertNotNull(eventsRes);
    }

    public void testFindEventsFromTo() throws Exception {
        Resource eventsRes = adapter.findEvents("query", "contains",
        		null, 0, "1976-08-31", "1978-08-31", "de", 10);
        assertNotNull(eventsRes);
    }

    /**
     * @throws Exception
     *
     */
    public void testGetHierachy() throws Exception {
        String topicID = "https://sns.uba.de/umthes/_00040282";
        try {
        	// max depth is 4 with new sns interface
            Resource hierachy = adapter.getHierachy(4, "down", true, "de", topicID);
            assertNotNull(hierachy);
            // TODO: assertEquals(190, RDFUtils.getConcepts(hierachy.getModel()).toList().size());
            assertTrue(RDFUtils.getConcepts(hierachy.getModel()).toList().size() > 0);
        } catch (Exception e) {
            fail("No exception should be thrown: " + e.getMessage());
        }
        try {
            Resource hierachy = adapter.getHierachy(4, "up", false, "de", topicID);
            assertNotNull(hierachy);
            // TODO: assertEquals(4, RDFUtils.getConcepts(hierachy.getModel()).toList().size());
            assertTrue(RDFUtils.getConcepts(hierachy.getModel()).toList().size() > 0);
        } catch (Exception e) {
            fail("No exception should be thrown: " + e.getMessage());
        }
    }

    public void testGetHierachyIncludeSiblings() throws Exception {
    	String topicID = "https://sns.uba.de/umthes/_00026981";
        try {
            Resource hierachy = adapter.getHierachy(4, "up", true, "de", topicID);
            assertNotNull(hierachy);
            assertTrue(RDFUtils.getConcepts(hierachy.getModel()).toList().size() > 50);
        } catch (Exception e) {
            fail("No exception should be thrown: " + e.getMessage());
        }
        try {
            Resource hierachy = adapter.getHierachy(4, "up", false, "de", topicID);
            assertNotNull(hierachy);
            assertEquals(5, RDFUtils.getConcepts(hierachy.getModel()).toList().size());
        } catch (Exception e) {
            fail("No exception should be thrown: " + e.getMessage());
        }
    }
}
