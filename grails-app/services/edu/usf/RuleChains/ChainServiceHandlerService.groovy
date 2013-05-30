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
            return [ error: "Method '${name}' for handler '${name}'" ]
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
}
