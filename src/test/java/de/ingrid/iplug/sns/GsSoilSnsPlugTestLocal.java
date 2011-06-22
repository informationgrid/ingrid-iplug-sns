/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.sns;

import junit.framework.TestCase;
import de.ingrid.iplug.sns.utils.Topic;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.IDataTypes;
import de.ingrid.utils.queryparser.QueryStringParser;
import de.ingrid.utils.tool.SNSUtil;

/**
 * 
 */
public class GsSoilSnsPlugTestLocal extends TestCase {
    private static PlugDescription fPlugDescription;

    static {
        fPlugDescription = new PlugDescription();
        fPlugDescription.setProxyServiceURL("aPlugId");
        fPlugDescription.put("username", "ms");
        fPlugDescription.put("password", "m3d1asyl3");
        fPlugDescription.put("language", "de");
        fPlugDescription.putInt("maxWordAnalyzing", 100);
    }

    public void testTOPIC_FROM_ID() throws Exception {
        SnsPlug plug = new SnsPlug(fPlugDescription);
        
		String marshalledTopicId = SNSUtil.marshallTopicId("http://www.eionet.europa.eu/gemet/supergroup/5499");

        IngridQuery query = QueryStringParser.parse(marshalledTopicId);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.addField(new FieldQuery(true, false, "lang", "en"));
        query.putInt(Topic.REQUEST_TYPE, Topic.TOPIC_FROM_ID);
        query.put("filter", "/thesa");
        IngridHits hits = plug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        assertTrue(hitsArray.length == 1);
        for (int i = 0; i < hitsArray.length; i++) {
            Topic hit = (Topic) hitsArray[i];
            System.out.println(hit.getTopicName() + ":" + hit.getTopicID() + ":" + hit.getTopicAssoc());
        }
    }
}
