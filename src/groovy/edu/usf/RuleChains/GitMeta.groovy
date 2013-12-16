/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.usf.RuleChains

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.InitCommand
import org.eclipse.jgit.api.CloneCommand
import org.eclipse.jgit.api.FetchCommand
import org.eclipse.jgit.api.LsRemoteCommand
import org.eclipse.jgit.api.PullCommand
import org.eclipse.jgit.api.PushCommand
import org.eclipse.jgit.api.SubmoduleSyncCommand
import org.eclipse.jgit.api.RmCommand
import org.eclipse.jgit.api.CheckoutCommand
import org.eclipse.jgit.api.StatusCommand
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.eclipse.jgit.api.errors.GitAPIException
import grails.converters.*
import edu.usf.RuleChains.*
import org.hibernate.FlushMode
import grails.util.Holders
import edu.usf.RuleChains.ChainJobListener

/**
 *
 * @author james
 */

class GitMeta {
    def gitRepository = null
    def buildMeta = { grailsApplication,usfCasService ->
        def localRepoFolder = new File(grailsApplication.mainContext.getResource('/').file.absolutePath + '/git/')
        localRepoFolder.deleteDir()
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder.setGitDir(localRepoFolder)
            .readEnvironment().findGitDir().setup().build();
        
        
        
//        def command = Git.init()
//        command.directory = new File(grailsApplication.mainContext.getResource('/').file.absolutePath + '/git/')

//        def repository
        def resolveEmail = { username ->
            if(Holders.config.gitConfig.cas.fallbackMap[username]) {
                return Holders.config.gitConfig.cas.fallbackMap[username]
            } else {
                try {
                    def email = usfCasService.attributes[Holders.config.gitConfig.cas.emailAttribute]
                    if(!!!!email) {
                        return email
                    }
                    return Holders.config.gitConfig.fallbackEmailDefault
                } catch(e) {
                    // use the default
                    return Holders.config.gitConfig.fallbackEmailDefault
                }
            }
        }
        def resolveUsername = {->
            try {
                def username = usfCasService.getUsername()
                if(!!!!username) {
                    return username
                }
                return Holders.config.gitConfig.fallbackUsername
            } catch(e) {
                return Holders.config.gitConfig.fallbackUsername
            }
        }
        CloneCommand clone = Git.cloneRepository()
//        clone.setBare(false).setCloneAllBranches(true) // This works,  now I'm working on selecting a specific branch
        clone.setBare(false).setBranch(Holders.config.gitConfig.branch)
        // clone.setDirectory(f).setURI("git@192.168.2.43:test.git")
        clone.setDirectory(localRepoFolder).setURI(Holders.config.gitConfig.gitRemoteURL);
        UsernamePasswordCredentialsProvider user = new UsernamePasswordCredentialsProvider(Holders.config.gitConfig.gitRemotelogin, Holders.config.gitConfig.gitRemotePassword);                
        clone.setCredentialsProvider(user);
        Git git
        try {
            git = clone.call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        PushCommand push = git.push()
        push.setCredentialsProvider(user)
        push.setRemote(Holders.config.gitConfig.gitRemoteURL)
        PullCommand pull = git.pull()
        pull.setCredentialsProvider(user)
        // pull.setRemote(Holders.config.gitConfig.gitRemoteURL)
        RmCommand rm = git.rm()        
        SubmoduleSyncCommand sync = git.submoduleSync()
        
        gitRepository = repository  
        def metaClasses = []        
        for (domainClass in grailsApplication.domainClasses.findAll { sc -> sc.name in ['DefinedService','SQLQuery','Snippet','StoredProcedureQuery','PHP','Groovy']}) {
            domainClass.metaClass.deleteGitWithComment {comment ->
                def relativePath = "ruleSets/${delegate.getPersistentValue('ruleSet').name}/${delegate.name}.json"
                pull.call()
                def f = new File("${localRepoFolder.absolutePath}/ruleSets/${delegate.getPersistentValue('ruleSet').name}/${delegate.name}.json")
                if(f.exists()) {
                    git.rm().addFilepattern("${relativePath}").call()
                    f.delete()
                }
                if(!git.status().call().isClean()) {
                    git.commit().setAuthor(resolveUsername.call(),resolveEmail.call(resolveUsername.call())).setMessage(comment).call()
                }
                push.call()
                pull.call()
            }
            domainClass.metaClass.updateGitWithComment {comment ->
                def relativePath = "ruleSets/${delegate.ruleSet.name}/${delegate.name}.json"
                def oldRelativePath = "ruleSets/${delegate.ruleSet.name}/${delegate.getPersistentValue("name")}.json"
                pull.call()
                if (delegate.isDirty('name')) {
                    def f = new File("${localRepoFolder.absolutePath}/ruleSets/${delegate.ruleSet.name}/${delegate.getPersistentValue("name")}.json")
                    if(f.exists()) {                    
                        f.renameTo(new File("${localRepoFolder.absolutePath}/ruleSets/${delegate.ruleSet.name}/${delegate.name}.json"))
                        git.add().addFilepattern("${relativePath}").call()
                        git.rm().addFilepattern("${oldRelativePath}").call()
                        if(!git.status().call().isClean()) {
                            git.commit().setAuthor(resolveUsername.call(),resolveEmail.call(resolveUsername.call())).setMessage(comment).call()                            
                        }
                        push.call();
                        pull.call()
                    }
                }
            }
            domainClass.metaClass.saveGitWithComment {comment ->
                def relativePath = "ruleSets/${delegate.ruleSet.name}/${delegate.name}.json"
                def f = new File("${localRepoFolder.absolutePath}/ruleSets/${delegate.ruleSet.name}/${delegate.name}.json")
                pull.call()
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
                git.add().addFilepattern("${relativePath}").call()
                if(!git.status().call().isClean()) {
                    git.commit().setAuthor(resolveUsername.call(),resolveEmail.call(resolveUsername.call())).setMessage(comment).call()
                }
                push.call()
                pull.call()
            }       
        }
        RuleSet.metaClass.deleteGitWithComment  = {comment->  
            def relativePath = "ruleSets/${delegate.name}/"
            pull.call()
            new File("${localRepoFolder.absolutePath}/ruleSets/${delegate.name}/").deleteDir()
            git.rm().addFilepattern("${relativePath}").call()
            if(!git.status().call().isClean()) {
                git.commit().setAuthor(resolveUsername.call(),resolveEmail.call(resolveUsername.call())).setMessage(comment).call()
            }
            push.call()
            pull.call()            
        }
        RuleSet.metaClass.updateGitWithComment = {comment ->
            def relativePath = "ruleSets/${delegate.name}/"
            def f = new File("${localRepoFolder.absolutePath}/ruleSets/${delegate.name}/")
            pull.call()
            if (delegate.isDirty('name')) {
                def oldRelativePath = "ruleSets/${delegate.getPersistentValue("name")}/"
                def of = new File("${localRepoFolder.absolutePath}/ruleSets/${delegate.getPersistentValue("name")}/")
                if(of.exists()) {
                    of.renameTo(f)
                    git.add().addFilepattern("${relativePath}").call()
                    git.rm().addFilepattern("${oldRelativePath}").call()
                } else {
                    if(!f.exists()) {
                        f.mkdirs()
                    }
                    git.add().addFilepattern("${relativePath}").call()
                }
                if(!git.status().call().isClean()) {
                    git.commit().setAuthor(resolveUsername.call(),resolveEmail.call(resolveUsername.call())).setMessage(comment).call()
                }
                push.call()
                pull.call()            
            }
        }
        RuleSet.metaClass.saveGitWithComment = {comment ->
            def relativePath = "ruleSets/${delegate.name}/"
            def f = new File("${localRepoFolder.absolutePath}/${relativePath}")
            pull.call()
            if(!f.exists()) {
                f.mkdirs()
            }            
            git.add().addFilepattern("${relativePath}").call()
            if(!git.status().call().isClean()) {
                git.commit().setAuthor(resolveUsername.call(),resolveEmail.call(resolveUsername.call())).setMessage(comment).call()
            }
            push.call()
            pull.call()
        }
        Chain.metaClass.deleteGitWithComment  = {comment->  
            def relativePath = "chains/${delegate.name}/"
            new File("${localRepoFolder.absolutePath}/chains/${delegate.name}/").deleteDir()
            pull.call()
            git.rm().addFilepattern("${relativePath}").call()
            if(!git.status().call().isClean()) {
                git.commit().setAuthor(resolveUsername.call(),resolveEmail.call(resolveUsername.call())).setMessage(comment).call()
            }
            push.call();
        }
        Chain.metaClass.updateGitWithComment = {comment ->
            def relativePath = "chains/${delegate.name}/"
            def f = new File("${localRepoFolder.absolutePath}/chains/${delegate.name}/")
            pull.call()
            if (delegate.isDirty('name')) {
                def oldRelativePath = "chains/${delegate.getPersistentValue("name")}/"
                def of = new File("${localRepoFolder.absolutePath}/chains/${delegate.getPersistentValue("name")}/")
                if(of.exists()) {
                    of.renameTo(f)
                    git.add().addFilepattern("${relativePath}").call()
                    git.rm().addFilepattern("${oldRelativePath}").call()                
                } else {
                    if(!f.exists()) {
                        f.mkdirs()
                    }
                    git.add().addFilepattern("${relativePath}").call()
                }
                if(!git.status().call().isClean()) {
                    git.commit().setAuthor(resolveUsername.call(),resolveEmail.call(resolveUsername.call())).setMessage(comment).call()
                }
                push.call()
                pull.call()
            }
        }
        Chain.metaClass.saveGitWithComment = {comment ->
            def relativePath = "chains/${delegate.name}/"
            def f = new File("${localRepoFolder.absolutePath}/chains/${delegate.name}/")
            pull.call()
            if(!f.exists()) {
                f.mkdirs()
            }         
            git.add().addFilepattern("${relativePath}").call()
            git.add().addFilepattern(".").call()
            if(!git.status().call().isClean()) {
                git.commit().setAuthor(resolveUsername.call(),resolveEmail.call(resolveUsername.call())).setMessage(comment).call()
            }
            push.call()
            pull.call()
        }
        ChainServiceHandler.metaClass.deleteGitWithComment {comment ->
            def relativePath = "chainServiceHandlers/${delegate.name}.json"
            pull.call()
            def f = new File("${localRepoFolder.absolutePath}/chainServiceHandlers/${delegate.name}.json")
            if(f.exists()) {
                f.delete()
                git.rm().addFilepattern("${relativePath}").call()
                if(!git.status().call().isClean()) {
                    git.commit().setAuthor(resolveUsername.call(),resolveEmail.call(resolveUsername.call())).setMessage(comment).call()
                }
                push.call()
            }
            pull.call()
        }
        ChainServiceHandler.metaClass.updateGitWithComment = {comment ->
            def relativePath = "chainServiceHandlers/${delegate.name}.json"
            pull.call()
            if (delegate.isDirty('name')) {
                def oldRelativePath = "chainServiceHandlers/${delegate.getPersistentValue("name")}.json"
                def f = new File("${localRepoFolder.absolutePath}/chainServiceHandlers/${delegate.getPersistentValue("name")}.json")
                if(f.exists()) {
                    f.renameTo(new File("${localRepoFolder.absolutePath}/chainServiceHandlers/${delegate.name}.json"))
                    git.add().addFilepattern("${relativePath}").call()
                    git.rm().addFilepattern("${oldRelativePath}").call()                
                    if(!git.status().call().isClean()) {
                        git.commit().setAuthor(resolveUsername.call(),resolveEmail.call(resolveUsername.call())).setMessage(comment).call()
                    }
                    push.call()
                    pull.call()
                }
            }
        }
        ChainServiceHandler.metaClass.saveGitWithComment {comment ->
            def relativePath = "chainServiceHandlers/${delegate.name}.json"
            new File("${localRepoFolder.absolutePath}/chainServiceHandlers/").mkdirs()
            def f = new File("${localRepoFolder.absolutePath}/chainServiceHandlers/${delegate.name}.json")
            pull.call()
            f.text = {j->
                j.setPrettyPrint(true)
                return j
            }.call([
                name: delegate.name,
                chain: delegate.chain.name,
                inputReorder: delegate.inputReorder,
                outputReorder: delegate.outputReorder,
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
                }.call(delegate.method)
            ] as JSON)
            git.add().addFilepattern("${relativePath}").call()
            git.add().addFilepattern(".").call()
            if(!git.status().call().isClean()) {
                git.commit().setAuthor(resolveUsername.call(),resolveEmail.call(resolveUsername.call())).setMessage(comment).call()
            }
            push.call()
            pull.call()
        }
        JobService.metaClass.getGitAuthorInfo { ->
            def gitAuthorInfo = [ user: resolveUsername.call() ]
            gitAuthorInfo.email = resolveEmail.call(gitAuthorInfo.user)
            return gitAuthorInfo
        }
        RuleChainsJobListener.metaClass.deleteGitWithComment {context,comment ->
            def jobKey = context.getJobDetail().getKey()
            pull.call()
            def relativePath = "jobs/${jobKey.name}.json"
            def dataMap = context.getMergedJobDataMap()
            def gitAuthorInfo = dataMap.get("gitAuthorInfo")            
            def f = new File("${localRepoFolder.absolutePath}/jobs/${jobKey.name}.json")
            if(f.exists()) {
                f.delete()
                git.rm().addFilepattern("${relativePath}").call()
                if(!git.status().call().isClean()) {
                    git.commit().setAuthor(gitAuthorInfo.user,gitAuthorInfo.email).setMessage(comment).call()
                }
                push.call()
            }
            pull.call()
        }    
        RuleChainsJobListener.metaClass.saveGitWithComment {context,comment ->
            def jobFolder = new File("${localRepoFolder.absolutePath}/jobs/")
            if(!jobFolder.exists()) {
                jobFolder.mkdirs()
            }
            pull.call()
            def jobKey = context.getJobDetail().getKey()
            println jobKey.name
            println jobKey
            def dataMap = context.getMergedJobDataMap()
            def gitAuthorInfo = dataMap.get("gitAuthorInfo")
            def relativePath = "jobs/${jobKey.name}.json"
            def f = new File("${localRepoFolder.absolutePath}/jobs/${jobKey.name}.json")
            f.text = {js->
                js.setPrettyPrint(true)
                return js                            
            }.call([
                group: jobKey.group,
                name: jobKey.name,
                triggers: context.getScheduler().getTriggersOfJob(jobKey).collect { it.getCronExpression() },
                chain: dataMap.getString("chain"),
                input: dataMap.get("input")
            ] as JSON)
            git.add().addFilepattern("${relativePath}").call()
            if(!git.status().call().isClean()) {
                git.commit().setAuthor(gitAuthorInfo.user,gitAuthorInfo.email).setMessage(comment).call()
            }
            push.call()
            pull.call() 
        }
        Link.metaClass.deleteGitWithComment {comment ->
            pull.call()
            new File("${localRepoFolder.absolutePath}/chains/${delegate.getPersistentValue('chain').name}/${delegate.sequenceNumber}.json").delete()
            git.add().addFilepattern(".").call()
            if(!git.status().call().isClean()) {
                git.commit().setAuthor(resolveUsername.call(),resolveEmail.call(resolveUsername.call())).setMessage(comment).call()
            }
            push.call()
            pull.call()
        }
        Link.metaClass.updateGitWithComment {comment ->
            def relativePath = "chains/${delegate.chain.name}/${delegate.sequenceNumber}.json"
            pull.call()
            if (delegate.isDirty('name')) {
                def f = new File("${localRepoFolder.absolutePath}/chains/${delegate.chain.name}/${delegate.getPersistentValue("sequenceNumber")}.json")
                if(f.exists()) {
                    f.renameTo(new File("${localRepoFolder.absolutePath}/chains/${delegate.chain.name}/${delegate.sequenceNumber}.json"))
                    git.add().addFilepattern("${relativePath}").call()
                    git.add().addFilepattern(".").call()
                    if(!git.status().call().isClean()) {
                        git.commit().setAuthor(resolveUsername.call(),resolveEmail.call(resolveUsername.call())).setMessage(comment).call()
                    }
                    push.call()
                    pull.call()
                }                
            }
        }
        Link.metaClass.saveGitWithComment {comment ->
            def relativePath = "chains/${delegate.chain.name}/${delegate.sequenceNumber}.json"
            def f = new File("${localRepoFolder.absolutePath}/chains/${delegate.chain.name}/${delegate.sequenceNumber}.json")
            pull.call()
            f.text = {j->
                j.setPrettyPrint(true)
                return j
            }.call([
                sequenceNumber: delegate.sequenceNumber,
                sourceName: delegate.sourceName,
                inputReorder: delegate.inputReorder,
                outputReorder: delegate.outputReorder,
                executeEnum:{ e->
                    switch(e) {
                        case ExecuteEnum.EXECUTE_USING_ROW:
                            return "EXECUTE_USING_ROW"
                            break
                        case ExecuteEnum.NORMAL:
                            return "NORMAL"
                            break
                    }                                
                }.call(delegate.executeEnum),
                resultEnum:{ r->
                    switch(r) {
                        case ResultEnum.NONE:
                            return "NONE"
                            break
                        case ResultEnum.UPDATE:
                            return "UPDATE"
                            break
                        case ResultEnum.RECORDSET:
                            return "RECORDSET"
                            break
                        case ResultEnum.ROW:
                            return "ROW"
                            break
                        case ResultEnum.APPENDTOROW:
                            return "APPENDTOROW"
                            break
                        case ResultEnum.PREPENDTOROW:
                            return "PREPENDTOROW"
                            break
                    }                                
                }.call(delegate.resultEnum),
                linkEnum:{ l->
                    switch(l) {
                        case LinkEnum.NONE:
                            return "NONE"
                            break
                        case LinkEnum.LOOP:
                            return "LOOP"
                            break
                        case LinkEnum.ENDLOOP:
                            return "ENDLOOP"
                            break
                        case LinkEnum.NEXT:
                            return "NEXT"
                            break
                    }                                
                }.call(delegate.linkEnum),
                rule: delegate.rule.name,
                "class": delegate['class']
            ] as JSON) 
            git.add().addFilepattern("${relativePath}").call()
            git.add().addFilepattern(".").call()
            if(!git.status().call().isClean()) {
                git.commit().setAuthor(resolveUsername.call(),resolveEmail.call(resolveUsername.call())).setMessage(comment).call()
            }
            push.call()
            pull.call()
        }
    }
}

