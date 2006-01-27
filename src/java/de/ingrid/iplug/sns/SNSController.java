package de.ingrid.iplug.sns;

import java.util.ArrayList;
import java.util.List;

import com.slb.taxi.webservice.xtm.stubs.SearchType;
import com.slb.taxi.webservice.xtm.stubs._topicMapFragment;
import com.slb.taxi.webservice.xtm.stubs.xtm._association;
import com.slb.taxi.webservice.xtm.stubs.xtm._member;
import com.slb.taxi.webservice.xtm.stubs.xtm._occurrence;
import com.slb.taxi.webservice.xtm.stubs.xtm._topic;

import de.ingrid.iplug.sns.utils.Topic;
import de.ingrid.iplug.sns.utils.DetailedTopic;

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

    private static final String[] fTypeFilters = new String[] { "narrowerTermAssoc", "widerTermAssoc", "synonymAssoc" };

    private static final String[] fAdministrativeTypes = new String[] { "communityType", "districtType", "quarterType",
            "stateType", "nationType" };

    /**
     * @param client
     */
    public SNSController(SNSClient client) {
        this.fServiceClient = client;
    }

    /**
     * @param queryTerm
     * @param start
     * @param maxResults
     * @return an array of assiciated topics or null in case the term itself is not found as topic
     * @throws Exception
     */
    public synchronized Topic[] getTopicsForTerm(String queryTerm, int start, int maxResults) throws Exception {
        _topic topic = getTopic(queryTerm, THESAURUS_DESCRIPTOR, start);
        if (topic != null) {
            _topic[] associatedTopics = getAssociatedTopics(topic, fTypeFilters);
            Topic[] topics = copyToTopicArray(associatedTopics, maxResults);
            return topics;
        }
        return null;
    }

    /**
     * @param topicId
     * @param maxResults
     * @return an array of associated topics for a type identified by id
     * @throws Exception
     */
    public synchronized Topic[] getTopicsForTopic(String topicId, int maxResults) throws Exception {
        _topic topic = new _topic();
        topic.setId(topicId);
        _topic[] associatedTopics = getAssociatedTopics(topic, fTypeFilters);
        if (associatedTopics != null) {
            return copyToTopicArray(associatedTopics, maxResults);
        }
        return null;
    }

    /**
     * @param documentText
     * @param maxToAnalyzeWords
     * @return array of detailed topics for the given text
     * @throws Exception
     */
    public synchronized DetailedTopic[] getTopicsForText(String documentText, int maxToAnalyzeWords) throws Exception {
        final _topicMapFragment mapFragment = this.fServiceClient.autoClassify(documentText, maxToAnalyzeWords);
        final _topic[] topics = mapFragment.getTopicMap().getTopic();
        if (topics != null) {
            return toDetailedTopicArray(topics);
        }
        return new DetailedTopic[0];
    }

    /**
     * @param topics
     * @return an array of detailed topics, we ignoring all topics of typ synonymType
     */
    private synchronized DetailedTopic[] toDetailedTopicArray(_topic[] topics) {
        final List returnList = new ArrayList();
        for (int i = 0; i < topics.length; i++) {
            System.out.println(topics[i].getInstanceOf()[0].getTopicRef().getHref());
            if (!topics[i].getInstanceOf()[0].getTopicRef().getHref().endsWith(SYNONYM_TYPE)) {
                returnList.add(buildDetailedTopicFrom_topic(topics[i]));
            }
        }
        return (DetailedTopic[]) returnList.toArray(new DetailedTopic[returnList.size()]);
    }

    /**
     * @param topic
     * @return a detailed topic from _topic
     */
    private synchronized DetailedTopic buildDetailedTopicFrom_topic(_topic topic) {
        DetailedTopic metaData = new DetailedTopic(topic.getId(), topic.getBaseName()[0].getBaseNameString().getValue());
        pushTimes(metaData, topic);
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
    private synchronized void pushTimes(DetailedTopic metaData, _topic topic) {
        _occurrence[] occurrences = topic.getOccurrence();
        String type = null;
        if (occurrences != null) {
            for (int i = 0; i < occurrences.length; i++) {
                if (occurrences[i].getInstanceOf() != null) {
                    type = occurrences[i].getInstanceOf().getTopicRef().getHref();
                    if (type.endsWith(TEMPORAL_AT_OCCURRENCE)) {
                        metaData.setFrom(occurrences[i].getResourceData().getValue());
                        metaData.setTo(metaData.getFrom());
                        break;
                    }
                    if (type.endsWith(TEMPORAL_FROM_OCCURRENCE)) {
                        metaData.setFrom(occurrences[i].getResourceData().getValue());
                    }
                    if (type.endsWith(TEMPORAL_TO_OCCURRENCE)) {
                        metaData.setTo(metaData.getFrom());
                    }
                }
            }
        }
    }

    /**
     * @param topic
     * @return a ingrid topic from a _topic
     */
    private synchronized Topic buildTopicFrom_topic(_topic topic) {
        return new Topic(topic.getId(), topic.getBaseName()[0].getBaseNameString().getValue());
    }

    /**
     * @param baseTopic
     * @param typePattern
     * @return _topic array of associated topics filter by the given patterns
     * @throws Exception
     */
    private synchronized _topic[] getAssociatedTopics(_topic baseTopic, String[] typePattern) throws Exception {
        ArrayList resultList = new ArrayList();

        _topicMapFragment mapFragment = this.fServiceClient.getPSI(baseTopic.getId(), 1);
        _topic[] topics = mapFragment.getTopicMap().getTopic();
        _association[] associations = mapFragment.getTopicMap().getAssociation();
        // iterate through associations to find the correct association types
        if (associations != null) {
            for (int i = 0; i < associations.length; i++) {
                _association association = associations[i];
                // association type
                String href = association.getInstanceOf().getTopicRef().getHref();
                if (containsTypes(typePattern, href)) {
                    // association mebers are the basetopic and it association
                    _member[] members = association.getMember();
                    for (int j = 0; j < members.length; j++) {
                        _member member = members[j];
                        // here is only the topic id available
                        String topicId = member.getTopicRef()[0].getHref();
                        if (!topicId.equals(baseTopic.getId())) {
                            _topic topicById = getTopicById(topics, topicId);
                            if (topicById != null) {
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
        // TODO why does this fail?
        // _topicMapFragment mapFragment =
        // this.fServiceClient.findTopics(queryTerm, topicType,
        // SearchType.exact,
        // FieldsType.allfields, 0);
        _topicMapFragment mapFragment = this.fServiceClient.findTopics(queryTerm, topicType, SearchType.exact, null,
                offSet);
        _topic[] topics = mapFragment.getTopicMap().getTopic();
        if (topics.length == 1) {
            return topics[0];
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
     * @return an array of Topic with the given lenght
     * @throws Exception
     */
    private Topic[] copyToTopicArray(_topic[] topics, int maxResults) throws Exception {
        int count = Math.min(maxResults, topics.length);
        Topic[] ingridTopics = new Topic[count];
        for (int i = 0; i < count; i++) {
            ingridTopics[i] = buildTopicFrom_topic(topics[i]);
        }
        return ingridTopics;
    }

}