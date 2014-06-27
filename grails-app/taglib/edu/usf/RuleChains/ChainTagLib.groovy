package edu.usf.RuleChains

/**
 * ChainTagLib is a taglib for Chain GSP views.
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 */ 
class ChainTagLib {
    static namespace = "chain"  
    def chainService
    /**
     * Provides a GSP method to produce a HTML select element of available chains.
     * 
     * @return A list of available chains formatted in an HTML select element
     */         
    def chainSelect = {
        out << '<label for="chain" style="padding-right:15px;">Select Chain To View</label>'
        out << g.select(name:"chain",from:chainService.listChains().chains.sort{ it.name },optionKey:"id",optionValue:"name",noSelection:['':'--ALL--'])
    }

}
