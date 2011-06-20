/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.sns;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.external.sns.SNSClient;
import de.ingrid.iplug.sns.utils.Topic;
import de.ingrid.utils.IPlug;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.processor.ProcessorPipe;
import de.ingrid.utils.processor.ProcessorPipeFactory;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.TermQuery;
import de.ingrid.utils.queryparser.IDataTypes;
import de.ingrid.utils.tool.SNSUtil;

/**
 * Semantic Network Service as IPlug.
 */
public class SnsPlug implements IPlug {

    private static Log log = LogFactory.getLog(SnsPlug.class);

    private SNSController fSnsController;

    private int fMaximalAnalyzedWord;

    private String fPlugId;

    private String fUserName;

    private String fPassWord;

    private String fLanguage;

    private String fServiceUrl;

	private ProcessorPipe _processorPipe = new ProcessorPipe();

    private static final long serialVersionUID = SnsPlug.class.getName().hashCode();

    /**
     * Default constructor needed for server instantiation.
     */
    public SnsPlug() {
        // Default constructor for server instantiation.
    }

    /**
     * Constructor with full description as IPlug.
     * 
     * @param description
     *            The description as IPlug.
     * @throws Exception
     */
    public SnsPlug(PlugDescription description) throws Exception {
        configure(description);
    }

    /**
     * @see de.ingrid.utils.IPlug#search(de.ingrid.utils.query.IngridQuery, int, int)
     */
    public IngridHits search(IngridQuery query, int start, int length)
			throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("incomming query : " + query.toString());
        }

        IngridHits ret = new IngridHits(this.fPlugId, 0, new IngridHit[0], true);
        _processorPipe.preProcess(query);
        
        if (containsSNSDataType(query.getDataTypes())) {
            int type = getRequestType(query);

            final String lang = getQueryLang(query);
            final boolean expired = getExpiredField(query);
            final boolean includeUse = getIncludeUseField(query);
            int[] totalSize = new int[1];
            totalSize[0] = 0;

            try {
                String filter = null;
                Topic[] hitsTemp = null;
                switch (type) {
                case Topic.TOPIC_FROM_TERM:
                	// ONLY RETURNS THESA TOPICS !!!
                    hitsTemp = this.fSnsController.getTopicsForTerm(getSearchTerm(query), start, Integer.MAX_VALUE,
                            this.fPlugId, totalSize, lang, expired, includeUse);
                    break;
                case Topic.TOPIC_FROM_TEXT:
                    filter = (String) query.get("filter");
                    hitsTemp = this.fSnsController.getTopicsForText(getSearchTerm(query), this.fMaximalAnalyzedWord,
                            filter, this.fPlugId, lang, totalSize, expired);
                    break;
                case Topic.TOPIC_FROM_URL:
                    filter = (String) query.get("filter");
                    hitsTemp = this.fSnsController.getTopicsForURL(getSearchTerm(query), this.fMaximalAnalyzedWord,
                            filter, this.fPlugId, lang, totalSize);
                    break;
                case Topic.TOPIC_FROM_TOPIC:
                	// ONLY CALLED FROM EXTENDED SEARCH THESAURUS !!!!
                	filter = "/thesa";
                    hitsTemp = this.fSnsController.getTopicsForTopic(getSearchTerm(query), Integer.MAX_VALUE,
                    		filter, this.fPlugId, lang, totalSize, expired);
                    break;
                case Topic.TOPIC_FROM_ID:
                	// ONLY CALLED FROM Thesaurus Browser in Portal (GSSoil fetch english term) !!!
                    filter = (String) query.get("filter");
                    hitsTemp = this.fSnsController.getTopicForId(getSearchTerm(query), filter, this.fPlugId, lang, totalSize);
                    break;
                case Topic.ANNIVERSARY_FROM_TOPIC:
                    hitsTemp = this.fSnsController.getAnniversaryFromTopic(getSearchTerm(query), Integer.MAX_VALUE,
                            this.fPlugId, totalSize);
                    break;
                case Topic.SIMILARTERMS_FROM_TOPIC:
                    hitsTemp = this.fSnsController.getSimilarTermsFromTopic(getSearchTerm(query), Integer.MAX_VALUE,
                            this.fPlugId, totalSize, lang);
                    break;
                case Topic.SIMILARLOCATIONS_FROM_TOPIC:
                    hitsTemp = this.fSnsController.getTopicSimilarLocationsFromTopic(getSearchTerm(query),
                            Integer.MAX_VALUE, this.fPlugId, totalSize, lang);
                    break;
                case Topic.EVENT_FROM_TOPIC:
                    final String[] eventType = (String[]) query.get("eventtype");
                    final String atDate = (String) query.get("t0");
                    final String fromDate = (String) query.get("t1");
                    final String toDate = (String) query.get("t2");
                    if (null != atDate) {
                        hitsTemp = this.fSnsController.getEventFromTopic(getSearchTerm(query), eventType, atDate,
                                start, length, this.fPlugId, totalSize, lang);
                    } else {
                        hitsTemp = this.fSnsController.getEventFromTopic(getSearchTerm(query), eventType, fromDate,
                                toDate, start, length, this.fPlugId, totalSize, lang);
                    }
                    break;
                case Topic.TOPIC_HIERACHY:
                    final String associationName = (String) query.get("association");
                    final long depth = Long.valueOf((String) query.get("depth")).longValue();
                    final String direction = (String) query.get("direction");
                    final boolean includeSiblings = Boolean.valueOf((String) query.get("includeSiblings"))
                            .booleanValue();
                    hitsTemp = this.fSnsController.getTopicHierachy(totalSize, associationName, depth, direction,
                            includeSiblings, lang, getSearchTerm(query), expired, this.fPlugId);
                    break;
                default:
                    log.error("Unknown topic request type.");
                    break;
                }
                Topic[] hits = new Topic[0];
                if (null != hitsTemp) {
                    hits = hitsTemp;
                }

                int max;
                if ((Topic.EVENT_FROM_TOPIC == type) || (Topic.TOPIC_FROM_TERM == type)) {
                    start = 0;
                }

                if (start > hits.length) {
                    start = hits.length;
                }
                max = Math.min((hits.length - start), length);
                IngridHit[] finalHits = new IngridHit[max];
                System.arraycopy(hits, start, finalHits, 0, max);
                if (log.isDebugEnabled()) {
                    log.debug("hits: " + totalSize[0]);
                }

                if ((0 == totalSize[0]) && (hits.length > 0)) {
                    totalSize[0] = hits.length;
                }
                ret = new IngridHits(this.fPlugId, totalSize[0], finalHits,
						false);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        } else {
            if (log.isErrorEnabled()) {
                log.error("not correct or unsetted datatype");
            }
        }
        
        _processorPipe.postProcess(query, ret.getHits());
		return ret;
    }

    private boolean getIncludeUseField(IngridQuery query) {
        boolean result = false;

        FieldQuery[] qFields = query.getFields();
        for (int i = 0; i < qFields.length; i++) {
            final String fieldName = qFields[i].getFieldName();
            if (fieldName.equals("includeUse")) {
                result = Boolean.valueOf(qFields[i].getFieldValue()).booleanValue();
            }
        }

        return result;
    }

    private int getRequestType(final IngridQuery query) {
        Object resultO = null;
        int result = -1;

        resultO = query.get(Topic.REQUEST_TYPE);

        if (null == resultO) {
            FieldQuery[] fieldQueries = query.getFields();
            for (int i = 0; i < fieldQueries.length; i++) {
                String fieldName = fieldQueries[i].getFieldName();
                if (fieldName.equals(Topic.REQUEST_TYPE)) {
                    resultO = fieldQueries[i].getFieldValue();
                }
            }
        }

        if (resultO instanceof Integer) {
            result = ((Integer) resultO).intValue();
        } else if (resultO instanceof String) {
            result = Integer.parseInt((String) resultO);
        }
        return result;
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

    /** Search term is the topic ID ! */
    private String getSearchTerm(IngridQuery query) {
        TermQuery[] terms = query.getTerms();
        if (terms.length > 1) {
            throw new IllegalArgumentException("only one term per query is allowed");
        }

        String searchTerm = "";
        if (terms.length > 0) {
            searchTerm = terms[0].getTerm();
        }

        // GSSoil Thesaurus Service topic id is marshalled due to special characters (URL)
		searchTerm = SNSUtil.unmarshallTopicId(searchTerm);
		if (log.isDebugEnabled()) {
			log.debug("unmarshalled topicID = " + searchTerm);
		}

        return searchTerm;
    }

    public void configure(PlugDescription plugDescription) throws Exception {
        this.fPlugId = plugDescription.getPlugId();
        this.fUserName = (String) plugDescription.get("username");
        this.fPassWord = (String) plugDescription.get("password");
        this.fLanguage = (String) plugDescription.get("language");
        this.fServiceUrl = (String) plugDescription.get("serviceUrl");
        this.fMaximalAnalyzedWord = plugDescription.getInt("maxWordAnalyzing");
        String nativeKeyPrefix = (String) plugDescription.get("nativeKeyPrefix");

        SNSClient snsClient = null;
        if ((this.fServiceUrl == null) || (this.fServiceUrl.trim().equals(""))) {
            snsClient = new SNSClient(this.fUserName, this.fPassWord, this.fLanguage);
        } else {
            snsClient = new SNSClient(this.fUserName, this.fPassWord, this.fLanguage, new URL(this.fServiceUrl));
        }

        snsClient.setTimeout(180000);
        if (null == nativeKeyPrefix) {
            nativeKeyPrefix = "ags:";
        }
        this.fSnsController = new SNSController(snsClient, nativeKeyPrefix);
		ProcessorPipeFactory processorPipeFactory = new ProcessorPipeFactory(
				plugDescription);
        _processorPipe = processorPipeFactory.getProcessorPipe();
    }

    /**
     * @see de.ingrid.utils.IDetailer#getDetail(de.ingrid.utils.IngridHit, de.ingrid.utils.query.IngridQuery,
     *      java.lang.String[])
     */
    public IngridHitDetail getDetail(IngridHit hit, IngridQuery query, String[] fields) throws Exception {
        String lang = getQueryLang(query);
        String filter = (String) query.get("filter");

        return this.fSnsController.getTopicDetail(hit, filter, lang);
    }

    private String getQueryLang(IngridQuery query) {
        String result = this.fLanguage;

        FieldQuery[] qFields = query.getFields();
        for (int i = 0; i < qFields.length; i++) {
            final String fieldName = qFields[i].getFieldName();
            if (fieldName.equals("lang")) {
                result = qFields[i].getFieldValue();
            }
        }

        return result;
    }

    private boolean getExpiredField(IngridQuery query) {
        boolean result = false;

        FieldQuery[] qFields = query.getFields();
        for (int i = 0; i < qFields.length; i++) {
            final String fieldName = qFields[i].getFieldName();
            if (fieldName.equals("expired")) {
                result = Boolean.valueOf(qFields[i].getFieldValue()).booleanValue();
            }
        }

        return result;
    }

    /**
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
