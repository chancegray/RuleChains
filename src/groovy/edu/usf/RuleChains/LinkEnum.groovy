/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.usf.RuleChains

/**
 * LinkEnum is an Enumerator for Link "Link" Types.
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 */ 
enum LinkEnum {
    NONE, LOOP, ENDLOOP, NEXT;
    /**
     * Converts a base string into a corresponding enumerated type
     * 
     * @param   str    The string name of the enumerated type
     * @return         A corresponding enumerated type matched on the string parameter
     */    
    public static LinkEnum byName(String str) {
        for(linkEnum in LinkEnum.values()) {
            if(str.trim().toUpperCase().equalsIgnoreCase(linkEnum.name())) {
                return linkEnum;
            }
        }
        return LinkEnum.NONE;            
    }             	
}

