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

        fClient = new SNSClient("ms", "portalu2006", "de");

        this.fToStdout = true;
    }

    /**
     * @throws Exception
     */
    public void testTopicsForTerm() throws Exception {
        SNSController controller = new SNSController(fClient);
        Topic[] topicsForTerm = controller.getTopicsForTerm("Wasser", 0, 1000, "aId");
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
        Topic[] topicsForTerm = controller.getTopicsForTopic("Wasser", 23, "aId");
        assertNull(topicsForTerm);
        topicsForTerm = controller.getTopicsForTopic(VALID_TOPIC_ID, 23,"aId");
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
        DetailedTopic[] topics = controller.getTopicsForText(text, 100, "aPlugId");
        assertNotNull(topics);

        text = "yyy xxx zzz";
        topics = controller.getTopicsForText(text, 100, "aPlugId");
        assertNotNull(topics);
    }
}
