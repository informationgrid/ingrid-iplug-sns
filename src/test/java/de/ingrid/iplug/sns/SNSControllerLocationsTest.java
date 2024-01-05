/*
 * **************************************************-
 * Ingrid iPlug SNS
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
/*
 * Copyright (c) 2003 by media style GmbH
 * 
 * $Source: /cvs/SiemensPI/ms_codetemplates.xml,v $
 */

package de.ingrid.iplug.sns;

import de.ingrid.external.sns.SNSClient;
import de.ingrid.iplug.sns.utils.DetailedTopic;
import de.ingrid.iplug.sns.utils.Topic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SNSControllerTest
 * 
 * <p/>created on 29.09.2005
 * 
 * @version $Revision: $
 * @author sg
 * @author $Author: ${lastedit}
 * 
 */
public class SNSControllerLocationsTest {

    private static SNSClient fClient;

    /**
     * @param client
     * @throws Exception
     */
    public void setSNSClient(SNSClient client) throws Exception {
        fClient = client;
    }

    /**
     * @see 
     */
    @BeforeEach
    public void setUp() throws Exception {

        fClient = new SNSClient("ms", "m3d1asyl3", "de");
        fClient.setTimeout(180000);
    }

    /**
     * @throws Exception
     */
    @Test
    public void testGetAssociatedTopics() throws Exception {
        SNSController controller = new SNSController(fClient, "agsNotation");
        int[] totalSize = new int[1];
        
        // LOCATION
		String locationId = "https://sns.uba.de/gazetteer/GEMEINDE0641200000"; // Frankfurt am Main
        Topic[] topics = controller.getTopicsForTopic(locationId, 23, "/location", "aId", "de", totalSize, false);
        assertEquals(23, topics.length);

        // LOCATION
        topics = controller.getTopicSimilarLocationsFromTopic(locationId, 23, "aId", totalSize, "de");
        assertEquals(23, topics.length);
    }

    /**
     * @throws Exception
     */
    @Test
    public void testGetDocumentRelatedTopics() throws Exception {
        SNSController controller = new SNSController(fClient, "agsNotation");
        int[] totalSize = new int[1];
        String text = "Tschernobyl liegt in Halle gefunden";
        DetailedTopic[] topics = controller.getTopicsForText(text, 100, "aPlugId", "de", totalSize, false);
        assertNotNull(topics);
        assertTrue(topics.length > 0);

        text = "yyy xxx zzz";
        topics = controller.getTopicsForText(text, 100, "aPlugId", "de", totalSize, false);
        assertNotNull(topics);
        assertEquals(0, topics.length);

        // valid URL
//        String url = "http://www.portalu.de";
        String url = "http://www.rmv.de";
        int maxWords = 200;
        topics = controller.getTopicsForURL(url, maxWords, null, "aPlugId", "de", totalSize);
        assertNotNull(topics);
        int numAllTopics = topics.length;
		assertTrue(numAllTopics > 0);

		// only thesa
        topics = controller.getTopicsForURL(url, maxWords, "/thesa", "aPlugId", "de", totalSize);
        assertNotNull(topics);
		assertTrue(topics.length > 0);
		assertTrue(topics.length < numAllTopics);

		// only locations
        topics = controller.getTopicsForURL(url, maxWords, "/location", "aPlugId", "de", totalSize);
        assertNotNull(topics);
		assertTrue(topics.length > 0);
		assertTrue(topics.length < numAllTopics);

		// only events
        topics = controller.getTopicsForURL(url, maxWords, "/event", "aPlugId", "de", totalSize);
        assertNotNull(topics);
        // May fail due to wrong content on Site ?!!!!????
		//assertTrue(topics.length > 0);
		//assertTrue(topics.length < numAllTopics);

		// INVALID URL
        url = "http://www.partalu.de";
        try {
            topics = controller.getTopicsForURL(url, maxWords, "/event", "aPlugId", "de", totalSize);        	
        } catch (Exception ex) {
            System.out.println("EXPECTED exception" + ex);
        }
        url = "htp://www.portalu .de";
        try {
            topics = controller.getTopicsForURL(url, maxWords, "/event", "aPlugId", "de", totalSize);
        } catch (Exception ex) {
            System.out.println("EXPECTED exception" + ex);
        }
    }

    /**
     * @throws Exception
     */
    @Test
    public void testTopicForId() throws Exception {
        SNSController controller = new SNSController(fClient, "agsNotation");
        int[] totalSize = new int[1];
        

        // #use6Type (LOCATION) Frankfurt am Main
        // ---------------
        DetailedTopic[] topicsForId = controller.getTopicForId("https://sns.uba.de/gazetteer/GEMEINDE0641200000", "/location", "plugId", "de", totalSize);
        assertEquals( 1, topicsForId.length );
        DetailedTopic dt = topicsForId[0];

        assertNotNull(dt);
        assertEquals("https://sns.uba.de/gazetteer/GEMEINDE0641200000", dt.getTopicID());
        assertEquals("Frankfurt am Main", dt.getTitle());
        assertTrue(dt.getTopicNativeKey().indexOf("06412000") != -1);
        assertEquals("https://sns.uba.de/gazetteer/GEMEINDE0641200000", dt.getAdministrativeID());
        assertTrue(dt.getSummary().indexOf("Gemeinde") != -1);

        // ALWAYS empty definitions cause using GazetterService API
        String[] array = dt.getDefinitions();
        assertEquals(0, array.length);

        // ALWAYS empty definitionTitles cause using GazetterService API
        array = dt.getDefinitionTitles();
        assertEquals(0, array.length);

        /*
        // ALWAYS empty samples cause using GazetterService API
        array = dt.getSamples();
        assertEquals(0, array.length);

        // ALWAYS empty sampleTitles cause using GazetterService API
        array = dt.getSampleTitles();
        assertEquals(0, array.length);

        // NO associations cause using GazetterService API
        bla = (String) dt.get(DetailedTopic.ASSOCIATED_OCC);
        assertNull(bla);*/

        // NO descriptionOcc cause using GazetterService API
        String bla = (String) dt.get(DetailedTopic.DESCRIPTION_OCC);
        assertNull(bla);
    }

    /**
     * @throws Exception
     */
    @Test
    public void testGetDetails() throws Exception {
        SNSController controller = new SNSController(fClient, "agsNotation");
        Topic topic = new Topic();
        
        // #use6Type (LOCATION) Frankfurt am Main
        // ---------------
        topic.setTopicID("https://sns.uba.de/gazetteer/GEMEINDE0641200000");

        DetailedTopic dt = controller.getTopicDetail(topic, "/location", "de");
        assertNotNull(dt);
        assertEquals("https://sns.uba.de/gazetteer/GEMEINDE0641200000", dt.getTopicID());
        assertEquals("Frankfurt am Main", dt.getTitle());
        assertTrue(dt.getTopicNativeKey().indexOf("06412000") != -1);
        assertEquals("https://sns.uba.de/gazetteer/GEMEINDE0641200000", dt.getAdministrativeID());
        //assertTrue(dt.getSummary().indexOf("location-admin-use6") != -1);
        assertTrue(dt.getSummary().indexOf("Gemeinde") != -1);

        // ALWAYS empty definitions cause using GazetterService API
        String[] array = dt.getDefinitions();
        assertEquals(0, array.length);

        // ALWAYS empty definitionTitles cause using GazetterService API
        array = dt.getDefinitionTitles();
        assertEquals(0, array.length);

        // ALWAYS empty samples cause using GazetterService API
        array = dt.getSamples();
        assertEquals(0, array.length);

        // ALWAYS empty sampleTitles cause using GazetterService API
        array = dt.getSampleTitles();
        assertEquals(0, array.length);

        // NO associations cause using GazetterService API
        String bla = (String) dt.get(DetailedTopic.ASSOCIATED_OCC);
        assertNull(bla);

        // NO descriptionOcc cause using GazetterService API
        bla = (String) dt.get(DetailedTopic.DESCRIPTION_OCC);
        assertNull(bla);
    }

    /**
     * @throws Exception
     */
//    public void testGetAssociatedTopicsExpired() throws Exception {
//        SNSController controller = new SNSController(fClient, "agsNotation");
//        int[] totalSize = new int[1];
//        // WITH INTRODUCTION OF GAZETTEER API NEVER RETURNS EXPIRED ONES !!!
//        Topic[] topicsForTopic = controller.getTopicSimilarLocationsFromTopic("https://sns.uba.de/gazetteer/GEMEINDE0325300005", 1000, "aId",
//                totalSize, "de");
////                totalSize, false, "de");
//        assertNotNull(topicsForTopic);
//        assertEquals(1, topicsForTopic.length);
//
//        // WITH INTRODUCTION OF GAZETTEER API NEVER RETURNS EXPIRED ONES !!!
//        /*topicsForTopic = controller.getTopicSimilarLocationsFromTopic("https://sns.uba.de/gazetteer/GEMEINDE0325300005", 1000, "aId", totalSize,
//                "de");
////                true, "de");
//        assertNotNull(topicsForTopic);
//        */
//    }


    /**
     * @throws Exception
     */
    @Test
    public void testGetTopicFromText() throws Exception {
    	// test terms
        SNSController controller = new SNSController(fClient, "agsNotation");
        String text = "Waldsterben Weser Explosion";
        
    	int[] totalSize = null;
		// test locations
        DetailedTopic[] topics = controller.getTopicsForText(text, 100, "/location", "aPlugId", "de", totalSize, false);
        assertEquals(2, totalSize[0]);
        assertNotNull(topics);
        assertEquals(2, topics.length);
        assertEquals("https://sns.uba.de/gazetteer/FLUSS4", topics[0].getTopicNativeKey());
        assertEquals("https://sns.uba.de/gazetteer/WASSEREINZUGSGEBIET496", topics[1].getTopicNativeKey());

        // BUG: httpss://github.com/innoq/iqvoc_gazetteer/issues/13
        topics = controller.getTopicsForText("Frankfurt", 100, "/location", "aPlugId", "de", new int[1], false);
        assertNotNull(topics);
        assertEquals(4, topics.length);
        assertEquals("06412000", topics[0].getTopicNativeKey());
        assertEquals("12053000", topics[1].getTopicNativeKey());

    }


    /**
     * @throws Exception
     */
    @Test
    public void testGetTopicFromTextNoNativeKey() throws Exception {
        SNSController controller = new SNSController(fClient, "agsNotation");
        String text = "Weser";
        DetailedTopic[] topics = controller.getTopicsForText(text, 100, "aPlugId", "de", new int[1], false);
        assertNotNull(topics);
        assertEquals(3, topics.length);
        assertEquals("https://sns.uba.de/gazetteer/FLUSS4", topics[0].getTopicNativeKey()); //https://sns.uba.de/umthes/_00100789
        assertEquals("https://sns.uba.de/gazetteer/WASSEREINZUGSGEBIET496", topics[1].getTopicNativeKey());
    }

    /**
     * @throws Exception
     */
    @Test
    public void testGetSimilarLocationsFromTopicNativeKeyHasLawaPrefix() throws Exception {
        SNSController controller = new SNSController(fClient, "agsNotation");
        String topicId = "https://sns.uba.de/gazetteer/NATURRAUM583";
        Topic[] topics = controller.getTopicSimilarLocationsFromTopic(topicId, 100, "aPlugId", new int[1], "de");
        assertNotNull(topics);
        assertEquals(82, topics.length);
        for (int i = 0; i < topics.length; i++) {
            assertFalse(topics[i].getTopicNativeKey().startsWith("lawa:"), "Does contain 'lawa:'.");
        }
    }

    @Test
    public void testCheckNativeKeyIsArs() throws Exception {
        // test terms
        SNSController controller = new SNSController(fClient, "");
        DetailedTopic[] topics = controller.getTopicsForText("Frankfurt", 100, "/location", "aPlugId", "de", new int[1], false);
        assertNotNull(topics);
        assertEquals(4, topics.length);
        // ags
        assertNotEquals("06412000", topics[0].getTopicNativeKey());
        assertNotEquals("12053000", topics[1].getTopicNativeKey());
        // ars
        assertEquals("06412", topics[0].getTopicNativeKey());
        assertEquals("064120000000", topics[1].getTopicNativeKey());

    }

}
