package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*
import groovy.lang.Binding
import grails.converters.*
import org.codehaus.groovy.grails.web.json.*;
import edu.usf.RuleChains.*
import java.util.regex.*
/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(ChainServiceHandlerController)
@Mock([ChainServiceHandler,ChainServiceHandlerService,Chain,Link,Rule,SQLQuery,RuleSet])
class ChainServiceHandlerControllerTests {

    void testHandleChainService() {
        controller.params << [
            handler: "newChainHandler",
            arbitrary: "some value"
        ]     
        controller.request.method = "GET"
        def control = mockFor(ChainServiceHandlerService)
        control.demand.handleChainService { handler,method,input -> 
            def c = new Chain([name: "newChain"])
            c.isSynced = false
            c.save()
            def rs = new RuleSet(name: "newRuleSet")
            rs.isSynced = false
            rs.save()
            def sr = new SQLQuery(name: "newRuleName",rule: "")
            sr.isSynced = false
            rs.addToRules(sr)
            rs.save()         
            def csh = new ChainServiceHandler(name: "newChainHandler",chain: c,method: MethodEnum.GET)
            csh.isSynced = false
            csh.save()            
            def chainServiceHandler = ChainServiceHandler.findByName(handler)
            assert input.arbitrary == "some value"
            return [
                "method": MethodEnum.byName(method),                
                "parameters": input 
            ]            
        }.rcurry((controller.params.inject([:]) {m,k,v ->
            if(!(k in ['handler','action','controller'])) {
                m[k] = v
            }
            return m
        }),"GET")
        controller.chainServiceHandlerService = control.createMock()
        
        controller.request.contentType = "text/json"
        def model = controller.handleChainService()
    }
    
    void testListChainServiceHandlers() {
        controller.params.pattern = null        
        controller.request.method = "GET"
        def control = mockFor(ChainServiceHandlerService)

        control.demand.listChainServiceHandlers { pattern -> 
            def cshObj = [
                "chainServiceHandlers": [
                    new ChainServiceHandler(name: "firstHandler",method: "GET",chain: new Chain(name: "nameChange")),
                    new ChainServiceHandler(name: "secondHandler",method: "GET",chain: new Chain(name: "nameChange"))
                ]
            ] 
            if(pattern) {
                return [chainServiceHandlers: cshObj.chainServiceHandlers.findAll {
                    Pattern.compile(pattern.trim()).matcher(it.name).matches()
                }]
            } else {
                return cshObj
            }
        }
        controller.chainServiceHandlerService = control.createMock()

        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.listChainServiceHandlers()
        assert model.chainServiceHandlers[0].name == "firstHandler"        
    }

    void testListChainServiceHandlersPattern() {
        controller.params.pattern = "^(second).*"     
        controller.request.method = "GET"
        def control = mockFor(ChainServiceHandlerService)

        control.demand.listChainServiceHandlers { pattern -> 
            def cshObj = [
                "chainServiceHandlers": [
                    new ChainServiceHandler(name: "firstHandler",method: "GET",chain: new Chain(name: "nameChange")),
                    new ChainServiceHandler(name: "secondHandler",method: "GET",chain: new Chain(name: "nameChange"))
                ]
            ] 
            if(pattern) {
                return [chainServiceHandlers: cshObj.chainServiceHandlers.findAll {
                    Pattern.compile(pattern.trim()).matcher(it.name).matches()
                }]
            } else {
                return cshObj
            }
        }
        controller.chainServiceHandlerService = control.createMock()

        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.listChainServiceHandlers()
        assert model.chainServiceHandlers[0].name == "secondHandler"        
    }

    void testAddChainServiceHandler() {
        controller.params << [
            name: "firstHandler",
            chain: "newChain"
        ]
        controller.request.method = "PUT"
        def control = mockFor(ChainServiceHandlerService)
        control.demand.addChainServiceHandler { name,chain ->
            def c = new Chain(name: "newChain")
            c.isSynced = false
            c.save()
            def rs = new RuleSet(name: "newRuleSet")
            rs.isSynced = false
            rs.save()
            def sr = new SQLQuery(name: "newRuleName",rule: "")
            sr.isSynced = false
            rs.addToRules(sr)
            rs.save()
            def l = new Link(rule: sr,sequenceNumber: 1)
            l.isSynced = false
            c.addToLinks(l)
            c.save()
            def chainServiceHandler = new ChainServiceHandler(name: name,chain: Chain.findByName(chain))
            chainServiceHandler.isSynced = false
            chainServiceHandler.save()
            return [ chainServiceHandler: chainServiceHandler ]
        }
        controller.chainServiceHandlerService = control.createMock()

        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.addChainServiceHandler()
        assert model.chainServiceHandler.name == "firstHandler"        
    }
}
