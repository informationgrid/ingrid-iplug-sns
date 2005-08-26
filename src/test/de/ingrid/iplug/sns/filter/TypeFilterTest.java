/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: DispatcherTest.java,v $
 */
package de.ingrid.iplug.sns.filter;

import junit.framework.TestCase;

/**
 * 
 * created on 21.07.2005 <p>
 *
 * @author hs
 */
public class TypeFilterTest extends TestCase {
    
    private static final String TYPE2 = "type2";
    private static final String PATH_PREFIX = "path/";
    private static final String TYPE1 = "type1";
    
    /**
     * 
     */
    public void testFilter(){
        TypeFilter filter = new TypeFilter(new String[]{TYPE1, TYPE2});
        assertTrue(filter.accept(PATH_PREFIX + TYPE1));
        assertTrue(filter.accept(PATH_PREFIX + TYPE2));
        assertFalse(filter.accept(PATH_PREFIX + "anyType"));
    }

}
