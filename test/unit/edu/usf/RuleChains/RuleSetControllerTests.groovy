package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*
import java.util.regex.*
/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(RuleSetController)
@Mock([RuleSetService,Rule,SQLQuery,RuleSet])
class RuleSetControllerTests {

    void testListRuleSets() {
        controller.params.pattern = null        
        controller.request.method = "GET"
        def control = mockFor(RuleSetService)

        control.demand.listRuleSets { pattern -> 
            def rsObj = [
                "ruleSets": [
                    new RuleSet(name: "firstRuleSet"),
                    new RuleSet(name: "secondRuleSet")
                ]
            ] 
            if(pattern) {
                return [ruleSets: rsObj.ruleSets.findAll {
                    Pattern.compile(pattern.trim()).matcher(it.name).matches()
                }]
            } else {
                return rsObj
            }
        }
        controller.ruleSetService = control.createMock()

        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.listRuleSets()
        assert model.ruleSets[0].name == "firstRuleSet"        
    }

    void testListRuleSetsPattern() {
        controller.params.pattern = "^(second).*"        
        controller.request.method = "GET"
        def control = mockFor(RuleSetService)

        control.demand.listRuleSets { pattern -> 
            def rsObj = [
                "ruleSets": [
                    new RuleSet(name: "firstRuleSet"),
                    new RuleSet(name: "secondRuleSet")
                ]
            ] 
            if(pattern) {
                return [ruleSets: rsObj.ruleSets.findAll {
                    Pattern.compile(pattern.trim()).matcher(it.name).matches()
                }]
            } else {
                return rsObj
            }
        }
        controller.ruleSetService = control.createMock()

        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.listRuleSets()
        assert model.ruleSets[0].name == "secondRuleSet"        
    }
}
