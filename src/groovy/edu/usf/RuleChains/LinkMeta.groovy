/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.usf.RuleChains
import edu.usf.RuleChains.LinkService
import groovy.sql.Sql
/**
 *
 * @author james
 */


class LinkMeta {
    def buildMeta = {grailsApplication->
        LinkService.metaClass.getSourceSession { String name ->
            String sfRoot = "sessionFactory_"
            def sfb = grailsApplication.mainContext.beanDefinitionNames.findAll{ it.startsWith( 'sessionFactory_' ) }.find{ it.endsWith(name) }
            if(!!!!sfb) {
                return grailsApplication.mainContext."${sfb}".currentSession
            }
            return grailsApplication.mainContext."sessionFactory".currentSession
        }     
        LinkService.metaClass.getSQLSource { String name ->
            String sfRoot = "sessionFactory_"
            def sfb = grailsApplication.mainContext.beanDefinitionNames.findAll{ it.startsWith( 'sessionFactory_' ) }.find{ it.endsWith(name) }
            if(!!!!sfb) {
                return new Sql(grailsApplication.mainContext."${sfb}".currentSession.connection())
            }
            return new Sql(grailsApplication.mainContext."sessionFactory".currentSession.connection())                
        }
        LinkService.metaClass.getSQLSources {
            String sfRoot = "sessionFactory_"
            def sfb = grailsApplication.mainContext.beanDefinitionNames.findAll{ it.startsWith( 'sessionFactory_' ) }.collectEntries { b ->
                return [ (b[sfRoot.size()..-1]) : new Sql(grailsApplication.mainContext."${b}".currentSession.connection()) ]
            }
        }
        
    }
	
}

