package edu.usf.RuleChains

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
                validator: { val, obj -> val ==~ /[A-Za-z0-9_-]+/ && ((obj instanceof Snippet)?(!!!!Chain.findByName(val)):true) }
            )               

    }
}
