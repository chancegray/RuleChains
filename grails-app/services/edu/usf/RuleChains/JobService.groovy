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
                [ jobLogs: JobLog.findAllByJobHistory(jobHistory, [sort: 'logTime', order:'desc', max: records, offset: offset]) ]
            }
            return [ error : "Job History named ${name} not found!"]  
        }
        return [ error: "You must supply a name" ]
    }
    def getJobHistories() {
        return [ 
            jobHistories: JobHistory.list().collect { jh -> 
                def p = [:]
                bindData(p, jh.properties, [exclude: 'jobLogs'])
                return p
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
