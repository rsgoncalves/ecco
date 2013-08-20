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
package uk.ac.manchester.cs.diff.concept;

import uk.ac.manchester.cs.diff.concept.changeset.ConceptChangeSet;
import uk.ac.manchester.cs.diff.output.xml.XMLConceptDiffReport;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public interface ConceptDiff {	
	
	/**
	 * Compute the concept diff and return a change set containing all differences
	 * @return Change set with concept differences
	 */
	public ConceptChangeSet getDiff();
	
	
	/**
	 * Get an XML file which represents the change set 
	 * @return XML file containing the change set
	 */
	public XMLConceptDiffReport getXMLReport();
	
	
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
