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
    }
    def beforeDelete() {
        deleteGitWithComment("Deleted ${name} Snippet")
    }
}
