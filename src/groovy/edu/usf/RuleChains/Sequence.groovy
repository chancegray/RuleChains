/**
 * 
 */
package edu.usf.RuleChains
/**
 * @author james
 *
 */
class Sequence {
	List<SequenceLink> sequenceLinks = []
	
	
	
	
	
	
	
	static buildSequence(List<Chain> chains,def snippets,def definedServices,def namedQueries) {
		Sequence sequence = new Sequence()
		chains.each { Chain chn ->
			sequence.sequenceLinks.addAll(chn.links.collect { lk ->
				return (lk as SequenceLink).buildLink(snippets,definedServices,namedQueries)
			})
		}
		return sequence
	}
}
