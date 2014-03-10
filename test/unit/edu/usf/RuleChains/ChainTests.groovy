package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*

/**
 * Testing Chain domain class being the sequencing object for processing
 * a sequence of rules.
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 * 
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Chain)
@Mock([Rule,RuleSet,ChainServiceHandler])
class ChainTests {
    /**
     * Testing the name validation
     */
    void testNewChain() {
        mockDomain(Chain)
        assert ([name: 'testChain'] as Chain).validate()
    }
    /**
     * Testing bad name's
     */
    void testChainName() {
        mockDomain(Chain)
        def newChain = new Chain([name: 'Bad Chain Name'])
        assert newChain.validate() == false
        assert newChain.errors.hasFieldErrors("name")
        assert newChain.errors.getFieldError("name").rejectedValue == 'Bad Chain Name'
    }
}
