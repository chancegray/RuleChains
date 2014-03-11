package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*

/**
 * Testing Link domain class
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 * 
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Link)
@Mock([Rule,SQLQuery,RuleSet,Chain,ChainServiceHandler,Link])
class LinkTests {

    /**
     * Testing the name validation
     */
    void testNewLink() {
        mockDomain(Link)
        def c = new Chain(name: 'testChain')
        c.isSynced = false
        c.save()
        def rs = new RuleSet(name: "newRuleSet")
        rs.isSynced = false
        rs.save()
        def sr = new SQLQuery(name: "newRuleName",rule: "")
        sr.isSynced = false
        rs.addToRules(sr)
        rs.save()
        def l = new Link(rule: sr,sequenceNumber: 1,sourceName: "testSource")
        l.isSynced = false
        c.addToLinks(l)
        c.save()
        assert l.validate()
    }
    /**
     * Testing a missing source name
     */
    void testLinkMissingSourceName() {
        mockDomain(Link)
        def c = new Chain(name: 'testChain')
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
        assert l.validate() == false
        assert l.errors.hasFieldErrors("sourceName")
        assert l.errors.getFieldError("sourceName").rejectedValue == null
    }
}
