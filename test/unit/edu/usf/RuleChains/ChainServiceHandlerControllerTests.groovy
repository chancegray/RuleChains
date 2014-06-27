package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*
import groovy.lang.Binding
import grails.converters.*
import org.codehaus.groovy.grails.web.json.*;
import edu.usf.RuleChains.*
import java.util.regex.*
/**
 * ChainServiceHandlerControllerTests provides for unit testing of REST services handling the processing and manipulation of ChainServiceHandler objects
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 * 
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(ChainServiceHandlerController)
@Mock([ChainServiceHandler,ChainServiceHandlerService,Chain,Link,Rule,SQLQuery,RuleSet])
class ChainServiceHandlerControllerTests {
    /**
     * Tests the handling of REST parameters on the matched Chain Service Handler {@link ChainServiceHandler}
     * against it's embedded Chain {@link Chain} with input parameters.
     * 
     * @see    Chain
     * @see    ChainServiceHandler
     */     
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
    /**
     * Tests the return of a list of ChainServiceHandler objects with an without option matching filter
     * 
     * @see    ChainServiceHandler
     */   
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
    /**
     * Tests the return of a list of ChainServiceHandler objects with an with option matching filter
     * 
     * @see    ChainServiceHandler
     */   
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
    /**
     * Tests the creation of a new ChainServiceHandler
     * 
     */ 
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
    /**
     * Tests the modification of an existing ChainServiceHandler with updated options
     * 
     */  
    void testModifyChainServiceHandler() {
        controller.params << [
            name: "firstHandler",
            chainServiceHandler: [
                name: "modifiedHandler"
            ]
        ]
        controller.request.method = "POST"
        def control = mockFor(ChainServiceHandlerService)
        control.demand.modifyChainServiceHandler { name,chainServiceHandler ->
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
            def csh = new ChainServiceHandler(name: name,chain: c)
            csh.isSynced = false
            csh.save()
            def modifiedChainServiceHandler = ChainServiceHandler.findByName(name.trim())
            modifiedChainServiceHandler.properties = chainServiceHandler.inject([:]) {m,k,v ->
                switch(k) {
                    case "method":
                        m[k] = MethodEnum.byName((("name" in v)?v.name:v))
                        break
                    case "chain":
                        m[k] = Chain.findByName(("name" in v)?v.name:v)
                        break
                    default:
                        m[k] = v
                        break                    
                }
                return m
            }
            modifiedChainServiceHandler.isSynced = false
            modifiedChainServiceHandler.save()
            return [ chainServiceHandler : modifiedChainServiceHandler ]
        }
        controller.chainServiceHandlerService = control.createMock()

        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.modifyChainServiceHandler()
        assert model.chainServiceHandler.name == "modifiedHandler"        
    }
    /**
     * Tests the removal of an existing ChainServiceHander by name
     * 
     */    
    void testDeleteChainServiceHandler() {
        controller.params.name = "firstHandler"
        controller.request.method = "POST"
        def control = mockFor(ChainServiceHandlerService)
        control.demand.deleteChainServiceHandler { name ->
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
            def csh = new ChainServiceHandler(name: "firstHandler",chain: c)
            csh.isSynced = false
            csh.save()
            def chainServiceHandler = ChainServiceHandler.findByName(name.trim())
            chainServiceHandler.isSynced = false
            chainServiceHandler.delete()
            return [ success : "Chain Service Handler deleted" ]
        }
        controller.chainServiceHandlerService = control.createMock()

        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.deleteChainServiceHandler()
        assert model.success == "Chain Service Handler deleted"               
    }
}
