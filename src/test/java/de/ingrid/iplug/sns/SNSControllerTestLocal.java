/*
 * **************************************************-
 * Ingrid iPlug SNS
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import de.ingrid.external.sns.SNSClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import de.ingrid.iplug.sns.utils.DetailedTopic;
import de.ingrid.iplug.sns.utils.Topic;

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
public class SNSControllerTestLocal {

    private static SNSClient fClient;

    private boolean fToStdout;

    private final static String VALID_TOPIC_ID = "https://sns.uba.de/umthes/_00040280";

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

        this.fToStdout = true;
    }

    /**
     * @throws Exception
     */
    @Test
    public void testTopicsForTerm() throws Exception {
        SNSController controller = new SNSController(fClient, "agsNotation");
        int[] totalSize = new int[1];
        // NOTICE: "Wasser" is LABEL topic !!!
        Topic[] topicsForTerm = controller.getTopicsForTerm("Wasser", 0, 1000, "aId", totalSize, "de", false, false);
        assertTrue( topicsForTerm.length > 30);
        for (int i = 0; i < topicsForTerm.length; i++) {
            Topic topic = topicsForTerm[i];
            if (this.fToStdout) {
                System.out.println(topic);
            }
        }

        // DESCRIPTOR topic !
        topicsForTerm = controller.getTopicsForTerm("Hydrosph\u00E4re", 0, 1000, "aId", totalSize, "de", false, false);
        assertEquals( 8, topicsForTerm.length );

        // case insensitive !!!
        topicsForTerm = controller.getTopicsForTerm("hydrosph\u00E4re", 0, 1000, "aId", totalSize, "de", false, false);
        assertEquals( 8, topicsForTerm.length );

        // NON DESCRIPTOR topic ! Here we do NOT get results !!!
        topicsForTerm = controller.getTopicsForTerm("Waldsterben", 0, 1000, "aId", totalSize, "de", false, false);
        assertEquals( 11, topicsForTerm.length );

        // TOP topic !!!
        topicsForTerm = controller.getTopicsForTerm("[Hydrosphäre - Wasser und Gewässer]", 0, 1000, "aId", totalSize, "de", false, false);
        assertTrue( topicsForTerm.length >= 8 );

        topicsForTerm = controller.getTopicsForTerm("no thesa topic available", 0, 1000, "aId", totalSize, "de", false, false);
        assertEquals( 0, topicsForTerm.length );
    }

    /**
     * @throws Exception
     */
    @Test
    public void testGetAssociatedTopics() throws Exception {
        SNSController controller = new SNSController(fClient, "agsNotation");
        int[] totalSize = new int[1];
        
        // THESA
        Topic[] topics = controller.getTopicsForTopic("Wasser", 23, "/thesa", "aId", "de", totalSize, false);
        assertNull(topics);
        topics = controller.getTopicsForTopic(VALID_TOPIC_ID, 23, "/thesa", "aId", "de", totalSize, false);
        assertEquals(23, topics.length);
        for (int i = 0; i < topics.length; i++) {
            Topic topic = topics[i];
            if (this.fToStdout) {
                System.out.println(topic);
            }
        }
    }

    /**
     * @throws Exception
     */
    @Disabled
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
        // #legalType (EVENT)
        // ---------------
        DetailedTopic[] topicsForId = controller.getTopicForId("https://sns.uba.de/chronik/t47098a_10220d1bc3e_4ee1", "/event", "plugId", "de", totalSize);
        assertEquals( 1, topicsForId.length );
        DetailedTopic dt = topicsForId[0];

        assertNotNull(dt);
        String[] array = dt.getDefinitions();
        /*assertEquals(1, array.length);
        System.out.println("Defs:");
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }

        array = dt.getDefinitionTitles();
        assertEquals(1, array.length);
        System.out.println("DefTit:");
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }*/

        /*array = dt.getSamples();
        assertEquals(2, array.length);
        System.out.println("Sam:");
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }

        array = dt.getSampleTitles();
        assertEquals(2, array.length);
        System.out.println("SamTit:");
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }

        System.out.println("Ass:");
        String bla = (String) dt.get(DetailedTopic.ASSOCIATED_OCC);
        System.out.println(bla);
		*/
        System.out.println("Des:");
        String bla = (String) dt.get(DetailedTopic.DESCRIPTION_OCC);
        System.out.println(bla);

        // #descriptorType (THESA) Waldschaden
        // ---------------
        topicsForId = controller.getTopicForId("https://sns.uba.de/umthes/_00027061", "/thesa", "plugId", "de", totalSize);
        assertEquals( 1, topicsForId.length );
        dt = topicsForId[0];

        assertNotNull(dt);
        assertEquals("https://sns.uba.de/umthes/_00027061", dt.getTopicID());
        assertEquals("Waldschaden", dt.getTitle());

        // ALWAYS empty definitions cause using ThesaurusService API
        array = dt.getDefinitions();
        assertEquals(0, array.length);

        // ALWAYS empty definitionTitles cause using ThesaurusService API
        array = dt.getDefinitionTitles();
        assertEquals(0, array.length);

        // ALWAYS empty samples cause using ThesaurusService API
        array = dt.getSamples();
        assertEquals(0, array.length);

        // ALWAYS empty sampleTitles cause using ThesaurusService API
        array = dt.getSampleTitles();
        assertEquals(0, array.length);

        // NO associations cause using ThesaurusService API
        bla = (String) dt.get(DetailedTopic.ASSOCIATED_OCC);
        assertNull(bla);

        // NO descriptionOcc cause using ThesaurusService API
        bla = (String) dt.get(DetailedTopic.DESCRIPTION_OCC);
        assertNull(bla);

    }

    /**
     * @throws Exception
     */
    @Test
    public void testGetDetails() throws Exception {
        SNSController controller = new SNSController(fClient, "agsNotation");
        Topic topic = new Topic();
        // #legalType (EVENT)
        // ---------------
        topic.setTopicID("https://sns.uba.de/chronik/t47098a_10220d1bc3e_4ee1");

        DetailedTopic dt = controller.getTopicDetail(topic, "de");

        String[] array = dt.getDefinitions();
        /*assertEquals(1, array.length);
        System.out.println("Defs:");
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }

        array = dt.getDefinitionTitles();
        assertEquals(1, array.length);
        System.out.println("DefTit:");
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }*/

        array = dt.getSamples();
        assertTrue(array.length >= 3);
        System.out.println("Sam:");
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }

        array = dt.getSampleTitles();
        assertTrue(array.length >= 3);
        System.out.println("SamTit:");
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }

        System.out.println("Ass:");
        String bla = (String) dt.get(DetailedTopic.ASSOCIATED_OCC);
        System.out.println(bla);

        System.out.println("Des:");
        bla = (String) dt.get(DetailedTopic.DESCRIPTION_OCC);
        System.out.println(bla);

        // #descriptorType (THESA) Waldschaden
        // ---------------
        topic.setTopicID("https://sns.uba.de/umthes/_00027061");

        dt = controller.getTopicDetail(topic, "/thesa", "de");
        assertNotNull(dt);
        assertEquals("https://sns.uba.de/umthes/_00027061", dt.getTopicID());
        assertEquals("Waldschaden", dt.getTitle());

        // ALWAYS empty definitions cause using ThesaurusService API
        array = dt.getDefinitions();
        assertEquals(0, array.length);

        // ALWAYS empty definitionTitles cause using ThesaurusService API
        array = dt.getDefinitionTitles();
        assertEquals(0, array.length);

        // ALWAYS empty samples cause using ThesaurusService API
        array = dt.getSamples();
        assertEquals(0, array.length);

        // ALWAYS empty sampleTitles cause using ThesaurusService API
        array = dt.getSampleTitles();
        assertEquals(0, array.length);

        // NO associations cause using ThesaurusService API
        bla = (String) dt.get(DetailedTopic.ASSOCIATED_OCC);
        assertNull(bla);

        // NO descriptionOcc cause using ThesaurusService API
        bla = (String) dt.get(DetailedTopic.DESCRIPTION_OCC);
        assertNull(bla);

    }

    /**
     * @throws Exception
     */
    @Test
    public void testGetHierachy() throws Exception {
        SNSController controller = new SNSController(fClient, "agsNotation");

        // toplevel
        String topicID = "toplevel";
        int[] totalSize = new int[1];
        Topic[] topicsHierachy = controller.getTopicHierachy(totalSize, "narrowerTermAssoc", 1, "down", false, "de",
                topicID, false, "pid");
        assertNotNull(topicsHierachy);
        assertEquals(1, topicsHierachy.length);
        System.out.println(topicsHierachy[0].getTopicID());
        // printHierachy(topicsHierachy[0].getSuccessors(), 1);

        // up
        topicID = "https://sns.uba.de/umthes/_00040282";
        topicsHierachy = controller.getTopicHierachy(totalSize, "narrowerTermAssoc", 5, "up", false, "de", topicID,
                false, "pid");
        assertNotNull(topicsHierachy);
        assertEquals(1, topicsHierachy.length);
        List<String> resultList = new ArrayList<String>();
        resultList.add(topicsHierachy[0].getTopicID());
        resultList.add(topicsHierachy[0].getTopicName());
        fill(topicsHierachy[0].getSuccessors(), resultList);

        assertTrue(resultList.contains("[Atmosph\u00E4re und Klima]"));
        assertTrue(resultList.contains("Luft"));
        assertTrue(resultList.contains("https://sns.uba.de/umthes/_00049251"));
        assertTrue(resultList.contains("https://sns.uba.de/umthes/_00040282"));

        // top node up
        topicID = "https://sns.uba.de/umthes/_00049251";
        topicsHierachy = controller.getTopicHierachy(totalSize, "narrowerTermAssoc", 5, "up", false, "de", topicID,
                false, "pid");
        assertNotNull(topicsHierachy);
        assertEquals(1, topicsHierachy.length);
        // return value is null !!!?
        assertNull(topicsHierachy[0]);

        // down
        topicID = "https://sns.uba.de/umthes/_00049251";
        topicsHierachy = controller.getTopicHierachy(totalSize, "narrowerTermAssoc", 2, "down", false, "de", topicID,
                false, "pid");
        assertNotNull(topicsHierachy);
        assertEquals(1, topicsHierachy.length);
        resultList = new ArrayList<String>();
        resultList.add(topicsHierachy[0].getTopicID());
        resultList.add(topicsHierachy[0].getTopicName());
        fill(topicsHierachy[0].getSuccessors(), resultList);

        assertTrue(resultList.contains("[Atmosph\u00E4re und Klima]"));
        assertTrue(resultList.contains("Luft"));
        assertTrue(resultList.contains("https://sns.uba.de/umthes/_00049251"));
        assertTrue(resultList.contains("https://sns.uba.de/umthes/_00040282"));

        // leaf down
        topicID = "https://sns.uba.de/umthes/de/concepts/_00040787"; // Kleinmenge
        topicsHierachy = controller.getTopicHierachy(totalSize, "narrowerTermAssoc", 2, "down", false, "de", topicID,
                false, "pid");
        assertNotNull(topicsHierachy);
        assertEquals(1, topicsHierachy.length);
        // return value is null !!!?
        assertNull(topicsHierachy[0]);
    }

    @Test
    public void testGetHierachyIncludeSiblings() throws Exception {
        SNSController controller = new SNSController(fClient, "agsNotation");

		// PATH OF SUB TERM in german
		// NOTICE: has 2 paths to top !
		// 1. uba_thes_13093 / uba_thes_47403 / uba_thes_47404 / uba_thes_49276
		// 2. uba_thes_13093 / uba_thes_13133 / uba_thes_49268
		String topicID = "https://sns.uba.de/umthes/_00013093"; // Immissionsdaten
//        String topicID = "uba_thes_27118";
        int[] totalSize = new int[1];
        Topic[] topicsHierachy = controller.getTopicHierachy(totalSize, "narrowerTermAssoc", 200, "up", true, "de",
                topicID, false, "pid");
        assertNotNull(topicsHierachy);
        // NOT VALID ANYMORE ! NEVER ADD SIBLINGS ! 
//        assertEquals(83, topicsHierachy.length);
        assertEquals(1, topicsHierachy.length);
        assertEquals(3, topicsHierachy[0].getSuccessors().size());
        List<String> resultList = new ArrayList<String>();
        fill(topicsHierachy[0].getSuccessors(), resultList);

        assertTrue(resultList.contains("Messergebnis [benutze Unterbegriffe]"));
        assertTrue(resultList.contains("Immissionssituation"));
        assertTrue(resultList.contains("https://sns.uba.de/umthes/_00047403"));
        assertTrue(resultList.contains("https://sns.uba.de/umthes/_00013133"));
    }

    /**
     * @throws Exception
     */
    @Test
    public void testGetSimilarTerms() throws Exception {
        SNSController controller = new SNSController(fClient, "agsNotation");
        int[] totalSize = new int[1];
        Topic[] topicsForTopic = controller.getSimilarTermsFromTopic("Abfall", 200, "pid", totalSize, "de");
        assertNotNull(topicsForTopic);
        assertTrue(topicsForTopic.length >= 4);
        // for (int i = 0; i < topicsForTopic.length; i++) {
        // System.out.println(topicsForTopic[i].getTopicID());
        // }
    }

    /**
     * @throws Exception
     */
    @Test
    public void testGetTopicFromText() throws Exception {
    	// test terms
        SNSController controller = new SNSController(fClient, "agsNotation");
        String text = "Waldsterben Weser Explosion";
        int[] totalSize = new int[1];
        DetailedTopic[] topics = controller.getTopicsForText(text, 100, "/thesa", "aPlugId", "de", totalSize, false);        
        assertEquals(3, totalSize[0]);
        assertNotNull(topics);
        assertEquals(3, topics.length);
        assertEquals("Waldschaden", topics[0].getTitle());
        assertEquals("Weser", topics[1].getTitle());
        //assertEquals("Explosion", topics[2].getTitle());

    	// test events
        // -> NOT SUPPORTED WITH INNOQ-SNS
        // topics = controller.getTopicsForText(text, 100, "/event", "aPlugId", "de", totalSize, false);
        // assertTrue(totalSize[0] > 0);
        // assertNotNull(topics);
        // assertTrue(topics.length > 0);
/*
        assertEquals("Chemieexplosion in Toulouse", topics[0].getTitle());
        assertEquals("Explosion im Stickstoffwerk Oppau", topics[1].getTitle());
        assertEquals("Kyschtym-Unfall von Majak", topics[2].getTitle());
*/
    	// test ALL TOPICS
        topics = controller.getTopicsForText(text, 100, null, "aPlugId", "de", totalSize, false);
        assertTrue(totalSize[0] >= 4);
        assertNotNull(topics);
        assertTrue(topics.length >= 4);
    }


    private void printHierachy(Set successors, int tab) {
		for (Object object : successors) {
			for (int j = 0; j < tab; j++) {
				System.out.print(' ');
			}
			Topic topic = (Topic) object;
			System.out.println(topic.getTopicID());
			printHierachy(topic.getSuccessors(), tab + 1);
		}
	}

    private void fill(Set<Topic> topicsHierachy, List<String> resultList) {
    	for (Topic topic : topicsHierachy) {
			resultList.add(topic.getTopicID());
			resultList.add(topic.getTopicName());
			fill(topic.getSuccessors(), resultList);
		}
    }
}
