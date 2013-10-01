package edu.usf.RuleChains
//import grails.plugins.quartz.GrailsJobClassConstants as Constants
//import static org.quartz.JobBuilder.*;
//import static org.quartz.TriggerBuilder.*;
//import static org.quartz.SimpleScheduleBuilder.*;
//import static org.quartz.CronScheduleBuilder.*;
//import static org.quartz.CalendarIntervalScheduleBuilder.*;
//import static org.quartz.JobKey.*;
//import static org.quartz.TriggerKey.*;
//import static org.quartz.DateBuilder.*;
//import static org.quartz.impl.matchers.KeyMatcher.*;
//import static org.quartz.impl.matchers.GroupMatcher.*;
//import static org.quartz.impl.matchers.AndMatcher.*;
//import static org.quartz.impl.matchers.OrMatcher.*;
//import static org.quartz.impl.matchers.EverythingMatcher.*;
//import static org.quartz.TriggerBuilder.newTrigger;
import org.hibernate.criterion.CriteriaSpecification
import groovy.time.*
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.impl.matchers.GroupMatcher.*
import grails.plugin.quartz2.InvokeMethodJob    
import grails.plugin.quartz2.ClosureJob    

class JobService {
    static transactional = true
    def grailsApplication
    
    def addJobHistory(String name) {
        if(!!name) {
            def jobHistory = [ name: name.trim() ] as JobHistory
            if(!jobHistory.save(failOnError:false, flush: true, insert: true, validate: true)) {
                return [ error : "'${jobHistory.errors.fieldError.field}' value '${jobHistory.errors.fieldError.rejectedValue}' rejected" ]
            } else {
                return [ jobHistory: jobHistory ]
            }
        }
        return [ error: "You must supply a name" ]
    }
    def findJobHistory(String name) {
        if(!!name) {
            def jobHistory = JobHistory.findByName(name.trim())
            if(!!jobHistory) {
                return [ jobHistory: jobHistory ]
            }
            return [ error : "Job History named ${name} not found!"]            
        }
        return [ error: "You must supply a name" ]
    }
    def getJobLogs(String name,Integer records = 20,Integer offset = 0) {
        if(!!name) {
            def jobHistory = JobHistory.findByName(name.trim())
            if(!!jobHistory) {
                return [
                    jobLogs: JobLog.createCriteria().list(sort: 'id', order:'desc', max: records, offset: offset) {
                        eq('jobHistory',jobHistory)
                        resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                        projections {
                            property('logTime', 'logTime')
                            property('line', 'line')
                            property('id','id')
                        }
                    },
                    jobHistories: getJobHistories().jobHistories,
                    total: JobLog.countByJobHistory(jobHistory)
                ]
            }
            return [ error : "Job History named ${name} not found!"]  
        }
        return [ error: "You must supply a name" ]
    }
    def getJobRuleTimings(String name,Integer records = 20,Integer offset = 0) {
        if(!!name) {
            def jobHistory = JobHistory.findByName(name.trim())
            if(!!jobHistory) {
                def endTime
                return [
                    jobLogs: { jls ->                        
                        endTime = JobLog.createCriteria().get {
                            eq('jobHistory',jobHistory)
                            gt('id',jls.last().id)
                            or {
                                like('line','[%] Detected a % for%')
                                like('line','[Finished] %')
                            }
                            projections {
                                min('logTime')
                            }
                        }
                        return jls.reverse().collect { jl ->
                            jl.duration = TimeCategory.minus(endTime, jl.logTime).toString()
                            endTime = jl.logTime
                            jl.ruleName = jl.line.tokenize().last()
                            return jl                            
                        }.reverse()                        
                    }.call(JobLog.createCriteria().list(sort: 'id', order:'asc', max: records, offset: offset) {
                        eq('jobHistory',jobHistory)
                        like('line','[%] Detected a % for%')
                        resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                        projections {
                            property('logTime', 'logTime')
                            property('line', 'line')
                            property('id','id')
                        }
                    }),
                    jobHistories: getJobHistories().jobHistories,
                    total: JobLog.countByJobHistoryAndLineLike(jobHistory,'[%] Detected a % for%')
                ]
            }
            return [ error : "Job History named ${name} not found!"]             
        }
        return [ error: "You must supply a name" ]
    }
    def getJobHistories() {
        return [ 
            jobHistories: JobHistory.withCriteria {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property('id', 'id')
                    property('name', 'name')
                    property('chain', 'chain')
                    property('groupName', 'groupName')
                    property('description', 'description')
                    property('cron', 'cron')
                    property('fireTime', 'fireTime')
                    property('scheduledFireTime', 'scheduledFireTime')
                }                
            }.collect { jh ->
                def jobHistory = JobHistory.findByName(jh.name)
                jh.endTime = JobLog.createCriteria().get {
                    eq('jobHistory',jobHistory)
                    projections {
                        max("logTime")
                    }
                }
                jh.startTime = JobLog.createCriteria().get {
                    eq('jobHistory',jobHistory)
                    projections {
                        min("logTime")
                    }
                }
                jh.duration = TimeCategory.minus(jh.endTime, jh.startTime).toString()
                return jh
            }
        ]
    }
    def deleteJobHistory(String name) {
        if(!!name) {
            def jobHistory = JobHistory.findByName(name.trim())
            if(!!jobHistory) {
                jobHistory.delete()
                return [ success : "Job History deleted" ]
            }
            return [ error : "Job History named ${name} not found!"]
        }
        return [ error: "You must supply a name" ]
    }
}
