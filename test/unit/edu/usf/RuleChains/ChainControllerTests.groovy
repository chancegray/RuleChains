package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*
import grails.converters.*
import org.codehaus.groovy.grails.web.json.*;
import edu.usf.RuleChains.*
import java.util.regex.*
/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(ChainController)
@Mock([Chain,Rule,RuleSet,ChainServiceHandler,ChainService])
class ChainControllerTests {

    void testListChains() {
        controller.params.pattern = null        
        controller.request.method = "GET"
        def control = mockFor(ChainService)

        control.demand.listChains { pattern ->            
            def chainsObj = ["chains": [new Chain(["name": "nameChange"]),new Chain(["name": "netidChange"])]]
            if(pattern) {
                return [queues: chainsObj.chains.findAll {
                    Pattern.compile(pattern.trim()).matcher(it.name).matches()
                }]
            } else {
                return chainsObj
            }
        }
        controller.chainService = control.createMock()

        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.listChains()
        assert model.chains[0].name == "nameChange"
    }

    void testListQueuesPattern() {
        controller.params.pattern = "^(netid).*"
        controller.request.method = "GET"
        def control = mockFor(ChainService)
        control.demand.listChains { pattern ->
            def chainsObj = ["chains": [new Chain(["name": "nameChange"]),new Chain(["name": "netidChange"])]]
            if(pattern) {
                return [chains: chainsObj.chains.findAll {
                    Pattern.compile(pattern.trim()).matcher(it.name).matches()
                }]
            } else {
                return chainsObj
            }
        }
        controller.chainService = control.createMock()
        
        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.listChains()
        assert model.chains[0].name == "netidChange"
    }
    
    void testAddChain() {
        controller.params.name = "newChain"
        controller.request.method = "PUT"
        def control = mockFor(ChainService)
        control.demand.addChain { name ->
            def chain = new Chain([ name: name ])
            return [ chain: chain ]
        }
        controller.chainService = control.createMock()
        
        controller.request.contentType = "text/json"
        def model = controller.addChain()
        assert model.chain.name == "newChain"
    }
    void testModifyChain() {
        controller.params << [
            name: "oldChain",
            chain: [ name: "newChain" ]
        ]
        controller.request.method = "POST"
        def control = mockFor(ChainService)
        control.demand.modifyChain { name,newName ->
            def c = new Chain([name: "oldChain"])
            c.isSynced = false
            c.metaClass.afterUpdate = {-> }
            c.save(failOnError:true, flush: true, insert: true, validate: true)  
            def chain = Chain.findByName(name.trim())
            chain.name = newName.trim()
            chain.isSynced = false
            chain.metaClass.afterUpdate = {-> }
            if(!chain.save(failOnError:false, flush: false, validate: true)) {
                return [ error : "'${chain.errors.fieldError.field}' value '${chain.errors.fieldError.rejectedValue}' rejected" ]
            } else {
                return [ chain: chain ]
            }            
        }
        controller.chainService = control.createMock()
        
        controller.request.contentType = "text/json"
        def model = controller.modifyChain()
        assert model.chain.name == "newChain"
    }
}
