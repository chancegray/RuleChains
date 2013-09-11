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
                println "Unmodified input for link ${i} is ${orderedLinks[i].input as JSON}"
                // Execute the rule based on it's type
                println "Modified rearranged input for link ${i} is ${Chain.rearrange(orderedLinks[i].input,orderedLinks[i].inputReorder) as JSON}"

                switch(orderedLinks[i].rule) {
                    case { it instanceof SQLQuery }:
                        println "Detected an SQLQuery for ${orderedLinks[i].rule.name}"
                        orderedLinks[i].output = linkService.justSQL(
                            { p ->
                                def gStringTemplateEngine = new GStringTemplateEngine()
                                def rule = [:]
                                rule << p
                                rule.rule = gStringTemplateEngine.createTemplate(rule.rule).make(Chain.rearrange(orderedLinks[i].input,orderedLinks[i].inputReorder)).toString()
                                println rule.rule
                                return rule
                            }.call(orderedLinks[i].rule.properties),
                            orderedLinks[i].sourceName,
                            orderedLinks[i].executeEnum,    
                            orderedLinks[i].resultEnum,
                            { e ->
                                switch(e) {
                                    case ExecuteEnum.EXECUTE_USING_ROW: 
                                        println "Modified input for link ${i} is ${Chain.rearrange(orderedLinks[i].input,orderedLinks[i].inputReorder) as JSON}"
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
                        println "Detected a StoredProcedureQuery Script for ${orderedLinks[i].rule.name}"
                        orderedLinks[i].output = linkService.justStoredProcedure(
                            { p ->
                                def gStringTemplateEngine = new GStringTemplateEngine()
                                def rule = [:]
                                rule << p
                                rule.rule = gStringTemplateEngine.createTemplate(rule.rule).make(Chain.rearrange(orderedLinks[i].input,orderedLinks[i].inputReorder)).toString()
                                println rule.rule
                                return rule
                            }.call(orderedLinks[i].rule.properties),
                            orderedLinks[i].sourceName,
                            orderedLinks[i].executeEnum,    
                            orderedLinks[i].resultEnum,
                            { e ->
                                switch(e) {
                                    case ExecuteEnum.EXECUTE_USING_ROW: 
                                        println "Before input reorder ${orderedLinks[i].input.toString()}"
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
                        println "Detected a Groovy Script for ${orderedLinks[i].rule.name}"
                        orderedLinks[i].output = { r ->
                            if([Collection, Object[]].any { it.isAssignableFrom(r.getClass()) }) {
                                println "Spying on r as "+(r as JSON)
                                switch(r) {
                                    case r.isEmpty():
                                        return r
                                        break
                                    case [Collection, Object[]].any { it.isAssignableFrom(r[0].getClass()) }:
                                        return r
                                        break
                                    default:
                                        println "Looks like there is something inside the array that looks like an object "+(r as JSON)
                                        // return [ r ]
                                        return r
                                        break
                                }
                                return r
                            } else {
                                println "I don't think you are an array of arrays so you get this"
                                // return [ [ r ] ]
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
                        switch(orderedLinks[i].rule.authType) {
                            case AuthTypeEnum.CASSPRING:
                                orderedLinks[i].output = linkService.casSpringSecurityRest(
                                    orderedLinks[i].rule.url,
                                    orderedLinks[i].rule.method.name(),
                                    orderedLinks[i].rule.user,
                                    orderedLinks[i].rule.password,
                                    orderedLinks[i].rule.headers,
                                    { e ->
                                        switch(e) {
                                            case ExecuteEnum.EXECUTE_USING_ROW: 
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
                                orderedLinks[i].output = linkService.casRest(
                                    orderedLinks[i].rule.url,
                                    orderedLinks[i].rule.method.name(),
                                    orderedLinks[i].rule.user,
                                    orderedLinks[i].rule.password,
                                    orderedLinks[i].rule.headers,
                                    { e ->
                                        switch(e) {
                                            case ExecuteEnum.EXECUTE_USING_ROW: 
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
                        orderedLinks[i].output = orderedLinks[i].rule.chain.execute(
                            { e ->
                                switch(e) {
                                    case ExecuteEnum.EXECUTE_USING_ROW: 
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
                    println "Setting the next output"
                    println orderedLinks[i].output as JSON
                    orderedLinks[i+1].input = (orderedLinks[i].output)?orderedLinks[i].output.first():[:] 
                    println "Thinks the next input is "+((orderedLinks[i].output)?orderedLinks[i].output.first():[:] as JSON)
                    println "Next input is: "+(orderedLinks[i+1].input as JSON)
                } else {
                    println "Not setting the next output for i=${i+1} and size ${orderedLinks.size()}"
                }
                // Handle link enum
                if((i+1) <= orderedLinks.size()) {
                    switch(orderedLinks[i].linkEnum) {
                        case [LinkEnum.NEXT]:
                            orderedLinks[i+1].input = Chain.rearrange(orderedLinks[i].input ,orderedLinks[i].outputReorder)
                            break
                        case [ LinkEnum.LOOP ]:
                            def endLoopIndex = Chain.findEndLoop(orderedLinks,i)
                            println "Detected a LOOP with End Loop Index ${endLoopIndex} starting at ${i+1}"
                            if(endLoopIndex != i) {
                                orderedLinks[endLoopIndex].output = execute(orderedLinks[i].output,orderedLinks[(i+1)..endLoopIndex])
                                i = endLoopIndex
                            }
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
