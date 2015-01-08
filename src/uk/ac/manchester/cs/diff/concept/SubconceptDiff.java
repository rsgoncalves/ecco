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
package uk.ac.manchester.cs.diff.concept;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import uk.ac.manchester.cs.diff.axiom.LogicalDiff;
import uk.ac.manchester.cs.diff.concept.change.ConceptChange;
import uk.ac.manchester.cs.diff.concept.change.LHSConceptChange;
import uk.ac.manchester.cs.diff.concept.change.RHSConceptChange;
import uk.ac.manchester.cs.diff.concept.changeset.ConceptChangeSet;
import uk.ac.manchester.cs.diff.concept.changeset.WitnessAxioms;
import uk.ac.manchester.cs.diff.concept.changeset.WitnessConcepts;
import uk.ac.manchester.cs.diff.concept.changeset.WitnessPack;
import uk.ac.manchester.cs.diff.output.csv.CSVConceptDiffReport;
import uk.ac.manchester.cs.diff.output.xml.XMLConceptDiffReport;
import uk.ac.manchester.cs.diff.utils.ReasonerLoader;

/**
 * @author Rafael S. Goncalves <br>
 * Information Management Group (IMG) <br>
 * School of Computer Science <br>
 * University of Manchester <br>
 */
public class SubconceptDiff implements ConceptDiff {
	protected OWLOntology ont1, ont2;
	protected OWLReasoner ont1reasoner, ont2reasoner;
	protected OWLDataFactory df;
	protected Map<OWLClass,Set<OWLClassExpression>> ont1_diffL, ont1_diffR, ont2_diffL, ont2_diffR;
	protected Set<OWLAxiom> extraAxioms;
	protected Set<OWLEntity> sigma;
	protected String outputDir;
	protected boolean verbose, atomicOnly = false;
	protected ConceptChangeSet changeSet;
	
	/**
	 * Constructor for subconcept diff w.r.t. Sigma = sig(O1) U sig(O2)
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 * @param outputDir	Output directory
	 * @param verbose	Verbose mode
	 */
	public SubconceptDiff(OWLOntology ont1, OWLOntology ont2, String outputDir, boolean verbose) {
		this.ont1 = ont1;
		this.ont2 = ont2;
		this.outputDir = outputDir;
		this.verbose = verbose;
		df = OWLManager.getOWLDataFactory();
		sigma = new HashSet<OWLEntity>(new Signature().getUnionConceptNames(ont1, ont2));
		sigma.add(df.getOWLNothing()); sigma.add(df.getOWLThing());
		initDataStructures();
		equalizeSignatures(ont1, ont2);
	}
	
	
	/**
	 * Constructor for subconcept diff w.r.t. given signature
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 * @param sig	Signature (set of concept names)
	 * @param outputDir	Output directory
	 * @param verbose	Verbose mode
	 */
	public SubconceptDiff(OWLOntology ont1, OWLOntology ont2, Set<OWLEntity> sig, String outputDir, boolean verbose) {
		this.ont1 = ont1;
		this.ont2 = ont2;
		this.sigma = sig;
		this.outputDir = outputDir;
		this.verbose = verbose;
		df = OWLManager.getOWLDataFactory();
		initDataStructures();
		equalizeSignatures(ont1, ont2);
	}

	
	/**
	 * Instantiate diff data structures: maps of concept names to sets of witness concepts  
	 */
	private void initDataStructures() {
		ont1_diffL = new HashMap<OWLClass,Set<OWLClassExpression>>();
		ont1_diffR = new HashMap<OWLClass,Set<OWLClassExpression>>();
		ont2_diffL = new HashMap<OWLClass,Set<OWLClassExpression>>();
		ont2_diffR = new HashMap<OWLClass,Set<OWLClassExpression>>();
	}


	/**
	 * Get the concept-based change set between the given ontologies 
	 * @return Concept-based change set
	 */
	public ConceptChangeSet getDiff() {
		long start = System.currentTimeMillis();
		System.out.println("Computing concept diff...");
		if(verbose) System.out.println("   Input signature: sigma contains " + sigma.size() + " concept names");
		
		Map<OWLClass,OWLClassExpression> map = null;
		if(!atomicOnly) map = getSubConceptsMapping();
		classifyOntologies(ont1, ont2);
		
		Set<OWLClass> affected = computeChangeWitnesses(map);
		long mid = System.currentTimeMillis();
		
		if(!atomicOnly) {
			ont1.getOWLOntologyManager().removeAxioms(ont1, extraAxioms);
			ont2.getOWLOntologyManager().removeAxioms(ont2, extraAxioms);
			classifyOntologies(ont1, ont2);
		}
		
		changeSet = splitDirectIndirectChanges(affected, ont1reasoner, ont2reasoner);
		long end = System.currentTimeMillis();
		changeSet.setEntailmentDiffTime((mid-start)/1000.0);
		changeSet.setPartitioningTime((end-mid)/1000.0);
		changeSet.setTotalTime((end-start)/1000.0);
		System.out.println("finished concept diff (" + (end-start)/1000.0 + " secs)"); 
		if(verbose) printDiff();
		return changeSet;
	}
	
	
	/**
	 * Classify both ontologies
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 */
	public void classifyOntologies(OWLOntology ont1, OWLOntology ont2) {
		long start = System.currentTimeMillis();
		 System.out.print("   Precomputing inferences... ");
		
		ExecutorService exec = Executors.newFixedThreadPool(2);
		
		Classifier ont1worker = new Classifier(ont1);
		Classifier ont2worker = new Classifier(ont2);
		
		exec.execute(ont1worker); exec.execute(ont2worker);
		exec.shutdown();
		try {
			exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		 System.out.println("done (" + (System.currentTimeMillis()-start)/1000.0 + " secs)");
		
		ont1reasoner = ont1worker.getReasoner(); ont2reasoner = ont2worker.getReasoner();
	}
	
	
	/**
	 * Compute change witnesses between the given ontologies
	 * @param map	Map of fresh concept names to complex concepts
	 * @return Set of affected concept names
	 */
	protected Set<OWLClass> computeChangeWitnesses(Map<OWLClass,OWLClassExpression> map) {
		System.out.print("   Computing change witnesses... ");
		Set<OWLClass> affected = new HashSet<OWLClass>();
		long start = System.currentTimeMillis();

		// Get specialisation and generalisation witnesses for each concept
		for(OWLEntity subc : sigma) {
			if(subc instanceof OWLClass) {
				OWLClass c = (OWLClass)subc;
				WitnessConcepts specWit = getSpecialisationWitnesses(c, map, ont1reasoner, ont2reasoner);
				WitnessConcepts genWit = getGeneralisationWitnesses(c, map, ont1reasoner, ont2reasoner);

				if((specWit != null && !specWit.isEmpty()) || (genWit != null && !genWit.isEmpty())) affected.add(c);
				
				if(genWit != null) {
					addChangeToMap(c, genWit.getLHSWitnesses(), ont1_diffR);
					addChangeToMap(c, genWit.getRHSWitnesses(), ont2_diffR);
				}
				if(specWit != null) {
					addChangeToMap(c, specWit.getLHSWitnesses(), ont1_diffL);
					addChangeToMap(c, specWit.getRHSWitnesses(), ont2_diffL);
				}
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("done (" + (end-start)/1000.0 + " secs)");
		return affected;
	}

	
	/**
	 * Distinguish between directly and indirectly affected concepts
	 * @param affected	Set of affected concept names
	 * @param ont1reasoner	Ontology 1 reasoner
	 * @param ont2reasoner	Ontology 2 reasoner
	 * @return Concept-based change set
	 */
	protected ConceptChangeSet splitDirectIndirectChanges(Set<OWLClass> affected, OWLReasoner ont1reasoner, OWLReasoner ont2reasoner) {
		System.out.print("   Splitting directly and indirectly affected concepts... ");
		long start = System.currentTimeMillis();
		/* 
		 * TODO: The filtering of changes to (or via) Bottom and Top is somewhat crippled: The OWL API only allows us to extract unsatisfiable 
		 * and global *atomic* concepts, meaning that we'd have to test whether each extracted subconcept is unsatisfiable or equivalent to Top
		 */
		Set<OWLClass> topSuper1 = ont1reasoner.getEquivalentClasses(df.getOWLThing()).getEntities();
		Set<OWLClass> topSuper2 = ont2reasoner.getEquivalentClasses(df.getOWLThing()).getEntities();
		Set<OWLClass> botSub1 = ont1reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
		Set<OWLClass> botSub2 = ont2reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
		
		WitnessPack lhs_spec = getWitnesses(ont1_diffL, ont1reasoner, true, topSuper1, botSub1);
		WitnessPack lhs_gen = getWitnesses(ont1_diffR, ont1reasoner, false, topSuper1, botSub1);

		WitnessPack rhs_spec = getWitnesses(ont2_diffL, ont2reasoner, true, topSuper2, botSub2);
		WitnessPack rhs_gen = getWitnesses(ont2_diffR, ont2reasoner, false, topSuper2, botSub2);

		long end = System.currentTimeMillis();
		System.out.println("done (" + (end-start)/1000.0 + " secs)");
		return sortOutChangeSet(affected, lhs_spec, lhs_gen, rhs_spec, rhs_gen);
	}
	
	
	/**
	 * Given the set of affected concepts, and their respective (direct and/or indirect witnesses), create a concept-based change set
	 * for both ontologies, as well as an overall change set representation which takes into account changes in both ontologies 
	 * @param affected	Set of affected concept names
	 * @param lhs_spec	Pack of direct and indirect specialisation witnesses in ontology 1 
	 * @param lhs_gen	Pack of direct and indirect generalisation witnesses in ontology 1
	 * @param rhs_spec	Pack of direct and indirect specialisation witnesses in ontology 2
	 * @param rhs_gen	Pack of direct and indirect generalisation witnesses in ontology 2
	 * @return Concept change set
	 */
	private ConceptChangeSet sortOutChangeSet(Set<OWLClass> affected, WitnessPack lhs_spec, WitnessPack lhs_gen, WitnessPack rhs_spec, WitnessPack rhs_gen) {
		Set<RHSConceptChange> rhsConceptChanges = new HashSet<RHSConceptChange>();
		Set<LHSConceptChange> lhsConceptChanges = new HashSet<LHSConceptChange>();
		Set<ConceptChange> conceptChanges = new HashSet<ConceptChange>();
		
		for(OWLClass c : affected) {
			WitnessAxioms ls = new WitnessAxioms(lhs_spec.getDirectWitnesses(c), lhs_spec.getIndirectWitnesses(c));
			WitnessAxioms rs = new WitnessAxioms(rhs_spec.getDirectWitnesses(c), rhs_spec.getIndirectWitnesses(c));

			WitnessAxioms lg = new WitnessAxioms(lhs_gen.getDirectWitnesses(c), lhs_gen.getIndirectWitnesses(c));
			WitnessAxioms rg = new WitnessAxioms(rhs_gen.getDirectWitnesses(c), rhs_gen.getIndirectWitnesses(c));

			if(!ls.isEmpty() || !lg.isEmpty())
				lhsConceptChanges.add(new LHSConceptChange(c, ls, lg));

			if(!rs.isEmpty() || !rg.isEmpty())
				rhsConceptChanges.add(new RHSConceptChange(c, rs, rg));

			// Create overall change
			if(!ls.isEmpty() || !lg.isEmpty() || !rs.isEmpty() || !rg.isEmpty()) {
				Set<OWLAxiom> dirSpec = new HashSet<OWLAxiom>(ls.getDirectWitnesses());
				dirSpec.addAll(rs.getDirectWitnesses());

				Set<OWLAxiom> indirSpec = new HashSet<OWLAxiom>(ls.getIndirectWitnesses());
				indirSpec.addAll(rs.getIndirectWitnesses());

				Set<OWLAxiom> dirGen = new HashSet<OWLAxiom>(lg.getDirectWitnesses());
				dirGen.addAll(rg.getDirectWitnesses());

				Set<OWLAxiom> indirGen = new HashSet<OWLAxiom>(lg.getIndirectWitnesses());
				indirGen.addAll(rg.getIndirectWitnesses());

				ConceptChange change = new ConceptChange(c, dirSpec, indirSpec, dirGen, indirGen);
				conceptChanges.add(change);
			}
		}
		return new ConceptChangeSet(lhsConceptChanges, rhsConceptChanges, conceptChanges);
	}
	
	
	/**
	 * Add affected concept and given witnesses to the specified diff map
	 * @param affected	Set of affected concepts
	 * @param witnesses	Set of witnesses
	 * @param map	Map of new concepts to subconcepts
	 */
	protected void addChangeToMap(OWLClass affected, Set<OWLClassExpression> witnesses, Map<OWLClass,Set<OWLClassExpression>> map) {
		if(!witnesses.isEmpty())
			map.put(affected, witnesses);
	}


	/**
	 * Get the sets of (LHS and RHS) generalisation witnesses for the given concept
	 * @param subc	Concept
	 * @param map	Map of fresh concept names to the concepts they represent
	 * @param ont1reasoner	Ontology 1 reasoner
	 * @param ont2reasoner	Ontology 2 reasoner
	 * @return Generalisation concept witnesses for the given concept
	 */
	protected WitnessConcepts getGeneralisationWitnesses(OWLClass subc, Map<OWLClass,OWLClassExpression> map, 
			OWLReasoner ont1reasoner, OWLReasoner ont2reasoner) {
		Set<OWLClass> ind1 = ont1reasoner.getEquivalentClasses(subc).getEntitiesMinus(subc);
		Set<OWLClass> ind2 = ont2reasoner.getEquivalentClasses(subc).getEntitiesMinus(subc);

		ind1.addAll(ont1reasoner.getSubClasses(subc, false).getFlattened());
		ind2.addAll(ont2reasoner.getSubClasses(subc, false).getFlattened());

		// Remove bottom
		ind1.remove(df.getOWLNothing()); ind2.remove(df.getOWLNothing());
		
		if(!subc.isOWLThing())
			return getDifferentConcepts(ind1, ind2, map);
		else
			return null;
	}
	
	
	/**
	 * Get the sets of (LHS and RHS) specialisation witnesses for the given concept
	 * @param subc	Concept
	 * @param map	Map of fresh concept names to the concepts they represent
	 * @param ont1reasoner	Ontology 1 reasoner
	 * @param ont2reasoner	Ontology 2 reasoner
	 * @return Specialisation concept witnesses for the given concept
	 */
	protected WitnessConcepts getSpecialisationWitnesses(OWLClass subc, Map<OWLClass,OWLClassExpression> map, 
			OWLReasoner ont1reasoner, OWLReasoner ont2reasoner) {
		Set<OWLClass> ind1 = ont1reasoner.getEquivalentClasses(subc).getEntitiesMinus(subc);
		Set<OWLClass> ind2 = ont2reasoner.getEquivalentClasses(subc).getEntitiesMinus(subc);

		ind1.addAll(ont1reasoner.getSuperClasses(subc, false).getFlattened());
		ind2.addAll(ont2reasoner.getSuperClasses(subc, false).getFlattened());

		// Remove top
		ind1.remove(df.getOWLThing()); ind2.remove(df.getOWLThing());
		
		if(!subc.isOWLNothing())
			return getDifferentConcepts(ind1, ind2, map);
		else
			return null;
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
	 * @param topSuper	Superclasses of Top
	 * @param unsat	Unsatisfiable classes
	 * @return Pack of direct and indirect witnesses
	 */
	private WitnessPack getWitnesses(Map<OWLClass,Set<OWLClassExpression>> affectedConceptMap, OWLReasoner reasoner, boolean diffL,
			Set<OWLClass> topSuper, Set<OWLClass> unsat) {
		Map<OWLClassExpression,Set<OWLClass>> witMap = getWitnessMap(affectedConceptMap);
		Map<OWLClass,Set<OWLAxiom>> directWits = new HashMap<OWLClass,Set<OWLAxiom>>();
		Map<OWLClass,Set<OWLAxiom>> indirectWits = new HashMap<OWLClass,Set<OWLAxiom>>();
		
		for(OWLClassExpression ce : witMap.keySet()) {
			Set<OWLClass> subs = null;
			if(diffL) {
				if(!reasoner.isSatisfiable(ce) || reasoner.isEntailed(df.getOWLEquivalentClassesAxiom(ce, df.getOWLThing()))) {
					subs = reasoner.getEquivalentClasses(ce).getEntities();
					if(!ce.isAnonymous()) subs.remove((OWLClass)ce);
				}
				else {
					subs = reasoner.getSubClasses(ce, true).getFlattened();
					subs.removeAll(unsat); // Remove unsat classes
				}
			}
			else {
				if(!reasoner.isSatisfiable(ce) || reasoner.isEntailed(df.getOWLEquivalentClassesAxiom(ce, df.getOWLThing()))) {
					subs = reasoner.getEquivalentClasses(ce).getEntities();
					if(!ce.isAnonymous()) subs.remove((OWLClass)ce);
				}
				else {
					subs = reasoner.getSuperClasses(ce, true).getFlattened();
					subs.removeAll(unsat); // Remove unsat classes
					subs.removeAll(topSuper);
				}
			}
			
			for(OWLClass c : witMap.get(ce)) {
				if(subs.contains(c)) { // direct witness
					if(directWits.containsKey(c)) {
						Set<OWLAxiom> wits = directWits.get(c);
						if(diffL) wits.add(df.getOWLSubClassOfAxiom(c, ce));
						else wits.add(df.getOWLSubClassOfAxiom(ce, c));
						directWits.put(c, wits);
					}
					else {
						if(diffL) directWits.put(c, new HashSet<OWLAxiom>(Collections.singleton(df.getOWLSubClassOfAxiom(c, ce))));
						else directWits.put(c, new HashSet<OWLAxiom>(Collections.singleton(df.getOWLSubClassOfAxiom(ce, c))));
					}
				}
				else { // indirect witness
					if(indirectWits.containsKey(c)) {
						Set<OWLAxiom> wits = indirectWits.get(c);
						if(diffL) wits.add(df.getOWLSubClassOfAxiom(c, ce));
						else wits.add(df.getOWLSubClassOfAxiom(ce, c));
						indirectWits.put(c, wits);
					}
					else {
						if(diffL) indirectWits.put(c, new HashSet<OWLAxiom>(Collections.singleton(df.getOWLSubClassOfAxiom(c, ce))));
						else indirectWits.put(c, new HashSet<OWLAxiom>(Collections.singleton(df.getOWLSubClassOfAxiom(ce, c))));
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
		private double time;

		public Classifier(OWLOntology ont) {
			this.ont = ont;
		}

		@Override
		public void run() {
			reasoner = new ReasonerLoader(ont, false).createReasoner(false);
			long start = System.currentTimeMillis();
			reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
			long end = System.currentTimeMillis();
			time = (end-start)/1000.0;
		}

		public OWLReasoner getReasoner() {
			return reasoner;
		}

		public double getClassificationTime() {
			return time;
		}
	}

	
	/**
	 * Collect sub-concepts in both ontologies
	 * @return Set of subconcepts
	 */
	protected Set<OWLClassExpression> collectSCs() {
		if(verbose) System.out.print("   Extracting subconcepts from given ontologies... ");
		Set<OWLClassExpression> scs = new HashSet<OWLClassExpression>(); 
		getSubConcepts(ont1, scs);
		getSubConcepts(ont2, scs);
		if(verbose) System.out.println("done (nr. of subconcepts: " + scs.size() + ")");
		return scs;
	}


	/**
	 * Get sub-concepts of an ontology
	 * @param ont	Ontology
	 * @param sc	Set of subconcepts
	 * @return Updated set of subconcepts
	 */
	protected Set<OWLClassExpression> getSubConcepts(OWLOntology ont, Set<OWLClassExpression> sc) {
		Set<OWLLogicalAxiom> axs = ont.getLogicalAxioms();
		for(OWLAxiom ax : axs) {
			Set<OWLClassExpression> ax_sc = ax.getNestedClassExpressions();
			for(OWLClassExpression ce : ax_sc) {
				if(!sc.contains(ce) && !ce.isOWLThing() && !ce.isOWLNothing()) {
					if(ce.isAnonymous() && sigma.containsAll(ce.getClassesInSignature())) {
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
	private Map<OWLClass,OWLClassExpression> getSubConceptsMapping() {
		Set<OWLClassExpression> sc = collectSCs();
		OWLDataFactory df = OWLManager.getOWLDataFactory();
		Map<OWLClass,OWLClassExpression> map = new HashMap<OWLClass,OWLClassExpression>();
		int counter = 1;
		extraAxioms = new HashSet<OWLAxiom>();
		for(OWLClassExpression ce : sc) {
			if(ce.isAnonymous()) {
				OWLClass c = df.getOWLClass(IRI.create("diffSubc_" + counter));
				map.put(c, ce);
				OWLAxiom ax = df.getOWLEquivalentClassesAxiom(c, ce);
				extraAxioms.add(ax); counter++;
			}
		}
		ont1.getOWLOntologyManager().addAxioms(ont1, extraAxioms);
		ont2.getOWLOntologyManager().addAxioms(ont2, extraAxioms);
		return map;
	}
	

	/**
	 * Check whether two sets of objects contain the same elements
	 * @param set1	Set of OWL objects
	 * @param set2	Set of OWL objects
	 * @return true if sets have the same elements, false otherwise
	 */
	@SuppressWarnings("unused")
	private boolean equals(Set<? extends OWLObject> set1, Set<? extends OWLObject> set2) {
		boolean isEqual = true;
		for(OWLObject c : set1) {
			if(!set2.contains(c)) {
				isEqual = false;
				break;
			}
		}
		if(isEqual) {
			for(OWLObject c : set2) {
				if(!set1.contains(c)) {
					isEqual = false;
					break;
				}
			}
		}
		return isEqual;
	}
	
	
	/**
	 * Determine if the given pair of ontologies are logically equivalent
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 * @return true if ontologies are equivalent, false otherwise
	 */
	protected boolean equiv(OWLOntology ont1, OWLOntology ont2) {
		LogicalDiff diff = new LogicalDiff(ont1, ont2, false);
		return diff.isEquivalent();
	}
	
	
	/**
	 * Given two ontologies, inject entity declarations so that both ontologies
	 * end up with the same signature
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 */
	protected void equalizeSignatures(OWLOntology ont1, OWLOntology ont2) {
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
	 * Trigger the use of atomic concept diff
	 * @param atomicOnly	true if only atomic concepts should be taken into account, false otherwise
	 */
	public void setAtomicConceptDiff(boolean atomicOnly) {
		this.atomicOnly = atomicOnly;
	}
	
	
	/**
	 * Get the XML report for concept diff
	 * @return Concept diff report object
	 */
	public XMLConceptDiffReport getXMLReport() {
		return new XMLConceptDiffReport(changeSet);
	}
	

	/**
	 * Get a CSV change report
	 * @return Change report as a CSV document
	 */
	public String getCSVChangeReport() {
		return new CSVConceptDiffReport().getReport(changeSet);
	}
	
	
	/**
	 * Print diff results
	 */
	public void printDiff() {
		System.out.println("   Concept changes:");
		System.out.println("\t[ont1]" +
				"\tSpecialised: " + changeSet.getLHSSpecialisedConcepts().size() + 
				"\tGeneralised: " + changeSet.getLHSGeneralisedConcepts().size() +
				"\tTotal affected: " + changeSet.getLHSAffectedConcepts().size());
		System.out.println("\t[ont2]" +
				"\tSpecialised: " + changeSet.getRHSSpecialisedConcepts().size() + 
				"\tGeneralised: " + changeSet.getRHSGeneralisedConcepts().size() +
				"\tTotal affected: " + changeSet.getRHSAffectedConcepts().size());
		System.out.println("\t[total]" +
				"\tSpecialised: " + changeSet.getAllSpecialisedConcepts().size() + 
				"\tGeneralised: " + changeSet.getAllGeneralisedConcepts().size() +
				"\tTotal affected: " + changeSet.getAllAffectedConcepts().size());
		
		System.out.println("\n\tAffected concepts categorisation:");
		System.out.println("\t   Directly generalised: " + changeSet.getAllDirectlyGeneralised().size());
		System.out.println("\t   Directly specialised: " + changeSet.getAllDirectlySpecialised().size());
		System.out.println("\t   Purely directly generalised: " + changeSet.getAllPurelyDirectlyGeneralised().size());
		System.out.println("\t   Purely directly specialised: " + changeSet.getAllPurelyDirectlySpecialised().size());
		System.out.println("\t   Purely indirectly generalised: " + changeSet.getAllPurelyIndirectlyGeneralised().size());
		System.out.println("\t   Purely indirectly specialised: " + changeSet.getAllPurelyIndirectlySpecialised().size());
		System.out.println("\t   Mixed generalised: " + changeSet.getAllMixedGeneralised().size());
		System.out.println("\t   Mixed specialised: " + changeSet.getAllMixedSpecialised().size() + "\n");
	}
}