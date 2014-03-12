package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*

/**
 * Testing SQLQuery domain class
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 * 
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(SQLQuery)
@Mock([Rule,RuleSet,Chain,ChainServiceHandler])
class SQLQueryTests {

    void testNewSQLQuery() {
        mockDomain(SQLQuery)
        def rs = new RuleSet(name: "newRuleSet")
        rs.isSynced = false
        rs.save()
        def r = new SQLQuery(name: 'testRule',ruleSet: rs)
        assert r.validate()
    }
    /**
     * Testing a missing source name
     */
    void testSQLRuleMissingRuleSet() {
        mockDomain(Link)
        def r = new SQLQuery(name: 'testRule')
        assert r.validate() == false
        assert r.errors.hasFieldErrors("ruleSet")
        assert r.errors.getFieldError("ruleSet").rejectedValue == null
    }
}
