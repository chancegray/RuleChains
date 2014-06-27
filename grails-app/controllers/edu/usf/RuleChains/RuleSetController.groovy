package edu.usf.RuleChains

import grails.converters.*
/**
 * RuleSetController provides for REST services handling the processing and manipulation of RuleSet objects
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 */ 
class RuleSetController {
    def ruleSetService
    /**
     * Returns a list of RuleSet objects objects with an option matching filter
     * 
     * @return          An object containing the resulting list of RuleSet objects
     * @see    RuleSet
     */    
    def listRuleSets() { 
        withFormat {
            html {
                return ruleSetService.listRuleSets(params.pattern)
            }
            xml {
                render ruleSetService.listRuleSets(params.pattern) as XML
            }
            json {
                JSON.use("deep") { render ruleSetService.listRuleSets(params.pattern) as JSON }
            }
        }                    
    }
    /**
     * Creates a new RuleSet
     * 
     * @return           Returns an object containing the new RuleSet
     */    
    def addRuleSet() {
        withFormat {
            html {
                return ruleSetService.addRuleSet(params.name)
            }
            xml {
                render ruleSetService.addRuleSet(params.name) as XML
            }
            json {
                render ruleSetService.addRuleSet(params.name) as JSON
            }
        }                    
    }
    /**
     * Finds a RuleSet by it's name
     * 
     * @return       Returns a RuleSet if matched or returns an error message
     * @see    RuleSet
     */
    def getRuleSet() {
        withFormat {
            html {
                return ruleSetService.getRuleSet(params.name)
            }
            xml {
                render ruleSetService.getRuleSet(params.name) as XML
            }
            json {
                JSON.use("deep") { render ruleSetService.getRuleSet(params.name) as JSON }
            }
        }                    
    }
    /**
     * Removes an existing RuleSet by name
     * 
     * @return           Returns an object containing the sucess or error message
     */    
    def deleteRuleSet() {
        withFormat {
            html {
                return ruleSetService.deleteRuleSet(params.name)
            }
            xml {
                render ruleSetService.deleteRuleSet(params.name) as XML
            }
            json {
                render ruleSetService.deleteRuleSet(params.name) as JSON
            }
        }                    
    }
    /**
     * Renames an existing RuleSet
     * 
     * @return                                   Returns an object containing the updated RuleSet
     */
    def modifyRuleSet() {
        withFormat {
            html {
                return ruleSetService.modifyRuleSet(params.name,params.ruleSet.name)
            }
            xml {
                render ruleSetService.modifyRuleSet(params.name,params.ruleSet.name) as XML
            }
            json {
                render ruleSetService.modifyRuleSet(params.name,params.ruleSet.name) as JSON
            }
        }                    
    }
    /**
     * Retrieves a Rule by it's RuleSet name and Rule name
     * 
     * @return                 Returns a Rule if matched or returns an error message
     * @see    Rule
     */    
    def getRule() {
        withFormat {
            html {
                return ruleSetService.getRule(params.name,params.id)
            }
            xml {
                render ruleSetService.getRule(params.name,params.id) as XML
            }
            json {
                JSON.use("deep") { render ruleSetService.getRule(params.name,params.id) as JSON }
            }
        }                    
    }
    /**
     * Creates a new Rule in an existing RuleSet
     * 
     * @return                 Returns an object containing the newly created Rule
     * @see Rule
     * @see RuleSet
     */
    def addRule() {
        withFormat {
            html {
                return ruleSetService.addRule(params.name,params.id,params.serviceType)
            }
            xml {
                render ruleSetService.addRule(params.name,params.id,params.serviceType) as XML
            }
            json {
                render ruleSetService.addRule(params.name,params.id,params.serviceType) as JSON
            }
        }                            
    }
    /**
     * Updates an existing rule in a RuleSet
     * 
     * @return                    Returns an object containing the updete Rule
     */
    def updateRule() {
        withFormat {
            html {
                return ruleSetService.updateRule(params.name,params.id,params.rule)
            }
            xml {
                render ruleSetService.updateRule(params.name,params.id,params.rule) as XML
            }
            json {
                render ruleSetService.updateRule(params.name,params.id,params.rule) as JSON
            }
        }                                    
    }
    /**
     * Renames an existing Rule
     * 
     * @return                                   Returns an object containing the updated Rule
     */
    def updateRuleName() {
        withFormat {
            html {
                return ruleSetService.updateRuleName(params.name,params.id,params.nameUpdate)
            }
            xml {
                render ruleSetService.updateRuleName(params.name,params.id,params.nameUpdate) as XML
            }
            json {
                render ruleSetService.updateRuleName(params.name,params.id,params.nameUpdate) as JSON
            }
        }                                            
    }
    /**
     * Removes an existing Rule by RuleSet name and Rule name. 
     * 
     * @return                 Returns an object containing the sucess or error message
     */    
    def deleteRule(String ruleSetName,String name) {
        withFormat {
            html {
                return ruleSetService.deleteRule(params.name,params.id)
            }
            xml {
                render ruleSetService.deleteRule(params.name,params.id) as XML
            }
            json {
                render ruleSetService.deleteRule(params.name,params.id) as JSON
            }
        }                                            
    }
    /**
     * Relocates an existing Rule in a different RuleSet
     *
     * @return                 Returns moved Rule if successful or returns an error message
     */
    def moveRule() {
        withFormat {
            html {
                return ruleSetService.moveRule(params.name,params.id,params.nameUpdate)
            }
            xml {
                render ruleSetService.moveRule(params.name,params.id,params.nameUpdate) as XML
            }
            json {
                render ruleSetService.moveRule(params.name,params.id,params.nameUpdate) as JSON
            }
        }                                            
    }
}
