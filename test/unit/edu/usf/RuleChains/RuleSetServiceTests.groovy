package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(RuleSetService)
@Mock([Rule,SQLQuery,RuleSet,ChainServiceHandler,Chain,Link])
class RuleSetServiceTests {

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
