/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.usf.RuleChains

/**
 * ExecuteEnum is an Enumerator for Link Execution Types.
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 */ 
enum ExecuteEnum {
    EXECUTE_USING_ROW, NORMAL;
    /**
     * Converts a base string into a corresponding enumerated type
     * 
     * @param   str    The string name of the enumerated type
     * @return         A corresponding enumerated type matched on the string parameter
     */    
    public static ExecuteEnum byName(String str) {
        for (executeEnum in ExecuteEnum.values()) {
            if(str.trim().toUpperCase().equalsIgnoreCase(executeEnum.name())) {
                return executeEnum;
            }
        }
        return ExecuteEnum.NORMAL;            
    }	
}

