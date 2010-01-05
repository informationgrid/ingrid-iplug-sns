package de.ingrid.iplug.sns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import de.ingrid.external.om.Term;
import de.ingrid.external.om.TreeTerm;
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

    private static final String TEMPORAL_TOOccurrence = "temporalToOcc";

    private static final String TEMPORAL_FROMOccurrence = "temporalFromOcc";

    private static final String TEMPORAL_ATOccurrence = "temporalAtOcc";

    private static final String SYNONYM_TYPE = "synonymType";

    private static final String THESAURUS_DESCRIPTOR = "/thesa/descriptor";

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
            "stateType", "nationType" };

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
     * For a given term (that should be a topic itself) an array of associated topics will returned.
     * 
     * @param queryTerm
     *            The query term.
     * @param start
     *            The start offset.
     * @param maxResults
     *            Limit number of results.
     * @param plugId
     *            The plugId as String.
     * @param totalSize
     *            The quantity of the found topics altogether.
     * @param lang
     *            Is used to specify the preferred language for requests.
     * @param expired
     * @param includeUse
     * @return an array of assiciated topics or null in case the term itself is not found as topic
     * @throws Exception
     */
    public synchronized de.ingrid.iplug.sns.utils.Topic[] getTopicsForTerm(String queryTerm, int start, int maxResults,
            String plugId, int[] totalSize, String lang, boolean expired, boolean includeUse) throws Exception {
        Map associationTypes = new HashMap();
        de.ingrid.iplug.sns.utils.Topic[] result = new de.ingrid.iplug.sns.utils.Topic[0];

        Topic topic = getTopic(queryTerm, THESAURUS_DESCRIPTOR, start, totalSize, lang, includeUse);
        if (topic != null) {
            Topic[] associatedTopics = getAssociatedTopics(topic, fTypeFilters, associationTypes, totalSize, expired);
            de.ingrid.iplug.sns.utils.Topic[] topics = copyToTopicArray(associatedTopics, associationTypes, maxResults,
                    plugId, lang);
            result = topics;
        }

        return result;
    }

    /**
     * For a given topic (identified by id) an array of associated topics will returned.
     * 
     * @param topicId
     *            The topic given by Id.
     * @param maxResults
     *            Limit number of results.
     * @param plugId
     *            The plugId as String
     * @param totalSize
     *            The quantity of the found topics altogether.
     * @param expired
     *            If true return also expired topics.
     * @return an array of associated topics for a type identified by id
     * @throws Exception
     */
    public synchronized de.ingrid.iplug.sns.utils.Topic[] getTopicsForTopic(String topicId, int maxResults,
            String plugId, int[] totalSize, boolean expired) throws Exception {
        Map associationTypes = new HashMap();
        Topic topic = new Topic();
        topic.setId(topicId);
        Topic[] associatedTopics = getAssociatedTopics(topic, fTypeFilters, associationTypes, totalSize, expired);
        if (associatedTopics != null) {
            return copyToTopicArray(associatedTopics, associationTypes, maxResults, plugId, "bla");
        }

        return null;
    }

    /**
     * For a given text an array of detailed topics will returned.
     * 
     * @param documentText
     *            The given text to analyze.
     * @param maxToAnalyzeWords
     *            Analyze only the first maxToAnalyzeWords words of the document in the body.
     * @param filter
     *            Topic type as search criterion (only root paths may be used).
     * @param plugId
     *            The plugId as String.
     * @param lang
     *            Is used to specify the preferred language for requests.
     * @param totalSize
     *            The quantity of the found topics altogether.
     * @param expired
     * @return array of detailed topics for the given text
     * @throws Exception
     */
    public DetailedTopic[] getTopicsForText(String documentText, int maxToAnalyzeWords, String filter, String plugId,
            String lang, int[] totalSize, boolean expired) throws Exception {
        final TopicMapFragment mapFragment = this.fServiceClient.autoClassify(documentText, maxToAnalyzeWords, filter,
                false, lang);
        final Topic[] topics = mapFragment.getTopicMap().getTopic();
        if (null != mapFragment.getListExcerpt()) {
            totalSize[0] = mapFragment.getListExcerpt().getTotalSize().intValue();
        }

        if (topics != null) {
            return toDetailedTopicArray(topics, plugId, lang, expired);
        }

        return new DetailedTopic[0];
    }

    /**
     * For a given URL an array of detailed topics will returned.
     * 
     * @param url
     *            The given url to analyze.
     * @param maxToAnalyzeWords
     *            Analyze only the first maxToAnalyzeWords words of the document in the body.
     * @param filter
     *            Topic type as search criterion (only root paths may be used).
     * @param plugId
     *            The plugId as String.
     * @param lang
     *            Is used to specify the preferred language for requests.
     * @param totalSize
     *            The quantity of the found topics altogether.
     * @param expired
     * @return array of detailed topics for the given text
     * @throws Exception
     */
    public DetailedTopic[] getTopicsForURL(String url, int maxToAnalyzeWords, String filter, String plugId,
            String lang, int[] totalSize, boolean expired) throws Exception {
        final TopicMapFragment mapFragment = this.fServiceClient.autoClassifyToUrl(url, maxToAnalyzeWords, filter,
                false, lang);
        final Topic[] topics = mapFragment.getTopicMap().getTopic();
        if (null != mapFragment.getListExcerpt()) {
            totalSize[0] = mapFragment.getListExcerpt().getTotalSize().intValue();
        }

        if (topics != null) {
            return toDetailedTopicArray(topics, plugId, lang, expired);
        }

        return new DetailedTopic[0];
    }

    /**
     * For a given topic array an array of associated topics which are not synonymous one will returned.
     * 
     * @param topics
     *            Array of given topics.
     * @param plugId
     *            The plugId as String.
     * @param lang
     *            Is used to specify the preferred language for requests.
     * @return an array of detailed topics, we ignoring all topics of typ synonymType
     */
    private DetailedTopic[] toDetailedTopicArray(Topic[] topics, String plugId, String lang, boolean expired) {
        final List returnList = new ArrayList();
        for (int i = 0; i < topics.length; i++) {
            if (!topics[i].getInstanceOf()[0].getTopicRef().getHref().endsWith(SYNONYM_TYPE)) {
                if (!expired) {
                    Date expiredDate = getExpiredDate(topics[i]);
                    if ((null != expiredDate) && expiredDate.before(new Date())) {
                        continue;
                    }
                }
                returnList.add(buildDetailedTopicFromTopic(topics[i], plugId, lang));
            }
        }

        return (DetailedTopic[]) returnList.toArray(new DetailedTopic[returnList.size()]);
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

    private void pushDefinitions(DetailedTopic metaData, Topic topic, String lang) {
        Occurrence[] occurrences = topic.getOccurrence();
        List titles = new ArrayList();
        List definitions = new ArrayList();

        String type = null;
        if (occurrences != null) {
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
        Occurrence[] occurrences = topic.getOccurrence();
        List titles = new ArrayList();
        List samples = new ArrayList();

        String type = null;
        if (occurrences != null) {
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
        Occurrence[] occurrences = topic.getOccurrence();
        String type = null;
        if (occurrences != null) {
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
        Occurrence[] occurrences = topic.getOccurrence();
        String type = null;
        if (occurrences != null) {
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
        String summary = title + ' ' + getTypeFromTerm(term);
        String topicId = term.getId();
        String associationType = "";
        de.ingrid.iplug.sns.utils.Topic result = new de.ingrid.iplug.sns.utils.Topic(plugId, topicId.hashCode(),
                topicId, title, summary, associationType, null);
        // if GEMET adapt data
        if (term.getAlternateId() != null) {
        	String gemetOcc = term.getAlternateId() + "@" + term.getName();
            result.put(DetailedTopic.GEMET_OCC, gemetOcc);
            // UMTHES name in AlternateName !
            if (term.getAlternateName() != null) {
                result.setTopicName(term.getAlternateName());            	
            }
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

    private static String getTypeFromTerm(Term term) {
    	// first check whether we have a tree term ! Only then we can determine whether top node !
		if (TreeTerm.class.isAssignableFrom(term.getClass())) {
	    	if (((TreeTerm)term).getParents() == null) {
	    		return "#topTermType";
	    	}    	
		}

    	TermType termType = term.getType();
    	if (termType == TermType.NODE_LABEL) 
			return "#nodeLabelType";
		if (termType == TermType.DESCRIPTOR) 
			return "#descriptorType";
		if (termType == TermType.NON_DESCRIPTOR) 
			return "#nonDescriptorType";
		return "#topTermType";
    }

    /**
     * @param baseTopic
     * @param typePattern
     * @param associationTypes
     * @param totalSize
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
     * @param queryTerm
     * @param topicType
     * @param offSet
     * @param totalSize
     * @param lang
     * @param includeUse
     * @return just one matching topic, in case more topics match or no topic match we return null
     * @throws Exception
     */
    private Topic getTopic(String queryTerm, String topicType, long offSet, int[] totalSize, String lang,
            boolean includeUse) throws Exception {
        // changed from FieldsType.captors to FieldTypes.names
        TopicMapFragment mapFragment = this.fServiceClient.findTopics(queryTerm, topicType, SearchType.exact,
                FieldsType.names, offSet, lang, includeUse);
        if (null != mapFragment) {
            totalSize[0] = mapFragment.getListExcerpt().getTotalSize().intValue();
            Topic[] topics = mapFragment.getTopicMap().getTopic();
            if ((null != topics) && (topics.length == 1)) {
                return topics[0];
            }
        }

        return null;
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
        duplicateList.clear();
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

    /**
     * Returns all similar terms to an array of terms.
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
    public de.ingrid.iplug.sns.utils.Topic[] getSimilarTermsFromTopic(String[] searchTerm, int length, String plugId,
            int[] totalSize, String lang) throws Exception {
        de.ingrid.iplug.sns.utils.Topic[] result = new de.ingrid.iplug.sns.utils.Topic[0];

        TopicMapFragment topicMapFragment = this.fServiceClient.getSimilarTerms(true, searchTerm, lang);
        Topic[] topic = topicMapFragment.getTopicMap().getTopic();
        if (null != topicMapFragment.getListExcerpt()) {
            totalSize[0] = topicMapFragment.getListExcerpt().getTotalSize().intValue();
        }

        if (topic != null) {
            de.ingrid.iplug.sns.utils.Topic[] topics = copyToTopicArray(topic, null, length, plugId, lang);
            result = topics;
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
     * Get detailed information for a hit.
     * 
     * @param hit
     *            The hit, for which further information should received.
     * @param filter
     *            Topic type as search criterion (only root paths may be used).
     * @param lang
     *            Is used to specify the preferred language for requests.
     * @return A detailed topic to a filter.
     * @throws Exception
     */
    public DetailedTopic getTopicDetail(IngridHit hit, String filter, String lang) throws Exception {
        de.ingrid.iplug.sns.utils.Topic topic = (de.ingrid.iplug.sns.utils.Topic) hit;
        String topicID = topic.getTopicID();
        DetailedTopic result = null;

        TopicMapFragment mapFragment = this.fServiceClient.getPSI(topicID, 0, filter);
        if (null != mapFragment) {
            Topic[] topics = mapFragment.getTopicMap().getTopic();

            for (int i = 0; i < topics.length; i++) {
                if (topics[i].getId().equals(topicID)) {
                    result = buildDetailedTopicFromTopic(topics[0], hit.getPlugId(), lang);
                }
            }
        }

        return result;
    }

    /**
     * Find for a given topic similar locations.
     * 
     * @param topicId
     *            The topic given by Id.
     * @param length
     *            Number of elements that should be retrieved.
     * @param plugId
     *            The plugId as string.
     * @param totalSize
     *            Has the total size of the query set after the call.
     * @param expired
     * @return A topic array from similar location topics.
     * @throws Exception
     */
    public de.ingrid.iplug.sns.utils.Topic[] getTopicSimilarLocationsFromTopic(String topicId, int length,
            String plugId, int[] totalSize, boolean expired) throws Exception {
        de.ingrid.iplug.sns.utils.Topic[] result = null;

        TopicMapFragment mapFragment = this.fServiceClient.getPSI(topicId, 0, "/location/");
        if (null != mapFragment) {
            if (null != mapFragment.getListExcerpt().getTotalSize()) {
                totalSize[0] = mapFragment.getListExcerpt().getTotalSize().intValue();
            }
            Topic[] topics = mapFragment.getTopicMap().getTopic();
            if (!expired) {
                List expiredTopics = new ArrayList();
                for (int i = 0; i < topics.length; i++) {
                    Date expiredDate = getExpiredDate(topics[i]);
                    if ((null != expiredDate) && expiredDate.before(new Date())) {
                        continue;
                    }
                    expiredTopics.add(topics[i]);
                }
                topics = (Topic[]) expiredTopics.toArray(new Topic[expiredTopics.size()]);
            }
            result = copyToTopicArray(topics, null, length, plugId, "bla");
        }

        return result;
    }

    /**
     * For a given text an array of detailed topics will returned (synchronized version).
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
     * @param expired
     * @return Array of detailed topics for the given text.
     * @throws Exception
     */
    public synchronized DetailedTopic[] getTopicsForText(String searchTerm, int maxToAnalyzeWords, String plugId,
            String lang, int[] totalSize, boolean expired) throws Exception {
        return getTopicsForText(searchTerm, maxToAnalyzeWords, null, plugId, lang, totalSize, expired);
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
     * @param totalSize ignored
     * @param associationName ignored
     * @param depth ignored
     * @param direction "down" -> thesaurusService.getHierarchyNextLevel(...), depth 2</br>
     * 		"up" -> thesaurusService.getHierarchyPathToTop(...), depth 0 (to top)</br>
     * @param includeSiblings ignored, always false
     * @param lang the language (e.g. "de")
     * @param root id of root topic (start topic)
     * @param expired ignored, always false (no filtering of expired topics)
     * @param plugId the plug id needed for setup of Topics
     * @return structure of ingrid topics
     * @throws Exception
     */
    public de.ingrid.iplug.sns.utils.Topic[] getTopicHierachy(int[] totalSize, String associationName, long depth,
            String direction, boolean includeSiblings, String lang, String root, boolean expired, String plugId)
            throws Exception {
    	List<de.ingrid.iplug.sns.utils.Topic> resultList = new ArrayList<de.ingrid.iplug.sns.utils.Topic>();

    	if ("down".equals(direction)) {
    		String topicId = root;
            if ("toplevel".equals(root)) {
            	topicId = null;
            }

    		// never with siblings, always depth 2 !
            if (log.isDebugEnabled()) {
                log.debug("calling API thesaurusService.getHierarchyNextLevel: " + topicId + " " + lang);
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
                log.debug("calling API thesaurusService.getHierarchyNextLevel: " + root + " " + lang);
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
}
