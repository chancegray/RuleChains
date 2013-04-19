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

class BootStrap {
    def grailsApplication
    def quartzScheduler
    def jobService
    def init = { servletContext ->
        def c = new Chain([ name: "TestChain" ])
        if(!c.save(failOnError:true, flush: true, insert: true, validate: true)) {
            c.errors.allErrors.each {
                println it
            }                
        } else {
            println "Created new Chain ${c.name}" 
            def rs = new RuleSet([ name: "TestRuleSet"])
            if(!rs.save(failOnError:true, flush: true, insert: true, validate: true)) {
                rs.errors.allErrors.each {
                    println it
                }                
            } else {
                println "Created new RuleSet ${rs.name}" 
                def sequenceNum = 1
                [
                    [ rule: [name:"SQLTest1", rule: "SELECT 'fart' FROM DUAL"] as SQLQuery, sourceName: "baggageclaim"] as Link,
                    [ rule: [name:"SQLTest2", rule: "INSERT INTO testtable (test) VALUES (?)"] as SQLQuery, sourceName: "baggageclaim"] as Link,
                ].eachWithIndex { l,i ->
                    l.sequenceNumber = i + 1
                    try {
                        if(!rs.addToRules(l.rule).save(failOnError:false, flush: true, validate: true)) {
                            rs.errors.allErrors.each {
                                println it
                            }           
                            println "'${rs.errors.fieldError.field}' value '${rs.errors.fieldError.rejectedValue}' rejected" 
                        } else {
                            println "Created new rule ${l.rule.name} in ${rs.name}"                             
                            try {
                                if(!c.addToLinks(l).save(failOnError:false, flush: true, validate: true)) {
                                    c.errors.allErrors.each {
                                        println it
                                    }
                                    println "'${c.errors.fieldError.field}' value '${c.errors.fieldError.rejectedValue}' rejected" 
                                } else {
                                    println "Created new link ${l.sequenceNumber} in ${c.name}" 
                                    
                                }
                            } catch(Exception ex) {    
                                l.errors.allErrors.each {
                                    println it
                                }           
                                println "'${l.errors.fieldError.field}' value '${l.errors.fieldError.rejectedValue}' rejected" 
                            }
                        }                    
                    } catch(Exception ex) {
                        l.rule.errors.allErrors.each {
                            println it
                        }           
                        println "'${l.rule.errors.fieldError.field}' value '${l.rule.errors.fieldError.rejectedValue}' rejected" 
                    }
                    
                    
                }
//                [
//                    [name:"SQLTest1", rule: "SELECT 'fart' FROM DUAL"] as SQLQuery,
//                    [name:"SQLTest2", rule: "INSERT INTO testtable (test) VALUES (?)"] as SQLQuery
//                ].each { r ->
//                    try {
//                        if(!rs.addToRules(r).save(failOnError:false, flush: true, validate: true)) {
//                            rs.errors.allErrors.each {
//                                println it
//                            }           
//                            println "'${rs.errors.fieldError.field}' value '${rs.errors.fieldError.rejectedValue}' rejected" 
//                        } else {
//                            println "Created new rule ${r.name} in ${rs.name}" 
//                            def l = new Link([rule: r, sequenceNumber: sequenceNum++, sourceName: "baggageclaim"])
//                            try {
//                                if(!c.addToLinks(l).save(failOnError:false, flush: true, validate: true)) {
//                                    c.errors.allErrors.each {
//                                        println it
//                                    }
//                                    println "'${c.errors.fieldError.field}' value '${c.errors.fieldError.rejectedValue}' rejected" 
//                                } else {
//                                    println "Created new link ${l.sequenceNumber} in ${c.name}" 
//                                    
//                                }
//                            } catch(Exception ex) {    
//                                l.errors.allErrors.each {
//                                    println it
//                                }           
//                                println "'${l.errors.fieldError.field}' value '${l.errors.fieldError.rejectedValue}' rejected" 
//                            }
//                        }                    
//                    } catch(Exception ex) {
//                        r.errors.allErrors.each {
//                            println it
//                        }           
//                        println "'${r.errors.fieldError.field}' value '${r.errors.fieldError.rejectedValue}' rejected" 
//                    }
//                }
            }            
        }        
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
                if(!!!sfb) {
                    return grailsApplication.mainContext."${sfb}".currentSession
                }
                return grailsApplication.mainContext."sessionFactory".currentSession
            }          
            LinkService.metaClass.getSQLSource { String name ->
                String sfRoot = "sessionFactory_"
                def sfb = grailsApplication.mainContext.beanDefinitionNames.findAll{ it.startsWith( 'sessionFactory_' ) }.find{ it.endsWith(name) }
                if(!!!sfb) {
                    return new Sql(grailsApplication.mainContext."${sfb}".currentSession.connection())
                }
                return new Sql(grailsApplication.mainContext."sessionFactory".currentSession.connection())                
            }
            LinkService.metaClass.getSQLSources {
                String sfRoot = "sessionFactory_"
                def sfb = grailsApplication.mainContext.beanDefinitionNames.findAll{ it.startsWith( 'sessionFactory_' ) }.collectEntries { b ->
                    return [ "${b.tokenize(sfRoot).last()}" : new Sql(grailsApplication.mainContext."${b}".currentSession.connection()) ]
                }
            }
//        ChainJob.unschedule()
//    }
            print jobService.listChainJobs()
        }
        switch(GrailsUtil.environment){
            case "development":
                println "#### Development Mode (Start Up)"
                Chain chain = new Chain()
                chain.input = ["fart"]
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
