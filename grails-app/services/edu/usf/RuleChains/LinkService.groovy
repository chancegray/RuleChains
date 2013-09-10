package edu.usf.RuleChains
import grails.converters.*
import groovy.lang.GroovyShell
import groovy.lang.Binding
import org.hibernate.ScrollMode
import edu.usf.RuleChains.*
import groovy.sql.Sql
import oracle.jdbc.driver.OracleTypes
import groovy.text.*

class LinkService {
    static transactional = true
    def grailsApplication
    
    def casSpringSecurityRest(String serviceUrl,String method = "GET",String username,String password,def headers=[:],def query=[:],String springSecurityBaseUrl) {
        return withCasSpringSecurityRest(
            serviceUrl,
            method,
            username,
            password,
            headers,
            query,
            springSecurityBaseUrl
        )                             
    }
    
    def casRest(String serviceUrl,String method = "GET",String username,String password,def headers=[:],def query=[:]) {
        return withCasRest(
            serviceUrl,
            method,
            username,
            password,
            headers,
            query
        )
    }
    def justRest(String serviceUrl, MethodEnum methodEnum, AuthTypeEnum authTypeEnum,ParseEnum parseEnum, String username,String password,def headers=[:],def query=[:]) {
        try {
            return { o ->
                switch(parseEnum) {
                    case ParseEnum.TEXT:
                        return [ o ]
                        break
                    case ParseEnum.JSON:
                        return JSON.parse(o)
                        break
                    case ParseEnum.XML:
                        return XML.parse(o)
                        break                                        
                }
            }.call(
                withRest(uri: serviceUrl) {
                    if(authTypeEnum in [AuthTypeEnum.BASIC,AuthTypeEnum.DIGEST]) {
                        auth.basic username, password
                    }
                    switch(methodEnum) {
                        case MethodEnum.GET:
                            return get(query: query ,headers: headers, requestContentType : 'application/x-www-form-urlencoded') { sresp, sreader -> 
                                switch(sresp.statusLine.statusCode) {
                                    case 200: 
                                        return sreader.toString()
                                        break
                                    default:
                                        return ""
                                        break
                                }                                                    
                            }
                            break
                        case MethodEnum.POST:
                            return post(body: query ,headers: headers, requestContentType : 'application/x-www-form-urlencoded') { sresp, sreader -> 
                                switch(sresp.statusLine.statusCode) {
                                    case 200: 
                                        return sreader.toString()
                                        break
                                    default:
                                        return ""
                                        break
                                }                                                    
                            }
                            break
                        case MethodEnum.PUT:
                            return put(body: query ,headers: headers, requestContentType : 'application/x-www-form-urlencoded') { sresp, sreader -> 
                                switch(sresp.statusLine.statusCode) {
                                    case 200: 
                                        return sreader.toString()
                                        break
                                    default:
                                        return ""
                                        break
                                }                                                    
                            }
                            break
                        case MethodEnum.DELETE:
                            return delete(query:query ,headers: headers, requestContentType : 'application/x-www-form-urlencoded') { sresp, sreader -> 
                                switch(sresp.statusLine.statusCode) {
                                    case 200: 
                                        return sreader.toString()
                                        break
                                    default:
                                        return ""
                                        break
                                }                                                    
                            }
                            break
                    }
                }
            )        
        } catch(Exception e) {
            log.debug "${rule.name} error: ${e.printStackTrace()} on service ${serviceUrl}"
            System.out.println("${rule.name} error: ${e.printStackTrace()} on service ${serviceUrl}")
            return [
                error: e.message,
                rule: rule.name,
                type: "REST",
                url: serviceUrl
            ]                
        }
    }
    def justGroovy(Rule rule,String sourceName,ExecuteEnum executeEnum,ResultEnum resultEnum,def input) {
        return Link.withTransaction{ status ->
            def sql = getSQLSource(sourceName)
            try {
                return {rows->
                    println "Groovy script raw returned "+(rows as JSON)
                    switch(resultEnum) {
                        case [ ResultEnum.ROW,ResultEnum.APPENDTOROW,ResultEnum.PREPENDTOROW ]:
                            println "Before ${rows as JSON}"
                            println "After ${((rows.size() > 0)?rows[0..0]:rows) as JSON}"
                            return (rows.size() > 0)?rows[0..0]:rows
                            break
                        case [ ResultEnum.RECORDSET ]: 
                            return rows
                            break
                        case [ ResultEnum.NONE,ResultEnum.UPDATE ]:
                            return []
                            break
                    }
                }.call(
                    new GroovyShell(new Binding([
                        longSQLplaceHolderUniqueVariable:sql,
                        longSQLSplaceHolderUniqueVariable:getSQLSources(),
                        longROWplaceHolderVariable: input
                    ])).evaluate("""\
                        def sql = longSQLplaceHolderUniqueVariable
                        def sqls = longSQLSplaceHolderUniqueVariable
                        def row = longROWplaceHolderVariable

                        ${rule.rule}
                    """)    
                )
            } catch(Exception e) {
                log.debug "${rule.name} error: ${e.printStackTrace()} on source named ${sourceName}"
                System.out.println("${rule.name} error: ${e.printStackTrace()} on source named ${sourceName}")
                return [
                    error: e.message,
                    rule: rule.name,
                    type: "Groovy",
                    source: sourceName
                ]                
            }
        }
    }
    def justSQL(def rule,String sourceName,ExecuteEnum executeEnum,ResultEnum resultEnum,def input) {
        Link.withTransaction {
            def sql = getSQLSource(sourceName)
            try {
                 println "Input is "+(input as JSON)
                switch(resultEnum) {
                     case [ ResultEnum.ROW,ResultEnum.APPENDTOROW,ResultEnum.PREPENDTOROW ]:
                         println input as JSON
                         return {rows-> (rows.size() > 0)?rows[0..0]:rows }.call((executeEnum in [ExecuteEnum.EXECUTE_USING_ROW])?sql.rows(rule.rule, input):sql.rows(rule.rule))
                         break
                     case [ ResultEnum.RECORDSET ]: 
                         return ((executeEnum in [ExecuteEnum.EXECUTE_USING_ROW])?sql.rows(rule.rule, input):sql.rows(rule.rule))
                         break
                     case [ ResultEnum.NONE,ResultEnum.UPDATE ]:
                         (executeEnum in [ExecuteEnum.EXECUTE_USING_ROW])?sql.execute(rule.rule, input):sql.execute(rule.rule)
                         return []
                         break
                 }
             } catch(Exception e) {
                 log.debug "${rule.name} error: ${e.printStackTrace()} on source named ${sourceName}"
                 System.out.println("${rule.name} error: ${e.printStackTrace()} on source named ${sourceName}")
                 return [
                     error: e.message,
                     rule: rule.name,
                     type: "SQL",
                     source: sourceName
                 ]
             }
        }
    }
    def justStoredProcedure(def rule,String sourceName,ExecuteEnum executeEnum,ResultEnum resultEnum,def input) {
        Link.withTransaction {
            //println input as JSON
            def sql = getSQLSource(sourceName)
            def binding = new Binding()
            def closure = new GroovyShell(binding).evaluate(rule.closure)
            closure.delegate=this
            // Execute the stored procedure to populate the "rows" bound variable
            try {
                ((executeEnum in [ExecuteEnum.EXECUTE_USING_ROW])?sql.call(rule.rule, input,closure):sql.call(rule.rule, closure))
                switch(resultEnum) {
                    case [ ResultEnum.ROW,ResultEnum.APPENDTOROW,ResultEnum.PREPENDTOROW ]:
                        return {rows-> (rows.size() > 0)?rows[0..0]:rows }.call(binding.rows)
                        break;
                    case [ ResultEnum.RECORDSET ]: 
                        return binding.rows
                        break
                    case [ ResultEnum.NONE,ResultEnum.UPDATE ]:
                        return []
                        break
                }
            } catch(Exception e) {
                log.debug "${rule.name} error: ${e.printStackTrace()} on source named ${sourceName}"
                System.out.println("${rule.name} error: ${e.printStackTrace()} on source named ${sourceName}")
                return [
                    error: e.message,
                    rule: rule.name,
                    type: "Stored Procedure",
                    source: sourceName
                ]
            }
        }
    }
        
}
