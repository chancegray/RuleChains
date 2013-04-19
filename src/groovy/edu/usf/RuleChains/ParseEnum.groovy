/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.usf.RuleChains

/**
 *
 * @author james
 */
enum ParseEnum {
    TEXT, XML, JSON;
    public static ParseEnum byName(String str) {
        for (parseEnum in ParseEnum.values()) {
            if(str.trim().toUpperCase().equalsIgnoreCase(parseEnum.name())) {
                return parseEnum;
            }
        }
        return ParseEnum.TEXT;            
    }   	    	
}

