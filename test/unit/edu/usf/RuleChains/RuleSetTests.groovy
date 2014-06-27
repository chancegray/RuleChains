package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*

/**
 * Testing RuleSet domain class
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 * 
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(RuleSet)
@Mock([Rule,Chain,ChainServiceHandler])
class RuleSetTests {
    /**
     * Testing the name validation
     */
    void testNewRuleSet() {
        mockDomain(RuleSet)
        assert ([name: 'testRuleSet'] as RuleSet).validate()
    }
    /**
     * Testing bad name's
     */
    void testRuleSetName() {
        mockDomain(RuleSet)
        def newRuleSet = new RuleSet([name: 'Bad RuleSet Name'])
        assert newRuleSet.validate() == false
        assert newRuleSet.errors.hasFieldErrors("name")
        assert newRuleSet.errors.getFieldError("name").rejectedValue == 'Bad RuleSet Name'
    }
}
