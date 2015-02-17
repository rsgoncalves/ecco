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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.diff.concept.sigma.Signature;

/**
 * @author Rafael S. Goncalves <br>
 * Information Management Group (IMG) <br>
 * School of Computer Science <br>
 * University of Manchester <br>
 */
public class SignatureSampler {
	private OWLOntology ont1, ont2;

	/**
	 * Single ontology constructor
	 * @param ont1	Ontology 1
	 */
	public SignatureSampler(OWLOntology ont1) {
		this.ont1 = ont1;
	}
	
	
	/**
	 * Constructor for two ontologies
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 */
	public SignatureSampler(OWLOntology ont1, OWLOntology ont2) {
		this.ont1 = ont1;
		this.ont2 = ont2;
	}
	
	
	/**
	 * Get concept sample of the specified size 
	 * @param sampleSize	Sample size
	 * @return Set of concept names in the sample
	 */
	public Set<OWLClass> getSample(int sampleSize) {
		System.out.print("Fetching signature sample... ");
		Set<OWLClass> sample = new HashSet<OWLClass>();
		Object[] arr = null;
		if(ont2 != null)
			arr = new Signature().getSharedConceptNames(ont1, ont2).toArray();
		else
			arr = ont1.getClassesInSignature().toArray();
		
		if(sampleSize > arr.length) sampleSize = arr.length;
		
		Random rand = new Random();
		while(sample.size() < sampleSize) {
			int random = rand.nextInt(arr.length);
			OWLClass c = (OWLClass)arr[random];
			sample.add(c);
		}

		System.out.println("done (sample size: " + sample.size() + " named concepts)");
		return sample;
	}
	
	
	/**
	 * main
	 * @param args	Arguments
	 */
	public static void main(String[] args) {
		int sampleSize = Integer.parseInt(args[0]);
		for(int i = 1; i < args.length; i++) {
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			File f = new File(args[i]);
			String path = f.getParentFile().getAbsolutePath();
			if(!path.endsWith(File.separator)) path += File.separator;
			
			OWLOntology ont = null;
			try {
				ont = man.loadOntologyFromOntologyDocument(f);
			} catch (OWLOntologyCreationException e) {
				e.printStackTrace();
			}
			System.out.println("Loaded Ontology");

			SignatureSampler sampler = new SignatureSampler(ont);
			Set<OWLClass> sampleSet = sampler.getSample(sampleSize);

			String sigList = "";
			for(OWLClass c : sampleSet) sigList += c.getIRI() + "\n";

			try {
				File file = new File(path + File.separator + "randomSample.txt");
				Writer output = new BufferedWriter(new FileWriter(file));
				System.out.println("Saved random sample at: " + file.getAbsolutePath());
				output.write(sigList);
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			man.removeOntology(ont);
		}
	}
}