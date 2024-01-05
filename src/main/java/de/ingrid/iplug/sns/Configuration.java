/*
 * **************************************************-
 * Ingrid iPlug XML
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
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
package de.ingrid.iplug.sns;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.admin.IConfig;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.utils.PlugDescription;
import org.springframework.beans.factory.annotation.Value;

@org.springframework.context.annotation.Configuration
public class Configuration implements IConfig {
    
    @SuppressWarnings("unused")
    private static Log log = LogFactory.getLog(Configuration.class);
    
    @Value("${sns.username:}")
    public String snsUsername;
    
    @Value("${sns.password:}")
    public String snsPassword;
    
    @Value("${sns.language:de}")
    public String snsLanguage;
    
    
    @Value("${sns.serviceURL.chronicle:}")
    public String snsUrlChronicle;
    
    @Value("${sns.serviceURL.thesaurus:}")
    public String snsUrlThesaurus;
    
    @Value("${sns.serviceURL.gazetteer:}")
    public String snsUrlGazetteer;
    
    @Value("${sns.nativeKeyPrefix:}")
    public String snsPrefix;

    
    @Override
    public void initialize() {}
    
    @Override
    public void addPlugdescriptionValues( PlugdescriptionCommandObject pdObject ) {
        pdObject.put( "iPlugClass", "de.ingrid.iplug.sns.SnsPlug");
        
        pdObject.removeFromList(PlugDescription.FIELDS, "lang");
        pdObject.removeFromList(PlugDescription.FIELDS, "t0");
        pdObject.removeFromList(PlugDescription.FIELDS, "t1");
        pdObject.removeFromList(PlugDescription.FIELDS, "t2");
        pdObject.removeFromList(PlugDescription.FIELDS, "eventtype");
        pdObject.removeFromList(PlugDescription.FIELDS, "filter");
        pdObject.removeFromList(PlugDescription.FIELDS, "sns_request_type");
        pdObject.removeFromList(PlugDescription.FIELDS, "expired");
        pdObject.removeFromList(PlugDescription.FIELDS, "association");
        pdObject.removeFromList(PlugDescription.FIELDS, "depth");
        pdObject.removeFromList(PlugDescription.FIELDS, "direction");
        pdObject.removeFromList(PlugDescription.FIELDS, "includeSiblings");
        pdObject.addField("lang");
    	pdObject.addField("t0");
    	pdObject.addField("t1");
    	pdObject.addField("t2");
    	pdObject.addField("eventtype");
    	pdObject.addField("filter");
    	pdObject.addField("sns_request_type");
    	pdObject.addField("expired");
    	pdObject.addField("association");
    	pdObject.addField("depth");
    	pdObject.addField("direction");
    	pdObject.addField("includeSiblings");
    	pdObject.addField("isfolder");

    }

    @Override
    public void setPropertiesFromPlugdescription( Properties props, PlugdescriptionCommandObject pd ) {
    }
}
