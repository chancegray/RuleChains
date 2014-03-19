package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*

/**
 * Testing PHP domain class
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 * 
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(PHP)
@Mock([Rule,RuleSet,Chain,ChainServiceHandler])
class PHPTests {
    /**
     * Testing creating a new PHP
     */
    void testNewPHP() {
        mockDomain(PHP)
        def rs = new RuleSet(name: "newRuleSet")
        rs.isSynced = false
        rs.save()
        def r = new PHP(name: 'testRule',ruleSet: rs)
        assert r.validate()
    }
    /**
     * Testing a missing ruleset
     */
    void testPHPRuleMissingRuleSet() {
        mockDomain(PHP)
        def r = new PHP(name: 'testRule')
        assert r.validate() == false
        assert r.errors.hasFieldErrors("ruleSet")
        assert r.errors.getFieldError("ruleSet").rejectedValue == null
    }
}
