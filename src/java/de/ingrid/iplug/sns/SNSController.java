/*
 * Copyright (c) 2003 by media style GmbH
 * 
 * $Source: /cvs/SiemensPI/ms_codetemplates.xml,v $
 */

package de.ingrid.iplug.sns;

import de.ingrid.iplug.sns.utils.Topic;
import de.ingrid.iplug.sns.utils.TopicMetaData;

public class SNSController {

    private SNSServiceClient fServiceClient;

    /**
     * Constructs an instance by using the given <code>SNSServiceClient</code>
     * 
     * @param client
     *            The adapter to use for communication with the sns webservice.
     */
    public SNSController(SNSServiceClient client) {
        this.fServiceClient = client;
    }

    /**
     * Tries to find a descriptor to the given queryTerm and asks the sns
     * webservice for assoziated topics. The result contains synonyms, narrower
     * and wider topics.
     * 
     * @param queryTerm
     *            The term to analyze.
     * @param maxResults
     *            The result will be limited to maxResults.
     * @throws Exception
     * @return
     */
    public Topic[] getTopicsForTerm(String queryTerm, int maxResults) throws Exception {
        return null;
      
    }

    /**
     * Tries to find associated topics to the topic represented by the given
     * topicID. The result contains synonyms, narrower and wider topics.
     * 
     * @param topicID
     *            The topic to browse.
     * @param maxResults
     *            The result will be limited to maxResults.
     * @throws Exception
     * @return
     */

    public Topic[] getTopicsForTopic(String topicID, int maxResults) throws Exception {
        return null;       
    }

    /**
     * Tries to find related topics for the given documentText.
     * 
     * @param documentText
     *            The text to analyze.
     * @param analyzeMaxWords
     *            Only the first analyzeMaxWords will be used for the analysis.
     * @throws Exception
     * @return
     */
    public TopicMetaData[] getTopicsForText(String documentText, int analyzeMaxWords) throws Exception {
        return null;
      
    }

}
