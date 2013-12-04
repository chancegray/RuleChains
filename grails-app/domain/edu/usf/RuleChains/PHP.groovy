package edu.usf.RuleChains

class PHP extends Rule {
    String rule = ""
    static constraints = {
    }
    static mapping = {
        rule type: 'text'
    }
    
    def afterInsert() {
        saveGitWithComment("Creating ${name} PHP")
    }
    def beforeUpdate() {
        updateGitWithComment("Renaming ${name} PHP")
    }
    def afterUpdate() {
        saveGitWithComment("Updating ${name} PHP")
    }
    def beforeDelete() {
        deleteGitWithComment("Deleted ${name} PHP")
    }
}