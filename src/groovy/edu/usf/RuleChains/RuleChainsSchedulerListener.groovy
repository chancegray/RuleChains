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
 * RuleChainsJobListener is a quartz job listener to intercept job execution and syncronize to the 
 * Git repository. The methods within extend the SchedulerListenerSupport interface
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 */ 
class RuleChainsSchedulerListener extends SchedulerListenerSupport {
    /**
     * Indicates the scheduler has started
     * 
     * @see      JobListenerSupport
     */
    @Override
    public void schedulerStarted() {
        // do something with the event
    }
    /**
     * Indicates the scheduler has shutdown
     * 
     * @see      JobListenerSupport
     */
    @Override
    public void schedulerShutdown() {
        // do something with the event
    }
    /**
     * Indicates a job has been added to the scheduler
     * 
     * @param    jobDetail           A quartz JobDetail object containing the job parameters
     * @see      JobDetail
     * @see      JobListenerSupport
     */
    @Override
    public void jobAdded(JobDetail jobDetail)  {
        // Called by the Scheduler when a JobDetail has been added.
        accessScheduler { qs ->             
            saveGitWithComment(jobDetail,qs.getTriggersOfJob(jobDetail.getKey()).collect { it.getCronExpression() },"Saving Scheduled Job ${jobDetail.getKey().name}")
        }
    }
    /**
     * Indicates a job has been removed from the scheduler
     * 
     * @param    jobKey             The quartz JobKey targeted for removal
     * @see      JobKey
     * @see      JobListenerSupport
     */
    @Override
    public void jobDeleted(JobKey jobKey) {
        // Called by the Scheduler when a JobDetail has been deleted.
    }
    /**
     * Indicates a job has been scheduled
     * 
     * @param    trigger             The quartz trigger
     * @see      Trigger
     * @see      JobListenerSupport
     */
    @Override
    public void jobScheduled(Trigger trigger) {
        accessScheduler { qs -> 
            def jobDetail = qs.getJobDetail(trigger.getJobKey()) 
            saveGitWithComment(jobDetail,qs.getTriggersOfJob(trigger.getJobKey()).collect { it.getCronExpression() },"Saving Scheduled Job ${jobDetail.getKey().name}")
        }
    }
    /**
     * Indicates a job has been unscheduled
     * 
     * @param    triggerKey             The quartz TriggerKey targeted for unscheduling
     * @see      TriggerKey
     * @see      JobListenerSupport
     */
    @Override
    public void jobUnscheduled(TriggerKey triggerKey) {
        // Called by the Scheduler when a JobDetail is unscheduled.
    }
    	
}

