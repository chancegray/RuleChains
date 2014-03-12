package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*

/**
 * Testing StoredProcedureQuery domain class
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 * 
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(StoredProcedureQuery)
@Mock([Rule,RuleSet,Chain,ChainServiceHandler])
class StoredProcedureQueryTests {
    /**
     * Testing creating a new SQLQuery
     */
    void testNewStoredProcedureQuery() {
        mockDomain(StoredProcedureQuery)
        def rs = new RuleSet(name: "newRuleSet")
        rs.isSynced = false
        rs.save()
        def r = new StoredProcedureQuery(name: 'testRule',ruleSet: rs)
        assert r.validate()
    }
    /**
     * Testing a missing ruleset
     */
    void testStoredProcedureRuleMissingRuleSet() {
        mockDomain(StoredProcedureQuery)
        def r = new StoredProcedureQuery(name: 'testRule')
        assert r.validate() == false
        assert r.errors.hasFieldErrors("ruleSet")
        assert r.errors.getFieldError("ruleSet").rejectedValue == null
    }
}
