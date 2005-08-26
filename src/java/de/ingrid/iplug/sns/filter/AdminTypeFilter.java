/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: DispatcherTest.java,v $
 */
package de.ingrid.iplug.sns.filter;


/**
 * <code>TypeFilter</code> implementation for administrative areas.
 * created on 21.07.2005 <p>
 * @author hs
 */
public class AdminTypeFilter extends TypeFilter {
    /**
     * Constructs a TypeFilter for administrative areas. The filter accepts:
     * <li>communityType</li>
     * <li>districtType</li>
     * <li>quarterType</li>
     * <li>stateType</li>
     * <li>nationType</li>  
     */
    
    public AdminTypeFilter(){
        super(new String[]{"communityType", "districtType", "quarterType", "stateType", "nationType"});
    }

}
