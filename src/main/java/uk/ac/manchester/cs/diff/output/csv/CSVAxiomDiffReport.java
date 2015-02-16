/*******************************************************************************
 * This file is part of ecco.
 * 
 * ecco is distributed under the terms of the GNU Lesser General Public License (LGPL), Version 3.0.
 *  
 * Copyright 2011-2014, The University of Manchester
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

import uk.ac.manchester.cs.diff.axiom.changeset.AxiomChangeSet;
import uk.ac.manchester.cs.diff.axiom.changeset.CategorisedChangeSet;
import uk.ac.manchester.cs.diff.axiom.changeset.LogicalChangeSet;
import uk.ac.manchester.cs.diff.axiom.changeset.StructuralChangeSet;

/**
 * @author Rafael S. Goncalves <br>
 * Information Management Group (IMG) <br>
 * School of Computer Science <br>
 * University of Manchester <br>
 */
public class CSVAxiomDiffReport {
	private String header, row;
	
	/**
	 * Constructor
	 */
	public CSVAxiomDiffReport() {
		header = "\n"; row = "";
	}
	
	
	/**
	 * Get a CSV-formatted change report
	 * @param changeSet	Change set
	 * @return Change report as a CSV-formatted string
	 */
	public String getReport(AxiomChangeSet changeSet) {
		String report = null;	
		if(changeSet instanceof StructuralChangeSet)
			report = getStructuralChangeSetReport((StructuralChangeSet)changeSet);
		else if(changeSet instanceof LogicalChangeSet)
			report = getLogicalChangeSetReport((LogicalChangeSet)changeSet);
		else if(changeSet instanceof CategorisedChangeSet)
			report = getCategorisedChangeSetReport((CategorisedChangeSet)changeSet);
		return report;
	}
	
	
	/**
	 * Get a CSV report of a structural change set
	 * @param stChangeSet	Structural change set
	 * @return CSV structural change set report
	 */
	private String getStructuralChangeSetReport(StructuralChangeSet stChangeSet) {
		header += "Ontology 1,Ontology 2,Structurally Equivalent,Structural Additions,Structural Removals,Shared Axioms,Structural Diff Time";
		row += stChangeSet.getOntology1FileName() + ",";
		row += stChangeSet.getOntology2FileName() + ",";
		row += (stChangeSet.isEmpty() ? "true" : "false") + ",";
		row += stChangeSet.getAddedAxioms().size() + ",";
		row += stChangeSet.getRemovedAxioms().size() + ",";
		row += stChangeSet.getShared().size() + ",";
		row += stChangeSet.getDiffTime();	
		return header + "\n" + row;
	}
	
	
	/**
	 * Get a CSV report of a logical change set
	 * @param logChangeSet	Logical change set
	 * @return CSV logical change set report
	 */
	private String getLogicalChangeSetReport(LogicalChangeSet logChangeSet) {
		header += ",Effectual Additions,Ineffectual Additions,Effectual Removals,Ineffectual Removals,Logical Diff Time";
		row += "," + logChangeSet.getEffectualAdditionAxioms().size();
		row += "," + logChangeSet.getIneffectualAdditionAxioms().size();
		row += "," + logChangeSet.getEffectualRemovalAxioms().size();
		row += "," + logChangeSet.getIneffectualRemovalAxioms().size();
		row += "," + logChangeSet.getDiffTime();
		return header + "\n" + row;
	}
	
	
	/**
	 * Get a CSV report of a categorised change set
	 * @param catChangeSet	Categorised change set
	 * @return CSV categorised change set report
	 */
	private String getCategorisedChangeSetReport(CategorisedChangeSet catChangeSet) {
		header += ",Strengthening,Strengthening NT,Extended Definition,Extended Definition NT,Pure Addition,Pure Addition NT,New Description,EAC Time," +
				"Added Rewrite,Added Standing Redundancy,Added Prospective Redundancy,Added Reshuffle,New,IAC Time,Just Find Time," +
				"Lac Just Time,Weakening,Weakening RT,Reduced Definition,Reduced Definition RT,Pure Removal,Pure Removal RT,Retired Description,ERC Time," +
				"Removed Rewrite,Removed Standing Redundancy,Removed Prospective Redundancy,Removed Reshuffle,New,IRC Time,Just Find Time," +
				"Lac Just Time,Total Time";
		// Effectual additions
		row += "," + catChangeSet.getStrengthenings().size();
		row += "," + catChangeSet.getStrengtheningsWithNewTerms().size();
		row += "," + catChangeSet.getAddedModifiedDefinitions().size();
		row += "," + catChangeSet.getAddedModifiedDefinitionsWithNewTerms().size();
		row += "," + catChangeSet.getPureAdditions().size();
		row += "," + catChangeSet.getPureAdditionsWithNewTerms().size();
		row += "," + catChangeSet.getNewDescriptions().size();
		row += "," + catChangeSet.getEffectualAdditionCategorisationTime();
		// Ineffectual additions
		row += "," + catChangeSet.getAddedRewrites().size();
		row += "," + catChangeSet.getAddedRedundancies().size();
		row += "," + catChangeSet.getAddedProspectiveRedundancies().size();
		row += "," + catChangeSet.getAddedReshuffleRedundancies().size();
		row += "," + catChangeSet.getAddedProspectiveNewRedundancies().size();
//		row += "," + catChangeSet.getAddedNovelRedundancies().size();
//		row += "," + catChangeSet.getAddedPseudoNovelRedundancies().size();
		row += "," + catChangeSet.getIneffectualAdditionCategorisationTime();
		row += "," + catChangeSet.getIneffectualAdditionJustificationFindingTime();
		row += "," + catChangeSet.getIneffectualAdditionLaconicJustificationFindingTime();
		// Effectual removals
		row += "," + catChangeSet.getWeakenings().size();
		row += "," + catChangeSet.getWeakeningsWithRetiredTerms().size();
		row += "," + catChangeSet.getRemovedModifiedDefinitions().size();
		row += "," + catChangeSet.getRemovedModifiedDefinitionsWithRetiredTerms().size();
		row += "," + catChangeSet.getPureRemovals().size();
		row += "," + catChangeSet.getPureRemovalsWithRetiredTerms().size();
		row += "," + catChangeSet.getRetiredDescriptions().size();
		row += "," + catChangeSet.getEffectualRemovalCategorisationTime();
		// Ineffectual removals
		row += "," + catChangeSet.getRemovedRewrites().size();
		row += "," + catChangeSet.getRemovedRedundancies().size();
		row += "," + catChangeSet.getRemovedProspectiveRedundancies().size();
		row += "," + catChangeSet.getRemovedReshuffleRedundancies().size();
		row += "," + catChangeSet.getRemovedProspectiveNewRedundancies().size();
//		row += "," + catChangeSet.getRemovedNovelRedundancies().size();
//		row += "," + catChangeSet.getRemovedPseudoNovelRedundancies().size();
		row += "," + catChangeSet.getIneffectualRemovalCategorisationTime();
		row += "," + catChangeSet.getIneffectualRemovalJustificationFindingTime();
		row += "," + catChangeSet.getIneffectualRemovalLaconicJustificationFindingTime();
		
		row += "," + catChangeSet.getDiffTime();
		return header + "\n" + row;
	}
}
