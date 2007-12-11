/*
 * Copyright (c) 2003 by media style GmbH
 * 
 * $Source: /cvs/SiemensPI/ms_codetemplates.xml,v $
 */

package de.ingrid.iplug.sns;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
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
        Topic[] topicsForTerm = controller.getTopicsForTerm("Wasser", 0, 1000, "aId", new int[1], "de", false);
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
        assertEquals(8, topics.length);

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

        String topicID = "uba_thes_40282";
        int[] totalSize = new int[1];
        Topic[] topicsForTopic = controller.getTopicHierachy(totalSize, "narrowerTermAssoc", 1000, "down", false, "de",
                topicID, false, "pid");
        assertNotNull(topicsForTopic);
        assertEquals(406, topicsForTopic.length);
        for (int i = 0; i < topicsForTopic.length; i++) {
            System.out.println("ti:" + topicsForTopic[i].getTopicID());
            System.out.println("tn:" + topicsForTopic[i].getTopicName());
            System.out.println("ta:" + topicsForTopic[i].getTopicAssoc());
            System.out.println("tk:" + topicsForTopic[i].getTopicNativeKey());
            System.out.println("tt:" + topicsForTopic[i].getTitle());
            System.out.println("ts:" + topicsForTopic[i].getSummary());
            System.out.println("---");
        }
        topicsForTopic = controller.getTopicHierachy(totalSize, "narrowerTermAssoc", 1000, "up", false, "de", topicID,
                false, "pid");
        assertNotNull(topicsForTopic);
        assertEquals(2, topicsForTopic.length);
        List resultList = new ArrayList();
        for (int i = 0; i < topicsForTopic.length; i++) {
            resultList.add(topicsForTopic[i].getTopicAssoc());
            resultList.add(topicsForTopic[i].getTopicName());
            resultList.add(topicsForTopic[i].getTopicID());
        }
        assertTrue(resultList.contains("Atmosphäre und Klima"));
        assertTrue(resultList.contains("Luft"));
        assertTrue(resultList
                .contains("http://www.semantic-network.de/xmlns/XTM/2005/2.0/sns-classes_2.0.xtm#widerTermMember"));
        assertTrue(resultList
                .contains("http://www.semantic-network.de/xmlns/XTM/2005/2.0/sns-classes_2.0.xtm#narrowerTermMember"));
        assertTrue(resultList.contains("uba_thes_49251"));
        assertTrue(resultList.contains("uba_thes_40282"));
    }
}
