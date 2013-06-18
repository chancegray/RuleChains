/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.usf.RuleChains

/**
 *
 * @author james
 */
enum ServiceTypeEnum {
    SQLQUERY,
    GROOVY,
    STOREDPROCEDUREQUERY, 
    DEFINEDSERVICE,
    SNIPPET;
        
    public static ServiceTypeEnum byName(String str) {
        for (serviceTypeEnum in ServiceTypeEnum.values()) {
            if(str.trim().toUpperCase().equalsIgnoreCase(serviceTypeEnum.name())) {
                return serviceTypeEnum;
            }
        }
        return ServiceTypeEnum.SQLQUERY;            
    }  	
}

