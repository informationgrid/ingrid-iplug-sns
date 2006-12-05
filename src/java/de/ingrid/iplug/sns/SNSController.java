package de.ingrid.iplug.sns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.slb.taxi.webservice.xtm.stubs.FieldsType;
import com.slb.taxi.webservice.xtm.stubs.SearchType;
import com.slb.taxi.webservice.xtm.stubs._topicMapFragment;
import com.slb.taxi.webservice.xtm.stubs.xtm._association;
import com.slb.taxi.webservice.xtm.stubs.xtm._baseName;
import com.slb.taxi.webservice.xtm.stubs.xtm._instanceOf;
import com.slb.taxi.webservice.xtm.stubs.xtm._member;
import com.slb.taxi.webservice.xtm.stubs.xtm._occurrence;
import com.slb.taxi.webservice.xtm.stubs.xtm._topic;

import de.ingrid.iplug.sns.utils.DetailedTopic;
import de.ingrid.iplug.sns.utils.Topic;
import de.ingrid.utils.IngridHit;

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

    private static final String TEMPORAL_TO_OCCURRENCE = "temporalToOcc";

    private static final String TEMPORAL_FROM_OCCURRENCE = "temporalFromOcc";

    private static final String TEMPORAL_AT_OCCURRENCE = "temporalAtOcc";

    private static final String SYNONYM_TYPE = "synonymType";

    private static final String THESAURUS_DESCRIPTOR = "/thesa/descriptor";

    private SNSClient fServiceClient;

    private static final String[] fTypeFilters = new String[] { "narrowerTermAssoc", "synonymAssoc",
            "relatedTermsAssoc" };

    private static final String[] fAdministrativeTypes = new String[] { "communityType", "districtType", "quarterType",
            "stateType", "nationType" };

    /**
     * Constructor for SNS controller.
     * 
     * @param client
     */
    public SNSController(SNSClient client) {
        this.fServiceClient = client;
    }

    /**
     * For a given term (that should be a topic itself) an array of associated topics will returned. 
     * 
     * @param queryTerm
     * 			The query term.
     * @param start
     * 			The start offset.
     * @param maxResults
     * 			Limit number of results.
     * @param plugId
     * 			The plugId as String.
     * @param totalSize
     * 			The quantity of the found topics altogether.
     * @param lang
     * 			Is used to specify the preferred language for requests.
     * @return an array of assiciated topics or null in case the term itself is not found as topic
     * @throws Exception
     */
    public synchronized Topic[] getTopicsForTerm(String queryTerm, int start, int maxResults, String plugId,
            int[] totalSize, String lang) throws Exception {
        HashMap associationTypes = new HashMap();
        Topic[] result = new Topic[0];

        _topic topic = getTopic(queryTerm, THESAURUS_DESCRIPTOR, start, totalSize, lang);
        if (topic != null) {
            _topic[] associatedTopics = getAssociatedTopics(topic, fTypeFilters, associationTypes, totalSize);
            Topic[] topics = copyToTopicArray(associatedTopics, associationTypes, maxResults, plugId, lang);
            result = topics;
        }

        return result;
    }

    /**
     * For a given topic (identified by id) an array of associated topics will returned.
     * 
     * @param topicId
     * 			The topic given by Id.
     * @param maxResults
     * 			Limit number of results.
     * @param plugId
     * 			The plugId as String
     * @param totalSize
     * 			The quantity of the found topics altogether.
     * @return an array of associated topics for a type identified by id
     * @throws Exception
     */
    public synchronized Topic[] getTopicsForTopic(String topicId, int maxResults, String plugId, int[] totalSize)
            throws Exception {
        HashMap associationTypes = new HashMap();
        _topic topic = new _topic();
        topic.setId(topicId);
        _topic[] associatedTopics = getAssociatedTopics(topic, fTypeFilters, associationTypes, totalSize);
        if (associatedTopics != null) {
            return copyToTopicArray(associatedTopics, associationTypes, maxResults, plugId, "bla");
        }

        return null;
    }

    /**
     * For a given text an array of detailed topics will returned.
     * 
     * @param documentText
     * 			The given text to analyze. 
     * @param maxToAnalyzeWords
     * 			Analyze only the first maxToAnalyzeWords words of the document in the body.
     * @param filter
     * 			Topic type as search criterion (only root paths may be used). 
     * @param plugId
     * 			The plugId as String.
     * @param lang
     * 			Is used to specify the preferred language for requests.
     * @param totalSize
     * 			The quantity of the found topics altogether.
     * @return array of detailed topics for the given text
     * @throws Exception
     */
    public DetailedTopic[] getTopicsForText(String documentText, int maxToAnalyzeWords, String filter,
            String plugId, String lang, int[] totalSize) throws Exception {
        final _topicMapFragment mapFragment = this.fServiceClient.autoClassify(documentText, maxToAnalyzeWords, filter,
                true, lang);
        final _topic[] topics = mapFragment.getTopicMap().getTopic();
        if (null != mapFragment.getListExcerpt()) {
            totalSize[0] = mapFragment.getListExcerpt().getTotalSize().intValue();
        }

        if (topics != null) {
            return toDetailedTopicArray(topics, plugId, lang);
        }

        return new DetailedTopic[0];
    }

    /**
     * For a given topic array an array of associated topics which are not synonymous one will returned.
     * 
     * @param topics
     * 			Array of given topics.
     * @param plugId
     * 			The plugId as String.
     * @param lang
     * 			Is used to specify the preferred language for requests.
     * @return an array of detailed topics, we ignoring all topics of typ synonymType
     */
    private DetailedTopic[] toDetailedTopicArray(_topic[] topics, String plugId, String lang) {
        final List returnList = new ArrayList();
        for (int i = 0; i < topics.length; i++) {
            // System.out.println(topics[i].getInstanceOf()[0].getTopicRef().getHref());
            if (!topics[i].getInstanceOf()[0].getTopicRef().getHref().endsWith(SYNONYM_TYPE)) {
                returnList.add(buildDetailedTopicFrom_topic(topics[i], plugId, lang));
            }
        }

        return (DetailedTopic[]) returnList.toArray(new DetailedTopic[returnList.size()]);
    }

    /**
     * Build a detailed metadata index for a given topic.
     * 
     * @param topic
     * 			A given topic.
     * @param plugId
     * 			The plugId as String
     * @param lang
     * 			Is used to specify the preferred language for requests.
     * @return A detailed topic from _topic.
     */
    private synchronized DetailedTopic buildDetailedTopicFrom_topic(_topic topic, String plugId, String lang) {
        String topicId = topic.getId();
        _baseName[] bn = topic.getBaseName();
        String title = "";
        for (int i = 0; i < bn.length; i++) {
            final String href = bn[i].getScope().getTopicRef()[0].getHref();
            if (href.endsWith('#' + lang)) {
                title = topic.getBaseName()[i].getBaseNameString().getValue();
                break;
            }
        }

        String summary = title + ' ' + topic.getInstanceOf()[0].getTopicRef().getHref();
        DetailedTopic metaData = new DetailedTopic(plugId, topicId.hashCode(), topicId, title, summary);
        _instanceOf[] instanceOfs = topic.getInstanceOf();
        for (int i = 0; i < instanceOfs.length; i++) {
            String href = instanceOfs[i].getTopicRef().getHref();
            metaData.addToList(DetailedTopic.INSTANCE_OF, href);
        }
        pushTimes(metaData, topic);
        pushDefinitions(metaData, topic, lang);
        pushSamples(metaData, topic, lang);
        pushOccurensie(DetailedTopic.DESCRIPTION_OCC, topic, metaData, lang);
        pushOccurensie(DetailedTopic.ASSOCIATED_OCC, topic, metaData, lang);

        if (containsTypes(fAdministrativeTypes, topic.getInstanceOf()[0].getTopicRef().getHref())) {
            metaData.setAdministrativeID(topic.getId());
        }

        return metaData;
    }

    private void pushDefinitions(DetailedTopic metaData, _topic topic, String lang) {
        _occurrence[] occurrences = topic.getOccurrence();
        ArrayList titles = new ArrayList();
        ArrayList definitions = new ArrayList();

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
                    if (type.endsWith(DetailedTopic.DESCRIPTION_OCC) && occurrences[i].getResourceRef() != null
                            && scope.endsWith('#' + lang)) {
                        definitions.add(occurrences[i].getResourceRef().getHref().toString());
                        titles.add(occurrences[i].getResourceRef().getTitle());
                    }
                }
            }
        }

        metaData.setDefinitions((String[]) definitions.toArray(new String[definitions.size()]));
        metaData.setDefinitionTitles((String[]) titles.toArray(new String[titles.size()]));
    }

    private void pushSamples(DetailedTopic metaData, _topic topic, String lang) {
        _occurrence[] occurrences = topic.getOccurrence();
        ArrayList titles = new ArrayList();
        ArrayList samples = new ArrayList();

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
                    if (type.endsWith(DetailedTopic.SAMPLE_OCC) && occurrences[i].getResourceRef() != null
                            && scope.endsWith('#' + lang)) {
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
     * 			The detailed topic for which the time data should be set.
     * @param topic
     * 			A given topic.
     */
    private void pushTimes(DetailedTopic metaData, _topic topic) {
        _occurrence[] occurrences = topic.getOccurrence();
        String type = null;
        if (occurrences != null) {
            for (int i = 0; i < occurrences.length; i++) {
                if (occurrences[i].getInstanceOf() != null) {
                    type = occurrences[i].getInstanceOf().getTopicRef().getHref();
                    if (type.endsWith(TEMPORAL_AT_OCCURRENCE)) {
                        final String at = occurrences[i].getResourceData().getValue();
                        metaData.setFrom(at);
                        metaData.setTo(at);
                        break;
                    }
                    if (type.endsWith(TEMPORAL_FROM_OCCURRENCE)) {
                        metaData.setFrom(occurrences[i].getResourceData().getValue());
                    }
                    if (type.endsWith(TEMPORAL_TO_OCCURRENCE)) {
                        metaData.setTo(occurrences[i].getResourceData().getValue());
                    }
                }
            }
        }
    }

    private synchronized void pushOccurensie(String occType, _topic topic, DetailedTopic detailedTopic, String lang) {
        _occurrence[] occurrences = topic.getOccurrence();
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
                    if (type.endsWith(occType) && occurrences[i].getResourceData() != null
                            && scope.endsWith('#' + lang)) {
                        detailedTopic.put(occType, occurrences[i].getResourceData().getValue());
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
     * @return a ingrid topic from a _topic
     */
    private synchronized Topic buildTopicFrom_topic(_topic topic, String plugId, String associationType, String lang) {
        _baseName[] baseNames = topic.getBaseName();
        // Set a default if for the selected language nothing exists.
        String title = baseNames[0].getBaseNameString().getValue();

        for (int i = 0; i < baseNames.length; i++) {
            final String href = baseNames[i].getScope().getTopicRef()[0].getHref();
            if (href.endsWith('#' + lang)) {
                title = baseNames[i].getBaseNameString().getValue();
                break;
            }
        }

        String summary = title + ' ' + topic.getInstanceOf()[0].getTopicRef().getHref();
        String topicId = topic.getId();
        return new Topic(plugId, topicId.hashCode(), topicId, title, summary, associationType);
    }

    /**
     * @param baseTopic
     * @param typePattern
     * @param associationTypes
     * @param totalSize
     * @return _topic array of associated topics filter by the given patterns
     * @throws Exception
     */
    private _topic[] getAssociatedTopics(_topic baseTopic, String[] typePattern, HashMap associationTypes,
            int[] totalSize) throws Exception {
        ArrayList resultList = new ArrayList();

        final _topicMapFragment mapFragment = this.fServiceClient.getPSI(baseTopic.getId(), 1, null);
        final _topic[] topics = mapFragment.getTopicMap().getTopic();
        if (null != mapFragment.getListExcerpt()) {
            if (null != mapFragment.getListExcerpt().getTotalSize()) {
                totalSize[0] = mapFragment.getListExcerpt().getTotalSize().intValue();
            }
        }
        final _association[] associations = mapFragment.getTopicMap().getAssociation();
        // iterate through associations to find the correct association types
        if (associations != null) {
            for (int i = 0; i < associations.length; i++) {
                final _association association = associations[i];
                // association type
                final String assocType = association.getInstanceOf().getTopicRef().getHref();
                if (containsTypes(typePattern, assocType)) {
                    // association mebers are the basetopic and it association
                    final _member[] members = association.getMember();
                    for (int j = 0; j < members.length; j++) {
                        final _member member = members[j];
                        // here is only the topic id available
                        final String topicId = member.getTopicRef()[0].getHref();
                        final String assocMember = member.getRoleSpec().getTopicRef().getHref();
                        if (!topicId.equals(baseTopic.getId())) {
                            final _topic topicById = getTopicById(topics, topicId);
                            if (topicById != null) {
                                if (null != associationTypes) {
                                    associationTypes.put(topicById.getId(), assocMember);
                                }
                                resultList.add(topicById);
                            }
                        }
                    }
                }
            }

            return (_topic[]) resultList.toArray(new _topic[resultList.size()]);
        }

        return null;
    }

    /**
     * @param topics
     * @param topicId
     * @return the topic that match the topicId from the given _topic array
     */
    private _topic getTopicById(_topic[] topics, String topicId) {
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
     * @return just one matching topic, in case more topics match or no topic match we return null
     * @throws Exception
     */
    private _topic getTopic(String queryTerm, String topicType, long offSet, int[] totalSize, String lang)
            throws Exception {
        _topicMapFragment mapFragment = this.fServiceClient.findTopics(queryTerm, topicType, SearchType.exact,
                FieldsType.captors, offSet, lang);
        if (null != mapFragment) {
            totalSize[0] = mapFragment.getListExcerpt().getTotalSize().intValue();
            _topic[] topics = mapFragment.getTopicMap().getTopic();
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
    private Topic[] copyToTopicArray(_topic[] topics, HashMap associationTypes, int maxResults, String plugId,
            String lang) throws Exception {
        ArrayList ingridTopics = new ArrayList();

        if (null != topics) {
            int count = Math.min(maxResults, topics.length);
            for (int i = 0; i < count; i++) {
                String topicId = topics[i].getId();
                if (!topicId.equals("_Interface0")) {
                    String associationType = "";
                    if ((null != associationTypes) && (associationTypes.containsKey(topicId))) {
                        associationType = (String) associationTypes.get(topicId);
                    }
                    ingridTopics.add(buildTopicFrom_topic(topics[i], plugId, associationType, lang));
                }
            }
        }

        return (Topic[]) ingridTopics.toArray(new Topic[ingridTopics.size()]);
    }

    /**
     * Search for a given date events of an requested event type.
     * 
     * @param searchTerm
     * 			The search term.
     * @param eventTypes
     * 			Array with one or more types of events.
     * @param atDate
     * 			A date at which an event occured.
     * @param start
     * 			Defines the number of elements to skip.
     * @param length
     * 			Number of elements that should be retrieved.
     * @param plugId
     * 			The plugId as String.
     * @param totalSize
     *          Has the total size of the query set after the call.
     * @param lang
     * 			Is used to specify the preferred language for requests.
     * @return A topic array of events.
     * @throws Exception
     */
    public Topic[] getEventFromTopic(String searchTerm, String[] eventTypes, String atDate, int start, int length,
            String plugId, int[] totalSize, String lang) throws Exception {
        Topic[] result = new Topic[0];
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
        _topicMapFragment topicMapFragment = this.fServiceClient.findEvents(searchTerm, true, searchType, eventPath,
                FieldsType.captors, start, atDate, lang, length);
        _topic[] topic = topicMapFragment.getTopicMap().getTopic();
        totalSize[0] = topicMapFragment.getListExcerpt().getTotalSize().intValue();
        if (topic != null) {
            Topic[] topics = copyToTopicArray(topic, null, length, plugId, lang);
            result = topics;
        }

        return result;
    }

    /**
     * Returns all similar terms to a term.
     * 
     * @param searchTerm
     * 			The given search term.
     * @param length
     * 			Number of elements that should be retrieved.
     * @param plugId
     * 			The plugId as String.
     * @param totalSize
     * 			The total size of the query set after the call.
     * @param lang
     * 			Is used to specify the preferred language for requests.
     * @return Topics to similar terms.
     * @throws Exception
     */
    public Topic[] getSimilarTermsFromTopic(String searchTerm, int length, String plugId, int[] totalSize, String lang)
            throws Exception {
        return getSimilarTermsFromTopic(new String[] { searchTerm }, length, plugId, totalSize, lang);
    }

    /**
     * Returns all similar terms to an array of terms.
     * 
     * @param searchTerm
     * 			The given search term.
     * @param length
     * 			Number of elements that should be retrieved.
     * @param plugId
     * 			The plugId as String.
     * @param totalSize
     * 			The total size of the query set after the call.
     * @param lang
     * 			Is used to specify the preferred language for requests.
     * @return Topics to similar terms.
     * @throws Exception
     */
    public Topic[] getSimilarTermsFromTopic(String[] searchTerm, int length, String plugId, int[] totalSize, String lang)
            throws Exception {
        Topic[] result = new Topic[0];

        _topicMapFragment topicMapFragment = this.fServiceClient.getSimilarTerms(true, searchTerm, lang);
        _topic[] topic = topicMapFragment.getTopicMap().getTopic();
        if (null != topicMapFragment.getListExcerpt()) {
            totalSize[0] = topicMapFragment.getListExcerpt().getTotalSize().intValue();
        }

        if (topic != null) {
            Topic[] topics = copyToTopicArray(topic, null, length, plugId, lang);
            result = topics;
        }

        return result;
    }

    /**
     * Returns all anniversaries to a date.
     * 
     * @param searchTerm
     * 			The given search term.
     * @param length
     * 			Number of elements that should be retrieved.
     * @param plugId
     * 			The plugId as String.
     * @param totalSize
     * 			The total size of the query set after the call.
     * @return Topics to an anniversary.
     * @throws Exception
     */
    public Topic[] getAnniversaryFromTopic(String searchTerm, int length, String plugId, int[] totalSize)
            throws Exception {
        Topic[] result = new Topic[0];

        _topicMapFragment topicMapFragment = this.fServiceClient.anniversary(searchTerm);
        _topic[] topic = topicMapFragment.getTopicMap().getTopic();
        totalSize[0] = topicMapFragment.getListExcerpt().getTotalSize().intValue();
        if (topic != null) {
            Topic[] topics = copyToTopicArray(topic, null, length, plugId, "bla");
            result = topics;
        }

        return result;
    }

    /**
     * Returns all events between two dates.
     * 
     * @param searchTerm
     * 			The given search term.
     * @param eventTypes
     * 			Array with one or more types of events.
     * @param fromDate
     * 			A date after that an event occured.
     * @param toDate
     * 			A date before an event occured.
     * @param start
     * 			Defines the number of elements to skip.
     * @param length
     * 			Number of elements that should be retrieved.
     * @param plugId
     * 			The plugId as String.
     * @param totalSize
     *          Has the total size of the query set after the call.
     * @param lang
     * 			Is used to specify the preferred language for requests.
     * @return Topics to an event.
     * @throws Exception
     */
    public Topic[] getEventFromTopic(String searchTerm, String[] eventTypes, String fromDate, String toDate, int start,
            int length, String plugId, int[] totalSize, String lang) throws Exception {
        Topic[] result = new Topic[0];
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
        _topicMapFragment topicMapFragment = this.fServiceClient.findEvents(searchTerm, true, searchType, eventPath,
                FieldsType.captors, start, fromDate, toDate, lang, length);
        _topic[] topic = topicMapFragment.getTopicMap().getTopic();
        totalSize[0] = topicMapFragment.getListExcerpt().getTotalSize().intValue();
        if (topic != null) {
            Topic[] topics = copyToTopicArray(topic, null, length, plugId, lang);
            result = topics;
        }

        return result;
    }

    /**
     * Deliver for a given hit the detailed topic description.
     * 
     * @param hit
     * 			The hit, for which further information should received.
     * @param lang
     * 			Is used to specify the preferred language for requests.
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
     * 			The hit, for which further information should received. 
     * @param filter
     * 			Topic type as search criterion (only root paths may be used). 
     * @param lang
     * 			Is used to specify the preferred language for requests.
     * @return A detailed topic to a filter.
     * @throws Exception
     */
    public DetailedTopic getTopicDetail(IngridHit hit, String filter, String lang) throws Exception {
        Topic topic = (Topic) hit;
        String topicID = topic.getTopicID();
        DetailedTopic result = null;

        _topicMapFragment mapFragment = this.fServiceClient.getPSI(topicID, 0, filter);
        if (null != mapFragment) {
            _topic[] topics = mapFragment.getTopicMap().getTopic();

            for (int i = 0; i < topics.length; i++) {
                if (topics[i].getId().equals(topicID)) {
                    result = buildDetailedTopicFrom_topic(topics[0], hit.getPlugId(), lang);
                }
            }
        }

        return result;
    }

    /**
     * Find for a given topic similar locations. 
     * 
     * @param topicId
     * 			The topic given by Id.
     * @param length
     * 			Number of elements that should be retrieved.
     * @param plugId
     * 			The plugId as string.
     * @param totalSize
     * 			Has the total size of the query set after the call.
     * @return A topic array from similar location topics.
     * @throws Exception
     */
    public Topic[] getTopicSimilarLocationsFromTopic(String topicId, int length, String plugId, int[] totalSize)
            throws Exception {
        Topic[] result = null;

        _topicMapFragment mapFragment = this.fServiceClient.getPSI(topicId, 0, "/location");
        if (null != mapFragment) {
            if (null != mapFragment.getListExcerpt().getTotalSize()) {
                totalSize[0] = mapFragment.getListExcerpt().getTotalSize().intValue();
            }
            _topic[] topics = mapFragment.getTopicMap().getTopic();
            result = copyToTopicArray(topics, null, length, plugId, "bla");
        }

        return result;
    }

    /**
     * For a given text an array of detailed topics will returned (synchronized version).
     * 
     * @param searchTerm
     * 			The given text to analyze.
     * @param maxToAnalyzeWords
     * 			Analyze only the first maxToAnalyzeWords words of the document in the body.
     * @param plugId
     * 			The plugId as String.
     * @param lang
     * 			Is used to specify the preferred language for requests.
     * @param totalSize
     * 			The quantity of the found topics altogether.
     * @return Array of detailed topics for the given text.
     * @throws Exception
     */
    public synchronized DetailedTopic[] getTopicsForText(String searchTerm, int maxToAnalyzeWords, String plugId, String lang,
            int[] totalSize) throws Exception {
        return getTopicsForText(searchTerm, maxToAnalyzeWords, null, plugId, lang, totalSize);
    }
}
