package edu.usf.RuleChains
import groovy.lang.GroovyShell
import groovy.lang.Binding
import grails.util.GrailsNameUtils
import grails.converters.*

class Chain {
    String name
    List<Link> links
    List<List> input = [[]]
    static hasMany = [links:Link]
    static transients = ['orderedLinks','input','output']
    static constraints = {
        name(   
                blank: false,
                nullable: false,
                size: 3..255,
                unique: true,
                //Custom constraint - only allow upper, lower, digits, dash and underscore
                validator: { val, obj -> val ==~ /[A-Za-z0-9_-]+/ }
            )               
    }
    def getOrderedLinks() {
        links.sort{it.sequenceNumber}
    }
    def getOutput() {
        getOrderedLinks().last().output
    }
    def execute(def input = [[]],List<Link> orderedLinks) {
        println "I'm running"
        if(!!!orderedLinks) {
            orderedLinks = getOrderedLinks()
        }
        
        def linkService = new LinkService()
        for(def row in input) {
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
                // Execute the rule based on it's type
                switch(orderedLinks[i].rule) {
                    case { it instanceof SQLQuery || it instanceof NamedQuery }:
                        println "Detected an SQLQuery"
                        orderedLinks[i].output = linkService.justSQL(
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
                                        return []
                                        break
                                }                                        
                            }.call(orderedLinks[i].executeEnum)
                        ).collect {
                            if(orderedLinks[i].resultEnum in [ResultEnum.APPENDTOROW]) {
                                return Chain.rearrange(orderedLinks[i].input + it,orderedLinks[i].outputReorder)
                            } else if(orderedLinks[i].resultEnum in [ResultEnum.PREPENDTOROW]) {
                                return Chain.rearrange(it + orderedLinks[i].input,orderedLinks[i].outputReorder)
                            }
                            return Chain.rearrange(it,orderedLinks[i].outputReorder)
                        }
                        break
                    case { it instanceof Groovy }:
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
                                        return [ r ]
                                        break
                                }
                                return r
                            } else {
                                return [ [ r ] ]
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
                                        return []
                                        break
                                }                                        
                            }.call(orderedLinks[i].executeEnum)
                        )).collect {
                            if(orderedLinks[i].resultEnum in [ResultEnum.APPENDTOROW]) {
                                return Chain.rearrange(orderedLinks[i].input + it,orderedLinks[i].outputReorder)
                            } else if(orderedLinks[i].resultEnum in [ResultEnum.PREPENDTOROW]) {
                                return Chain.rearrange(it + orderedLinks[i].input,orderedLinks[i].outputReorder)
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
                                                return []
                                                break
                                        }                                        
                                    }.call(orderedLinks[i].executeEnum),
                                    orderedLinks[i].rule.springSecurityBaseURL
                                ).collect {
                                    if(orderedLinks[i].resultEnum in [ResultEnum.APPENDTOROW]) {
                                        return Chain.rearrange(orderedLinks[i].input + it,orderedLinks[i].outputReorder)
                                    } else if(orderedLinks[i].resultEnum in [ResultEnum.PREPENDTOROW]) {
                                        return Chain.rearrange(it + orderedLinks[i].input,orderedLinks[i].outputReorder)
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
                                                return []
                                                break
                                        }                                        
                                    }.call(orderedLinks[i].executeEnum)
                                ).collect {
                                    if(orderedLinks[i].resultEnum in [ResultEnum.APPENDTOROW]) {
                                        return Chain.rearrange(orderedLinks[i].input + it,orderedLinks[i].outputReorder)
                                    } else if(orderedLinks[i].resultEnum in [ResultEnum.PREPENDTOROW]) {
                                        return Chain.rearrange(it + orderedLinks[i].input,orderedLinks[i].outputReorder)
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
                                                return []
                                                break
                                        }                                        
                                    }.call(orderedLinks[i].executeEnum)
                                ).collect {
                                    if(orderedLinks[i].resultEnum in [ResultEnum.APPENDTOROW]) {
                                        return Chain.rearrange(orderedLinks[i].input + it,orderedLinks[i].outputReorder)
                                    } else if(orderedLinks[i].resultEnum in [ResultEnum.PREPENDTOROW]) {
                                        return Chain.rearrange(it + orderedLinks[i].input,orderedLinks[i].outputReorder)
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
                                        return [[]]
                                        break
                                }                                        
                            }.call(orderedLinks[i].executeEnum)
                        ).collect {
                            if(orderedLinks[i].resultEnum in [ResultEnum.APPENDTOROW]) {
                                return Chain.rearrange(orderedLinks[i].input + it,orderedLinks[i].outputReorder)
                            } else if(orderedLinks[i].resultEnum in [ResultEnum.PREPENDTOROW]) {
                                return Chain.rearrange(it + orderedLinks[i].input,orderedLinks[i].outputReorder)
                            }
                            return Chain.rearrange(it,orderedLinks[i].outputReorder)
                        }
                        break                
                }
                // Handle result (aka: output)
                if((i+1) < orderedLinks.size() && orderedLinks[i].resultEnum in [ ResultEnum.ROW,ResultEnum.APPENDTOROW,ResultEnum.PREPENDTOROW ]) {
                    orderedLinks[i+1].input = orderedLinks[i].output.first() 
                }
                // Handle link enum
                if((i+1) < orderedLinks.size()) {
                    switch(orderedLinks[i].linkEnum) {
                        case [LinkEnum.NEXT]:
                            orderedLinks[i+1].input = Chain.rearrange(orderedLinks[i].input ,orderedLinks[i].outputReorder)
                            break
                        case [ LinkEnum.LOOP ]:
                            def endLoopIndex = Chain.findEndLoop(orderedLinks,i)
                            if(endLoopIndex != i) {
                                orderedLinks[endLoopIndex].output = execute(orderedLinks[i].output,orderedLinks[(i+1)..endLoopIndex])
                                i = endLoopIndex
                            }
                            break
                    }
                }
            }
        }
        return (orderedLinks.isEmpty())?[[]]:orderedLinks.last().output
    }
    
    static def rearrange(def row,String rearrange){
        if(!!rearrange) {
            String toBeEvaluated = """

                def row = x
                return ${rearrange}
            """        
            return new GroovyShell(new Binding(x:row)).evaluate(toBeEvaluated)
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
