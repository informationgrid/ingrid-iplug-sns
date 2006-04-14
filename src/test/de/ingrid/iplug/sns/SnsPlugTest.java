/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.sns;

import junit.framework.TestCase;
import de.ingrid.iplug.sns.utils.DetailedTopic;
import de.ingrid.iplug.sns.utils.Topic;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.query.FieldQuery;
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

    private String[] fields;

    /**
     * @throws Exception
     */
    public void testTOPIC_FROM_TERM() throws Exception {
        SnsPlug plug = new SnsPlug(fPlugDescription);
        String q = "Wasser";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
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
        query.addTerm(new TermQuery(true, false, q));
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.TOPIC_FROM_TEXT);
        IngridHits hits = plug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        for (int i = 0; i < hitsArray.length; i++) {
            DetailedTopic hit = (DetailedTopic) hitsArray[i];
            System.out.println(hit.getTopicName() + ":" + hit.getAdministrativeID() + ":" + hit.getTopicID());
        }
    }

    /**
     * @throws Exception
     */
    public void testTOPIC_FROM_TOPIC() throws Exception {
        SnsPlug plug = new SnsPlug(fPlugDescription);
        String q = "uba_thes_500855";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
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
        String q = "2006-04-06";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.ANNIVERSARY_FROM_TOPIC);
        IngridHits hits = plug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        for (int i = 0; i < hitsArray.length; i++) {
            Topic hit = (Topic) hitsArray[i];
            System.out.println(hit.getTopicName() + ":" + hit.getTopicID());
            IngridHitDetail detail = plug.getDetail(hit, query, fields);
            assertNotNull(detail);
            assertNotNull( detail.get(DetailedTopic.DESCRIPTION_OCC));
//            assertNotNull(detail.get(DetailedTopic.SAMPLE_OCC));
            assertNotNull(detail.get(DetailedTopic.ASSICIATED_OCC));
        }
        
    }

    /**
     * @throws Exception
     */
    public void testSIMILARTERMS() throws Exception {
        SnsPlug plug = new SnsPlug(fPlugDescription);
        String q = "blau";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
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
        String q = "Tschernobyl";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
        query.put("eventtype", "industrialAccident");
        query.put("t0", "1999-03-08");
        IngridHits hits = plug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        for (int i = 0; i < hitsArray.length; i++) {
            Topic hit = (Topic) hitsArray[i];
            System.out.println(hit.getTopicName() + ":" + hit.getTopicID());
            plug.getDetail(hit, query, fields);
        }
    }

    /**
     * @throws Exception
     */
    public void testEVENT_AT_FILTER_NOOUTPUT() throws Exception {
        SnsPlug plug = new SnsPlug(fPlugDescription);
        String q = "Tschernobyl";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
        query.put("eventtype", "marineAccident");
        query.put("t0", "1999-03-08");
        IngridHits hits = plug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        assertEquals(0, hitsArray.length);
    }

    /**
     * @throws Exception
     */
    public void testEVENT_BETWEEN() throws Exception {
        SnsPlug plug = new SnsPlug(fPlugDescription);
        String q = "Tschernobyl";
        IngridQuery query = QueryStringParser.parse(q); 
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
        query.put("eventtype", "industrialAccident");
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
        String q = "Tschernobyl";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
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
        String q = "Tschernobyl";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
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
        String q = "tschernobyl";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
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
