package edu.usf.RuleChains
import org.hibernate.FlushMode

/**
 * RuleSet domain class is a container for organizing rule.
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 */ 
class RuleSet {
    String name
    static hasMany = [rules:Rule]
    boolean isSynced = true    
    static transients = ['isSynced']    
    static constraints = {
        name(
            blank: false,
            nullable: false,
            unique: true,
            size: 3..255,
            //Custom constraint - only allow upper, lower, digits, dash and underscore
            // validator: { val, obj -> val ==~ /[A-Za-z0-9_.-]+/ },
            validator: { val, obj -> val ==~ /[A-Za-z0-9_.-]+/ && {
                    boolean valid = true;
                    RuleSet.withNewSession { session ->
                        session.flushMode = FlushMode.MANUAL
                        try {
                            valid = !!!Rule.findByName(val) && !!!Chain.findByName(val) && !!!ChainServiceHandler.findByName(val)
                        } finally {
                            session.setFlushMode(FlushMode.AUTO)
                        }
                    }
                    return valid
                }.call()            
            }
        )
    }   
    /*
     * Handles syncronization for saves 
     */    
    def afterInsert() {
        if(isSynced) {
            saveGitWithComment("Creating ${name} RuleSet")
        }
    }
    /*
     * Handles syncronization for update
     */    
    def beforeUpdate() {
        if(isSynced) {
            updateGitWithComment("Updating ${name} RuleSet")
        }
    }
    /*
     * Handles syncronization for post-update saves 
     */        
    def afterUpdate() {
        if(isSynced) {
            // saveGitWithComment("Updating ${name} RuleSet")
        }
    }
    /*
     * Handles syncronization for deletes 
     */        
    def afterDelete() {
        if(isSynced) {
            deleteGitWithComment("Deleted ${name} RuleSet")
        }
    }    
}
