package edu.usf.RuleChains
import grails.converters.*
import groovy.lang.GroovyShell
import groovy.lang.Binding
import org.hibernate.ScrollMode
import edu.usf.RuleChains.*
import groovy.sql.Sql

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
    }
    def justGroovy(Rule rule,String sourceName,ExecuteEnum executeEnum,ResultEnum resultEnum,def input) {
        return Link.withTransaction{ status ->
            def sql = getSQLSource(sourceName)
            return new GroovyShell(new Binding([
                longSQLplaceHolderUniqueVariable:sql,
                longSQLSplaceHolderUniqueVariable:getSQLSources(),
                longROWplaceHolderVariable: input
            ])).evaluate("""\
                def sql = longSQLplaceHolderUniqueVariable
                def sqls = longSQLSplaceHolderUniqueVariable
                def row = longROWplaceHolderVariable

                ${rule.rule}
            """)    
        }
    }
    def justSQL(Rule rule,String sourceName,ExecuteEnum executeEnum,ResultEnum resultEnum,def input) {
        Link.withTransaction {
            def sql = getSQLSource(sourceName)
            switch(resultEnum) {
                case [ ResultEnum.ROW,ResultEnum.APPENDTOROW,ResultEnum.PREPENDTOROW ]:
                    return ((executeEnum in [ExecuteEnum.EXECUTE_USING_ROW])?sql.rows(rule.rule, input):sql.rows(rule.rule))[0..<2]
                    break
                case [ ResultEnum.RECORDSET ]: 
                    return ((executeEnum in [ExecuteEnum.EXECUTE_USING_ROW])?sql.rows(rule.rule, input):sql.rows(rule.rule))
                    break
                case [ ResultEnum.NONE,ResultEnum.UPDATE ]:
                    (executeEnum in [ExecuteEnum.EXECUTE_USING_ROW])?sql.execute(rule.rule, input):sql.execute(rule.rule)
                    return []
                    break
            }
        }
    }
    def justStoredProcedure(Rule rule,String sourceName,ExecuteEnum executeEnum,ResultEnum resultEnum,def input) {
        Link.withTransaction {
            //println input as JSON
            def sql = getSQLSource(sourceName)
            def binding = new Binding()
            def closure = new GroovyShell(binding).evaluate(rule.closure)
            closure.delegate=this
            // Execute the stored procedure to populate the "rows" bound variable
            ((executeEnum in [ExecuteEnum.EXECUTE_USING_ROW])?sql.call(rule.rule, input,closure):sql.call(rule.rule, closure))
            switch(resultEnum) {
                case [ ResultEnum.ROW,ResultEnum.APPENDTOROW,ResultEnum.PREPENDTOROW ]:
                    return binding.rows[0..<2]
                    break;
                case [ ResultEnum.RECORDSET ]: 
                    return binding.rows
                    break
                case [ ResultEnum.NONE,ResultEnum.UPDATE ]:
                    return []
                    break
            }
        }
    }
        
}
