package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*

/**
 * ChainTagLibTest is test for the ChainTagLib.
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 * 
 * See the API for {@link grails.test.mixin.web.GroovyPageUnitTestMixin} for usage instructions
 */
@TestFor(ChainTagLib)
@Mock([ChainService])
class ChainTagLibTests {
    /**
     * Provides a test for the GSP method to produce a HTML select element of available chains.
     * 
     */
    void testChainSelect() {
        def chainService = mockFor(ChainService)
        chainService.demand.listChains{-> 
            return [
                chains: [[ id: 1, name: "testChainChain" ]]
            ]
        }
        tagLib.chainService = chainService.createMock()
        String result = applyTemplate("<chain:chainSelect />")
        def parser = new org.cyberneko.html.parsers.SAXParser()
        parser.setFeature('http://xml.org/sax/features/namespaces', false)
        def selectHtml = new XmlParser(parser).parseText(result)
        assertNotNull selectHtml.BODY.SELECT.first().children().find { it.value().first().toString() == "testChainChain" }
    }
}
