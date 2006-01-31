/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.sns;

import junit.framework.TestCase;
import de.ingrid.iplug.PlugDescription;
import de.ingrid.iplug.sns.utils.DetailedTopic;
import de.ingrid.iplug.sns.utils.Topic;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.TermQuery;
import de.ingrid.utils.queryparser.IDataTypes;
import de.ingrid.utils.queryparser.QueryStringParser;

public class SnsPlugTest extends TestCase {
    private static PlugDescription fPlugDescription;
    static {
        fPlugDescription = new PlugDescription();
        fPlugDescription.setPlugId("aPlugId");
        fPlugDescription.put("username", "ms");
        fPlugDescription.put("password", "portalu2006");
        fPlugDescription.put("language", "de");
        fPlugDescription.putInt("maxWordForAnalyzing", 100);

    }

    public void testTOPIC_FROM_TERM() throws Exception {
        SnsPlug plug = new SnsPlug(fPlugDescription);
        String q = "Wasser";
        IngridQuery query = QueryStringParser.parse(q);
        query.setDataType(IDataTypes.SNS);
        query.putInt(Topic.REQUEST_TYPE, Topic.TOPIC_FROM_TERM);
        IngridHits hits = plug.search(query, 0, 10);
        IngridHit[] hitsArray =  hits.getHits();
        assertNotNull(hitsArray);
        for (int i = 0; i < hitsArray.length; i++) {
            Topic hit = (Topic) hitsArray[i];
            System.out.println(hit.getTopicName()+":"+hit.getTopicID());
        }
        System.out.println("##########");
    }
    
    
    public void testTOPIC_FROM_TEXT() throws Exception {
        SnsPlug plug = new SnsPlug(fPlugDescription);
        String q = "\"Tschernobyl liegt in Halle gefunden\"";
//        IngridQuery query = QueryStringParser.parse(q);
        IngridQuery query = new IngridQuery();
        query.addTerm(new TermQuery(TermQuery.AND, q));
        query.setDataType(IDataTypes.SNS);
        query.putInt(Topic.REQUEST_TYPE, Topic.TOPIC_FROM_TEXT);
        IngridHits hits = plug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        for (int i = 0; i < hitsArray.length; i++) {
            DetailedTopic hit = (DetailedTopic) hitsArray[i];
            System.out.println(hit.getTopicName()+":"+hit.getAdministrativeID());
        }
    }
    
    public void testTOPIC_FROM_TOPIC() throws Exception {
        SnsPlug plug = new SnsPlug(fPlugDescription);
        String q = "uba_thes_500855";
        IngridQuery query = QueryStringParser.parse(q);
        
        query.setDataType(IDataTypes.SNS);
        query.putInt(Topic.REQUEST_TYPE, Topic.TOPIC_FROM_TOPIC);
        IngridHits hits = plug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        for (int i = 0; i < hitsArray.length; i++) {
            Topic hit = (Topic) hitsArray[i];
            System.out.println(hit.getTopicName()+":"+hit.getTopicID());
        }
    }
    
    

}
