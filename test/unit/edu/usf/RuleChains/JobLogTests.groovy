package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*

/**
 * Testing JobLog domain class
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 * 
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(JobLog)
@Mock([JobHistory])
class JobLogTests {
    /**
     * Testing the new JobLog validation
     */
    void testNewJobLog() {
        mockDomain(JobLog)
        def jh = new JobHistory(name: 'testHistory')
        jh.save()
        def l = new JobLog(line: "test log entry",jobHistory: jh)
        assert l.validate()
    }
    /**
     * Testing a missing JobHistory
     */
    void testJobLogMissingJobHistory() {
        mockDomain(JobLog)
        def l = new JobLog(line: "test log entry")
        assert l.validate() == false
        assert l.errors.hasFieldErrors("jobHistory")
        assert l.errors.getFieldError("jobHistory").rejectedValue == null
    }

}
