package uk.ac.manchester.cs.diff.concept.changeset;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class WitnessConcepts {
	private Set<OWLClassExpression> lhsWit, rhsWit;
	
	/**
	 * Constructor
	 * @param lhsWit	Ontology 1 (LHS) witnesses
	 * @param rhsWit	Ontology 2 (RHS) witnesses
	 */
	public WitnessConcepts(Set<OWLClassExpression> lhsWit, Set<OWLClassExpression> rhsWit) {
		this.lhsWit = lhsWit;
		this.rhsWit = rhsWit;
	}
	
	
	/**
	 * Get ontology 1 (LHS) witnesses
	 * @return Ontology 1 witnesses
	 */
	public Set<OWLClassExpression> getLHSWitnesses() {
		return lhsWit;
	}
	
	
	/**
	 * Get ontology 2 (RHS) witnesses
	 * @return Ontology 2 witnesses
	 */
	public Set<OWLClassExpression> getRHSWitnesses() {
		return rhsWit;
	}
	
	
	/**
	 * Check if there are any concept witnesses, either LHS or RHS
	 * @return true if there any witnesses, false otherwise
	 */
	public boolean isEmpty() {
		if(lhsWit.isEmpty() && rhsWit.isEmpty())
			return true;
		else
			return false;
	}
}
