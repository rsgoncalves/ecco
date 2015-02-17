/*******************************************************************************
// * This file is part of ecco.
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
package uk.ac.manchester.cs.diff;

import java.io.File;
import java.io.InputStream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.IRIDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.util.VersionInfo;

import uk.ac.manchester.cs.diff.exception.DuplicateArgumentException;
import uk.ac.manchester.cs.diff.exception.InsufficientArgumentsException;
import uk.ac.manchester.cs.diff.exception.MissingArgumentException;
import uk.ac.manchester.cs.diff.exception.UnrecognizedArgumentException;

/**
 * @author Rafael S. Goncalves <br>
 * Information Management Group (IMG) <br>
 * School of Computer Science <br>
 * University of Manchester <br>
 */
public class EccoRunner {
	private OWLOntologyLoaderConfiguration config;
	private EccoSettings settings;
	private static final String
			versionInfo = "2.7",
			releaseDate = "16-Feb-2015",
			owlapiVersion = VersionInfo.getVersionInfo().getVersion(),
			programTitle = 
			"-------------------------------------------------------------------\n" +
			"	     ecco: a diff tool for OWL 2 ontologies\n" +
			"	        v" + versionInfo + " released on " + releaseDate + "\n" +		
			"-------------------------------------------------------------------\n" +
			"by Rafael Goncalves. Powered by the OWL API version " + owlapiVersion + "\n";
	
	
	/**
	 * Constructor
	 * @param settings	ecco settings
	 */
	public EccoRunner(EccoSettings settings) {
		this.settings = settings;
		config = new OWLOntologyLoaderConfiguration();
		config = config.setLoadAnnotationAxioms(false);
		if(!settings.isProcessingImports()) {
			config = config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
			config = config.setFollowRedirects(false);
		}
	}
	
	
	/**
	 * Start ecco diff
	 * @param ont1filePath	Ontology 1 file path
	 * @param localOnt1	true if ontology 1 is a file in the local file system, false otherwise
	 * @param ont2filePath	Ontology 2 file path
	 * @param localOnt2	true if ontology 2 is a file in the local file system, false otherwise
	 */
	public void start(String ont1filePath, boolean localOnt1, String ont2filePath, boolean localOnt2) {
		OWLOntology ont1 = loadOntology(1, ont1filePath, localOnt1);
		OWLOntology ont2 = loadOntology(2, ont2filePath, localOnt2);
	
		if(ont1 != null && ont2 != null) {
			Ecco ecco = new Ecco(ont1, ont2, settings);
			ecco.computeDiff();
		}
	}
	
	
	/**
	 * Load ontology from a file path
	 * @param ontNr	Ontology number
	 * @param filepath	Ontology file path
	 * @param localFile	true if local file, false otherwise
	 * @return Loaded ontology
	 */
	public OWLOntology loadOntology(int ontNr, String filepath, boolean localFile) {
		String filename = filepath.substring(filepath.lastIndexOf(File.separator)+1, filepath.length());
		System.out.println("Input " + ontNr + ": " + filename + " (" + filepath + ")");
		if(filepath.contains("\\")) filepath = filepath.replace("\\", "/");
		
		// Load ontology
		OWLOntology ont = null;
		try {
			if(localFile) ont = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new IRIDocumentSource(IRI.create("file:///" + filepath)), config);
			else ont = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new IRIDocumentSource(IRI.create(filepath)), config);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		return ont;
	}
	
	
	/**
	 * Load ontology from an input stream
	 * @param ontNr	Ontology nunmber
	 * @param stream	Input stream
	 * @return OWL ontology
	 */
	public OWLOntology loadOntology(int ontNr, InputStream stream) {
		OWLOntology ont = null;
		try {
			ont = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new StreamDocumentSource(stream), config); 
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		return ont;
	}
	
	
	/**
	 * Print usage message 
	 */
	private static void printUsage() {
		System.out.println(" Usage:\n\t-ont1 [ONTOLOGY] -ont2 [ONTOLOGY] [OPTIONS]");
		System.out.println();
		System.out.println("	[ONTOLOGY]	An input ontology file path or URL");
		System.out.println();
		System.out.println("	[OPTIONS]");
		System.out.println("	-o		absolute output directory [default: ECCO-FOLDER/out ]");
		System.out.println("	-t		transform resulting XML report into HTML");
		System.out.println("	-c		compute one of: [ at | sub | gr | cvs ] concept diff");
		System.out.println("	-r		analyze root ontologies only, i.e., ignore imports");
		System.out.println("	-n		normalize entity URIs, i.e. if two ontologies have the same entity names");
		System.out.println("			in a different namespace, this trigger establishes a common namespace");
		System.out.println("	-i		ignore Abox axioms");
		System.out.println("	-j		maximum number of justifications computed per ineffectual change. Reducing");
		System.out.println("			this can significantly speed up computation [default: 10]");
		System.out.println("	-v		verbose mode");
		System.out.println("	-h -help	print help message\n");
	}
	
	
	/**
	 * main
	 * @param args	Arguments
	 */
	public static void main(String[] args) {
		boolean isOnt1Set = false, isOnt2Set = false, localOnt1 = true, localOnt2 = true;
		String ont1filePath = null, ont2filePath = null;
		EccoSettings settings = new EccoSettings();
		System.out.println("\n" + programTitle);
		
		for(int i = 0; i < args.length; i++) {
			String arg = args[i].trim();
			if(arg.equalsIgnoreCase("-ont1")) {	// Ontology 1
				if(isOnt1Set) throw new DuplicateArgumentException("\nToo many -ont1 options provided.\n");
				if(++i == args.length) throw new MissingArgumentException("\n-ont1 must be followed by an ontology URI.\n");
				ont1filePath = args[i].trim(); isOnt1Set = true;
				if(ont1filePath.contains("http") || ont1filePath.contains("ftp")) localOnt1 = false;
			} 
			else if(arg.equalsIgnoreCase("-ont2")) {	// Ontology 2
				if(isOnt2Set) throw new DuplicateArgumentException("\nToo many -ont2 options provided.\n");
				if(++i == args.length) throw new MissingArgumentException("\n-ont2 must be followed by an ontology URI.\n");
				ont2filePath = args[i].trim(); isOnt2Set = true; 
				if(ont2filePath.contains("http") || ont2filePath.contains("ftp")) localOnt2 = false;
			}
			else if(arg.equalsIgnoreCase("-o")) {	// Output directory
				if(++i == args.length) throw new MissingArgumentException("\n-o must be followed by an output directory.\n");
				arg = args[i].trim(); settings.setOutputDirectory(arg);
			}
			else if(arg.equalsIgnoreCase("-c"))	{	// Concept diff type
				if(++i == args.length) throw new MissingArgumentException("\n-c must be followed by one of [ atomic | subconcept | grammar | contentcvs ].\n");
				arg = args[i].trim(); settings.setConceptDiffType(arg);
			}
			else if(arg.equalsIgnoreCase("-n")) settings.setNormalizeURIs(true); 		// Normalize entity namespaces
			else if(arg.equalsIgnoreCase("-t")) settings.setTransformToHTML(true);		// Transform XML into HTML
			else if(arg.equalsIgnoreCase("-r")) settings.setProcessImports(false);		// Process root ontologies only
			else if(arg.equalsIgnoreCase("-i"))	settings.setIgnoreAbox(true);			// Ignore Abox axioms
			else if(arg.equalsIgnoreCase("-v")) settings.setVerbose(true);				// Verbose mode
			else if(arg.equalsIgnoreCase("-j"))	{	// Number of justifications per ineffectual change
				if(++i == args.length) throw new MissingArgumentException("\n-j must be followed by a positive integer.");
				int nrJusts = Integer.parseInt(args[i].trim());
				if(nrJusts > 0) settings.setNumberOfJustifications(nrJusts);
			}
			else if(arg.equalsIgnoreCase("-h") || arg.equalsIgnoreCase("-help")) {		// Print help message
				printUsage(); System.exit(0);
			}
			else if(arg.startsWith("-")) {		// Unknown argument
				printUsage();
				throw new UnrecognizedArgumentException("\nUnrecognized option: " + arg + ". See usage above.\n");
			} 
			else {
				printUsage();
				throw new InsufficientArgumentsException("\nThere are not enough arguments to start ecco. "
						+ "The minimum parameters are -ont1 and -ont2. See full usage above.\n");
			}
		}
		System.out.println();
		if(ont1filePath == null) {
			printUsage();
			throw new MissingArgumentException("\nMissing -ont1 argument, which should be followed by a file path or URL.\n");
		}
		if(ont2filePath == null) {
			printUsage();
			throw new MissingArgumentException("\nMissing -ont2 argument, which should be followed by a file path or URL.\n");
		}
		if(ont1filePath != null && ont2filePath != null) {
			EccoRunner runner = new EccoRunner(settings);
			runner.start(ont1filePath, localOnt1, ont2filePath, localOnt2);
		}
	}
}