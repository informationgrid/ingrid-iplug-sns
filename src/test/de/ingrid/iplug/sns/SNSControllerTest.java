/*
 * Copyright (c) 2003 by media style GmbH
 * 
 * $Source: /cvs/SiemensPI/ms_codetemplates.xml,v $
 */

package de.ingrid.iplug.sns;

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

    private static SNSClient fClient = null;

    private boolean fToStdout = false;

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
        SNSController controller = new SNSController(fClient);
        Topic[] topicsForTerm = controller.getTopicsForTerm("Wasser", 0, 1000, "aId", new int[1], "de");
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
        SNSController controller = new SNSController(fClient);
        Topic[] topicsForTerm = controller.getTopicsForTopic("Wasser", 23, "aId", new int[1]);
        assertNull(topicsForTerm);
        topicsForTerm = controller.getTopicsForTopic(VALID_TOPIC_ID, 23, "aId", new int[1]);
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
        SNSController controller = new SNSController(fClient);
        String text = "Tschernobyl liegt in Halle gefunden";
        DetailedTopic[] topics = controller.getTopicsForText(text, 100, "aPlugId", "de", new int[1]);
        assertNotNull(topics);

        text = "yyy xxx zzz";
        topics = controller.getTopicsForText(text, 100, "aPlugId", "de", new int[1]);
        assertNotNull(topics);
    }
    
    /**
     * @throws Exception
     */
    public void testGetDetails() throws Exception {
        SNSController controller = new SNSController(fClient);
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
}
