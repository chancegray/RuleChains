package edu.usf.RuleChains

class Groovy extends Rule {
    String rule = ""
    static constraints = {
    }
    static mapping = {
        rule type: 'text'
    }
    def afterInsert() {
        saveGitWithComment("Creating ${name} Groovy")
    }
    def beforeUpdate() {
        updateGitWithComment("Renaming ${name} Groovy")
    }
    def afterUpdate() {
        saveGitWithComment("Updating ${name} Groovy")
    }
    def beforeDelete() {
        deleteGitWithComment("Deleted ${name} Groovy")
    }
    
}
