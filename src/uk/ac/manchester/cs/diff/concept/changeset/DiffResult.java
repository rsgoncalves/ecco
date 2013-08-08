package uk.ac.manchester.cs.diff.concept.changeset;

public class DiffResult {
	private WitnessPack lhs, rhs;
	
	/**
	 * Constructor
	 * @param lhs	Ontology 1 witness pack
	 * @param rhs	Ontology 2 witness pack
	 */
	public DiffResult(WitnessPack lhs, WitnessPack rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}
	
	
	/**
	 * Get the witness pack for ontology 1
	 * @return Witness pack for ontology 1
	 */
	public WitnessPack getLHSWitnessPack() {
		return lhs;
	}
	
	
	/**
	 * Get the witness pack for ontology 2
	 * @return Witness pack for ontology 2
	 */
	public WitnessPack getRHSWitnessPack() {
		return rhs;
	}
}
