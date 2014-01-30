package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*
import groovy.lang.Binding
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
}
