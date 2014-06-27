/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.usf.RuleChains

/**
 * AuthTypeEnum is an Enumerator for Authentication Types.
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 */ 
enum AuthTypeEnum {
    NONE,BASIC,DIGEST,CAS,CASSPRING;
    /**
     * Converts a base string into a corresponding enumerated type
     * 
     * @param   str    The string name of the enumerated type
     * @return         A corresponding enumerated type matched on the string parameter
     */
    public static AuthTypeEnum byName(String str) {
        for (authTypeEnum in AuthTypeEnum.values()) {
            if(str.trim().toUpperCase().equalsIgnoreCase(authTypeEnum.name())) {
                return authTypeEnum;
            }
        }
        return AuthTypeEnum.NONE;            
    }   	
}

