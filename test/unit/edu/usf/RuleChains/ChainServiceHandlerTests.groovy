package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*

/**
 * Testing ChainServiceHandler domain class exposing rule chaings to incoming REST requests
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 * 
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(ChainServiceHandler)
@Mock([Rule,RuleSet,Chain])
class ChainServiceHandlerTests {

    /**
     * Testing the name validation
     */
    void testNewChainServiceHandler() {
        mockDomain(ChainServiceHandler)
        def c = new Chain(name: 'testChain')
        assert (new ChainServiceHandler([name: 'testChainServiceHandler',chain: c])).validate()
    }
    /**
     * Testing bad name's
     */
    void testChainServiceHandlerName() {
        mockDomain(ChainServiceHandler)
        def newChain = new ChainServiceHandler([name: 'Bad Chain Name'])
        assert newChain.validate() == false
        assert newChain.errors.hasFieldErrors("name")
        assert newChain.errors.getFieldError("name").rejectedValue == 'Bad Chain Name'
    }
}
