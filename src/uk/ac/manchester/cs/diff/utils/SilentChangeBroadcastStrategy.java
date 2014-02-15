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
package uk.ac.manchester.cs.diff.utils;

import java.util.List;

import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeBroadcastStrategy;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class SilentChangeBroadcastStrategy implements OWLOntologyChangeBroadcastStrategy {
	private static final long serialVersionUID = 8035434562859540956L;

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLOntologyChangeBroadcastStrategy#broadcastChanges(org.semanticweb.owlapi.model.OWLOntologyChangeListener, java.util.List)
	 */
	@Override
	public void broadcastChanges(OWLOntologyChangeListener arg0, List<? extends OWLOntologyChange> arg1) throws OWLException {
		// shush just do nothing
	}
}
