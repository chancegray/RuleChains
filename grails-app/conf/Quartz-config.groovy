import static org.quartz.JobBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;
import static org.quartz.TriggerBuilder.*;
import grails.plugin.quartz2.InvokeMethodJob

grails.plugin.quartz2.autoStartup = true 


org{
    quartz{
        //anything here will get merged into the quartz.properties so you don't need another file
        scheduler.instanceName = 'MyAppScheduler'
        threadPool.class = 'org.quartz.simpl.SimpleThreadPool'
        threadPool.threadCount = 20
        threadPool.threadsInheritContextClassLoaderOfInitializingThread = true
        jobStore.class = 'org.quartz.simpl.RAMJobStore'
    }
}

//quartz {
//    autoStartup = true
//    jdbcStore = false
//    waitForJobsToCompleteOnShutdown = true
//    exposeSchedulerInRepository = false
//
//    props {
//        scheduler.skipUpdateCheck = true
//    }
//}
//
//environments {
//    test {
//        quartz {
//            autoStartup = false
//        }
//    }
//}
