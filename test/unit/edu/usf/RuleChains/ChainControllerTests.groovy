package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*
import grails.converters.*
import org.codehaus.groovy.grails.web.json.*;
import edu.usf.RuleChains.*
import java.util.regex.*
/**
 * ChainControllerTests provides for unit testing of REST services handling the processing and manipulation of Chain objects
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 * 
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(ChainController)
@Mock([Chain,Link,Rule,SQLQuery,RuleSet,ChainServiceHandler,ChainService])
class ChainControllerTests {
    /**
     * Tests a return list of Chain objects without an option matching filter
     * 
     * @see    Chain
     */        
    void testListChains() {
        controller.params.pattern = null        
        controller.request.method = "GET"
        def control = mockFor(ChainService)

        control.demand.listChains { pattern ->            
            def chainsObj = ["chains": [new Chain(["name": "nameChange"]),new Chain(["name": "netidChange"])]]
            if(pattern) {
                return [queues: chainsObj.chains.findAll {
                    Pattern.compile(pattern.trim()).matcher(it.name).matches()
                }]
            } else {
                return chainsObj
            }
        }
        controller.chainService = control.createMock()

        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.listChains()
        assert model.chains[0].name == "nameChange"
    }
    /**
     * Tests a return list of Chain objects with an option matching filter
     * 
     * @see    Chain
     */        
    void testListChainsPattern() {
        controller.params.pattern = "^(netid).*"
        controller.request.method = "GET"
        def control = mockFor(ChainService)
        control.demand.listChains { pattern ->
            def chainsObj = ["chains": [new Chain(["name": "nameChange"]),new Chain(["name": "netidChange"])]]
            if(pattern) {
                return [chains: chainsObj.chains.findAll {
                    Pattern.compile(pattern.trim()).matcher(it.name).matches()
                }]
            } else {
                return chainsObj
            }
        }
        controller.chainService = control.createMock()
        
        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.listChains()
        assert model.chains[0].name == "netidChange"
    }
    /**
     * Tests the create a new Chain
     * 
     */
    void testAddChain() {
        controller.params.name = "newChain"
        controller.request.method = "PUT"
        def control = mockFor(ChainService)
        control.demand.addChain { name ->
            def chain = new Chain([ name: name ])
            return [ chain: chain ]
        }
        controller.chainService = control.createMock()
        
        controller.request.contentType = "text/json"
        def model = controller.addChain()
        assert model.chain.name == "newChain"
    }
    /**
     * Tests the renaming of an existing Chain
     * 
     */
    void testModifyChain() {
        controller.params << [
            name: "oldChain",
            chain: [ name: "newChain" ]
        ]
        controller.request.method = "POST"
        def control = mockFor(ChainService)
        control.demand.modifyChain { name,newName ->
            def c = new Chain([name: "oldChain"])
            c.isSynced = false
            c.metaClass.afterUpdate = {-> }
            c.save(failOnError:true, flush: true, insert: true, validate: true)  
            def chain = Chain.findByName(name.trim())
            chain.name = newName.trim()
            chain.isSynced = false
            chain.metaClass.afterUpdate = {-> }
            if(!chain.save(failOnError:false, flush: false, validate: true)) {
                return [ error : "'${chain.errors.fieldError.field}' value '${chain.errors.fieldError.rejectedValue}' rejected" ]
            } else {
                return [ chain: chain ]
            }            
        }
        controller.chainService = control.createMock()
        
        controller.request.contentType = "text/json"
        def model = controller.modifyChain()
        assert model.chain.name == "newChain"
    }
    /**
     * Tests the removal of an existing Chain by name
     * 
     */    
    void testDeleteChain() {
        controller.params.name = "newChain"
        controller.request.method = "DELETE"
        def control = mockFor(ChainService)
        control.demand.deleteChain { name ->
            def c = new Chain([name: "newChain"])
            c.isSynced = false
            c.metaClass.afterUpdate = {-> }
            c.save(failOnError:true, flush: true, insert: true, validate: true)  
            def chain = Chain.findByName(name.trim())
            chain.isSynced = false
            chain.metaClass.afterUpdate = {-> }
            chain.delete()
            return [ success : "Chain deleted" ]
        }
        controller.chainService = control.createMock()
        
        controller.request.contentType = "text/json"
        def model = controller.deleteChain()
        assert model.success == "Chain deleted"
    }
    /**
     * Tests finding a Chain by it's name
     * 
     * @see    Chain
     */
    void testGetChain() {
        controller.params.name = "newChain"
        controller.request.method = "GET"
        def control = mockFor(ChainService)
        control.demand.getChain { name ->
            def c = new Chain([name: "newChain"])
            c.isSynced = false
            c.metaClass.afterUpdate = {-> }
            c.save(failOnError:true, flush: true, insert: true, validate: true) 
            def chain = Chain.findByName(name.trim())
            return [ chain: chain ]
        }
        controller.chainService = control.createMock()
        
        controller.request.contentType = "text/json"
        def model = controller.getChain()
        assert model.chain.name == "newChain"        
    }
    /**
     * Tests finding a Link by it's sequence number and Chain name
     * 
     * @see    Link
     */    
    void testGetChainLink() {
        controller.params << [
            name: "newChain",
            sequenceNumber: 1
        ]
        controller.request.method = "GET"
        def control = mockFor(ChainService)
        control.demand.getChainLink { name,sequenceNumber ->
            def c = new Chain(name: "newChain")
            c.isSynced = false
            c.save()
            def rs = new RuleSet(name: "newRuleSet")
            rs.isSynced = false
            rs.save()
            def sr = new SQLQuery(name: "newRuleName",rule: "")
            sr.isSynced = false
            rs.addToRules(sr)
            rs.save()
            def l = new Link(rule: sr,sequenceNumber: 1)
            l.isSynced = false
            c.addToLinks(l)
            c.save()
            def chain = Chain.findByName(name.trim())
            def link = chain.links.find { it.sequenceNumber == 1 }
            return [ link: link ]
        }
        controller.chainService = control.createMock()
        
        controller.request.contentType = "text/json"
        def model = controller.getChainLink()
        assert model.link.sequenceNumber == 1   
    }
    /**
     * Tests adding a new link to an existing chain
     * 
     */    
    void testAddChainLink() {
        controller.params << [
            name: "newChain",
            link: [
                rule: "newRuleName",
                sequenceNumber: 1
            ]
        ]
        controller.request.method = "PUT"
        def control = mockFor(ChainService)
        control.demand.addChainLink { name,link ->
            def c = new Chain([name: "newChain"])
            c.isSynced = false
            c.save()
            def rs = new RuleSet(name: "newRuleSet")
            rs.isSynced = false
            rs.save()
            def sr = new SQLQuery(name: "newRuleName",rule: "")
            sr.isSynced = false
            rs.addToRules(sr)
            rs.save()            
            def l = new Link(link.inject([:]) {l,k,v ->
                switch(k) {
                    case "executeEnum":
                        l[k] = ExecuteEnum.byName((("name" in v)?v.name:v).tokenize('.').last())
                        break
                    case "resultEnum":
                        l[k] = ResultEnum.byName((("name" in v)?v.name:v).tokenize('.').last())
                        break
                    case "linkEnum":
                        l[k] = LinkEnum.byName((("name" in v)?v.name:v).tokenize('.').last())
                        break
                    case "sequenceNumber":
                        l[k] = v.toLong()
                        break
                    case "rule":
                        l[k] = Rule.findByName(("name" in v)?v.name:v)
                        l[k].isSynced = false
                        break
                    default:
                        l[k] = v
                        break                    
                }
                return l
            })
            l.isSynced = false
            c.addToLinks(l)
            c.save()
            return [ chain: c ]
        }
        controller.chainService = control.createMock()
        
        controller.request.contentType = "text/json"
        def model = controller.addChainLink()
        assert model.chain.links[0].sequenceNumber == 1   
    }
    /**
     * Tests removing an existing link by sequence number and Chain name. 
     * 
     */ 
    void testDeleteChainLink() {
        controller.params << [
            name: "newChain",
            sequenceNumber: 1
        ]
        controller.request.method = "DELETE"
        def control = mockFor(ChainService)
        control.demand.deleteChainLink { name,sequenceNumber ->
            def c = new Chain(name: "newChain")
            c.isSynced = false
            c.save()
            def rs = new RuleSet(name: "newRuleSet")
            rs.isSynced = false
            rs.save()
            def sr = new SQLQuery(name: "newRuleName",rule: "")
            sr.isSynced = false
            rs.addToRules(sr)
            rs.save()
            def l = new Link(rule: sr,sequenceNumber: 1)
            l.isSynced = false
            c.addToLinks(l)
            c.save()
            def chain = Chain.findByName(name.trim())
            chain.isSynced = false
            def link = chain.links.find { it.sequenceNumber == sequenceNumber }
            link.isSynced = false
            chain.removeFromLinks(link).save()
            return [ chain: chain ]
        }
        controller.chainService = control.createMock()
        
        controller.request.contentType = "text/json"
        def model = controller.deleteChainLink()
        assert !!!(model.chain.links)
    }
    /**
     * Tests updating a target link's property in a chain.
     * 
     * @see    Link
     * @see    Chain
     */  
    void testModifyChainLink() {
        controller.params << [
            name: "newChain",
            sequenceNumber: 1,
            link: [
                rule: "newRuleName2"
            ]
        ]
        controller.request.method = "POST"
        def control = mockFor(ChainService)
        control.demand.modifyChainLink { name,sequenceNumber,link ->
            def c = new Chain(name: "newChain")
            c.isSynced = false
            c.save()
            def rs = new RuleSet(name: "newRuleSet")
            rs.isSynced = false
            rs.save()
            def sr = new SQLQuery(name: "newRuleName2",rule: "")
            sr.isSynced = false
            rs.addToRules(sr)
            rs.save()
            sr = new SQLQuery(name: "newRuleName",rule: "")
            sr.isSynced = false
            rs.addToRules(sr)
            rs.save()
            def l = new Link(rule: sr,sequenceNumber: 1)
            l.isSynced = false
            c.addToLinks(l)
            c.save()
            def chain = Chain.findByName(name.trim())
            chain.isSynced = false
            def updatedLink = chain.links.find { it.sequenceNumber == sequenceNumber }
            updatedLink.isSynced = false
            
            updatedLink.properties = link.inject([:]) {m,k,v ->
                switch(k) {
                    case "executeEnum":
                        m[k] = ExecuteEnum.byName((("name" in v)?v.name:v).tokenize('.').last())
                        break
                    case "resultEnum":
                        m[k] = ResultEnum.byName((("name" in v)?v.name:v).tokenize('.').last())
                        break
                    case "linkEnum":
                        m[k] = LinkEnum.byName((("name" in v)?v.name:v).tokenize('.').last())
                        break
                    case "sequenceNumber":
                        m[k] = v.toLong()
                        break
                    case "rule":
                        m[k] = Rule.findByName(("name" in v)?v.name:v)
                        m[k].isSynced = false
                        break
                    default:
                        m[k] = v
                        break                    
                }
                return m
            }
            updatedLink.save()
            return [ link: updatedLink ]
        }        
        controller.chainService = control.createMock()
        
        controller.request.contentType = "text/json"
        def model = controller.modifyChainLink()
        assert model.link.rule.name == "newRuleName2"
    }
    /**
     * Tests retrieving a list of available sources and other objects strictly for the user interface
     * 
     */    
    void testGetSources() {
        controller.request.method = "GET"
        def control = mockFor(ChainService)
        control.demand.getSources { ->
            def mockObj = [ 
                sources: [ "testSource "],
                actions: [
                    execute: ExecuteEnum.values().collect { it.name() },
                    result: ResultEnum.values().collect { it.name() },
                    link: LinkEnum.values().collect { it.name() }
                ],
                jobGroups: [],
                executingJobs: []            
            ]
            return mockObj            
        }
        controller.chainService = control.createMock()
        
        controller.request.contentType = "text/json"
        def model = controller.modifyChainLink()
        assert model.actions.execute.contains("NORMAL")
    }
}
