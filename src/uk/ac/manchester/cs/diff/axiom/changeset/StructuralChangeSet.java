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
package uk.ac.manchester.cs.diff.axiom.changeset;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;

import uk.ac.manchester.cs.diff.axiom.change.StructuralAddition;
import uk.ac.manchester.cs.diff.axiom.change.StructuralRemoval;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class StructuralChangeSet implements AxiomChangeSet {
	private Set<OWLAxiom> additions, removals, shared;
	private String ont1name, ont2name;
	private double diffTime;
	
	/**
	 * Constructor
	 * @param additions	Set of added axioms
	 * @param removals	Set of removed axioms
	 * @param shared	Set of shared axioms
	 */
	public StructuralChangeSet(Set<OWLAxiom> additions, Set<OWLAxiom> removals, Set<OWLAxiom> shared) {
		this.additions = additions;
		this.removals = removals;
		this.shared = shared;
	}
	
	
	/**
	 * Set file name of specified ontology (1 or 2)
	 * @param name	Ontology file name
	 * @deprecated
	 */
	public void setOntologyName(int ontNr, String name) {
		if(ontNr == 1) ont1name = name;
		else if(ontNr == 2) ont2name = name;
	}
	
	
	/**
	 * Set diff time 
	 * @param time	Diff time
	 * @deprecated
	 */
	public void setDiffTime(double time) {
		diffTime = time;
	}
	
	
	/**
	 * Get the CPU time (in seconds) spent in structural diff
	 * @return CPU time (in seconds) spent in structural diff
	 */
	public double getDiffTime() {
		return diffTime;
	}
	
	
	/**
	 * Get file name of ontology 1
	 * @return File name of ontology 1
	 */
	public String getOntology1FileName() {
		return ont1name;
	}
	
	
	/**
	 * Get file name of ontology 2
	 * @return File name of ontology 2
	 */
	public String getOntology2FileName() {
		return ont2name;
	}
	
	
	/**
	 * Get the set of structural additions
	 * @return Set of structural additions
	 */
	public Set<StructuralAddition> getAdditions() {
		Set<StructuralAddition> additionSet = new HashSet<StructuralAddition>();
		for(OWLAxiom ax : additions)
			additionSet.add(new StructuralAddition(ax));		
		return additionSet;
	}
	
	
	/**
	 * Get the set of structural removals
	 * @return Set of structural removals
	 */
	public Set<StructuralRemoval> getRemovals() {
		Set<StructuralRemoval> removalSet = new HashSet<StructuralRemoval>();
		for(OWLAxiom ax : removals)
			removalSet.add(new StructuralRemoval(ax));
		return removalSet;
	}
	
	
	/**
	 * Get the set of added axioms
	 * @return Set of added axioms
	 */
	public Set<OWLAxiom> getAddedAxioms() {
		return additions;
	}
	
	
	/**
	 * Get the set of removed axioms
	 * @return Set of removed axioms
	 */
	public Set<OWLAxiom> getRemovedAxioms() {
		return removals;
	}
	
	
	/**
	 * Get the set of shared axioms
	 * @return Set of shared axioms
	 */
	public Set<OWLAxiom> getShared() {
		return shared;
	}
	
	
	/**
	 * Check if change set is empty (i.e., contains no additions or removals)
	 */
	public boolean isEmpty() {
		if(removals.isEmpty() && additions.isEmpty())
			return true;
		else
			return false;
	}
}
