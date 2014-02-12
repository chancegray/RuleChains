package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*

/**
 * RuleSetServiceTests provides for unit testing of services handling the processing and manipulation of RuleSet objects
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 * 
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(RuleSetService)
@Mock([Rule,SQLQuery,RuleSet,ChainServiceHandler,Chain,Link])
class RuleSetServiceTests {
    /**
     * Tests the returns of a list of RuleSet objects objects without a matching filter
     * 
     */ 
    void testListRuleSets() {
        def ruleSetService = new RuleSetService()
        [
            new RuleSet(name: "firstRuleSet"),
            new RuleSet(name: "secondRuleSet")
        ].each { rs ->
            rs.isSynced = false
            rs.save()
        }
        def result = ruleSetService.listRuleSets()
        assert result.ruleSets.size() == 2
        assert result.ruleSets.find { it.name == "firstRuleSet" }.name == "firstRuleSet"
        assert result.ruleSets.find { it.name == "secondRuleSet" }.name == "secondRuleSet"
    }
    /**
     * Tests the returns of a list of RuleSet objects objects with a matching filter
     * 
     */ 
    void testListRuleSetsPattern() {
        def ruleSetService = new RuleSetService()
        [
            new RuleSet(name: "firstRuleSet"),
            new RuleSet(name: "secondRuleSet")
        ].each { rs ->
            rs.isSynced = false
            rs.save()
        }
        def result = ruleSetService.listRuleSets("^(second).*")
        assert result.ruleSets.size() == 1
        assert result.ruleSets.find { it.name == "secondRuleSet" }.name == "secondRuleSet"
    }
}
