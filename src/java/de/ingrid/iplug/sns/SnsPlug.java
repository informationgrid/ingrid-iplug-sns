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
    
    public SnsPlug() {
     // default constructor for server instantiation..
    }

    /**
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

        if (query.getDataType()!=null && query.getDataType().equals(IDataTypes.SNS)) {
            Topic[] hits = new Topic[0];
            int type = query.getInt(Topic.REQUEST_TYPE);

            // FIXME: By TOPIC FROM TOPIC i get the range by the other i must
            // select my range. Is this correct?
            try {
                if (type == Topic.TOPIC_FROM_TERM) {
                    hits = this.fSnsController.getTopicsForTerm(
                            getSearchTerm(query), start, length);
                } else if (type == Topic.TOPIC_FROM_TEXT) {
                    hits = this.fSnsController.getTopicsForText(getSearchTerm(query)
                            , this.fMaximalAnalyzedWord);
                } else if (type == Topic.TOPIC_FROM_TOPIC) {
                    hits = this.fSnsController.getTopicsForTopic(
                            getSearchTerm(query), length);
                }

                int max = Math.min(hits.length, length);
                IngridHit[] finalHits = new IngridHit[max];
                System.arraycopy(hits, start, finalHits, 0, max);
                return new IngridHits(this.fPlugId, hits.length, finalHits);
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }

        return new IngridHits(this.fPlugId, 0, new IngridHit[0]);
    }

    // private IngridHitSns[] translateTopicToHit(Topic[] topic, int type, int
    // start, int max) {
    // IngridHitSns[] hits = new IngridHitSns[max];
    // for (int i = start; i < (max + start); i++) {
    // // FIXME: What is the id?
    // final int id = 1;
    // // FIXME: What is the score?
    // final float score = 1;
    // // FIXME: What is the datasource ID?
    // final int dataSourceId = 1;
    //
    // IngridHitSns ingridHit = new IngridHitSns(this.fPlugId, id, dataSourceId,
    // score, type,topic[i].toString());
    //
    // hits[i - start] = ingridHit;
    // }
    // return hits;
    // }

    private String getSearchTerm(IngridQuery query) {
        TermQuery[] terms = query.getTerms();
        if (terms.length > 1) {
            throw new IllegalArgumentException(
                    "only one term per query is allowed");
        }
        String searchTerm = terms[0].getTerm();
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
        return null;
    }
}
