/*
 * **************************************************-
 * Ingrid iPlug SNS
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
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

package de.ingrid.iplug.sns.gssoil;

import de.ingrid.iplug.sns.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import de.ingrid.iplug.sns.SnsPlug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
public class GsSoilSnsPlugTestLocal {
    private static PlugDescription fPlugDescription;

    static {
        fPlugDescription = new PlugDescription();
        fPlugDescription.setProxyServiceURL("aPlugId");
        fPlugDescription.put("username", "ms");
        fPlugDescription.put("password", "m3d1asyl3");
        fPlugDescription.put("language", "de");
        fPlugDescription.putInt("maxWordAnalyzing", 100);
    }

    @BeforeEach
    public void setUp() throws Exception {
        configuration = new Configuration();
    }

    private Configuration configuration;

    @Test
    public void testTOPIC_FROM_ID() throws Exception {
    	SnsPlug plug = new SnsPlug(null, null, configuration, null, null);
        plug.configure(fPlugDescription);
        
		String marshalledTopicId = SNSUtil.marshallTopicId("http://www.eionet.europa.eu/gemet/supergroup/5499");

        IngridQuery query = QueryStringParser.parse(marshalledTopicId);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.addField(new FieldQuery(true, false, "lang", "en"));
        query.put("filter", "/thesa");
        query.putInt(Topic.REQUEST_TYPE, Topic.TOPIC_FROM_ID);

        IngridHits hits = plug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        assertEquals(hitsArray.length, 1);
        for (int i = 0; i < hitsArray.length; i++) {
            Topic hit = (Topic) hitsArray[i];
            System.out.println(hit.getTopicName() + ":" + hit.getTopicID() + ":" + hit.getTopicAssoc());
        }
    }

    @Test
    public void testTOPIC_FROM_TEXT() throws Exception {
    	SnsPlug plug = new SnsPlug(null, null, configuration, null, null);
        plug.configure(fPlugDescription);
        
        String term = "Lissabon";

    	// enclose term in '"' if the term has a space, otherwise no results will be returned from SNS
    	if (term.indexOf(" ") != -1 && !term.startsWith("\"") && !term.endsWith("\"")) {
    		term = "\"".concat(term).concat("\"");
    	}
    	IngridQuery query = QueryStringParser.parse(term);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.addField(new FieldQuery(true, false, "lang", "de"));
        query.put("filter", "/location");
        query.putInt(Topic.REQUEST_TYPE, Topic.TOPIC_FROM_TEXT);

        IngridHits hits = plug.search(query, 0, 10);
        IngridHit[] hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        assertTrue(hitsArray.length > 0);

        // now lower case ! SET CASE SENSITIVITY IN sns.properties !
        term = "lissabon";

    	query = QueryStringParser.parse(term);
        query.addField(new FieldQuery(true, false, "datatype", IDataTypes.SNS));
        query.addField(new FieldQuery(true, false, "lang", "de"));
        query.put("filter", "/location");
        query.putInt(Topic.REQUEST_TYPE, Topic.TOPIC_FROM_TEXT);

        hits = plug.search(query, 0, 10);
        hitsArray = hits.getHits();
        assertNotNull(hitsArray);
        // if gazetteerService.getLocationsFromText.ignoreCase=false in external-services.xml
//        assertTrue(hitsArray.length == 0);
        // if gazetteerService.getLocationsFromText.ignoreCase=true in external-services.xml
        assertTrue(hitsArray.length > 0);
    }

}
