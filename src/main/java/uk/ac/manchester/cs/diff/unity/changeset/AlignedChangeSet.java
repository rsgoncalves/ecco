package uk.ac.manchester.cs.diff.unity.changeset;

import uk.ac.manchester.cs.diff.exception.NotImplementedException;

/**
 * @author Rafael S. Goncalves <br>
 * Stanford Center for Biomedical Informatics Research (BMIR) <br>
 * School of Medicine, Stanford University <br>
 */
public class AlignedChangeSet implements AlignedChangeSetInt {
	private AlignedDirectChangeSet directChangeSet;
	private AlignedIndirectChangeSet indirectChangeSet;
	
	
	/**
	 * Constructor
	 * @param directChangeSet	Direct change set
	 * @param indirectChangeSet	Indirect change set
	 */
	public AlignedChangeSet(AlignedDirectChangeSet directChangeSet, AlignedIndirectChangeSet indirectChangeSet) {
		this.directChangeSet = directChangeSet;
		this.indirectChangeSet = indirectChangeSet;
	}
	
	
	@Override
	public AlignedDirectChangeSet getDirectChangeSet() {
		return directChangeSet;
	}
	

	@Override
	public AlignedIndirectChangeSet getIndirectChangeSet() {
		return indirectChangeSet;
	}


	@Override
	public boolean isEmpty() {
		if(directChangeSet.isEmpty() && indirectChangeSet.isEmpty())
			return true;
		else
			return false;
	}


	@Override
	public double getOperationTime() {
		// TODO: not implemented
		throw new NotImplementedException("not implemented".toUpperCase());
	}
}
