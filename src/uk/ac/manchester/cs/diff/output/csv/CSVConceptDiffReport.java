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
package uk.ac.manchester.cs.diff.output.csv;

import uk.ac.manchester.cs.diff.concept.changeset.ConceptChangeSet;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class CSVConceptDiffReport {
	private String header, row;
	
	/**
	 * Constructor
	 */
	public CSVConceptDiffReport() {
		header = "\n"; row = "";
	}
	
	
	/**
	 * Get a CSV-formatted change report
	 * @param changeSet	Change set
	 * @return Change report as a CSV-formatted string
	 */
	public String getReport(ConceptChangeSet changeSet) {
		header += "Ontology 1,Ontology 2,Specialised,Generalised,Affected,Entailment diff time,Specialisations,,,,,Generalisations,,,,,Partitioning time\n";
		header += ",,,,,,Direct,Indirect,Mix,Purely Direct,Purely Indirect,Direct,Indirect,Mix,Purely Direct,Purely Indirect";
	
		row +=	"ont1,ont2" +
				"," + changeSet.getAllSpecialisedConcepts().size() + 
				"," + changeSet.getAllGeneralisedConcepts().size() +
				"," + changeSet.getAllAffectedConcepts().size() +
				"," + changeSet.getEntailmentDiffTime() +
				"," + changeSet.getAllDirectlySpecialised().size() +
				"," + changeSet.getAllIndirectlySpecialised().size() +
				"," + changeSet.getAllMixedSpecialised().size() +
				"," + changeSet.getAllPurelyDirectlySpecialised().size() +
				"," + changeSet.getAllPurelyIndirectlySpecialised().size() +
				"," + changeSet.getAllDirectlyGeneralised().size() +
				"," + changeSet.getAllIndirectlyGeneralised().size() +
				"," + changeSet.getAllMixedGeneralised().size() + 
				"," + changeSet.getAllPurelyDirectlyGeneralised().size() +
				"," + changeSet.getAllPurelyIndirectlyGeneralised().size() +
				"," + changeSet.getPartitioningTime();
		
		return header + "\n" + row;
	}
}
