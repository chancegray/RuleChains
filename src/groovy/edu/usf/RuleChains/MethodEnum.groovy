/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.usf.RuleChains

/**
 * MethodEnum is an Enumerator for REST Method Types.
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 */ 
enum MethodEnum {
    GET, POST, PUT, DELETE;
    /**
     * Converts a base string into a corresponding enumerated type
     * 
     * @param   str    The string name of the enumerated type
     * @return         A corresponding enumerated type matched on the string parameter
     */        
    public static MethodEnum byName(String str) {
        for (methodEnum in MethodEnum.values()) {
            if(str.trim().toUpperCase().equalsIgnoreCase(methodEnum.name())) {
                return methodEnum;
            }
        }
        return MethodEnum.GET;            
    }   	    
}

