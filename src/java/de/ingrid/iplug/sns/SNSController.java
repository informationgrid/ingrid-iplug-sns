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

    private SNSClient fServiceClient = null;

    private String fLanguage;

    private static final String[] fTypeFilters = new String[] { "narrowerTermAssoc", "widerTermAssoc", "synonymAssoc",
            "relatedTermsAssoc" };

    private static final String[] fAdministrativeTypes = new String[] { "communityType", "districtType", "quarterType",
            "stateType", "nationType" };

    /**
     * @param client
     */
    public SNSController(SNSClient client) {
        this.fServiceClient = client;
        this.fLanguage = client.getLanguage();
    }

    /**
     * @param queryTerm
     * @param start
     * @param maxResults
     * @param plugId
     * @return an array of assiciated topics or null in case the term itself is not found as topic
     * @throws Exception
     */
    public synchronized Topic[] getTopicsForTerm(String queryTerm, int start, int maxResults, String plugId)
            throws Exception {
        HashMap associationTypes = new HashMap();
        Topic[] result = new Topic[0];

        //FIXME: Hier wirklich /thesa/descriptor und nicht nur /thesa?
        _topic topic = getTopic(queryTerm, THESAURUS_DESCRIPTOR, start);
        if (topic != null) {
            _topic[] associatedTopics = getAssociatedTopics(topic, fTypeFilters, associationTypes);
            Topic[] topics = copyToTopicArray(associatedTopics, associationTypes, maxResults, plugId);
            result = topics;
        }

        return result;
    }

    /**
     * @param topicId
     * @param maxResults
     * @param plugId
     * @return an array of associated topics for a type identified by id
     * @throws Exception
     */
    public synchronized Topic[] getTopicsForTopic(String topicId, int maxResults, String plugId) throws Exception {
        HashMap associationTypes = new HashMap();
        _topic topic = new _topic();
        topic.setId(topicId);
        _topic[] associatedTopics = getAssociatedTopics(topic, fTypeFilters, associationTypes);
        if (associatedTopics != null) {
            return copyToTopicArray(associatedTopics, associationTypes, maxResults, plugId);
        }

        return null;
    }

    /**
     * @param documentText
     * @param maxToAnalyzeWords
     * @param filter
     * @param plugId
     * @return array of detailed topics for the given text
     * @throws Exception
     */
    public synchronized DetailedTopic[] getTopicsForText(String documentText, int maxToAnalyzeWords, String filter,
            String plugId) throws Exception {
        final _topicMapFragment mapFragment = this.fServiceClient.autoClassify(documentText, maxToAnalyzeWords, filter,
                true);
        final _topic[] topics = mapFragment.getTopicMap().getTopic();
        if (topics != null) {
            return toDetailedTopicArray(topics, plugId);
        }

        return new DetailedTopic[0];
    }

    /**
     * @param topics
     * @param plugId
     * @return an array of detailed topics, we ignoring all topics of typ synonymType
     */
    private synchronized DetailedTopic[] toDetailedTopicArray(_topic[] topics, String plugId) {
        final List returnList = new ArrayList();
        for (int i = 0; i < topics.length; i++) {
            // System.out.println(topics[i].getInstanceOf()[0].getTopicRef().getHref());
            if (!topics[i].getInstanceOf()[0].getTopicRef().getHref().endsWith(SYNONYM_TYPE)) {
                returnList.add(buildDetailedTopicFrom_topic(topics[i], plugId));
            }
        }

        return (DetailedTopic[]) returnList.toArray(new DetailedTopic[returnList.size()]);
    }

    /**
     * @param topic
     * @param plugId
     * @return A detailed topic from _topic.
     */
    private synchronized DetailedTopic buildDetailedTopicFrom_topic(_topic topic, String plugId) {
        String topicId = topic.getId();
        _baseName[] bn = topic.getBaseName();
        String title = "";
        for (int i = 0; i < bn.length; i++) {
            final String href = bn[i].getScope().getTopicRef()[0].getHref();
            if (href.endsWith("#" + this.fLanguage)) {
                title = topic.getBaseName()[i].getBaseNameString().getValue();
                break;
            }
        }

        String summary = title + " " + topic.getInstanceOf()[0].getTopicRef().getHref();
        DetailedTopic metaData = new DetailedTopic(plugId, topicId.hashCode(), topicId, title, summary);
        _instanceOf[] instanceOfs = topic.getInstanceOf();
        for (int i = 0; i < instanceOfs.length; i++) {
            String href = instanceOfs[i].getTopicRef().getHref();
            metaData.addToList(DetailedTopic.INSTANCE_OF, href);
        }
        pushTimes(metaData, topic);
        pushOccurensie(DetailedTopic.DESCRIPTION_OCC, topic, metaData);
        pushOccurensie(DetailedTopic.SAMPLE_OCC, topic, metaData);
        pushOccurensie(DetailedTopic.ASSICIATED_OCC, topic, metaData);

        if (containsTypes(fAdministrativeTypes, topic.getInstanceOf()[0].getTopicRef().getHref())) {
            metaData.setAdministrativeID(topic.getId());
        }

        return metaData;
    }

    /**
     * pushs the time data in to the detailed topic
     * 
     * @param metaData
     * @param topic
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

    private synchronized void pushOccurensie(String occType, _topic topic, DetailedTopic detailedTopic) {
        _occurrence[] occurrences = topic.getOccurrence();
        String type = null;
        if (occurrences != null) {
            for (int i = 0; i < occurrences.length; i++) {
                if (occurrences[i].getInstanceOf() != null) {
                    // Only compare the scope to the language if the element has one set.
                    String scope = "#" + this.fLanguage;
                    if (occurrences[i].getScope() != null) {
                        scope = occurrences[i].getScope().getTopicRef(0).getHref();
                    }
                    type = occurrences[i].getInstanceOf().getTopicRef().getHref();
                    if (type.endsWith(occType) && occurrences[i].getResourceData() != null
                            && scope.endsWith("#" + this.fLanguage)) {
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
     * @return a ingrid topic from a _topic
     */
    private synchronized Topic buildTopicFrom_topic(_topic topic, String plugId, String associationType) {
        String title = topic.getBaseName()[0].getBaseNameString().getValue();
        String summary = title + " " + topic.getInstanceOf()[0].getTopicRef().getHref();
        String topicId = topic.getId();
        return new Topic(plugId, topicId.hashCode(), topicId, title, summary, associationType);
    }

    /**
     * @param baseTopic
     * @param typePattern
     * @param associationTypes 
     * @return _topic array of associated topics filter by the given patterns
     * @throws Exception
     */
    private synchronized _topic[] getAssociatedTopics(_topic baseTopic, String[] typePattern, HashMap associationTypes)
            throws Exception {
        ArrayList resultList = new ArrayList();

        _topicMapFragment mapFragment = this.fServiceClient.getPSI(baseTopic.getId(), 1, null);
        _topic[] topics = mapFragment.getTopicMap().getTopic();
        _association[] associations = mapFragment.getTopicMap().getAssociation();
        // iterate through associations to find the correct association types
        if (associations != null) {
            for (int i = 0; i < associations.length; i++) {
                _association association = associations[i];
                // association type
                String assocType = association.getInstanceOf().getTopicRef().getHref();
                if (containsTypes(typePattern, assocType)) {
                    // association mebers are the basetopic and it association
                    _member[] members = association.getMember();
                    for (int j = 0; j < members.length; j++) {
                        _member member = members[j];
                        // here is only the topic id available
                        String topicId = member.getTopicRef()[0].getHref();
                        if (!topicId.equals(baseTopic.getId())) {
                            _topic topicById = getTopicById(topics, topicId);
                            if (topicById != null) {
                                if (null != associationTypes) {
                                    associationTypes.put(topicById.getId(), assocType);
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
     * @return just one matching topic, in case more topics match or no topic match we return null
     * @throws Exception
     */
    private _topic getTopic(String queryTerm, String topicType, long offSet) throws Exception {
        _topicMapFragment mapFragment = this.fServiceClient.findTopics(queryTerm, topicType, SearchType.exact,
                FieldsType.captors, offSet);
        if (null != mapFragment) {
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
     * @return An array of Topic with the given length.
     * @throws Exception
     */
    private Topic[] copyToTopicArray(_topic[] topics, HashMap associationTypes, int maxResults, String plugId)
            throws Exception {
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
                    ingridTopics.add(buildTopicFrom_topic(topics[i], plugId, associationType));
                }
            }
        }

        return (Topic[]) ingridTopics.toArray(new Topic[ingridTopics.size()]);
    }

    /**
     * @param searchTerm
     * @param eventTypes
     * @param atDate
     * @param start
     * @param length
     * @param plugId
     * @return A topic array of events.
     * @throws Exception
     */
    public Topic[] getEventFromTopic(String searchTerm, String[] eventTypes, String atDate, int start, int length,
            String plugId) throws Exception {
        Topic[] result = new Topic[0];
        String[] eventPath = null;

        if (null != eventTypes) {
            eventPath = new String[eventTypes.length];
            for (int i = 0; i < eventPath.length; i++) {
                eventPath[i] = "/event/" + eventTypes[i] + "/";
            }
        } else {
            eventPath = new String[] { "/event/" };
        }

        SearchType searchType = SearchType.exact;
        if ((null == searchTerm) || (searchTerm.trim().equals(""))) {
            searchType = SearchType.contains;
        }
        _topicMapFragment topicMapFragment = this.fServiceClient.findEvents(searchTerm, true, searchType, eventPath,
                FieldsType.captors, start, atDate);
        _topic[] topic = topicMapFragment.getTopicMap().getTopic();
        if (topic != null) {
            Topic[] topics = copyToTopicArray(topic, null, length, plugId);
            result = topics;
        }

        return result;
    }

    /**
     * Returns all similar terms to a term.
     * 
     * @param searchTerm
     * @param length
     * @param plugId
     * @return Topics to similar terms.
     * @throws Exception
     */
    public Topic[] getSimilarTermsFromTopic(String searchTerm, int length, String plugId) throws Exception {
        return getSimilarTermsFromTopic(new String[] { searchTerm }, length, plugId);
    }

    /**
     * Returns all similar terms to an array of terms.
     * 
     * @param searchTerm
     * @param length
     * @param plugId
     * @return Topics to similar terms.
     * @throws Exception
     */
    public Topic[] getSimilarTermsFromTopic(String[] searchTerm, int length, String plugId) throws Exception {
        Topic[] result = new Topic[0];

        _topicMapFragment topicMapFragment = this.fServiceClient.getSimilarTerms(true, searchTerm);
        _topic[] topic = topicMapFragment.getTopicMap().getTopic();

        if (topic != null) {
            Topic[] topics = copyToTopicArray(topic, null, length, plugId);
            result = topics;
        }

        return result;
    }

    /**
     * Returns all anniversaries to a date.
     * 
     * @param searchTerm
     * @param length
     * @param plugId
     * @return Topics to an anniversary.
     * @throws Exception
     */
    public Topic[] getAnniversaryFromTopic(String searchTerm, int length, String plugId) throws Exception {
        Topic[] result = new Topic[0];

        _topicMapFragment topicMapFragment = this.fServiceClient.anniversary(searchTerm);
        _topic[] topic = topicMapFragment.getTopicMap().getTopic();
        if (topic != null) {
            Topic[] topics = copyToTopicArray(topic, null, length, plugId);
            result = topics;
        }

        return result;
    }

    /**
     * Returns all events between two dates.
     * 
     * @param searchTerm
     * @param eventTypes
     * @param fromDate
     * @param toDate
     * @param start
     * @param length
     * @param plugId
     * @return Topics to an event.
     * @throws Exception
     */
    public Topic[] getEventFromTopic(String searchTerm, String[] eventTypes, String fromDate, String toDate, int start,
            int length, String plugId) throws Exception {
        Topic[] result = new Topic[0];
        String[] eventPath = null;

        if (null != eventTypes) {
            eventPath = new String[eventTypes.length];
            for (int i = 0; i < eventPath.length; i++) {
                eventPath[i] = "/event/" + eventTypes[i] + "/";
            }
        } else {
            eventPath = new String[] { "/event/" };
        }

        SearchType searchType = SearchType.exact;
        if ((null == searchTerm) || (searchTerm.trim().equals(""))) {
            searchType = SearchType.contains;
        }
        _topicMapFragment topicMapFragment = this.fServiceClient.findEvents(searchTerm, true, searchType, eventPath,
                FieldsType.captors, start, fromDate, toDate);
        _topic[] topic = topicMapFragment.getTopicMap().getTopic();
        if (topic != null) {
            Topic[] topics = copyToTopicArray(topic, null, length, plugId);
            result = topics;
        }

        return result;
    }

    /**
     * @param hit
     * @return A detailed topic.
     * @throws Exception
     */
    public DetailedTopic getTopicDetail(IngridHit hit) throws Exception {
        return getTopicDetail(hit, null);
    }

    /**
     * @param hit
     * @param filter
     * @return A detailed topic to a filter.
     * @throws Exception
     */
    public DetailedTopic getTopicDetail(IngridHit hit, String filter) throws Exception {
        Topic topic = (Topic) hit;
        String topicID = topic.getTopicID();
        DetailedTopic result = null;

        _topicMapFragment mapFragment = this.fServiceClient.getPSI(topicID, 0, filter);
        if (null != mapFragment) {
            _topic[] topics = mapFragment.getTopicMap().getTopic();

            for (int i = 0; i < topics.length; i++) {
                if (topics[i].getId().equals(topicID)) {
                    result = buildDetailedTopicFrom_topic(topics[0], hit.getPlugId());
                }
            }
        }

        return result;
    }

    /**
     * @param topicId
     * @param length
     * @param plugId
     * @return A topic array from similar location topics.
     * @throws Exception
     */
    public Topic[] getTopicSimilarLocationsFromTopic(String topicId, int length, String plugId) throws Exception {
        Topic[] result = null;

        _topicMapFragment mapFragment = this.fServiceClient.getPSI(topicId, 0, "/location");
        if (null != mapFragment) {
            _topic[] topics = mapFragment.getTopicMap().getTopic();
            result = copyToTopicArray(topics, null, length, plugId);
        }

        return result;
    }

    /**
     * @param searchTerm
     * @param i
     * @param plugId
     * @return Array of detailed topics for the given text.
     * @throws Exception
     */
    public synchronized DetailedTopic[] getTopicsForText(String searchTerm, int i, String plugId) throws Exception {
        return getTopicsForText(searchTerm, i, null, plugId);
    }
}
