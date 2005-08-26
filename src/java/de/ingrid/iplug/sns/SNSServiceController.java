package de.ingrid.iplug.sns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.slb.taxi.webservice.xtm.stubs.SearchType;
import com.slb.taxi.webservice.xtm.stubs._topicMapFragment;
import com.slb.taxi.webservice.xtm.stubs.xtm._association;
import com.slb.taxi.webservice.xtm.stubs.xtm._occurrence;
import com.slb.taxi.webservice.xtm.stubs.xtm._topic;

import de.ingrid.iplug.sns.filter.AdminTypeFilter;
import de.ingrid.iplug.sns.filter.TypeFilter;
import de.ingrid.iplug.sns.utils.DocumentMetaData;
import de.ingrid.iplug.sns.utils.Topic;

/**
 * 
 * created on 21.07.2005
 * <p>
 * 
 * @author hs
 */
public class SNSServiceController {
    private static final String TEMPORAL_TO_OCCURRENCE = "temporalToOcc";

    private static final String TEMPORAL_FROM_OCCURRENCE = "temporalFromOcc";

    private static final String TEMPORAL_AT_OCCURRENCE = "temporalAtOcc";

    private static final String SYNONYM_TYPE = "synonymType";

    private static final String SYNONYM_ASSOC = "synonymAssoc";

    private static final String WIDER_TERM_ASSOC = "widerTermAssoc";

    private static final String NARROWER_TERM_ASSOC = "narrowerTermAssoc";

    private static final String THESA_DESCRIPTOR = "/thesa/descriptor";

    private SNSServiceAdapter fServiceAdapter = null;

    private static TypeFilter analyzeQueryAssocFilter = new TypeFilter(new String[] { NARROWER_TERM_ASSOC,
            WIDER_TERM_ASSOC, SYNONYM_ASSOC });
    
    private static TypeFilter administrativeTypes = new AdminTypeFilter();
    
    /**
     * Constructs an instance by using the given <code>SNSServiceAdapter</code>
     * 
     * @param adapter
     *            The adapter to use for communication with the sns webservice.
     */
    public SNSServiceController(SNSServiceAdapter adapter) {
        this.fServiceAdapter = adapter;
    }
    
    /**
     * 
     */
    public SNSServiceController(){
        super();
    }

    /**
     * Tries to find a descriptor to the given queryTerm and asks the sns webservice 
     * for assoziated topics. The result contains synonyms, narrower and wider topics.  
     * @param queryTerm The term to analyze.
     * @param maxResults The result will be limited to maxResults.
     * @throws
     *         Exception
     * @return 
     */
    public Topic[] analyzeQuery(String queryTerm, int maxResults) throws Exception {
        final List descriptorList = findTopics(queryTerm, THESA_DESCRIPTOR, 1, 1, null);
        if (descriptorList.size() > 0) {
            return toTopicArray(getAssociatedTopics((_topic) descriptorList.get(0), analyzeQueryAssocFilter),
                    maxResults);
        }
        return new Topic[0];
    }

    /**
     * Tries to find associated topics to the topic represented by the given topicID.
     * The result contains synonyms, narrower and wider topics. 
     * @param topicID The topic to browse. 
     * @param maxResults The result will be limited to maxResults.
     * @throws
     *         Exception
     * @return 
     */

    public Topic[] browseThesaurus(String topicID, int maxResults) throws Exception {
        final _topic baseTopic = new _topic();
        baseTopic.setId(topicID);
        final List returnList = getAssociatedTopics(baseTopic, analyzeQueryAssocFilter);
        return toTopicArray(returnList, maxResults);
    }

    /**
     * Tries to find related topics for the given documentText. 
     * @param documentText The text to analyze.
     * @param analyzeMaxWords Only the first analyzeMaxWords will be used for the analysis.  
     * @throws
     *         Exception
     * @return 
     */
    public DocumentMetaData[] getDocumentRelatedTopics(String documentText, int analyzeMaxWords) throws Exception {
        final _topicMapFragment mapFragment = this.fServiceAdapter.autoClassify(documentText, analyzeMaxWords);
        final _topic[] topics = mapFragment.getTopicMap().getTopic();
        if (topics != null) {
            return toDocumentMetaDataArray(topics);
        }
        return new DocumentMetaData[0];
    }

    /**
     * @param topics
     * @return
     */

    private DocumentMetaData[] toDocumentMetaDataArray(_topic[] topics) {
        final List returnList = new ArrayList();
        for (int i = 0; i < topics.length; i++) {
            if (!topics[i].getInstanceOf()[0].getTopicRef().getHref().endsWith(SYNONYM_TYPE)) {
                returnList.add(toDocumentMetaDataTopic(topics[i]));
            }
        }
        return (DocumentMetaData[]) returnList.toArray(new DocumentMetaData[returnList.size()]);
    }

    /**
     * Collects all needed information from the given <code>_topic</code>.  
     * @param topic Holds the needed information.
     * @return
     */
    private static DocumentMetaData toDocumentMetaDataTopic(_topic topic) {
        final DocumentMetaData metaData = new DocumentMetaData(topic.getId(), topic.getBaseName()[0]
                .getBaseNameString().getValue());
        pushTimesTo(metaData, topic);
        if(administrativeTypes.accept(topic.getInstanceOf()[0].getTopicRef().getHref())){
            metaData.setAdministrativeID(topic.getId());
        }
        return metaData;
    }

    /**
     * Convenience method to push related times to the given <code>DocumentMetaData</code> instance.  
     * @param metaData The container to push from the given <code>_topic</code> available times in.
     * @param topic The <code>_topic</code> that probably contains related time descriptions.
     */
    private static void pushTimesTo(DocumentMetaData metaData, _topic topic) {
        final _occurrence[] occurrences = topic.getOccurrence();
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
     * Uses the underlaying sns webservice client to find assoziated topics
     * based on the given base topic. The resulting topics will be limited by
     * using the <code>TypeFilter</code>
     * 
     * @param baseTopic
     * @param filter
     * @throws Exception
     * @return
     */

    private List getAssociatedTopics(_topic baseTopic, TypeFilter filter) throws Exception {
        final List returnList = new ArrayList();
        final _topicMapFragment mapFragment = this.fServiceAdapter.getPSI(baseTopic.getId(), 1);
        final _association[] associations = mapFragment.getTopicMap().getAssociation();
        if (associations != null && mapFragment.getTopicMap().getTopic() != null) {
            final Map topicsMap = toTopicMap(mapFragment);
            _association association = null;
            String memberTopic = null;
            for (int i = 0; i < associations.length; i++) {
                association = associations[i];
                if (filter.accept(association.getInstanceOf().getTopicRef().getHref())) {
                    for (int member = 0; member < association.getMember().length; member++) {
                        memberTopic = association.getMember(member).getTopicRef()[0].getHref();
                        if (!memberTopic.equals(baseTopic.getId())) {
                            returnList.add(topicsMap.get(memberTopic));
                            break;
                        }
                    }
                }
            }
        }
        return returnList;
    }

    /**
     * Uses the underlaying sns webservice client to collect all related topics
     * to the given query.
     * 
     * @param queryTerm
     * @param path
     *            Limits the search scope.
     * @param maxResults
     *            Limits the resultsize.
     * @param offset
     *            The number of topics to skip.
     * @param collector
     *            The collection for results. If collection null a new
     *            <code>List</code> will be created.
     * @throws Exception
     * @return 
     */

    private List findTopics(String queryTerm, String path, int maxResults, int offset, List collector) throws Exception {
        collector = collector == null ? new ArrayList() : collector;
        final _topicMapFragment mapFragment = this.fServiceAdapter.findTopics(queryTerm, path, SearchType.exact, null,
                offset);
        final _topic[] topics = mapFragment.getTopicMap().getTopic();
        if (topics != null) {
            for (int i = 0; i < topics.length && collector.size() < maxResults; i++) {
                collector.add(topics[i]);
            }
            if (collector.size() < maxResults && topics.length == 20) {
                return findTopics(queryTerm, path, maxResults, offset + 20, collector);
            }
        }
        return collector;
    }

    /**
     * Stores all topics from the given <code>_topicMapFragment</code> in a
     * <code>Map</code>. The key for accessing each topics is the topicID.
     * 
     * @param mapFragment
     *            The <code>_topicMapFragment</code> which contains the topics
     *            to store in the map.
     * @return
     */
    private static Map toTopicMap(_topicMapFragment mapFragment) {
        final HashMap topicMap = new HashMap();
        final _topic[] topics = mapFragment.getTopicMap().getTopic();
        if (topics != null) {
            for (int i = 0; i < topics.length; i++) {
                topicMap.put(topics[i].getId(), topics[i]);
            }
        }
        return topicMap;
    }

    /**
     * Converts a list of <code>_topics</code> s to an array of
     * <code>Topics</code>s.
     * 
     * @param topicList
     *            The list which contains the <code>_topic</code> instances.
     * @param maxSize
     *            The size of the resulting array will be limited to maxSize.
     * @return
     */

    private static Topic[] toTopicArray(List topicList, int maxSize) {
        final Topic[] returnTopics = new Topic[Math.min(maxSize, topicList.size())];
        _topic topic = null;
        for (int i = 0; i < returnTopics.length; i++) {
            topic = (_topic) topicList.get(i);
            returnTopics[i] = new Topic(topic.getId(), topic.getBaseName()[0].getBaseNameString().getValue());
        }
        return returnTopics;
    }
    
    /**
     * @param serviceAdapter The fServiceAdapter to set.
     */
    public void setServiceAdapter(SNSServiceAdapter serviceAdapter) {
        this.fServiceAdapter = serviceAdapter;
    }

}