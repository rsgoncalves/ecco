/*******************************************************************************
 * This file is part of ecco.
 * 
 * ecco is distributed under the terms of the GNU Lesser General Public License (LGPL), Version 3.0.
 *  
 * Copyright 2011-2013, The University of Manchester
 *  
 * ecco is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *  
 * ecco is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even 
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
 * General Public License for more details.
 *  
 * You should have received a copy of the GNU Lesser General Public License along with ecco.
 * If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package uk.ac.manchester.cs.diff.axiom;

import uk.ac.manchester.cs.diff.axiom.changeset.ChangeSet;
import uk.ac.manchester.cs.diff.output.XMLReport;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public interface AxiomDiff {	
	/**
	 * Compute the diff and return a change set containing all differences
	 * @return Change set with the differences
	 */
	public ChangeSet getDiff();
	
	
	/**
	 * Check if the given ontologies are equivalent w.r.t. the (instantiated) diff's equivalence notion
	 * @return true if ontologies are equivalent, false otherwise
	 */
	public boolean isEquivalent();
	
	
	/**
	 * Get an XML file which represents the change set 
	 * @return XML file containing the change set
	 */
	public XMLReport getXMLReport();
	
	
	/**
	 * Get a summary CSV file containing the number and types of changes
	 * @return CSV file containing a summary of the number and types of changes
	 */
	public String getCSVChangeReport();
	
	
	/**
	 * Print the diff results to stdout
	 */
	public void printDiff();
}
