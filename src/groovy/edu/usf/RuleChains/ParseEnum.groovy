/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.usf.RuleChains

/**
 * ParseEnum is an Enumerator for output Parsing Types.
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 */ 
enum ParseEnum {
    TEXT, XML, JSON;
    /**
     * Converts a base string into a corresponding enumerated type
     * 
     * @param   str    The string name of the enumerated type
     * @return         A corresponding enumerated type matched on the string parameter
     */            
    public static ParseEnum byName(String str) {
        for (parseEnum in ParseEnum.values()) {
            if(str.trim().toUpperCase().equalsIgnoreCase(parseEnum.name())) {
                return parseEnum;
            }
        }
        return ParseEnum.TEXT;            
    }   	    	
}

