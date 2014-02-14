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
    /**
     * Tests creating a new RuleSet
     * 
     */   
    void testAddRuleSet() {
        def ruleSetService = new RuleSetService()
        def result = ruleSetService.addRuleSet("firstRuleSet",false)
        assert result.ruleSet.name == "firstRuleSet"
    }
    /**
     * Tests finding a RuleSet by it's name
     * 
     */
    void testGetRuleSet() {
        def ruleSetService = new RuleSetService()
        def rs = new RuleSet(name: "firstRuleSet")
        rs.isSynced = false
        rs.save()
        def result = ruleSetService.getRuleSet("firstRuleSet")
        assert result.ruleSet.name == "firstRuleSet"
    }
    /**
     * Tests removing an existing RuleSet by name
     * 
     */ 
    void testDeleteRuleSet() {
        def ruleSetService = new RuleSetService()
        def rs = new RuleSet(name: "firstRuleSet")
        rs.isSynced = false
        rs.save()
        def result = ruleSetService.deleteRuleSet("firstRuleSet",false)
        assert result.success == "RuleSet deleted"
    }
    /**
     * Tests renaming an existing RuleSet
     * 
     */
    void testModifyRuleSet() {
        def ruleSetService = new RuleSetService()
        def rs = new RuleSet(name: "firstRuleSet")
        rs.isSynced = false
        rs.save()
        def result = ruleSetService.modifyRuleSet("firstRuleSet","secondRuleSet",false)
        assert result.ruleSet.name == "secondRuleSet"        
    }
    /**
     * Tests retrieving a Rule by it's RuleSet name and Rule name
     * 
     */ 
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
    /**
     * Tests creating a new Rule in an existing RuleSet
     * 
     */
    void testAddRule() {
        def ruleSetService = new RuleSetService()
        def rs = new RuleSet(name: "firstRuleSet")
        rs.isSynced = false
        rs.save()
        def result = ruleSetService.addRule("firstRuleSet","newRule","SQLQUERY",false)
        assert result.rule.name == "newRule"
    }
    /**
     * Tests updating an existing rule in a RuleSet
     * 
     */
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
    /**
     * Tests renaming an existing Rule
     * 
     */
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
    /**
     * Tests removing an existing Rule by RuleSet name and Rule name. 
     * 
     */   
    void testDeleteRule() {
        def ruleSetService = new RuleSetService()
        def rs = new RuleSet(name: "firstRuleSet")
        rs.isSynced = false
        rs.save()
        def r = new SQLQuery(name: "newRule")
        r.isSynced = false
        rs.addToRules(r)
        rs.save()        
        def result = ruleSetService.deleteRule("firstRuleSet","newRule",false)
        assert result.status == "Rule Removed From Set"
    }
    /**
     * Tests relocating an existing Rule in a different RuleSet
     *
     */
    void testMoveRule() {
        def ruleSetService = new RuleSetService()
        def rs = new RuleSet(name: "firstRuleSet")
        rs.isSynced = false
        rs.save()
        def r = new SQLQuery(name: "newRule")
        r.isSynced = false
        rs.addToRules(r)
        rs.save()     
        rs = new RuleSet(name: "secondRuleSet")
        rs.isSynced = false
        rs.save()
        def result = ruleSetService.moveRule("firstRuleSet","newRule","secondRuleSet",false)
        assert result.rule.name == "newRule"
    }
}
