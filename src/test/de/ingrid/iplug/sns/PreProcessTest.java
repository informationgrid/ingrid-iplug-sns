/*
 * Copyright (c) 1997-2006 by media style GmbH
 */

package de.ingrid.iplug.sns;

import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.TermQuery;
import junit.framework.TestCase;

/**
 * 
 */
public class PreProcessTest extends TestCase {

    private PreProcess fPreProcess;

    protected void setUp() throws Exception {
        super.setUp();

        this.fPreProcess = new PreProcess(new SNSController(new SNSClient("ms", "portalu2006", "de")));
    }

    /**
     * @throws Exception
     * 
     */
    public void testProcess() throws Exception {
        IngridQuery query = new IngridQuery(true, false,IngridQuery.TERM, "blabla");
        query.addTerm(new TermQuery(true, false, "blau"));
        query.addTerm(new TermQuery(true, false, "grün"));
        query.addTerm(new TermQuery(true, false, "rot"));
        query.addTerm(new TermQuery(true, false, "schwarz"));
        query.addTerm(new TermQuery(true, false, "gelb"));

        this.fPreProcess.process(query);

        String[] terms = (String[]) query.get("similarTerms");
        assertNotNull(terms);
        for (int i = 0; i < terms.length; i++) {
            System.out.println(terms[i]);
        }
    }
}
