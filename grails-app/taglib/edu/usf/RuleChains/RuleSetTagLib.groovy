package edu.usf.RuleChains

/**
 * RuleSetTagLib is a taglib for RuleSet GSP views.
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 */ 
class RuleSetTagLib {
    static namespace = "ruleSet"  
    def ruleSetService
    
    /**
     * Provides a GSP method to produce a HTML select element of available rulesets.
     * 
     * @return A list of available rulesets formatted in an HTML select element
     */         
    def ruleSetSelect = {
        out << '<label for="ruleSet" style="padding-right:15px;">Select Rule Set To View</label>'
        out << g.select(name:"ruleSet",from:ruleSetService.listRuleSets().ruleSets.sort{ it.name },optionKey:"id",optionValue:"name",noSelection:['':'--ALL--'])
    }
    
}
