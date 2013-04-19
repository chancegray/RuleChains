/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.usf.RuleChains

/**
 *
 * @author james
 */
enum ResultEnum {
    NONE, UPDATE, RECORDSET, ROW, APPENDTOROW, PREPENDTOROW;
    public static ResultEnum byName(String str) {
        for (resultEnum in ResultEnum.values()) {
            if(str.trim().toUpperCase().equalsIgnoreCase(resultEnum.name())) {
                return resultEnum;
            }
        }
        return ResultEnum.NONE;            
    }    	
}

