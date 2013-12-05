package edu.usf.RuleChains

class ChainServiceHandler {
    String name
    Chain chain
    String inputReorder = ''
    String outputReorder = ''
    MethodEnum method = MethodEnum.GET
    static mapping = {
        inputReorder type: 'text'
        outputReorder type: 'text'
    }    
    
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
    def afterInsert() {
        saveGitWithComment("Creating ${name} ChainServiceHandler")
    }
    def beforeUpdate() {
        updateGitWithComment("Renaming ${name} ChainServiceHandler")
    }
    def afterUpdate() {
        saveGitWithComment("Updating ${name} ChainServiceHandler")
    }
    def beforeDelete() {
        deleteGitWithComment("Deleted ${name} ChainServiceHandler")
    }        
}
