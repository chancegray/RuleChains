package edu.usf.RuleChains



class ChainJob {
//    static triggers = {
//      simple repeatInterval: 5000l // execute job once in 5 seconds
//    }

    def execute(context) {
        // execute job
        def chain = Chain.findByName(context.mergedJobDataMap.get('name'))
        def input = Chain.findByName(context.mergedJobDataMap.get('input'))
        //println context.mergedJobDataMap.get('name')
        if(!!chain) {
            chain.execute(input)
        }
    }
}
