package edu.usf.RuleChains

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import grails.converters.*
import org.hibernate.criterion.CriteriaSpecification

class ChainService {
    static transactional = true
    def grailsApplication
    def jobService
    
    def listChains(String pattern = null) { 
        if(!!pattern) {
            return [chains: Chain.list().findAll(fetch:[links:"eager"]) {
                Pattern.compile(pattern.trim()).matcher(it.name).matches()
            }]
        } else {
            return [ chains: Chain.list() ]
        }
    }
    def addChain(String name,boolean isSynced = true) {
        if(!!name) {
            def chain = [ name: name.trim() ] as Chain
            chain.isSynced = isSynced
            if(!chain.save(failOnError:false, flush: true, insert: true, validate: true)) {
                return [ error : "'${chain.errors.fieldError.field}' value '${chain.errors.fieldError.rejectedValue}' rejected" ]
            } else {
                return getChain(name.trim())
            }
        }
        return [ error: "You must supply a name" ]
    }
    def modifyChain(String name,String newName,boolean isSynced = true) {
        if(!!name && !!newName) {
            def chain = Chain.findByName(name.trim())
            if(!!chain) {         
                chain.isSynced = isSynced
                System.out.println(newName)
                chain.name = newName.trim()
                if(!chain.save(failOnError:false, flush: true, validate: true)) {
                    return [ error : "'${chain.errors.fieldError.field}' value '${chain.errors.fieldError.rejectedValue}' rejected" ]
                } else {
                    return getChain(newName.trim())
                }
            }
            return [ error : "Chain named ${name} not found!"]
        }
        return [ error : "You must supply a name and new name for the target chain"]
    }
    def deleteChain(String name,boolean isSynced = true) {
        if(!!name) {
            def chain = Chain.findByName(name.trim())
            if(!!chain) {
                chain.isSynced = isSynced
                chain.delete()
                return [ success : "Chain deleted" ]
            }
            return [ error : "Chain named ${name} not found!"]
        }
        return [ error : "You must supply a name for the target Chain"]
    }
    def getChain(String name) {
        if(!!name) {
            def chain = Chain.findByName(name.trim())
            if(!!chain) {
                def resultSet = [:]
                resultSet << chain.properties.subMap(['name','links','id'])
                if(!!!!resultSet.links) {
                    resultSet.links = Link.createCriteria().list(sort: 'sequenceNumber',order: 'asc') {
                        eq('chain',chain)
                        resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                        projections {
                            property('sequenceNumber', 'sequenceNumber')
                            property('rule', 'rule')
                            property('sourceName','sourceName')
                            property('executeEnum', 'executeEnum')
                            property('resultEnum', 'resultEnum')
                            property('linkEnum', 'linkEnum')
                            property('inputReorder', 'inputReorder')
                            property('outputReorder', 'outputReorder')
                        }                        
                    }
                }
                return [ chain: resultSet ]                    
            }
            return [ error : "Chain named ${name} not found!"]
        }
        return [ error : "You must supply a name for the target Chain"]
    }
    def addChainLink(String name,def newLink,boolean isSynced = true) {
        def chain = Chain.findByName(name.trim())
        if(!!chain) {
            chain.isSynced = isSynced
            // Get an ordered list of links
            Link.createCriteria().list(sort: 'sequenceNumber',order: 'desc') {
                eq('chain',chain)
                ge('sequenceNumber',newLink.sequenceNumber.toLong())
            }.each { l ->
                l.isSynced = isSynced
                // increment each one
                l.sequenceNumber++
                if(!l.save(failOnError:false, flush: true, validate: true)) {
                    return [ error : "'${l.errors.fieldError.field}' value '${l.errors.fieldError.rejectedValue}' rejected" ]
                }
            }
            def link = new Link(newLink.inject([:]) {l,k,v ->
                switch(k) {
                    case "executeEnum":
                        l[k] = ExecuteEnum.byName((("name" in v)?v.name:v).tokenize('.').last())
                        break
                    case "resultEnum":
                        l[k] = ResultEnum.byName((("name" in v)?v.name:v).tokenize('.').last())
                        break
                    case "linkEnum":
                        l[k] = LinkEnum.byName((("name" in v)?v.name:v).tokenize('.').last())
                        break
                    case "sequenceNumber":
                        l[k] = v.toLong()
                        break
                    case "rule":
                        l[k] = Rule.findByName(("name" in v)?v.name:v)
                        l[k].isSynced = isSynced
                        break
                    default:
                        l[k] = v
                        break                    
                }
                return l
            })
            link.isSynced = isSynced
            try {
                if(!chain.addToLinks(link).save(failOnError:false, flush: true, validate: true)) {
                    chain.errors.allErrors.each {
                        println "Error:"+it
                    }           
                    return [ error : "'${chain.errors.fieldError.field}' value '${chain.errors.fieldError.rejectedValue}' rejected" ]                                
                }
            } catch(Exception ex) {
                link.errors.allErrors.each {
                    println it                        
                }    
                log.info ex.printStackTrace()
                return [ error: "'${link.errors.fieldError.field}' value '${link.errors.fieldError.rejectedValue}' rejected" ]                
            }    
            return getChain(chain.name)
        }
        return [ error : "Chain named ${name} not found!"]
    }
    def getChainLink(String name,def sequenceNumber) {
        def chain = Chain.findByName(name.trim())
        if(!!chain) {
            def link = chain.links.find { it.sequenceNumber == sequenceNumber }
            if(!!link) {
                return [ link: link ]
            }
            return [ error : "Link sequence Number ${sequenceNumber} not found!"]
        }        
        return [ error : "Chain named ${name} not found!"]
    }
    def deleteChainLink(String name,def sequenceNumber,boolean isSynced = true) {
        def chain = Chain.findByName(name.trim())
        if(!!chain) {
            chain.isSynced = isSynced
            def link = Link.createCriteria().get {
                eq("chain",chain)
                eq("sequenceNumber",sequenceNumber.toLong())
            }
            if(!!link) {
                link.isSynced = isSynced
                if(!chain.removeFromLinks(link).save(failOnError:false, flush: false, validate: true)) {
                    chain.errors.allErrors.each {
                        println "Error:"+it
                    }           
                    return [ error : "'${chain.errors.fieldError.field}' value '${chain.errors.fieldError.rejectedValue}' rejected" ]                    
                } else {
                    link.delete()
                    def sequenceNumberIndex = 1
                    Link.createCriteria().list(sort: 'sequenceNumber',order: 'asc') {
                        eq('chain',chain)
                        ne('sequenceNumber',sequenceNumber.toLong())
                    }.each{ l -> 
                        l.sequenceNumber = sequenceNumberIndex
                        sequenceNumberIndex++
                        if(!l.save(failOnError:false, flush: true, validate: true)) {
                            l.errors.allErrors.each {
                                println it
                            }    
                            return [ error : "'${l.errors.fieldError.field}' value '${l.errors.fieldError.rejectedValue}' rejected" ]                
                        }
                    }
                    return getChain(name.trim())
                }
            }
            return [ error : "Link sequence Number ${sequenceNumber} not found!"]
        }
        return [ error : "Chain named ${name} not found!"]
    }
    def modifyChainLink(String name,def sequenceNumber,def updatedLink) {
        def chain = Chain.findByName(name.trim())
        if(!!chain) {
            println "Modify chain link ${sequenceNumber}"
            def link = chain.links.find { it.sequenceNumber.toString() == sequenceNumber }
            if(!!link) {
                println "Found Link"
                link.properties['sourceName','inputReorder','outputReorder','sequenceNumber','executeEnum','linkEnum','resultEnum'] = updatedLink.collectEntries {
                    if(it.key in ['executeEnum','linkEnum','resultEnum']) {
                        switch(it.key) {
                            case "executeEnum":
                                return [ "${it.key}": ("name" in it.value)?ExecuteEnum.byName(it.value.name):ExecuteEnum.byName(it.value) ]                                
                                break
                            case "linkEnum":
                                return [ "${it.key}": ("name" in it.value)?LinkEnum.byName(it.value.name):LinkEnum.byName(it.value) ]                                
                                break
                            case "resultEnum":
                                return [ "${it.key}": ("name" in it.value)?ResultEnum.byName(it.value.name):ResultEnum.byName(it.value) ]                                
                                break
                        }
                    }
                    return [ "${it.key}": it.value ]
                }
                link.rule = ("name" in updatedLink.rule)?Rule.findByName(updatedLink.rule.name):Rule.get(updatedLink.rule.id)
                if(!link.save(failOnError:false, flush: true, validate: true)) {
                    link.errors.allErrors.each {
                        println it
                    }           
                    return [ error : "'${link.errors.fieldError.field}' value '${link.errors.fieldError.rejectedValue}' rejected" ]                
                }
                return [ link : link]
            } else {
                println "Didn't Find link"
            }
            return [ error : "Link with sequence ${sequenceNumber} not found!"]
        }
        return [ error : "Chain named ${name} not found!"]
    }
    def getSources() {
        String sfRoot = "sessionFactory_"
        return [ 
            sources: grailsApplication.mainContext.beanDefinitionNames.findAll{ it.startsWith( sfRoot ) }.collect { sf ->
                sf[sfRoot.size()..-1]
            },
            actions: [
                execute: ExecuteEnum.values().collect { it.name() },
                result: ResultEnum.values().collect { it.name() },
                link: LinkEnum.values().collect { it.name() }
            ],
            jobGroups: jobService.listChainJobs().jobGroups,
            executingJobs: jobService.listCurrentlyExecutingJobs()?.executingJobs            
        ]
        
    }    
}
