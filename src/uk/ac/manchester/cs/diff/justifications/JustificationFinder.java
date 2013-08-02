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
package uk.ac.manchester.cs.diff.justifications;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owl.explanation.api.ExplanationGenerator;
import org.semanticweb.owl.explanation.api.ExplanationGeneratorFactory;
import org.semanticweb.owl.explanation.api.ExplanationManager;
import org.semanticweb.owl.explanation.impl.laconic.LaconicExplanationGeneratorFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import uk.ac.manchester.cs.diff.axiom.CategoricalDiff;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class JustificationFinder {
	private OWLOntology ont;
	private OWLReasonerFactory rf;
	private OWLOntologyManager man;
	private ExplanationGeneratorFactory<OWLAxiom> regFac, lacFac;
	private int lacJustLimit, justLimit;
	private int justCounter = 0, entCounter = 0;

	/**
	 * Constructor
	 * @param ont	OWL ontology
	 */
	public JustificationFinder(OWLOntology ont, int nrJusts) {
		this.ont = ont;
		this.justLimit = nrJusts;
		this.lacJustLimit = nrJusts;
		man = OWLManager.createOWLOntologyManager();
		rf = new org.semanticweb.HermiT.Reasoner.ReasonerFactory();
		regFac = ExplanationManager.createExplanationGeneratorFactory(rf);
		lacFac = new LaconicExplanationGeneratorFactory<OWLAxiom>(regFac);
	} 
	
	
	/**
	 * Get all justifications for a given set of entailments (concurrently)
	 * @param entailments	Set of entailments
	 * @return Set of (sets of) justifications for the given entailments
	 */
	public Set<Set<Explanation<OWLAxiom>>> getJustificationsConcurrently(Set<OWLAxiom> entailments) throws OWLOntologyCreationException {
		ForkJoinPool fjPool = new ForkJoinPool();
		return fjPool.invoke(new RegularJustificationFinder(entailments, justLimit));
	}
	
	
	/**
	 * Get all justifications for a given set of entailments (sequentially)
	 * @param entailments	Set of entailments
	 * @return Set of (sets of) justifications for the given entailments
	 */
	public Set<Set<Explanation<OWLAxiom>>> getJustifications(Set<OWLAxiom> entailments) throws OWLOntologyCreationException {
		Set<Set<Explanation<OWLAxiom>>> regExps = new HashSet<Set<Explanation<OWLAxiom>>>();
		for(OWLAxiom ax : entailments) {
			ExplanationGenerator<OWLAxiom> exGen = regFac.createExplanationGenerator(ont);
			Set<Explanation<OWLAxiom>> justs = exGen.getExplanations(ax, justLimit);
			regExps.add(justs);
			if(justs.isEmpty())
				System.err.println("\n\t !! Could not retrieve justifications for axiom:\n\t\t" + CategoricalDiff.getManchesterRendering(ax));
		}
		return regExps;
	}
	
	
	/**
	 * Justification finder
	 */
	public class RegularJustificationFinder extends RecursiveTask<Set<Set<Explanation<OWLAxiom>>>> {
		private static final long serialVersionUID = -953162808746083965L;
		private Set<OWLAxiom> axioms;
    	private int limit;
    	private int MAX_AXIOM_SET_SIZE = 10;
    	
    	/**
    	 * Constructor
    	 * @param axioms	Set of axioms to get justifications for
    	 * @param limit	Number of desired justifications per axiom
    	 */
		public RegularJustificationFinder(Set<OWLAxiom> axioms, int limit) {
			this.axioms = axioms;
			this.limit = limit;
		}

		/**
		 * Compute the set of entailments for the given set of axioms
		 * @return Set of sets of justifications
		 */
		public Set<Set<Explanation<OWLAxiom>>> computeDirectly() {
			Set<Set<Explanation<OWLAxiom>>> regExps = new HashSet<Set<Explanation<OWLAxiom>>>();
			for(OWLAxiom ax : axioms) {
				ExplanationGenerator<OWLAxiom> exGen = regFac.createExplanationGenerator(ont);
				Set<Explanation<OWLAxiom>> justs = exGen.getExplanations(ax, limit);
				regExps.add(justs);
				if(justs.isEmpty())
					System.err.println("\n\t !! Could not retrieve justifications for axiom:\n\t\t" + CategoricalDiff.getManchesterRendering(ax));
			}
			return regExps;
		}
	
		@Override
		protected Set<Set<Explanation<OWLAxiom>>> compute() {
			Set<Set<Explanation<OWLAxiom>>> result = new HashSet<Set<Explanation<OWLAxiom>>>();
			if(axioms.size() > MAX_AXIOM_SET_SIZE) {
				int mid = axioms.size()/2;
				OWLAxiom[] axArr = axioms.toArray(new OWLAxiom[axioms.size()]);
				Set<OWLAxiom> firstHalf = new HashSet<OWLAxiom>();
				Set<OWLAxiom> secondHalf = new HashSet<OWLAxiom>();
				for(int i = 0; i < mid; i++)			
					firstHalf.add(axArr[i]);
				for(int i = mid; i < axArr.length; i++)	
					secondHalf.add(axArr[i]);
		
				RegularJustificationFinder cat1 = new RegularJustificationFinder(firstHalf, limit);
				cat1.fork();
				RegularJustificationFinder cat2 = new RegularJustificationFinder(secondHalf, limit);
				result.addAll(cat2.invoke());
				result.addAll(cat1.join());
			}
			else result.addAll(computeDirectly());
			return result;
		}
    }
	
	
	/**
	 * Given an entailment and a set of regular justifications, compute and return the set of laconicized
	 * justifications for that entailment
	 * @param ax	Entailment
	 * @param exps	Set of regular justifications for the specifed entailment
	 * @return Set of laconic justifications for the entailment
	 */
	public Set<Set<OWLAxiom>> getLaconicJustifications(OWLAxiom ax, Set<Explanation<OWLAxiom>> exps) {
		Set<Set<OWLAxiom>> results = new HashSet<Set<OWLAxiom>>();
		Set<Future<Set<Set<OWLAxiom>>>> futureList = new HashSet<Future<Set<Set<OWLAxiom>>>>();
		ExecutorService exec = Executors.newFixedThreadPool(exps.size());
		
		for(Explanation<OWLAxiom> exp : exps) {
			boolean forkProcess = false;
			for(OWLAxiom axiom : exp.getAxioms()) {
				if(axiom.getNestedClassExpressions().size() >= 20) {
					forkProcess = true;
					break;
				}
			}
			Future<Set<Set<OWLAxiom>>> handler = null;
			if(forkProcess)
				handler = exec.submit(new ProcessLaconicJustificationFinder(ax, exp.getAxioms()));
			else
				handler = exec.submit(new ThreadedLaconicJustificationFinder(ax, exp.getAxioms()));
			futureList.add(handler);
		}

		for(Future<Set<Set<OWLAxiom>>> f : futureList) {
			try {
				if(f.get() != null)
					results.addAll(f.get());
			} catch (CancellationException | InterruptedException | ExecutionException e) { /* Do nothing */ }
		}
		exec.shutdownNow();
		return results;
	}
	
	
	/**
	 * Process-based laconic justification finder with built-in timeout
	 */
	public class ProcessLaconicJustificationFinder implements Callable<Set<Set<OWLAxiom>>> {
		private OWLAxiom ax;
		private Set<OWLAxiom> just;
		
		/**
		 * Constructor
		 * @param ax	Entailment
		 * @param just	Justification
		 */
		public ProcessLaconicJustificationFinder(OWLAxiom ax, Set<OWLAxiom> just) {
			this.ax = ax;
			this.just = just;
		}
		
		@Override
		public Set<Set<OWLAxiom>> call() {
			Set<Set<OWLAxiom>> out = new HashSet<Set<OWLAxiom>>();
			List<String> list = new ArrayList<String>();
			
			File entFile = serializeOntAndGetFile("ent", entCounter, Collections.singleton(ax));
			list.add(entFile.getAbsolutePath());
			
			File justFile = serializeOntAndGetFile("just", justCounter, just);
			list.add(justFile.getAbsolutePath());
			
			String output = null;
			try {
				Process p = executeOperation(LaconicJustificationFinder.class, false, list);
				output = streamToString(p.getInputStream());
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
			if(output != null) {
				if(output.startsWith("Ontology")) {
					try {
						out.add(man.loadOntologyFromOntologyDocument(new StringDocumentSource(output)).getAxioms());
					} catch (OWLOntologyCreationException e) {
						e.printStackTrace();
					}
				}
			}
			justFile.deleteOnExit();
			entFile.deleteOnExit();
			return out;
		}
		
		/**
		 * Create and serialize an ontology containing the given axioms
		 * @param desc	File description 
		 * @param counter	File counter
		 * @param axioms	Set of axioms
		 * @return The serialized ontology file object
		 */
		private File serializeOntAndGetFile(String desc, int counter, Set<OWLAxiom> axioms) {
			File f = new File("temp" + File.separator + desc + counter + ".owl");
			try {
				OWLOntology ont = man.createOntology(axioms);
				man.saveOntology(ont, new OWLXMLOntologyFormat(), IRI.create(f));
				if(desc.equals("ent")) entCounter++;
				else if(desc.equals("just")) justCounter++;
			} catch (OWLOntologyStorageException | OWLOntologyCreationException e) {
				e.printStackTrace();
			}
			return f;
		}
		
		/**
		 * Convert a given input stream into a String
		 * @param in	Input stream
		 * @return String containing the content of the input stream
		 * @throws IOException
		 */
		private String streamToString(InputStream in) throws IOException {
			StringBuilder out = new StringBuilder();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			for(String line = br.readLine(); line != null; line = br.readLine()) 
				out.append(line);
			br.close();
			return out.toString();
		}
		
		/**
		 * Execute a new process based on the given parameters 
		 * @param c	Class to execute
		 * @param redirectIO	true if I/O of the class is to be redirected to this process, false otherwise
		 * @param args	Additional arguments
		 * @return Executed process
		 * @throws IOException
		 * @throws InterruptedException
		 */
		private Process executeOperation(Class<? extends Object> c, boolean redirectIO, List<String> args) 
				throws IOException, InterruptedException {
			String javaHome = System.getProperty("java.home");
			String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
			String classPath = System.getProperty("java.class.path");
			String className = c.getCanonicalName();

			ArrayList<String> cmdArgs = new ArrayList<String>();
			cmdArgs.add(javaBin);
			cmdArgs.add("-cp");
			cmdArgs.add(classPath);
			cmdArgs.add(className);
			cmdArgs.addAll(args);

			ProcessBuilder builder = new ProcessBuilder(cmdArgs);
			builder.redirectError(Redirect.INHERIT);			
			if(redirectIO) builder.redirectOutput(Redirect.INHERIT);
			else builder.redirectOutput(Redirect.PIPE);
			
			Process process = builder.start();
			process.waitFor();
			return process;
		}
	}


	/**
	 * Thread-based laconic justification finder
	 */
    public class ThreadedLaconicJustificationFinder implements Callable<Set<Set<OWLAxiom>>> {
    	private Set<OWLAxiom> exp;
    	private OWLAxiom axiom;
    	
    	/**
    	 * Constructor
    	 * @param axiom	Entailment
    	 * @param exp	Justification
    	 */
		public ThreadedLaconicJustificationFinder(OWLAxiom axiom, Set<OWLAxiom> exp) {
			this.exp = exp;
			this.axiom = axiom;
		}

		@Override
		public Set<Set<OWLAxiom>> call() throws Exception {
			ExplanationGenerator<OWLAxiom> lacGen = lacFac.createExplanationGenerator(exp);
			Set<Set<OWLAxiom>> results = new HashSet<Set<OWLAxiom>>();
			try {
				Set<Explanation<OWLAxiom>> lacjusts = lacGen.getExplanations(axiom, lacJustLimit);
				for(Explanation<OWLAxiom> exp : lacjusts) {
					results.add(exp.getAxioms());
				}
			} catch(IllegalArgumentException e) {
				e.printStackTrace();
			}
			return results;
		}
    }
}
