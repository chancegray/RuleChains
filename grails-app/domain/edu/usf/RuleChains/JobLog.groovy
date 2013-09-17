package edu.usf.RuleChains

class JobLog {
    Date logTime = new Date()
    String line
    static belongsTo = [jobHistory: JobHistory]
    static constraints = {
        line(blank:true)
    }
    static mapping = {
        line type: 'text'
    }    
}
