package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*

/**
 * ChainServiceHandlerTagLibTest is unit test for the taglib ChainServiceHandlerTagLib.
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 * 
 * See the API for {@link grails.test.mixin.web.GroovyPageUnitTestMixin} for usage instructions
 */
@TestFor(ChainServiceHandlerTagLib)
@Mock([ChainServiceHandlerService])
class ChainServiceHandlerTagLibTests {
    /**
     * Provides testing for a GSP method to produce a HTML select element of available chain service handlers.
     * 
     */ 
    void testChainServiceHandlerSelect() {
        def chainServiceHandlerService = mockFor(ChainServiceHandlerService)
        chainServiceHandlerService.demand.listChainServiceHandlers{-> 
            return [
                chainServiceHandlers: [[ id: 1, name: "testChainServiceHandler" ]]
            ]
        }
        tagLib.chainServiceHandlerService = chainServiceHandlerService.createMock()
        String result = applyTemplate("<chainServiceHandler:chainServiceHandlerSelect />")
        // println result
        def parser = new org.cyberneko.html.parsers.SAXParser()
        parser.setFeature('http://xml.org/sax/features/namespaces', false)
        def selectHtml = new XmlParser(parser).parseText(result)
        assertNotNull selectHtml.BODY.SELECT.first().children().find { it.value().first().toString() == "testChainServiceHandler" }
    }
}
