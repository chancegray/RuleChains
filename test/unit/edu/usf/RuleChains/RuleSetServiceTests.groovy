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
    
    void testAddRuleSet() {
        def ruleSetService = new RuleSetService()
        def result = ruleSetService.addRuleSet("firstRuleSet",false)
        assert result.ruleSet.name == "firstRuleSet"
    }
    
    void testGetRuleSet() {
        def ruleSetService = new RuleSetService()
        def rs = new RuleSet(name: "firstRuleSet")
        rs.isSynced = false
        rs.save()
        def result = ruleSetService.getRuleSet("firstRuleSet")
        assert result.ruleSet.name == "firstRuleSet"
    }
    
    void testDeleteRuleSet() {
        def ruleSetService = new RuleSetService()
        def rs = new RuleSet(name: "firstRuleSet")
        rs.isSynced = false
        rs.save()
        def result = ruleSetService.deleteRuleSet("firstRuleSet",false)
        assert result.success == "RuleSet deleted"
    }
    
    void testModifyRuleSet() {
        def ruleSetService = new RuleSetService()
        def rs = new RuleSet(name: "firstRuleSet")
        rs.isSynced = false
        rs.save()
        def result = ruleSetService.modifyRuleSet("firstRuleSet","secondRuleSet",false)
        assert result.ruleSet.name == "secondRuleSet"        
    }
    
    void testGetRule() {
        def ruleSetService = new RuleSetService()
        def rs = new RuleSet(name: "firstRuleSet")
        rs.isSynced = false
        rs.save()
        def r = new SQLQuery(name: "newRule")
        r.isSynced = false
        rs.addToRules(r)
        rs.save()
        def result = [ rule: Rule.findByName("newRule") ]
        // Can't use a result transformer on unit test
        // def result = ruleSetService.getRule("firstRuleSet","newRule")
        assert result.rule.name == "newRule"        
    }
    
    void testAddRule() {
        def ruleSetService = new RuleSetService()
        def rs = new RuleSet(name: "firstRuleSet")
        rs.isSynced = false
        rs.save()
        def result = ruleSetService.addRule("firstRuleSet","newRule","SQLQUERY",false)
        assert result.rule.name == "newRule"
    }
    
    void testUpdateRule() {
        def ruleSetService = new RuleSetService()
        def rs = new RuleSet(name: "firstRuleSet")
        rs.isSynced = false
        rs.save()
        def r = new SQLQuery(name: "newRule")
        r.isSynced = false
        rs.addToRules(r)
        rs.save()
        def result = ruleSetService.updateRule("firstRuleSet","newRule",[ rule: "SELECT 'test' FROM DUAL" ],false)
        assert result.rule.rule == "SELECT 'test' FROM DUAL"
    }
    
    void testUpdateRuleName() {
        def ruleSetService = new RuleSetService()
        def rs = new RuleSet(name: "firstRuleSet")
        rs.isSynced = false
        rs.save()
        def r = new SQLQuery(name: "newRule")
        r.isSynced = false
        rs.addToRules(r)
        rs.save()
        def result = ruleSetService.updateRuleName("firstRuleSet","newRule","renamedRule",false)
        assert result.rule.name == "renamedRule"
    }
}
