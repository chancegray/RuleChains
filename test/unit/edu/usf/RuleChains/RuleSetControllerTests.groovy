package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*
import java.util.regex.*
/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(RuleSetController)
@Mock([RuleSetService,Rule,SQLQuery,RuleSet,ChainServiceHandler,Chain,Link])
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
    
    void testAddRuleSet() {
        controller.params.name = "newRuleSet"
        controller.request.method = "PUT"
        def control = mockFor(RuleSetService)

        control.demand.addRuleSet { name -> 
            def rs = new RuleSet(name: name)
            rs.isSynced = false
            rs.save()
            return [ ruleSet: rs ]
        }
        controller.ruleSetService = control.createMock()

        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.addRuleSet()
        assert model.ruleSet.name == "newRuleSet"          
    }
    
    void testGetRuleSet() {
        controller.params.name = "newRuleSet"
        controller.request.method = "GET"
        def control = mockFor(RuleSetService)

        control.demand.getRuleSet { name -> 
            def rs = new RuleSet(name: name)
            rs.isSynced = false
            rs.save()
            def ruleSet = RuleSet.findByName(name.trim())
            return [ ruleSet: ruleSet ]
        }
        controller.ruleSetService = control.createMock()

        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.getRuleSet()
        assert model.ruleSet.name == "newRuleSet"          
    }
    
    void testDeleteRuleSet() {
        controller.params.name = "newRuleSet"
        controller.request.method = "DELETE"
        def control = mockFor(RuleSetService)

        control.demand.deleteRuleSet { name -> 
            def rs = new RuleSet(name: name)
            rs.isSynced = false
            rs.save()
            def ruleSet = RuleSet.findByName(name.trim())
            ruleSet.isSynced = false
            ruleSet.delete()
            return [ success : "RuleSet deleted" ]
        }
        controller.ruleSetService = control.createMock()

        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.deleteRuleSet()
        assert model.success == "RuleSet deleted"          
    }
    
    void testModifyRuleSet() {
        controller.params << [
            name: "newRuleSet",
            ruleSet: [
                name: "renamedRuleSet"
            ]
        ]
        controller.request.method = "POST"
        def control = mockFor(RuleSetService)

        control.demand.modifyRuleSet { name,ruleSetname -> 
            def rs = new RuleSet(name: name)
            rs.isSynced = false
            rs.save()
            def modifyRuleSet = RuleSet.findByName(name.trim())
            modifyRuleSet.isSynced = false
            modifyRuleSet.name = ruleSetname
            modifyRuleSet.save()
            return [ ruleSet: modifyRuleSet ]
        }
        controller.ruleSetService = control.createMock()

        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.modifyRuleSet()
        assert model.ruleSet.name == "renamedRuleSet"        
    }
    
    void testGetRule() {
        controller.params << [
            name: "newRuleSet",
            id: "newRule"
        ]        
        controller.request.method = "GET"
        def control = mockFor(RuleSetService)

        control.demand.getRule { name,id -> 
            def rs = new RuleSet(name: name)
            rs.isSynced = false
            rs.save()
            def r = new SQLQuery(name: id)
            r.isSynced = false
            rs.addToRules(r)
            rs.save()     
            def ruleSet = RuleSet.findByName(name)
            def rule = ruleSet.rules.find {
                it.name == id
            }
            return [ rule: rule ]
        }
        controller.ruleSetService = control.createMock()

        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.getRule()
        assert model.rule.name == "newRule"   
    }
    
    void testAddRule() {
        controller.params << [
            name: "newRuleSet",
            id: "newRule",
            serviceType: "SQLQUERY"
        ]        
        controller.request.method = "PUT"
        def control = mockFor(RuleSetService)

        control.demand.addRule { name,id,serviceType -> 
            def rs = new RuleSet(name: name)
            rs.isSynced = false
            rs.save()
            def ruleSet = RuleSet.findByName(name)
            ruleSet.isSynced = false
            def serviceTypeEnum = ServiceTypeEnum.byName(serviceType.trim())
            def rule
            switch(serviceTypeEnum) {
                case ServiceTypeEnum.SQLQUERY:
                    rule = [ name: id, rule: "" ] as SQLQuery
                    rule.isSynced = false
                    break
                case ServiceTypeEnum.GROOVY:
                    rule = [ name: id, rule: "" ] as Groovy
                    rule.isSynced = false
                    break
                case ServiceTypeEnum.STOREDPROCEDUREQUERY:
                    rule = [ name: id ] as StoredProcedureQuery
                    rule.isSynced = false
                    break
                case ServiceTypeEnum.DEFINEDSERVICE:
                    rule = [ name: id ] as DefinedService
                    rule.isSynced = false                    
                    break
                case ServiceTypeEnum.SNIPPET:
                    def chain = Chain.findByName(id)
                    /** TODO
                     * Removed temporarily until the Chain interface is in place
                     **/
                    if(!chain) {
                        return [ error: "Chain '${id}' does not exist! You must specify a name for an existing chain to reference it as a snippet."]
                    }
                    rule = [ name: id, chain: chain ] as Snippet
                    rule.isSynced = false                    
                    break
            }
            ruleSet.addToRules(rule)
            ruleSet.save() 
            return [ rule: rule ]
        }
        controller.ruleSetService = control.createMock()

        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.addRule()
        assert model.rule.name == "newRule" 
    }
    
    void testUpdateRule() {
        controller.params << [
            name: "newRuleSet",
            id: "newRule",
            rule: [
                rule: "select 1 from dual"
            ]
        ]        
        controller.request.method = "POST"
        def control = mockFor(RuleSetService)

        control.demand.updateRule { ruleSetName,name,ruleUpdate -> 
            def rs = new RuleSet(name: ruleSetName)
            rs.isSynced = false
            rs.save()
            def r = new SQLQuery(name: name)
            r.isSynced = false
            rs.addToRules(r)
            rs.save()     
            def ruleSet = RuleSet.findByName(ruleSetName)
            def rule = ruleSet.rules.find { it.name == name }
            rule.properties = ruleUpdate.inject([:]) {m,k,v ->
                switch(k) {
                    case "chain":
                        m[k] = ("name" in v)?Chain.findByName(v.name):Chain.findByName(v)
                        break
                    case "method":
                        m[k] = MethodEnum.byName(("name" in v)?v.name:v)
                        break
                    case "parse":
                        m[k] = ParseEnum.byName(("name" in v)?v.name:v)
                        break
                    case "authType":
                        m[k] = AuthTypeEnum.byName(("name" in v)?v.name:v)
                        break
                    default:
                        m[k] = v
                        break
                }
                return m
            }
            rule.isSynced = false
            rule.save()
            return [ rule: rule ]
        }
        controller.ruleSetService = control.createMock()

        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.updateRule()
        assert model.rule.rule == "select 1 from dual"         
    }
}
