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
 * Copyright (c) 2010 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.sns.gssoil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import de.ingrid.external.ThesaurusService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import de.ingrid.external.ThesaurusService.MatchingType;
import de.ingrid.external.om.RelatedTerm;
import de.ingrid.external.om.Term;
import de.ingrid.external.om.TreeTerm;
import de.ingrid.external.sns.SNSClient;
import de.ingrid.iplug.sns.SNSController;
import de.ingrid.iplug.sns.utils.DetailedTopic;
import de.ingrid.iplug.sns.utils.Topic;
import de.ingrid.utils.tool.SpringUtil;

/**
 * Tests of GSSoil implementations of Thesaurus/Gazetteer/FullClassify APi !!!
 * GEMET VERSION !!!
 */
public class GsSoilThesaurusTestLocal {

    private static SNSClient fClient;

    private boolean fToStdout;

    private final static String VALID_TOPIC_ID = "http://www.eionet.europa.eu/gemet/concept/8167";

    public void setSNSClient(SNSClient client) throws Exception {
        fClient = client;
    }

    @BeforeEach
    public void setUp() throws Exception {

        fClient = new SNSClient("ms", "m3d1asyl3", "de");
        fClient.setTimeout(180000);

        this.fToStdout = true;
    }

    @Test
    public void testThesaurusDirectly() throws Exception {
        SpringUtil springUtil = new SpringUtil("spring/external-services.xml");
        final Class<ThesaurusService> _thesaurusService = null;
        ThesaurusService thesaurus = springUtil.getBean("thesaurusService", _thesaurusService);

        // getTerm
        // --------------------
        String topicID = "http://www.eionet.europa.eu/gemet/concept/7843"; // Boden
        Term term = thesaurus.getTerm(topicID, new Locale("en"));
        assertTrue(term != null);
        assertEquals(term.getName(), "soil");

        // getHierarchyNextLevel
        // --------------------
        TreeTerm[] treeTerms = thesaurus.getHierarchyNextLevel(null, new Locale("en"));
        assertTrue(treeTerms != null);
        assertTrue(treeTerms.length > 0);

        topicID = "http://www.eionet.europa.eu/gemet/supergroup/5499";
        treeTerms = thesaurus.getHierarchyNextLevel(topicID, new Locale("en"));
        assertTrue(treeTerms.length > 0);

        // getHierarchyPathToTop
        // --------------------
        topicID = "http://www.eionet.europa.eu/gemet/concept/7843"; // Boden
        TreeTerm treeTerm = thesaurus.getHierarchyPathToTop(topicID, new Locale("en"));
        assertNotNull(treeTerm);

        // findTermsFromQueryTerm
        // --------------------     
        Term[] terms = thesaurus.findTermsFromQueryTerm("Soil", MatchingType.EXACT, false, new Locale("en"));
        assertNotNull(terms);
        assertTrue(terms.length > 0);

        // getRelatedTermsFromTerm
        // --------------------     
        RelatedTerm[] relTerms = thesaurus.getRelatedTermsFromTerm(topicID, new Locale("en"));
        assertNotNull(relTerms);
        assertTrue(relTerms.length > 0);

        // getSimilarTermsFromNames
        // --------------------
        String[] names = new String[] { "soil", "water" };
        terms = thesaurus.getSimilarTermsFromNames(names, true, new Locale("en"));
        assertNotNull(terms);
        assertTrue(terms.length > 0);

        // getTermsFromText
        // --------------------
        terms = thesaurus.getTermsFromText("soil water lisbon", 10, true, new Locale("en"));
        assertNotNull(terms);
        assertTrue(terms.length > 0);
    }

    @Test
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
    }

    @Test
    public void testTopicForId() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");
        int[] totalSize = new int[1];

        String id = "http://www.eionet.europa.eu/gemet/supergroup/5499";
        DetailedTopic[] topicsForId = controller.getTopicForId(id, "/thesa", "plugId", "en", totalSize);

        assertEquals(topicsForId.length, 1);
        DetailedTopic dt = topicsForId[0];

        assertNotNull(dt);
        assertEquals(id, dt.getTopicID());
        assertEquals("NATURAL ENVIRONMENT, ANTHROPIC ENVIRONMENT", dt.getTitle());

        // ALWAYS empty definitions cause using ThesaurusService API
        String[] array = dt.getDefinitions();
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
    }

    @Test
    public void testTopicsForTerm() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");
        int[] totalSize = new int[1];

        // DESCRIPTOR topic !
        Topic[] topicsForTerm = controller.getTopicsForTerm("subsoil", 0, 1000, "aId", totalSize, "en", false, false);
        assertEquals(topicsForTerm.length, 3);

        // case insensitive !!!
        topicsForTerm = controller.getTopicsForTerm("SUBsoiL", 0, 1000, "aId", totalSize, "en", false, false);
        assertEquals(topicsForTerm.length, 3);

        // TOP topic !!!
        topicsForTerm = controller.getTopicsForTerm("ACCESSORY LISTS", 0, 1000, "aId", totalSize, "en", false, false);
        assertEquals(topicsForTerm.length, 2);

        topicsForTerm = controller.getTopicsForTerm("no thesa topic available", 0, 1000, "aId", totalSize, "en", false, false);
        assertEquals(topicsForTerm.length, 0);

        // ONLY "Boden" CONCEPT topic = http://www.eionet.europa.eu/gemet/concept/7843 !!!
        // No themes: http://www.eionet.europa.eu/gemet/theme/35, http://inspire.jrc.it/theme/16
        // so only stuff part of thesaurus tree is returned !
        topicsForTerm = controller.getTopicsForTerm("Boden", 0, 1000, "aId", totalSize, "de", false, false);
        // NOTICE: SNS Controller fetches concept topic AND THEN ASSOCIATED TOPICS !!! So theres a further getRelatedTermsFromTerm executed in controller !  
//        assertTrue(topicsForTerm.length == 1);
//        assertEquals("http://www.eionet.europa.eu/gemet/concept/7843", topicsForTerm[0].getTopicID());
        assertTrue(topicsForTerm.length > 0);
    }

    @Test
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
		assertTrue(topics.length > 0);
    }

    @Test
    public void testGetDocumentRelatedTopics() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");
        int[] totalSize = new int[1];
        String text = "soil water sun";
        DetailedTopic[] topics = controller.getTopicsForText(text, 100, "aPlugId", "en", totalSize, false);
        assertNotNull(topics);
		assertTrue(topics.length > 0);

        text = "yyy xxx zzz";
        topics = controller.getTopicsForText(text, 100, "aPlugId", "de", totalSize, false);
        assertNotNull(topics);
        assertEquals(0, topics.length);

        // valid URL
        // NO FILTER -> SNS !
        String url = "http://www.portalu.de";
        int maxWords = 200;
/*
        topics = controller.getTopicsForURL(url, maxWords, null, "aPlugId", "de", totalSize);
        assertNotNull(topics);
        int numAllTopics = topics.length;
		assertTrue(numAllTopics > 0);
*/
		// only thesa -> GS Soil
        topics = controller.getTopicsForURL(url, maxWords, "/thesa", "aPlugId", "de", totalSize);
        assertNotNull(topics);
		assertTrue(topics.length > 0);
//		assertTrue(topics.length < numAllTopics);

		// only events -> SNS 
        topics = controller.getTopicsForURL(url, maxWords, "/event", "aPlugId", "de", totalSize);
        assertNotNull(topics);
        assertEquals(0, topics.length);

		// INVALID URL
        url = "http://www.partalu.de";
        try {
            topics = controller.getTopicsForURL(url, maxWords, "/thesa", "aPlugId", "de", totalSize);        	
        } catch (Exception ex) {
            System.out.println("EXPECTED exception" + ex);
        }
        url = "htp://www.portalu .de";
        try {
            topics = controller.getTopicsForURL(url, maxWords, "/thesa", "aPlugId", "de", totalSize);
        } catch (Exception ex) {
            System.out.println("EXPECTED exception" + ex);
        }
    }

    @Test
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

    @Test
    public void testGetSimilarTerms() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");
        int[] totalSize = new int[1];
        Topic[] topicsForTopic = controller.getSimilarTermsFromTopic("water", 200, "pid", totalSize, "en");
        assertNotNull(topicsForTopic);
        assertTrue(topicsForTopic.length > 0);
    }

    @Test
    public void testGetTopicFromText() throws Exception {
    	// test terms
        SNSController controller = new SNSController(fClient, "ags:");
        String text = "soil water sun";
        int[] totalSize = new int[1];
        DetailedTopic[] topics = controller.getTopicsForText(text, 100, "/thesa", "aPlugId", "en", totalSize, false);        
        assertTrue(totalSize[0] > 0);
        assertNotNull(topics);
        assertTrue(topics.length > 0);
//        assertEquals("alkali soil", topics[0].getTitle());
//        assertEquals("contaminated soil", topics[1].getTitle());

    	// test terms
        // check INSPIRE Themes !
        text = "Boden Bodennutzung";
        topics = controller.getTopicsForText(text, 100, "/thesa", "aPlugId", "de", totalSize, false);        
        int numTopics = topics.length;
        assertTrue(totalSize[0] > 0);
        assertNotNull(topics);
        assertTrue(topics.length > 0);

        text = "Bodennutzung Boden";
        topics = controller.getTopicsForText(text, 100, "/thesa", "aPlugId", "de", totalSize, false);        
        assertTrue(totalSize[0] > 0);
        assertNotNull(topics);
        assertTrue(topics.length > 0);
        assertEquals(numTopics, topics.length);

    	// "invalid" text. NO TERMS !
        text = "yyy xxx zzz";
        topics = controller.getTopicsForText(text, 100, "/thesa", "aPlugId", "de", totalSize, false);
        assertEquals(totalSize[0], 0);
        assertNotNull(topics);
        assertEquals(topics.length, 0);

    	// test events WITH en !
        topics = controller.getTopicsForText(text, 100, "/event", "aPlugId", "en", totalSize, false);
        assertNotNull(topics);
//        assertEquals(0, topics.length);
//        assertEquals(2, topics.length);

    	// test ALL TOPICS
        text = "Wasser";
        topics = controller.getTopicsForText(text, 100, null, "aPlugId", "de", totalSize, false);
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
