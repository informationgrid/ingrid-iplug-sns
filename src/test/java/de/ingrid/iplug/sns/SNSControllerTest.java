/*
 * Copyright (c) 2003 by media style GmbH
 * 
 * $Source: /cvs/SiemensPI/ms_codetemplates.xml,v $
 */

package de.ingrid.iplug.sns;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import de.ingrid.external.sns.SNSClient;
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
public class SNSControllerTest extends TestCase {

    private static SNSClient fClient;

    private boolean fToStdout;

    private final static String VALID_TOPIC_ID = "uba_thes_19054";

    /**
     * @param client
     * @throws Exception
     */
    public void setSNSClient(SNSClient client) throws Exception {
        fClient = client;
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        fClient = new SNSClient("ms", "m3d1asyl3", "de");
        fClient.setTimeout(180000);

        this.fToStdout = true;
    }

    /**
     * @throws Exception
     */
    public void testTopicsForTerm() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");
        Topic[] topicsForTerm = controller.getTopicsForTerm("Wasser", 0, 1000, "aId", new int[1], "de", false, false);
        assertTrue(topicsForTerm.length > 0);
        for (int i = 0; i < topicsForTerm.length; i++) {
            Topic topic = topicsForTerm[i];
            if (this.fToStdout) {
                System.out.println(topic);
            }
        }
    }

    /**
     * @throws Exception
     */
    public void testGetAssociatedTopics() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");
        Topic[] topicsForTerm = controller.getTopicsForTopic("Wasser", 23, "aId", new int[1], false);
        assertNull(topicsForTerm);
        topicsForTerm = controller.getTopicsForTopic(VALID_TOPIC_ID, 23, "aId", new int[1], false);
        assertTrue(topicsForTerm.length > 0);
        for (int i = 0; i < topicsForTerm.length; i++) {
            Topic topic = topicsForTerm[i];
            if (this.fToStdout) {
                System.out.println(topic);
            }
        }
    }

    /**
     * @throws Exception
     */
    public void testGetDocumentRelatedTopics() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");
        String text = "Tschernobyl liegt in Halle gefunden";
        DetailedTopic[] topics = controller.getTopicsForText(text, 100, "aPlugId", "de", new int[1], false);
        assertNotNull(topics);
        assertEquals(10, topics.length);

        text = "yyy xxx zzz";
        topics = controller.getTopicsForText(text, 100, "aPlugId", "de", new int[1], false);
        assertNotNull(topics);
        assertEquals(0, topics.length);
    }

    /**
     * @throws Exception
     */
    public void testGetDetails() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");
        Topic topic = new Topic();
        topic.setTopicID("t47098a_10220d1bc3e_4ee1");

        DetailedTopic dt = controller.getTopicDetail(topic, "de");

        String[] array = dt.getDefinitions();
        assertEquals(1, array.length);
        System.out.println("Defs:");
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }

        array = dt.getDefinitionTitles();
        assertEquals(1, array.length);
        System.out.println("DefTit:");
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }

        array = dt.getSamples();
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

        System.out.println("Des:");
        bla = (String) dt.get(DetailedTopic.DESCRIPTION_OCC);
        System.out.println(bla);
    }

    /**
     * @throws Exception
     */
    public void testGetAssociatedTopicsExpired() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");
        int[] totalSize = new int[1];
        Topic[] topicsForTopic = controller.getTopicSimilarLocationsFromTopic("GEMEINDE0325300005", 1000, "aId",
                totalSize, false);
        assertNotNull(topicsForTopic);
        assertEquals(9, topicsForTopic.length);

        topicsForTopic = controller.getTopicSimilarLocationsFromTopic("GEMEINDE0325300005", 1000, "aId", totalSize,
                true);
        assertNotNull(topicsForTopic);
        assertEquals(17, topicsForTopic.length);
    }

    /**
     * @throws Exception
     */
    public void testGetHierachy() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");

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
        topicID = "uba_thes_40282";
        topicsHierachy = controller.getTopicHierachy(totalSize, "narrowerTermAssoc", 5, "up", false, "de", topicID,
                false, "pid");
        assertNotNull(topicsHierachy);
        assertEquals(1, topicsHierachy.length);
        List<String> resultList = new ArrayList<String>();
        resultList.add(topicsHierachy[0].getTopicID());
        resultList.add(topicsHierachy[0].getTopicName());
        fill(topicsHierachy[0].getSuccessors(), resultList);

        assertTrue(resultList.contains("Atmosph\u00E4re und Klima"));
        assertTrue(resultList.contains("Luft"));
        assertTrue(resultList.contains("uba_thes_49251"));
        assertTrue(resultList.contains("uba_thes_40282"));

        // top node up
        topicID = "uba_thes_49251";
        topicsHierachy = controller.getTopicHierachy(totalSize, "narrowerTermAssoc", 5, "up", false, "de", topicID,
                false, "pid");
        assertNotNull(topicsHierachy);
        assertEquals(1, topicsHierachy.length);
        // return value is null !!!?
        assertNull(topicsHierachy[0]);

        // down
        topicID = "uba_thes_49251";
        topicsHierachy = controller.getTopicHierachy(totalSize, "narrowerTermAssoc", 2, "down", false, "de", topicID,
                false, "pid");
        assertNotNull(topicsHierachy);
        assertEquals(1, topicsHierachy.length);
        resultList = new ArrayList<String>();
        resultList.add(topicsHierachy[0].getTopicID());
        resultList.add(topicsHierachy[0].getTopicName());
        fill(topicsHierachy[0].getSuccessors(), resultList);

        assertTrue(resultList.contains("Atmosph\u00E4re und Klima"));
        assertTrue(resultList.contains("Luft"));
        assertTrue(resultList.contains("uba_thes_49251"));
        assertTrue(resultList.contains("uba_thes_40282"));

        // leaf down
        topicID = "uba_thes_40787"; // Kleinmenge
        topicsHierachy = controller.getTopicHierachy(totalSize, "narrowerTermAssoc", 2, "down", false, "de", topicID,
                false, "pid");
        assertNotNull(topicsHierachy);
        assertEquals(1, topicsHierachy.length);
        // return value is null !!!?
        assertNull(topicsHierachy[0]);
    }

    public void testGetHierachyIncludeSiblings() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");

		// PATH OF SUB TERM in german
		// NOTICE: has 2 paths to top !
		// 1. uba_thes_13093 / uba_thes_47403 / uba_thes_47404 / uba_thes_49276
		// 2. uba_thes_13093 / uba_thes_13133 / uba_thes_49268
		String topicID = "uba_thes_13093"; // Immissionsdaten
//        String topicID = "uba_thes_27118";
        int[] totalSize = new int[1];
        Topic[] topicsHierachy = controller.getTopicHierachy(totalSize, "narrowerTermAssoc", 200, "up", true, "de",
                topicID, false, "pid");
        assertNotNull(topicsHierachy);
        // NOT VALID ANYMORE ! NEVER ADD SIBLINGS ! 
//        assertEquals(83, topicsHierachy.length);
        assertEquals(1, topicsHierachy.length);
        assertEquals(2, topicsHierachy[0].getSuccessors().size());
        List<String> resultList = new ArrayList<String>();
        fill(topicsHierachy[0].getSuccessors(), resultList);

        assertTrue(resultList.contains("Messergebnis"));
        assertTrue(resultList.contains("Immissionssituation"));
        assertTrue(resultList.contains("uba_thes_47403"));
        assertTrue(resultList.contains("uba_thes_13133"));
    }

    /**
     * @throws Exception
     */
    public void testGetSimilarTerms() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");
        int[] totalSize = new int[1];
        Topic[] topicsForTopic = controller.getSimilarTermsFromTopic("Abfall", 200, "pid", totalSize, "de");
        assertNotNull(topicsForTopic);
        assertEquals(24, topicsForTopic.length);
        // for (int i = 0; i < topicsForTopic.length; i++) {
        // System.out.println(topicsForTopic[i].getTopicID());
        // }
    }

    /**
     * @throws Exception
     */
    public void testGetTopicFromTextNoNativeKey() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");
        String text = "Wesertal";
        DetailedTopic[] topics = controller.getTopicsForText(text, 100, "aPlugId", "de", new int[1], false);
        assertNotNull(topics);
        assertEquals(2, topics.length);
        assertEquals("NATURRAUM583", topics[0].getTopicNativeKey());
        assertEquals("NATURRAUM620", topics[1].getTopicNativeKey());
    }

    /**
     * @throws Exception
     */
    public void testGetSimilarLocationsFromTopicNativeKeyHasLawaPrefix() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");
        String text = "NATURRAUM583";
        Topic[] topics = controller.getTopicSimilarLocationsFromTopic(text, 100, "aPlugId", new int[1], false);
        assertNotNull(topics);
        assertEquals(90, topics.length);
        for (int i = 0; i < topics.length; i++) {
            assertTrue("Does contain 'lawa:'.", !topics[i].getTopicNativeKey().startsWith("lawa:"));
        }
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
