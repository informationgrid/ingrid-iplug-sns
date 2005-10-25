/*
 * Copyright (c) 2003 by media style GmbH
 * 
 * $Source: /cvs/SiemensPI/ms_codetemplates.xml,v $
 */

package de.ingrid.iplug.sns;

import de.ingrid.iplug.sns.utils.Topic;
import de.ingrid.iplug.sns.utils.DetailedTopic;
import junit.framework.TestCase;

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
    private final static String VALID_TOPIC_ID = "uba_thes_19054";

    /**
     * @throws Exception
     */
    public void testTopicsForTerm() throws Exception {
        SNSClient client = new SNSClient("ms", "m3d1asyl3", "de");
        SNSController controller = new SNSController(client);
        Topic[] topicsForTerm = controller.getTopicsForTerm("Wasser", 23);
        assertTrue(topicsForTerm.length > 0);
        for (int i = 0; i < topicsForTerm.length; i++) {
            Topic topic = topicsForTerm[i];
            System.out.println(topic);
        }
    }

    /**
     * @throws Exception
     */
    public void testGetAssociatedTopics() throws Exception {
        SNSClient client = new SNSClient("ms", "m3d1asyl3", "de");
        SNSController controller = new SNSController(client);
        Topic[] topicsForTerm = controller.getTopicsForTopic("Wasser", 23);
        assertNull(topicsForTerm);
        topicsForTerm = controller.getTopicsForTopic(VALID_TOPIC_ID, 23);
        assertTrue(topicsForTerm.length > 0);
        for (int i = 0; i < topicsForTerm.length; i++) {
            Topic topic = topicsForTerm[i];
            System.out.println(topic);

        }
    }

    /**
     * @throws Exception
     */
    public void testGetDocumentRelatedTopics() throws Exception {
        SNSClient client = new SNSClient("ms", "m3d1asyl3", "de");
        SNSController controller = new SNSController(client);
        String text = "Tschernobyl liegt in Halle gefunden";
        DetailedTopic[] topics = controller.getTopicsForText(text, 100);
        assertNotNull(topics);

        text = "yyy xxx zzz";
        topics = controller.getTopicsForText(text, 100);
        assertNotNull(topics);
    }

}
