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
        /**
         * Anytime a rule is renamed, any link referenced rule name in git repo needs to be updated (if exists)
         **/
        Link.findAllByRule(this).each { l ->
            l.saveGitWithComment("Updating Link referencing ${name} SQLQuery")
        }
    }
    def beforeDelete() {
        deleteGitWithComment("Deleted ${name} Groovy")
    }
    
}
