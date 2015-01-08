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
package uk.ac.manchester.cs.diff.axiom.changeset;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;

import uk.ac.manchester.cs.diff.axiom.change.LogicalAddition;
import uk.ac.manchester.cs.diff.axiom.change.LogicalRemoval;

/**
 * @author Rafael S. Goncalves <br>
 * Information Management Group (IMG) <br>
 * School of Computer Science <br>
 * University of Manchester <br>
 */
public class LogicalChangeSet implements AxiomChangeSet {
	private Set<OWLAxiom> effectualAdditions, ineffectualAdditions, effectualRemovals, ineffectualRemovals;
	private double diffTime;
	
	/**
	 * Constructor
	 * @param effectualAdditions	Effectual additions
	 * @param ineffectualAdditions	Ineffectual additions
	 * @param effectualRemovals	Effectual removals
	 * @param ineffectualRemovals	Ineffectual removals
	 */
	public LogicalChangeSet(Set<OWLAxiom> effectualAdditions, Set<OWLAxiom> ineffectualAdditions, 
			Set<OWLAxiom> effectualRemovals, Set<OWLAxiom> ineffectualRemovals) {
		this.effectualAdditions = effectualAdditions;
		this.ineffectualAdditions = ineffectualAdditions;
		this.effectualRemovals = effectualRemovals;
		this.ineffectualRemovals = ineffectualRemovals;
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
	 * Get the set of effectual and ineffectual additions
	 * @return Set of effectual and ineffectual additions
	 */
	public Set<LogicalAddition> getAdditions() {
		Set<LogicalAddition> additionSet = new HashSet<LogicalAddition>();
		for(OWLAxiom ax : effectualAdditions)
			additionSet.add(new LogicalAddition(ax, true));
		for(OWLAxiom ax : ineffectualAdditions)
			additionSet.add(new LogicalAddition(ax, false));
		return additionSet;
	}

	
	/**
	 * Get the set of effectual and ineffectual removals
	 * @return Set of effectual and ineffectual removals
	 */
	public Set<LogicalRemoval> getRemovals() {
		Set<LogicalRemoval> removalSet = new HashSet<LogicalRemoval>();
		for(OWLAxiom ax : effectualRemovals)
			removalSet.add(new LogicalRemoval(ax, true));
		for(OWLAxiom ax : ineffectualRemovals)
			removalSet.add(new LogicalRemoval(ax, false));
		return removalSet;
	}

	
	/**
	 * Get the set of effectual addition axioms
	 * @return Set of effectual addition axioms
	 */
	public Set<OWLAxiom> getEffectualAdditionAxioms() {
		return effectualAdditions;
	}
	
	
	/**
	 * Get the set of effectual removal axioms
	 * @return Set of effectual removal axioms
	 */
	public Set<OWLAxiom> getEffectualRemovalAxioms() {
		return effectualRemovals;
	}
	
	
	/**
	 * Get the set of ineffectual addition axioms
	 * @return Set of ineffectual addition axioms
	 */
	public Set<OWLAxiom> getIneffectualAdditionAxioms() {
		return ineffectualAdditions;
	}
	
	
	/**
	 * Get the set of ineffectual removal axioms
	 * @return Set of ineffectual removal axioms
	 */
	public Set<OWLAxiom> getIneffectualRemovalAxioms() {
		return ineffectualRemovals;
	}
	
	
	/**
	 * Check if change set contains no changes
	 * @return true if change set contains no changes, false otherwise
	 */
	public boolean isEmpty() {
		if(effectualAdditions.isEmpty() && effectualRemovals.isEmpty() 
				&& ineffectualAdditions.isEmpty() && ineffectualAdditions.isEmpty())
			return true;
		else
			return false;
	}
}
