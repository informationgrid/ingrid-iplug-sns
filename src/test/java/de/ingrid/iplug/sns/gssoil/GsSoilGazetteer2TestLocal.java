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

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import de.ingrid.external.GazetteerService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import de.ingrid.external.GazetteerService.MatchingType;
import de.ingrid.external.GazetteerService.QueryType;
import de.ingrid.external.om.Location;
import de.ingrid.external.sns.SNSClient;
import de.ingrid.iplug.sns.SNSController;
import de.ingrid.iplug.sns.utils.DetailedTopic;
import de.ingrid.iplug.sns.utils.Topic;
import de.ingrid.utils.tool.SpringUtil;

/**
 * Tests of GSSoil implementations of Thesaurus/Gazetteer/FullClassify APi !!!
 */
public class GsSoilGazetteer2TestLocal {

    private static SNSClient fClient;

    String idLisbon = "2267057";
    String idPortugal = "2264397";
    String idPorto = "2735943";
    String idBerlin = "2950157";
    String idDeutschland = "2921044";

    public void setSNSClient(SNSClient client) throws Exception {
        fClient = client;
    }

    @BeforeEach
    public void setUp() throws Exception {

        fClient = new SNSClient("ms", "m3d1asyl3", "de");
        fClient.setTimeout(180000);
    }

    @Test
    public void testGazetteerDirectly() throws Exception {
        SpringUtil springUtil = new SpringUtil("spring/external-services.xml");
        final Class<GazetteerService> _gazetteerService = null;
        GazetteerService gazetteer = springUtil.getBean("gazetteerService", _gazetteerService);
        
        // getLocationsFromText
        // --------------------
        // NOTICE: Locations are now checked in passed language !  

        // pass correct language OF TEXT ! returns Lisbon !
        Location[] locations = gazetteer.getLocationsFromText("Lisbon", 100, false, new Locale("en"));
        assertTrue(locations.length > 0);
        assertEquals(locations[0].getId(), idLisbon);
        assertEquals(locations[0].getName(), "Lisbon");

        // pass wrong language of text -> no results !
        locations = gazetteer.getLocationsFromText("Lisbon", 100, false, new Locale("de"));
        assertEquals(locations.length, 0);

        locations = gazetteer.getLocationsFromText("Lisboa", 100, false, new Locale("pt"));
        assertTrue(locations.length > 0);
        assertEquals(locations[0].getId(), idLisbon);
        assertEquals(locations[0].getName(), "Lisboa");

        locations = gazetteer.getLocationsFromText("Berlin", 100, false, new Locale("de"));
        assertTrue(locations.length > 0);
        assertTrue(locations[0].getName().contains("Berlin"));

        locations = gazetteer.getLocationsFromText("ruhlsdorf", 100, false, new Locale("de"));
        assertTrue(locations.length > 0);
        
        locations = gazetteer.getLocationsFromText("Brief Porto nach Berlin, Deutschland", 100, false, new Locale("de"));
        assertTrue(locations.length > 1);

        // invalid text
        locations = gazetteer.getLocationsFromText("yyy xxx zzz", 100, false, new Locale("en"));
        assertEquals(locations.length, 0);

        // findLocationsFromQueryTerm
        // --------------------
        // NOTICE: Passed Name of location and language must match, e.g. "Lisbon"<->"en" or "Lisboa"<->"pt" ... 

        locations = gazetteer.findLocationsFromQueryTerm("Lisbon", QueryType.ALL_LOCATIONS, MatchingType.EXACT, new Locale("en"));
        assertTrue(locations.length > 0);
        assertEquals(locations[0].getId(), idLisbon);
        assertEquals(locations[0].getName(), "Lisbon");

        locations = gazetteer.findLocationsFromQueryTerm("Lisbon", QueryType.ALL_LOCATIONS, MatchingType.EXACT, new Locale("de"));
        assertEquals(locations.length, 0);

        locations = gazetteer.findLocationsFromQueryTerm("Berlin", QueryType.ALL_LOCATIONS, MatchingType.CONTAINS, new Locale("de"));
        assertTrue(locations.length > 0);
        assertTrue(locations[0].getName().contains("Berlin"));

        locations = gazetteer.findLocationsFromQueryTerm("Lisboa", QueryType.ALL_LOCATIONS, MatchingType.BEGINS_WITH, new Locale("pt"));
        assertTrue(locations.length > 0);
        assertEquals(locations[0].getId(), idLisbon);
        assertEquals(locations[0].getName(), "Lisboa");

        locations = gazetteer.findLocationsFromQueryTerm("ruhlsdorf", QueryType.ALL_LOCATIONS, MatchingType.CONTAINS, new Locale("de"));
        assertTrue(locations.length > 0);

        // getLocation
        // --------------------
        // NOTICE: returns location name in passed language !

        Location location = gazetteer.getLocation(idLisbon, new Locale("en"));
        assertTrue(location != null);
        assertEquals(location.getId(), idLisbon);
        assertEquals(location.getName(), "Lisbon");

        location = gazetteer.getLocation(idBerlin, new Locale("de"));
        assertTrue(location != null);
        assertEquals(location.getId(), idBerlin);
        assertEquals(location.getName(), "Berlin");

        location = gazetteer.getLocation(idPortugal, new Locale("pt"));
        assertTrue(location != null);
        assertEquals(location.getId(), idPortugal);
        assertEquals(location.getName(), "Portugal");

        location = gazetteer.getLocation(idPorto, new Locale("en"));
        assertTrue(location != null);
        assertEquals(location.getId(), idPorto);
        assertEquals(location.getName(), "Porto");

        // getRelatedLocationsFromLocation
        // --------------------
        // NOTICE: returns locations in passed language !

        // include location with passed id !
        locations = gazetteer.getRelatedLocationsFromLocation(idLisbon, true, new Locale("en"));
        assertTrue(locations.length > 0);
        assertEquals(locations[0].getId(), idLisbon);
        // do NOT include location with passed id !
        locations = gazetteer.getRelatedLocationsFromLocation(idLisbon, false, new Locale("de"));
        assertTrue(locations != null);
        if (locations.length > 0) {
            assertNotEquals(locations[0].getId(), idLisbon);        	
        }

        // include location with passed id !
        locations = gazetteer.getRelatedLocationsFromLocation(idPortugal, true, new Locale("en"));
        assertTrue(locations.length > 0);
        assertEquals(locations[0].getId(), idPortugal);
        // do NOT include location with passed id !
        locations = gazetteer.getRelatedLocationsFromLocation(idPortugal, false, new Locale("en"));
        assertTrue(locations != null);
        if (locations.length > 0) {
            assertNotEquals(locations[0].getId(), idPortugal);        	
        }
    }

    @Test
    public void testGetDetails() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");
        Topic topic = new Topic();

        // GSSoil (LOCATION)
        // ---------------
        topic.setTopicID(idLisbon);

        DetailedTopic dt = controller.getTopicDetail(topic, "/location", "en");
        assertNotNull(dt);
        assertTrue(dt.getTopicID().indexOf(idLisbon) != -1);
        assertTrue(dt.getTitle().indexOf("Lisbon") != -1);

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

    @Test
    public void testGetAssociatedTopics() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");
        int[] totalSize = new int[1];        
        Topic[] topics = null;
        
        // LOCATION
        topics = controller.getTopicsForTopic(idLisbon, 100, "/location", "aId", "en", totalSize, false);
        assertNotNull(topics);
        assertTrue(topics.length > 0);

        // LOCATION
        // "Lisboa"
        topics = controller.getTopicSimilarLocationsFromTopic(idLisbon, 100, "aId", totalSize, "en");
        assertTrue(topics.length > 0);
    }

    @Test
    public void testGetTopicFromText() throws Exception {
    	// test terms
        SNSController controller = new SNSController(fClient, "ags:");
        int[] totalSize = new int[1];
        DetailedTopic[] topics;

    	// test locations, "de"
        topics = controller.getTopicsForText("Berlin Deutschland", 100, "/location", "aPlugId", "de", totalSize, false);
        assertTrue(topics.length > 0);

    	// test locations, "en"
        topics = controller.getTopicsForText("Lisbon", 100, "/location", "aPlugId", "en", totalSize, false);
        assertTrue(topics.length > 0);

    	// test invalid text
        topics = controller.getTopicsForText("yyy xxx zzz", 100, "/location", "aPlugId", "en", totalSize, false);
        assertEquals(topics.length, 0);

    	// test ALL TOPICS
        topics = controller.getTopicsForText("Lisbon", 100, null, "aPlugId", "en", totalSize, false);
        assertTrue(topics.length > 0);
    }

    @Test
    public void testGetDocumentRelatedTopics() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");
        int[] totalSize = new int[1];
        DetailedTopic[] topics;
        
        String text = "soil water sun lisbon";

        // ALWAYS NO FILTER !
        topics = controller.getTopicsForText(text, 100, "aPlugId", "en", totalSize, false);
        assertNotNull(topics);
        assertTrue(topics.length > 0);

        // no results due to invalid text !
        topics = controller.getTopicsForText("yyy xxx zzz", 100, "aPlugId", "de", totalSize, false);
        assertNotNull(topics);
        assertEquals(0, topics.length);

/*
        // NO FILTER !
        topics = controller.getTopicsForURL("http://www.visitlisboa.com", maxWords, null, "aPlugId", "de", totalSize);
        assertNotNull(topics);
        int numAllTopics = topics.length;
		assertTrue(numAllTopics > 0);
*/
		// only locations
        topics = controller.getTopicsForURL("http://www.visitlisboa.com", 1000, "/location", "aPlugId", "pt", totalSize);
        assertNotNull(topics);
		assertTrue(topics.length > 0);
    }
}
