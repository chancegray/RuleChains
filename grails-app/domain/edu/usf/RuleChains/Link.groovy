package edu.usf.RuleChains

import edu.usf.RuleChains.ExecuteEnum
import edu.usf.RuleChains.ResultEnum
import edu.usf.RuleChains.LinkEnum

class Link {
    Long sequenceNumber
    String sourceName
    Rule rule
    String inputReorder = ''
    String outputReorder = ''
    ExecuteEnum executeEnum = ExecuteEnum.NORMAL
    ResultEnum resultEnum = ResultEnum.NONE
    LinkEnum linkEnum = LinkEnum.NONE
    def input = [:]
    def output  = [[:]] 
    static transients = ['input','output']
    static belongsTo = [chain: Chain]
    static mapping = {
        inputReorder type: 'text'
        outputReorder type: 'text'
    }    
    static constraints = {
        sourceName(
            blank: false,
            nullable: false,
            size: 3..255,
            validator: { val, obj -> 
                Link.sourceNameVerified(val)
            }
        )
        inputReorder(blank:true)
        outputReorder(blank:true)
        executeEnum( 
            blank: false,
            validator: { val, obj -> 
                return val.name() in ExecuteEnum.values().collect { it.name() }
            }        
        )
        resultEnum( 
            blank: false,
            validator: { val, obj -> 
                return val.name() in ResultEnum.values().collect { it.name() }
            }        
        )
        linkEnum( 
            blank: false,
            validator: { val, obj -> 
                return val.name() in LinkEnum.values().collect { it.name() }
            }        
        )
        
    }
    static sourceNameVerified(String sourceName) {
        def grailsApplication = new Link().domainClass.grailsApplication
        def ctx = grailsApplication.mainContext
        def config = grailsApplication.config
        // def fooService = ctx.fooService
        return !!grailsApplication.mainContext.beanDefinitionNames.findAll{ it.startsWith( 'sessionFactory_' ) }.find{ it.endsWith(sourceName) }
    }    
}
