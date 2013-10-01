package edu.usf.RuleChains

class JobTagLib {
    static namespace = "job"  
    def jobService
    def jobHistorySelect = {
        out << '<label for="jobHistories" style="padding-right:15px;">Select Job History To View</label>'
        out << g.select(name:"jobHistory",from:jobService.getJobHistories().jobHistories.sort{ it.name },optionKey:"name",optionValue:"name",noSelection:['':'--ALL--'])
    }

}