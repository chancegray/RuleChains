package edu.usf.RuleChains
import grails.converters.*
import groovy.lang.GroovyShell
import groovy.lang.Binding
import org.hibernate.ScrollMode

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
            return new GroovyShell(new Binding([
                longSQLplaceHolderUniqueVariable:getSQLSource(sourceName),
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
        return Link.withTransaction{ status ->
            def session = getSourceSession(sourceName)
            // Link."${sourceName}"
            def query = session.createSQLQuery(rule.rule).setResultTransformer(org.hibernate.transform.Transformers.TO_LIST).setReadOnly(true).setFetchSize(Integer.MIN_VALUE).setCacheable(false)
            switch(executeEnum) {
                case ExecuteEnum.EXECUTE_USING_ROW: 
                    switch(rule) {
                        case { it instanceof NamedQuery }:
                            query.setProperties(input.collectEntries { [(it.key): it.value.toString()] })                                
                            break
                        case { it instanceof SQLQuery }:
                            input.eachWithIndex { p,index ->
                                query.setParameter(index, p.toString());
                            }                                                
                            break
                    }
                    break
            }
            switch(resultEnum) {
                case [ ResultEnum.ROW,ResultEnum.APPENDTOROW,ResultEnum.PREPENDTOROW ]:
                    return { s ->
                        def row = (s.next())?s.get():[]
                        s.close()
                        session.flush()
                        return row
                    }.call(query.scroll(ScrollMode.FORWARD_ONLY))
                    break
                case [ ResultEnum.RECORDSET ]: 
                    return { s ->
                        def out = []
                        while(s.next()) {
                            out.add(s.get()[0])
                        }
                        s.close()
                        session.flush() 
                        println "Recordset out is ${out}"
                        return out
                    }.call(query.scroll(ScrollMode.FORWARD_ONLY))                
                    break
                case [ ResultEnum.NONE ]: 
                    return { s ->
                        s.close()
                        session.flush()
                        return []
                    }.call(query.scroll(ScrollMode.FORWARD_ONLY))
                    break
                case [ ResultEnum.UPDATE ]:
                    return [ 
                        { s ->
                            session.flush()
                            return [ s ]
                        }.call(query.executeUpdate()) 
                    ]            
                    break                
            }
        }
    }    
    
}
