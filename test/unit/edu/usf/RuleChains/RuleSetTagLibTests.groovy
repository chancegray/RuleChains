package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*

/**
 * RuleSetTagLibTests test the taglib for RuleSet GSP views.
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 *
 * See the API for {@link grails.test.mixin.web.GroovyPageUnitTestMixin} for usage instructions
 */
@TestFor(RuleSetTagLib)
@Mock([RuleSetService])
class RuleSetTagLibTests {
    /**
     * Provides a test for a GSP method to produce a HTML select element of available rulesets.
     * 
     */      
    void testRuleSetSelect() {
        def ruleSetService = mockFor(RuleSetService)
        ruleSetService.demand.listRuleSets{-> 
            return [
                ruleSets: [[ id: 1, name: "testRuleSet" ]]
            ]
        }
        tagLib.ruleSetService = ruleSetService.createMock()
        String result = applyTemplate("<ruleSet:ruleSetSelect />")
        def parser = new org.cyberneko.html.parsers.SAXParser()
        parser.setFeature('http://xml.org/sax/features/namespaces', false)
        def selectHtml = new XmlParser(parser).parseText(result)
        assertNotNull selectHtml.BODY.SELECT.first().children().find { it.value().first().toString() == "testRuleSet" }
    }
}
