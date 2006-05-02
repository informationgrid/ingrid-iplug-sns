/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.sns;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.iplug.sns.utils.Topic;
import de.ingrid.utils.IPlug;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.query.FieldQuery;
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

    private static final long serialVersionUID = SnsPlug.class.getName().hashCode();

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
     * @see de.ingrid.utils.IPlug#search(de.ingrid.utils.query.IngridQuery, int, int)
     */
    public IngridHits search(IngridQuery query, int start, int length) {
        if (log.isDebugEnabled()) {
            log.debug("incomming query : " + query.toString());
        }

        if (containsSNSDataType(query.getDataTypes())) {
            int type = query.getInt(Topic.REQUEST_TYPE);

            try {
                Topic[] hitsTemp = null;
                switch (type) {
                case Topic.TOPIC_FROM_TERM:
                    hitsTemp = this.fSnsController.getTopicsForTerm(getSearchTerm(query), start, length, this.fPlugId);
                    break;
                case Topic.TOPIC_FROM_TEXT:
                    final String filter = (String) query.get("filter");
                    hitsTemp = this.fSnsController.getTopicsForText(getSearchTerm(query), this.fMaximalAnalyzedWord,
                            filter, this.fPlugId);
                    break;
                case Topic.TOPIC_FROM_TOPIC:
                    hitsTemp = this.fSnsController.getTopicsForTopic(getSearchTerm(query), length, this.fPlugId);
                    break;
                case Topic.ANNIVERSARY_FROM_TOPIC:
                    hitsTemp = this.fSnsController.getAnniversaryFromTopic(getSearchTerm(query), length, this.fPlugId);
                    break;
                case Topic.SIMILARTERMS_FROM_TOPIC:
                    hitsTemp = this.fSnsController.getSimilarTermsFromTopic(getSearchTerm(query), length, this.fPlugId);
                    break;
                case Topic.SIMILARLOCATIONS_FROM_TOPIC:
                    hitsTemp = this.fSnsController.getTopicSimilarLocationsFromTopic(getSearchTerm(query), length,
                            this.fPlugId);
                    break;
                case Topic.EVENT_FROM_TOPIC:
                    final String[] eventType = (String[]) query.get("eventtype");
                    final String atDate = (String) query.get("t0");
                    final String fromDate = (String) query.get("t1");
                    final String toDate = (String) query.get("t2");
                    if (null != atDate) {
                        hitsTemp = this.fSnsController.getEventFromTopic(getSearchTerm(query), eventType, atDate,
                                start, length, this.fPlugId);
                    } else {
                        hitsTemp = this.fSnsController.getEventFromTopic(getSearchTerm(query), eventType, fromDate,
                                toDate, start, length, this.fPlugId);
                    }
                    break;
                default:
                    log.error("Unknown topic request type.");
                    break;
                }
                Topic[] hits = new Topic[0];
                if (null != hitsTemp) {
                    hits = hitsTemp;
                }

                int max = Math.min(hits.length, length);
                IngridHit[] finalHits = new IngridHit[max];
                System.arraycopy(hits, start, finalHits, 0, max);
                if (log.isDebugEnabled()) {
                    log.debug("hits: " + hits.length);
                }

                return new IngridHits(this.fPlugId, hits.length, finalHits, false);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        } else {
            if (log.isErrorEnabled()) {
                log.error("not correct or unsetted datatype");
            }
        }
        return new IngridHits(this.fPlugId, 0, new IngridHit[0], true);
    }

    private boolean containsSNSDataType(FieldQuery[] dataTypes) {
        int count = dataTypes.length;
        for (int i = 0; i < count; i++) {
            FieldQuery query = dataTypes[i];
            if (query.getFieldValue().equals(IDataTypes.SNS) && !query.isProhibited()) {
                return true;
            }
        }
        return false;
    }

    private String getSearchTerm(IngridQuery query) {
        TermQuery[] terms = query.getTerms();
        if (terms.length > 1) {
            throw new IllegalArgumentException("only one term per query is allowed");
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
        this.fMaximalAnalyzedWord = plugDescription.getInt("maxWordForAnalyzing");
        SNSClient snsClient = new SNSClient(this.fUserName, this.fPassWord, this.fLanguage);
        snsClient.setTimeout(180000);
        this.fSnsController = new SNSController(snsClient);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.utils.IDetailer#getDetail(de.ingrid.utils.IngridHit, de.ingrid.utils.query.IngridQuery,
     *      java.lang.String[])
     */
    public IngridHitDetail getDetail(IngridHit hit, IngridQuery query, String[] fields) throws Exception {
        return this.fSnsController.getTopicDetail(hit);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.utils.IDetailer#getDetails(de.ingrid.utils.IngridHit[], de.ingrid.utils.query.IngridQuery,
     *      java.lang.String[])
     */
    public IngridHitDetail[] getDetails(IngridHit[] hits, IngridQuery query, String[] requestedFields) throws Exception {
        IngridHitDetail[] details = new IngridHitDetail[hits.length];
        for (int i = 0; i < hits.length; i++) {
            details[i] = getDetail(hits[i], query, requestedFields);
        }
        return details;
    }

    public void close() throws Exception {
        // nothing to do.
    }
}
