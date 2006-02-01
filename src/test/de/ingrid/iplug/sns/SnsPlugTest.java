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

/**
 * 
 */
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

    /**
     * @throws Exception
     */
    public void testTOPIC_FROM_TERM() throws Exception {
        SnsPlug plug = new SnsPlug(fPlugDescription);
        String q = "Wasser";
        IngridQuery query = QueryStringParser.parse(q);
        query.setDataType(IDataTypes.SNS);
        query.putInt(Topic.REQUEST_TYPE, Topic.TOPIC_FROM_TERM);
        IngridHits hits = plug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        for (int i = 0; i < hitsArray.length; i++) {
            Topic hit = (Topic) hitsArray[i];
            System.out.println(hit.getTopicName() + ":" + hit.getTopicID());
        }
        System.out.println("##########");
    }

    /**
     * @throws Exception
     */
    public void testTOPIC_FROM_TEXT() throws Exception {
        SnsPlug plug = new SnsPlug(fPlugDescription);
        String q = "\"Tschernobyl liegt in Halle gefunden\"";
        // IngridQuery query = QueryStringParser.parse(q);
        IngridQuery query = new IngridQuery();
        query.addTerm(new TermQuery(IngridQuery.AND, q));
        query.setDataType(IDataTypes.SNS);
        query.putInt(Topic.REQUEST_TYPE, Topic.TOPIC_FROM_TEXT);
        IngridHits hits = plug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        for (int i = 0; i < hitsArray.length; i++) {
            DetailedTopic hit = (DetailedTopic) hitsArray[i];
            System.out.println(hit.getTopicName() + ":" + hit.getAdministrativeID());
        }
    }

    /**
     * @throws Exception
     */
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
            System.out.println(hit.getTopicName() + ":" + hit.getTopicID());
        }
    }
    
    /**
     * @throws Exception
     */
    public void testANNIVERSARY() throws Exception {
        SnsPlug plug = new SnsPlug(fPlugDescription);
        String q = "1978-07-30";
        IngridQuery query = QueryStringParser.parse(q);
        query.setDataType(IDataTypes.SNS);
        query.putInt(Topic.REQUEST_TYPE, Topic.ANNIVERSARY_FROM_TOPIC);
        IngridHits hits = plug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        for (int i = 0; i < hitsArray.length; i++) {
            Topic hit = (Topic) hitsArray[i];
            System.out.println(hit.getTopicName() + ":" + hit.getTopicID());
        }
    }
    
    /**
     * @throws Exception
     */
    public void testSIMILARTERMS() throws Exception {
        SnsPlug plug = new SnsPlug(fPlugDescription);
        String q = "blau";
        IngridQuery query = QueryStringParser.parse(q);
        query.setDataType(IDataTypes.SNS);
        query.putInt(Topic.REQUEST_TYPE, Topic.SIMILARTERMS_FROM_TOPIC);
        IngridHits hits = plug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        for (int i = 0; i < hitsArray.length; i++) {
            Topic hit = (Topic) hitsArray[i];
            System.out.println(hit.getTopicName() + ":" + hit.getTopicID());
        }
    }
    
    /**
     * @throws Exception
     */
    public void testEVENT_AT() throws Exception {
        SnsPlug plug = new SnsPlug(fPlugDescription);
        String q = "ohio";
        IngridQuery query = QueryStringParser.parse(q);
        query.setDataType(IDataTypes.SNS);
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
        query.put("eventtype", "industrialAccident");
        query.put("t0", "1976-08-31");
        IngridHits hits = plug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        for (int i = 0; i < hitsArray.length; i++) {
            Topic hit = (Topic) hitsArray[i];
            System.out.println(hit.getTopicName() + ":" + hit.getTopicID());
        }
    }
    
    /**
     * @throws Exception
     */
    public void testEVENT_BETWEEN() throws Exception {
        SnsPlug plug = new SnsPlug(fPlugDescription);
        String q = "ohio";
        IngridQuery query = QueryStringParser.parse(q);
        query.setDataType(IDataTypes.SNS);
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
        query.put("eventtype", "disaster");
        query.put("t1", "1800-09-09");
        query.put("t2", "2005-09-09");
        IngridHits hits = plug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        for (int i = 0; i < hitsArray.length; i++) {
            Topic hit = (Topic) hitsArray[i];
            System.out.println(hit.getTopicName() + ":" + hit.getTopicID());
        }
    }
    
    /**
     * @throws Exception
     */
    public void testEVENT_FROM() throws Exception {
        SnsPlug plug = new SnsPlug(fPlugDescription);
        String q = "ohio";
        IngridQuery query = QueryStringParser.parse(q);
        query.setDataType(IDataTypes.SNS);
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
        query.put("eventtype", "industrialAccident");
        query.put("t1", "1800-09-09");
        IngridHits hits = plug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        for (int i = 0; i < hitsArray.length; i++) {
            Topic hit = (Topic) hitsArray[i];
            System.out.println(hit.getTopicName() + ":" + hit.getTopicID());
        }
    }
    
    /**
     * @throws Exception
     */
    public void testEVENT_TO() throws Exception {
        SnsPlug plug = new SnsPlug(fPlugDescription);
        String q = "ohio";
        IngridQuery query = QueryStringParser.parse(q);
        query.setDataType(IDataTypes.SNS);
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
        query.put("eventtype", "industrialAccident");
        query.put("t2", "2006-01-01");
        IngridHits hits = plug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        for (int i = 0; i < hitsArray.length; i++) {
            Topic hit = (Topic) hitsArray[i];
            System.out.println(hit.getTopicName() + ":" + hit.getTopicID());
        }
    }
    
    /**
     * @throws Exception
     */
    public void testEVENT_NODATESET() throws Exception {
        SnsPlug plug = new SnsPlug(fPlugDescription);
        String q = "ohio";
        IngridQuery query = QueryStringParser.parse(q);
        query.setDataType(IDataTypes.SNS);
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
        query.put("eventtype", "industrialAccident");
        IngridHits hits = plug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        for (int i = 0; i < hitsArray.length; i++) {
            Topic hit = (Topic) hitsArray[i];
            System.out.println(hit.getTopicName() + ":" + hit.getTopicID());
        }
    }
}
