/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.usf.RuleChains

/**
 *
 * @author james
 */
interface ServiceGatherInterface {
    List<DefinedService> buildReferencedDefinedServices(def chains)
    List<Snippet> buildReferencedSnippets(def chains)
    List<NamedQuery> buildReferencedNamedQueries(def chains)
    Sequence buildSequence(def chains)
}

