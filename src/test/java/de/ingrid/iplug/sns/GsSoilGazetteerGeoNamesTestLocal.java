/*
 * Copyright (c) 2010 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.sns;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;
import de.ingrid.external.GazetteerService;
import de.ingrid.external.GazetteerService.MatchingType;
import de.ingrid.external.GazetteerService.QueryType;
import de.ingrid.external.om.Location;
import de.ingrid.external.sns.SNSClient;
import de.ingrid.iplug.sns.utils.DetailedTopic;
import de.ingrid.iplug.sns.utils.Topic;
import de.ingrid.utils.tool.SpringUtil;

/**
 * Tests of GSSoil implementations of Thesaurus/Gazetteer/FullClassify APi !!!
 */
public class GsSoilGazetteerGeoNamesTestLocal extends TestCase {

    private static SNSClient fClient;

    String idLisbon = "2267057";
    String idPortugal = "2264397";
    String idPorto = "2735943";

    public void setSNSClient(SNSClient client) throws Exception {
        fClient = client;
    }

    protected void setUp() throws Exception {
        super.setUp();

        fClient = new SNSClient("ms", "m3d1asyl3", "de");
        fClient.setTimeout(180000);
    }

    public void testGazetteerDirectly() throws Exception {
        SpringUtil springUtil = new SpringUtil("spring/external-services.xml");
        final Class<GazetteerService> _gazetteerService = null;
        GazetteerService gazetteer = springUtil.getBean("gazetteerService", _gazetteerService);
        
        // getLocationsFromText
        // --------------------
        // NOTICE: service checks text on all languages, so passed language isn't necessary for finding location !
        // But found locations then are translated to passed language !

        // pass correct language OF TEXT ! returns Lisbon !
        Location[] locations = gazetteer.getLocationsFromText("Lisbon", 100, false, new Locale("en"));
        assertTrue(locations.length > 0);
        assertTrue(locations[0].getId().equals(idLisbon));
        assertTrue(locations[0].getName().equals("Lisbon"));

        // pass wrong language of text -> finds location ! service checks text in all languages !?
        // BUT RETURNS location in passed language !
        locations = gazetteer.getLocationsFromText("Lisbon", 100, false, new Locale("de"));
        assertTrue(locations.length > 0);
        assertTrue(locations[0].getId().equals(idLisbon));
        assertTrue(locations[0].getName().equals("Lissabon"));

        locations = gazetteer.getLocationsFromText("Lissabon", 100, false, new Locale("pt"));
        assertTrue(locations.length > 0);
        assertTrue(locations[0].getId().equals(idLisbon));
        assertTrue(locations[0].getName().equals("Lisboa"));

        // FINDET AUCH "Porto" als Ort, obwohl Brief Porto gemeint ist, da Text sprachenunabhängig analysiert !!!!! :(
        locations = gazetteer.getLocationsFromText("Brief Porto nach Lissabon, Portugal", 100, false, new Locale("de"));
        assertTrue(locations.length > 0);
        String[] names = new String[] { "Lissabon", "Portugal", "Porto"};
        List<String> nameList = Arrays.asList(names); 
        String[] ids = new String[] { idLisbon, idPortugal, idPorto};
        List<String> idList = Arrays.asList(ids);
        for (Location loc : locations) {
            assertTrue(idList.contains(loc.getId()));
            assertTrue(nameList.contains(loc.getName()));
        }

        // invalid text
        locations = gazetteer.getLocationsFromText("yyy xxx zzz", 100, false, new Locale("en"));
        assertTrue(locations.length == 0);

        // findLocationsFromQueryTerm
        // --------------------
        // NOTICE: Here passed Name of location and language must match, e.g. "Lisbon"<->"en" or "Lissabon"<->"de" ... 

        locations = gazetteer.findLocationsFromQueryTerm("Lisbon", QueryType.ALL_LOCATIONS, MatchingType.EXACT, new Locale("en"));
        assertTrue(locations.length > 0);
        assertTrue(locations[0].getId().equals(idLisbon));
        assertTrue(locations[0].getName().equals("Lisbon"));

        locations = gazetteer.findLocationsFromQueryTerm("Lisbon", QueryType.ALL_LOCATIONS, MatchingType.EXACT, new Locale("de"));
        assertTrue(locations.length == 0);
       
        locations = gazetteer.findLocationsFromQueryTerm("Lissabon", QueryType.ALL_LOCATIONS, MatchingType.CONTAINS, new Locale("de"));
        assertTrue(locations.length > 0);
        assertTrue(locations[0].getId().equals(idLisbon));
        assertTrue(locations[0].getName().equals("Lissabon"));

        locations = gazetteer.findLocationsFromQueryTerm("Lisboa", QueryType.ALL_LOCATIONS, MatchingType.BEGINS_WITH, new Locale("pt"));
        assertTrue(locations.length > 0);
        assertTrue(locations[0].getId().equals(idLisbon));
        assertTrue(locations[0].getName().equals("Lisboa"));

        // getLocation
        // --------------------
        // NOTICE: returns location name in passed language !

        Location location = gazetteer.getLocation(idLisbon, new Locale("en"));
        assertTrue(location != null);
        assertTrue(location.getId().equals(idLisbon));
        assertTrue(location.getName().equals("Lisbon"));
        location = gazetteer.getLocation(idLisbon, new Locale("de"));
        assertTrue(location != null);
        assertTrue(location.getId().equals(idLisbon));
        assertTrue(location.getName().equals("Lissabon"));

        location = gazetteer.getLocation(idPortugal, new Locale("pt"));
        assertTrue(location != null);
        assertTrue(location.getId().equals(idPortugal));
        assertTrue(location.getName().equals("Portugal"));

        location = gazetteer.getLocation(idPorto, new Locale("en"));
        assertTrue(location != null);
        assertTrue(location.getId().equals(idPorto));
        assertTrue(location.getName().equals("Porto"));

        // getRelatedLocationsFromLocation
        // --------------------
        // NOTICE: returns locations in passed language !

        // include location with passed id !
        locations = gazetteer.getRelatedLocationsFromLocation(idLisbon, true, new Locale("en"));
        assertTrue(locations.length > 0);
        assertTrue(locations[0].getId().equals(idLisbon));
        // do NOT include location with passed id !
        locations = gazetteer.getRelatedLocationsFromLocation(idLisbon, false, new Locale("de"));
        assertTrue(locations != null);
        if (locations.length > 0) {
            assertFalse(locations[0].getId().equals(idLisbon));        	
        }

        // include location with passed id !
        locations = gazetteer.getRelatedLocationsFromLocation(idPortugal, true, new Locale("en"));
        assertTrue(locations.length > 0);
        assertTrue(locations[0].getId().equals(idPortugal));
        // do NOT include location with passed id !
        locations = gazetteer.getRelatedLocationsFromLocation(idPortugal, false, new Locale("en"));
        assertTrue(locations != null);
        if (locations.length > 0) {
            assertFalse(locations[0].getId().equals(idPortugal));        	
        }
    }

    public void testGetDetails() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");
        Topic topic = new Topic();

        // GSSoil (LOCATION)
        // ---------------
        topic.setTopicID(idLisbon);

        DetailedTopic dt = controller.getTopicDetail(topic, "/location", "de");
        assertNotNull(dt);
        assertTrue(dt.getTopicID().indexOf(idLisbon) != -1);
        assertTrue(dt.getTitle().indexOf("Lissabon") != -1);

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

    public void testGetAssociatedTopics() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");
        int[] totalSize = new int[1];        
        Topic[] topics = null;
        
        // LOCATION
        topics = controller.getTopicsForTopic(idLisbon, 100, "/location", "aId", "de", totalSize, false);
        assertNotNull(topics);
        assertTrue(topics.length > 0);

        // LOCATION
        // "Lisboa"
        topics = controller.getTopicSimilarLocationsFromTopic(idLisbon, 100, "aId", totalSize, "de");
        assertTrue(topics.length > 0);
    }

    public void testGetTopicFromText() throws Exception {
    	// test terms
        SNSController controller = new SNSController(fClient, "ags:");
        String text = "Lisbon";
        int[] totalSize = new int[1];
        DetailedTopic[] topics;

    	// test locations, "de"
        topics = controller.getTopicsForText(text, 100, "/location", "aPlugId", "de", totalSize, false);
        assertTrue(topics.length > 0);

    	// test locations, "en"
        topics = controller.getTopicsForText(text, 100, "/location", "aPlugId", "en", totalSize, false);
        assertTrue(topics.length > 0);

    	// test invalid text
        topics = controller.getTopicsForText("yyy xxx zzz", 100, "/location", "aPlugId", "en", totalSize, false);
        assertTrue(topics.length == 0);

    	// test ALL TOPICS
        topics = controller.getTopicsForText(text, 100, null, "aPlugId", "de", totalSize, false);
        assertTrue(topics.length > 0);
    }

    public void testGetDocumentRelatedTopics() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");
        int[] totalSize = new int[1];
        DetailedTopic[] topics;
        
        String text = "soil water sun lisboa";

        // ALWAYS NO FILTER !
        topics = controller.getTopicsForText(text, 100, "aPlugId", "en", totalSize, false);
        assertNotNull(topics);
        assertTrue(topics.length > 0);

        // no results due to invalid text !
        topics = controller.getTopicsForText("yyy xxx zzz", 100, "aPlugId", "de", totalSize, false);
        assertNotNull(topics);
        assertEquals(0, topics.length);

        // valid URL
//        String url = "http://www.portalu.de";
        String url = "http://www.visitlisboa.com";

/*
        // NO FILTER !
        topics = controller.getTopicsForURL(url, maxWords, null, "aPlugId", "de", totalSize);
        assertNotNull(topics);
        int numAllTopics = topics.length;
		assertTrue(numAllTopics > 0);
*/
		// only locations
        topics = controller.getTopicsForURL(url, 1000, "/location", "aPlugId", "en", totalSize);
        assertNotNull(topics);
		assertTrue(topics.length > 0);
//		assertTrue(topics.length < numAllTopics);
    }
}
