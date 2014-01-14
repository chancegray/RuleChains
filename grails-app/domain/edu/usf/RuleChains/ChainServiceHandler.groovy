package edu.usf.RuleChains
import edu.usf.RuleChains.*
import org.hibernate.FlushMode

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
        name(   
            blank: false,
            nullable: false,
            size: 3..255,
            unique: true,
            //Custom constraint - only allow upper, lower, digits, dash and underscore
            validator: { val, obj -> 
                val ==~ /[A-Za-z0-9_.-]+/ && {  
                    boolean valid = true;
                    ChainServiceHandler.withNewSession { session ->
                        session.flushMode = FlushMode.MANUAL
                        try {
                            valid = !!!Rule.findByName(val) && !!!RuleSet.findByName(val) && !!!Chain.findByName(val)
                        } finally {
                            session.setFlushMode(FlushMode.AUTO)
                        }
                    }
                    return valid
                }.call() 
            }
        )               
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
