/*
 * Copyright (c) 1997-2006 by media style GmbH
 */

package de.ingrid.iplug.sns;

import java.util.ArrayList;

import de.ingrid.iplug.sns.utils.Topic;
import de.ingrid.utils.processor.IPreProcessor;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.TermQuery;

/**
 * 
 */
public class PreProcess implements IPreProcessor {

    private SNSController fController;

    /**
     * @param controller
     */
    public PreProcess(SNSController controller) {
        this.fController = controller;
    }

    public void process(IngridQuery query) throws Exception {
        switch (query.getType()) {
        case IngridQuery.TERM:
            ArrayList termsList = new ArrayList();
            TermQuery[] termQuery = query.getTerms();
            for (int i = 0; i < termQuery.length; i++) {
                termsList.add(termQuery[i].getTerm());
            }
            String[] terms = new String[termsList.size()];
            terms = (String[]) termsList.toArray(terms);

            ArrayList similarTermsList = new ArrayList();
            if (terms.length > 0) {
                Topic[] topic = this.fController.getSimilarTermsFromTopic(terms, Integer.MAX_VALUE);
                for (int i = 0; i < topic.length; i++) {
                    similarTermsList.add(topic[i].getTopicName());
                }
                String[] similarTerms = new String[similarTermsList.size()];
                similarTerms = (String[]) similarTermsList.toArray(terms);

                query.put("similarTerms", similarTerms);
            }
            break;
        }
    }
}
