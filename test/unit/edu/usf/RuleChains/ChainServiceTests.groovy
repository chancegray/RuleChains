package edu.usf.RuleChains



import grails.converters.*
import grails.test.mixin.*
import org.junit.*
import grails.util.GrailsUtil

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ChainService)
@Mock([Chain,Link,RuleSet,Rule,Snippet,SQLQuery,ChainServiceHandler])
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
    /**
     * Tests the create a new Chain
     * 
     */
    void testAddChain() {
        def chainService = new ChainService()
        def result = chainService.addChain("testChain",false)
        assert result.chain.name == "testChain"
    }
    /**
     * Tests the renaming of an existing Chain
     * 
     */
    void testModifyChain() {
        def c = new Chain(name: "testChain")
        c.isSynced = false
        c.save()
        def chainService = new ChainService()
        def result = chainService.modifyChain("testChain","renamedChain",false)
        assert result.chain.name == "renamedChain"
    }
    /**
     * Tests the removal of an existing Chain by name
     * 
     */    
    void testDeleteChain() {
        def c = new Chain(name: "testChain")
        c.isSynced = false
        c.save()
        def chainService = new ChainService()
        def result = chainService.deleteChain("testChain",false)
        assert result.success == "Chain deleted"
    }
    /**
     * Tests finding a Chain by it's name
     * 
     */
    void testGetChain() {
        def c = new Chain(name: "testChain")
        c.isSynced = false
        c.save()
        def chainService = new ChainService()
        def result = chainService.getChain("testChain")
        assert result.chain.name == "testChain"
    }
    /**
     * Tests finding a Link by it's sequence number and Chain name
     * 
     */    
    void testGetChainLink() {
        def c = new Chain(name: "testChain")
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
        def chainService = new ChainService()
        def result = chainService.getChainLink("testChain",1)
        assert result.link.sequenceNumber == 1
    }
    /**
     * Tests adding a new link to an existing chain
     * 
     */    
    void testAddChainLink() {
        def c = new Chain(name: "testChain")
        c.isSynced = false
        c.save()
        def rs = new RuleSet(name: "newRuleSet")
        rs.isSynced = false
        rs.save()
        def sr = new SQLQuery(name: "newRuleName",rule: "")
        sr.isSynced = false
        rs.addToRules(sr)
        rs.save()
        def chainService = new ChainService()
        def result = chainService.addChainLink("testChain",[
            sequenceNumber: 1,
            rule: "newRuleName",
            executeEnum: "NORMAL",
            resultEnum: "NONE",
            linkEnum: "NONE",
            sourceName: "mytestsource"
        ],false)
        assert result.chain.links.find { it.sequenceNumber == 1 }.rule.name == "newRuleName"
    }
    /**
     * Tests removing an existing link by sequence number and Chain name. 
     * 
     */ 
    void testDeleteChainLink() {
        def c = new Chain(name: "testChain")
        c.isSynced = false
        c.save()
        def rs = new RuleSet(name: "newRuleSet")
        rs.isSynced = false
        rs.save()
        def sr = new SQLQuery(name: "newRuleName",rule: "")
        sr.isSynced = false
        rs.addToRules(sr)
        rs.save()
        def l = new Link(rule: sr,sequenceNumber: 1,sourceName: "mytestsource")
        l.isSynced = false
        c.addToLinks(l)
        c.save()
        l.save()
        def chainService = new ChainService()
        def result = chainService.deleteChainLink("testChain",1,false)
        assert result.chain.links.size() == 0
        assert result.chain.name == "testChain"
    }
    /**
     * Tests updating a target link's property in a chain.
     * 
     */  
    void testModifyChainLink() {
        def c = new Chain(name: "testChain")
        c.isSynced = false
        c.save()
        def rs = new RuleSet(name: "newRuleSet")
        rs.isSynced = false
        rs.save()
        def sr = new SQLQuery(name: "newRuleName",rule: "")
        sr.isSynced = false
        rs.addToRules(sr)
        rs.save()
        sr.save()
        sr = new SQLQuery(name: "newRuleNameModified",rule: "")
        sr.isSynced = false
        rs.addToRules(sr)
        rs.save()
        sr.save()
        def l = new Link(rule: sr,sequenceNumber: 1,sourceName: "mytestsource")
        l.isSynced = false
        c.addToLinks(l)
        c.save()
        l.save()        
        def chainService = new ChainService()
        def result = chainService.modifyChainLink("testChain",1,[
            rule: "newRuleNameModified"
        ],false)
        assert result.link.rule.name == "newRuleNameModified"
    }
}
