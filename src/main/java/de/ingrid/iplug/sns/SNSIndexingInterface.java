/*
 * Copyright (c) 1997-2006 by media style GmbH
 */

package de.ingrid.iplug.sns;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.external.FullClassifyService;
import de.ingrid.external.om.Event;
import de.ingrid.external.om.FullClassifyResult;
import de.ingrid.external.om.Location;
import de.ingrid.external.om.Term;
import de.ingrid.utils.tool.SNSUtil;
import de.ingrid.utils.tool.SpringUtil;

/**
 * An interface to the SN Service. It is mainly used to get some data for indexing.
 */
public class SNSIndexingInterface {

    private static Log log = LogFactory.getLog(SNSIndexingInterface.class);

    private List<Temporal> fTemporal = new ArrayList<Temporal>();

    private List<Wgs84Box> fWgs84Box = new ArrayList<Wgs84Box>();

    private List<String> fLocationNames = new ArrayList<String>();

    private String fLanguage;

    private List<String> fTopicIds = new ArrayList<String>();

    private String fGemeindeKennzifferPrefix = "ags:";

    private final Class<FullClassifyService> _fullClassifyService = null;
    private FullClassifyService fFullClassifyService;

    private List<Term> fTerms = new ArrayList<Term>();
    private List<Location> fLocations = new ArrayList<Location>();
    private List<Event> fEvents = new ArrayList<Event>();


    /**
     * Interface for indexing service connection handling.
     * NOTICE: Now handled via abstract FullClassifyService API, so login parameters ARE IGNORED !!!!
     * (e.g. instead read from sns.properties). But you can set a default language !
     * @param login  IGNORED !!!! Username for the SN service.
     * @param password  IGNORED !!!!Password for the SN service. 
     * @param language The DEFAULT language the result should be e.g. "de" (used if no language is passed).
     * @throws Exception If we cannot connect to the sns server.
     */
    public SNSIndexingInterface(final String login, final String password, final String language) throws Exception {
        this.fLanguage = language;

        SpringUtil springUtil = new SpringUtil("spring/external-services.xml");
        this.fFullClassifyService = springUtil.getBean("fullClassifyService", _fullClassifyService);
    }

    /**
     * IGNORED ! Timeout of IndexingInterface now handled in abstract API ! (e.g. read from sns.properties) 
     * @param timeout The timeout in milliseconds. IGNORED !!!!
     */
    public void setTimeout(final int timeout) {
//        this.fSNSClient.setTimeout(timeout);
    }

    /**
     * @param prefix
     */
    public void setGemeindeKennzifferPrefix(String prefix) {
        this.fGemeindeKennzifferPrefix = prefix;
    }

    /**
     * All buzzwords to the given document. You must call this method first to get results from
     * <code>getReferencesToTime</code> and <code>getReferencesToSpace</code>.
     * 
     * @param text
     *            The document to analyze.
     * @param maxToAnalyzeWords
     *            The first <code>maxToAnalyzeWords</code> words of the document that should be
     *            analyzed.
     * @param ignoreCase
     *            Set to true ignore case of the document.
     * @return A string array filled with all buzzwords.
     * @throws Exception
     *             If we cannot connect to the sns server.
     */
    public String[] getBuzzwords(final String text, final int maxToAnalyzeWords, boolean ignoreCase) throws Exception {
        return getBuzzwords(text, maxToAnalyzeWords, ignoreCase, this.fLanguage);
    }

    /**
     * All buzzwords to the given document. You must call this method first to get results from
     * <code>getReferencesToTime</code> and <code>getReferencesToSpace</code>.
     * 
     * @param text The document to analyze.
     * @param maxToAnalyzeWords The first <code>maxToAnalyzeWords</code> words of the document that should be analyzed.
     * @param ignoreCase Set to true ignore case of the document.
     * @param language The language the text is encoded.
     * @return A string array filled with all buzzwords.
     * @throws Exception If we cannot connect to the sns server.
     */
    public String[] getBuzzwords(final String text, final int maxToAnalyzeWords, boolean ignoreCase, String language) throws Exception {
    	if (language == null) {
    		language = this.fLanguage;
    	}
    	
    	if (log.isDebugEnabled()) {
            log.debug("     !!!!!!!!!! calling API fullClassifyService.autoClassifyText filter=" + null + " " + language);
        }

        FullClassifyResult classifyResult =
        	fFullClassifyService.autoClassifyText(text, maxToAnalyzeWords, ignoreCase, null, new Locale(language));

    	initializeFromFullClassifyResult(classifyResult);
        String[] result = getBasenames();

        return result;
    }

    /**
     * All buzzwords to the given URL. You must call this method first to get results from
     * <code>getReferencesToTime</code> and <code>getReferencesToSpace</code>.
     * 
     * @param urlStr The url to analyze.
     * @param maxToAnalyzeWords The first <code>maxToAnalyzeWords</code> words of the document that should be analyzed.
     * @param ignoreCase Set to true ignore case of the document.
     * @param language The language the text is encoded.
     * @return A string array filled with all buzzwords.
     * @throws Exception If we cannot connect to the sns server.
     */
    public String[] getBuzzwordsToUrl(final String urlStr, final int maxToAnalyzeWords, boolean ignoreCase, String language) throws Exception {
    	URL url = createURL(urlStr);

    	if (log.isDebugEnabled()) {
            log.debug("     !!!!!!!!!! calling API fullClassifyService.autoClassifyURL " + url + ", filter=" + null + ", " + language);
        }

        FullClassifyResult classifyResult;
    	try {
            classifyResult =
            	fFullClassifyService.autoClassifyURL(url, maxToAnalyzeWords, false, null, new Locale(language));
    	} catch (Exception ex) {
    		log.warn("Problems autoClassifyURL " + urlStr, ex);
    		throw ex;
    	}

    	initializeFromFullClassifyResult(classifyResult);
    	String[] result = getBasenames();

        return result;
    }

    /** Set up initial structures from classification result.
     * @param classifyResult
     */
    private void initializeFromFullClassifyResult(FullClassifyResult classifyResult) {
    	// we already filter stuff to be sure we get valid results ! We never know (SNS or GS Soil Services or ...)

    	this.fTerms = new ArrayList<Term>();
        for (Term term : classifyResult.getTerms()) {
            if (term == null || term.getName() == null || term.getName().trim().isEmpty()) {
            	continue;
            }
            this.fTerms.add(term);
        }

        // GSSoil Gazetteer delivers null locations and names !? So we filter empty elements !
    	this.fLocations = new ArrayList<Location>();
        for (Location location : classifyResult.getLocations()) {
            if (location == null || location.getName() == null || location.getName().trim().isEmpty()) {
            	continue;
            }
            this.fLocations.add(location);
        }

        this.fEvents = classifyResult.getEvents();
        if (this.fEvents == null) {
        	this.fEvents = new ArrayList<Event>();
        }

        // we set these ones to NULL ! So we know not processed yet (set to empty list when processed !)
        this.fTopicIds = null;
        this.fTemporal = null;
        this.fWgs84Box = null;
        this.fLocationNames = null;
    }

    
    /** Return the BUZZWORDS ! NOTICE: terms, locations AND events ! */
    private String[] getBasenames() {
    	// we use set (ordered), so duplicates are removed !!!
        Set<String> result = new LinkedHashSet<String>();
        
        for (Term term : fTerms) {
        	result.add(term.getName());
        }

        for (Location location : fLocations) {
        	result.add(location.getName());
        }

        for (Event event : fEvents) {
    		// may not be set if NOT using SNS and event is just a time reference !
        	if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
        		continue;
        	}
        	result.add(event.getTitle());
        }

        if (log.isDebugEnabled()) {
        	String output = "     \"buzzword\" index field SE (Terms, Locations, Events in that order): ";
        	for (String buzzword : result) {
        		output = output + buzzword + ", ";
        	}
            log.debug("     " + output);
        }

        return result.toArray(new String[result.size()]);
    }

    private void getReferences() throws Exception, ParseException {
    	// initialize Lists here ! So we can differ whether getReferences() already called !
       	fTopicIds = new ArrayList<String>();
        fTemporal = new ArrayList<Temporal>();
        fWgs84Box = new ArrayList<Wgs84Box>();
        fLocationNames = new ArrayList<String>();

        // NOTICE: the fTopicIds go into the "areaid" index field in the SE !
        // so we first process locations and then the terms, see below. 
    	for (Location location : fLocations) {
            this.fTopicIds.add(location.getId());
            this.fLocationNames.add(location.getName());

            boolean wgs84BoxSet = false;
            final Wgs84Box wgs84Box = new Wgs84Box(location.getName(), 0, 0, 0, 0, "");
            float[] bbox = location.getBoundingBox();
            if (bbox != null) {
            	// we want keep exactly same precision !
                wgs84Box.setX1(Double.parseDouble(Float.toString(bbox[0])));
                wgs84Box.setY1(Double.parseDouble(Float.toString(bbox[1])));
                wgs84Box.setX2(Double.parseDouble(Float.toString(bbox[2])));
                wgs84Box.setY2(Double.parseDouble(Float.toString(bbox[3])));
                wgs84BoxSet = true;
            }
            if (location.getNativeKey() != null) {
                String gemeindekennziffer = SNSUtil.transformSpacialReference(this.fGemeindeKennzifferPrefix, location.getNativeKey());
                if (gemeindekennziffer.startsWith("lawa:")) {
                    gemeindekennziffer = SNSUtil.transformSpacialReference("lawa:", location.getNativeKey());
                }
                wgs84Box.setGemeindekennziffer(gemeindekennziffer);
                wgs84BoxSet = true;
            }
            
            if (wgs84BoxSet) {
                this.fWgs84Box.add(wgs84Box);
            }
    	}

        // NOTICE: the fTopicIds go into the "areaid" index field in the SE !
        // So why add the ids of the terms ? We move this one behind the locations
        // to guarantee locations first in index field (number of entries in index
    	// field may be limited)
    	for (Term term : fTerms) {
            this.fTopicIds.add(term.getId());
    	}

    	for (Event event : fEvents) {
    		// may be not set if NOT using SNS and event is just a time reference !
    		if (event.getId() != null && event.getId().length() > 0) {
                this.fTopicIds.add(event.getId());    			
    		}

            final Temporal temporal = new Temporal();
            temporal.setAt(event.getTimeAt());
            temporal.setFrom(event.getTimeRangeFrom());
            temporal.setTo(event.getTimeRangeTo());
            if (!temporal.isEmpty()) {
                this.fTemporal.add(temporal);
            }
    	}
    }

    /**
     * All topic ids to the result.
     * 
     * @return
     * @throws Exception
     * @throws ParseException
     */
    public String[] getTopicIds() throws Exception, ParseException {
        if (this.fTopicIds == null) {
            getReferences();
        }

        // we use set to remove duplicates
        Set<String> ret = new LinkedHashSet<String>();
        for (String topicId : fTopicIds) {
            ret.add(topicId);
        }

        if (log.isDebugEnabled()) {
        	String output = "     topicIds (\"areaid\" index field SE = Locations, Terms, Events in that order)): ";
        	for (String topicId : ret) {
        		output = output + topicId + ", ";
        	}
            log.debug("     " + output);
        }

        return (String[]) ret.toArray(new String[ret.size()]);
    }

    /**
     * All time references to a document that is analyzed by <code>getBuzzwords</code>.
     * 
     * @return Array of strings. It is empty if nothing is found.
     * @throws Exception
     *             If we cannot connect to the sns server.
     * @throws ParseException
     *             If the date cannot be parsed.
     */
    public Temporal[] getReferencesToTime() throws Exception, ParseException {
        if (this.fTemporal == null) {
            getReferences();
        }

        return (Temporal[]) this.fTemporal.toArray(new Temporal[this.fTemporal.size()]);
    }

    /**
     * All coordinate references to a document that is analyzed by <code>getBuzzwords</code>.
     * 
     * @return Array of strings. It is empty if nothing is found.
     * @throws Exception
     *             If we cannot connect to the sns server.
     */
    public Wgs84Box[] getReferencesToSpace() throws Exception {
        if (this.fWgs84Box == null) {
            getReferences();
        }

        return (Wgs84Box[]) this.fWgs84Box.toArray(new Wgs84Box[this.fWgs84Box.size()]);
    }

    /**
     * The method searches all topics of type <b>location</b> and provides the baseNames. The topics
     * came from the document that was analyzed by a previous
     * {@linkplain #getBuzzwords(String, int, boolean)} call.
     * 
     * @return A set of locations. It is empty, if no location topics are available.
     */
    public Set<String> getLocations() throws Exception {
        if (this.fLocationNames == null) {
            getReferences();
        }

        // we use set to remove duplicates
        Set<String> ret = new LinkedHashSet<String>();
        for (String locationName : fLocationNames) {
            ret.add(locationName);
        }

        if (log.isDebugEnabled()) {
        	String output = "     \"location\" index field SE: ";
        	for (String location : ret) {
        		output = output + location + ", ";
        	}
            log.debug("     " + output);
        }

        return ret;
    }

	/** Create URL from url String. Returns null if problems !!! */
	private URL createURL(String urlStr) throws MalformedURLException {
    	URL url = null;
    	try {
    		url = new URL(urlStr);
    	} catch (MalformedURLException ex) {
    		log.warn("Error building URL " + urlStr, ex);
    		throw ex;
    	}
    	
    	return url;
	}
}
