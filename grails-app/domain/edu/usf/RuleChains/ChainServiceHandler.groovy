package edu.usf.RuleChains

class ChainServiceHandler {
    String name
    Chain chain
    String inputReorder = ''
    String outputReorder = ''
    MethodEnum method = MethodEnum.GET
    
    static constraints = {
        inputReorder(blank:true)
        outputReorder(blank:true) 
        chain(nullable:false)
        method( 
            blank: false,
            validator: { val, obj -> 
                return val.name() in MethodEnum.values().collect { it.name() }                
            }        
        )
    }
}
