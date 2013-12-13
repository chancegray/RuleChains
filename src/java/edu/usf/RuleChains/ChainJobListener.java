/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.usf.RuleChains;

import java.util.Date;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
/**
 * This Job Listener is mainly to trigger an update for Git synchronization
 * @author james
 */
public class ChainJobListener implements JobListener {
    public String getName() {
        return "ChainJobListener";
    }
 
    public void jobToBeExecuted(JobExecutionContext context) {
        System.out.println("jobToBeExecuted has execute. "+new Date());
    }
 
    public void jobExecutionVetoed(JobExecutionContext context) {
        System.out.println("jobExecutionVetoed has execute. "+new Date());
    }
 
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        System.out.println("jobWasExecuted has execute. "+new Date()+"\r\n");
        syncronizeGitWithComment();
    }    
    
    public void syncronizeGitWithComment() {}
}
