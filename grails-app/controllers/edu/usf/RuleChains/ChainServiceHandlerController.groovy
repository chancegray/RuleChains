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
}
