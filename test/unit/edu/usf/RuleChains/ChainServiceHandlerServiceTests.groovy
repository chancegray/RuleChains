package edu.usf.RuleChains



import grails.test.mixin.*
import groovy.sql.Sql
import grails.util.Holders
import org.junit.*

/**
 * ChainServiceHandlerServiceTests provides for unit testing of REST services handling the processing and manipulation of ChainServiceHandler objects
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 * 
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ChainServiceHandlerService)
@Mock([ChainServiceHandler,ChainServiceHandlerService,ChainService,LinkService,JobService,Chain,Link,Rule,Groovy,SQLQuery,RuleSet,JobHistory,JobLog])
class ChainServiceHandlerServiceTests {
     /**
     * Tests the handling of REST parameters on the matched Chain Service Handler {@link ChainServiceHandler}
     * against it's embedded Chain {@link Chain} with input parameters.
     * 
     * @see    Chain
     * @see    ChainServiceHandler
     */     
    void testHandleChainService() {
        def chainServiceHandlerService = new ChainServiceHandlerService()
        chainServiceHandlerService.chainService = new ChainService()
        chainServiceHandlerService.jobService = new JobService()
        // def grailsApplication = new Chain().domainClass.grailsApplication
        def c = new Chain([name: "newChain"])
        c.isSynced = false
        c.save()
        def rs = new RuleSet(name: "newRuleSet")
        rs.isSynced = false
        rs.save()
        // def sr = new SQLQuery(name: "newRuleName",rule: "SELECT :mykey as testKey FROM DUAL")
        def sr = new Groovy(name: "newRuleName",rule: "return [ [ testKey: row.mykey ] ]")
        sr.isSynced = false
        rs.addToRules(sr)
        rs.save()         
        def l = new Link(rule: sr,sequenceNumber: 1,executeEnum: ExecuteEnum.EXECUTE_USING_ROW,resultEnum: ResultEnum.ROW,linkEnum: LinkEnum.NONE,sourceName: "testSource")
        l.isSynced = false
        c.addToLinks(l)
        c.save()
        l.save()
        def csh = new ChainServiceHandler(name: "newChainHandler",chain: c,method: MethodEnum.GET)
        csh.isSynced = false
        csh.save()       
        LinkService.metaClass.getSQLSource { String name ->
            // return new Sql(grailsApplication.mainContext."sessionFactory".currentSession.connection())                
            return null
        }
        LinkService.metaClass.getSQLSources { ->
            return []
        }
        def result = chainServiceHandlerService.handleChainService("newChainHandler","GET",[ mykey: "my key value" ])
        assert result.first().testKey == "my key value"
    }
    /**
     * Tests the return of a list of ChainServiceHandler objects with an without option matching filter
     * 
     * @see    ChainServiceHandler
     */   
    void testListChainServiceHandlers() {
        def chainServiceHandlerService = new ChainServiceHandlerService()
        def c = new Chain([name: "newChain"])
        c.isSynced = false
        c.save()
        def rs = new RuleSet(name: "newRuleSet")
        rs.isSynced = false
        rs.save()
        // def sr = new SQLQuery(name: "newRuleName",rule: "SELECT :mykey as testKey FROM DUAL")
        def sr = new Groovy(name: "newRuleName",rule: "return [ [ testKey: row.mykey ] ]")
        sr.isSynced = false
        rs.addToRules(sr)
        rs.save()         
        def l = new Link(rule: sr,sequenceNumber: 1,executeEnum: ExecuteEnum.EXECUTE_USING_ROW,resultEnum: ResultEnum.ROW,linkEnum: LinkEnum.NONE,sourceName: "testSource")
        l.isSynced = false
        c.addToLinks(l)
        c.save()
        l.save()        
        [
            new ChainServiceHandler(name: "1stChainHandler",chain: c,method: MethodEnum.GET),
            new ChainServiceHandler(name: "2ndChainHandler",chain: c,method: MethodEnum.GET),
            new ChainServiceHandler(name: "3rdChainHandler",chain: c,method: MethodEnum.GET)
        ].each { csh ->
            csh.isSynced = false
            csh.save()
        }
        def result = chainServiceHandlerService.listChainServiceHandlers()
        assert result.chainServiceHandlers.size() == 3        
    }
    /**
     * Tests the return of a list of ChainServiceHandler objects with an with option matching filter
     * 
     * @see    ChainServiceHandler
     */   
    void testListChainServiceHandlersPattern() {
        def chainServiceHandlerService = new ChainServiceHandlerService()
        def c = new Chain([name: "newChain"])
        c.isSynced = false
        c.save()
        def rs = new RuleSet(name: "newRuleSet")
        rs.isSynced = false
        rs.save()
        // def sr = new SQLQuery(name: "newRuleName",rule: "SELECT :mykey as testKey FROM DUAL")
        def sr = new Groovy(name: "newRuleName",rule: "return [ [ testKey: row.mykey ] ]")
        sr.isSynced = false
        rs.addToRules(sr)
        rs.save()         
        def l = new Link(rule: sr,sequenceNumber: 1,executeEnum: ExecuteEnum.EXECUTE_USING_ROW,resultEnum: ResultEnum.ROW,linkEnum: LinkEnum.NONE,sourceName: "testSource")
        l.isSynced = false
        c.addToLinks(l)
        c.save()
        l.save()        
        [
            new ChainServiceHandler(name: "1stChainHandler",chain: c,method: MethodEnum.GET),
            new ChainServiceHandler(name: "2ndChainHandler",chain: c,method: MethodEnum.GET),
            new ChainServiceHandler(name: "3rdChainHandler",chain: c,method: MethodEnum.GET)
        ].each { csh ->
            csh.isSynced = false
            csh.save()
        }
        def result = chainServiceHandlerService.listChainServiceHandlers("^(2nd).*")
        assert result.chainServiceHandlers.first().name == "2ndChainHandler"                
    }
}
