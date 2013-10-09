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
    def addChain(String name) {
        if(!!name) {
            def chain = [ name: name.trim() ] as Chain
            if(!chain.save(failOnError:false, flush: true, insert: true, validate: true)) {
                return [ error : "'${chain.errors.fieldError.field}' value '${chain.errors.fieldError.rejectedValue}' rejected" ]
            } else {
                return getChain(name.trim())
            }
        }
        return [ error: "You must supply a name" ]
    }
    def modifyChain(String name,String newName) {
        if(!!name && !!newName) {
            def chain = Chain.findByName(name.trim())
            if(!!chain) {                
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
    def deleteChain(String name) {
        if(!!name) {
            def chain = Chain.findByName(name.trim())
            if(!!chain) {
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
                resultSet << chain.properties.subMap(['name','links'])
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
    def addChainLink(String name,def newLink) {
        def chain = Chain.findByName(name.trim())
        if(!!chain) {
            def tempChain = addChain("${(new Date()).time}")
            if('chain' in tempChain) {
                tempChain.chain = Chain.findByName(tempChain.chain.name)
                def rule = Rule.findByName(newLink.rule.name)
                if(!!rule) {
                    newLink.rule = rule
                    Link link = new Link(newLink.collectEntries {
                        switch(it.key) {
                            case "executeEnum":
                                return [ "${it.key}": ExecuteEnum.byName((("name" in it.value)?it.value.name:it.value).tokenize('.').last()) ]
                                break
                            case "resultEnum":
                                return [ "${it.key}": ResultEnum.byName((("name" in it.value)?it.value.name:it.value).tokenize('.').last()) ]
                                break
                            case "linkEnum":
                                return [ "${it.key}": LinkEnum.byName((("name" in it.value)?it.value.name:it.value).tokenize('.').last()) ]
                                break
                            default:
                                return [ "${it.key}": it.value ]
                                break
                        }
                    })
                    def sequenceNumber = 1
                    def inserted = false
                    Link.createCriteria().list(sort: 'sequenceNumber',order: 'asc') {
                        eq('chain',chain)
                    }.each{ l -> 
                        if(l.sequenceNumber >= link.sequenceNumber && !inserted) {
                            // insert the new link
                            link.sequenceNumber = sequenceNumber
                            try {
                                if(!tempChain.chain.addToLinks(link).save(failOnError:false, flush: false, validate: true)) {
                                    tempChain.chain.errors.allErrors.each {
                                        println "Error:"+it
                                    }           
                                    return [ error : "'${tempChain.chain.errors.fieldError.field}' value '${tempChain.chain.errors.fieldError.rejectedValue}' rejected" ]                                
                                }
                                sequenceNumber++                                
                                inserted = true
                            } catch(Exception ex) {
                                link.errors.allErrors.each {
                                    println it                        
                                }          
                                return [ error: "'${link.errors.fieldError.field}' value '${link.errors.fieldError.rejectedValue}' rejected" ]                
                            }
                            println "Added new link"
                        }
                        l.sequenceNumber = sequenceNumber
                        def cl = new Link(l.properties.subMap(['executeEnum','resultEnum','linkEnum','rule','sequenceNumber','sourceName','inputReorder','outputReorder']))
                        try {
                            if(!tempChain.chain.addToLinks(cl).save(failOnError:false, flush: false, validate: true)) {
                                tempChain.chain.errors.allErrors.each {
                                    println "Error:"+it
                                }           
                                return [ error : "'${tempChain.chain.errors.fieldError.field}' value '${tempChain.chain.errors.fieldError.rejectedValue}' rejected" ]                                
                            }
                            sequenceNumber++
                        } catch(Exception ex) {
                            cl.errors.allErrors.each {
                                println it                        
                            }   
                            log.info ex.printStackTrace()
                            return [ error: "'${cl.errors.fieldError.field}' value '${cl.errors.fieldError.rejectedValue}' rejected" ]                
                        }
                        println "Adding existing link back"
                    }
                    if(!inserted) {
                        // must be the last entry
                        // insert the new link
                        link.sequenceNumber = sequenceNumber
                        try {
                            if(!tempChain.chain.addToLinks(link).save(failOnError:false, flush: false, validate: true)) {
                                tempChain.chain.errors.allErrors.each {
                                    println "Error:"+it
                                }           
                                return [ error : "'${tempChain.chain.errors.fieldError.field}' value '${tempChain.chain.errors.fieldError.rejectedValue}' rejected" ]                                
                            }
                            inserted = true
                        } catch(Exception ex) {
                            link.errors.allErrors.each {
                                println it                        
                            }    
                            log.info ex.printStackTrace()
                            return [ error: "'${link.errors.fieldError.field}' value '${link.errors.fieldError.rejectedValue}' rejected" ]                
                        }    
                        println "Added new link (last chance)"
                    }
                    // Flipping the names and removing the old chain
                    // Removing original chain
                    deleteChain(name.trim())
                    // Returning the renamed temporary chain with the original name
                    return modifyChain(tempChain.chain.name,name.trim())
                }
                return [ error: "The rule specified in the new link named ${newLink.rule.name} doesn't exist"]
            }
            return [ error : "Could not create the temporary chain" ]
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
    def deleteChainLink(String name,def sequenceNumber) {
        def chain = Chain.findByName(name.trim())
        if(!!chain) {
            def link = Link.createCriteria().get {
                eq("chain",chain)
                eq("sequenceNumber",sequenceNumber.toLong())
            }
            if(!!link) {
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
