/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: DispatcherTest.java,v $
 */
package de.ingrid.iplug.sns.filter;




/**
 * Configurable utility to detect specific sns topic types.
 * created on 21.07.2005 <p>
 *
 * @author hs
 */
public class TypeFilter{
    private final String[] fSuffixes;
    
    /**
     * Constructs an instance by using the given types.
     * @param types
     */
    public TypeFilter(String[] types){
        this.fSuffixes = types;
    }
    
    /**
     * Accepts the given topic reference if the topic type suffix a known suffix stored 
     * in the field fSuffixes.    
     * @param href The topic type represented by its topic reference to accept.
     * @return true if the given reference has a suffix which is accepted by the filter.
     */
    public boolean accept(String href) {
        for(int i = 0; i < this.fSuffixes.length; i ++){
            if(href.endsWith(this.fSuffixes[i])){
                return true;
            }
        }
        return false; 
    }
}
