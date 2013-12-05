package edu.usf.RuleChains

class Snippet extends Rule {
    Chain chain
    static constraints = {
        chain(nullable:true)
    }
    
    def afterInsert() {
        saveGitWithComment("Creating ${name} Snippet")
    }
    def beforeUpdate() {
        updateGitWithComment("Renaming ${name} Snippet")
    }
    def afterUpdate() {
        saveGitWithComment("Updating ${name} Snippet")
        /**
         * Anytime a rule is renamed, any link referenced rule name in git repo needs to be updated (if exists)
         **/
        Link.findAllByRule(this).each { l ->
            l.saveGitWithComment("Updating Link referencing ${name} SQLQuery")
        }        
    }
    def beforeDelete() {
        deleteGitWithComment("Deleted ${name} Snippet")
    }
}
