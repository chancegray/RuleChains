package edu.usf.RuleChains

class RuleSet {
    String name
    static hasMany = [rules:Rule]
    static constraints = {
        name(
            blank: false,
            nullable: false,
            unique: true,
            size: 3..255,
            //Custom constraint - only allow upper, lower, digits, dash and underscore
            validator: { val, obj -> val ==~ /[A-Za-z0-9_-]+/ }
        )
    }        
}
