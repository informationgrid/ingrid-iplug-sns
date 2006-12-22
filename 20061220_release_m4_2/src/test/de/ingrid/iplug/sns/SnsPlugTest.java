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
        fPlugDescription.setProxyServiceURL("aPlugId");
        fPlugDescription.put("username", "ms");
        fPlugDescription.put("password", "m3d1asyl3");
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
            assertFalse(hit.getTopicAssoc().trim().equals(""));

            System.out.println(hit.getTopicName() + ":" + hit.getTopicID() + ":" + hit.getTopicAssoc());
        }
        System.out.println("##########");
    }

    /**
     * @throws Exception
     */
    public void testTOPIC_FROM_TEXT() throws Exception {
        SnsPlug plug = new SnsPlug(fPlugDescription);
        String q = "\"Tschernobyl liegt in Halle gefunden\"";
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
    public void testTOPIC_FROM_TEXT_WITH_FILTER() throws Exception {
        SnsPlug plug = new SnsPlug(fPlugDescription);
        String q = "Frankfurt";
        IngridQuery query = new IngridQuery();
        query.addTerm(new TermQuery(true, false, q));
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.TOPIC_FROM_TEXT);
        query.put("filter", "/location");
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
            System.out.println(hit.getTopicName() + ":" + hit.getTopicID() + ":" + hit.getTopicAssoc());
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
            IngridHitDetail detail = plug.getDetail(hit, query, this.fields);
            assertNotNull(detail);
            assertNotNull(detail.get(DetailedTopic.DESCRIPTION_OCC));
            // assertNotNull(detail.get(DetailedTopic.SAMPLE_OCC));
            assertNotNull(detail.get(DetailedTopic.ASSOCIATED_OCC));
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
        String q = "tschernobyl";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
        query.put("eventtype", new String[] { "industrialAccident" });
        query.put("t0", "1999-03-08");
        IngridHits hits = plug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        for (int i = 0; i < hitsArray.length; i++) {
            Topic hit = (Topic) hitsArray[i];
            System.out.println(hit.getTopicName() + ":" + hit.getTopicID());
            plug.getDetail(hit, query, this.fields);
        }
    }

    /**
     * @throws Exception
     */
    public void testEVENT_WITHOUT_TERM() throws Exception {
        SnsPlug plug = new SnsPlug(fPlugDescription);
        String q = "";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
        query.put("eventtype", new String[] { "industrialAccident" });
        query.put("t0", "1999-03-08");
        IngridHits hits = plug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        for (int i = 0; i < hitsArray.length; i++) {
            Topic hit = (Topic) hitsArray[i];
            System.out.println(hit.getTopicName() + ":" + hit.getTopicID());
            plug.getDetail(hit, query, this.fields);
        }
    }

    /**
     * @throws Exception
     */
    public void testEVENT_WITHOUT_TERM_KATASTROPHE() throws Exception {
        SnsPlug plug = new SnsPlug(fPlugDescription);
        String q = "";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
        query.put("eventtype", new String[] { "disaster" });
        query.put("t2", "3010-03-08");
        IngridHits hits = plug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        for (int i = 0; i < hitsArray.length; i++) {
            Topic hit = (Topic) hitsArray[i];
            System.out.println(hit.getTopicName() + ":" + hit.getTopicID());
            plug.getDetail(hit, query, this.fields);
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
        query.put("eventtype", new String[] { "marineAccident" });
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
        query.put("eventtype", new String[] { "industrialAccident" });
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
        query.put("eventtype", new String[] { "industrialAccident" });
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
     * Tests that the result is in german.
     * 
     * @throws Exception
     */
    public void testEVENT_FROM_BUNDESWALDGESETZ() throws Exception {
        SnsPlug plug = new SnsPlug(fPlugDescription);
        String q = "Bundeswaldgesetz";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
        query.put("t2", "3000-09-09");
        IngridHits hits = plug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        for (int i = 0; i < hitsArray.length; i++) {
            Topic hit = (Topic) hitsArray[i];
            System.out.println(hit.getTopicName() + ":" + hit.getTopicID());

            IngridHitDetail ihd = plug.getDetail(hit, query, null);
            System.out.println(ihd.get(DetailedTopic.DESCRIPTION_OCC));
            System.out.println(ihd.get(DetailedTopic.TO));
            System.out.println(ihd.get(DetailedTopic.FROM));
            System.out.println(ihd.get(DetailedTopic.ASSOCIATED_OCC));
            System.out.println(ihd.get(DetailedTopic.SAMPLE_OCC));
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
        query.put("eventtype", new String[] { "industrialAccident" });
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
        query.put("eventtype", new String[] { "industrialAccident" });
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
    public void testSIMILARLOCATIONS_FROM_TOPIC() throws Exception {
        final String q = "GEMEINDE0325502016";

        SnsPlug plug = new SnsPlug(fPlugDescription);
        IngridQuery query = new IngridQuery();
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.SIMILARLOCATIONS_FROM_TOPIC);
        query.addTerm(new TermQuery(true, false, q));

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
    public void testDateInFuture() throws Exception {
        final String q = "";

        SnsPlug plug = new SnsPlug(fPlugDescription);
        IngridQuery query = QueryStringParser.parse(q);
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
        String[] eventTypes = new String[] { "industrialAccident" };
        query.put("eventtype", eventTypes);
        query.put("t2", "3000-01-01");
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));

        IngridHits hits = plug.search(query, 0, 10);
        System.out.println(hits.length());
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
    public void testMORE_THAN_TEN_RESULTS() throws Exception {
        SnsPlug plug = new SnsPlug(fPlugDescription);
        String q = "";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
        query.put("t2", "3000-01-01");
        IngridHits hits = plug.search(query, 0, 57);
        assertEquals(601, hits.length());
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);

        for (int i = 0; i < hitsArray.length; i++) {
            Topic hit = (Topic) hitsArray[i];
            System.out.println(hit.getTopicID());
        }
        assertEquals(57, hitsArray.length);
    }

    /**
     * @throws Exception
     */
    public void testSIMILARTERMSINENGLISH() throws Exception {
        SnsPlug plug = new SnsPlug(fPlugDescription);
        String q = "water";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.addField(new FieldQuery(true, false, "lang", "en"));
        query.putInt(Topic.REQUEST_TYPE, Topic.SIMILARTERMS_FROM_TOPIC);
        IngridHits hits = plug.search(query, 0, 600);
        assertEquals(145, hits.length());
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        assertEquals(145, hitsArray.length);
        for (int i = 0; i < hitsArray.length; i++) {
            Topic hit = (Topic) hitsArray[i];
            System.out.println(hit.getTopicName() + " -- " + hit.getTopicID() + " -- " + hit.getSummary());
        }
    }
}
