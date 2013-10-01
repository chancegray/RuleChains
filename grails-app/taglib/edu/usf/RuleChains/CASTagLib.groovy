package edu.usf.RuleChains
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

class CASTagLib {
    static namespace = "castag"
    def usfCasService;
    def casAttributes = {
        out << "<select name='attributes' id='attributes'>"
        usfCasService.attributes.each { 
            out << "<option>$it</option>"
        }
        out << "</select>"
    }
    def casRoles = {
        out << "<select name='authorities' id='authorities'>"
        SpringSecurityUtils.getPrincipalAuthorities().each { 
            out << "<option>${it.authority}</option>"
        }
        out << "</select>"
    }
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
