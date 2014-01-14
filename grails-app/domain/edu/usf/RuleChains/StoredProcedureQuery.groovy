package edu.usf.RuleChains

class StoredProcedureQuery extends Rule {
    String rule = ""
    String closure = """ 
{-> 
    rows = [
        [
            
        ]
    ]
}
"""
    static constraints = {
    }
    static mapping = {
        rule type: 'text'
    }    
    
    def afterInsert() {
        if(isSynced) {
            saveGitWithComment("Creating ${name} StoredProcedureQuery")
        }
    }
    def beforeUpdate() {
        if(isSynced) {
            updateGitWithComment("Renaming ${name} StoredProcedureQuery")
        }
    }
    def afterUpdate() {
        if(isSynced) {
            saveGitWithComment("Updating ${name} StoredProcedureQuery")
            /**
             * Anytime a rule is renamed, any link referenced rule name in git repo needs to be updated (if exists)
             **/
            Link.findAllByRule(this).each { l ->
                l.saveGitWithComment("Updating Link referencing ${name} SQLQuery")
            }        
        }
    }
    def beforeDelete() {
        if(isSynced) {
            deleteGitWithComment("Deleted ${name} StoredProcedureQuery")
        }
    }
    
}
