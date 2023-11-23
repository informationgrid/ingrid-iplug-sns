/*
 * **************************************************-
 * Ingrid iPlug SNS
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
/*
 * Copyright (c) 1997-2005 by media style GmbH
 *
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.sns;

import de.ingrid.admin.Config;
import de.ingrid.iplug.sns.utils.DetailedTopic;
import de.ingrid.iplug.sns.utils.Topic;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.metadata.DefaultMetadataInjector;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.TermQuery;
import de.ingrid.utils.queryparser.IDataTypes;
import de.ingrid.utils.queryparser.QueryStringParser;
import de.ingrid.utils.tool.SNSUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
public class SnsPlugTest {
    private static PlugDescription fPlugDescription;

    static {
        fPlugDescription = new PlugDescription();
        fPlugDescription.setProxyServiceURL("aPlugId");

//        SnsPlug.conf = new Configuration();
//        SnsPlug.conf.snsLanguage = "de";
//        SnsPlug.conf.snsPrefix = "agsNotation";
//        SnsPlug.conf.snsUrlThesaurus = ResourceBundle.getBundle("sns").getString("sns.serviceURL.thesaurus");
//        SnsPlug.conf.snsUrlGazetteer = ResourceBundle.getBundle("sns").getString("sns.serviceURL.gazetteer");
//        SnsPlug.conf.snsUrlChronicle = ResourceBundle.getBundle("sns").getString("sns.serviceURL.chronicle");
    }

    private Configuration configuration;
    private Config baseConfig;

    @BeforeEach
    public void setUp() throws Exception {

//        new JettyStarter(false);
//        JettyStarter.baseConfig = new Config();
//        JettyStarter.baseConfig.communicationProxyUrl = "ibus-client-test";

        configuration = new Configuration();
        configuration.snsLanguage = "de";
        baseConfig = new Config();
        baseConfig.plugdescriptionLocation = "";
    }

    private String[] fields;

    /**
     * @throws Exception
     */
    @Test
    public void testTOPIC_FROM_TERM() throws Exception {
        SnsPlug plug = new SnsPlug(new DefaultMetadataInjector[0], baseConfig,  configuration, null, null);
        plug.configure(fPlugDescription);
        String q = "Wasser";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.TOPIC_FROM_TERM);
        IngridHits hits = plug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        for (int i = 0; i < hitsArray.length; i++) {
            Topic hit = (Topic) hitsArray[i];
            assertNotEquals(hit.getTopicAssoc().trim(), "");

            System.out.println(hit.getTopicName() + ":" + hit.getTopicID() + ":" + hit.getTopicAssoc());
        }
        System.out.println("##########");
    }

    /**
     * @throws Exception
     */
    @Test
    public void testTOPIC_FROM_TEXT() throws Exception {
        SnsPlug plug = new SnsPlug(new DefaultMetadataInjector[0], baseConfig, configuration, null, null);
        plug.configure(fPlugDescription);
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
    @Test
    public void testTOPIC_FROM_TEXT_WITH_FILTER() throws Exception {
        SnsPlug plug = new SnsPlug(new DefaultMetadataInjector[0], baseConfig, configuration, null, null);
        plug.configure(fPlugDescription);
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
    @Test
    public void testTOPIC_FROM_TOPIC() throws Exception {
        SnsPlug plug = new SnsPlug(new DefaultMetadataInjector[0], baseConfig, configuration, null, null);
        plug.configure(fPlugDescription);
        String q = "http://umthes.innoq.com/_00500855";
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

    @Test
    public void testTOPIC_FROM_ID() throws Exception {
        SnsPlug plug = new SnsPlug(new DefaultMetadataInjector[0], baseConfig, configuration, null, null);
        plug.configure(fPlugDescription);
        String q = SNSUtil.marshallTopicId("https://sns.uba.de/umthes/_00027061");
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.addField(new FieldQuery(true, false, "lang", "de"));
        query.putInt(Topic.REQUEST_TYPE, Topic.TOPIC_FROM_ID);
        query.put("filter", "/thesa");
        IngridHits hits = plug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        assertEquals(hitsArray.length, 1);
        for (int i = 0; i < hitsArray.length; i++) {
            Topic hit = (Topic) hitsArray[i];
            System.out.println(hit.getTopicName() + ":" + hit.getTopicID() + ":" + hit.getTopicAssoc());
        }
    }

    /**
     * @throws Exception
     */
    @Test
    public void testANNIVERSARY() throws Exception {
        SnsPlug plug = new SnsPlug(new DefaultMetadataInjector[0], baseConfig, configuration, null, null);
        plug.configure(fPlugDescription);
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
            //TODO: assertNotNull(detail.get(DetailedTopic.ASSOCIATED_OCC));
        }

    }

    /**
     * @throws Exception
     */
    @Test
    public void testSIMILARTERMS() throws Exception {
        SnsPlug plug = new SnsPlug(new DefaultMetadataInjector[0], baseConfig, configuration, null, null);
        plug.configure(fPlugDescription);
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
    @Test
    public void testEVENT_AT() throws Exception {
        SnsPlug plug = new SnsPlug(new DefaultMetadataInjector[0], baseConfig, configuration, null, null);
        plug.configure(fPlugDescription);
        String q = "tschernobyl";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
        query.put("eventtype", new String[]{"industrialAccident"});
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
    @Test
    public void testEVENT_WITHOUT_TERM() throws Exception {
        SnsPlug plug = new SnsPlug(new DefaultMetadataInjector[0], baseConfig, configuration, null, null);
        plug.configure(fPlugDescription);
        String q = "";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
        query.put("eventtype", new String[]{"industrialAccident"});
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
    @Test
    public void testEVENT_WITHOUT_TERM_KATASTROPHE() throws Exception {
        SnsPlug plug = new SnsPlug(new DefaultMetadataInjector[0], baseConfig, configuration, null, null);
        plug.configure(fPlugDescription);
        String q = "";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
        query.put("eventtype", new String[]{"disaster"});
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
    @Test
    public void testEVENT_AT_FILTER_NOOUTPUT() throws Exception {
        SnsPlug plug = new SnsPlug(new DefaultMetadataInjector[0], baseConfig, configuration, null, null);
        plug.configure(fPlugDescription);
        String q = "Tschernobyl";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
        query.put("eventtype", new String[]{"marineAccident"});
        query.put("t0", "1999-03-08");
        IngridHits hits = plug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        assertEquals(0, hitsArray.length);
    }

    /**
     * @throws Exception
     */
    @Test
    public void testEVENT_BETWEEN() throws Exception {
        SnsPlug plug = new SnsPlug(new DefaultMetadataInjector[0], baseConfig, configuration, null, null);
        plug.configure(fPlugDescription);
        String q = "Tschernobyl";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
        query.put("eventtype", new String[]{"industrialAccident"});
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
    @Test
    public void testEVENT_FROM() throws Exception {
        SnsPlug plug = new SnsPlug(new DefaultMetadataInjector[0], baseConfig, configuration, null, null);
        plug.configure(fPlugDescription);
        String q = "Tschernobyl";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
        query.put("eventtype", new String[]{"industrialAccident"});
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
    @Test
    public void testEVENT_FROM_BUNDESWALDGESETZ() throws Exception {
        SnsPlug plug = new SnsPlug(new DefaultMetadataInjector[0], baseConfig, configuration, null, null);
        plug.configure(fPlugDescription);
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
    @Test
    public void testEVENT_TO() throws Exception {
        SnsPlug plug = new SnsPlug(new DefaultMetadataInjector[0], baseConfig, configuration, null, null);
        plug.configure(fPlugDescription);
        String q = "Tschernobyl";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
        query.put("eventtype", new String[]{"industrialAccident"});
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
    @Test
    public void testEVENT_NODATESET() throws Exception {
        SnsPlug plug = new SnsPlug(new DefaultMetadataInjector[0], baseConfig, configuration, null, null);
        plug.configure(fPlugDescription);
        String q = "tschernobyl";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
        query.put("eventtype", new String[]{"industrialAccident"});
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
    @Test
    public void testSIMILARLOCATIONS_FROM_TOPIC() throws Exception {
        final String q = "GEMEINDE0325502016";

        SnsPlug plug = new SnsPlug(new DefaultMetadataInjector[0], baseConfig, configuration, null, null);
        plug.configure(fPlugDescription);
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
    @Test
    public void testDateInFuture() throws Exception {
        final String q = "";

        SnsPlug plug = new SnsPlug(new DefaultMetadataInjector[0], baseConfig, configuration, null, null);
        plug.configure(fPlugDescription);
        IngridQuery query = QueryStringParser.parse(q);
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
        String[] eventTypes = new String[]{"industrialAccident"};
        query.put("eventtype", eventTypes);
        query.put("t2", "3000-01-01");
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
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
    @Test
    @Disabled("Error in external service!?")
    public void testMORE_THAN_TEN_RESULTS() throws Exception {
        SnsPlug plug = new SnsPlug(new DefaultMetadataInjector[0], baseConfig, configuration, null, null);
        plug.configure(fPlugDescription);
        String q = "";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
        query.put("t2", "3000-01-01");
        IngridHits hits = plug.search(query, 0, 57);
        assertTrue(hits.length() > 10, "Hits should be more than 10 but was: " + hits.length());
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
    @Test
    public void testSIMILARTERMSINENGLISH() throws Exception {
        SnsPlug plug = new SnsPlug(new DefaultMetadataInjector[0], baseConfig, configuration, null, null);
        plug.configure(fPlugDescription);
        String q = "water";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.addField(new FieldQuery(true, false, "lang", "en"));
        query.putInt(Topic.REQUEST_TYPE, Topic.SIMILARTERMS_FROM_TOPIC);
        IngridHits hits = plug.search(query, 0, 600);
        // TODO: wait for english
        /*assertEquals(46, hits.length());
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        assertEquals(46, hitsArray.length);
        for (int i = 0; i < hitsArray.length; i++) {
            Topic hit = (Topic) hitsArray[i];
            System.out.println(hit.getTopicName() + " -- " + hit.getTopicID() + " -- " + hit.getSummary());
        }*/
    }

    /**
     * @throws Exception
     */
    @Test
    public void testGETHIERACHY() throws Exception {
        SnsPlug plug = new SnsPlug(new DefaultMetadataInjector[0], baseConfig, configuration, null, null);
        plug.configure(fPlugDescription);
        String q = "toplevel";
        IngridQuery query = QueryStringParser.parse(q);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.addField(new FieldQuery(true, false, "lang", "de"));
        query.put("includeSiblings", "false");
        query.put("association", "narrowerTermAssoc");
        query.put("depth", "1");
        query.put("direction", "down");
        query.putInt(Topic.REQUEST_TYPE, Topic.TOPIC_HIERACHY);
        IngridHits hits = plug.search(query, 0, 600);
        assertEquals(1, hits.length());
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        assertEquals(1, hitsArray.length);
        for (int i = 0; i < hitsArray.length; i++) {
            Topic hit = (Topic) hitsArray[i];
            System.out.println(" " + hit.getTopicID() + " : "
                    + hit.getLanguage());
            final Set<Topic> successors = hit.getSuccessors();
            for (Topic topic : successors) {
                System.out.println(topic.getTopicID() + ":"
                        + topic.getLanguage());
                final Set<Topic> successors2 = topic
                        .getSuccessors();
                for (Topic topic2 : successors2) {
                    System.out.println(topic2.getTopicID() + ":"
                            + topic2.getLanguage());
                }
            }
        }
    }
}
