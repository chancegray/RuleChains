/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.usf.RuleChains

/**
 *
 * @author james
 */
enum ExecuteEnum {
    EXECUTE_USING_ROW, NORMAL;
    public static ExecuteEnum byName(String str) {
        for (executeEnum in ExecuteEnum.values()) {
            if(str.trim().toUpperCase().equalsIgnoreCase(executeEnum.name())) {
                return executeEnum;
            }
        }
        return ExecuteEnum.NORMAL;            
    }	
}

