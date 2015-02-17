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
package uk.ac.manchester.cs.diff.concept.witnesses;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * @author Rafael S. Goncalves <br>
 * Information Management Group (IMG) <br>
 * School of Computer Science <br>
 * University of Manchester <br>
 */
public class WitnessAxioms {
	private Set<OWLAxiom> direct, indirect;
	
	/**
	 * Constructor
	 * @param direct	Set of direct witness axioms
	 * @param indirect	Set of indirect witness axioms
	 */
	public WitnessAxioms(Set<OWLAxiom> direct, Set<OWLAxiom> indirect) {
		this.direct = direct;
		this.indirect = indirect;
	}
	
	
	/**
	 * Get the set of direct witness axioms
	 * @return Set of direct witness axioms
	 */
	public Set<OWLAxiom> getDirectWitnesses() {
		return direct;
	}
	
	
	/**
	 * Get the set of indirect witness axioms
	 * @return Set of indirect witness axioms
	 */
	public Set<OWLAxiom> getIndirectWitnesses() {
		return indirect;
	}
	
	
	/**
	 * Check if there are any witness axioms (direct or otherwise)
	 * @return true if this contains any witness axioms
	 */
	public boolean isEmpty() {
		if((direct == null || direct.isEmpty()) && (indirect == null || indirect.isEmpty()))
			return true;
		else
			return false;
	}
}