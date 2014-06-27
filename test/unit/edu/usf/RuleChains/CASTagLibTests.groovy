package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*
import edu.usf.cims.UsfCasService
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
/**
 * Tests CASTagLib as a taglib for CAS GSP views.
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 * 
 * See the API for {@link grails.test.mixin.web.GroovyPageUnitTestMixin} for usage instructions
 */
@TestFor(CASTagLib)
@Mock([UsfCasService])
class CASTagLibTests {
    /**
     * Tests a GSP method to produce a HTML select element of assigned CAS attributes.
     * 
     */ 
    void testCasAttributes() {
        def usfCasService = mockFor(UsfCasService)
        usfCasService.demand.getAttributes{-> 
            return [
                "testAttribute"
            ]
        }
        tagLib.usfCasService = usfCasService.createMock()
        String result = applyTemplate("<castag:casAttributes />")
        assert result == "<select name='attributes' id='attributes'><option>testAttribute</option></select>"
    }
    /**
     * Tests a GSP method to produce a HTML select element of assigned CAS roles.
     * 
     */ 
    void testCasRoles() {
        String result = applyTemplate("<castag:casRoles />")
        assert result == "<select name='authorities' id='authorities'></select>"
    }
}
