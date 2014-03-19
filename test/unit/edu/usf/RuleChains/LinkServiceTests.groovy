package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*

/**
 * LinkServiceTests provides for unit testing of the execution of various rule types and helper functions.
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 * 
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(LinkService)
class LinkServiceTests {
    /**
     * Retrieves the global variables hashmap from the config called "rcGlobals"
     * and combines it with an optional provided Map and some local variables on the 
     * current local environment.
     * 
     */
    void testGetMergedGlobals() {
        def linkService = new LinkService()
        def result = linkService.getMergedGlobals([testKey: "testValue"])
        assert result.testKey == "testValue"
    }    
}
