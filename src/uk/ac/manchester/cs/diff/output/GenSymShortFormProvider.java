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
package uk.ac.manchester.cs.diff.output;

import java.util.HashMap;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

/**
 * @author Rafael S. Goncalves <br>
 * Information Management Group (IMG) <br>
 * School of Computer Science <br>
 * University of Manchester <br>
 */
public class GenSymShortFormProvider implements ShortFormProvider {
	private HashMap<OWLEntity,String> map;
	
	public GenSymShortFormProvider(HashMap<OWLEntity,String> map) {
		this.map = map;
	}
	
	@Override
	public void dispose() {
		// Do nothing
	}

	@Override
	public String getShortForm(OWLEntity arg0) {
		String output = "";
		SimpleShortFormProvider fp = new SimpleShortFormProvider();
		if(map.get(arg0) != null)
			output = map.get(arg0);
		else
			output = fp.getShortForm(arg0);
		return output;
	}
}
