    /** SSL key & truststore configuration key */
    rest.https.truststore.path = 'resources/certs/rest_client_keystore.jks'
    rest.https.keystore.path='resources/certs/rest_client_keystore.jks'
    /** Certificate Hostname Verifier configuration key */
    rest.https.cert.hostnameVerifier = 'ALLOW_ALL'
    /** Enforce SSL Socket Factory */
    rest.https.sslSocketFactory.enforce = true

    casRestClient.cas.server = "https://authtest.it.usf.edu"
    casRestClient.cas.ticketsPath = "/v1/tickets"        
    casRestClient.cas.username = "myusername"    
    casRestClient.cas.password = "mypassword"