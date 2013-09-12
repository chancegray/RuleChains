package edu.usf.RuleChains

class JobHistory {
    String name
    String chain = ""
    String groupName = ""
    String description = ""
    String cron = ""
    Date fireTime
    Date scheduledFireTime
    String jobLog = ""
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
         jobLog(blank:true)
    }
    static mapping = {
        jobLog type: 'text'
    }  
    def appendToLog(String logtext) {
        jobLog = (!!jobLog)?[jobLog,logtext].join("\n"):logtext
        if(!this.save(failOnError:false, flush: true, insert: false, validate: true)) {
            log.error "'${this.errors.fieldError.field}' value '${this.errors.fieldError.rejectedValue}' rejected" 
            def s = ""
            new StringReader( jobLog ).withReader { r ->
                // Drop the lines to make space for the new log entry
                def size = 0
                while(size < (logtext.size()+1)) {
                    size += r.readLine().size()+1
                }
                while( ( line = r.readLine() ) != null ) {
                    s = (!!s)?[s,line].join("\n"):line
                }
            }
            jobLog = s                
            if(!this.save(failOnError:false, flush: true, insert: false, validate: true)) {
                log.error "'${this.errors.fieldError.field}' value '${this.errors.fieldError.rejectedValue}' rejected after reducing the first line" 
            }
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
