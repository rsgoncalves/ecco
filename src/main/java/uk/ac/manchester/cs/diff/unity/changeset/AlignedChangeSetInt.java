package uk.ac.manchester.cs.diff.unity.changeset;

/**
 * @author Rafael S. Goncalves <br>
 * Stanford Center for Biomedical Informatics Research (BMIR) <br>
 * School of Medicine, Stanford University <br>
 */
public interface AlignedChangeSetInt extends ChangeSet {

	/**
	 * Get direct change set
	 * @return Direct change set
	 */
	public AlignedDirectChangeSet getDirectChangeSet();
	
	
	/**
	 * Get indirect change set
	 * @return Indirect change set
	 */
	public AlignedIndirectChangeSet getIndirectChangeSet();
	
}
