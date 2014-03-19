package edu.usf.RuleChains

/**
 * A simple Quartz Job for handling scheduled RuleChain execution
 * <p>
 * Developed originally for the University of South Florida
 * 
 * @author <a href='mailto:james@mail.usf.edu'>James Jones</a> 
 */

class ChainJob {
//    static triggers = {
//      simple repeatInterval: 5000l // execute job once in 5 seconds
//    }
    /**
     * The base execution method which extracts the chain name and executes it
     * 
     * @param   context    The quartz scheduler context for the scheduled job
     */
    def execute(context) {
        // execute job
        def chain = Chain.findByName(context.mergedJobDataMap.get('name'))
        def input = Chain.findByName(context.mergedJobDataMap.get('input'))
        //println context.mergedJobDataMap.get('name')
        if(!!chain) {
            chain.execute((!!input)?input:[[:]])
        }
    }
}
