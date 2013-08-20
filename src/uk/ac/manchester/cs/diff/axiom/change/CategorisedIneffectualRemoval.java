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
package uk.ac.manchester.cs.diff.axiom.change;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class CategorisedIneffectualRemoval extends CategorisedChange {
	private Map<Explanation<OWLAxiom>,Set<IneffectualRemovalCategory>> cats;
	
	/**
	 * Constructor
	 * @param ax	OWL Axiom
	 * @param cats	Map of justifications to change category
	 */
	public CategorisedIneffectualRemoval(OWLAxiom ax, Map<Explanation<OWLAxiom>,Set<IneffectualRemovalCategory>> cats) {
		super(ax, false);
		this.cats = cats;
	}
	
	
	/**
	 * Get the map of justifications to change category
	 * @return Map of justifications to categories
	 */
	public Map<Explanation<OWLAxiom>,Set<IneffectualRemovalCategory>> getJustificationMap() {
		return cats;
	}
	
	
	/**
	 * Get ineffectual removal categories
	 * @return Change categories
	 */
	public Set<IneffectualRemovalCategory> getCategories() {
		Set<IneffectualRemovalCategory> result = new HashSet<IneffectualRemovalCategory>();
		for(Explanation<OWLAxiom> set : cats.keySet()) {
			result.addAll(cats.get(set));
		}
		return result;
	}
	
	
	/**
	 * Categories of ineffectual removal
	 */
	public enum IneffectualRemovalCategory {
		REWRITE ("RemovedRewrite"),
		PREWRITE ("RemovedPartialRewrite"),
		REDUNDANCY ("RemovedRedundancy"),
		NEWPROSPREDUNDANCY ("RemovedNewProspectiveRedundancy"),
		RESHUFFLEREDUNDANCY ("RemovedReshuffleProspectiveRedundancy");
		
		String name;
		IneffectualRemovalCategory(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}
	}
}
