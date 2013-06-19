package edu.usf.RuleChains

import grails.converters.*

class ChainServiceHandlerController {
    def chainService
    def chainServiceHandlerService
    
    def handleChainService() {
        withFormat {
            html {
                return chainServiceHandlerService.handleChainService(params.handler,request.method,params.input)
            }
            xml {
                render chainServiceHandlerService.handleChainService(params.handler,request.method,params.input) as XML
            }
            json {
                JSON.use("deep") { render chainServiceHandlerService.handleChainService(params.handler,request.method,params.input) as JSON }
            }
        }        
    }
    def listChainServiceHandlers() {
        withFormat {
            html {
                return chainServiceHandlerService.listChainServiceHandlers(params.pattern)
            }
            xml {
                render chainServiceHandlerService.listChainServiceHandlers(params.pattern) as XML
            }
            json {
                JSON.use("deep") { render chainServiceHandlerService.listChainServiceHandlers(params.pattern) as JSON }
            }
        }
    }
    def addChainServiceHandler() {
        withFormat {
            html {
                return chainServiceHandlerService.addChainServiceHandler(params.name,params.chain)
            }
            xml {
                render chainServiceHandlerService.addChainServiceHandler(params.name,params.chain) as XML
            }
            json {
                render chainServiceHandlerService.addChainServiceHandler(params.name,params.chain) as JSON
            }
        }                            
    }
    def modifyChainServiceHandler() {
        withFormat {
            html {
                return chainServiceHandlerService.modifyChainServiceHandler(params.name,params.chainServiceHandler)
            }
            xml {
                render chainServiceHandlerService.modifyChainServiceHandler(params.name,params.chainServiceHandler) as XML
            }
            json {
                JSON.use("deep") { render chainServiceHandlerService.modifyChainServiceHandler(params.name,params.chainServiceHandler) as JSON }
            }
        }                                    
    }
    def deleteChainServiceHandler() {
        withFormat {
            html {
                return chainServiceHandlerService.deleteChainServiceHandler(params.name)
            }
            xml {
                render chainServiceHandlerService.deleteChainServiceHandler(params.name) as XML
            }
            json {
                JSON.use("deep") { render chainServiceHandlerService.deleteChainServiceHandler(params.name) as JSON }
            }
        }                                    
    }
}
