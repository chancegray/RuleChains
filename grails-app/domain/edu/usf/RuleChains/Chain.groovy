package edu.usf.RuleChains
import groovy.lang.GroovyShell
import groovy.lang.Binding
import grails.util.GrailsNameUtils
import grails.converters.*
import edu.usf.RuleChains.*
import org.hibernate.FlushMode
import groovy.sql.Sql
import oracle.jdbc.driver.OracleTypes
import groovy.text.*

class Chain {
    String name
    List<Link> links
    List<List> input = [[:]]
    JobHistory jobHistory
    static hasMany = [links:Link]
    static transients = ['orderedLinks','input','output','jobHistory']
    static constraints = {
        name(   
                blank: false,
                nullable: false,
                size: 3..255,
                unique: true,
                //Custom constraint - only allow upper, lower, digits, dash and underscore
                validator: { val, obj -> 
                    val ==~ /[A-Za-z0-9_.-]+/ && {  
                        boolean valid = true;
                        Rule.withNewSession { session ->
                            session.flushMode = FlushMode.MANUAL
                            try {
                                def r = Rule.findByName(val)
                                valid = ((r instanceof Snippet)?!!!!r:!!!r) && !!!RuleSet.findByName(val) && !!!ChainServiceHandler.findByName(val)
                            } finally {
                                session.setFlushMode(FlushMode.AUTO)
                            }
                        }
                        return valid
                    }.call() 
                }
            )               
    }
    /**
     * Anytime a chain is renamed, snippet reference name needs to be renamed (if exists)
     **/
    def afterUpdate() {        
        Snippet.findAllByChain(this).each { s ->
            if(s.name != name) {
                s.name=name
                s.save(flush: true)
            }
        }
    }
    /**
     * Before a chain is deleted, snippet reference and any links using it need to be removed
     **/
    def beforeDelete() {
        Snippet.findAllByChain(this).each { s ->
            Link.findAllByRule(s).each { l ->
                (new ChainService()).deleteChainLink(l.chain.name,l.sequenceNumber)
            }
            (new RuleSetService()).deleteRule(s.ruleSet.name,s.name)
        }
    }
    def getOrderedLinks() {
        links.sort{it.sequenceNumber}
    }
    def getOutput() {
        getOrderedLinks().last().output
    }
    def execute(def input = [[:]],List<Link> orderedLinks) {
        println "I'm running"
        if(!!!orderedLinks) {
            orderedLinks = getOrderedLinks()
        }
        
        def linkService = new LinkService()
        input.each { row ->
//         for(def row in input) {
            /**
             * Pre-populate input based on incoming data array
            **/
            println "Made it this far with ${row}"
            for(int i = 0; i < orderedLinks.size(); i++) {
                orderedLinks[i].input = row
            }
            /**
             * Distinguish what kind of "rules" and handle them by type
            **/
            for(int i = 0; i < orderedLinks.size(); i++) {
                log.info "Unmodified input for link ${i} is ${orderedLinks[i].input as JSON}"
                // Execute the rule based on it's type
                log.info "Modified rearranged input for link ${i} is ${Chain.rearrange(orderedLinks[i].input,orderedLinks[i].inputReorder) as JSON}"

                switch(orderedLinks[i].rule) {
                    case { it instanceof SQLQuery }:
                        jobHistory.appendToLog("[SQLQuery] Detected a SQLQuery for ${orderedLinks[i].rule.name}")
                        log.info "Detected an SQLQuery for ${orderedLinks[i].rule.name}"
                        orderedLinks[i].output = linkService.justSQL(
                            { p ->
                                def gStringTemplateEngine = new GStringTemplateEngine()
                                def rule = [:]
                                rule << p
                                rule << [
                                    rule: gStringTemplateEngine.createTemplate(rule.rule).make(Chain.rearrange(orderedLinks[i].input,orderedLinks[i].inputReorder)).toString(),
                                    jobHistory: jobHistory
                                ]
                                log.info rule.rule
                                jobHistory.appendToLog("[SQLQuery] Untemplated Rule is: ${p.rule}")
                                jobHistory.appendToLog("[SQLQuery] Unmodified input for Templating Rule on link ${i} is ${orderedLinks[i].input as JSON}")
                                jobHistory.appendToLog("[SQLQuery] Modified input for Templating Rule on link ${i} is ${Chain.rearrange(orderedLinks[i].input,orderedLinks[i].inputReorder) as JSON}")
                                jobHistory.appendToLog("[SQLQuery] Templated Rule is: ${rule.rule}")
                                return rule
                            }.call(orderedLinks[i].rule.properties),
                            orderedLinks[i].sourceName,
                            orderedLinks[i].executeEnum,    
                            orderedLinks[i].resultEnum,
                            { e ->
                                switch(e) {
                                    case ExecuteEnum.EXECUTE_USING_ROW: 
                                        jobHistory.appendToLog("[SQLQuery] Execute Using Row being used")
                                        jobHistory.appendToLog("[SQLQuery] Unmodified input for Executing Row on link ${i} is ${orderedLinks[i].input as JSON}")
                                        jobHistory.appendToLog("[SQLQuery] Modified input for Executing Row link ${i} is ${Chain.rearrange(orderedLinks[i].input,orderedLinks[i].inputReorder) as JSON}")
                                        return Chain.rearrange(orderedLinks[i].input,orderedLinks[i].inputReorder)
                                        break
                                    default:
                                        return [:]
                                        break
                                }                                        
                            }.call(orderedLinks[i].executeEnum)
                        ).collect {
                            if(orderedLinks[i].resultEnum in [ResultEnum.APPENDTOROW]) {
                                return Chain.rearrange((([:] << orderedLinks[i].input) << it),orderedLinks[i].outputReorder)
                            } else if(orderedLinks[i].resultEnum in [ResultEnum.PREPENDTOROW]) {
                                return Chain.rearrange((([:] << it) << orderedLinks[i].input),orderedLinks[i].outputReorder)
                            }
                            return Chain.rearrange(it,orderedLinks[i].outputReorder)
                        }
                        break
                    case { it instanceof StoredProcedureQuery }:
                        jobHistory.appendToLog("[StoredProcedureQuery] Detected a Stored Procedure for ${orderedLinks[i].rule.name}")                        
                        log.info "Detected a StoredProcedureQuery Script for ${orderedLinks[i].rule.name}"
                        orderedLinks[i].output = linkService.justStoredProcedure(
                            { p ->
                                def gStringTemplateEngine = new GStringTemplateEngine()
                                def rule = [:]
                                rule << p
                                rule << [
                                    rule: gStringTemplateEngine.createTemplate(rule.rule).make(Chain.rearrange(orderedLinks[i].input,orderedLinks[i].inputReorder)).toString(),
                                    jobHistory: jobHistory
                                ]
                                log.info rule.rule
                                jobHistory.appendToLog("[StoredProcedureQuery] Untemplated Rule is: ${p.rule}")
                                jobHistory.appendToLog("[StoredProcedureQuery] Unmodified input for Templating Rule on link ${i} is ${orderedLinks[i].input as JSON}")
                                jobHistory.appendToLog("[StoredProcedureQuery] Modified input for Templating Rule on link ${i} is ${Chain.rearrange(orderedLinks[i].input,orderedLinks[i].inputReorder) as JSON}")
                                jobHistory.appendToLog("[StoredProcedureQuery] Templated Rule is: ${rule.rule}")
                                println rule.rule
                                return rule
                            }.call(orderedLinks[i].rule.properties),
                            orderedLinks[i].sourceName,
                            orderedLinks[i].executeEnum,    
                            orderedLinks[i].resultEnum,
                            { e ->
                                switch(e) {
                                    case ExecuteEnum.EXECUTE_USING_ROW: 
                                        jobHistory.appendToLog("[StoredProcedureQuery] Execute Using Row being used")
                                        jobHistory.appendToLog("[StoredProcedureQuery] Unmodified input for Executing Row on link ${i} is ${orderedLinks[i].input as JSON}")
                                        jobHistory.appendToLog("[StoredProcedureQuery] Modified input for Executing Row link ${i} is ${Chain.rearrange(orderedLinks[i].input,orderedLinks[i].inputReorder) as JSON}")                                        
                                        return Chain.rearrange(orderedLinks[i].input,orderedLinks[i].inputReorder)
                                        break
                                    default:
                                        return [:]
                                        break
                                }                                        
                            }.call(orderedLinks[i].executeEnum)
                        ).collect {
                            if(orderedLinks[i].resultEnum in [ResultEnum.APPENDTOROW]) {
                                return Chain.rearrange((([:] << orderedLinks[i].input) << it),orderedLinks[i].outputReorder)
                            } else if(orderedLinks[i].resultEnum in [ResultEnum.PREPENDTOROW]) {
                                return Chain.rearrange((([:] << it) << orderedLinks[i].input),orderedLinks[i].outputReorder)
                            }
                            return Chain.rearrange(it,orderedLinks[i].outputReorder)
                        }
                        break
                    case { it instanceof Groovy }:
                        jobHistory.appendToLog("[Groovy] Detected a Groovy script for ${orderedLinks[i].rule.name}")                        
                        log.info "Detected a Groovy Script for ${orderedLinks[i].rule.name}"
                        orderedLinks[i].rule.jobHistory = jobHistory
                        orderedLinks[i].output = { r ->
                            if([Collection, Object[]].any { it.isAssignableFrom(r.getClass()) }) {
                                switch(r) {
                                    case r.isEmpty():
                                        return r
                                        break
                                    case [Collection, Object[]].any { it.isAssignableFrom(r[0].getClass()) }:
                                        return r
                                        break
                                    default:
                                        return r
                                        break
                                }
                                return r
                            } else {
                                jobHistory.appendToLog("[Groovy] Object needs to be an array of objects so wrapping it as an array like this ${[r] as JSON}") 
                                return [ r ] 
                            }
                        }.call(linkService.justGroovy(
                            orderedLinks[i].rule,
                            orderedLinks[i].sourceName,
                            orderedLinks[i].executeEnum,    
                            orderedLinks[i].resultEnum,
                            { e ->
                                switch(e) {
                                    case ExecuteEnum.EXECUTE_USING_ROW: 
                                        jobHistory.appendToLog("[Groovy] Execute Using Row being used")
                                        jobHistory.appendToLog("[Groovy] Unmodified input for Executing Row on link ${i} is ${orderedLinks[i].input as JSON}")
                                        jobHistory.appendToLog("[Groovy] Modified input for Executing Row link ${i} is ${Chain.rearrange(orderedLinks[i].input,orderedLinks[i].inputReorder) as JSON}")                                        
                                        return Chain.rearrange(orderedLinks[i].input,orderedLinks[i].inputReorder)
                                        break
                                    default:
                                        return [:]
                                        break
                                }                                        
                            }.call(orderedLinks[i].executeEnum)
                        )).collect {
                            if(orderedLinks[i].resultEnum in [ResultEnum.APPENDTOROW]) {
                                return Chain.rearrange((([:] << orderedLinks[i].input) << it),orderedLinks[i].outputReorder)
                            } else if(orderedLinks[i].resultEnum in [ResultEnum.PREPENDTOROW]) {
                                return Chain.rearrange((([:] << it) << orderedLinks[i].input),orderedLinks[i].outputReorder)
                            }
                            return Chain.rearrange(it,orderedLinks[i].outputReorder)
                        }
                        break
                    case { it instanceof DefinedService }:
                        jobHistory.appendToLog("[DefinedService] Detected a Defined service for ${orderedLinks[i].rule.name}")                        
                        log.info "Detected a Defined Service ${orderedLinks[i].rule.name}" 
                        orderedLinks[i].rule.jobHistory = jobHistory
                        switch(orderedLinks[i].rule.authType) {
                            case AuthTypeEnum.CASSPRING:
                                jobHistory.appendToLog("[DefinedService] Detected a CASSPRING service") 
                                orderedLinks[i].output = linkService.casSpringSecurityRest(
                                    orderedLinks[i].rule.url,
                                    orderedLinks[i].rule.method.name(),
                                    orderedLinks[i].rule.user,
                                    orderedLinks[i].rule.password,
                                    orderedLinks[i].rule.headers,
                                    { e ->
                                        switch(e) {
                                            case ExecuteEnum.EXECUTE_USING_ROW: 
                                                jobHistory.appendToLog("[DefinedService] Execute Using Row being used")
                                                jobHistory.appendToLog("[DefinedService] Unmodified input for Executing Row on link ${i} is ${orderedLinks[i].input as JSON}")
                                                jobHistory.appendToLog("[DefinedService] Modified input for Executing Row link ${i} is ${Chain.rearrange(orderedLinks[i].input,orderedLinks[i].inputReorder) as JSON}")                                        
                                                return Chain.rearrange(orderedLinks[i].input,orderedLinks[i].inputReorder)
                                                break
                                            default:
                                                return [:]
                                                break
                                        }                                        
                                    }.call(orderedLinks[i].executeEnum),
                                    orderedLinks[i].rule.springSecurityBaseURL
                                ).collect {
                                    if(orderedLinks[i].resultEnum in [ResultEnum.APPENDTOROW]) {
                                        return Chain.rearrange((([:] << orderedLinks[i].input) << it),orderedLinks[i].outputReorder)
                                    } else if(orderedLinks[i].resultEnum in [ResultEnum.PREPENDTOROW]) {
                                        return Chain.rearrange((([:] << it) << orderedLinks[i].input),orderedLinks[i].outputReorder)
                                    }
                                    return Chain.rearrange(it,orderedLinks[i].outputReorder)
                                }
                                break;
                            case AuthTypeEnum.CAS:
                                jobHistory.appendToLog("[DefinedService] Detected a CAS service") 
                                orderedLinks[i].output = linkService.casRest(
                                    orderedLinks[i].rule.url,
                                    orderedLinks[i].rule.method.name(),
                                    orderedLinks[i].rule.user,
                                    orderedLinks[i].rule.password,
                                    orderedLinks[i].rule.headers,
                                    { e ->
                                        switch(e) {
                                            case ExecuteEnum.EXECUTE_USING_ROW: 
                                                jobHistory.appendToLog("[DefinedService] Execute Using Row being used")
                                                jobHistory.appendToLog("[DefinedService] Unmodified input for Executing Row on link ${i} is ${orderedLinks[i].input as JSON}")
                                                jobHistory.appendToLog("[DefinedService] Modified input for Executing Row link ${i} is ${Chain.rearrange(orderedLinks[i].input,orderedLinks[i].inputReorder) as JSON}")                                        
                                                return Chain.rearrange(orderedLinks[i].input,orderedLinks[i].inputReorder)
                                                break
                                            default:
                                                return [:]
                                                break
                                        }                                        
                                    }.call(orderedLinks[i].executeEnum)
                                ).collect {
                                    if(orderedLinks[i].resultEnum in [ResultEnum.APPENDTOROW]) {
                                        return Chain.rearrange((([:] << orderedLinks[i].input) << it),orderedLinks[i].outputReorder)
                                    } else if(orderedLinks[i].resultEnum in [ResultEnum.PREPENDTOROW]) {
                                        return Chain.rearrange((([:] << it) << orderedLinks[i].input),orderedLinks[i].outputReorder)
                                    }
                                    return Chain.rearrange(it,orderedLinks[i].outputReorder)
                                }                                
                                break;
                            case [AuthTypeEnum.BASIC,AuthTypeEnum.DIGEST,AuthTypeEnum.NONE]:
                                jobHistory.appendToLog("[DefinedService] Detected a REST service")                                 
                                orderedLinks[i].output = linkService.justRest(
                                    orderedLinks[i].rule.url,
                                    orderedLinks[i].rule.method,
                                    orderedLinks[i].rule.authType, 
                                    orderedLinks[i].rule.parse,
                                    orderedLinks[i].rule.user,
                                    orderedLinks[i].rule.password,
                                    orderedLinks[i].rule.headers,
                                    { e ->
                                        switch(e) {
                                            case ExecuteEnum.EXECUTE_USING_ROW: 
                                                jobHistory.appendToLog("[DefinedService] Execute Using Row being used")
                                                jobHistory.appendToLog("[DefinedService] Unmodified input for Executing Row on link ${i} is ${orderedLinks[i].input as JSON}")
                                                jobHistory.appendToLog("[DefinedService] Modified input for Executing Row link ${i} is ${Chain.rearrange(orderedLinks[i].input,orderedLinks[i].inputReorder) as JSON}")                                        
                                                return Chain.rearrange(orderedLinks[i].input,orderedLinks[i].inputReorder)
                                                break
                                            default:
                                                return [:]
                                                break
                                        }                                        
                                    }.call(orderedLinks[i].executeEnum)
                                ).collect {
                                    if(orderedLinks[i].resultEnum in [ResultEnum.APPENDTOROW]) {
                                        return Chain.rearrange((([:] << orderedLinks[i].input) << it),orderedLinks[i].outputReorder)
                                    } else if(orderedLinks[i].resultEnum in [ResultEnum.PREPENDTOROW]) {
                                        return Chain.rearrange((([:] << it) << orderedLinks[i].input),orderedLinks[i].outputReorder)
                                    }
                                    return Chain.rearrange(it,orderedLinks[i].outputReorder)
                                }
                                break;
                        }
                        break
                    case { it instanceof Snippet }:
                        jobHistory.appendToLog("[Snippet] Detected a Snippet for ${orderedLinks[i].rule.name}")                        
                        log.info "Detected a Snippet ${orderedLinks[i].rule.name}"   
                        orderedLinks[i].rule.chain.jobHistory = jobHistory
                        orderedLinks[i].output = orderedLinks[i].rule.chain.execute(
                            { e ->
                                switch(e) {
                                    case ExecuteEnum.EXECUTE_USING_ROW: 
                                        jobHistory.appendToLog("[Snippet] Execute Using Row being used")
                                        jobHistory.appendToLog("[Snippet] Unmodified input for Executing Row on link ${i} is ${orderedLinks[i].input as JSON}")
                                        jobHistory.appendToLog("[Snippet] Modified input for Executing Row link ${i} is ${Chain.rearrange(orderedLinks[i].input,orderedLinks[i].inputReorder) as JSON}")                                        
                                        return Chain.rearrange(orderedLinks[i].input,orderedLinks[i].inputReorder)
                                        break
                                    default:
                                        return [[:]]
                                        break
                                }                                        
                            }.call(orderedLinks[i].executeEnum)
                        ).collect {
                            if(orderedLinks[i].resultEnum in [ResultEnum.APPENDTOROW]) {
                                return Chain.rearrange((([:] << orderedLinks[i].input) << it),orderedLinks[i].outputReorder)
                            } else if(orderedLinks[i].resultEnum in [ResultEnum.PREPENDTOROW]) {
                                return Chain.rearrange((([:] << it) << orderedLinks[i].input),orderedLinks[i].outputReorder)
                            }
                            return Chain.rearrange(it,orderedLinks[i].outputReorder)
                        }
                        break                
                }
                // Handle result (aka: output)
                if((i+1) < orderedLinks.size() && orderedLinks[i].resultEnum in [ ResultEnum.ROW,ResultEnum.APPENDTOROW,ResultEnum.PREPENDTOROW ]) {
                    log.info "Setting the next output"
                    jobHistory.appendToLog("[NextInput] Setting the next input ${((orderedLinks[i].output)?orderedLinks[i].output.first():[:] as JSON)}")
                    orderedLinks[i+1].input = (orderedLinks[i].output)?orderedLinks[i].output.first():[:] 
                } else {
                    jobHistory.appendToLog("[NextInput] Not setting the next input for i=${i+1} and size ${orderedLinks.size()}")
                }
                // Handle link enum
                if((i+1) <= orderedLinks.size()) {
                    switch(orderedLinks[i].linkEnum) {
                        case [LinkEnum.NEXT]:
                            orderedLinks[i+1].input = Chain.rearrange(orderedLinks[i].input ,orderedLinks[i].outputReorder)
                            jobHistory.appendToLog("[Next] Carrying the current input to the following input ${orderedLinks[i+1].input as JSON}")
                            break
                        case [ LinkEnum.LOOP ]:
                            def endLoopIndex = Chain.findEndLoop(orderedLinks,i)
                            jobHistory.appendToLog("[LOOP] Detected a LOOP with End Loop Index ${endLoopIndex} starting at ${i+1}")
                            if(endLoopIndex != i) {
                                orderedLinks[endLoopIndex].output = execute(orderedLinks[i].output,orderedLinks[(i+1)..endLoopIndex])
                                i = endLoopIndex
                            }
                            jobHistory.appendToLog("[LOOP] is ended at i=${i}")
                            break
                    }
                }
            }
        }
        return (orderedLinks.isEmpty())?[[:]]:orderedLinks.last().output
    }
    
    static def rearrange(def row,String rearrange){
        if(!!rearrange) {
            String toBeEvaluated = """
                import groovy.sql.Sql
                import oracle.jdbc.driver.OracleTypes

                row
                ${rearrange}
            """        
            try {
                return new GroovyShell(new Binding("row":row)).evaluate(toBeEvaluated)
            } catch(Exception e) {
                System.out.println("${row.toString()} error: ${e.message} on closure: ${toBeEvaluated}")
            }
        }
        return row
    }    
    static def findEndLoop(List<Link> links,int i) {
        def endFound = false
        def endIndex = links.size()
        int loopCount = 1
        for( int l = (i+1) ; ( l < links.size() && !endFound) ; l++ ) {
            LinkEnum linkEnum = links[l].linkEnum
            switch(links[l].linkEnum) {
                case LinkEnum.LOOP:
                    loopCount++
                    break
                case LinkEnum.ENDLOOP:
                    loopCount--
                    if(!loopCount) {
                        endIndex = l
                        endFound = true
                    }
                    break
            }
        }                
        return endIndex
    }    
}
