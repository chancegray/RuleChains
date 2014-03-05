package edu.usf.RuleChains

/**
 * ChainServiceHandlerTagLib is a taglib for ChainServiceHandler GSP views.
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 */ 
class ChainServiceHandlerTagLib {
    static namespace = "chainServiceHandler" 
    def chainServiceHandlerService
    /**
     * Provides a GSP method to produce a HTML select element of available chain service handlers.
     * 
     * @return A list of available chain service handlers formatted in an HTML select element
     */         
    def chainServiceHandlerSelect = {
        out << '<label for="chain" style="padding-right:15px;">Select Chain Service Handler To View</label>'
        out << g.select(name:"chainServiceHandle",from:chainServiceHandlerService.listChainServiceHandlers().chainServiceHandlers.sort{ it.name },optionKey:"id",optionValue:"name",noSelection:['':'--ALL--'])
    }

}
