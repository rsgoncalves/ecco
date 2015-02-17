package uk.ac.manchester.cs.diff.test;

import uk.ac.manchester.cs.diff.concept.witnesses.WitnessGroup;

public class DiffResult {
	private WitnessGroup lhs, rhs;
	
	/**
	 * Constructor
	 * @param lhs	Ontology 1 witness pack
	 * @param rhs	Ontology 2 witness pack
	 */
	public DiffResult(WitnessGroup lhs, WitnessGroup rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}
	
	
	/**
	 * Get the witness pack for ontology 1
	 * @return Witness pack for ontology 1
	 */
	public WitnessGroup getLHSWitnessPack() {
		return lhs;
	}
	
	
	/**
	 * Get the witness pack for ontology 2
	 * @return Witness pack for ontology 2
	 */
	public WitnessGroup getRHSWitnessPack() {
		return rhs;
	}
}
