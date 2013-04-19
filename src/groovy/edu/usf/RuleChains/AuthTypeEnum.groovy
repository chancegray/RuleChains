/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.usf.RuleChains

/**
 *
 * @author james
 */
enum AuthTypeEnum {
    NONE,BASIC,DIGEST,CAS,CASSPRING;
    public static AuthTypeEnum byName(String str) {
        for (authTypeEnum in AuthTypeEnum.values()) {
            if(str.trim().toUpperCase().equalsIgnoreCase(authTypeEnum.name())) {
                return authTypeEnum;
            }
        }
        return AuthTypeEnum.NONE;            
    }   	
}

