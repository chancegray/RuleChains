/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.usf.RuleChains

/**
 * ResultEnum is an Enumerator for Link Result Types.
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
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

