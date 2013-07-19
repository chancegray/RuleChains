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