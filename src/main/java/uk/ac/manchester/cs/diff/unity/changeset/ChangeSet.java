package uk.ac.manchester.cs.diff.unity.changeset;

/**
 * @author Rafael S. Goncalves <br>
 * Stanford Center for Biomedical Informatics Research (BMIR) <br>
 * School of Medicine, Stanford University <br>
 */
public interface ChangeSet {

	/**
	 * Check if change set is empty
	 * @return true if change set is empty, false otherwise
	 */
	public boolean isEmpty();
	
	
	/**
	 * Get the operation time 
	 * @return Operation time
	 */
	public double getOperationTime();
	
}
