package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ConfigService)
@Mock([ChainServiceHandler,Chain,Link,Rule,SQLQuery,RuleSet])
class ConfigServiceTests {

    /**
     * Tests the downloading data method
     */
    void testDownloadChainData() {
        
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
