/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.usf.RuleChains

/**
 *
 * @author james
 */
enum LinkEnum {
    NONE, LOOP, ENDLOOP, NEXT;
    public static LinkEnum byName(String str) {
        for(linkEnum in LinkEnum.values()) {
            if(str.trim().toUpperCase().equalsIgnoreCase(linkEnum.name())) {
                return linkEnum;
            }
        }
        return LinkEnum.NONE;            
    }             	
}

