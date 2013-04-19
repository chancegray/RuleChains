package edu.usf.RuleChains

class ChainTagLib {
    static namespace = "chain"  
    def chainService
    def chainSelect = {
        out << '<label for="chain" style="padding-right:15px;">Select Chain To View</label>'
        out << g.select(name:"chain",from:chainService.listChains().chains.sort{ it.name },optionKey:"id",optionValue:"name",noSelection:['':'--ALL--'])
    }

}
