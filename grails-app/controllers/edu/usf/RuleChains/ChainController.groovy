package edu.usf.RuleChains

import grails.converters.*

class ChainController {
    def chainService
    def listChains() { 
        withFormat {
            html {
                return chainService.listChains(params.pattern)
            }
            xml {
                render chainService.listChains(params.pattern) as XML
            }
            json {
                JSON.use("deep") { render chainService.listChains(params.pattern) as JSON }
            }
        }                    
    }
    def addChain() {
        withFormat {
            html {
                return chainService.addChain(params.name)
            }
            xml {
                render chainService.addChain(params.name) as XML
            }
            json {
                render chainService.addChain(params.name) as JSON
            }
        }                    
    }
    def modifyChain() {
        withFormat {
            html {
                return chainService.modifyChain(params.name,params.ruleSet.name)
            }
            xml {
                render chainService.modifyChain(params.name,params.ruleSet.name) as XML
            }
            json {
                render chainService.modifyChain(params.name,params.ruleSet.name) as JSON
            }
        }                    
    }
    def deleteChain() {
        withFormat {
            html {
                return chainService.deleteChain(params.name)
            }
            xml {
                render chainService.deleteChain(params.name) as XML
            }
            json {
                render chainService.deleteChain(params.name) as JSON
            }
        }                    
    }
    def getChain() {
        withFormat {
            html {
                return chainService.getChain(params.name)
            }
            xml {
                render chainService.getChain(params.name) as XML
            }
            json {
                JSON.use("deep") { render chainService.getChain(params.name) as JSON }
            }
        }                    
    }    
    def getChainLink() {
        withFormat {
            html {
                return chainService.getChainLink(params.name,params.sequenceNumber)
            }
            xml {
                render chainService.getChainLink(params.name,params.sequenceNumber) as XML
            }
            json {
                JSON.use("deep") { render chainService.getChainLink(params.name,params.sequenceNumber) as JSON }
            }
        }                            
    }
    def addChainLink() {
        withFormat {
            html {
                return chainService.addChainLink(params.name,params.link)
            }
            xml {
                render chainService.addChainLink(params.name,params.link) as XML
            }
            json {
                JSON.use("deep") { render chainService.addChainLink(params.name,params.link) as JSON }
            }
        }                            
    }
    def deleteChainLink() {
        withFormat {
            html {
                return chainService.deleteChainLink(params.name,params.sequenceNumber)
            }
            xml {
                render chainService.deleteChainLink(params.name,params.sequenceNumber) as XML
            }
            json {
                JSON.use("deep") { render chainService.deleteChainLink(params.name,params.sequenceNumber) as JSON }
            }
        }                            
    }
    def modifyChainLink() {
        withFormat {
            html {
                return chainService.modifyChainLink(params.name,params.sequenceNumber,params.link)
            }
            xml {
                render chainService.modifyChainLink(params.name,params.sequenceNumber,params.link) as XML
            }
            json {
                JSON.use("deep") { render chainService.modifyChainLink(params.name,params.sequenceNumber,params.link) as JSON }
            }
        }                            
    }
    def getSources() {
        withFormat {
            html {
                return chainService.getSources()
            }
            xml {
                render chainService.getSources() as XML
            }
            json {
                render chainService.getSources() as JSON
            }
        }                            
    }
    
}
