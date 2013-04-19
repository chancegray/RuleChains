/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.usf.RuleChains

/**
 *
 * @author james
 */
enum MethodEnum {
    GET, POST, PUT, DELETE;
    public static MethodEnum byName(String str) {
        for (methodEnum in MethodEnum.values()) {
            if(str.trim().toUpperCase().equalsIgnoreCase(methodEnum.name())) {
                return methodEnum;
            }
        }
        return MethodEnum.GET;            
    }   	    
}

