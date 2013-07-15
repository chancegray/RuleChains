import grails.util.GrailsNameUtils
import grails.util.GrailsUtil
import edu.usf.RuleChains.Chain
import edu.usf.RuleChains.JobService
import edu.usf.RuleChains.JobController
import edu.usf.RuleChains.LinkService
import edu.usf.RuleChains.SQLQuery
import edu.usf.RuleChains.RuleSet
import edu.usf.RuleChains.Link
import groovy.sql.Sql
import static org.quartz.impl.matchers.GroupMatcher.*
import static org.quartz.TriggerBuilder.*
import grails.plugin.quartz2.ClosureJob
import org.quartz.*
import static org.quartz.CronScheduleBuilder.cronSchedule
import edu.usf.RuleChains.Groovy
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

class BootStrap {
    def grailsApplication
    def quartzScheduler
    def jobService
    def springSecurityService
    def init = { servletContext ->
        if(!!!quartzScheduler) {
            print "Didn't get set!"
        } else {
            // JobService.metaClass.quartzScheduler = quartzScheduler
            JobService.metaClass.listChainJobs  = {->  
                return [ 
                    jobGroups: quartzScheduler.getJobGroupNames().collect { g ->
                        return [
                            name: g,
                            // jobKeys: quartzScheduler.getJobKeys(groupEquals(g)),
                            jobs: quartzScheduler.getJobKeys(groupEquals(g)).collect { jk ->
                                return [
                                    name: jk.name,
                                    triggers: quartzScheduler.getTriggersOfJob(jk).collect { t ->
                                        return t.getCronExpression() 
                                    }                                
                                ]
                            }.findAll {
                                it.triggers.size() > 0
                            }
                        ]
                    }
                ]            
            }
            JobService.metaClass.createChainJob = { String cronExpression,String name,def input = [] ->
                def suffix = System.currentTimeMillis()
                if((quartzScheduler.getJobGroupNames().findAll { g -> return quartzScheduler.getJobKeys(groupEquals(g)).collect { it.name }.contains(name) }.size() > 0)) {
                    def jobKey = quartzScheduler.getJobKeys(
                        groupEquals(quartzScheduler.getJobGroupNames().find { g -> return quartzScheduler.getJobKeys(groupEquals(g)).collect { it.name }.contains(name) })
                    ).find { jk -> return (it.name == name) }                    
                    try {
                        try {
                            def trigger = newTrigger()
                                .withIdentity("${name}:${suffix}")
                                .withSchedule(cronSchedule(cronExpression))
                                .forJob(jobKey)
                                .build();  

                            return [
                                date: quartzScheduler.scheduleJob(trigger)                                
                            ]                        
                        } catch (ex) {
                            return [
                                error: ex.getLocalizedMessage()
                            ]                        
                        }
                    } catch (ex) {                        
                        return [
                            error: ex.getLocalizedMessage()
                        ]                        
                    }
                } else {
                    try {
                        def jobDetail = ClosureJob.createJob(name:"${name}:${suffix}",durability:true,concurrent:false,jobData: [input: input,chain: name]){ jobCtx , appCtx->
                            println "************* it ran ***********"
                            //do something  
                            def chain = Chain.findByName(jobCtx.mergedJobDataMap.get('chain'))
                            if(!!chain) {
                                def result = chain.execute(jobCtx.mergedJobDataMap.get('input'))
                                println "Result is ${result}"
                            } else {
                                println "Chain not found ${jobCtx.mergedJobDataMap.get('chain')}"
                            }
                        }
                        try {
                            def trigger = newTrigger().withIdentity("${name}:${suffix}")
                                .withSchedule(cronSchedule(cronExpression))
                                .build()
                            return [
                                date: quartzScheduler.scheduleJob(jobDetail, trigger)                                
                            ]
                        } catch (ex) {
                            return [
                                error: ex.getLocalizedMessage()
                            ]                        
                        }
                    } catch (ex) {
                        return [
                            error: ex.getLocalizedMessage()
                        ]                        
                    }
                }
            }
            JobService.metaClass.updateChainJob { String name,String newName ->
                def suffix = System.currentTimeMillis()
                if((quartzScheduler.getJobGroupNames().findAll { g -> return quartzScheduler.getJobKeys(groupEquals(g)).collect { jk ->
                        println "${jk.name} and ${name}"
                        jk.name 
                    }.contains(name) }.size() > 0)) {
                    def jobKey = quartzScheduler.getJobKeys(
                        groupEquals(quartzScheduler.getJobGroupNames().find { g -> return quartzScheduler.getJobKeys(groupEquals(g)).collect { it.name }.contains(name) })
                    ).find { jk -> return (jk.name == name) }
                    def jobDataMap = quartzScheduler.getJobDetail(jobKey).getJobDataMap()
                    
                    
                    //.usingJobData("jobSays", "Hello World!")
                    def jobDetail = ClosureJob.createJob(name:"${newName}:${suffix}",durability:true,concurrent:false,jobData: [input: jobDataMap.get("input"),chain: newName]){ jobCtx , appCtx->
                        println "************* it ran ***********"
                        //do something  
                        def chain = Chain.findByName(jobCtx.mergedJobDataMap.get('chain'))
                        if(!!chain) {
                            chain.execute(jobCtx.mergedJobDataMap.get('input'))
                        }
                    }
                    quartzScheduler.scheduleJobs([
                        (jobDetail):  quartzScheduler.getTriggersOfJob(jobKey).collect { t ->
                            return newTrigger()
                            .withIdentity("${name}:${suffix}")
                            .withSchedule(cronSchedule(t.getCronExpression()))
                            .forJob(jobDetail.getKey())
                            .build()
                        }
                    ],true)
                    return [
                        updated: quartzScheduler.deleteJob(jobKey)
                    ]
                }
                return [
                    updated: false
                ]
            }
            JobService.metaClass.removeChainJob { String name ->
                def results = []
                quartzScheduler.getJobGroupNames().findAll { g ->
                    if(quartzScheduler.getJobKeys(groupEquals(g)).collect { it.name }.contains(name)) {
                        return !!!!quartzScheduler.getJobKeys(groupEquals(g)).findAll { jk ->
                            return (jk.name == name)                           
                        }
                    } else {
                        return false
                    }                    
                }.each { g ->
                    quartzScheduler.getJobKeys(groupEquals(g)).findAll { jk ->
                        return (jk.name == name)                           
                    }.each { jk ->
                        // Delete the whole job
                        results << [
                            jobName: jk.name,
                            jobGroup: g,
                            removed: quartzScheduler.deleteJob(jk)
                        ]                            
                    }
                }
                return [ status: results ]
            }
            JobService.metaClass.unscheduleChainJob { String cronExpression, String name ->
                def results = []
                println(cronExpression)
                quartzScheduler.getJobGroupNames().findAll { g ->
                    if(quartzScheduler.getJobKeys(groupEquals(g)).collect { it.name }.contains(name)) {
                        return !!!!quartzScheduler.getJobKeys(groupEquals(g)).findAll { jk ->
                            return ((jk.name == name) && (quartzScheduler.getTriggersOfJob(jk).collect { it.getCronExpression() }.contains(cronExpression)))                            
                        }
                    } else {
                        return false
                    }                    
                }.each { g ->    
                    println(g)
                    quartzScheduler.getJobKeys(groupEquals(g)).findAll { jk ->
                        return ((jk.name == name) && (quartzScheduler.getTriggersOfJob(jk).collect { it.getCronExpression() }.contains(cronExpression)))                            
                    }.each { jk ->
                        if(quartzScheduler.getTriggersOfJob(jk).size() > 1) {
                            // Delete the trigger only
                            quartzScheduler.getTriggersOfJob(jk).findAll { it.getCronExpression() == cronExpression }.collect { t ->
                                results << [
                                    jobName: jk.name,
                                    jobGroup: g,
                                    unscheduled: quartzScheduler.unscheduleJob(t.getKey())
                                ]
                            }                            
                        } else {
                            results << [
                                jobName: jk.name,
                                jobGroup: g,
                                removed: quartzScheduler.deleteJob(jk)
                            ]                            
                        }
                    }
                }
                if(results.size() > 0) {
                    return [ status: results ]
                } else {
                    return [ error: "Did not match an existing trigger!" ]
                }
            }
            JobService.metaClass.rescheduleChainJob { String cronExpression, String newCronExpression, String name ->
                def results = []
                quartzScheduler.getJobGroupNames().findAll { g ->
                    if(quartzScheduler.getJobKeys(groupEquals(g)).collect { it.name }.contains(name)) {
                        return !!!!quartzScheduler.getJobKeys(groupEquals(g)).findAll { jk ->
                            return ((jk.name == name) && (quartzScheduler.getTriggersOfJob(jk).collect { it.getCronExpression() }.contains(cronExpression)))                            
                        }
                    } else {
                        return false
                    }                    
                }.each { g ->
                    quartzScheduler.getJobKeys(groupEquals(g)).findAll { jk ->
                        return ((jk.name == name) && (quartzScheduler.getTriggersOfJob(jk).collect { it.getCronExpression() }.contains(cronExpression)))                            
                    }.each { jk ->
                        quartzScheduler.getTriggersOfJob(jk).findAll { it.getCronExpression() == cronExpression }.each { t ->
                            results << [
                                jobName: jk.name,
                                jobGroup: g,
                                scheduled: quartzScheduler.rescheduleJob(t.getKey(), t.getTriggerBuilder().withSchedule(cronSchedule(newCronExpression)).build())
                            ]                            
                        }
                    }
                }
                return [ status: results ]
            }
            JobService.metaClass.addscheduleChainJob { String cronExpression, String name ->
                def suffix = System.currentTimeMillis()
                if((quartzScheduler.getJobGroupNames().findAll { g -> return quartzScheduler.getJobKeys(groupEquals(g)).collect { jk ->
                        println "${jk.name} and ${name}"
                        jk.name 
                    }.contains(name) }.size() > 0)) {
                    def jobKey = quartzScheduler.getJobKeys(
                        groupEquals(quartzScheduler.getJobGroupNames().find { g -> return quartzScheduler.getJobKeys(groupEquals(g)).collect { it.name }.contains(name) })
                    ).find { jk -> return (jk.name == name) }
                    try {
                        def trigger = newTrigger()
                            .withIdentity("${name.split(":")[0]}:${suffix}")
                            .withSchedule(cronSchedule(cronExpression))
                            .forJob(jobKey)
                            .build()
                        return [
                            date: quartzScheduler.scheduleJob(trigger)                                
                        ]
                    } catch (ex) {
                        return [
                            error: ex.getLocalizedMessage()
                        ]                        
                    }
                }
                return [ error: "Job not found" ]
            }
            JobService.metaClass.mergescheduleChainJob { String mergeName, String name ->
                if((quartzScheduler.getJobGroupNames().findAll { g -> return quartzScheduler.getJobKeys(groupEquals(g)).collect { jk ->
                        println "${jk.name} and ${name}"
                        jk.name 
                    }.contains(name) }.size() > 0) &&
                    (quartzScheduler.getJobGroupNames().findAll { g -> return quartzScheduler.getJobKeys(groupEquals(g)).collect { jk ->
                        println "${jk.name} and ${mergeName}"
                        jk.name 
                    }.contains(name) }.size() > 0)) {
                        def jobKey = quartzScheduler.getJobKeys(
                            groupEquals(quartzScheduler.getJobGroupNames().find { g -> return quartzScheduler.getJobKeys(groupEquals(g)).collect { it.name }.contains(name) })
                        ).find { jk -> return (jk.name == name) }
                        def removedJobKey = quartzScheduler.getJobKeys(
                            groupEquals(quartzScheduler.getJobGroupNames().find { g -> return quartzScheduler.getJobKeys(groupEquals(g)).collect { it.name }.contains(name) })
                        ).find { jk -> return (jk.name == mergeName) }  
                        def results = [
                            mergedTriggers: quartzScheduler.getTriggersOfJob(removedJobKey).findAll{ t -> !!!(t.getCronExpression() in quartzScheduler.getTriggersOfJob(jobKey).collect { it.getCronExpression() }) }.collect { t ->
                                return quartzScheduler.scheduleJob(t.getTriggerBuilder().withIdentity("${name.split(":")[0]}:${System.currentTimeMillis()}").forJob(jobKey).build())
                            }
                        ]
                        results.delete = quartzScheduler.deleteJob(removedJobKey)
                        return results
                }
                return [ error: "One of the jobs was not found" ]
            }
            LinkService.metaClass.getSourceSession { String name ->
                String sfRoot = "sessionFactory_"
                def sfb = grailsApplication.mainContext.beanDefinitionNames.findAll{ it.startsWith( 'sessionFactory_' ) }.find{ it.endsWith(name) }
                if(!!!!sfb) {
                    return grailsApplication.mainContext."${sfb}".currentSession
                }
                return grailsApplication.mainContext."sessionFactory".currentSession
            }          
            LinkService.metaClass.getSQLSource { String name ->
                String sfRoot = "sessionFactory_"
                def sfb = grailsApplication.mainContext.beanDefinitionNames.findAll{ it.startsWith( 'sessionFactory_' ) }.find{ it.endsWith(name) }
                if(!!!!sfb) {
                    return new Sql(grailsApplication.mainContext."${sfb}".currentSession.connection())
                }
                return new Sql(grailsApplication.mainContext."sessionFactory".currentSession.connection())                
            }
            LinkService.metaClass.getSQLSources {
                String sfRoot = "sessionFactory_"
                def sfb = grailsApplication.mainContext.beanDefinitionNames.findAll{ it.startsWith( 'sessionFactory_' ) }.collectEntries { b ->
                    return [ (b[sfRoot.size()..-1]) : new Sql(grailsApplication.mainContext."${b}".currentSession.connection()) ]
                }
            }
            print jobService.listChainJobs()
        }
        switch(GrailsUtil.environment){
            case "development":
                println "#### Development Mode (Start Up)"
                break
            case "test":
                println "#### Test Mode (Start Up)"
                break
            case "production":
                println "#### Production Mode (Start Up)"
                break
        }        
    }
    def destroy = {
        switch(GrailsUtil.environment){
            case "development":
                println "#### Development Mode (Shut Down)"
                break
            case "test":
                println "#### Test Mode (Shut Down)"
                break
            case "production":
                println "#### Production Mode (Shut Down)"
                break
        }        
    }
}
