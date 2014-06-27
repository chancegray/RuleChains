package edu.usf.RuleChains

/**
 * JobLog domain class stores JobHistory entries related to an executing rule chain job.
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 */ 
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
