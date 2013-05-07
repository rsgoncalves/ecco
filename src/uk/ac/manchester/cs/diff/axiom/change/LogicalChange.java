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

import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class LogicalChange extends StructuralChange {
	private boolean isEffectual;
	
	/**
	 * Constructor
	 * @param axiom	Changed axiom
	 * @param isEffectual	true if change is effectual, false otherwise
	 */
	public LogicalChange(OWLAxiom axiom, boolean isEffectual) {
		super(axiom);
		this.isEffectual = isEffectual;
	}
	
	
	/**
	 * Check if change is logically effectual
	 * @return true if change is logically effectual, false otherwise
	 */
	public boolean isEffectual() {
		return isEffectual;
	}


	/**
	 * Check if change is logically ineffectual
	 * @return true if change is logically ineffectual, false otherwise
	 */
	public boolean isIneffectual() {
		return !isEffectual;
	}
}
