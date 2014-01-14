/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.usf.RuleChains

import org.quartz.Trigger
import org.quartz.TriggerKey
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.listeners.SchedulerListenerSupport
/**
 *
 * @author james
 */
class RuleChainsSchedulerListener extends SchedulerListenerSupport {

    @Override
    public void schedulerStarted() {
        // do something with the event
    }

    @Override
    public void schedulerShutdown() {
        // do something with the event
    }
    
    @Override
    public void jobAdded(JobDetail jobDetail)  {
        // Called by the Scheduler when a JobDetail has been added.
        accessScheduler { qs ->             
            saveGitWithComment(jobDetail,qs.getTriggersOfJob(jobDetail.getKey()).collect { it.getCronExpression() },"Saving Scheduled Job ${jobDetail.getKey().name}")
        }
    }
    
    @Override
    public void jobDeleted(JobKey jobKey) {
        // Called by the Scheduler when a JobDetail has been deleted.
    }

    @Override
    public void jobScheduled(Trigger trigger) {
        accessScheduler { qs -> 
            def jobDetail = qs.getJobDetail(trigger.getJobKey()) 
            saveGitWithComment(jobDetail,qs.getTriggersOfJob(trigger.getJobKey()).collect { it.getCronExpression() },"Saving Scheduled Job ${jobDetail.getKey().name}")
        }
    }
    
    @Override
    public void jobUnscheduled(TriggerKey triggerKey) {
        // Called by the Scheduler when a JobDetail is unscheduled.
    }
    	
}

