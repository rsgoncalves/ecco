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
package uk.ac.manchester.cs.diff.concept.change;

import org.semanticweb.owlapi.model.OWLClass;

import uk.ac.manchester.cs.diff.concept.changeset.WitnessAxioms;

/**
 * @author Rafael S. Goncalves <br>
 * Information Management Group (IMG) <br>
 * School of Computer Science <br>
 * University of Manchester <br>
 */
public class LHSConceptChange extends ConceptChange {

	/**
	 * Constructor
	 * @param c	Concept name
	 * @param specWits	Witnesses for a specialisation
	 * @param genWits	Witnesses for a generalisation
	 */
	public LHSConceptChange(OWLClass c, WitnessAxioms specWits, WitnessAxioms genWits) {
		super(c, specWits, genWits);
	}
}
