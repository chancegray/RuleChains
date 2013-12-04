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
        saveGitWithComment("Creating ${name} StoredProcedureQuery")
    }
    def beforeUpdate() {
        updateGitWithComment("Renaming ${name} StoredProcedureQuery")
    }
    def afterUpdate() {
        saveGitWithComment("Updating ${name} StoredProcedureQuery")
    }
    def beforeDelete() {
        deleteGitWithComment("Deleted ${name} StoredProcedureQuery")
    }
    
}
