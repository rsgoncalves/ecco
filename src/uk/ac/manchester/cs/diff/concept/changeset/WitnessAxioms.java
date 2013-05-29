package uk.ac.manchester.cs.diff.concept.changeset;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class WitnessAxioms {
	private Set<OWLAxiom> direct, indirect;
	
	/**
	 * Constructor
	 * @param direct	Set of direct witness axioms
	 * @param indirect	Set of indirect witness axioms
	 */
	public WitnessAxioms(Set<OWLAxiom> direct, Set<OWLAxiom> indirect) {
		this.direct = direct;
		this.indirect = indirect;
	}
	
	
	/**
	 * Get the set of direct witness axioms
	 * @return Set of direct witness axioms
	 */
	public Set<OWLAxiom> getDirectWitnesses() {
		return direct;
	}
	
	
	/**
	 * Get the set of indirect witness axioms
	 * @return Set of indirect witness axioms
	 */
	public Set<OWLAxiom> getIndirectWitnesses() {
		return indirect;
	}
	
	
	/**
	 * Check if there are any witness axioms (direct or otherwise)
	 * @return true if this contains any witness axioms
	 */
	public boolean isEmpty() {
		if((direct == null || direct.isEmpty()) && (indirect == null || indirect.isEmpty()))
			return true;
		else
			return false;
	}
}