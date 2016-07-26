/*
 * **************************************************-
 * Ingrid iPlug XML
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertiesFiles;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertyLocations;
import com.tngtech.configbuilder.annotation.valueextractor.DefaultValue;
import com.tngtech.configbuilder.annotation.valueextractor.PropertyValue;

import de.ingrid.admin.IConfig;
import de.ingrid.admin.command.PlugdescriptionCommandObject;

@PropertiesFiles( {"config", "sns"} )
@PropertyLocations(directories = {"conf"}, fromClassLoader = true)
public class Configuration implements IConfig {
    
    @SuppressWarnings("unused")
    private static Log log = LogFactory.getLog(Configuration.class);
    
    @PropertyValue("sns.username")
    public String snsUsername;
    
    @PropertyValue("sns.password")
    public String snsPassword;
    
    @PropertyValue("sns.language")
    @DefaultValue("de")
    public String snsLanguage;
    
    
    @PropertyValue("sns.serviceURL.chronicle")
    public String snsUrlChronicle;
    
    @PropertyValue("sns.serviceURL.thesaurus")
    public String snsUrlThesaurus;
    
    @PropertyValue("sns.serviceURL.gazetteer")
    public String snsUrlGazetteer;
    
    @PropertyValue("sns.nativeKeyPrefix")
    public String snsPrefix;

    
    @Override
    public void initialize() {}
    
    @Override
    public void addPlugdescriptionValues( PlugdescriptionCommandObject pdObject ) {
        pdObject.put( "iPlugClass", "de.ingrid.iplug.sns.SnsPlug");
        
    }

    @Override
    public void setPropertiesFromPlugdescription( Properties props, PlugdescriptionCommandObject pd ) {
    }
}
