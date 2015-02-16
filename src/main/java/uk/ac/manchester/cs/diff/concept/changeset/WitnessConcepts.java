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
package uk.ac.manchester.cs.diff.concept.changeset;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * @author Rafael S. Goncalves <br>
 * Information Management Group (IMG) <br>
 * School of Computer Science <br>
 * University of Manchester <br>
 */
public class WitnessConcepts {
	private Set<OWLClassExpression> lhsWit, rhsWit;
	
	/**
	 * Constructor
	 * @param lhsWit	Ontology 1 (LHS) witnesses
	 * @param rhsWit	Ontology 2 (RHS) witnesses
	 */
	public WitnessConcepts(Set<OWLClassExpression> lhsWit, Set<OWLClassExpression> rhsWit) {
		this.lhsWit = lhsWit;
		this.rhsWit = rhsWit;
	}
	
	
	/**
	 * Get ontology 1 (LHS) witnesses
	 * @return Ontology 1 witnesses
	 */
	public Set<OWLClassExpression> getLHSWitnesses() {
		return lhsWit;
	}
	
	
	/**
	 * Get ontology 2 (RHS) witnesses
	 * @return Ontology 2 witnesses
	 */
	public Set<OWLClassExpression> getRHSWitnesses() {
		return rhsWit;
	}
	
	
	/**
	 * Check if there are any concept witnesses, either LHS or RHS
	 * @return true if there any witnesses, false otherwise
	 */
	public boolean isEmpty() {
		if(lhsWit.isEmpty() && rhsWit.isEmpty())
			return true;
		else
			return false;
	}
}
