package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*

/**
 * Testing ConfigService upload and download of RuleChains data
 * <p>
 * Developed originally for the University of South Florida
 * 
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ConfigService)
@Mock([ChainServiceHandler,Chain,Link,Rule,SQLQuery,RuleSet])
class ConfigServiceTests {

    /**
     * Tests the downloading data method
     */
    void testDownloadChainData() {
        def configService = new ConfigService()
        configService.ruleSetService = new RuleSetService()
        configService.chainService = new ChainService()
        configService.chainServiceHandlerService = new ChainServiceHandlerService()
        configService.jobService = new JobService()
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
        def result = configService.downloadChainData()
        assert result.ruleSets.find { it.name == "newRuleSet" }?.name == "newRuleSet"
    }
    /**
     * Tests the uploading data method
     */
    void testUploadChainData() {
        def configService = new ConfigService()
        configService.ruleSetService = new RuleSetService()
        configService.chainService = new ChainService()
        configService.chainServiceHandlerService = new ChainServiceHandlerService()
        configService.jobService = new JobService()
        def result = configService.uploadChainData([
            ruleSets: [
                [ name: "newRuleSet" ]
            ]
        ],false)
        assert result.status == "complete"
        assert RuleSet.findByName("newRuleSet")?.name == "newRuleSet"
    }
}
