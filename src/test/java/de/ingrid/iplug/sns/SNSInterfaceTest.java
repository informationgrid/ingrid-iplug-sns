/*
 * **************************************************-
 * Ingrid iPlug SNS
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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
import junit.framework.TestCase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 */
public class SNSInterfaceTest extends TestCase {

    private static PlugDescription fPlugDescription;

    static {
        fPlugDescription = new PlugDescription();
//        SnsPlug.conf = new Configuration();
//        SnsPlug.conf.snsLanguage = "de";
//        SnsPlug.conf.snsPrefix = "agsNotation";
//        SnsPlug.conf.snsUrlThesaurus = ResourceBundle.getBundle("sns").getString("sns.serviceURL.thesaurus");
//        SnsPlug.conf.snsUrlGazetteer = ResourceBundle.getBundle("sns").getString("sns.serviceURL.gazetteer");
//        SnsPlug.conf.snsUrlChronicle = ResourceBundle.getBundle("sns").getString("sns.serviceURL.chronicle");
    }

    private SnsPlug fPlug;

    static int HITS_PER_PAGE = 10;

    static int CURRENT_PAGE = 1;

    protected void setUp() throws Exception {
        super.setUp();

//        new JettyStarter(false);
//        JettyStarter.baseConfig = new Config();
//        JettyStarter.baseConfig.communicationProxyUrl = "ibus-client-test";
        Configuration configuration = new Configuration();

        this.fPlug = new SnsPlug(new DefaultMetadataInjector[0], null, configuration, null, null);
        this.fPlug.configure(fPlugDescription);
    }

    /**
     * @throws Exception
     */
    public void testGetAnniversaries() throws Exception {
        System.out.println("########## testGetAnniversaries()");
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        //            cal.set(Calendar.DATE, 15);
        //            cal.set(Calendar.MONTH, 2);
        cal.add(Calendar.DATE, 1);
        Date queryDate = cal.getTime();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = df.format(queryDate);

        IngridQuery query = QueryStringParser.parse(dateStr);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.ANNIVERSARY_FROM_TOPIC);


        IngridHits hits = this.fPlug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        if (hitsArray.length == 0) {
            System.out.println("!!!!!!!!!!!!!!!! NO ANNIVERSARY HIT FOUND");
        } else {
            for (int i = 0; i < hitsArray.length; i++) {
                Topic hit = (Topic) hitsArray[i];
                final String output = hit.getTopicName() + ":" + hit.getTopicID();
                System.out.println(output);
            }
        }
        System.out.println("--- try to fetch details:");
        IngridHitDetail[] details = this.fPlug.getDetails(hitsArray, query, new String[0]);
        assertNotNull(details);
        if (details.length > 0) {
            for (int i = 0; i < details.length; i++) {
                assertTrue(details[i] instanceof DetailedTopic);
                DetailedTopic detail = (DetailedTopic) details[i];
                final String output = detail.getTopicName() + ':' + detail.getTopicID() + ':' + detail.getFrom() + ':'
                        + detail.getTo() + ':' + detail.getAdministrativeID();
                System.out.println(output);
            }
        } else {
            System.out.println("!!!!!!!!!!!!!!!! NO ANNIVERSARY DETAILS FOUND");
        }
    }

    /**
     * @throws Exception
     */
    public void testEVENTS_FROM_TERM() throws Exception {
        System.out.println("########## testEVENTS_FROM_TERM()");
        String term = "Reaktorunglück Tschernobyl";
        IngridQuery query = new IngridQuery();
        query.addTerm(new TermQuery(true, false, term));
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);

        IngridHits hits = this.fPlug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        if (hitsArray.length == 0) {
            System.out.println("!!!!!!!!!!!!!!!! NO EVENT WITH TERM FOUND");
        } else {
            for (int i = 0; i < hitsArray.length; i++) {
                Topic hit = (Topic) hitsArray[i];
                final String output = hit.getTopicName() + ':' + hit.getTopicID();
                System.out.println(output);
            }
        }
    }

    /**
     * @throws Exception
     */
    public void testEVENTS_FROM_TYPE() throws Exception {
        System.out.println("########## testEVENTS_FROM_TYPE()");
        String term = "";
        String eventType = "industrialAccident";
        System.out.println("TYPE = " + eventType);
        IngridQuery query = QueryStringParser.parse(term);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
        query.put("eventtype", new String[]{eventType});
        // Set a date in the future to get all events.
        query.put("t2", "6001-01-01");


        IngridHits hits = this.fPlug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        if (hitsArray.length == 0) {
            System.out.println("!!!!!!!!!!!!!!!! NO EVENT OF TYPE FOUND");
        } else {
            for (int i = 0; i < hitsArray.length; i++) {
                Topic hit = (Topic) hitsArray[i];
                System.out.println(hit.getTopicName() + ':' + hit.getTopicID());
            }
        }
    }

    /**
     * @throws Exception
     */
    public void testEVENTS_FROM_TYPE_AND_TERM() throws Exception {
        System.out.println("########## testEVENTS_FROM_TYPE_AND_TERM()");
        String term = "Tschernobyl";
        String eventType = "industrialAccident";
        System.out.println("TERM = " + term);
        System.out.println("TYPE = " + eventType);
        IngridQuery query = QueryStringParser.parse(term);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
        query.put("eventtype", new String[]{eventType});

        IngridHits hits = this.fPlug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        if (hitsArray.length == 0) {
            System.out.println("!!!!!!!!!!!!!!!! NO EVENT OF TYPE AND TERM FOUND");
        } else {
            for (int i = 0; i < hitsArray.length; i++) {
                Topic hit = (Topic) hitsArray[i];
                System.out.println(hit.getTopicName() + ':' + hit.getTopicID());
            }
        }
    }

    /**
     * @throws Exception
     */
    public void testEVENT_DETAILS() throws Exception {
        System.out.println("########## testEVENT_DETAILS()");
        String term = "Tschernobyl";
//        String eventType = "industrialAccident";
        System.out.println("TERM = ".concat(term));
//          System.out.println("TYPE = " + eventType);
        IngridQuery query = QueryStringParser.parse(term);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);
//        query.put("eventtype", eventType);


        IngridHits hits = this.fPlug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        if (hitsArray.length == 0) {
            System.out.println("!!!!!!!!!!!!!!!! NO EVENT OF TYPE AND TERM FOUND");
        } else {
            Topic hit = (Topic) hitsArray[0];
            System.out.println((hit.getTopicName() + ':').concat(hit.getTopicID()));

        }
        IngridHitDetail[] details = this.fPlug.getDetails(hitsArray, query, new String[0]);
        assertNotNull(details);
        if (details.length > 0) {
            for (int i = 0; i < details.length; i++) {
                assertTrue(details[i] instanceof DetailedTopic);
                DetailedTopic detail = (DetailedTopic) details[i];
                System.out.println(detail.getTopicName() + ':' + detail.getTopicID() + ':' + detail.getFrom() + ':'
                        + detail.getTo() + ':' + detail.getType() + ':' + detail.getAdministrativeID());
            }
        } else {
            System.out.println("!!!!!!!!!!!!!!!! NO EVENT DETAILS FOUND");
        }
    }
}
