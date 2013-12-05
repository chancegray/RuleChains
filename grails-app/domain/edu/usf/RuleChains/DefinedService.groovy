package edu.usf.RuleChains

import edu.usf.RuleChains.MethodEnum
import edu.usf.RuleChains.AuthTypeEnum
import edu.usf.RuleChains.ParseEnum

class DefinedService extends Rule {
    MethodEnum method = MethodEnum.GET
    AuthTypeEnum authType = AuthTypeEnum.NONE
    ParseEnum parse = ParseEnum.TEXT
    String url = ""
    String user = ""
    String password = ""    
    String springSecurityBaseURL = ""   
    Map headers = [:]
    static constraints = {
        method( 
            blank: false,
            validator: { val, obj -> 
                return val.name() in MethodEnum.values().collect { it.name() }                
            }        
        )
        authType( 
            blank: false,
            validator: { val, obj -> 
                return val.name() in AuthTypeEnum.values().collect { it.name() }                
            }        
        )
        parse( 
            blank: false,
            validator: { val, obj -> 
                return val.name() in ParseEnum.values().collect { it.name() }                
            }        
        )
    }
    
    def afterInsert() {
        saveGitWithComment("Creating ${name} DefinedService")
    }
    def beforeUpdate() {
        updateGitWithComment("Renaming ${name} DefinedService")
    }
    def afterUpdate() {
        saveGitWithComment("Updating ${name} DefinedService")
        /**
         * Anytime a rule is renamed, any link referenced rule name in git repo needs to be updated (if exists)
         **/
        Link.findAllByRule(this).each { l ->
            l.saveGitWithComment("Updating Link referencing ${name} SQLQuery")
        }
    }
    def beforeDelete() {
        deleteGitWithComment("Deleted ${name} DefinedService")
    }
}
