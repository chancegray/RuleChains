package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*

/**
 * Testing Groovy domain class
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 * 
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Groovy)
@Mock([Rule,RuleSet,Chain,ChainServiceHandler])
class GroovyTests {
    /**
     * Testing creating a new Groovy
     */
    void testNewGroovy() {
        mockDomain(Groovy)
        def rs = new RuleSet(name: "newRuleSet")
        rs.isSynced = false
        rs.save()
        def r = new Groovy(name: 'testRule',ruleSet: rs)
        assert r.validate()
    }
    /**
     * Testing a missing ruleset
     */
    void testGroovyRuleMissingRuleSet() {
        mockDomain(Groovy)
        def r = new Groovy(name: 'testRule')
        assert r.validate() == false
        assert r.errors.hasFieldErrors("ruleSet")
        assert r.errors.getFieldError("ruleSet").rejectedValue == null
    }
}
