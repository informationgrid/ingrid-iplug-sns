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
/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: DispatcherTest.java,v $
 */

package de.ingrid.iplug.sns.v04;

import junit.framework.TestCase;
import de.ingrid.external.sns.SNSClient;
import de.ingrid.iplug.sns.SNSControllerTestLocal;
import de.ingrid.iplug.sns.SNSServiceClientTest;

/**
 * Test for service-adapter-feature (INGRID-17). created on 21.07.2005
 * <p>
 * 
 * @author hs
 */
public class ServiceAdapterTest extends TestCase {

    /**
     * INGRID-100
     * 
     * @throws Exception
     */
    public void testCommunicationWithSNS() throws Exception {
        SNSControllerTestLocal controller = new SNSControllerTestLocal();
        controller.setSNSClient(new SNSClient("ms", "m3d1asyl3", "de"));
        
        controller.testGetAssociatedTopics();
        controller.testGetDocumentRelatedTopics();
        controller.testTopicsForTerm();

        new SNSServiceClientTest().testAutoClassify();
        new SNSServiceClientTest().testFindTopics();
    }
}
