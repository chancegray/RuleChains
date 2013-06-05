package edu.usf.RuleChains

class ChainServiceHandlerTagLib {
    static namespace = "chainServiceHandler" 
    def chainServiceHandlerService
    def chainServiceHandlerSelect = {
        out << '<label for="chain" style="padding-right:15px;">Select Chain To View</label>'
        out << g.select(name:"chain",from:chainService.listChains().chains.sort{ it.name },optionKey:"id",optionValue:"name",noSelection:['':'--ALL--'])
    }

}
