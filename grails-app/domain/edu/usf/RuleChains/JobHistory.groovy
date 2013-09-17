package edu.usf.RuleChains

class JobHistory {
    String name
    String chain = ""
    String groupName = ""
    String description = ""
    String cron = ""
    Date fireTime
    Date scheduledFireTime
    static hasMany = [jobLogs:JobLog]
    static constraints = {
         name(   
             blank: false,
             nullable: false,
             size: 3..255,
             unique: true,
         )        
         chain(blank:true)
         groupName(blank:true)
         description(blank:true)
         cron(blank:true)
         fireTime(nullable:true)
         scheduledFireTime(nullable:true)
    }
    def appendToLog(String logtext) {
        if(!this.addToJobLogs(new JobLog(line: logtext)).save(failOnError:false,flush:true,validate:true)) {
            log.error "'${this.errors.fieldError.field}' value '${this.errors.fieldError.rejectedValue}' rejected" 
        }
    }
    def updateJobProperties(def jobCtx) {
        chain = jobCtx.getJobDetail().getJobDataMap().get("chain")
        description = (!!!!jobCtx.getJobDetail().getDescription())?jobCtx.getJobDetail().getDescription():""
        groupName = jobCtx.getJobDetail().getKey().getGroup()
        cron = { t ->
            return t.metaClass.respondsTo(t, 'getCronExpression')?t.getCronExpression():""
        }.call(jobCtx.getTrigger())
        fireTime = jobCtx.getFireTime()
        scheduledFireTime = jobCtx.getScheduledFireTime()    
        if(!this.save(failOnError:false, flush: true, insert: false, validate: true)) {
            log.error "'${this.errors.fieldError.field}' value '${this.errors.fieldError.rejectedValue}' rejected" 
        }
   }
}
