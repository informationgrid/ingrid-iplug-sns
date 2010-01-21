/*
 * Copyright (c) 2010 wemove digital solutions. All rights reserved.
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
 * Tests of GSSoil implementations of Thesaurus/Gazetteer/FullClassify APi !!!
 */
public class GSSoilControllerTestLocal extends TestCase {

    private static SNSClient fClient;

    private boolean fToStdout;

    private final static String VALID_TOPIC_ID = "http://www.eionet.europa.eu/gemet/concept/8167";

    public void setSNSClient(SNSClient client) throws Exception {
        fClient = client;
    }

    protected void setUp() throws Exception {
        super.setUp();

        fClient = new SNSClient("ms", "m3d1asyl3", "de");
        fClient.setTimeout(180000);

        this.fToStdout = true;
    }

    public void testGetDetails() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");
        Topic topic = new Topic();
        // #legalType (EVENT)
        // ---------------
        topic.setTopicID("t47098a_10220d1bc3e_4ee1");

        DetailedTopic dt = controller.getTopicDetail(topic, "de");

        String[] array = dt.getDefinitions();
        assertEquals(1, array.length);

        array = dt.getDefinitionTitles();
        assertEquals(1, array.length);

        array = dt.getSamples();
        assertEquals(2, array.length);

        array = dt.getSampleTitles();
        assertEquals(2, array.length);

        // (THESA)
        // ---------------
        String id = "http://www.eionet.europa.eu/gemet/supergroup/5499";
        topic.setTopicID(id);

        dt = controller.getTopicDetail(topic, "/thesa", "en");
        assertNotNull(dt);
        assertEquals(id, dt.getTopicID());
        assertEquals("NATURAL ENVIRONMENT, ANTHROPIC ENVIRONMENT", dt.getTitle());

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
        String bla = (String) dt.get(DetailedTopic.ASSOCIATED_OCC);
        assertNull(bla);

        // NO descriptionOcc cause using ThesaurusService API
        bla = (String) dt.get(DetailedTopic.DESCRIPTION_OCC);
        assertNull(bla);

        // #use6Type (LOCATION) Frankfurt am Main
        // ---------------
        topic.setTopicID("GEMEINDE0641200000");

        dt = controller.getTopicDetail(topic, "/location", "de");
        assertNotNull(dt);
        assertEquals("GEMEINDE0641200000", dt.getTopicID());
        assertEquals("Frankfurt am Main", dt.getTitle());
        assertTrue(dt.getTopicNativeKey().indexOf("06412000") != -1);
        assertEquals("GEMEINDE0641200000", dt.getAdministrativeID());
        assertTrue(dt.getSummary().indexOf("use6Type") != -1);

        // GSSoil (LOCATION)
        // ---------------
/*
        topic.setTopicID("Berlin");

        dt = controller.getTopicDetail(topic, "/location", "de");
        if (dt == null) {
        	return;
        }
        assertNotNull(dt);
        assertTrue(dt.getTopicID().indexOf("Berlin") != -1);
        assertTrue(dt.getTitle().indexOf("Berlin") != -1);
*/
        // ALWAYS empty definitions cause using GazetterService API
        array = dt.getDefinitions();
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
        bla = (String) dt.get(DetailedTopic.ASSOCIATED_OCC);
        assertNull(bla);

        // NO descriptionOcc cause using GazetterService API
        bla = (String) dt.get(DetailedTopic.DESCRIPTION_OCC);
        assertNull(bla);
    }

    public void testTopicsForTerm() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");
        int[] totalSize = new int[1];

        // DESCRIPTOR topic !
        Topic[] topicsForTerm = controller.getTopicsForTerm("subsoil", 0, 1000, "aId", totalSize, "en", false, false);
        assertTrue(topicsForTerm.length == 3);

        // case insensitive !!!
        topicsForTerm = controller.getTopicsForTerm("SUBsoiL", 0, 1000, "aId", totalSize, "en", false, false);
        assertTrue(topicsForTerm.length == 3);

        // TOP topic !!!
        topicsForTerm = controller.getTopicsForTerm("ACCESSORY LISTS", 0, 1000, "aId", totalSize, "en", false, false);
        assertTrue(topicsForTerm.length == 2);

        topicsForTerm = controller.getTopicsForTerm("no thesa topic available", 0, 1000, "aId", totalSize, "en", false, false);
        assertTrue(topicsForTerm.length == 0);
    }

    public void testGetAssociatedTopics() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");
        int[] totalSize = new int[1];
        
        // THESA
        Topic[] topics = null;
        
        // TODO:
        // INDEX OUT OF BOUNDS !
//        topics = controller.getTopicsForTopic("subsoil", 23, "/thesa", "aId", "en", totalSize, false);
//        assertNull(topics);
        
        topics = controller.getTopicsForTopic(VALID_TOPIC_ID, 23, "/thesa", "aId", "en", totalSize, false);
        assertEquals(3, topics.length);

        // LOCATION
		String locationId = "GEMEINDE0641200000"; // Frankfurt am Main
        topics = controller.getTopicsForTopic(locationId, 23, "/location", "aId", "de", totalSize, false);
        assertEquals(23, topics.length);

        // LOCATION
        topics = controller.getTopicSimilarLocationsFromTopic(locationId, 23, "aId", totalSize, "de");
        assertEquals(23, topics.length);
    }

    public void testGetDocumentRelatedTopics() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");
        int[] totalSize = new int[1];
        String text = "soil water sun";
        DetailedTopic[] topics = controller.getTopicsForText(text, 100, "aPlugId", "en", totalSize, false);
        assertNotNull(topics);
//        assertEquals(0, topics.length);
//        assertEquals(2, topics.length);

        text = "yyy xxx zzz";
        topics = controller.getTopicsForText(text, 100, "aPlugId", "de", totalSize, false);
        assertNotNull(topics);
        assertEquals(0, topics.length);

        // valid URL
        String url = "http://www.portalu.de";
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
		assertTrue(topics.length > 0);
		assertTrue(topics.length < numAllTopics);

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

    public void testGetHierachy() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");

        // toplevel
        String topicID = "toplevel";
        int[] totalSize = new int[1];
        Topic[] topicsHierachy = controller.getTopicHierachy(totalSize, "narrowerTermAssoc", 1, "down", false, "en",
                topicID, false, "pid");
        assertNotNull(topicsHierachy);
        assertEquals(1, topicsHierachy.length);
        System.out.println(topicsHierachy[0].getTopicID());
        // printHierachy(topicsHierachy[0].getSuccessors(), 1);

        // up
        topicID = "http://www.eionet.europa.eu/gemet/concept/11007";
        topicsHierachy = controller.getTopicHierachy(totalSize, "narrowerTermAssoc", 5, "up", false, "en", topicID,
                false, "pid");
        assertNotNull(topicsHierachy);
        assertEquals(1, topicsHierachy.length);
        List<String> resultList = new ArrayList<String>();
        resultList.add(topicsHierachy[0].getTopicID());
        resultList.add(topicsHierachy[0].getTopicName());
        fill(topicsHierachy[0].getSuccessors(), resultList);

        assertTrue(resultList.contains("public function"));
        assertTrue(resultList.contains("ADMINISTRATION, MANAGEMENT, POLICY, POLITICS, INSTITUTIONS, PLANNING"));
        assertTrue(resultList.contains("http://www.eionet.europa.eu/gemet/concept/95"));
        assertTrue(resultList.contains("http://www.eionet.europa.eu/gemet/supergroup/2894"));

        // top node up
        topicID = "http://www.eionet.europa.eu/gemet/supergroup/5306";
        topicsHierachy = controller.getTopicHierachy(totalSize, "narrowerTermAssoc", 5, "up", false, "en", topicID,
                false, "pid");
        assertNotNull(topicsHierachy);
        assertEquals(1, topicsHierachy.length);
        // return value is null !!!?
        assertNull(topicsHierachy[0]);

        // down
        topicID = "http://www.eionet.europa.eu/gemet/supergroup/5499";
        topicsHierachy = controller.getTopicHierachy(totalSize, "narrowerTermAssoc", 2, "down", false, "en", topicID,
                false, "pid");
        assertNotNull(topicsHierachy);
        assertEquals(1, topicsHierachy.length);
        resultList = new ArrayList<String>();
        resultList.add(topicsHierachy[0].getTopicID());
        resultList.add(topicsHierachy[0].getTopicName());
        fill(topicsHierachy[0].getSuccessors(), resultList);

        assertTrue(resultList.contains("ATMOSPHERE (air, climate)"));
        assertTrue(resultList.contains("TIME (chronology)"));
        assertTrue(resultList.contains("http://www.eionet.europa.eu/gemet/group/4856"));
        assertTrue(resultList.contains("http://www.eionet.europa.eu/gemet/group/1062"));

        // leaf down
        topicID = "http://www.eionet.europa.eu/gemet/group/14979"; // TIME (chronology)
        topicsHierachy = controller.getTopicHierachy(totalSize, "narrowerTermAssoc", 2, "down", false, "en", topicID,
                false, "pid");
        assertNotNull(topicsHierachy);
        assertEquals(1, topicsHierachy.length);
        // return value is null !!!?
        assertNull(topicsHierachy[0]);
    }

    public void testGetSimilarTerms() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");
        int[] totalSize = new int[1];
        Topic[] topicsForTopic = controller.getSimilarTermsFromTopic("water", 200, "pid", totalSize, "en");
        assertNotNull(topicsForTopic);
        assertEquals(170, topicsForTopic.length);
    }

    public void testGetTopicFromText() throws Exception {
    	// test terms
        SNSController controller = new SNSController(fClient, "ags:");
        String text = "soil water sun";
        int[] totalSize = new int[1];
        DetailedTopic[] topics = controller.getTopicsForText(text, 100, "/thesa", "aPlugId", "en", totalSize, false);        
        assertEquals(236, totalSize[0]);
        assertNotNull(topics);
        assertEquals(236, topics.length);
        assertEquals("alkali soil", topics[0].getTitle());
        assertEquals("contaminated soil", topics[1].getTitle());

    	// test locations
        topics = controller.getTopicsForText(text, 100, "/location", "aPlugId", "en", totalSize, false);
        assertEquals(0, totalSize[0]);
        assertNotNull(topics);
        assertEquals(0, topics.length);

        topics = controller.getTopicsForText("Frankfurt", 100, "/location", "aPlugId", "en", new int[1], false);
        assertNotNull(topics);
        assertEquals(4, topics.length);
        assertEquals("06412000", topics[0].getTopicNativeKey());
        assertEquals("12053000", topics[1].getTopicNativeKey());

    	// test events
        topics = controller.getTopicsForText(text, 100, "/event", "aPlugId", "en", totalSize, false);
        assertNotNull(topics);
//        assertEquals(0, topics.length);
//        assertEquals(2, topics.length);

    	// test ALL TOPICS
        topics = controller.getTopicsForText(text, 100, null, "aPlugId", "en", totalSize, false);
        assertNotNull(topics);
//        assertEquals(0, topics.length);
//      assertEquals(2, topics.length);
    }

    private void fill(Set<Topic> topicsHierachy, List<String> resultList) {
    	for (Topic topic : topicsHierachy) {
			resultList.add(topic.getTopicID());
			resultList.add(topic.getTopicName());
			fill(topic.getSuccessors(), resultList);
		}
    }
}
