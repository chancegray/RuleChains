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
import grails.converters.*
import edu.usf.RuleChains.*
import org.hibernate.FlushMode

/**
 *
 * @author james
 */

class GitMeta {
    def gitRepository = null
    def buildMeta = { grailsApplication ->
        def command = Git.init()
        command.directory = new File(grailsApplication.mainContext.getResource('/').file.absolutePath + '/git/')

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
        def metaClasses = []
        for (domainClass in grailsApplication.domainClasses.findAll { sc -> sc.name in ['DefinedService','SQLQuery','Snippet','StoredProcedureQuery','PHP','Groovy']}) {
            domainClass.metaClass.deleteGitWithComment {comment ->
                new File("${command.directory.absolutePath}/ruleSets/${delegate.getPersistentValue('ruleSet').name}/${delegate.name}.json").delete()
                git.commit().setMessage(comment).call()
            }
            domainClass.metaClass.updateGitWithComment {comment ->
                if (delegate.isDirty('name')) {
                    def f = new File("${command.directory.absolutePath}/ruleSets/${delegate.ruleSet.name}/${delegate.getPersistentValue("name")}.json")
                    if(f.exists()) {
                        f.renameTo(new File("${command.directory.absolutePath}/ruleSets/${delegate.ruleSet.name}/${delegate.name}.json"))
                        git.commit().setMessage(comment).call()
                    }
                }
            }
            domainClass.metaClass.saveGitWithComment {comment ->
                def f = new File("${command.directory.absolutePath}/ruleSets/${delegate.ruleSet.name}/${delegate.name}.json")
                switch(delegate) {
                    case { it instanceof SQLQuery }:
                        println "Writing SQLQuery File ${delegate.name}.json"
                        f.text = {j->
                            j.setPrettyPrint(true)
                            return j
                        }.call([
                            name: delegate.name,
                            rule: delegate?.rule,
                            "class": delegate['class']
                        ] as JSON)                        
                        break
                    case { it instanceof Groovy }:
                        println "Writing Groovy File ${delegate.name}.json"
                        f.text = {j->
                            j.setPrettyPrint(true)
                            return j
                        }.call([
                            name: delegate.name,
                            rule: delegate?.rule,
                            "class": delegate['class']
                        ] as JSON)                        
                        break
                    case { it instanceof PHP }:
                        println "Writing PHP File ${delegate.name}.json"
                        f.text = {j->
                            j.setPrettyPrint(true)
                            return j
                        }.call([
                            name: delegate.name,
                            rule: delegate?.rule,
                            "class": delegate['class']
                        ] as JSON)                        
                        break
                    case { it instanceof StoredProcedureQuery }:
                        println "Writing StoredProcedureQuery File ${delegate.name}.json"
                        f.text = {j->
                            j.setPrettyPrint(true)
                            return j
                        }.call([
                            name: delegate.name,
                            rule: delegate?.rule,
                            "class": delegate['class'],
                            "closure": delegate['closure']
                        ] as JSON)                        
                        break
                    case { it instanceof DefinedService }:  
                        println "Writing DefinedService File ${delegate.name}.json"
                        def ds = delegate as JSON
                        println ds
                        f.text = {j->
                            j.setPrettyPrint(true)
                            return j
                        }.call([
                            name: delegate.name,
                            method:{ m->
                                switch(m) {
                                    case MethodEnum.GET:
                                        return "GET"
                                        break
                                    case MethodEnum.POST:
                                        return "POST"
                                        break
                                    case MethodEnum.PUT:
                                        return "PUT"
                                        break
                                    case MethodEnum.DELETE:
                                        return "DELETE"
                                        break
                                }                                
                            }.call(delegate.method),
                            authType:{ t->
                                switch(t) {
                                    case AuthTypeEnum.NONE:
                                        return "NONE"
                                        break
                                    case AuthTypeEnum.BASIC:
                                        return "BASIC"
                                        break
                                    case AuthTypeEnum.DIGEST:
                                        return "DIGEST"
                                        break
                                    case AuthTypeEnum.CAS:
                                        return "CAS"
                                        break
                                    case AuthTypeEnum.CASSPRING:
                                        return "CASSPRING"
                                        break
                                }                                
                            }.call(delegate.authType),
                            parse:{ p->
                                switch(p) {
                                    case ParseEnum.TEXT:
                                        return "TEXT"
                                        break
                                    case ParseEnum.XML:
                                        return "XML"
                                        break
                                    case ParseEnum.JSON:
                                        return "JSON"
                                        break
                                }                                
                            }.call(delegate.parse),
                            url: delegate.url,
                            springSecurityBaseURL: delegate.springSecurityBaseURL,
                            user: delegate.user,
                            password: delegate.password,
                            "class": delegate['class']
                        ] as JSON)    
                        println "DONE!"
                        break
                    case { it instanceof Snippet }:
                        println "Writing Snippet File ${delegate.name}.json"
                        f.text = {j->
                            j.setPrettyPrint(true)
                            return j
                        }.call([
                            name: delegate.name,
                            chain: delegate?.chain?.name,
                            "class": delegate['class']
                        ] as JSON)                        
                        break
                }
                git.commit().setMessage(comment).call()
            }       
        }
        RuleSet.metaClass.deleteGitWithComment  = {comment->  
            new File("${command.directory.absolutePath}/ruleSets/${delegate.name}/").deleteDir()
            git.commit().setMessage(comment).call()
        }
        RuleSet.metaClass.updateGitWithComment = {comment ->
            def of = new File("${command.directory.absolutePath}/ruleSets/${delegate.getPersistentValue("name")}/")
            def f = new File("${command.directory.absolutePath}/ruleSets/${delegate.name}/")
            if(of.exists()) {
                of.renameTo(f)
            } else if(!f.exists()) {
                f.mkdirs()
            }
            git.commit().setMessage(comment).call()
        }
        RuleSet.metaClass.saveGitWithComment = {comment ->
            def f = new File("${command.directory.absolutePath}/ruleSets/${delegate.name}/")
            if(!f.exists()) {
                f.mkdirs()
            }            
            git.commit().setMessage(comment).call()
        }
        Chain.metaClass.deleteGitWithComment  = {comment->  
            new File("${command.directory.absolutePath}/chains/${delegate.name}/").deleteDir()
            git.commit().setMessage(comment).call()
        }
        Chain.metaClass.updateGitWithComment = {comment ->
            def of = new File("${command.directory.absolutePath}/chains/${delegate.getPersistentValue("name")}/")
            def f = new File("${command.directory.absolutePath}/chains/${delegate.name}/")
            if(of.exists()) {
                of.renameTo(f)
            } else if(!f.exists()) {
                f.mkdirs()
            }
            git.commit().setMessage(comment).call()
        }
        Chain.metaClass.saveGitWithComment = {comment ->
            def f = new File("${command.directory.absolutePath}/chains/${delegate.name}/")
            if(!f.exists()) {
                f.mkdirs()
            }            
            git.commit().setMessage(comment).call()
        }
        
    }
}

