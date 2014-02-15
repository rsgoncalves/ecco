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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class WitnessPack {
	private Map<OWLClass, Set<OWLAxiom>> direct, indirect;

	/**
	 * Constructor
	 * @param direct	Map of concepts to their direct change witnesses
	 * @param indirect	Map of concepts to their indirect change witnesses
	 */
	public WitnessPack(Map<OWLClass, Set<OWLAxiom>> direct, Map<OWLClass, Set<OWLAxiom>> indirect) {
		this.direct = direct;
		this.indirect = indirect;
	}

	
	/**
	 * Get the map of concepts to their direct change witnesses
	 * @return Map of concepts to their direct change witnesses
	 */
	public Map<OWLClass, Set<OWLAxiom>> getDirectWitnesses() {
		return direct;
	}

	
	/**
	 * Get the map of concepts to their indirect change witnesses
	 * @return Map of concepts to their indirect change witnesses
	 */
	public Map<OWLClass, Set<OWLAxiom>> getIndirectWitnesses() {
		return indirect;
	}

	
	/**
	 * Get the set of direct witness axioms to the change of a given concept
	 * @param c	Concept
	 * @return Set of direct witness axioms
	 */
	public Set<OWLAxiom> getDirectWitnesses(OWLClass c) {
		Set<OWLAxiom> out = new HashSet<OWLAxiom>();
		if(direct.get(c) != null)
			out.addAll(direct.get(c));
		return out;
	}

	
	/**
	 * Get the set of indirect witness axioms to the change of a given concept
	 * @param c	Concept
	 * @return Set of indirect witness axioms
	 */
	public Set<OWLAxiom> getIndirectWitnesses(OWLClass c) {
		Set<OWLAxiom> out = new HashSet<OWLAxiom>();
		if(indirect.get(c) != null)
			out.addAll(indirect.get(c));
		return out;
	}
}
