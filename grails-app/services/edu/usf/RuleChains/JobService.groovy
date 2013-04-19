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
    // def scheduler
    // def quartzScheduler
    
    
    // def createChainJob(String cronExpression,String name,Map params) { }
    
    // def listChainJobs() {   }
    
//    def listChainTriggers() {        
//        // ChainJob.getTriggers()
//        return [
//            triggers: grailsApplication.getJobClass("ChainJob").getTriggers()
//        ]
////        ((GrailsJobClass) ChainJob.class)
////        getTriggers()
////        List<Trigger> jobTriggers = ChainJob.schedule.getTriggersOfJob(jobKey("jobName", "jobGroup"));
//    }
//    def listChainJobs(String groupName = "") {
//        return [
//            groups: (ChainJob.schedule.getJobGroupNames().contains(groupName)?[ groupName ]:ChainJob.schedule.getJobGroupNames()).collect { g ->
//                return [
//                    group: g,
//                    jobKeys: ChainJob.schedule.getJobKeys(groupEquals(g)).collect { it }
//                ]
//            }
//        ]
//    }
//    
//    def removeChainJob() {
//        ChainJob.unschedule()
//    }
}
