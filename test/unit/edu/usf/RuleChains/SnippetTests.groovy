package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*

/**
 * Testing Snippet domain class
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 * 
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Snippet)
@Mock([Rule,RuleSet,Chain,ChainServiceHandler])
class SnippetTests {
    /**
     * Testing creating a new Snippet
     */
    void testNewSnippet() {
        mockDomain(Snippet)
        def rs = new RuleSet(name: "newRuleSet")
        rs.isSynced = false
        rs.save()
        def c = new Chain(name: "testChain")
        c.isSynced = false
        c.save()
        def r = new Snippet(name: 'testChain',ruleSet: rs, chain: c)
        assert r.validate()
    }
    /**
     * Testing a missing ruleset
     */
    void testSnippetRuleMissingRuleSet() {
        mockDomain(Snippet)
        def c = new Chain(name: "testChain")
        c.isSynced = false
        c.save()
        def r = new Snippet(name: 'testChain',chain: c)
        assert r.validate() == false
        assert r.errors.hasFieldErrors("ruleSet")
        assert r.errors.getFieldError("ruleSet").rejectedValue == null
    }
}
