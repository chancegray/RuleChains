package edu.usf.RuleChains

class Snippet extends Rule {
    Chain chain
    static constraints = {
        chain(nullable:true)
    }
    
    def afterInsert() {
        if(isSynced) {
            saveGitWithComment("Creating ${name} Snippet")
        }
    }
    def beforeUpdate() {
        if(isSynced) {
            updateGitWithComment("Renaming ${name} Snippet")
        }
    }
    def afterUpdate() {
        if(isSynced) {
            saveGitWithComment("Updating ${name} Snippet")
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
            deleteGitWithComment("Deleted ${name} Snippet")
        }
    }
}
