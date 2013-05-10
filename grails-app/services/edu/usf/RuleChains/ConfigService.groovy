package edu.usf.RuleChains

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import grails.converters.*
import grails.util.DomainBuilder
import groovy.swing.factory.ListFactory
import groovy.json.JsonSlurper

class ConfigService {
    static transactional = true
    def grailsApplication
    def chainService
    def ruleSetService
    
    def importChainData() {
        // def o = JSON.parse(new File('Samples/import.json').text); // Parse a JSON String

    }
}
