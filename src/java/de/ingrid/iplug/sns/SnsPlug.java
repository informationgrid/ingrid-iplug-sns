/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.sns;

import de.ingrid.iplug.IPlug;
import de.ingrid.iplug.PlugDescription;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.config.Configuration;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.TermQuery;
import de.ingrid.utils.queryparser.IDataTypes;

/**
 */
public class SnsPlug implements IPlug {

    private static final String CONFIGURATIONFILE = "sns.config.xml";

    private static final String REQUEST_TYPE = "sns_request_type";

    private static final String TOPIC_FROM_TERM = "topicFromTerm";

    private static final String TOPIC_FROM_TEXT = "topicFromText";

    private static final String TOPIC_FROM_TOPIC = "topicFromTopic";

    private SNSController fSnsController;

    private int fMaximalAnalyzedWord;

    /**
     * connects to the sns server
     * 
     * @throws Exception
     */
    public SnsPlug() throws Exception {
        Configuration configuration = new Configuration();
        configuration.load(this.getClass().getResourceAsStream(CONFIGURATIONFILE));
        String username = configuration.get("username", "");
        String password = configuration.get("password", "");
        String language = configuration.get("language", "de");
        this.fMaximalAnalyzedWord = configuration.getAsInt("maxWordForAnalyzing", 1000);
        this.fSnsController = new SNSController(new SNSClient(username, password, language));
    }

    /**
     * @see de.ingrid.iplug.IPlug#search(de.ingrid.utils.query.IngridQuery, int, int)
     */
    public IngridHits search(IngridQuery query, int start, int lenght) {
        if (query.getDataType().equals(IDataTypes.SNS)) {
            String type = (String) query.get(REQUEST_TYPE);
            try {
                if (type.equals(TOPIC_FROM_TERM)) {
                    this.fSnsController.getTopicsForTerm(getSearchTerm(query), start, lenght);
                } else if (type.equals(TOPIC_FROM_TEXT)) {
                    this.fSnsController.getTopicsForText((String) query.getContent(), this.fMaximalAnalyzedWord);
                } else if (type.equals(TOPIC_FROM_TOPIC)) {
                    this.fSnsController.getTopicsForTopic(getSearchTerm(query), lenght);
                }
            } catch (Exception e) {
                //TODO: log/react
            }
        }

        return null;
    }

    private String getSearchTerm(IngridQuery query) {
        TermQuery[] terms = query.getTerms();
        if (terms.length > 1) {
            throw new IllegalArgumentException("only one term per query is allowed");
        }
        String searchTerm = terms[0].getTerm();
        return searchTerm;
    }

    public void configure(PlugDescription plugDescription) throws Exception {
        // need some config
    }

    public IngridDocument getDetails(IngridHit hit) throws Exception {
        return null;
    }
}
