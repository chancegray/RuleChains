package edu.usf.RuleChains

class ChainServiceHandlerService {
    def chainService
    
    def handleChainService(String name,String method,def input) {
        def chainServiceHandlerResponse = getChainServiceHandler(name)
        if("error" in chainServiceHandlerResponse) {
            return [ error: "Chain Service Handler not found" ]
        } else if(chainServiceHandlerResponse.chainServiceHandler.method in [ MethodEnum.byName(method) ]) {
            // Execute the rule chain
            def chain = Chain.findByName(chainServiceHandlerResponse.chainServiceHandler.chain.name)
            if(!!chain) {
                return [ result: Chain.rearrange(chain.execute(Chain.rearrange(input,chainServiceHandlerResponse.chainServiceHandler.inputReorder)),chainServiceHandlerResponse.chainServiceHandler.outputReorder) ]
            } else {
                return [ error: "Chain not found ${chainServiceHandlerResponse.chainServiceHandler.chain.name}" ]
            }            
        } else {
            return [ error: "Method '${method}' for handler '${name}' not defined" ]
        }
    }
    def getChainServiceHandler(String name) {
        if(!!name) {
            def chainServiceHandler = ChainServiceHandler.findByName(name.trim())
            if(!!chainServiceHandler) {
                return [ chainServiceHandler: chainServiceHandler ]
            }
            return [ error : "ChainServiceHandler named ${name} not found!"]
        }
        return [ error : "You must supply a name for the target ChainServiceHandler"]        
    }
    def listChainServiceHandlers(String pattern = null) { 
        if(!!pattern) {
            return [chainServiceHandlers: ChainServiceHandler.list().findAll(fetch:[links:"eager"]) {
                Pattern.compile(pattern.trim()).matcher(it.name).matches()
            }]
        } else {
            return [ chainServiceHandlers: ChainServiceHandler.list() ]
        }
    }
    def addChainServiceHandler(String name,def ch,boolean isSynced = true) {
        def chain = Chain.findByName(('name' in ch)?ch.name:ch)
        if(!!name && !!!!chain) {
            chain.isSynced = isSynced
            def chainServiceHandler = [ name: name.trim(), chain: chain ] as ChainServiceHandler
            chainServiceHandler.isSynced = isSynced
            if(!chainServiceHandler.save(failOnError:false, flush: true, insert: true, validate: true)) {
                return [ error : "Name value '${chainServiceHandler.errors.fieldError.rejectedValue}' rejected" ]
            } else {
                return [ chainServiceHandler: chainServiceHandler ]
            }
        } else if(!!!chain) {
            return [ error: "The chain specified does not exist" ]
        }
        return [ error: "You must supply a name" ]
    }
    def modifyChainServiceHandler(String name,def updatedChainServiceHandler,boolean isSynced = true) {
        def chainServiceHandler = ChainServiceHandler.findByName(name.trim())
        if(!!chainServiceHandler) {
            chainServiceHandler.properties = updatedChainServiceHandler.collectEntries {
                switch(it.key) {
                    case "method":
                        return [ "${it.key}": MethodEnum.byName((("name" in it.value)?it.value.name:it.value)) ]
                        break
                    case "chain":
                        return [ "${it.key}": Chain.findByName(("name" in it.value)?it.value.name:it.value) ]
                        break
                    default:
                        return [ "${it.key}": it.value ]
                        break
                }
            }
            chainServiceHandler.isSynced
            if(!chainServiceHandler.save(failOnError:false, flush: true, validate: true)) {
                chainServiceHandler.errors.allErrors.each {
                    println it
                }           
                return [ error : "'${chainServiceHandler.errors.fieldError.field}' value '${chainServiceHandler.errors.fieldError.rejectedValue}' rejected" ]                
            }
            return [ chainServiceHandler : chainServiceHandler]
        }
        return [ error : "Chain Service Handler named ${name} not found!"]
    }
    def deleteChainServiceHandler(String name) {
        if(!!name) {
            def chainServiceHandler = ChainServiceHandler.findByName(name.trim())
            if(!!chainServiceHandler) {
                chainServiceHandler.delete()
                return [ success : "Chain Service Handler deleted" ]
            }
            return [ error : "Chain Service Handler named ${name} not found!"]
        }
        return [ error : "You must supply a name for the target Chain Service Handler"]        
    }
}
