package edu.usf.RuleChains

class SQLQuery extends Rule {
    String rule = ""
    static constraints = {
    }
    static mapping = {
        rule type: 'text'
    }
    def afterInsert() {
        saveGitWithComment("Creating ${name} SQLQuery")
    }
    def beforeUpdate() {
        updateGitWithComment("Renaming ${name} SQLQuery")
    }
    def afterUpdate() {
        saveGitWithComment("Updating ${name} SQLQuery")
    }
    def beforeDelete() {
        deleteGitWithComment("Deleted ${name} SQLQuery")
    }
}
