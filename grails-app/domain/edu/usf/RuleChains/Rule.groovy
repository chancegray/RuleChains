package edu.usf.RuleChains
import edu.usf.RuleChains.*
import org.hibernate.FlushMode

abstract class Rule {
    String name
    static belongsTo = [ruleSet: RuleSet]
    static constraints = {
        name(   
                blank: false,
                nullable: false,
                size: 3..255,
                unique: true,
                //Custom constraint - only allow upper, lower, digits, dash and underscore
                validator: { val, obj -> val ==~ /[A-Za-z0-9_.-]+/ && {
                        boolean valid = true;
                        Chain.withNewSession { session ->
                            session.flushMode = FlushMode.MANUAL
                            try {
                                valid =  (obj instanceof Snippet)?!!!!Chain.findByName(val):!!!Chain.findByName(val)
                                if(!valid) {
                                    print "${val} - ${Chain.findByName(val)}"
                                }
                            } finally {
                                session.setFlushMode(FlushMode.AUTO)
                            }
                        }
                        return valid
                    }.call()
                }
            )               

    }
}
