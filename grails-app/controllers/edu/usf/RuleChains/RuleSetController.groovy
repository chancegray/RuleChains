package edu.usf.RuleChains

import grails.converters.*

class RuleSetController {
    def ruleSetService
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
