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
 *
 * @author james
 */
class RuleChainsJobListener {
    public String getName() {
        return "RuleChainsJobListener"
    }
    public void jobToBeExecuted(JobExecutionContext context) {
        // saveGitWithComment(context,"Saving Scheduled Job ${context.getJobDetail().getKey().name}")
        System.out.println("jobToBeExecuted has executed. "+new Date());
    }
 
    public void jobExecutionVetoed(JobExecutionContext context) {
        System.out.println("jobExecutionVetoed has execute. "+new Date());
    }
 
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

