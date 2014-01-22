package edu.usf.RuleChains

/**
 * JobTagLib is a taglib for Quart Job GSP views.
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 */ 
class JobTagLib {
    static namespace = "job"  
    def jobService
    /**
     * Provides a GSP method to produce a HTML select element of available job histories.
     * 
     * @return A list of available job histories formatted in an HTML select element
     */         
    def jobHistorySelect = {
        out << '<label for="jobHistories" style="padding-right:15px;">Select Job History To View</label>'
        out << g.select(name:"jobHistory",from:jobService.getJobHistories().jobHistories.sort{ it.name },optionKey:"name",optionValue:"name",noSelection:['':'--ALL--'])
    }

}