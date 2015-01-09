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

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.RecursiveTask;

import org.semanticweb.owlapi.io.IRIDocumentSource;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;

/**
 * @author Rafael S. Goncalves <br>
 * Information Management Group (IMG) <br>
 * School of Computer Science <br>
 * University of Manchester <br>
 */
public class OntologyLoader extends RecursiveTask<OWLOntology> {
	private static final long serialVersionUID = -5909585243181647744L;
	private OWLOntologyManager man;
	private OWLOntologyLoaderConfiguration config;
	private boolean processImports, ignoreAbox, localFile;
	private int ontNr;
	private String filepath;
	
	
	public OntologyLoader(OWLOntologyManager man, OWLOntologyLoaderConfiguration config, 
			int ontNr, String filepath, boolean processImports, boolean ignoreAbox, boolean localFile) {
		this.man = man;
		this.config = config;
		this.ontNr = ontNr;
		this.filepath = filepath;
		this.processImports = processImports;
		this.ignoreAbox = ignoreAbox;
		this.localFile = localFile;
	}
	
	@Override
	protected OWLOntology compute() {
		String sep = null;
		if(localFile) sep = System.getProperty("file.separator");
		else sep = "/";
		
		String filename = filepath.substring(filepath.lastIndexOf(sep)+1, filepath.length());
		System.out.println("Input " + ontNr + ": " + filename + " (" + filepath + ")");
		
		// Load ontology
		OWLOntology ont = null;
		try {
			if(localFile) ont = man.loadOntologyFromOntologyDocument(new IRIDocumentSource(IRI.create("file:" + new File(filepath))), config);
			else ont = man.loadOntologyFromOntologyDocument(new IRIDocumentSource(IRI.create(filepath)));
		} catch (OWLOntologyCreationException e) {
			System.out.println("[Invalid IRI]\tUnable to load ontology " + ontNr + ".\n\tInput: " + filepath);
		}
		
		Set<OWLAxiom> result = null;
		if(ont != null) {
			if(ignoreAbox) removeAbox(ont);
			result = ont.getAxioms();
			System.out.println("\tLoaded ontology " + ontNr + " (" + ont.getLogicalAxiomCount() + " logical axioms)");
			
			// Process imports
			if(processImports) {
				if(!ont.getImportsDeclarations().isEmpty()) {
					for(OWLImportsDeclaration d : ont.getImportsDeclarations()) {
						try {
							OWLOntology imported = man.loadOntology(d.getIRI());
							result.addAll(imported.getLogicalAxioms());
							man.removeOntology(imported);
						} catch (OWLOntologyCreationException e) {
							System.out.println("[Invalid IRI]\tUnable to load the imported ontology: " + d.getIRI());
						}
					}
				}
			}
			normalize(result);
			man.removeOntology(ont);
		}
		OWLOntology out = null;
		if(result != null) {
			try {
				out = man.createOntology(result);
			} catch (OWLOntologyCreationException e) {
				e.printStackTrace();
			}
		} 
		return out;
	}
	
	
	/**
	 * Given a set of axioms, remove unary disjointness axioms
	 * @param set	Set of axioms to analyse
	 * @return Set of axioms without unary disjointness axioms
	 */
	private Set<OWLAxiom> normalize(Set<OWLAxiom> set) {
		Set<OWLAxiom> toRemove = new HashSet<OWLAxiom>();
		for(OWLAxiom ax : set) {
			if(ax.isOfType(AxiomType.DISJOINT_CLASSES)) {
				OWLDisjointClassesAxiom dis = (OWLDisjointClassesAxiom)ax;
				if(dis.getClassesInSignature().size() < 2)
					toRemove.add(ax);
			}
		}
		set.removeAll(toRemove);
		return set;
	}
	

	/**
	 * Remove Abox axioms from given ontology
	 * @param ont	Ontology to remove axioms from
	 */
	private void removeAbox(OWLOntology ont) {
		Set<OWLAxiom> aboxAxs = ont.getABoxAxioms(Imports.INCLUDED);
		ont.getOWLOntologyManager().removeAxioms(ont, aboxAxs);
	}
}
