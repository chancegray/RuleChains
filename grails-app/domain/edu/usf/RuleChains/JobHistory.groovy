package edu.usf.RuleChains

class JobHistory {
    String name
    String groupName = ""
    String description = ""
    String cron = ""
    Date fireTime
    Date scheduledFireTime
    String log = ""
    static constraints = {
         name(   
             blank: false,
             nullable: false,
             size: 3..255,
             unique: true,
         )        
         groupName(blank:true)
         description(blank:true)
         cron(blank:true)
         fireTime(nullable:true)
         scheduledFireTime(nullable:true)
         log(
             blank:true
         )
    }
    static mapping = {
        log type: 'text'
    }  
    def appendToLog(String logtext) {
        log = [log,logtext].join("\n")
    }
}
