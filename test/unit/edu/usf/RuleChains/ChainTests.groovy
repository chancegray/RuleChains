package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Chain)
@Mock([Rule,RuleSet,ChainServiceHandler])
class ChainTests {

    void testNewChain() {
        mockDomain(Chain)
        assert ([name: 'testChain'] as Chain).validate()
    }
    
    void testChainName() {
        mockDomain(Chain)
        def newChain = new Chain([name: 'Bad Chain Name'])
        assert newChain.validate() == false
        assert newChain.errors.hasFieldErrors("name")
        assert newChain.errors.getFieldError("name").rejectedValue == 'Bad Chain Name'
    }
}
