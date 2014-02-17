package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ChainService)
@Mock([Chain,Link,RuleSet,Rule,ChainServiceHandler])
class ChainServiceTests {
    /**
     * Tests a return list of Chain objects without an option matching filter
     * 
     */        
    void testListChains() {
        def chainService = new ChainService()
        [
            new Chain(name: "1stChain"),
            new Chain(name: "2ndChain"),
            new Chain(name: "3rdChain")
        ].each { c ->
            c.isSynced = false
            c.save()
        }
        def result = chainService.listChains()
        assert result.chains.size() == 3
    }
    /**
     * Tests a return list of Chain objects with an option matching filter
     * 
     */  
    void testListChainsPattern() {
        def chainService = new ChainService()
        [
            new Chain(name: "1stChain"),
            new Chain(name: "2ndChain"),
            new Chain(name: "3rdChain")
        ].each { c ->
            c.isSynced = false
            c.save()
        }
        def result = chainService.listChains("^(1st).*")
        assert result.chains.size() == 1
        assert result.chains[0].name == "1stChain"
    }
}
