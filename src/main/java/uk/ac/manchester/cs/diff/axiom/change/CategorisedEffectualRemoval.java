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
package uk.ac.manchester.cs.diff.axiom.change;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * @author Rafael S. Goncalves <br>
 * Information Management Group (IMG) <br>
 * School of Computer Science <br>
 * University of Manchester <br>
 */
public class CategorisedEffectualRemoval extends CategorisedEffectualChange {
	private EffectualRemovalCategory cat;
	
	/**
	 * Constructor
	 * @param ax	OWL Axiom
	 * @param cat	Change category
	 * @param axAlignment	Set of alignments
	 * @param retiredTerms	Set of retired terms used in the axiom
	 */
	public CategorisedEffectualRemoval(OWLAxiom ax, EffectualRemovalCategory cat, Set<OWLAxiom> axAlignment, Set<OWLEntity> retiredTerms) {
		super(ax, axAlignment, retiredTerms);
		this.cat = cat;
	}
	
	
	/**
	 * Get effectual removal category name
	 * @return Change category name
	 */
	public String getCategoryName() {
		return cat.toString();
	}
	
	
	/**
	 * Get effectual removal category
	 * @return Change category
	 */
	public EffectualRemovalCategory getCategory() {
		return cat;
	}
	
	
	/**
	 * Categories of effectual removals
	 */
	public enum EffectualRemovalCategory {
		WEAKENING ("Weakening"),
		WEAKENINGRT ("WeakeningWithRetiredTerms"),
		PUREREMOVAL ("PureRemoval"),
		PUREREMOVALRT ("PureRemovalWithRetiredTerms"),
		RETIREDDESCRIPTION ("RetiredDescription"),
		MODIFIEDDEFINITION ("RetiredModifiedDefinition"),
		MODIFIEDDEFINITIONRT ("RetiredModifiedDefinitionWithRetiredTerms");

		String name;
		EffectualRemovalCategory(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}
	}
}
