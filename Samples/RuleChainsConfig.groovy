grails.plugins.springsecurity.securityConfigType = "InterceptUrlMap"

grails.plugins.springsecurity.interceptUrlMap = [
    // Basic resources
    '/js/**':                       ['permitAll'],
    '/css/**':                      ['permitAll'],
    '/images/**':                   ['permitAll'],
    // Front Page
    '/*':                           ['isFullyAuthenticated()'],
    // Basic handling for errors and auth
    '/error':                       ['permitAll'],
    '/errors/**':                   ['permitAll'],
    '/login/**':                    ['permitAll'],
    '/logout/**':                   ['permitAll'],
    // Built in services
    '/source/':                     ['isFullyAuthenticated()'],
    '/ruleSet/':                    ['isFullyAuthenticated()'],
    '/ruleSet/*':                   ['isFullyAuthenticated()'],
    '/ruleSet/*/*':                 ['isFullyAuthenticated()'],
    '/ruleSet/*/*/*':               ['isFullyAuthenticated()'],
    '/chain/':                      ['isFullyAuthenticated()'],
    '/chain/*':                     ['isFullyAuthenticated()'],
    '/chain/*/*':                   ['isFullyAuthenticated()'],
    '/job/':                        ['isFullyAuthenticated()'],
    '/job/*':                       ['isFullyAuthenticated()'],
    '/job/*/*':                     ['isFullyAuthenticated()'],
    '/chainServiceHandler/':        ['isFullyAuthenticated()'],
    '/chainServiceHandler/*':       ['isFullyAuthenticated()'],
    '/backup/download/':            ["hasRole('ROLE_ITPRSUPERVISOR')"],
    '/backup/upload/':              ["hasRole('ROLE_ITPRSUPERVISOR')"],
    // Definable services tied to a rule chain
    '/service/testServicehandler/': ["hasRole('ROLE_SOME_ROLE_FOR_DEFINEDSERVICE')"]
]
environments {
    production {
        grails.serverURL = "http://somedomain.org:8080/RuleChains"
        grails.plugins.springsecurity.cas.serverUrlPrefix = 'https://authtest.it.usf.edu'
    }
    development {
        grails.serverURL = "http://localhost:8080/RuleChains"
        grails.plugins.springsecurity.cas.serverUrlPrefix = 'https://authtest.it.usf.edu'
    }
    test {
        grails.serverURL = "http://localhost:8080/RuleChains"
        grails.plugins.springsecurity.cas.serverUrlPrefix = 'https://authtest.it.usf.edu'
    }
}

// Added by the Spring Security CAS (USF) plugin:
grails.plugins.springsecurity.userLookup.userDomainClassName = 'edu.usf.cims.UsfCasUser'
grails.plugins.springsecurity.cas.active = true
grails.plugins.springsecurity.cas.sendRenew = false
grails.plugins.springsecurity.cas.key = '9a3433aca7184008df30ee8f5c62f160' //unique value for each app
grails.plugins.springsecurity.cas.artifactParameter = 'ticket'
grails.plugins.springsecurity.cas.serviceParameter = 'service'
grails.plugins.springsecurity.cas.filterProcessesUrl = '/j_spring_cas_security_check'
grails.plugins.springsecurity.cas.proxyCallbackUrl = "${grails.serverURL}/secure/receptor"
grails.plugins.springsecurity.cas.proxyReceptorUrl = '/secure/receptor'
grails.plugins.springsecurity.cas.useSingleSignout = false
grails.plugins.springsecurity.cas.driftTolerance = 120000
grails.plugins.springsecurity.cas.loginUri = '/login'
grails.plugins.springsecurity.cas.useSamlValidator = true
grails.plugins.springsecurity.cas.authorityAttribute = 'eduPersonEntitlement'
grails.plugins.springsecurity.cas.serviceUrl = "${grails.serverURL}/j_spring_cas_security_check"