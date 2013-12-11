package edu.usf.RuleChains

class ChainServiceHandler {
    String name
    Chain chain
    String inputReorder = ''
    String outputReorder = ''
    MethodEnum method = MethodEnum.GET
    boolean isSynced = true
    static transients = ['isSynced']
    
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
        if(isSynced) {
            saveGitWithComment("Creating ${name} ChainServiceHandler")
        }
    }
    def beforeUpdate() {
        if(isSynced) {
            updateGitWithComment("Renaming ${name} ChainServiceHandler")
        }
    }
    def afterUpdate() {
        if(isSynced) {
            saveGitWithComment("Updating ${name} ChainServiceHandler")
        }
    }
    def beforeDelete() {
        if(isSynced) {
            deleteGitWithComment("Deleted ${name} ChainServiceHandler")
        }
    }        
}
