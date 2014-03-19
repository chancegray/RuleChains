/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.usf.RuleChains

import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.JobListener
/**
 * RuleChainsJobListener is a quartz job listener to intercept job execution and syncronize to the 
 * Git repository. The methods within extend the JobListenerSupport interface
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 */ 
class RuleChainsJobListener {
    /**
     * Retrieves the name of the listener
     * 
     * @return   The string name of the listener.
     * @see      JobListenerSupport
     */
    public String getName() {
        return "RuleChainsJobListener"
    }
    /**
     * Markes the beginning of the quartz job execution
     * 
     * @param   context    The Quartz jobExecutionContext
     * @see     JobExecutionContext
     * @see     JobListenerSupport
     */
    public void jobToBeExecuted(JobExecutionContext context) {
        // saveGitWithComment(context,"Saving Scheduled Job ${context.getJobDetail().getKey().name}")
        System.out.println("jobToBeExecuted has executed. "+new Date());
    }
    /**
     * Markes the rejection of the quartz job execution
     * 
     * @param   context    The Quartz jobExecutionContext
     * @see     JobExecutionContext
     * @see     JobListenerSupport
     */
    public void jobExecutionVetoed(JobExecutionContext context) {
        System.out.println("jobExecutionVetoed has execute. "+new Date());
    }
    /**
     * Markes the completion of the quartz job execution
     * 
     * @param   context    The Quartz jobExecutionContext
     * @see     JobExecutionContext
     * @see     JobListenerSupport
     */
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        if(context.getNextFireTime()) {
            // Update (just use save here)
            saveGitWithComment(context,"Updating Scheduled Job ${context.getJobDetail().getKey().name}")
        } else {
            // Delete
            deleteGitWithComment(context,"Removing Scheduled Job ${context.getJobDetail().getKey().name}")
        }
        System.out.println("jobWasExecuted has executed. "+new Date()+"\r\n");
    }    
    
}

