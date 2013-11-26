/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.usf.RuleChains

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.InitCommand
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.lib.Repository

/**
 *
 * @author james
 */

class GitMeta {
    def gitRepository = null
    def buildMeta = { baseFolder ->
        def command = Git.init()
        command.directory = new File(baseFolder + '/git/')

        def repository
        
        try {
            repository = command.call().repository
            println "Initialised empty git repository for the project."
        }
        catch (Exception ex) {
            println "Unable to initialise git repository - ${ex.message}"
            exit 1
        }
        
        // Now commit the files that aren't ignored to the repository.
        def git = new Git(repository)
        git.add().addFilepattern(".").call()
        git.commit().setMessage("Initial commit of RuleChains code sources.").call()
        println "Committed initial code to the git repository."

        gitRepository = repository       
        
        
    }
}

