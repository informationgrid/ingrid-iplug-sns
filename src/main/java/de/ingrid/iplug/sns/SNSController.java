/*
 * **************************************************-
 * Ingrid iPlug SNS
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iplug.sns;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.external.ChronicleService;
import de.ingrid.external.FullClassifyService;
import de.ingrid.external.GazetteerService;
import de.ingrid.external.ThesaurusService;
import de.ingrid.external.ThesaurusService.MatchingType;
import de.ingrid.external.om.Event;
import de.ingrid.external.om.FullClassifyResult;
import de.ingrid.external.om.Link;
import de.ingrid.external.om.Location;
import de.ingrid.external.om.RelatedTerm;
import de.ingrid.external.om.RelatedTerm.RelationType;
import de.ingrid.external.om.Term;
import de.ingrid.external.om.Term.TermType;
import de.ingrid.external.om.TreeTerm;
import de.ingrid.external.sns.SNSClient;
import de.ingrid.iplug.sns.utils.DetailedTopic;
import de.ingrid.iplug.sns.utils.Topic;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.tool.SNSUtil;
import de.ingrid.utils.tool.SpringUtil;

/**
 * A API to access the main SNS WebService functionality
 * 
 * created on 29.09.2005
 * 
 * @author sg
 * @version $Revision: 1.2 $
 * 
 */
public class SNSController {

    private static Log log = LogFactory.getLog(SNSController.class);

    //private static final SimpleDateFormat expiredDateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

    private final Class<ThesaurusService> _thesaurusService = null;
    private ThesaurusService thesaurusService;
    private final Class<GazetteerService> _gazetteerService = null;
    private GazetteerService gazetteerService;
    private final Class<ChronicleService> _chronicleService = null;
    private ChronicleService chronicleService;
    private final Class<FullClassifyService> _fullClassifyService = null;
    private FullClassifyService fullClassifyService;

    private String fNativeKeyPrefix;

    /** Can be set from spring/external-services.xml, default is false */
    private boolean getLocationsFromText_ignoreCase = false;

    private static ResourceBundle mappingBundle;

    /**
     * Constructor for SNS controller.
     * 
     * @param client
     * @param nativeKeyPrefix
     */
    public SNSController(SNSClient client, String nativeKeyPrefix) {
        this.fNativeKeyPrefix = nativeKeyPrefix;

        SpringUtil springUtil = new SpringUtil("spring/external-services.xml");
        try {
            this.thesaurusService = springUtil.getBean("thesaurusService", _thesaurusService);
            this.gazetteerService = springUtil.getBean("gazetteerService", _gazetteerService);
            this.chronicleService = springUtil.getBean("chronicleService", _chronicleService);
            this.fullClassifyService = springUtil.getBean("fullClassifyService", _fullClassifyService);
        } catch (Exception ex) {
            log.error( "Error initializing beans", ex ); 
        }
        
        mappingBundle = ResourceBundle.getBundle("mapping");
        
        // change default parameters e.g. query case sensitive or not ! (in GS Soil different than in PortalU)
    	try {
            final Class<Map> _map = null;
            Map props = springUtil.getBean("servicesProperties", _map);
        	getLocationsFromText_ignoreCase = new Boolean((String)props.get("gazetteerService.getLocationsFromText.ignoreCase"));
    	} catch (Exception ex) {
        	if (log.isInfoEnabled()) {
        		log.info("Problems fetching properties from spring/external-services.xml, we use defaults:\n" +
        			"gazetteerService.getLocationsFromText.ignoreCase:" + getLocationsFromText_ignoreCase);
        	}    		
    	}
    }

    /**
     * For a given term (that should be a thesaurus "descriptor" topic itself !) an array
     * of associated topics will be returned.
     * 
     * @param queryTerm The query term.
     * @param start IGNORED
     * @param maxResults Limit number of results.
     * @param plugId The plugId as String.
     * @param totalSize The quantity of the found topics altogether.
     * @param lang Is used to specify the preferred language for requests.
     * @param expired return also expired topics ? IGNORED when calling ThesaurusService API
     * @param includeUse
     * @return an array of associated topics or null in case the term itself is not found as topic
     * @throws Exception
     */
    public synchronized Topic[] getTopicsForTerm(String queryTerm, int start, int maxResults,
            String plugId, int[] totalSize, String lang, boolean expired, boolean includeUse) throws Exception {
        Topic[] result = new Topic[0];

        Topic ingridTopic =
        	getThesaurusDescriptorTopic(queryTerm, totalSize, lang, includeUse, plugId);
        if (ingridTopic != null) {
            result = getTopicsForTopic(ingridTopic.getTopicID(), maxResults,
            		"/thesa", plugId, lang, totalSize, expired);
        }

        return result;
    }

    /**
     * For a given topic (identified by id) an array of associated topics will be returned.</br>
     * Calls</br>
     * <ul>
     * <li>thesaurusService.getRelatedTermsFromTerm()
     * <li>gazetteerService.getRelatedLocationsFromLocation
     * <li>direct autoClassify()
     * </ul>
     * dependent from passed filter.
     * @param topicId The topic given by Id.
     * @param maxResults Limit number of results.
     * @param filter Topic type as search criterion
     * @param plugId The plugId as String
     * @param totalSize The quantity of the found topics altogether.
     * @param expired return also expired topics ? IGNORED when calling ThesaurusService / GazetteerService API
     * @return an array of associated topics for a type identified by id
     * @throws Exception
     */
    public synchronized Topic[] getTopicsForTopic(String topicId, int maxResults,
    		String filter, String plugId, String lang, int[] totalSize, boolean expired) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("getTopicsForTopic: filter=" + filter + ", lang=" + lang);
        }

    	// TERMS
    	if ("/thesa".equals(filter)) {
            if (log.isDebugEnabled()) {
                log.debug("     !!!!!!!!!! calling API thesaurusService.getRelatedTermsFromTerm " + topicId + " " + lang);
            }
            RelatedTerm[] terms = thesaurusService.getRelatedTermsFromTerm(topicId, new Locale(lang));

        	List<Topic> resultList = new ArrayList<Topic>();
            List<String> duplicateList = new ArrayList<String>();
            for (RelatedTerm term : terms) {
            	if (!duplicateList.contains(term.getId())) {
                	resultList.add(buildTopicFromRelatedTerm(term, plugId, lang));
                	duplicateList.add(term.getId());
                	if (resultList.size() == maxResults) {
                		break;
                	}
            	}
            }
            if (resultList.size() > 0) {
            	totalSize[0] = resultList.size(); 
            	return resultList.toArray(new Topic[resultList.size()]);
            }

           	// LOCATIONS
    	} else if ("/location".equals(filter)) {
    		return getTopicSimilarLocationsFromTopic(topicId, maxResults, plugId, totalSize, lang);

    	} else  {
    		// IS THIS EVER CALLED FOR ANOTHER TOPIC TYPE ? All Topics (filter null) ? Then this is executed.
            /*Map associationTypes = new HashMap();
            Topic topic = new Topic();
            topic.setId(topicId);
            Topic[] associatedTopics = getAssociatedTopics(topic, fTypeFilters, associationTypes, totalSize, expired);
            if (associatedTopics != null) {
                return copyToTopicArray(associatedTopics, associationTypes, maxResults, plugId, "bla");
            }
            */
    	}

        return null;
    }

    /**
     * For a given text an array of detailed topics will be returned (synchronized version). NO FILTER (all topic types !).
     * 
     * @param searchTerm
     *            The given text to analyze.
     * @param maxToAnalyzeWords
     *            Analyze only the first maxToAnalyzeWords words of the document in the body.
     * @param plugId
     *            The plugId as String.
     * @param lang
     *            Is used to specify the preferred language for requests.
     * @param totalSize
     *            The quantity of the found topics altogether.
     * @param expired return also expired topics ? 
     * @return Array of detailed topics for the given text.
     * @throws Exception
     */
    public synchronized DetailedTopic[] getTopicsForText(String searchTerm, int maxToAnalyzeWords, String plugId,
            String lang, int[] totalSize, boolean expired) throws Exception {
        return getTopicsForText(searchTerm, maxToAnalyzeWords, null, plugId, lang, totalSize, expired);
    }

    /** For a given text an array of detailed topics will be returned.</br>
     * Calls</br>
     * <ul>
     * <li>thesaurusService.getTermsFromText()
     * <li>gazetteerService.getLocationsFromText() -> ONLY NON EXPIRED ONES !
     * <li>direct autoClassify()
     * </ul>
     * dependent from passed filter.
     * @param documentText The given text to analyze.
     * @param maxToAnalyzeWords Analyze only the first maxToAnalyzeWords words of the document in the body.
     * @param filter Topic type as search criterion (only root paths may be used).
     * @param plugId The plugId as String.
     * @param lang Is used to specify the preferred language for requests.
     * @param totalSize The quantity of the found topics altogether.
     * @param expired return also expired topics ? IGNORED ! expired attribute only set in SNS
     * 		location topics. There we call Gazetteer API which always filters expired ones (SNS) !
     * 		Was also filtered before introducing Gazetteer API.
     * @return array of detailed topics for the given text
     * @throws Exception
     */
    public DetailedTopic[] getTopicsForText(String documentText, int maxToAnalyzeWords, String filter, String plugId,
            String lang, int[] totalSize, boolean expired) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("getTopicsForText: filter=" + filter + ", lang=" + lang);
        }

        DetailedTopic[] result = new DetailedTopic[0];

    	// TERMS
    	if ("/thesa".equals(filter)) {
            if (log.isDebugEnabled()) {
                log.debug("     !!!!!!!!!! calling API thesaurusService.getTermsFromText " + lang);
            }
        	Term[] terms = thesaurusService.getTermsFromText(documentText, maxToAnalyzeWords,
        			true, new Locale(lang));

            if (terms != null) {
            	totalSize[0] = terms.length;
            	result = toDetailedTopicArray(terms, plugId, lang);
            }

       	// LOCATIONS
    	} else if ("/location".equals(filter)) {

            if (log.isDebugEnabled()) {
                log.debug("     !!!!!!!!!! calling API gazetteerService.getLocationsFromText " + lang);
            }
            
            // TODO: prefer OR although it still delivers less results due to a bug
            // -> https://github.com/innoq/iqvoc_gazetteer/issues/13
            // search for all words in document (OR)
        	Location[] locations = gazetteerService.getLocationsFromText(documentText, maxToAnalyzeWords, getLocationsFromText_ignoreCase, new Locale(lang));
        	
        	// all words have to be in search result (AND)
        	//Location[] locations = gazetteerService.findLocationsFromQueryTerm( documentText, QueryType.ALL_LOCATIONS, de.ingrid.external.GazetteerService.MatchingType.CONTAINS, new Locale(lang) );

            if (locations != null) {
            	totalSize[0] = locations.length;
            	result = toDetailedTopicArray(locations, plugId, lang);
            }

    	} else  {
    		// IS THIS EVER CALLED FOR ANOTHER TOPIC TYPE ? Event ? All Topics (filter null) ? Then this is executed.
    		// NOTICE: we may always execute this with according filter !!! but let's keep the old structure
            
    		de.ingrid.external.FullClassifyService.FilterType filterType = getFullClassifyFilterType(filter);

            if (log.isDebugEnabled()) {
                log.debug("     !!!!!!!!!! calling API fullClassifyService.autoClassifyText " + filter + " " + lang);
            }
            FullClassifyResult classifyResult =
            	fullClassifyService.autoClassifyText(documentText, maxToAnalyzeWords, false, filterType, new Locale(lang));

            if (classifyResult != null) {
            	result = toDetailedTopicArray(classifyResult, plugId, lang);
               	totalSize[0] = result.length;
            }
    	}

        return result;
    }

    /**
     * For a given URL an array of detailed topics will be returned.
     * NOTICE: Returns only NON EXPIRED topics. 
     * 
     * @param urlStr The given url to analyze.
     * @param maxToAnalyzeWords Analyze only the first maxToAnalyzeWords words of the document in the body.
     * @param filter Topic type as search criterion (only root paths may be used).
     * @param plugId The plugId as String.
     * @param lang Is used to specify the preferred language for requests.
     * @param totalSize The quantity of the found topics altogether.
     * @return array of detailed topics for the given text
     * @throws Exception
     */
    public DetailedTopic[] getTopicsForURL(String urlStr, int maxToAnalyzeWords, String filter, String plugId,
            String lang, int[] totalSize) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("getTopicsForURL: " + urlStr + ", filter=" + filter + ", lang=" + lang);
        }

    	URL url = createURL(urlStr);
		de.ingrid.external.FullClassifyService.FilterType filterType = getFullClassifyFilterType(filter);

        if (log.isDebugEnabled()) {
            log.debug("     !!!!!!!!!! calling API fullClassifyService.autoClassifyURL " + url
            		+ " " + filter + " " + lang);
        }
        FullClassifyResult classifyResult =
        	fullClassifyService.autoClassifyURL(url, maxToAnalyzeWords, false, filterType, new Locale(lang));

        DetailedTopic[] result = new DetailedTopic[0];
        if (classifyResult != null) {
        	result = toDetailedTopicArray(classifyResult, plugId, lang);
           	totalSize[0] = result.length;
        }

        return result;
    }

    /**
     * For a given topic ID the detailed topic will be returned in an array.
     * @param topicID the id of the topic
     * @param filter Topic type as search criterion (only root paths may be used).
     * @param plugId The plugId as String.
     * @param lang Is used to specify the preferred language for requests.
     * @param totalSize The quantity of the found topics altogether (is 1 !)
     * @return array of detailed topics for the given topic id (should be size 1, if problems is size 0
     * @throws Exception
     */
    public DetailedTopic[] getTopicForId(String topicID, String filter, String plugId,
            String lang, int[] totalSize) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("getTopicForId: " + topicID + ", filter=" + filter + ", lang=" + lang);
        }

        DetailedTopic detailedTopic = getTopicDetail(topicID, filter, lang, plugId);
        
        DetailedTopic[] result = new DetailedTopic[0];
        if (detailedTopic != null) {
        	result = new DetailedTopic[]{ detailedTopic };
           	totalSize[0] = 1;
        }

        return result;
    }

    /**
     * @return An array of ingrid detailed topics from a Term array.
     */
    private DetailedTopic[] toDetailedTopicArray(Term[] terms, String plugId, String lang) {
        final List<DetailedTopic> returnList = new ArrayList<DetailedTopic>();
        for (Term term : terms) {
        	if (term.getType() != TermType.DESCRIPTOR) {
        		continue;
        	}
            
        	returnList.add(buildDetailedTopicFromTerm(term, plugId, lang));
        }

        return returnList.toArray(new DetailedTopic[returnList.size()]);
    }

    /**
     * @return An array of ingrid detailed topics from a Location array.
     */
    private DetailedTopic[] toDetailedTopicArray(Location[] locations, String plugId, String lang) {
        final List<DetailedTopic> returnList = new ArrayList<DetailedTopic>();
        for (Location location : locations) {
            // GSSoil Gazetteer delivers null locations and names !?
            if (location == null || location.getName() == null) {
            	continue;
            }
        	returnList.add(buildDetailedTopicFromLocation(location, plugId, lang));
        }

        return returnList.toArray(new DetailedTopic[returnList.size()]);
    }

    /**
     * @param length TODO
     * @return An array of ingrid detailed topics from an Event array.
     */
    private DetailedTopic[] toDetailedTopicArray(Event[] events, String plugId, String lang, int length) {
        final List<DetailedTopic> returnList = new ArrayList<DetailedTopic>();
        int count = Math.min(length, events.length);
        for (int i = 0; i < events.length; i++) {
            if(events[i] != null){
                returnList.add(buildDetailedTopicFromEvent(events[i], plugId, lang));
            }
        }

//        return returnList.toArray(new DetailedTopic[returnList.size()]);
        return returnList.toArray(new DetailedTopic[events.length]);
    }

    /**
     * @return An array of ingrid detailed topics from a result of a full classification.
     */
    private DetailedTopic[] toDetailedTopicArray(FullClassifyResult classifyResult, String plugId, String lang) {
        final List<DetailedTopic> returnList = new ArrayList<DetailedTopic>();
        DetailedTopic[] topics;
        
        List<Term> classifyTerms = classifyResult.getTerms();
        if (classifyTerms != null && classifyTerms.size() > 0) {
            topics = toDetailedTopicArray(classifyTerms.toArray(new Term[classifyTerms.size()]), plugId, lang);
            if (topics.length > 0) {
                returnList.addAll(Arrays.asList(topics));        	
            }        	
        }

        List<Location> classifyLocations = classifyResult.getLocations();
        if (classifyLocations != null && classifyLocations.size() > 0) {
            topics = toDetailedTopicArray(classifyLocations.toArray(new Location[classifyLocations.size()]), plugId, lang);
            if (topics.length > 0) {
                returnList.addAll(Arrays.asList(topics));        	
            }        	
        }

        List<Event> classifyEvents = classifyResult.getEvents();
        if (classifyEvents != null && classifyEvents.size() > 0) {
            topics = toDetailedTopicArray(classifyEvents.toArray(new Event[classifyEvents.size()]), plugId, lang, 10);
            if (topics.length > 0) {
                returnList.addAll(Arrays.asList(topics));        	
            }        	
        }

        return returnList.toArray(new DetailedTopic[returnList.size()]);
    }

    /** Build A detailed topic from Event.
     * NOTICE: NO MAPPING OF ASSOCIATED TERMS YET !!!!!! */
    private synchronized DetailedTopic buildDetailedTopicFromEvent(Event event, String plugId, String lang) {
        String topicId = event.getId();
        String title = event.getTitle();
        String snsInstanceOf = getSNSInstanceOf(event);
        String summary = title + ' ' + snsInstanceOf;

        DetailedTopic result = new DetailedTopic(plugId, topicId.hashCode(), topicId, title, summary, null);
        result.addToList(DetailedTopic.INSTANCE_OF, snsInstanceOf);
        result.put(DetailedTopic.DESCRIPTION_OCC, event.getDescription());

        // we push the stuff which IS ALWAYS ADDED, even if empty
        // NOTICE: Further this converts the DetailedTopic to an IngridDocument !!!!?
        //pushDefinitions(result, lang);
        pushSamples(result, event.getLinks(), lang);
        
        // NO TERM ASSOCIATIONS MAPPED in EVENT SO FAR !!!
//        pushOccurensie(DetailedTopic.ASSOCIATED_OCC, topic, metaData, lang);
        

        if (event.getTimeAt() != null) {
        	String at = getSNSDateString(event.getTimeAt());
           	result.setFrom(at);
           	result.setTo(at);
        } else {
           	result.setFrom(getSNSDateString(event.getTimeRangeFrom()));
           	result.setTo(getSNSDateString(event.getTimeRangeTo()));
        }

        return result;
    }

    /** Build A detailed topic from Location */
    private synchronized DetailedTopic buildDetailedTopicFromLocation(Location location, String plugId, String lang) {
        String topicId = location.getId();
        String title = location.getName();
        String snsInstanceOf = getSNSInstanceOf(location);
        String summary = title + ' ' + snsInstanceOf;

        DetailedTopic result = new DetailedTopic(plugId, topicId.hashCode(), topicId, title, summary, null);
        result.addToList(DetailedTopic.INSTANCE_OF, snsInstanceOf);

        // we push the stuff which IS ALWAYS ADDED, even if empty
        // NOTICE: Further this converts the DetailedTopic to an IngridDocument !!!!?
        pushDefinitions(result, lang);
        // TODO: pushSamples(result, lang);

        if (location.getNativeKey() != null) {
        	result.setTopicNativeKey(SNSUtil.transformSpacialReference(this.fNativeKeyPrefix, location.getNativeKey()));
        } else {
        	result.setTopicNativeKey(topicId);
        }

        // if administrative location, also set topic id as administrative id !?
        String locationType = location.getTypeId();
        if (isAdministrationType(locationType)) {
       		result.setAdministrativeID(topicId);
        }
        
        // NO BBox !!!? was never delivered !

        return result;
    }

    /**
     * Determine if the type is and administration type, which is identified by its
     * name. This SNS must contain the String "admin-".
     * @param locationType
     * @return
     */
    private boolean isAdministrationType(String locationType) {
		return locationType != null && locationType.indexOf("admin-") != -1;
	}

	/** Build A detailed topic from Term */
    private synchronized DetailedTopic buildDetailedTopicFromTerm(Term term, String plugId, String lang) {
        String topicId = term.getId();
        String title = term.getName();
        String snsInstanceOf = getSNSInstanceOf(term);
        String summary = title + ' ' + snsInstanceOf;

        DetailedTopic result = new DetailedTopic(plugId, topicId.hashCode(), topicId, title, summary, null);
        result.addToList(DetailedTopic.INSTANCE_OF, snsInstanceOf);

        // we push the stuff which IS ALWAYS ADDED, even if empty
        // NOTICE: Further this converts the DetailedTopic to an IngridDocument !!!!?
        pushDefinitions(result, lang);
        // TODO: pushSamples(result, lang);

        pushGemetDataFromTerm(result, term);
        result.setTopicNativeKey(topicId);

        return result;
    }

    /** If GEMET data set in term, adapt topic */
    private void pushGemetDataFromTerm(Topic topic, Term term) {
        if (term.getAlternateId() != null) {
        	String gemetOcc = term.getAlternateId() + "@" + term.getName();
        	topic.put(DetailedTopic.GEMET_OCC, gemetOcc);
            // UMTHES name in AlternateName !
            if (term.getAlternateName() != null) {
            	topic.setTopicName(term.getAlternateName());            	
            }
        }
    }

    
    private void pushDefinitions(DetailedTopic metaData, String lang) {
        List<String> titles = new ArrayList<String>();
        List<String> definitions = new ArrayList<String>();

        // just add empty definitions as long as there are none defined
        
        /*if (metaData.getString(DetailedTopic.DESCRIPTION_OCC) != null) {
        	definitions.add(metaData.getString(DetailedTopic.DESCRIPTION_OCC));
        	titles.add(metaData.getTitle());
        }*/

        metaData.setDefinitions((String[]) definitions.toArray(new String[definitions.size()]));
        metaData.setDefinitionTitles((String[]) titles.toArray(new String[titles.size()]));
    }
    
    private void pushSamples(DetailedTopic metaData, List<Link> links, String lang) {
        String[] titles = new String[links.size()];
        String[] samples = new String[links.size()];
        
        for (int i = 0; i < links.size(); i++) {
			titles[i] = links.get(i).getTitle();
			samples[i] = links.get(i).getLinkAddress();
		}

        metaData.setSamples(samples);
        metaData.setSampleTitles(titles);
    }
    
    /**
     * @return A ingrid topic from a Term.
     */
    private Topic buildTopicFromTerm(Term term, String plugId, String lang) {
        String title = term.getName();
        String summary = title + ' ' + getSNSInstanceOf(term);
        String topicId = term.getId();
        String associationType = "";
        Topic result = new Topic(plugId, topicId.hashCode(),
                topicId, title, summary, associationType, null);
        pushGemetDataFromTerm(result, term);
        result.setLanguage(lang);

        return result;
    }

    /**
     * @return A ingrid topic from a Location.
     */
    private Topic buildTopicFromLocation(Location location, String plugId, String lang) {
        String title = location.getName();
        String summary = title + ' ' + getSNSInstanceOf(location);
        String topicId = location.getId();
        String associationType = "";
        Topic result = new Topic(plugId, topicId.hashCode(),
                topicId, title, summary, associationType, null);

        if (location.getNativeKey() != null) {
            String ags = SNSUtil.transformSpacialReference(this.fNativeKeyPrefix, location.getNativeKey());
            if (ags.startsWith("lawa:")) {
                ags = SNSUtil.transformSpacialReference("lawa:", location.getNativeKey());
            }
        	result.setTopicNativeKey(SNSUtil.transformSpacialReference(this.fNativeKeyPrefix, location.getNativeKey()));
        } else {
        	result.setTopicNativeKey(topicId);
        }
        
        result.setLanguage(lang);

        return result;
    }

    /**
     * Also adds children OR parents as successors to ingrid topic !
     * @param addParentsAsSuccessors true = the parents of the passed term are added
     * 		as successors to the topic (recursively)</br>
     * 		false = the children of passed term are added as successors
     * @return A ingrid topic from a TreeTerm. Also sets up successors in topic !
     */
    private Topic buildTopicFromTreeTerm(TreeTerm term, String plugId, String lang,
    		boolean addParentsAsSuccessors) {
    	Topic resultTopic = buildTopicFromTerm(term, plugId, lang);
    	
    	// add children or parents as successors dependent from flag
    	List<TreeTerm> successorTerms = term.getChildren();
    	if (addParentsAsSuccessors) {
    		successorTerms = term.getParents();
    	}

    	if (successorTerms != null) {
        	for (TreeTerm successorTerm : successorTerms) {
        		Topic successorTopic =
        			buildTopicFromTreeTerm(successorTerm, plugId, lang, addParentsAsSuccessors);
        		resultTopic.addSuccessor(successorTopic);
        	}
    	}
    	
    	return resultTopic;
    }

    /**
     * Also adds relation information to ingrid topic !
     * @return A ingrid topic from a RelatedTerm. Also sets up relation info in topic !
     */
    private Topic buildTopicFromRelatedTerm(RelatedTerm term, String plugId, String lang) {
    	Topic resultTopic = buildTopicFromTerm(term, plugId, lang);

    	// add association type dependent from relation
    	String memberType = getSNSAssociationMemberType(term);
    	resultTopic.setTopicAssoc(memberType);
    	
    	return resultTopic;
    }

    private String getSNSDateString(Date date) {
    	if (date == null) {
    		return null;
    	}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat.format(date);
    }

    /** Extract SNS instanceOf href from event ! */
    private static String getSNSInstanceOf(Event event) {
    	String base = event.getId().substring(event.getId().lastIndexOf('/'));
		return base + "collections/" + event.getTypeId();
    }

    /** Extract SNS instanceOf href from location ! */
    private static String getSNSInstanceOf(Location location) {
        String id = location.getId();
        int indexOfSlash = id.lastIndexOf('/');
    	String base = indexOfSlash != -1 ? location.getId().substring(indexOfSlash) : id;
        String mappedType = location.getTypeId();
        try {
            mappedType = mappingBundle.getString("gazetteer.de." + location.getTypeId());
        } catch(MissingResourceException ex) { /* ignore */}
        
		return base + "#" + mappedType;
    }

    /** Extract SNS instanceOf href from term ! */
    private static String getSNSInstanceOf(Term term) {
    	// unknown term types are handled as top terms !
    	String snsInstanceOf = "#topTermType";

    	// first check whether we have a tree term ! Only then we can determine whether top node !
    	boolean determined = false;
		if (TreeTerm.class.isAssignableFrom(term.getClass())) {
	    	if (((TreeTerm)term).getParents() == null) {
	    		snsInstanceOf = "#topTermType";
	    		determined = true;
	    	}    	
		}
		
		if (!determined) {
	    	TermType termType = term.getType();
	    	if (termType == TermType.NODE_LABEL) {
	    		snsInstanceOf = "#nodeLabelType";
	    	} else if (termType == TermType.DESCRIPTOR) {
	    		snsInstanceOf = "#descriptorType";
	    	} else if (termType == TermType.NON_DESCRIPTOR) {
	    		snsInstanceOf = "#nonDescriptorType";
	    	}			
		}

		return snsInstanceOf;
    }

    /** Extract SNS assoziation member type from related term ! */
    private static String getSNSAssociationMemberType(RelatedTerm term) {
    	// default is synonym member type)
    	String snsMemberType = "#synonymMember";

    	RelationType relationType = term.getRelationType();
    	if (relationType == RelationType.CHILD) {
    		snsMemberType = "#narrowerTermMember";
    	} else if (relationType == RelationType.PARENT) {
    		snsMemberType = "#widerTermMember";
    	} else if (relationType == RelationType.RELATIVE) {
    		// check type of term !
    		if (term.getType() == TermType.DESCRIPTOR) {
        		snsMemberType = "#descriptorMember";
    		} else {
        		snsMemberType = "#synonymMember";
    		}
    	}
			
		return snsMemberType;
    }

    /**
     * ATTENTION: This method had a bug when using the old controller before introducing the ThesaurusService API !
     * top and label topics were treated as descriptors !!! But not non descriptor topics !
     * WE STILL SIMULATE THIS BUG HERE !!! Cause don't know where this query is called ! 
     * @return just one matching topic, in case more topics match we return the FIRST ONE ! If no topic match we return null.
     * @throws Exception
     */
    private Topic getThesaurusDescriptorTopic(String queryTerm, int[] totalSize, String lang,
            boolean includeUse, String plugId) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("getTopic: " + queryTerm + ", lang=" + lang);
        }

        if (log.isDebugEnabled()) {
            log.debug("     !!!!!!!!!! calling API thesaurusService.findTermsFromQueryTerm: " + queryTerm + " ... " + lang);
        }
    	Term[] terms = thesaurusService.findTermsFromQueryTerm(queryTerm, MatchingType.EXACT, includeUse, new Locale(lang));

    	// filter terms. Same name !
    	// NOTICE: old findTopics call before integration of API was
    	//    findTopics(queryTerm, "/thesa/descriptor", SearchType.exact, FieldsType.names, offSet, lang, includeUse);
    	// Although "/thesa/descriptor" is passed, SNS returns also a top topic (Hydrosphäre - Wasser und Gewässer) 
    	// or a label topic (Wasser) BUT FILTERS non decriptor topics (Waldsterben) !
    	// further the returned topic name matches the passed queryTerm, BUT CASE INSENSITIVE !
    	// check here: http://www.semantic-network.de/doc_findtopics.html?lang=de

    	List<Term> filteredTerms = new ArrayList<Term>();
        List<String> duplicateList = new ArrayList<String>();
        for (Term term : terms) {
        	// NO ! Not only descriptors ! see above !
//        	if (term.getType() != TermType.DESCRIPTOR) {
//        		continue;
//        	}
        	// filter non descriptors, see above !
        	if (term.getType() == TermType.NON_DESCRIPTOR) {
        		continue;
        	}

        	// CASE INSENSITIVE, see above ! 
        	if (!term.getName().equalsIgnoreCase(queryTerm)) {
        		continue;
        	}
        	if (duplicateList.contains(term.getId())) {
        		continue;
        	}
        	
        	filteredTerms.add(term);
        	duplicateList.add(term.getId());        	
        }

        // RETURN FIRST TOPIC !
        Topic result = null;
        if (filteredTerms.size() > 0) {
        	result = buildTopicFromTerm(filteredTerms.get(0), plugId, lang);
        	totalSize[0] = 1;
        }

        return result;
    }

    /**
     * @return ingrid Topics from Terms.
     */
    private Topic[] copyToTopicArray(Term[] terms, int maxResults,
            String plugId, String lang) throws Exception {
        final List<Topic> ingridTopics =
        	new ArrayList<Topic>();
        final List<String> duplicateList = new ArrayList<String>();

        if (null != terms) {
            int count = Math.min(maxResults, terms.length);
            for (int i = 0; i < count; i++) {
            	// for synonyms the Id is the label because they all have the same Id!
                final String topicId = terms[i].getId();
                if (!duplicateList.contains(topicId)) {
                	ingridTopics.add(buildTopicFromTerm(terms[i], plugId, lang));
                    duplicateList.add(topicId);
                }
            }
        }
        return (Topic[]) ingridTopics
                .toArray(new Topic[ingridTopics.size()]);
    }

    /**
     * @return ingrid Topics from Locations.
     */
    private Topic[] copyToTopicArray(Location[] locations, int maxResults,
            String plugId, String lang) throws Exception {
        final List<Topic> ingridTopics =
        	new ArrayList<Topic>();
        final List<String> duplicateList = new ArrayList<String>();

        if (null != locations) {
            int count = Math.min(maxResults, locations.length);
            for (int i = 0; i < count; i++) {
                final String topicId = locations[i].getId();
                // GSSoil Gazetteer delivers null locations and names !?
                if (locations[i] == null || locations[i].getName() == null) {
                	continue;
                }
                if (!duplicateList.contains(topicId)) {
                	ingridTopics.add(buildTopicFromLocation(locations[i], plugId, lang));
                    duplicateList.add(topicId);
                }
            }
        }
        return (Topic[]) ingridTopics
                .toArray(new Topic[ingridTopics.size()]);
    }

    /**
     * Search for a given date events of an requested event type.
     * 
     * @param searchTerm
     *            The search term.
     * @param eventTypes
     *            Array with one or more types of events.
     * @param atDate
     *            A date at which an event occured.
     * @param start
     *            Defines the number of elements to skip.
     * @param length
     *            Number of elements that should be retrieved.
     * @param plugId
     *            The plugId as String.
     * @param totalSize
     *            Has the total size of the query set after the call.
     * @param lang
     *            Is used to specify the preferred language for requests.
     * @return A topic array of events.
     * @throws Exception
     */
    public Topic[] getEventFromTopic(String searchTerm, String[] eventTypes, String atDate,
            int start, int length, String plugId, int[] totalSize, String lang) throws Exception {

        return getEventFromTopic(searchTerm, eventTypes, atDate, atDate, start, length, plugId, totalSize, lang); //result;
    }

    /**
     * Returns all similar terms to a term.
     * 
     * @param searchTerm
     *            The given search term.
     * @param length
     *            Number of elements that should be retrieved.
     * @param plugId
     *            The plugId as String.
     * @param totalSize
     *            The total size of the query set after the call.
     * @param lang
     *            Is used to specify the preferred language for requests.
     * @return Topics to similar terms.
     * @throws Exception
     */
    public Topic[] getSimilarTermsFromTopic(String searchTerm, int length, String plugId,
            int[] totalSize, String lang) throws Exception {
        return getSimilarTermsFromTopic(new String[] { searchTerm }, length, plugId, totalSize, lang);
    }

    /** Calls thesaurusService.getSimilarTermsFromNames !
     * @param searchTerm The given search term(s)
     * @param length Number of elements that should be retrieved.
     * @param plugId The plugId as String.
     * @param totalSize IGNORED
     * @param lang Is used to specify the preferred language for requests.
     * @return Topics to similar terms.
     * @throws Exception
     */
    public Topic[] getSimilarTermsFromTopic(String[] searchTerm, int length, String plugId,
            int[] totalSize, String lang) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("getSimilarTermsFromTopic: searchTerm[]=" + searchTerm + ", lang=" + lang);
        }


        if (log.isDebugEnabled()) {
            log.debug("     !!!!!!!!!! calling API thesaurusService.getSimilarTermsFromNames: " + searchTerm + " ... " + lang);
        }
    	Term[] terms = thesaurusService.getSimilarTermsFromNames(searchTerm, true, new Locale(lang));

        Topic[] result = new Topic[0];
        if (terms != null) {
        	result = copyToTopicArray(terms, length, plugId, lang);
        }

        return result;
    }

    /**
     * Returns all anniversaries to a date.
     * 
     * @param searchTerm
     *            The given search term.
     * @param length
     *            Number of elements that should be retrieved.
     * @param plugId
     *            The plugId as String.
     * @param totalSize
     *            The total size of the query set after the call.
     * @return Topics to an anniversary.
     * @throws Exception
     */
    public Topic[] getAnniversaryFromTopic(String searchTerm, String lang, int length, String plugId,
            int[] totalSize) throws Exception {
        Event[] anniversaries = chronicleService.getAnniversaries(searchTerm, new Locale(lang));
        return toDetailedTopicArray(anniversaries, plugId, lang, length);
    }

	/**
     * Returns all events between two dates.
     * 
     * @param searchTerm
     *            The given search term.
     * @param eventTypes
     *            Array with one or more types of events (mapped to collections).
     * @param fromDate
     *            A date after that an event occured.
     * @param toDate
     *            A date before an event occured.
     * @param start
     *            Defines the number of elements to skip.
     * @param length
     *            Number of elements that should be retrieved.
     * @param plugId
     *            The plugId as String.
     * @param totalSize
     *            Has the total size of the query set after the call.
     * @param lang
     *            Is used to specify the preferred language for requests.
     * @return Topics to an event.
     * @throws Exception
     */
    public Topic[] getEventFromTopic(String searchTerm, String[] eventTypes, String fromDate,
            String toDate, int start, int length, String plugId, int[] totalSize, String lang) throws Exception {
        Topic[] result = new Topic[0];

        // now always "contains" like default on http://www.semantic-network.de/doc_findevents.html?lang=de
        //String searchType = "contains";

        Event[] res = chronicleService.findEventsFromQueryTerm(searchTerm, de.ingrid.external.ChronicleService.MatchingType.CONTAINS, eventTypes, fromDate, toDate, new Locale(lang), start, length);
        result = toDetailedTopicArray(res, plugId, lang, length);

        return result;
    }

    /**
     * Deliver for a given hit the detailed topic description.
     * 
     * @param hit
     *            The hit, for which further information should received.
     * @param lang
     *            Is used to specify the preferred language for requests.
     * @return A detailed topic.
     * @throws Exception
     */
    public DetailedTopic getTopicDetail(IngridHit hit, String lang) throws Exception {
        return getTopicDetail(hit, null, lang);
    }

    /**
     * Get detailed information for a hit.</br>
     * Calls</br>
     * <ul>
     * <li>thesaurusService.getTerm()
     * <li>gazetterService.getLocation()
     * <li>direct getPSI()()
     * </ul>
     * dependent from passed filter.
     * @param hit The hit, for which further information should received.
     * @param filter Topic type as search criterion (only root paths shall be used).
     * @param lang Is used to specify the preferred language for requests.
     * @return A detailed topic to a filter.
     * @throws Exception
     */
    public DetailedTopic getTopicDetail(IngridHit hit, String filter, String lang) throws Exception {
        Topic topic = (Topic) hit;
        return getTopicDetail(topic.getTopicID(), filter, lang, hit.getPlugId());
    }

    /**
     * Get detailed information for topic identified by its id.</br>
     * Calls</br>
     * <ul>
     * <li>thesaurusService.getTerm()
     * <li>gazetterService.getLocation()
     * <li>direct getPSI()()
     * </ul>
     * dependent from passed filter.
     * @param topicID The id of the topic for which further information should received.
     * @param filter Topic type as search criterion (only root paths shall be used).
     * @param lang Is used to specify the preferred language for requests.
     * @param plugId the plug id to be added to detail
     * @return A detailed topic to a filter.
     * @throws Exception
     */
    private DetailedTopic getTopicDetail(String topicID, String filter, String lang, String plugId) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("getTopicDetail: topicID=" + topicID + ", filter=" + filter + ", lang=" + lang);
        }

        DetailedTopic result = null;

    	// TERMS
    	if ("/thesa".equals(filter)) {
            if (log.isDebugEnabled()) {
                log.debug("     !!!!!!!!!! calling API thesaurusService.getTerm: " + topicID + " " + lang);
            }
        	Term term = thesaurusService.getTerm(topicID, new Locale(lang));

            if (term != null) {
            	result = buildDetailedTopicFromTerm(term, plugId, lang);
            }
    		
       	// LOCATIONS
    	} else if ("/location".equals(filter)) {

            if (log.isDebugEnabled()) {
                log.debug("     !!!!!!!!!! calling API gazetteerService.getLocation: " + topicID + " " + lang);
            }
        	Location location = gazetteerService.getLocation(topicID, new Locale(lang));

            if (location != null) {
            	result = buildDetailedTopicFromLocation(location, plugId, lang);
            }

    	} else  {
    		// filter: "/event" or null
    		// TODO: implement else call 
    		Event event = chronicleService.getEvent(topicID, new Locale(lang));
    		
            if (event!= null) {
            	result = buildDetailedTopicFromEvent(event, plugId, lang);
            }
    	}

        return result;
    }

    /**
     * Find for a given topic similar locations. ONLY NON EXPIRED ONES !!!
     * Calls gazetteerService.getRelatedLocationsFromLocation().
    * @param topicId The topic given by Id.
    * @param length Number of elements that should be retrieved.
    * @param plugId The plugId as string.
    * @param totalSize Has the total size of the query set after the call.
    * @param lang Is used to specify the preferred language for requests.
    * @return A topic array from similar location topics (NOT EXPIRED) or null !
    * @throws Exception
    */
    public Topic[] getTopicSimilarLocationsFromTopic(String topicId, int length,
            String plugId, int[] totalSize, String lang) throws Exception {
        Topic[] result = null;

        if (log.isDebugEnabled()) {
            log.debug("     !!!!!!!!!! calling API gazetteerService.getRelatedLocationsFromLocation" + topicId + " " + lang);
        }
        Location[] locations = gazetteerService.getRelatedLocationsFromLocation(topicId, true, new Locale(lang));
        if (locations.length > 0) {
        	totalSize[0] = locations.length;
            result = copyToTopicArray(locations, length, plugId, lang);
        }

        return result;
    }

    /** Calls thesaurusService dependent from passed direction and map results to ingrid Topics.
     * Never includesSiblings ! Never filters expired topics !
     * @param totalSize IGNORED
     * @param associationName IGNORED
     * @param depth IGNORED
     * @param direction "down" -> thesaurusService.getHierarchyNextLevel(...), depth 2</br>
     * 		"up" -> thesaurusService.getHierarchyPathToTop(...), depth 0 (to top)</br>
     * @param includeSiblings IGNORED, always false
     * @param lang the language (e.g. "de")
     * @param root id of root topic (start topic)
     * @param expired return also expired topics ? IGNORED, expired topics are always removed
     * @param plugId the plug id needed for setup of Topics
     * @return structure of ingrid topics
     * @throws Exception
     */
    public Topic[] getTopicHierachy(int[] totalSize, String associationName, long depth,
            String direction, boolean includeSiblings, String lang, String root, boolean expired, String plugId)
            throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("getTopicHierachy: topicID=" + root + ", direction=" + direction + ", lang=" + lang);
        }

    	List<Topic> resultList = new ArrayList<Topic>();

    	if ("down".equals(direction)) {
    		String topicId = root;
            if ("toplevel".equals(root)) {
            	topicId = null;
            }

    		// never with siblings, always depth 2 !
            if (log.isDebugEnabled()) {
                log.debug("     !!!!!!!!!! calling API thesaurusService.getHierarchyNextLevel: " + topicId + " " + lang);
            }
        	TreeTerm[] childTerms = thesaurusService.getHierarchyNextLevel(topicId, new Locale(lang));

            if (log.isDebugEnabled()) {
                log.debug("num childTerms: " + childTerms.length);
            }

        	// set up root topic encapsulating children
    		// NOTICE: default is null ! If no children ! Evaluated in Test.
        	Topic rootTopic = null;
        	if (topicId == null) {
        		// toplevel nodes, we create dummy parent
        		rootTopic = new Topic(plugId, -1, root, null, null, null, null);
        	} else {
        		// start node is existing topic
        		if (childTerms.length > 0) {
        			// there are children ! Every child encapsulates its parent (root).
        			rootTopic = buildTopicFromTerm(childTerms[0].getParents().get(0), plugId, lang);
        		}
        	}
        	resultList.add(rootTopic);
        	
        	// set up children structure
        	for (TreeTerm childTerm : childTerms) {
        		Topic childTopic = buildTopicFromTreeTerm(childTerm, plugId, lang, false);
        		rootTopic.addSuccessor(childTopic);
        	}
        	
        } else {

    		// never with siblings !
            if (log.isDebugEnabled()) {
                log.debug("     !!!!!!!!!! calling API thesaurusService.getHierarchyPathToTop: " + root + " " + lang);
            }
        	TreeTerm startTerm = thesaurusService.getHierarchyPathToTop(root, new Locale(lang));

        	// set up root topic encapsulating parents as successors
    		// NOTICE: default is null ! If no parents ! Evaluated in Test.
        	Topic rootTopic = null;
    		// start node is existing topic
    		if (startTerm.getParents() != null) {
    			// we have parents ! build topic structure with parents as successors !
    			rootTopic = buildTopicFromTreeTerm(startTerm, plugId, lang, true);
    		}
        	resultList.add(rootTopic);
        }

        return resultList.toArray(new Topic[resultList.size()]);
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

	/** Determine FullClassifyService.FilterType from passed SNS filter. */
	private de.ingrid.external.FullClassifyService.FilterType getFullClassifyFilterType(String filterStr) {
		de.ingrid.external.FullClassifyService.FilterType filterType = null;
		if ("/thesa".equals(filterStr)) {
			filterType = de.ingrid.external.FullClassifyService.FilterType.ONLY_TERMS;
    	} else if ("/location".equals(filterStr)) {
			filterType = de.ingrid.external.FullClassifyService.FilterType.ONLY_LOCATIONS;
    	} else  if ("/event".equals(filterStr)) {
			filterType = de.ingrid.external.FullClassifyService.FilterType.ONLY_EVENTS;
    	} 

    	return filterType;
	}
}
