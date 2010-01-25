package de.ingrid.iplug.sns;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.slb.taxi.webservice.xtm.stubs.FieldsType;
import com.slb.taxi.webservice.xtm.stubs.SearchType;
import com.slb.taxi.webservice.xtm.stubs.TopicMapFragment;
import com.slb.taxi.webservice.xtm.stubs.xtm.Association;
import com.slb.taxi.webservice.xtm.stubs.xtm.BaseName;
import com.slb.taxi.webservice.xtm.stubs.xtm.InstanceOf;
import com.slb.taxi.webservice.xtm.stubs.xtm.Member;
import com.slb.taxi.webservice.xtm.stubs.xtm.Occurrence;
import com.slb.taxi.webservice.xtm.stubs.xtm.Scope;
import com.slb.taxi.webservice.xtm.stubs.xtm.Topic;

import de.ingrid.external.FullClassifyService;
import de.ingrid.external.GazetteerService;
import de.ingrid.external.ThesaurusService;
import de.ingrid.external.ThesaurusService.MatchingType;
import de.ingrid.external.om.Event;
import de.ingrid.external.om.FullClassifyResult;
import de.ingrid.external.om.Location;
import de.ingrid.external.om.RelatedTerm;
import de.ingrid.external.om.Term;
import de.ingrid.external.om.TreeTerm;
import de.ingrid.external.om.RelatedTerm.RelationType;
import de.ingrid.external.om.Term.TermType;
import de.ingrid.external.sns.SNSClient;
import de.ingrid.iplug.sns.utils.DetailedTopic;
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

    private static final String SNS_INSTANCE_OF_URL = "http://www.semantic-network.de/xmlns/XTM/2005/2.0/sns-classes_2.0.xtm";
    
    private static final String TEMPORAL_TOOccurrence = "temporalToOcc";

    private static final String TEMPORAL_FROMOccurrence = "temporalFromOcc";

    private static final String TEMPORAL_ATOccurrence = "temporalAtOcc";

    private static final SimpleDateFormat expiredDateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

    private SNSClient fServiceClient;

    private final Class<ThesaurusService> _thesaurusService = null;
    private ThesaurusService thesaurusService;
    private final Class<GazetteerService> _gazetteerService = null;
    private GazetteerService gazetteerService;
    private final Class<FullClassifyService> _fullClassifyService = null;
    private FullClassifyService fullClassifyService;

    private static final String[] fTypeFilters = new String[] { "narrowerTermAssoc", "synonymAssoc",
            "relatedTermsAssoc" };

    private static final String[] fAdministrativeTypes = new String[] { "communityType", "districtType", "quarterType",
            "stateType", "nationType",
            // extend with newest types !
            "use6Type", "use4Type", "use2Type"};

    private String fNativeKeyPrefix;

    /**
     * Constructor for SNS controller.
     * 
     * @param client
     * @param nativeKeyPrefix
     */
    public SNSController(SNSClient client, String nativeKeyPrefix) {
        this.fServiceClient = client;
        this.fNativeKeyPrefix = nativeKeyPrefix;

        SpringUtil springUtil = new SpringUtil("spring/external-services.xml");
        this.thesaurusService = springUtil.getBean("thesaurusService", _thesaurusService);
        this.gazetteerService = springUtil.getBean("gazetteerService", _gazetteerService);
        this.fullClassifyService = springUtil.getBean("fullClassifyService", _fullClassifyService);
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
    public synchronized de.ingrid.iplug.sns.utils.Topic[] getTopicsForTerm(String queryTerm, int start, int maxResults,
            String plugId, int[] totalSize, String lang, boolean expired, boolean includeUse) throws Exception {
        de.ingrid.iplug.sns.utils.Topic[] result = new de.ingrid.iplug.sns.utils.Topic[0];

        de.ingrid.iplug.sns.utils.Topic ingridTopic =
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
    public synchronized de.ingrid.iplug.sns.utils.Topic[] getTopicsForTopic(String topicId, int maxResults,
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

        	List<de.ingrid.iplug.sns.utils.Topic> resultList = new ArrayList<de.ingrid.iplug.sns.utils.Topic>();
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
            	return resultList.toArray(new de.ingrid.iplug.sns.utils.Topic[resultList.size()]);
            }

           	// LOCATIONS
    	} else if ("/location".equals(filter)) {
    		return getTopicSimilarLocationsFromTopic(topicId, maxResults, plugId, totalSize, lang);

    	} else  {
    		// IS THIS EVER CALLED FOR ANOTHER TOPIC TYPE ? All Topics (filter null) ? Then this is executed.
            Map associationTypes = new HashMap();
            Topic topic = new Topic();
            topic.setId(topicId);
            Topic[] associatedTopics = getAssociatedTopics(topic, fTypeFilters, associationTypes, totalSize, expired);
            if (associatedTopics != null) {
                return copyToTopicArray(associatedTopics, associationTypes, maxResults, plugId, "bla");
            }
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
        			false, new Locale(lang));

            if (terms != null) {
            	totalSize[0] = terms.length;
            	result = toDetailedTopicArray(terms, plugId, lang);
            }

       	// LOCATIONS
    	} else if ("/location".equals(filter)) {

            if (log.isDebugEnabled()) {
                log.debug("     !!!!!!!!!! calling API gazetteerService.getLocationsFromText " + lang);
            }
        	Location[] locations = gazetteerService.getLocationsFromText(documentText, maxToAnalyzeWords,
        			false, new Locale(lang));

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
     * @return An array of ingrid detailed topics from an Event array.
     */
    private DetailedTopic[] toDetailedTopicArray(Event[] events, String plugId, String lang) {
        final List<DetailedTopic> returnList = new ArrayList<DetailedTopic>();
        for (Event event : events) {
        	returnList.add(buildDetailedTopicFromEvent(event, plugId, lang));
        }

        return returnList.toArray(new DetailedTopic[returnList.size()]);
    }

    /**
     * @return An array of ingrid detailed topics from a result of a full classification.
     */
    private DetailedTopic[] toDetailedTopicArray(FullClassifyResult classifyResult, String plugId, String lang) {
        final List<DetailedTopic> returnList = new ArrayList<DetailedTopic>();
        DetailedTopic[] topics;
        
        List<Term> classifyTerms = classifyResult.getTerms();
        if (classifyTerms.size() > 0) {
            topics = toDetailedTopicArray(classifyTerms.toArray(new Term[classifyTerms.size()]), plugId, lang);
            if (topics.length > 0) {
                returnList.addAll(Arrays.asList(topics));        	
            }        	
        }

        List<Location> classifyLocations = classifyResult.getLocations();
        if (classifyLocations.size() > 0) {
            topics = toDetailedTopicArray(classifyLocations.toArray(new Location[classifyLocations.size()]), plugId, lang);
            if (topics.length > 0) {
                returnList.addAll(Arrays.asList(topics));        	
            }        	
        }

        List<Event> classifyEvents = classifyResult.getEvents();
        if (classifyEvents.size() > 0) {
            topics = toDetailedTopicArray(classifyEvents.toArray(new Event[classifyEvents.size()]), plugId, lang);
            if (topics.length > 0) {
                returnList.addAll(Arrays.asList(topics));        	
            }        	
        }

        return returnList.toArray(new DetailedTopic[returnList.size()]);
    }

    /**
     * Build a detailed metadata index for a given topic.
     * 
     * @param topic
     *            A given topic.
     * @param plugId
     *            The plugId as String
     * @param lang
     *            Is used to specify the preferred language for requests.
     * @return A detailed topic from Topic.
     */
    private synchronized DetailedTopic buildDetailedTopicFromTopic(Topic topic, String plugId, String lang) {
        String topicId = topic.getId();
        BaseName[] bn = topic.getBaseName();
        String title = "";
        for (int i = 0; i < bn.length; i++) {
            final String href = bn[i].getScope().getTopicRef()[0].getHref();
            if (href.endsWith('#' + lang)) {
                title = topic.getBaseName()[i].getBaseNameString().get_value();
                break;
            }
        }

        String summary = title + ' ' + topic.getInstanceOf()[0].getTopicRef().getHref();
        DetailedTopic metaData = new DetailedTopic(plugId, topicId.hashCode(), topicId, title, summary, null);
        InstanceOf[] instanceOfs = topic.getInstanceOf();
        for (int i = 0; i < instanceOfs.length; i++) {
            String href = instanceOfs[i].getTopicRef().getHref();
            metaData.addToList(DetailedTopic.INSTANCE_OF, href);
        }
        pushTimes(metaData, topic);
        pushDefinitions(metaData, topic, lang);
        pushSamples(metaData, topic, lang);
        pushOccurensie(DetailedTopic.DESCRIPTION_OCC, topic, metaData, lang);
        pushOccurensie(DetailedTopic.ASSOCIATED_OCC, topic, metaData, lang);
        pushOccurensie(DetailedTopic.GEMET_OCC, topic, metaData, lang);
        pushOccurensie(de.ingrid.iplug.sns.utils.Topic.NATIVEKEY_OCC, topic, metaData, lang);
        String topicNativeKey = metaData.getTopicNativeKey();
        if (null != topicNativeKey) {
            metaData.setTopicNativeKey(SNSUtil.transformSpacialReference(this.fNativeKeyPrefix, topicNativeKey));
        } else {
            metaData.setTopicNativeKey(topicId);
        }

        if (containsTypes(fAdministrativeTypes, topic.getInstanceOf()[0].getTopicRef().getHref())) {
            metaData.setAdministrativeID(topic.getId());
        }

        return metaData;
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

        // we push the stuff which IS ALWAYS ADDED, even if empty
        // NOTICE: Further this converts the DetailedTopic to an IngridDocument !!!!?
        pushDefinitions(result, null, lang);
        pushSamples(result, null, lang);
        // NO TERM ASSOCIATIONS MAPPED in EVENT SO FAR !!!
//        pushOccurensie(DetailedTopic.ASSOCIATED_OCC, topic, metaData, lang);

        result.put(DetailedTopic.DESCRIPTION_OCC, event.getDescription());
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
        pushDefinitions(result, null, lang);
        pushSamples(result, null, lang);

        if (location.getNativeKey() != null) {
        	result.setTopicNativeKey(SNSUtil.transformSpacialReference(this.fNativeKeyPrefix, location.getNativeKey()));
        } else {
        	result.setTopicNativeKey(topicId);
        }

        // if administrative location, also set topic id as administrative id !?
        String locationType = location.getTypeId();
        for (String adminType : fAdministrativeTypes) {
        	if (adminType.equals(locationType)) {
        		result.setAdministrativeID(topicId);
        		break;
        	}
        }
        
        // NO BBox !!!? was never delivered !

        return result;
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
        pushDefinitions(result, null, lang);
        pushSamples(result, null, lang);

        pushGemetDataFromTerm(result, term);
        result.setTopicNativeKey(topicId);

        return result;
    }

    /** If GEMET data set in term, adapt topic */
    private void pushGemetDataFromTerm(de.ingrid.iplug.sns.utils.Topic topic, Term term) {
        if (term.getAlternateId() != null) {
        	String gemetOcc = term.getAlternateId() + "@" + term.getName();
        	topic.put(DetailedTopic.GEMET_OCC, gemetOcc);
            // UMTHES name in AlternateName !
            if (term.getAlternateName() != null) {
            	topic.setTopicName(term.getAlternateName());            	
            }
        }
    }

    private void pushDefinitions(DetailedTopic metaData, Topic topic, String lang) {
        List titles = new ArrayList();
        List definitions = new ArrayList();

        Occurrence[] occurrences = null;
        if (topic != null) {
        	occurrences = topic.getOccurrence();
        }
        if (occurrences != null) {
            String type = null;
            for (int i = 0; i < occurrences.length; i++) {
                if (occurrences[i].getInstanceOf() != null) {
                    // Only compare the scope to the language if the element has one set.
                    String scope = '#' + lang;
                    if (occurrences[i].getScope() != null) {
                        scope = occurrences[i].getScope().getTopicRef(0).getHref();
                    }
                    type = occurrences[i].getInstanceOf().getTopicRef().getHref();
                    if (type.endsWith(DetailedTopic.DESCRIPTION_OCC) && occurrences[i].getResourceRef() != null &&
                            scope.endsWith('#' + lang)) {
                        definitions.add(occurrences[i].getResourceRef().getHref().toString());
                        titles.add(occurrences[i].getResourceRef().getTitle());
                    }
                }
            }
        }

        metaData.setDefinitions((String[]) definitions.toArray(new String[definitions.size()]));
        metaData.setDefinitionTitles((String[]) titles.toArray(new String[titles.size()]));
    }

    private void pushSamples(DetailedTopic metaData, Topic topic, String lang) {
        List titles = new ArrayList();
        List samples = new ArrayList();

        Occurrence[] occurrences = null;
        if (topic != null) {
        	occurrences = topic.getOccurrence();
        }
        if (occurrences != null) {
            String type = null;
            for (int i = 0; i < occurrences.length; i++) {
                if (occurrences[i].getInstanceOf() != null) {
                    // Only compare the scope to the language if the element has one set.
                    String scope = '#' + lang;
                    if (occurrences[i].getScope() != null) {
                        scope = occurrences[i].getScope().getTopicRef(0).getHref();
                    }
                    type = occurrences[i].getInstanceOf().getTopicRef().getHref();
                    if (type.endsWith(DetailedTopic.SAMPLE_OCC) && occurrences[i].getResourceRef() != null &&
                            scope.endsWith('#' + lang)) {
                        samples.add(occurrences[i].getResourceRef().getHref().toString());
                        titles.add(occurrences[i].getResourceRef().getTitle());
                    }
                }
            }
        }

        metaData.setSamples((String[]) samples.toArray(new String[samples.size()]));
        metaData.setSampleTitles((String[]) titles.toArray(new String[titles.size()]));
    }

    /**
     * Pushs the time data in to the detailed topic.
     * 
     * @param metaData
     *            The detailed topic for which the time data should be set.
     * @param topic
     *            A given topic.
     */
    private void pushTimes(DetailedTopic metaData, Topic topic) {
        Occurrence[] occurrences = null;
        if (topic != null) {
        	occurrences = topic.getOccurrence();
        }
        if (occurrences != null) {
            String type = null;
            for (int i = 0; i < occurrences.length; i++) {
                if (occurrences[i].getInstanceOf() != null) {
                    type = occurrences[i].getInstanceOf().getTopicRef().getHref();
                    if (type.endsWith(TEMPORAL_ATOccurrence)) {
                        final String at = occurrences[i].getResourceData().get_value();
                        metaData.setFrom(at);
                        metaData.setTo(at);
                        break;
                    }
                    if (type.endsWith(TEMPORAL_FROMOccurrence)) {
                        metaData.setFrom(occurrences[i].getResourceData().get_value());
                    }
                    if (type.endsWith(TEMPORAL_TOOccurrence)) {
                        metaData.setTo(occurrences[i].getResourceData().get_value());
                    }
                }
            }
        }
    }

    private synchronized void pushOccurensie(String occType, Topic topic,
            de.ingrid.iplug.sns.utils.Topic detailedTopic, String lang) {
        Occurrence[] occurrences = null;
        if (topic != null) {
        	occurrences = topic.getOccurrence();
        }
        if (occurrences != null) {
            String type = null;
            for (int i = 0; i < occurrences.length; i++) {
                if (occurrences[i].getInstanceOf() != null) {
                    // Only compare the scope to the language if the element has one set.
                    String scope = '#' + lang;
                    if (occurrences[i].getScope() != null) {
                        scope = occurrences[i].getScope().getTopicRef(0).getHref();
                    }
                    type = occurrences[i].getInstanceOf().getTopicRef().getHref();
                    if (type.endsWith(occType) && occurrences[i].getResourceData() != null &&
                            scope.endsWith('#' + lang)) {
                        detailedTopic.put(occType, occurrences[i].getResourceData().get_value());
                    }
                }
            }
        }
    }

    /**
     * @param topic
     * @param plugId
     * @param associationType
     * @param lang
     * @return A ingrid topic from a Topic.
     */
    private synchronized de.ingrid.iplug.sns.utils.Topic buildTopicFromTopic(Topic topic, String plugId,
            String associationType, String lang) {
        BaseName[] baseNames = topic.getBaseName();
        // Set a default if for the selected language nothing exists.
        String title = baseNames[0].getBaseNameString().get_value();
        String topicLang = "en";
        for (int i = 0; i < baseNames.length; i++) {
            final Scope scope = baseNames[i].getScope();
            if (scope != null) {
                final String href = scope.getTopicRef()[0].getHref();
                if (href.endsWith('#' + lang)) {
                    title = baseNames[i].getBaseNameString().get_value();
                    topicLang = lang;
                    break;
                }
            }
        }

        String summary = title + ' ' + topic.getInstanceOf()[0].getTopicRef().getHref();
        String topicId = topic.getId();
        de.ingrid.iplug.sns.utils.Topic result = new de.ingrid.iplug.sns.utils.Topic(plugId, topicId.hashCode(),
                topicId, title, summary, associationType, null);
        pushOccurensie(de.ingrid.iplug.sns.utils.Topic.NATIVEKEY_OCC, topic, result, lang);
        pushOccurensie(DetailedTopic.GEMET_OCC, topic, result, lang);
        String topicNativeKey = result.getTopicNativeKey();
        if (null != topicNativeKey) {
            String ags = SNSUtil.transformSpacialReference(this.fNativeKeyPrefix, topicNativeKey);
            if (ags.startsWith("lawa:")) {
                ags = SNSUtil.transformSpacialReference("lawa:", topicNativeKey);
            }
            result.setTopicNativeKey(ags);
        } else {
            result.setTopicNativeKey(topicId);
        }
        result.setLanguage(topicLang);
        return result;
    }

    /**
     * @return A ingrid topic from a Term.
     */
    private de.ingrid.iplug.sns.utils.Topic buildTopicFromTerm(Term term, String plugId, String lang) {
        String title = term.getName();
        String summary = title + ' ' + getSNSInstanceOf(term);
        String topicId = term.getId();
        String associationType = "";
        de.ingrid.iplug.sns.utils.Topic result = new de.ingrid.iplug.sns.utils.Topic(plugId, topicId.hashCode(),
                topicId, title, summary, associationType, null);
        pushGemetDataFromTerm(result, term);
        result.setLanguage(lang);

        return result;
    }

    /**
     * @return A ingrid topic from a Location.
     */
    private de.ingrid.iplug.sns.utils.Topic buildTopicFromLocation(Location location, String plugId, String lang) {
        String title = location.getName();
        String summary = title + ' ' + getSNSInstanceOf(location);
        String topicId = location.getId();
        String associationType = "";
        de.ingrid.iplug.sns.utils.Topic result = new de.ingrid.iplug.sns.utils.Topic(plugId, topicId.hashCode(),
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
    private de.ingrid.iplug.sns.utils.Topic buildTopicFromTreeTerm(TreeTerm term, String plugId, String lang,
    		boolean addParentsAsSuccessors) {
    	de.ingrid.iplug.sns.utils.Topic resultTopic = buildTopicFromTerm(term, plugId, lang);
    	
    	// add children or parents as successors dependent from flag
    	List<TreeTerm> successorTerms = term.getChildren();
    	if (addParentsAsSuccessors) {
    		successorTerms = term.getParents();
    	}

    	if (successorTerms != null) {
        	for (TreeTerm successorTerm : successorTerms) {
        		de.ingrid.iplug.sns.utils.Topic successorTopic =
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
    private de.ingrid.iplug.sns.utils.Topic buildTopicFromRelatedTerm(RelatedTerm term, String plugId, String lang) {
    	de.ingrid.iplug.sns.utils.Topic resultTopic = buildTopicFromTerm(term, plugId, lang);

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
		return SNS_INSTANCE_OF_URL + "#" + event.getTypeId();
    }

    /** Extract SNS instanceOf href from location ! */
    private static String getSNSInstanceOf(Location location) {
		return SNS_INSTANCE_OF_URL + "#" + location.getTypeId();
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

		return SNS_INSTANCE_OF_URL + snsInstanceOf;
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
			
		return SNS_INSTANCE_OF_URL + snsMemberType;
    }

    /**
     * @param baseTopic
     * @param typePattern
     * @param associationTypes
     * @param totalSize
     * @param expired return also expired topics ?
     * @return Topic array of associated topics filter by the given patterns
     * @throws Exception
     */
    private Topic[] getAssociatedTopics(Topic baseTopic, String[] typePattern, Map associationTypes, int[] totalSize,
            boolean expired) throws Exception {
        List resultList = new ArrayList();

        final TopicMapFragment mapFragment = this.fServiceClient.getPSI(baseTopic.getId(), 1, null);
        final Topic[] topics = mapFragment.getTopicMap().getTopic();
        if (null != mapFragment.getListExcerpt()) {
            if (null != mapFragment.getListExcerpt().getTotalSize()) {
                totalSize[0] = mapFragment.getListExcerpt().getTotalSize().intValue();
            }
        }
        final Association[] associations = mapFragment.getTopicMap().getAssociation();
        // iterate through associations to find the correct association types
        if (associations != null) {
            for (int i = 0; i < associations.length; i++) {
                final Association association = associations[i];
                // association type
                final String assocType = association.getInstanceOf().getTopicRef().getHref();
                if (containsTypes(typePattern, assocType)) {
                    // association members are the basetopic and it association
                    final Member[] members = association.getMember();
                    for (int j = 0; j < members.length; j++) {
                        final Member member = members[j];
                        // here is only the topic id available
                        final String topicId = member.getTopicRef()[0].getHref();
                        final String assocMember = member.getRoleSpec().getTopicRef().getHref();
                        if (!topicId.equals(baseTopic.getId())) {
                            final Topic topicById = getTopicById(topics, topicId);
                            if (topicById != null) {
                                if (!expired) {
                                    Date expiredDate = getExpiredDate(topicById);
                                    if ((null != expiredDate) && expiredDate.before(new Date())) {
                                        continue;
                                    }
                                }
                                if (null != associationTypes) {
                                    associationTypes.put(topicById.getId(), assocMember);
                                }
                                resultList.add(topicById);
                            }
                        }
                    }
                }
            }

            return (Topic[]) resultList.toArray(new Topic[resultList.size()]);
        }

        return null;
    }

    /**
     * @param topics
     * @param topicId
     * @return the topic that match the topicId from the given Topic array
     */
    private Topic getTopicById(Topic[] topics, String topicId) {
        for (int k = 0; k < topics.length; k++) {
            if (topicId.equals(topics[k].getId())) {
                return topics[k];
            }
        }

        return null;
    }

    /**
     * ATTENTION: This method had a bug when using the old controller before introducing the ThesaurusService API !
     * top and label topics were treated as descriptors !!! But not non descriptor topics !
     * WE STILL SIMULATE THIS BUG HERE !!! Cause don't know where this query is called ! 
     * @return just one matching topic, in case more topics match or no topic match we return null
     * @throws Exception
     */
    private de.ingrid.iplug.sns.utils.Topic getThesaurusDescriptorTopic(String queryTerm, int[] totalSize, String lang,
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

        // only return result if exactly ONE topic found !
        de.ingrid.iplug.sns.utils.Topic result = null;
        if (filteredTerms.size() == 1) {
        	result = buildTopicFromTerm(filteredTerms.get(0), plugId, lang);
        	totalSize[0] = 1;
        }

        return result;
    }

    /**
     * @param patterns
     * @param pattern
     * @return true in case the pattern is one of the given patterns
     */
    private boolean containsTypes(String[] patterns, String pattern) {
        for (int i = 0; i < patterns.length; i++) {
            if (pattern.endsWith(patterns[i])) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param maxResults
     * @param topics
     * @param associationTypes
     * @param plugId
     * @param lang
     * @return An array of Topic with the given length.
     * @throws Exception
     */
    private de.ingrid.iplug.sns.utils.Topic[] copyToTopicArray(Topic[] topics, Map associationTypes, int maxResults,
            String plugId, String lang) throws Exception {
        final List ingridTopics = new ArrayList();
        final List duplicateList = new ArrayList();

        if (null != topics) {
            int count = Math.min(maxResults, topics.length);
            for (int i = 0; i < count; i++) {
                final String topicId = topics[i].getId();
                if (!duplicateList.contains(topicId)) {
                    if (!topicId.startsWith("_Interface")) {
                        String associationType = "";
                        if ((null != associationTypes) && (associationTypes.containsKey(topicId))) {
                            associationType = (String) associationTypes.get(topicId);
                        }
                        ingridTopics.add(buildTopicFromTopic(topics[i], plugId, associationType, lang));
                    }
                    duplicateList.add(topicId);
                }
            }
        }
        return (de.ingrid.iplug.sns.utils.Topic[]) ingridTopics
                .toArray(new de.ingrid.iplug.sns.utils.Topic[ingridTopics.size()]);
    }

    /**
     * @return ingrid Topics from Terms.
     */
    private de.ingrid.iplug.sns.utils.Topic[] copyToTopicArray(Term[] terms, int maxResults,
            String plugId, String lang) throws Exception {
        final List<de.ingrid.iplug.sns.utils.Topic> ingridTopics =
        	new ArrayList<de.ingrid.iplug.sns.utils.Topic>();
        final List<String> duplicateList = new ArrayList<String>();

        if (null != terms) {
            int count = Math.min(maxResults, terms.length);
            for (int i = 0; i < count; i++) {
                final String topicId = terms[i].getId();
                if (!duplicateList.contains(topicId)) {
                	ingridTopics.add(buildTopicFromTerm(terms[i], plugId, lang));
                    duplicateList.add(topicId);
                }
            }
        }
        return (de.ingrid.iplug.sns.utils.Topic[]) ingridTopics
                .toArray(new de.ingrid.iplug.sns.utils.Topic[ingridTopics.size()]);
    }

    /**
     * @return ingrid Topics from Locations.
     */
    private de.ingrid.iplug.sns.utils.Topic[] copyToTopicArray(Location[] locations, int maxResults,
            String plugId, String lang) throws Exception {
        final List<de.ingrid.iplug.sns.utils.Topic> ingridTopics =
        	new ArrayList<de.ingrid.iplug.sns.utils.Topic>();
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
        return (de.ingrid.iplug.sns.utils.Topic[]) ingridTopics
                .toArray(new de.ingrid.iplug.sns.utils.Topic[ingridTopics.size()]);
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
    public de.ingrid.iplug.sns.utils.Topic[] getEventFromTopic(String searchTerm, String[] eventTypes, String atDate,
            int start, int length, String plugId, int[] totalSize, String lang) throws Exception {
        de.ingrid.iplug.sns.utils.Topic[] result = new de.ingrid.iplug.sns.utils.Topic[0];
        String[] eventPath = null;

        if (null != eventTypes) {
            eventPath = new String[eventTypes.length];
            for (int i = 0; i < eventPath.length; i++) {
                eventPath[i] = "/event/" + eventTypes[i] + '/';
            }
        } else {
            eventPath = new String[] { "/event/" };
        }

        SearchType searchType = SearchType.exact;
        if ((null == searchTerm) || (searchTerm.trim().equals(""))) {
            searchType = SearchType.contains;
        }
        TopicMapFragment topicMapFragment = this.fServiceClient.findEvents(searchTerm, true, searchType, eventPath,
                FieldsType.captors, start, atDate, lang, length);
        Topic[] topic = topicMapFragment.getTopicMap().getTopic();
        totalSize[0] = topicMapFragment.getListExcerpt().getTotalSize().intValue();
        if (topic != null) {
            de.ingrid.iplug.sns.utils.Topic[] topics = copyToTopicArray(topic, null, length, plugId, lang);
            result = topics;
        }

        return result;
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
    public de.ingrid.iplug.sns.utils.Topic[] getSimilarTermsFromTopic(String searchTerm, int length, String plugId,
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
    public de.ingrid.iplug.sns.utils.Topic[] getSimilarTermsFromTopic(String[] searchTerm, int length, String plugId,
            int[] totalSize, String lang) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("getSimilarTermsFromTopic: searchTerm[]=" + searchTerm + ", lang=" + lang);
        }


        if (log.isDebugEnabled()) {
            log.debug("     !!!!!!!!!! calling API thesaurusService.getSimilarTermsFromNames: " + searchTerm + " ... " + lang);
        }
    	Term[] terms = thesaurusService.getSimilarTermsFromNames(searchTerm, true, new Locale(lang));

        de.ingrid.iplug.sns.utils.Topic[] result = new de.ingrid.iplug.sns.utils.Topic[0];
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
    public de.ingrid.iplug.sns.utils.Topic[] getAnniversaryFromTopic(String searchTerm, int length, String plugId,
            int[] totalSize) throws Exception {
        de.ingrid.iplug.sns.utils.Topic[] result = new de.ingrid.iplug.sns.utils.Topic[0];

        TopicMapFragment topicMapFragment = this.fServiceClient.anniversary(searchTerm);
        Topic[] topic = topicMapFragment.getTopicMap().getTopic();
        totalSize[0] = topicMapFragment.getListExcerpt().getTotalSize().intValue();
        if (topic != null) {
            de.ingrid.iplug.sns.utils.Topic[] topics = copyToTopicArray(topic, null, length, plugId, "bla");
            result = topics;
        }

        return result;
    }

    /**
     * Returns all events between two dates.
     * 
     * @param searchTerm
     *            The given search term.
     * @param eventTypes
     *            Array with one or more types of events.
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
    public de.ingrid.iplug.sns.utils.Topic[] getEventFromTopic(String searchTerm, String[] eventTypes, String fromDate,
            String toDate, int start, int length, String plugId, int[] totalSize, String lang) throws Exception {
        de.ingrid.iplug.sns.utils.Topic[] result = new de.ingrid.iplug.sns.utils.Topic[0];
        String[] eventPath = null;

        if (null != eventTypes) {
            eventPath = new String[eventTypes.length];
            for (int i = 0; i < eventPath.length; i++) {
                eventPath[i] = "/event/" + eventTypes[i] + '/';
            }
        } else {
            eventPath = new String[] { "/event/" };
        }

        SearchType searchType = SearchType.exact;
        if ((null == searchTerm) || (searchTerm.trim().equals(""))) {
            searchType = SearchType.contains;
        }
        TopicMapFragment topicMapFragment = this.fServiceClient.findEvents(searchTerm, true, searchType, eventPath,
                FieldsType.captors, start, fromDate, toDate, lang, length);
        Topic[] topic = topicMapFragment.getTopicMap().getTopic();
        totalSize[0] = topicMapFragment.getListExcerpt().getTotalSize().intValue();
        if (topic != null) {
            de.ingrid.iplug.sns.utils.Topic[] topics = copyToTopicArray(topic, null, length, plugId, lang);
            result = topics;
        }

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
        de.ingrid.iplug.sns.utils.Topic topic = (de.ingrid.iplug.sns.utils.Topic) hit;
        String topicID = topic.getTopicID();
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
            	result = buildDetailedTopicFromTerm(term, hit.getPlugId(), lang);
            }
    		
       	// LOCATIONS
    	} else if ("/location".equals(filter)) {

            if (log.isDebugEnabled()) {
                log.debug("     !!!!!!!!!! calling API gazetteerService.getLocation: " + topicID + " " + lang);
            }
        	Location location = gazetteerService.getLocation(topicID, new Locale(lang));

            if (location != null) {
            	result = buildDetailedTopicFromLocation(location, hit.getPlugId(), lang);
            }

    	} else  {
    		// filter: "/event" or null
            TopicMapFragment mapFragment = this.fServiceClient.getPSI(topicID, 0, filter);
            if (null != mapFragment) {
                Topic[] topics = mapFragment.getTopicMap().getTopic();

                for (int i = 0; i < topics.length; i++) {
                    if (topics[i].getId().equals(topicID)) {
                        result = buildDetailedTopicFromTopic(topics[0], hit.getPlugId(), lang);
                    }
                }
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
    public de.ingrid.iplug.sns.utils.Topic[] getTopicSimilarLocationsFromTopic(String topicId, int length,
            String plugId, int[] totalSize, String lang) throws Exception {
        de.ingrid.iplug.sns.utils.Topic[] result = null;

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

    private Date getExpiredDate(Topic topic) {
        Date result = null;
        Occurrence[] occurrences = topic.getOccurrence();
        if (null != occurrences) {
            for (int i = 0; i < occurrences.length; i++) {
                final InstanceOf instanceOf = occurrences[i].getInstanceOf();
                if (instanceOf != null) {
                    final String type = instanceOf.getTopicRef().getHref();
                    if (type.endsWith("expiredOcc")) {
                        try {
                            result = expiredDateParser.parse(occurrences[i].getResourceData().get_value());
                        } catch (ParseException e) {
                            log.error("Not expected date format in sns expiredOcc.", e);
                        }
                    }
                }
            }
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
    public de.ingrid.iplug.sns.utils.Topic[] getTopicHierachy(int[] totalSize, String associationName, long depth,
            String direction, boolean includeSiblings, String lang, String root, boolean expired, String plugId)
            throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("getTopicHierachy: topicID=" + root + ", direction=" + direction + ", lang=" + lang);
        }

    	List<de.ingrid.iplug.sns.utils.Topic> resultList = new ArrayList<de.ingrid.iplug.sns.utils.Topic>();

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
        	
        	// set up root topic encapsulating children
    		// NOTICE: default is null ! If no children ! Evaluated in Test.
        	de.ingrid.iplug.sns.utils.Topic rootTopic = null;
        	if (topicId == null) {
        		// toplevel nodes, we create dummy parent
        		rootTopic = new de.ingrid.iplug.sns.utils.Topic(plugId, -1, root, null, null, null, null);
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
        		de.ingrid.iplug.sns.utils.Topic childTopic = buildTopicFromTreeTerm(childTerm, plugId, lang, false);
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
        	de.ingrid.iplug.sns.utils.Topic rootTopic = null;
    		// start node is existing topic
    		if (startTerm.getParents() != null) {
    			// we have parents ! build topic structure with parents as successors !
    			rootTopic = buildTopicFromTreeTerm(startTerm, plugId, lang, true);
    		}
        	resultList.add(rootTopic);
        }

        return resultList.toArray(new de.ingrid.iplug.sns.utils.Topic[resultList.size()]);
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
