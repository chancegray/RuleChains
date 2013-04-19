package edu.usf.RuleChains

class RuleSetTagLib {
    static namespace = "ruleSet"  
    def ruleSetService
    def ruleSetSelect = {
        out << '<label for="ruleSet" style="padding-right:15px;">Select Rule Set To View</label>'
        out << g.select(name:"ruleSet",from:ruleSetService.listRuleSets().ruleSets.sort{ it.name },optionKey:"id",optionValue:"name",noSelection:['':'--ALL--'])
    }
    
}
