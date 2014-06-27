package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*

/**
 * JobTagLibTests is a taglib tests for Quartz Job GSP views.
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 * 
 * See the API for {@link grails.test.mixin.web.GroovyPageUnitTestMixin} for usage instructions
 */
@TestFor(JobTagLib)
@Mock([JobService])
class JobTagLibTests {
    /**
     * Tests a GSP method to produce a HTML select element of available job histories.
     * 
     */         
    void testJobHistorySelect() {
        def jobService = mockFor(JobService)
        jobService.demand.getJobHistories{-> 
            return [
                jobHistories: [[ id: 1, name: "testJobHistory" ]]
            ]
        }
        tagLib.jobService = jobService.createMock()
        String result = applyTemplate("<job:jobHistorySelect />")
        def parser = new org.cyberneko.html.parsers.SAXParser()
        parser.setFeature('http://xml.org/sax/features/namespaces', false)
        def selectHtml = new XmlParser(parser).parseText(result)
        assertNotNull selectHtml.BODY.SELECT.first().children().find { it.value().first().toString() == "testJobHistory" }
    }
}
