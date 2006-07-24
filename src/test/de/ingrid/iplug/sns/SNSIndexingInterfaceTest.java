/*
 * Copyright (c) 1997-2006 by media style GmbH
 */

package de.ingrid.iplug.sns;

import junit.framework.TestCase;

/**
 * 
 */
public class SNSIndexingInterfaceTest extends TestCase {

    private SNSIndexingInterface fSnsInterface;

    private boolean fToStdout = false;

    protected void setUp() throws Exception {
        super.setUp();

        this.fSnsInterface = new SNSIndexingInterface("ms", "m3d1asyl3", "de");
        this.fSnsInterface.setTimeout(180000);
        
        this.fToStdout = true;
    }

   /**
    * @param iinterface 
 * @throws Exception
    */
   public void setSNSIndexingInterface(SNSIndexingInterface iinterface) throws Exception {
       this.fSnsInterface = iinterface;
   }
    
    /**
     * @throws Exception
     */
    public void testGetReferencesToSpace() throws Exception {
        this.fSnsInterface.getBuzzwords("Halle", 1000, false);

        final Wgs84Box[] result = this.fSnsInterface.getReferencesToSpace();
        assertNotNull(result);
        assertEquals(5, result.length);

        for (int i = 0; i < result.length; i++) {
            System.out.println(result[i].getTopicName());
            System.out.println(result[i].getX1());
            System.out.println(result[i].getX2());
            System.out.println(result[i].getY1());
            System.out.println(result[i].getY2());
            System.out.println(result[i].getGemeindekennziffer());
        }
    }

    /**
     * @throws Exception
     */
    public void testGetReferencesToSpaceBundesland() throws Exception {
        this.fSnsInterface.getBuzzwords("Sachsen", 1000, true);

        final Wgs84Box[] result = this.fSnsInterface.getReferencesToSpace();
        assertNotNull(result);
        assertEquals(2, result.length);

        for (int i = 0; i < result.length; i++) {
            System.out.println(result[i].getTopicName());
            System.out.println(result[i].getX1());
            System.out.println(result[i].getX2());
            System.out.println(result[i].getY1());
            System.out.println(result[i].getY2());
            System.out.println(result[i].getGemeindekennziffer());
        }
    }

    /**
     * @throws Exception
     */
    public void testGetReferencesToTime() throws Exception {
        this.fSnsInterface.getBuzzwords("Tschernobyl Ohio", 1000, false);

        final Temporal[] result = this.fSnsInterface.getReferencesToTime();
        assertNotNull(result);
        assertEquals(4, result.length);
        System.out.println(result[0].getAt());
    }

    /**
     * @throws Exception
     */
    public void testGetBuzzword() throws Exception {
        String[] result = null;
        final String words = "In diesem Jahr können sich kleine und mittlere Unternehmen bis zum 15. "
                + "August 2006 bewerben. Eine aus Vertretern von Wissenschaft, Wirtschaft und mittelständischen "
                + "Anwenderunternehmen besetzte Jury wird bis zu drei Bewerber aus den Kategorien E-Business, Breitband und "
                + "Mobilität auswählen und mit Preisen in Höhe von je 25.000 Euro auszeichnen. Die Preisverleihung findet im "
                + "Rahmen des 2. Deutschen ITK-Mittelstandstages im November 2006 statt Für die Verwendung der "
                + "Ein-Ausgabe-Klassen muss das Package java.io importiert werden Wir haben bereits gelernt, wie die Ein- und "
                + "Ausgabe in Graphischen User-Interfaces programmiert wird. Nun wollen wir uns auch damit beschäftigen, wie wir "
                + "Daten von Dateien einlesen und in Dateien speichern können. Wir haben bereits gelernt, wie die Ein- und "
                + "Ausgabe in Graphischen User-Interfaces programmiert wird.";
        final long start = System.currentTimeMillis();
        try {
            result = this.fSnsInterface.getBuzzwords(words, 1000, false);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        final long end = System.currentTimeMillis();

        if (this.fToStdout) {
            System.out.println("Time for getting all buzzwords: " + ((end - start) / 1000) + " s");
        }

        assertNotNull(result);
    }

    /**
     * @throws Exception
     */
    public void testGetBuzzwordNotExistent() throws Exception {
        final String[] result = this.fSnsInterface.getBuzzwords("blabla", 1000, false);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    /**
     * @throws Exception
     */
    public void testGetReferencesNotExistent() throws Exception {
        this.fSnsInterface.getBuzzwords("blabla", 1000, false);

        final Temporal[] result = this.fSnsInterface.getReferencesToTime();
        assertNotNull(result);
        assertEquals(0, result.length);
    }
}
