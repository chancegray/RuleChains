package edu.usf.RuleChains

import grails.plugins.springsecurity.Secured
import grails.converters.*

/**
 * ErrorsController provides for REST services handling of errors
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 */ 
@Secured(['permitAll'])
class ErrorsController {
    /**
     * Handles error 403
     */
    def error403 = {
        withFormat {
            html {
                render view: '/errors/error403'
            }
            xml {
                render ([
                    error: "Access Denied. We're sorry, but you are not authorized to perform the requested operation."
                ] as Map) as XML
            }
            json {
                JSON.use("deep") { render ([
                    error: "Access Denied. We're sorry, but you are not authorized to perform the requested operation."
                ] as Map) as JSON }
            }
        }                            
    }
    /**
     * Handles error 404
     */
    def error404 = {
        withFormat {
            html {
                render view: '/errors/error404'
            }
            xml {
                render ([
                    error: "Access Denied. We're sorry, but you are not authorized to perform the requested operation."
                ] as Map) as XML
            }
            json {
                JSON.use("deep") { render ([
                    error: "Access Denied. We're sorry, but you are not authorized to perform the requested operation."
                ] as Map) as JSON }
            }
        }                                    
    }
    /**
     * Handles error 500
     */
    def error500 = {
        withFormat {
            html {
                render view: '/error'
            }
            xml {
                render ([
                    error: "${params.exception}"
                ] as Map) as XML
            }
            json {
                JSON.use("deep") { render ([
                    error: "${params.exception}"
                ] as Map) as JSON }
            }
        }                                    
    }

}
