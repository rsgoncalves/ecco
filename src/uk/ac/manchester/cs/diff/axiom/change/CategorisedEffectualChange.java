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

import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class CategorisedEffectualChange extends CategorisedChange {
	private Set<OWLAxiom> axAlignment;
	private Set<OWLEntity> differentTerms;
	
	/**
	 * Constructor
	 * @param axiom	OWL axiom
	 * @param axAlignment	Set of axioms with which this change aligns to
	 * @param differentTerms	Set of new or retired terms used in the axiom
	 */
	public CategorisedEffectualChange(OWLAxiom axiom, Set<OWLAxiom> axAlignment, Set<OWLEntity> differentTerms) {
		super(axiom, true);
		this.axAlignment = axAlignment;
		this.differentTerms = differentTerms;
	}

	
	/**
	 * Get the set of axiomatic alignments for this change
	 * @return Set of axiomatic alignments for this change
	 */
	public Set<OWLAxiom> getAxiomAlignment() {
		return axAlignment;
	}
	
	
	/**
	 * Get the new terms used in this change
	 * @return The set of new terms used in this change
	 */
	public Set<OWLEntity> getDifferentTerms() {
		return differentTerms;
	}
}
