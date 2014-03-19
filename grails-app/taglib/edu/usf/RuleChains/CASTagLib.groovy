package edu.usf.RuleChains
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

/**
 * CASTagLib is a taglib for CAS GSP views.
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 */ 
class CASTagLib {
    static namespace = "castag"
    def usfCasService
    /**
     * Provides a GSP method to produce a HTML select element of assigned CAS attributes.
     * 
     * @return A list of assigned CAS attributes formatted in an HTML select element
     */             
    def casAttributes = {
        out << "<select name='attributes' id='attributes'>"
        usfCasService.attributes.each { 
            out << "<option>$it</option>"
        }
        out << "</select>"
    }
    /**
     * Provides a GSP method to produce a HTML select element of assigned CAS roles.
     * 
     * @return A list of assigned CAS roles formatted in an HTML select element
     */             
    def casRoles = {
        out << "<select name='authorities' id='authorities'>"
        SpringSecurityUtils.getPrincipalAuthorities().each { 
            out << "<option>${it.authority}</option>"
        }
        out << "</select>"
    }
    /**
     * Provides a GSP method to produce a HTML button element of for CAS logout.
     * 
     * @return A HTML button element
     */             
    def casLogoutButton = {
        out << g.link(controller: "logout",action: "index",elementId: "casLogoutButton",class:"ui-btn-right",style:"float:right") {
            out << "Sign Out: ${sec.username()}"
        }
        out << r.script() {
            out << '''
                var button = $("#casLogoutButton").button();
                button.button("option","text",true)
            '''
        }
    }
}
