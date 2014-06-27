package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*

/**
 * Testing JobHistory domain class
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 * 
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(JobHistory)
class JobHistoryTests {
    /**
     * Testing the name validation
     */
    void testNewJobHistory() {
        mockDomain(JobHistory)
        assert ([name: 'testJobHistory'] as JobHistory).validate()
    }
    /**
     * Testing bad name's
     */
    void testJobHistoryName() {
        mockDomain(JobHistory)
        def newJobHistory = new JobHistory([name: 'tt'])
        assert newJobHistory.validate() == false
        assert newJobHistory.errors.hasFieldErrors("name")
        assert newJobHistory.errors.getFieldError("name").rejectedValue == 'tt'
    }
}
