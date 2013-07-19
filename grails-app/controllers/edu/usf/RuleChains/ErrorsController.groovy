package edu.usf.RuleChains

import grails.plugins.springsecurity.Secured
import grails.converters.*

@Secured(['permitAll'])
class ErrorsController {

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
