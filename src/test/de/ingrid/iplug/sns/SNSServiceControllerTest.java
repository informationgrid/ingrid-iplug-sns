/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: DispatcherTest.java,v $
 */
package de.ingrid.iplug.sns;

import junit.framework.TestCase;
import de.ingrid.iplug.sns.utils.DocumentMetaData;
import de.ingrid.iplug.sns.utils.Topic;

/**
 * 
 * created on 21.07.2005 <p>
 *
 * @author hs
 */
public class SNSServiceControllerTest extends TestCase {
    private static SNSServiceController controller = null;
    private final static String VALID_QUERY = "Sauerstoff";
    private final static String INVALID_QUERY = "xyz";
    private final static String VALID_TOPIC_ID = "uba_thes_19054";
    private final static String INVALID_TOPIC_ID = "uvw_xyz";
    
    static{
        try {
            SNSServiceAdapter adapter = new SNSServiceAdapter("ms", "m3d1asyl3", "de");
            controller = new SNSServiceController(adapter);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        
    }
    
    /**
     * @throws Exception
     */
    
    public void testAnalyzeQuery() throws Exception{
        Topic[] topicResult = controller.analyzeQuery(VALID_QUERY, 20 );
        assertNotNull(topicResult);
        topicResult = controller.analyzeQuery(VALID_QUERY, 0);
        assertNotNull(topicResult);
        assertEquals(0, topicResult.length);
        topicResult = controller.analyzeQuery(INVALID_QUERY, 20);
        assertNotNull(topicResult);
    }
    
    /**
     * @throws Exception
     */
    
    public void testBrowseThesaurus() throws Exception{
        Topic[] topicResult = controller.browseThesaurus(VALID_TOPIC_ID, 20);
        assertNotNull(topicResult);
        topicResult = controller.browseThesaurus(VALID_TOPIC_ID, 0);
        assertNotNull(topicResult);
        assertEquals(0, topicResult.length);
        assertNotNull(controller.browseThesaurus(INVALID_TOPIC_ID, 20));
    }
    
    /**
     * 
     * @throws Exception
     */
    public void testGetDocumentRelatedTopics() throws Exception{
        String text = "Tschernobyl liegt in Halle gefunden";
        DocumentMetaData[] topics = controller.getDocumentRelatedTopics(text, 100);
        assertNotNull(topics);
        
        text = "yyy xxx zzz";
        topics = controller.getDocumentRelatedTopics(text, 100);
        assertNotNull(topics);
        
        
        
        
    }
}
