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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.diff.concept.GrammarDiff;
import uk.ac.manchester.cs.diff.concept.changeset.ConceptChangeSet;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class ConceptDiffRunner {

	/**
	 * Serialize signature sample as a text file
	 * @param sampleSet	Set of entities in the sample
	 * @param outputDir	Output directory
	 */
	public static void serializeSample(Set<OWLEntity> sampleSet, String outputDir) {
		String sigList = "";
		for(OWLEntity c : sampleSet) sigList += c.getIRI() + "\n";
		try {
			new File(outputDir).mkdirs();
			File file = new File(outputDir + "randomSample.txt");
			Writer output = new BufferedWriter(new FileWriter(file, false));
			System.out.println("Saved random sample at: " + file.getAbsolutePath());
			output.write(sigList);
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Serialize string to file
	 * @param s	String to serialize
	 * @param outputDir	Output directory
	 * @param filename	File name
	 */
	public static void serializeString(String s, String outputDir, String filename) {
		try {
			FileWriter fw = new FileWriter(new File(outputDir + filename), true);
			fw.append(s);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Tester class
	 * @param 0: ontology1
	 * @param 1: ontology2
	 * @param 2: output directory
	 * @param 3: signature file
	 * @throws OWLOntologyCreationException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws OWLOntologyCreationException, InterruptedException {
		File f1 = new File(args[0]), f2 = new File(args[1]);
		OWLOntologyManager man1 = OWLManager.createOWLOntologyManager(), man2 = OWLManager.createOWLOntologyManager();
		
		OWLOntology ont1 = man1.loadOntologyFromOntologyDocument(f1);
		System.out.println("Loaded ontology 1: " + f1.getAbsolutePath());
		
		OWLOntology ont2 = man2.loadOntologyFromOntologyDocument(f2);
		System.out.println("Loaded ontology 2: " + f2.getAbsolutePath());
		
		String outputDir = args[2];
		if(!outputDir.endsWith(File.separator)) outputDir += File.separator;
		
		// Remove abox for NCIt
		man1.removeAxioms(ont1, ont1.getABoxAxioms(true)); 
		man2.removeAxioms(ont2, ont2.getABoxAxioms(true));
		
		// get diff
		GrammarDiff diff = new GrammarDiff(ont1, ont2, outputDir, true);
		ConceptChangeSet cs = diff.getDiff();
		
		if(cs != null) {
			String report = diff.getCSVChangeReport();
			serializeString(report, outputDir, "diffLog.csv");
			diff.serializeXMLReport();
		}
	}
}