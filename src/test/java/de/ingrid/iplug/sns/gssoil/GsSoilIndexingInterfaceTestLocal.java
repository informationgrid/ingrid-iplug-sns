/*
 * Copyright (c) 1997-2006 by media style GmbH
 */

package de.ingrid.iplug.sns.gssoil;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.external.FullClassifyService;
import de.ingrid.external.FullClassifyService.FilterType;
import de.ingrid.external.om.FullClassifyResult;
import de.ingrid.external.om.Location;
import de.ingrid.external.om.Term;
import de.ingrid.iplug.sns.SNSIndexingInterface;
import de.ingrid.iplug.sns.Temporal;
import de.ingrid.iplug.sns.Wgs84Box;
import de.ingrid.utils.tool.SpringUtil;

/**
 * 
 */
public class GsSoilIndexingInterfaceTestLocal extends TestCase {

    private static Log log = LogFactory.getLog(GsSoilIndexingInterfaceTestLocal.class);

    private SNSIndexingInterface fSnsInterface;

    private boolean fToStdout;
    private String text = "Wasser Boden Fluss Baden-Württemberg berlin frankfurt Bavaria Rhineland-Palatinate Arzberg Thuringia Barmstedt Bremen Bernkastel-Kues Bitterfeld-Wolfen Delbrück Eisenhüttenstadt Flöha Fürth";

    protected void setUp() throws Exception {
        super.setUp();

        this.fSnsInterface = new SNSIndexingInterface("ms", "m3d1asyl3", "de");
        this.fSnsInterface.setTimeout(180000);

        this.fToStdout = true;
    }

    /**
     * @param iinterface
     * @throws Exception
     */
    public void setSNSIndexingInterface(SNSIndexingInterface iinterface) throws Exception {
        this.fSnsInterface = iinterface;
    }

    public void testFullClassifyDirectly() throws Exception {
        SpringUtil springUtil = new SpringUtil("spring/external-services.xml");
        final Class<FullClassifyService> _fullClassifyService = null;
        FullClassifyService fullClassify = springUtil.getBean("fullClassifyService", _fullClassifyService);
        
        // autoClassifyText
        // --------------------
        FullClassifyResult res = fullClassify.autoClassifyText("lisbon soil", 10, true, FilterType.ONLY_TERMS, new Locale("en"));
        assertTrue(res.getTerms() != null);
        assertTrue(res.getTerms().size() > 0);
        res = fullClassify.autoClassifyText("lisbon soil", 10, true, FilterType.ONLY_LOCATIONS, new Locale("en"));
        assertTrue(res.getLocations() != null);
        assertTrue(res.getLocations().size() > 0);
        res = fullClassify.autoClassifyText("lisbon soil", 10, true, null, new Locale("en"));
        assertTrue(res.getTerms() != null);
        assertTrue(res.getTerms().size() > 0);
        assertTrue(res.getLocations() != null);
        assertTrue(res.getLocations().size() > 0);

        // autoClassifyURL
        // --------------------
//        String url = "http://www.visitlisboa.com";
        String url = "http://www.berlin.de/";

        log.info("START -> autoClassifyURL 1000 ONLY_TERMS ignoreCase=true 'en' " + url);
        res = fullClassify.autoClassifyURL(new URL(url), 1000, true, FilterType.ONLY_TERMS, new Locale("en"));
        log.info("END -> autoClassifyURL " + url);
        assertTrue(res.getTerms() != null);
        assertTrue(res.getTerms().size() > 0);
        log.info("res.getTerms().size() " + res.getTerms().size());
        log.info("res.getTerms() " + getTermListOutput(res.getTerms()));
        System.out.println();

        // ignore case
        log.info("START -> autoClassifyURL 1000 ONLY_LOCATIONS ignoreCase=true 'pt' " + url);
        res = fullClassify.autoClassifyURL(new URL(url), 1000, true, FilterType.ONLY_LOCATIONS, new Locale("pt"));
        log.info("END -> autoClassifyURL " + url);
        assertTrue(res.getLocations() != null);
        assertTrue(res.getLocations().size() > 0);
        log.info("res.getLocations().size() " + res.getLocations().size());
        log.info("res.getLocations() " + getLocationListOutput(res.getLocations()));
        System.out.println();

        // IGNORE CASE, results with "de"
        log.info("START -> autoClassifyURL 1000 ONLY_LOCATIONS ignoreCase=true 'de' " + url);
        res = fullClassify.autoClassifyURL(new URL(url), 1000, true, FilterType.ONLY_LOCATIONS, new Locale("de"));
        log.info("END -> autoClassifyURL " + url);
        assertTrue(res.getLocations() != null);
        assertTrue(res.getLocations().size() > 0);
        log.info("res.getLocations().size() " + res.getLocations().size());
        log.info("res.getLocations() " + getLocationListOutput(res.getLocations()));
        System.out.println();

        // DO NOT IGNORE CASE, results with "de"
        log.info("START -> autoClassifyURL 1000 ONLY_LOCATIONS ignoreCase=FALSE 'de' " + url);
        res = fullClassify.autoClassifyURL(new URL(url), 1000, false, FilterType.ONLY_LOCATIONS, new Locale("de"));
        log.info("END -> autoClassifyURL " + url);
        assertTrue(res.getLocations() != null);
        assertTrue(res.getLocations().size() > 0);
        log.info("res.getLocations().size() " + res.getLocations().size());
        log.info("res.getLocations() " + getLocationListOutput(res.getLocations()));
        System.out.println();

        // DO NOT IGNORE CASE, NO results with "en" (only uppercase "Berlin" on page !
        log.info("START -> autoClassifyURL 1000 ONLY_LOCATIONS ignoreCase=FALSE 'en' " + url);
        res = fullClassify.autoClassifyURL(new URL(url), 1000, false, FilterType.ONLY_LOCATIONS, new Locale("en"));
        log.info("END -> autoClassifyURL " + url);
        assertTrue(res.getLocations() != null);
//        assertTrue(res.getLocations().size() == 0);
        log.info("res.getLocations().size() " + res.getLocations().size());
        log.info("res.getLocations() " + getLocationListOutput(res.getLocations()));
        System.out.println();

        // get all in "en", IGNORE CASE
        log.info("START -> autoClassifyURL 1000 ALL ignoreCase=true 'en' " + url);
        res = fullClassify.autoClassifyURL(new URL(url), 1000, true, null, new Locale("en"));
        log.info("END -> autoClassifyURL " + url);
        assertTrue(res.getTerms() != null);
        assertTrue(res.getTerms().size() > 0);
        assertTrue(res.getLocations() != null);
        assertTrue(res.getLocations().size() > 0);
        log.info("res.getTerms().size() " + res.getTerms().size());
        log.info("res.getTerms() " + getTermListOutput(res.getTerms()));
        log.info("res.getLocations().size() " + res.getLocations().size());
        log.info("res.getLocations() " + getLocationListOutput(res.getLocations()));
        System.out.println();

        // get all in "de"
        log.info("START -> autoClassifyURL 1000 ALL ignoreCase=true 'de' " + url);
        res = fullClassify.autoClassifyURL(new URL(url), 1000, true, null, new Locale("de"));
        log.info("END -> autoClassifyURL " + url);
        assertTrue(res.getTerms() != null);
        assertTrue(res.getTerms().size() > 0);
        assertTrue(res.getLocations() != null);
        assertTrue(res.getLocations().size() > 0);
        log.info("res.getTerms().size() " + res.getTerms().size());
        log.info("res.getTerms() " + getTermListOutput(res.getTerms()));
        log.info("res.getLocations().size() " + res.getLocations().size());
        log.info("res.getLocations() " + getLocationListOutput(res.getLocations()));
        System.out.println();
    }

    /**
     * @throws Exception
     */
    public void testGetReferencesToSpace() throws Exception {
        this.fSnsInterface.getBuzzwords("Berlin", 1000, false);

        final Wgs84Box[] result = this.fSnsInterface.getReferencesToSpace();
        assertNotNull(result);
        assertTrue(result.length > 0);

        for (int i = 0; i < result.length; i++) {
            System.out.println(result[i].getTopicName());
            System.out.println("x1:" + result[i].getX1());
            System.out.println("y1:" + result[i].getY1());
            System.out.println("x2:" + result[i].getX2());
            System.out.println("y2:" + result[i].getY2());
            System.out.println("Gemeindekennziffer: " + result[i].getGemeindekennziffer());
            System.out.println();
        }
    }

    /**
     * @throws Exception
     */
    public void testGetReferencesToSpaceBundesland() throws Exception {
        this.fSnsInterface.getBuzzwords("Bayern", 1000, false);

        final Wgs84Box[] result = this.fSnsInterface.getReferencesToSpace();
        assertNotNull(result);
        assertTrue(result.length > 0);

        for (int i = 0; i < result.length; i++) {
            System.out.println(result[i].getTopicName());
            System.out.println(result[i].getX1());
            System.out.println(result[i].getX2());
            System.out.println(result[i].getY1());
            System.out.println(result[i].getY2());
            System.out.println("Gemeindekennziffer: " + result[i].getGemeindekennziffer());
        }
    }

    /**
     * @throws Exception
     */
    public void testGetLocations() throws Exception {
        this.fSnsInterface.getBuzzwords("Berlin Deutschland", 1000, false);

        Set<String> locations = fSnsInterface.getLocations();
        for (String location : locations) {
            System.out.println("location (de): " + location);
        }
        assertTrue(locations.size() > 0);

        this.fSnsInterface.getBuzzwords("Lisbon Porto", 1000, false, "en");

        locations = fSnsInterface.getLocations();
        for (String location : locations) {
            System.out.println("location (en): " + location);
        }
        assertTrue(locations.size() > 0);
    }

    /**
     * @throws Exception
     */
    public void testGetReferencesToTime() throws Exception {
        this.fSnsInterface.getBuzzwords("Tschernobyl Ohio", 1000, false);

        final Temporal[] result = this.fSnsInterface.getReferencesToTime();
        // NO REFERENCES TO EVENTS IN GS SOIL !!!
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    /**
     * @throws Exception
     */
    public void testGetBuzzword() throws Exception {
        String[] result = null;
        final String words = "Waldsterben Wesertal Explosion. "
        		+ "In diesem Jahr können sich kleine und mittlere Unternehmen bis zum 15. "
                + "August 2006 bewerben. Eine aus Vertretern von Wissenschaft, Wirtschaft und mittelständischen "
                + "Anwenderunternehmen besetzte Jury wird bis zu drei Bewerber aus den Kategorien E-Business, Breitband und "
                + "Mobilität auswählen und mit Preisen in Höhe von je 25.000 Euro auszeichnen. Die Preisverleihung findet im "
                + "Rahmen des 2. Deutschen ITK-Mittelstandstages im November 2006 statt Für die Verwendung der "
                + "Ein-Ausgabe-Klassen muss das Package java.io importiert werden Wir haben bereits gelernt, wie die Ein- und "
                + "Ausgabe in Graphischen User-Interfaces programmiert wird. Nun wollen wir uns auch damit beschäftigen, wie wir "
                + "Daten von Dateien einlesen und in Dateien speichern können. Wir haben bereits gelernt, wie die Ein- und "
                + "Ausgabe in Graphischen User-Interfaces programmiert wird.";

        final long start = System.currentTimeMillis();
        try {
            result = this.fSnsInterface.getBuzzwords(words, 1000, false);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        final long end = System.currentTimeMillis();

        if (this.fToStdout) {
            final String output = "Time for getting all buzzwords: " + ((end - start) / 1000) + " s";
            System.out.println(output);
        }
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    /**
     * @throws Exception
     */
    public void testGetBuzzwordToUrl() throws Exception {
        // VALID URL GERMAN
        String[] result = null;
        String url = "http://www.portalu.de/";
        long start = System.currentTimeMillis();
        try {
            result = this.fSnsInterface.getBuzzwordsToUrl(url, 1000, false, "de");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        long end = System.currentTimeMillis();

        if (this.fToStdout) {
            final String output = "Time for getting all buzzwords: " + ((end - start) / 1000) + " s";
            System.out.println(output);
        }
        assertNotNull(result);
        assertTrue(result.length > 0);

    	// VALID URL ENGLISH
        url = "http://www.bbc.com/";
        try {
            result = this.fSnsInterface.getBuzzwordsToUrl(url, 1000, false, "en");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        assertNotNull(result);
        assertTrue(result.length > 0);

        // NONEXISTENT URL, but correct URL syntax !
        url = "http://www.partalu.de/";
        result = null;
        try {
            result = this.fSnsInterface.getBuzzwordsToUrl(url, 1000, false, "de");
        } catch (Exception e) {
        	System.out.println(e);
        }
//        assertNull(result);
        assertNotNull(result);
        assertTrue(result.length == 0);


    	// INVALID URL SYNTAX !
        url = "htp://www.portalu.de/";
        result = null;
        try {
            result = this.fSnsInterface.getBuzzwordsToUrl(url, 1000, false, "de");
        } catch (Exception e) {
        	System.out.println(e);
        }
        assertNull(result);
    }

    /**
     * @throws Exception
     */
    public void testGetBuzzwordEnglish() throws Exception {
        String[] result = null;
        final String words = "In this year we are all happy. Tschernobyl Lisbon Soil";
        final long start = System.currentTimeMillis();
        try {
            result = this.fSnsInterface.getBuzzwords(words, 1000, false, "en");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        final long end = System.currentTimeMillis();

        if (this.fToStdout) {
            final String output = "Time for getting all english buzzwords: " + ((end - start) / 1000) + " s";
            System.out.println(output);
        }

        assertNotNull(result);
        assertTrue(result.length > 0);
        
        for (int i = 0; i < result.length; i++) {
            System.out.println(result[i]);
        }
    }

    /**
     * @throws Exception
     */
    public void testGetBuzzwordNotExistent() throws Exception {
        final String[] result = this.fSnsInterface.getBuzzwords("blabla", 1000, false);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    /**
     * @throws Exception
     */
    public void testGetReferencesNotExistent() throws Exception {
        this.fSnsInterface.getBuzzwords("blabla", 1000, false);

        final Temporal[] result = this.fSnsInterface.getReferencesToTime();
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    /**
     * @throws Exception
     */
    public void testGetTopicIds() throws Exception {
//        this.fSnsInterface.getBuzzwordsToUrl("http://www.portalu.de/", 1000, false, "de");
    	// JUST USE 10 ! 1000 takes "hours" !
        this.fSnsInterface.getBuzzwordsToUrl("http://www.portalu.de/", 10, false, "de");

        final String[] result = this.fSnsInterface.getTopicIds();
        assertNotNull(result);
        // sometimes fails ?
        assertTrue(result.length > 0);
    }

    public void testUrlAllResults() throws Exception {
        // VALID URL GERMAN
        String[] resultStrings = null;
        String url = "http://www.berlin.de/";

        long start = System.currentTimeMillis();
        try {
            resultStrings = this.fSnsInterface.getBuzzwordsToUrl(url, 1000, false, "de");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        long end = System.currentTimeMillis();

        System.out.println();
        System.out.println("Analyzed: " + url);
        if (this.fToStdout) {
            System.out.println("Needed Time: " + ((end - start) / 1000) + " s");
        }
        System.out.println();

        // Buzzwords
        assertNotNull(resultStrings);
        System.out.println(resultStrings.length + " Results BUZZWORDS (includes Terms, Locations, Events in that order ! Duplicates filtered !) -> indexed in SE as \"buzzword\" field:");
        assertTrue(resultStrings.length > 0);
        for (String buzzword : resultStrings) {
            System.out.println("buzzword: " + buzzword);
        }
        System.out.println();

        // Locations
        Set<String> locations = fSnsInterface.getLocations();
        System.out.println(locations.size() + " Results LOCATIONS (! Duplicates filtered !) -> indexed in SE as \"location\" field:");
        assertTrue(locations.size() > 0);
        for (String location : locations) {
            System.out.println("location: " + location);
        }
        System.out.println();

        // BBoxes
        final Wgs84Box[] resultBBoxes = this.fSnsInterface.getReferencesToSpace();
        assertNotNull(resultBBoxes);
        System.out.println(resultBBoxes.length + " Results Wgs84Box(es) -> indexed in SE as \"x1\",\"y1\",\"x2\",\"y2\",\"areaid\"(=Gemeindekennziffer) fields:");
        assertTrue(resultBBoxes.length > 0);
        for (int i = 0; i < resultBBoxes.length; i++) {
            System.out.println(resultBBoxes[i].getTopicName());
            System.out.println("  x1:" + resultBBoxes[i].getX1());
            System.out.println("  y1:" + resultBBoxes[i].getY1());
            System.out.println("  x2:" + resultBBoxes[i].getX2());
            System.out.println("  y2:" + resultBBoxes[i].getY2());
            System.out.println("  Gemeindekennziffer: " + resultBBoxes[i].getGemeindekennziffer());
        }
        System.out.println();

        // Time
        final Temporal[] resultTime = this.fSnsInterface.getReferencesToTime();
        // NO REFERENCES TO EVENTS IN GS SOIL !!!
        assertNotNull(resultTime);
        System.out.println(resultTime.length + " Results Temporal: -> indexed in SE as \"t0\",\"t1\",\"t2\" fields:");
        assertEquals(0, resultTime.length);
        System.out.println();

        // TopicIds
        resultStrings = this.fSnsInterface.getTopicIds();
        assertNotNull(resultStrings);
        System.out.println(resultStrings.length + " Results TopicIds (includes Locations, Terms, Events in that order ! Duplicates filtered !) -> indexed in SE as \"areaid\" field:");
        assertTrue(resultStrings.length > 0);
        assertTrue(resultStrings.length > 0);
        for (String topicId : resultStrings) {
            System.out.println("topicId: " + topicId);
        }
        System.out.println();
    }
    
    private String getTermListOutput(List<Term> terms) {
    	String result = "";
    	for (Term term : terms) {
    		result += getTermOutput(term);
    		result += ", ";
    	}
    	return result;
    }
    private String getTermOutput(Term term) {
    	return term.getName();
    }
    private String getLocationListOutput(List<Location> locations) {
    	String result = "";
    	for (Location location : locations) {
    		result += getLocationOutput(location);
    		result += ", ";
    	}
    	return result;
    }
    private String getLocationOutput(Location location) {
    	return location.getName();
    }
}
