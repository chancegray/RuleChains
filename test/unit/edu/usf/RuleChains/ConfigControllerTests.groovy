package edu.usf.RuleChains



import grails.converters.*
import grails.test.mixin.*
import org.junit.*

/**
 * Testing ConfigController upload and download of RuleChains data
 * <p>
 * Developed originally for the University of South Florida
 * 
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(ConfigController)
@Mock([ChainServiceHandler,ConfigService,Chain,Link,Rule,SQLQuery,RuleSet])
class ConfigControllerTests {
    /**
     * Tests the downloading data method
     */
    void testDownloadChainData() {
        controller.request.method = "GET"
        def control = mockFor(ConfigService)
        control.demand.downloadChainData { -> 
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
            def csh = new ChainServiceHandler(name: "newChainServiceHandler",chain: c)
            csh.isSynced = false
            csh.save()
            return [
                ruleSets: RuleSet.list(),
                chains: Chain.list(),
                chainServiceHandlers: ChainServiceHandler.list()
            ]            
        }
        controller.configService = control.createMock()

        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.downloadChainData()
        def respObj = JSON.parse(controller.response.text)
        assert respObj.chainServiceHandlers[0].name == "newChainServiceHandler" 
        assert respObj.chains[0].name == "newChain"
        assert respObj.ruleSets[0].name == "newRuleSet"
        assert respObj.ruleSets[0].rules[0].name == "newRuleName"
    }
    /**
     * Tests the uploading data method
     */
    void testUploadChainData() {
        controller.params << [
            upload: ([
                ruleSets: [
                    name: "newRuleSet"
                ]
            ] as JSON).toString()
        ]
        controller.request.method = "POST"
        def control = mockFor(ConfigService)
        control.demand.uploadChainData { upload -> 
            upload.ruleSets.each { rs ->
                def ruleSet = new RuleSet()
                ruleSet.properties = rs
                ruleSet.isSynced = false
                ruleSet.save()
            }
            return [ status: "complete"]
        }
        controller.configService = control.createMock()

        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.uploadChainData()
        assert model.status == "complete"
    }
}
