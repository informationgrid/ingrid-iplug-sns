/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.sns;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.iplug.IPlug;
import de.ingrid.iplug.PlugDescription;
import de.ingrid.iplug.sns.utils.Topic;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.TermQuery;
import de.ingrid.utils.queryparser.IDataTypes;

/**
 */
public class SnsPlug implements IPlug {

    private static Log log = LogFactory.getLog(SnsPlug.class);

    private SNSController fSnsController;

    private int fMaximalAnalyzedWord;

    private String fPlugId;

    private String fUserName;

    private String fPassWord;

    private String fLanguage;

    /**
     */
    public SnsPlug() {
        // default constructor for server instantiation..
    }

    /**
     * @param description
     * @throws Exception
     */
    public SnsPlug(PlugDescription description) throws Exception {
        configure(description);
    }

    /**
     * @see de.ingrid.iplug.IPlug#search(de.ingrid.utils.query.IngridQuery, int,
     *      int)
     */
    public IngridHits search(IngridQuery query, int start, int length) {

        if (log.isDebugEnabled()) {
            log.debug("incomming query : " + query.toString());
        }

        if (query.getDataType() != null
                && query.getDataType().equals(IDataTypes.SNS)) {
            Topic[] hits = new Topic[0];
            int type = query.getInt(Topic.REQUEST_TYPE);

            // FIXME: By TOPIC FROM TOPIC i get the range by the other i must
            // select my range. Is this correct?
            try {
                switch (type) {
                case Topic.TOPIC_FROM_TERM:
                    hits = this.fSnsController.getTopicsForTerm(
                            getSearchTerm(query), start, length);
                    break;
                case Topic.TOPIC_FROM_TEXT:
                    hits = this.fSnsController.getTopicsForText(
                            getSearchTerm(query), this.fMaximalAnalyzedWord);
                    break;
                case Topic.TOPIC_FROM_TOPIC:
                    hits = this.fSnsController.getTopicsForTopic(
                            getSearchTerm(query), length);
                    break;
                case Topic.ANNIVERSARY_FROM_TOPIC:
                    hits = this.fSnsController.getAnniversaryFromTopic(
                            getSearchTerm(query), length);
                    break;
                case Topic.SIMILARTERMS_FROM_TOPIC:
                    hits = this.fSnsController.getSimilarTermsFromTopic(
                            getSearchTerm(query), length);
                    break;
                case Topic.EVENT_FROM_TOPIC:
                    final String eventType = (String) query.get("eventtype");
                    final String atDate = (String) query.get("t0");
                    final String fromDate = (String) query.get("t1");
                    final String toDate = (String) query.get("t2");
                    if (null != atDate) {
                        hits = this.fSnsController.getEventFromTopic(
                                getSearchTerm(query), eventType, atDate, start,
                                length);
                    } else {
                        hits = this.fSnsController.getEventFromTopic(
                                getSearchTerm(query), eventType, fromDate,
                                toDate, start, length);
                    }
                    break;
                default:
                    log.error("Unknown topic request type.");
                    break;
                }

                // FIXME: I think this is wrong if you want to get a range. But
                // see FIXME above.
                int max = Math.min(hits.length, length);
                IngridHit[] finalHits = new IngridHit[max];
                System.arraycopy(hits, start, finalHits, 0, max);
                if (log.isDebugEnabled()) {
                    log.debug("hits: " + hits.length);
                }
                // lets set the plugId and documentId;
                int count = finalHits.length;
                for (int i = 0; i < count; i++) {
                    IngridHit hit = finalHits[i];
                    hit.setPlugId(this.fPlugId);
                    hit.setDataSourceId(i);
                }

                return new IngridHits(this.fPlugId, hits.length, finalHits,
                        false);
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("not correct or unsetted datatype");
        }
        return new IngridHits(this.fPlugId, 0, new IngridHit[0], false);
    }

    private String getSearchTerm(IngridQuery query) {
        TermQuery[] terms = query.getTerms();
        if (terms.length > 1) {
            throw new IllegalArgumentException(
                    "only one term per query is allowed");
        }

        String searchTerm = "";
        if (terms.length > 0) {
            searchTerm = terms[0].getTerm();
        }
        return searchTerm;
    }

    public void configure(PlugDescription plugDescription) throws Exception {
        this.fPlugId = plugDescription.getPlugId();
        this.fUserName = (String) plugDescription.get("username");
        this.fPassWord = (String) plugDescription.get("password");
        this.fLanguage = (String) plugDescription.get("language");
        this.fMaximalAnalyzedWord = plugDescription
                .getInt("maxWordForAnalyzing");
        this.fSnsController = new SNSController(new SNSClient(this.fUserName,
                this.fPassWord, this.fLanguage));
    }

    public IngridHitDetail getDetails(IngridHit hit, IngridQuery query)
            throws Exception {
        IngridHitDetail result = null;
        Topic topic = (Topic) hit;
        return this.fSnsController.getTopicDetail(topic.getTopicID());

    }
}
