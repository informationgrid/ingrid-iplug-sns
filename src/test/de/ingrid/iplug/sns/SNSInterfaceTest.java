package de.ingrid.iplug.sns;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;
import de.ingrid.iplug.sns.utils.DetailedTopic;
import de.ingrid.iplug.sns.utils.Topic;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.IDataTypes;
import de.ingrid.utils.queryparser.QueryStringParser;

/**
 * 
 */
public class SNSInterfaceTest extends TestCase {

    private static PlugDescription fPlugDescription;

    static {
        fPlugDescription = new PlugDescription();
        fPlugDescription.setPlugId("aPlugId");
        fPlugDescription.put("username", "ms");
        fPlugDescription.put("password", "m3d1asyl3");
        fPlugDescription.put("language", "de");
        fPlugDescription.putInt("maxWordForAnalyzing", 100);
    }
    private SnsPlug fPlug;
    
    static int HITS_PER_PAGE = 10;

    static int CURRENT_PAGE = 1;

    protected void setUp() throws Exception {
        super.setUp();
        this.fPlug = new SnsPlug(fPlugDescription);
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
                System.out.println(hit.getTopicName() + ":" + hit.getTopicID());
            }
        }
        System.out.println("--- try to fetch details:");
        IngridHitDetail[] details = this.fPlug.getDetails(hitsArray, query, new String[0]);
        assertNotNull(details);
        if (details.length > 0) {
            for (int i = 0; i < details.length; i++) {
                assertTrue(details[i] instanceof DetailedTopic);
                DetailedTopic detail = (DetailedTopic) details[i];
                System.out.println(detail.getTopicName() + ":" + detail.getTopicID() + ":" + detail.getFrom() + ":"
                        + detail.getTo() + ":" + detail.getAdministrativeID());
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
        String term = "Tschernobyl";
        System.out.println("TERM = " + term);
        IngridQuery query = QueryStringParser.parse(term);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.putInt(Topic.REQUEST_TYPE, Topic.EVENT_FROM_TOPIC);

        IngridHits hits = this.fPlug.search(query, 0,10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        if (hitsArray.length == 0) {
            System.out.println("!!!!!!!!!!!!!!!! NO EVENT WITH TERM FOUND");
        } else {
            for (int i = 0; i < hitsArray.length; i++) {
                Topic hit = (Topic) hitsArray[i];
                System.out.println(hit.getTopicName() + ":" + hit.getTopicID());
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
        query.put("eventtype",new String[]{ eventType});

        
        IngridHits hits = this.fPlug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        if (hitsArray.length == 0) {
            System.out.println("!!!!!!!!!!!!!!!! NO EVENT OF TYPE FOUND");
        } else {
            for (int i = 0; i < hitsArray.length; i++) {
                Topic hit = (Topic) hitsArray[i];
                System.out.println(hit.getTopicName() + ":" + hit.getTopicID());
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
        query.put("eventtype", new String[] {eventType});

        IngridHits hits = this.fPlug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        if (hitsArray.length == 0) {
            System.out.println("!!!!!!!!!!!!!!!! NO EVENT OF TYPE AND TERM FOUND");
        } else {
            for (int i = 0; i < hitsArray.length; i++) {
                Topic hit = (Topic) hitsArray[i];
                System.out.println(hit.getTopicName() + ":" + hit.getTopicID());
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
        System.out.println("TERM = " + term);
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
            System.out.println(hit.getTopicName() + ":" + hit.getTopicID());

        }
        IngridHitDetail[] details = this.fPlug.getDetails(hitsArray, query, new String[0]);
        assertNotNull(details);
        if (details.length > 0) {
            for (int i = 0; i < details.length; i++) {
                assertTrue(details[i] instanceof DetailedTopic);
                DetailedTopic detail = (DetailedTopic) details[i];
                System.out.println(detail.getTopicName() + ":" + detail.getTopicID() + ":" + detail.getFrom() + ":"
                        + detail.getTo() + ":" + detail.getType() + ":" + detail.getAdministrativeID());
            }
        } else {
            System.out.println("!!!!!!!!!!!!!!!! NO EVENT DETAILS FOUND");
        }
    }
}
