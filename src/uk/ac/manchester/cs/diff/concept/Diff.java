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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import uk.ac.manchester.cs.diff.concept.changeset.DiffResult;
import uk.ac.manchester.cs.diff.concept.changeset.WitnessConcepts;
import uk.ac.manchester.cs.diff.concept.changeset.WitnessPack;
import uk.ac.manchester.cs.diff.utils.ReasonerLoader;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class Diff implements Callable<DiffResult>{
	private OWLOntology ont1, ont2;
	private OWLReasoner ont1reasoner, ont2reasoner;
	private OWLDataFactory df;
	private Map<OWLClass,Set<OWLClassExpression>> ont1diff, ont2diff;
	private Set<OWLAxiom> extraAxioms;
	private Set<OWLClass> sig, affected;
	private boolean verbose;
	private String diff;

	
	/**
	 * Constructor for subconcept diff w.r.t. given signature
	 * @param f1	Ontology 1 file
	 * @param f2	Ontology 2 file
	 * @param diff	Diff type
	 * @param verbose	Verbose mode
	 */
	public Diff(File f1, File f2, String diff, boolean verbose) {
		this.ont1 = loadOntology(f1);
		this.ont2 = loadOntology(f2);
		this.diff = diff;
		this.verbose = verbose;
		sig = new HashSet<OWLClass>(ont1.getClassesInSignature());
		sig.addAll(ont2.getClassesInSignature());
		df = OWLManager.getOWLDataFactory();
		initDataStructures();
		equalizeSignatures(ont1, ont2);
	}
	
	
	/**
	 * Constructor for subconcept diff w.r.t. given signature
	 * @param f1	Ontology 1 file
	 * @param f2	Ontology 2 file
	 * @param sig	Set of concept names
	 * @param diff	Diff type
	 * @param verbose	Verbose mode
	 */
	public Diff(File f1, File f2, Set<OWLClass> sig, String diff, boolean verbose) {
		this.ont1 = loadOntology(f1);
		this.ont2 = loadOntology(f2);
		this.sig = sig;
		this.diff = diff;
		this.verbose = verbose;
		df = OWLManager.getOWLDataFactory();
		initDataStructures();
		equalizeSignatures(ont1, ont2);
	}

	
	/**
	 * Load given ontology file
	 * @param f	File
	 * @return Load OWL ontology
	 */
	private OWLOntology loadOntology(File f) {
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology ont = null;
		try {
			ont = man.loadOntologyFromOntologyDocument(f);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		man.removeAxioms(ont, ont.getABoxAxioms(true));
		return ont;
	}

	
	/**
	 * Instantiate diff data structures: maps of concept names to sets of witness concepts  
	 */
	private void initDataStructures() {
		ont1diff = new HashMap<OWLClass,Set<OWLClassExpression>>();
		ont2diff = new HashMap<OWLClass,Set<OWLClassExpression>>();
	}


	/**
	 * Get the concept-based change set between the given ontologies 
	 * @return Diff result
	 * @throws InterruptedException
	 */
	public DiffResult call() {
		long start = System.currentTimeMillis();
		boolean atdiff = false;
		Map<OWLClass,OWLClassExpression> map = null;
		if(!atdiff) map = getSubconceptsMapping();
		
		// Precompute subsumptions
		classifyOntologies();
		equalizeSignatures(ont1, ont2);
		
		// Get set of affected concept names
		affected = computeChangeWitnesses(map);

		if(!atdiff) { // Remove extra axioms
			ont1.getOWLOntologyManager().removeAxioms(ont1, extraAxioms);
			ont2.getOWLOntologyManager().removeAxioms(ont2, extraAxioms);
		}

		// Precompute atomic subsumptions of altered ontologies (incremental reasoning would be good here)
		classifyOntologies();
		
		// Get the final, partitioned according to witness impact, change set
		DiffResult diffResult = splitDirectIndirectChanges(affected);

		long end = System.currentTimeMillis();
		System.out.println("finished diff " + diff + " (total time: " + (end-start)/1000.0 + " secs)");

		return diffResult;
	}


	/**
	 * Classify both ontologies
	 * @throws InterruptedException
	 */
	public void classifyOntologies() {
		if(ont1reasoner!=null) ont1reasoner.dispose();
		if(ont2reasoner!=null) ont2reasoner.dispose();
		
		long start = System.currentTimeMillis();
		if(verbose) System.out.println("[diff" + diff + "] Classifying ontologies...");
//		ExecutorService exec = Executors.newSingleThreadExecutor();
		
		System.out.println("[diff " + diff + "] Ontology 1: " + ont1.getLogicalAxiomCount() + 
				", Ontology 2: " + ont2.getLogicalAxiomCount());
		Classifier ont1worker = new Classifier(ont1);

		Thread t1 = new Thread(ont1worker);
		t1.start();
		
//		exec.execute(ont1worker);
		ont2reasoner = classifyHere(ont2);
		
		try {
			t1.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
//		exec.shutdown();
//		try { exec.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS); } 
//		catch (InterruptedException e) { e.printStackTrace(); }
		ont1reasoner = ont1worker.getReasoner();
		
		if(verbose) System.out.println("[diff" + diff + "] done (" + (System.currentTimeMillis()-start)/1000.0 + " secs)");
	}
	
	
	private OWLReasoner classifyHere(OWLOntology ont) {
		System.out.println("[diff" + diff + "] [internal] Starting classification on given ontology");
		OWLReasoner reasoner = new ReasonerLoader(ont, verbose).createFactReasoner(false);
		
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		
		System.out.println("[diff" + diff + "] [internal] done classifying");
		return reasoner;
	}


	/**
	 * Compute change witnesses between the given ontologies
	 * @param ont1reasoner	Ontology 1 reasoner instance
	 * @param ont2reasoner	Ontology 2 reasoner instance
	 * @param map	Map of fresh concept names to complex concepts
	 * @return Set of affected concept names
	 */
	private Set<OWLClass> computeChangeWitnesses(Map<OWLClass,OWLClassExpression> map) {
		long start = System.currentTimeMillis();
		if(verbose) System.out.print("[diff" + diff + "] Computing change witnesses... ");
		Set<OWLClass> affected = new HashSet<OWLClass>();
		Set<OWLClass> toRemove1 = null, toRemove2 = null;
		if(diff.equals("L")) {
			toRemove1 = ont1reasoner.getSuperClasses(df.getOWLThing(), false).getFlattened();
			toRemove2 = ont2reasoner.getSuperClasses(df.getOWLThing(), false).getFlattened();
		}
		else if(diff.equals("R")) {
			toRemove1 = ont1reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
			toRemove2 = ont2reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
		}

		// Get change witnesses for each concept
		for(OWLClass subc : sig) {
			WitnessConcepts wit = getWitnesses(subc, map, toRemove1, toRemove2, ont1reasoner, ont2reasoner);

			if(!wit.isEmpty()) affected.add(subc);
			addChangeToMap(subc, wit.getLHSWitnesses(), ont1diff);
			addChangeToMap(subc, wit.getRHSWitnesses(), ont2diff);
		}
		long end = System.currentTimeMillis();
		if(verbose) System.out.println("[diff" + diff + "] done (" + (end-start)/1000.0 + " secs)");
		return affected;
	}


	/**
	 * Distinguish between directly and indirectly affected concepts
	 * @param affected	Set of affected concept names
	 * @return Concept-based change set
	 */
	private DiffResult splitDirectIndirectChanges(Set<OWLClass> affected) {
		if(verbose) System.out.print("[diff" + diff + "] Splitting directly and indirectly affected concepts... ");
		long start3 = System.currentTimeMillis();

		WitnessPack lhs = getWitnesses(ont1diff, ont1reasoner);
		WitnessPack rhs = getWitnesses(ont2diff, ont2reasoner);

		long end = System.currentTimeMillis();
		if(verbose) System.out.println("[diff" + diff + "] done (" + (end-start3)/1000.0 + " secs)");
		return new DiffResult(lhs, rhs);
	}


	/**
	 * Add affected concept and given witnesses to the specified diff map
	 * @param affected
	 * @param witnesses
	 * @param map
	 */
	private void addChangeToMap(OWLClass affected, Set<OWLClassExpression> witnesses, Map<OWLClass,Set<OWLClassExpression>> map) {
		if(!witnesses.isEmpty())
			map.put(affected, witnesses);
	}


	/**
	 * Get the sets of (LHS and RHS) specialisation witnesses for the given concept
	 * @param subc	Concept
	 * @param map	Map of fresh concept names to the concepts they represent
	 * @return Concept witnesses for the given concept
	 */
	private WitnessConcepts getWitnesses(OWLClass subc, Map<OWLClass,OWLClassExpression> map, 
			Set<OWLClass> toRemove1, Set<OWLClass> toRemove2, OWLReasoner ont1reasoner, OWLReasoner ont2reasoner) {
		Set<OWLClass> ind1 = null, ind2 = null;
		if(diff.equals("L")) {
			ind1 = ont1reasoner.getSuperClasses(subc, false).getFlattened();
			ind2 = ont2reasoner.getSuperClasses(subc, false).getFlattened();
			if(!subc.isOWLThing()) {
				ind1.removeAll(toRemove1); ind2.removeAll(toRemove2);
			}
		}
		else if(diff.equals("R")) {
			ind1 = ont1reasoner.getSubClasses(subc, false).getFlattened();
			ind2 = ont2reasoner.getSubClasses(subc, false).getFlattened();
			if(!subc.isOWLNothing()) {
				ind1.removeAll(toRemove1); ind2.removeAll(toRemove2);
			}
		}
		return getDifferentConcepts(ind1, ind2, map);
	}


	/**
	 * Get the witnesses for the the difference in the given sub or superclass sets
	 * @param set1	Set of classes
	 * @param set2	Set of classes
	 * @param map	Map of fresh concept names to concepts
	 * @return Witness concepts in the sub or superclass sets diff 
	 */
	private WitnessConcepts getDifferentConcepts(Set<OWLClass> set1, Set<OWLClass> set2, Map<OWLClass,OWLClassExpression> map) {
		Set<OWLClassExpression> rhsWit = getWitnessDiff(set1, set2, map);
		Set<OWLClassExpression> lhsWit = getWitnessDiff(set2, set1, map);
		return new WitnessConcepts(lhsWit, rhsWit);
	}


	/**
	 * Get the set of different concepts between the given sets
	 * @param set1	Set of classes
	 * @param set2	Set of classes
	 * @param map	Map of fresh concept names to concepts
	 * @return Set of different concepts between sets
	 */
	private Set<OWLClassExpression> getWitnessDiff(Set<OWLClass> set1, Set<OWLClass> set2, Map<OWLClass,OWLClassExpression> map) {
		Set<OWLClassExpression> wit = new HashSet<OWLClassExpression>();
		for(OWLClass c : set2) {
			if(!set1.contains(c)) {
				OWLClassExpression ce = c;
				if(map != null && map.containsKey(c))
					ce = map.get(c);
				wit.add(ce);
			}
		}
		return wit;
	}


	/**
	 * Extrapolate direct and indirect witnesses from the given map of affected concepts and witnesses 
	 * @param affectedConceptMap	Map of concepts to their change witnesses
	 * @param reasoner	Reasoner instance
	 * @param diffL	true if checking specialisations, false if generalisations
	 * @return Pack of direct and indirect witnesses
	 */
	private WitnessPack getWitnesses(Map<OWLClass,Set<OWLClassExpression>> affectedConceptMap, OWLReasoner reasoner) {
		Map<OWLClassExpression,Set<OWLClass>> witMap = getWitnessMap(affectedConceptMap);
		Map<OWLClass, Set<OWLAxiom>> directWits = new HashMap<OWLClass,Set<OWLAxiom>>();
		Map<OWLClass, Set<OWLAxiom>> indirectWits = new HashMap<OWLClass,Set<OWLAxiom>>();
		for(OWLClassExpression ce : witMap.keySet()) {
			//				System.out.println("Checking concept: " + getManchesterRendering(ce));
			Set<OWLClass> subs = null;
			if(diff.equals("L")) subs = reasoner.getSubClasses(ce, true).getFlattened();
			else subs = reasoner.getSuperClasses(ce, true).getFlattened();

			//				for(OWLClass c : subs) {
			//					System.out.println("\tDirect subclass: " + getManchesterRendering(c));
			//				}

			for(OWLClass c : witMap.get(ce)) {
				if(subs.contains(c)) { // direct witness
					//						System.out.println("\tDirect witness for " + getManchesterRendering(c) );
					if(directWits.containsKey(c)) {
						Set<OWLAxiom> wits = directWits.get(c);
						if(diff.equals("L")) wits.add(df.getOWLSubClassOfAxiom(c, ce));
						else wits.add(df.getOWLSubClassOfAxiom(ce, c));
						directWits.put(c, wits);
					}
					else {
						if(diff.equals("L")) 
							directWits.put(c, new HashSet<OWLAxiom>(Collections.singleton(df.getOWLSubClassOfAxiom(c, ce))));
						else 
							directWits.put(c, new HashSet<OWLAxiom>(Collections.singleton(df.getOWLSubClassOfAxiom(ce, c))));
					}
				}
				else { // indirect witness
					//						System.out.println("\tIndirect witness for " + getManchesterRendering(c));
					if(indirectWits.containsKey(c)) {
						Set<OWLAxiom> wits = indirectWits.get(c);
						if(diff.equals("L")) wits.add(df.getOWLSubClassOfAxiom(c, ce));
						else wits.add(df.getOWLSubClassOfAxiom(ce, c));
						indirectWits.put(c, wits);
					}
					else {
						if(diff.equals("L")) 
							indirectWits.put(c, new HashSet<OWLAxiom>(Collections.singleton(df.getOWLSubClassOfAxiom(c, ce))));
						else 
							indirectWits.put(c, new HashSet<OWLAxiom>(Collections.singleton(df.getOWLSubClassOfAxiom(ce, c))));
					}
				}
			}
		}
		return new WitnessPack(directWits, indirectWits);
	}


	/**
	 * Given a map of concepts to witnesses, get a reversed map of witnesses to concepts whose change they witness
	 * @param map	Map of concepts to witnesses
	 * @return Map of witnesses to concepts whose change they witness
	 */
	private Map<OWLClassExpression,Set<OWLClass>> getWitnessMap(Map<OWLClass,Set<OWLClassExpression>> map) {
		Map<OWLClassExpression,Set<OWLClass>> output = new HashMap<OWLClassExpression,Set<OWLClass>>();
		for(OWLClass c : map.keySet()) {
			Set<OWLClassExpression> wits = map.get(c);
			for(OWLClassExpression wit : wits) {
				if(output.containsKey(wit)) {
					Set<OWLClass> classes = output.get(wit);
					classes.add(c);
					output.put(wit, classes);
				}
				else 
					output.put(wit, new HashSet<OWLClass>(Collections.singleton(c)));
			}
		}
		return output;
	}


	/**
	 * Class hierarchy pre-computation
	 */
	class Classifier implements Runnable {
		private OWLOntology ont;
		private OWLReasoner reasoner;

		public Classifier(OWLOntology ont) {
			this.ont = ont;
		}

		@Override
		public void run() {
			System.out.println("[diff" + diff + "] [Classifier] Starting classification on given ontology");
			reasoner = new ReasonerLoader(ont, verbose).createFactReasoner(false);
			reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
			System.out.println("[diff" + diff + "] [Classifier] done classifying ");
		}

		public OWLReasoner getReasoner() {
			return reasoner;
		}
	}
	

	/**
	 * Collect sub-concepts in both ontologies
	 * @return Set of subconcepts
	 */
	private Set<OWLClassExpression> collectSCs() {
		if(verbose) System.out.print("[diff" + diff + "] Extracting subconcepts from given ontologies... ");
		Set<OWLClassExpression> scs = new HashSet<OWLClassExpression>(); 
		getSubConcepts(ont1, scs);
		getSubConcepts(ont2, scs);
		if(verbose) System.out.println("[diff" + diff + "] done. Nr. of subconcepts: " + scs.size());
		return scs;
	}


	/**
	 * Get sub-concepts of an ontology
	 * @param ont	Ontology
	 * @param sc	Set of subconcepts
	 * @return Updated set of subconcepts
	 */
	private Set<OWLClassExpression> getSubConcepts(OWLOntology ont, Set<OWLClassExpression> sc) {
		Set<OWLLogicalAxiom> axs = ont.getLogicalAxioms();
		for(OWLAxiom ax : axs) {
			Set<OWLClassExpression> ax_sc = ax.getNestedClassExpressions();
			for(OWLClassExpression ce : ax_sc) {
				if(!sc.contains(ce) && !ce.isOWLThing() && !ce.isOWLNothing()) {
					if(ce.isAnonymous()) {
						sc.add(ce); getSubConcepts(ce, sc);
					}
				}
			}
		}
		return sc;
	}


	/**
	 * Recursively get subconcepts of subconcept
	 * @param ce	Subconcept
	 * @param sc	Set of subconcepts
	 */
	private void getSubConcepts(OWLClassExpression ce, Set<OWLClassExpression> sc) {
		if(ce.getNestedClassExpressions().size() > 0) {
			for(OWLClassExpression c : ce.getNestedClassExpressions()) {
				if(!sc.contains(c) && !c.isOWLThing() && !c.isOWLNothing()) {
					if(c.isAnonymous()) {
						sc.add(c);
						getSubConcepts(c, sc);
					}
				}
			}
		}
	}


	/**
	 * Create a mapping between a new term "TempX" and each sub-concept, and add the appropriate
	 * equivalence axioms to each ontology
	 * @return Map of new terms to subconcepts
	 */
	private Map<OWLClass,OWLClassExpression> getSubconceptsMapping() {
		Set<OWLClassExpression> sc = collectSCs();
		Map<OWLClass,OWLClassExpression> map = new HashMap<OWLClass,OWLClassExpression>();
		int counter = 1;
		extraAxioms = new HashSet<OWLAxiom>();
		System.out.println("[diff" + diff + "] Adding axioms...");
		for(OWLClassExpression ce : sc) {
			OWLClass c = df.getOWLClass(IRI.create("diffSubc_" + counter));
			map.put(c, ce);
			OWLAxiom ax = null;
			if(diff.equals("L"))
				ax = df.getOWLSubClassOfAxiom(c, ce);
			else if(diff.equals("R"))
				ax = df.getOWLEquivalentClassesAxiom(c, ce);
			extraAxioms.add(ax); counter++;
		}
		ont1.getOWLOntologyManager().addAxioms(ont1, extraAxioms);
		ont2.getOWLOntologyManager().addAxioms(ont2, extraAxioms);
		System.out.println("[diff" + diff + "] Added " + extraAxioms.size() + " axioms");
		return map;
	}
	
	
	/**
	 * Given two ontologies, inject entity declarations so that both ontologies
	 * end up with the same signature
	 */
	private void equalizeSignatures(OWLOntology ont1, OWLOntology ont2) {
		Set<OWLEntity> ont1sig = ont1.getSignature();
		Set<OWLEntity> ont2sig = ont2.getSignature();
		
		ont1sig.removeAll(ont2sig);
		ont2sig.removeAll(ont1sig);
		
		List<AddAxiom> ont1axs = new ArrayList<AddAxiom>();
		for(OWLEntity c : ont1sig) {
			ont1axs.add(new AddAxiom(ont2, df.getOWLDeclarationAxiom(c)));
		}
		ont2.getOWLOntologyManager().applyChanges(ont1axs);
		
		List<AddAxiom> ont2axs = new ArrayList<AddAxiom>();
		for(OWLEntity c : ont2sig) {
			ont2axs.add(new AddAxiom(ont1, df.getOWLDeclarationAxiom(c)));
		}
		ont1.getOWLOntologyManager().applyChanges(ont2axs);
	}
	
	
	/**
	 * Get the set of affected concept names
	 * @return Set of affected concept names
	 */
	public Set<OWLClass> getAffectedConcepts() {
		return affected;
	}
}