/**
 * 
 */
package edu.usf.RuleChains

/**
 * @author james
 *
 */
class SequenceLink extends Link {
	def input = []
	def output = []
	ServiceTypeEnum serviceTypeEnum;
	def service
	def buildLink(def snippets,def definedServices,def namedQueries) {
		// Evaluate the rule
		serviceTypeEnum = { List<String> t ->
			if(t.size()) {
				return ServiceTypeEnum.byName(t[0].trim().toUpperCase())
			}
			return ServiceTypeEnum.SQLQUERY
		}.call(rule.rule.tokenize(':'))
		switch(serviceTypeEnum) {
			case ServiceTypeEnum.DEFINEDSERVICE:
				service = definedServices.find { DefinedService ds ->
					ds.name.toUpperCase() == rule.rule.tokenize(':')[1].trim().toUpperCase()
				}
				break
			case ServiceTypeEnum.SNIPPET:
				service = Sequence.buildSequence(snippets.find { Snippet sn ->
					sn.name.toUpperCase() == rule.rule.tokenize(':')[1].trim().toUpperCase()
				},snippets,definedServices,namedQueries)
				break
			case ServiceTypeEnum.NAMEDQUERY:
				service = namedQueries.find { NamedQuery nq ->
					nq.name.toUpperCase() == rule.rule.tokenize(':')[1].trim().toUpperCase()
				}
				break
		}
		return this
	}
}
