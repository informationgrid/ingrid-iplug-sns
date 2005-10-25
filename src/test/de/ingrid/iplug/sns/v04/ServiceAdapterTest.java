/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: DispatcherTest.java,v $
 */

package de.ingrid.iplug.sns.v04;

import junit.framework.TestCase;
import de.ingrid.iplug.sns.SNSControllerTest;
import de.ingrid.iplug.sns.SNSServiceClientTest;

/**
 * Test for service-adapter-feature (INGRID-17).
 * created on 21.07.2005 <p>
 *
 * @author hs
 */
public class ServiceAdapterTest extends TestCase {
	/**
	 * INGRID-100 
	 * @throws Exception 
	 */
	public void testCommunicationWithSNS() throws Exception{
		new SNSControllerTest().testGetAssociatedTopics();
        new SNSControllerTest().testGetDocumentRelatedTopics();
        new SNSControllerTest().testTopicsForTerm();
        
        new SNSServiceClientTest().testAutoClassify();
        new SNSServiceClientTest().testFindTopics();
        new SNSServiceClientTest().testGetPSI();
        new SNSServiceClientTest().testGetTypes();
	}
	
}
