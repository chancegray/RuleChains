package edu.usf.RuleChains

class StoredProcedureQuery extends Rule {
    String rule = ""
    String closure = """ 
{-> 
    rows = [
        [
            
        ]
    ]
}
"""
    static constraints = {
    }
    static mapping = {
        rule type: 'text'
    }    
}
