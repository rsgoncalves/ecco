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
package uk.ac.manchester.cs.diff.concept;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class Signature {
	
	/**
	 * Constructor
	 */
	public Signature() {}


	/**
	 * Get signature from specified file
	 * @param f	Signature (text) file
	 * @return Set of concepts in the file 
	 */
	public Set<OWLClass> getSignatureFromFile(File f) {
		OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		Set<OWLClass> sig = new HashSet<OWLClass>();
		try {
			FileInputStream fstream = new FileInputStream(f);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String strLine;
			while ((strLine = br.readLine()) != null) {
				OWLClass c = df.getOWLClass(IRI.create(strLine));
				sig.add(c);
			}
			
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sig;
	}


	/**
	 * Get union of both ontologies concept names
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 * @return Set of concept names in the signature-union
	 */
	public Set<OWLClass> getUnionConceptNames(OWLOntology ont1, OWLOntology ont2) {
		Set<OWLClass> sig = new HashSet<OWLClass>();
		Set<OWLClass> ont1sig = ont1.getClassesInSignature();
		Set<OWLClass> ont2sig = ont2.getClassesInSignature();
		sig.addAll(ont1sig); sig.addAll(ont2sig);
		return sig;
	}


	/**
	 * Get shared concept names between given ontologies
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 * @return Set of concept names in the signature-intersection
	 */
	public Set<OWLClass> getSharedConceptNames(OWLOntology ont1, OWLOntology ont2) {
		Set<OWLClass> sig = new HashSet<OWLClass>();
		Set<OWLClass> ont1sig = ont1.getClassesInSignature();
		Set<OWLClass> ont2sig = ont2.getClassesInSignature();
		for(OWLClass c : ont1sig) {
			if(ont2sig.contains(c))
				sig.add(c);
		}
		return sig;
	}
	
	
	/**
	 * Get shared roles between given ontologies
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 * @return Set of roles in the signature-intersection
	 */
	public Set<OWLObjectProperty> getSharedRoles(OWLOntology ont1, OWLOntology ont2) {
		Set<OWLObjectProperty> sig = new HashSet<OWLObjectProperty>();
		Set<OWLObjectProperty> ont1sig = ont1.getObjectPropertiesInSignature();
		Set<OWLObjectProperty> ont2sig = ont2.getObjectPropertiesInSignature();
		for(OWLObjectProperty c : ont1sig) {
			if(ont2sig.contains(c))
				sig.add(c);
		}
		return sig;
	}
	
	
	/**
	 * Get all object properties in signature union
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 * @return Set of roles in the signature-union
	 */
	public Set<OWLObjectProperty> getRolesInWholeSignature(OWLOntology ont1, OWLOntology ont2) {
		Set<OWLObjectProperty> set = new HashSet<OWLObjectProperty>();
		set.addAll(ont1.getObjectPropertiesInSignature());
		set.addAll(ont2.getObjectPropertiesInSignature());
		return set;
	}
}
