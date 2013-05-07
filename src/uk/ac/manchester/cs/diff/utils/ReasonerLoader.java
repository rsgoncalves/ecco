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
package uk.ac.manchester.cs.diff.utils;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.FreshEntityPolicy;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class ReasonerLoader {
	private OWLOntology ont;
	private boolean verbose = false;
	
	/**
	 * Constructor
	 * @param ont OWL Ontology
	 */
	public ReasonerLoader(OWLOntology ont) {
		this.ont = ont;
	}
	
	
	/**
	 * Constructor
	 * @param ont OWL Ontology
	 * @param verbose
	 */
	public ReasonerLoader(OWLOntology ont, boolean verbose) {
		this.ont = ont;
		this.verbose = verbose;
	}
	
	
	/**
	 * Create a Hermit reasoner instance
	 * @return JFact reasoner instance
	 */
	public OWLReasoner createReasoner() {
		Configuration config = new Configuration();
		config.freshEntityPolicy = FreshEntityPolicy.ALLOW;
		
		if(verbose) System.out.print("   Creating reasoner... ");
		long start = System.currentTimeMillis();
		
		OWLReasoner reasoner = new Reasoner(config, ont);
		
		long end = System.currentTimeMillis();
		if(verbose) System.out.println("done (" + (end-start)/1000.0 + " secs)");
		return reasoner;
	}
	
	
	/**
	 * Get ontology used to instatiate the reasoner
	 * @return OWL ontology
	 */
	public OWLOntology getOntology() {
		return ont;
	}
}
