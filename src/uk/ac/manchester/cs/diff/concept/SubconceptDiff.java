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

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import uk.ac.manchester.cs.diff.concept.change.ConceptChange;
import uk.ac.manchester.cs.diff.concept.change.LHSConceptChange;
import uk.ac.manchester.cs.diff.concept.change.RHSConceptChange;
import uk.ac.manchester.cs.diff.concept.changeset.ConceptChangeSet;
import uk.ac.manchester.cs.diff.concept.changeset.WitnessAxioms;
import uk.ac.manchester.cs.diff.concept.changeset.WitnessConcepts;
import uk.ac.manchester.cs.diff.concept.changeset.WitnessPack;
import uk.ac.manchester.cs.diff.utils.ReasonerLoader;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxObjectRenderer;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class SubconceptDiff {
	private OWLOntology ont1, ont2;
	private OWLReasoner ont1reasoner, ont2reasoner;
	private OWLOntologyManager man;
	private OWLDataFactory df;
	private Map<OWLClass,Set<OWLClassExpression>> ont1_diffL, ont1_diffR, ont2_diffL, ont2_diffR;
	private Set<OWLClass> sig;
	private boolean verbose;

	/**
	 * Constructor
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 * @param verbose	Verbose mode
	 */
	public SubconceptDiff(OWLOntology ont1, OWLOntology ont2, boolean verbose) {
		this.ont1 = ont1;
		this.ont2 = ont2;
		this.verbose = verbose;
		man = OWLManager.createOWLOntologyManager();
		df = man.getOWLDataFactory();
		ont1_diffL = new HashMap<OWLClass,Set<OWLClassExpression>>();
		ont1_diffR = new HashMap<OWLClass,Set<OWLClassExpression>>();
		ont2_diffL = new HashMap<OWLClass,Set<OWLClassExpression>>();
		ont2_diffR = new HashMap<OWLClass,Set<OWLClassExpression>>();
		sig = new HashSet<OWLClass>(ont1.getClassesInSignature());
		sig.addAll(ont2.getClassesInSignature());
	}


	/**
	 * Constructor that takes pre-instantiated reasoners
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 * @param ont1reasoner	Ontology 1 reasoner instance
	 * @param ont2reasoner	Ontology 2 reasoner instance
	 * @param verbose	Verbose mode
	 */
	public SubconceptDiff(OWLOntology ont1, OWLOntology ont2, OWLReasoner ont1reasoner, OWLReasoner ont2reasoner, boolean verbose) {
		this.ont1 = ont1;
		this.ont2 = ont2;
		this.ont1reasoner = ont1reasoner;
		this.ont2reasoner = ont2reasoner;
		this.verbose = verbose;
		man = OWLManager.createOWLOntologyManager();
		df = man.getOWLDataFactory();
		ont1_diffL = new HashMap<OWLClass,Set<OWLClassExpression>>();
		ont1_diffR = new HashMap<OWLClass,Set<OWLClassExpression>>();
		ont2_diffL = new HashMap<OWLClass,Set<OWLClassExpression>>();
		ont2_diffR = new HashMap<OWLClass,Set<OWLClassExpression>>();
		sig = new HashSet<OWLClass>(ont1.getClassesInSignature());
		sig.addAll(ont2.getClassesInSignature());
	}


	/**
	 * Get the concept-based change set between the given ontologies 
	 * @param atomicOnly	true if only atomic subsumptions should be considered, false otherwise
	 * @return Concept-based change set
	 * @throws InterruptedException
	 */
	public ConceptChangeSet getDiff(boolean atomicOnly) throws InterruptedException {
		Map<OWLClass,OWLClassExpression> map = null;
		if(!atomicOnly) { 
			Set<OWLClassExpression> subcs = collectSCs();
			map = getSubConceptsMapping(subcs);
		}

		Set<RHSConceptChange> rhsConceptChanges = new HashSet<RHSConceptChange>();
		Set<LHSConceptChange> lhsConceptChanges = new HashSet<LHSConceptChange>();
		Set<ConceptChange> conceptChanges = new HashSet<ConceptChange>();
		Set<OWLClass> affected = new HashSet<OWLClass>();

		long start = System.currentTimeMillis();
		if(verbose) System.out.println("Classifying ontologies...");

		ExecutorService exec = Executors.newFixedThreadPool(2);
		Classifier ont1worker = new Classifier(ont1);
		Classifier ont2worker = new Classifier(ont2);
		exec.execute(ont1worker); exec.execute(ont2worker);
		exec.shutdown();
		exec.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		if(verbose) System.out.println("done (" + (System.currentTimeMillis()-start)/1000.0 + " secs)");

		ont1reasoner = ont1worker.getReasoner();
		ont2reasoner = ont2worker.getReasoner();

		if(verbose) System.out.print("Computing change witnesses... ");
		long start2 = System.currentTimeMillis();

		// Get specialisation and generalisation witnesses for each concept
		for(OWLClass subc : sig) {
			WitnessConcepts specWit = getSpecialisationWitnesses(subc, map);
			WitnessConcepts genWit = getGeneralisationWitnesses(subc, map);
			
			if(!specWit.isEmpty() || !genWit.isEmpty()) affected.add(subc);
			if(!specWit.getLHSWitnesses().isEmpty()) ont1_diffL.put(subc, specWit.getLHSWitnesses());
			if(!genWit.getLHSWitnesses().isEmpty()) ont1_diffR.put(subc, genWit.getLHSWitnesses());
			if(!specWit.getRHSWitnesses().isEmpty()) ont2_diffL.put(subc, specWit.getRHSWitnesses());
			if(!genWit.getRHSWitnesses().isEmpty()) ont2_diffR.put(subc, genWit.getRHSWitnesses());
		}
		long end2 = System.currentTimeMillis();
		if(verbose) System.out.println("done (" + (end2-start2)/1000.0 + " secs)");
		
		if(verbose) System.out.print("Splitting directly and indirectly affected concepts... ");
		long start3 = System.currentTimeMillis();

		WitnessPack lhs_spec = getWitnesses(ont1_diffL, ont1reasoner, true);
		WitnessPack lhs_gen = getWitnesses(ont1_diffR, ont1reasoner, false);

		WitnessPack rhs_spec = getWitnesses(ont2_diffL, ont2reasoner, true);
		WitnessPack rhs_gen = getWitnesses(ont2_diffR, ont2reasoner, false);

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
		long end = System.currentTimeMillis();
		if(verbose) System.out.println("done (" + (end-start3)/1000.0 + " secs)");
		
		ConceptChangeSet changeSet = new ConceptChangeSet(lhsConceptChanges, rhsConceptChanges, conceptChanges);
		printDiff(changeSet);
		System.out.println("finished (total diff time: " + (end-start)/1000.0 + " secs)");
		return changeSet;
	}

	
	/**
	 * Get the sets of (LHS and RHS) generalisation witnesses for the given concept
	 * @param subc	Concept
	 * @param map	Map of fresh concept names to the concepts they represent
	 * @return Generalisation concept witnesses for the given concept
	 */
	private WitnessConcepts getGeneralisationWitnesses(OWLClass subc, Map<OWLClass,OWLClassExpression> map) {
		// Get all subclasses
		Set<OWLClass> ont2_set = ont2reasoner.getSubClasses(subc, false).getFlattened();
		Set<OWLClass> ont1_set = ont1reasoner.getSubClasses(subc, false).getFlattened();

		// Remove subclasses of Bottom
		if(!subc.isOWLNothing()) {
			ont2_set.removeAll(ont2reasoner.getUnsatisfiableClasses().getEntities());
			ont1_set.removeAll(ont1reasoner.getUnsatisfiableClasses().getEntities());
		}
		return produceWitnessDiff(ont1_set, ont2_set, map);
	}
	
	
	/**
	 * Get the sets of (LHS and RHS) specialisation witnesses for the given concept
	 * @param subc	Concept
	 * @param map	Map of fresh concept names to the concepts they represent
	 * @return Specialisation concept witnesses for the given concept
	 */
	private WitnessConcepts getSpecialisationWitnesses(OWLClass subc, Map<OWLClass,OWLClassExpression> map) {
		// Get all superclasses
		Set<OWLClass> ont2_set = ont2reasoner.getSuperClasses(subc, false).getFlattened();
		Set<OWLClass> ont1_set = ont1reasoner.getSuperClasses(subc, false).getFlattened();

		// Remove superclasses of Top
		if(!subc.isOWLThing()) {
			ont2_set.removeAll(ont2reasoner.getSuperClasses(df.getOWLThing(), false).getFlattened());
			ont1_set.removeAll(ont1reasoner.getSuperClasses(df.getOWLThing(), false).getFlattened());
		}
		return produceWitnessDiff(ont1_set, ont2_set, map);
	}
	
	
	/**
	 * Get the witnesses for the the difference in the given sub or superclass sets
	 * @param set1	Set of classes
	 * @param set2	Set of classes
	 * @param map	Map of fresh concept names to concepts
	 * @return Witness concepts in the sub or superclass sets diff 
	 */
	private WitnessConcepts produceWitnessDiff(Set<OWLClass> set1, Set<OWLClass> set2, Map<OWLClass,OWLClassExpression> map) {
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
	private WitnessPack getWitnesses(Map<OWLClass,Set<OWLClassExpression>> affectedConceptMap, OWLReasoner reasoner, boolean diffL) {
		Map<OWLClassExpression,Set<OWLClass>> witMap = getWitnessMap(affectedConceptMap);
		Map<OWLClass, Set<OWLAxiom>> directWits = new HashMap<OWLClass,Set<OWLAxiom>>();
		Map<OWLClass, Set<OWLAxiom>> indirectWits = new HashMap<OWLClass,Set<OWLAxiom>>();
		for(OWLClassExpression ce : witMap.keySet()) {
			Set<OWLClass> subs = null;
			if(diffL) subs = reasoner.getSubClasses(ce, true).getFlattened();
			else subs = reasoner.getSuperClasses(ce, true).getFlattened();
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
			reasoner = new ReasonerLoader(ont, false).createFactReasoner();
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
	 * Print diff
	 * @param changeSet	Concept-based change set
	 */
	public void printDiff(ConceptChangeSet changeSet) {
		System.out.println("\nSubconcept diff results:");
		System.out.println("  [ont1]   Specialised: " + changeSet.getLHSSpecialisedConcepts().size() + 
				"\tGeneralised: " + changeSet.getLHSGeneralisedConcepts().size() +
				"\tTotal affected: " + changeSet.getLHSAffectedConcepts().size());
		System.out.println("  [ont2]   Specialised: " + changeSet.getRHSSpecialisedConcepts().size() + 
				"\tGeneralised: " + changeSet.getRHSGeneralisedConcepts().size() +
				"\tTotal affected: " + changeSet.getRHSAffectedConcepts().size());
		System.out.println("  [total]  Specialised: " + changeSet.getAllSpecialisedConcepts().size() + 
				"\tGeneralised: " + changeSet.getAllGeneralisedConcepts().size() +
				"\tTotal affected: " + changeSet.getAllAffectedConcepts().size());
		
		System.out.println("\n  Overall affected concepts categorisation:");
		System.out.println("    Direct Generalised: " + changeSet.getAllDirectlyGeneralised().size());
		System.out.println("    Direct Specialised: " + changeSet.getAllDirectlySpecialised().size());
		System.out.println("    Purely directly generalised: " + changeSet.getAllPurelyDirectlyGeneralised().size());
		System.out.println("    Purely directly specialised: " + changeSet.getAllPurelyDirectlySpecialised().size());
		System.out.println("    Purely indirectly generalised: " + changeSet.getAllPurelyIndirectlyGeneralised().size());
		System.out.println("    Purely indirectly specialised: " + changeSet.getAllPurelyIndirectlySpecialised().size());
		System.out.println("    Mixed generalised: " + changeSet.getAllMixedGeneralised().size());
		System.out.println("    Mixed specialised: " + changeSet.getAllMixedSpecialised().size());

		print("Purely directly generalised", changeSet.getAllPurelyDirectlyGeneralised());
		print("Purely directly specialised", changeSet.getAllPurelyDirectlySpecialised());
		print("Purely indirectly generalised", changeSet.getAllPurelyIndirectlyGeneralised());
		print("Purely indirectly specialised", changeSet.getAllPurelyIndirectlySpecialised());
		print("Mixed generalised", changeSet.getAllMixedGeneralised());		
		print("Mixed specialised", changeSet.getAllMixedSpecialised());
		print("Specialisation witnesses", changeSet.getSpecialisationWitnesses());
		print("Generalisation witnesses", changeSet.getGeneralisationWitnesses());
	}


	/**
	 * Collect sub-concepts in both ontologies
	 * @return Set of subconcepts
	 */
	private Set<OWLClassExpression> collectSCs() {
		if(verbose) System.out.print("Getting subconcepts of O1 and O2... ");
		Set<OWLClassExpression> scs = new HashSet<OWLClassExpression>(); 
		getSubConcepts(ont1, scs);
		getSubConcepts(ont2, scs);
		if(verbose) System.out.println("done. Nr. of subconcepts: " + scs.size());
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
						sc.add(ce);
						getSubConcepts(ce, sc);
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
	 * @param sc	Set of subconcepts
	 * @return Map of new terms to subconcepts
	 */
	private Map<OWLClass,OWLClassExpression> getSubConceptsMapping(Set<OWLClassExpression> sc) {
		Map<OWLClass,OWLClassExpression> map = new HashMap<OWLClass,OWLClassExpression>();
		int counter = 1;
		for(OWLClassExpression ce : sc) {
			if(ce.isAnonymous()) {
				OWLClass c = df.getOWLClass(IRI.create("diffSubc_" + counter));
				map.put(c, ce);
				OWLEquivalentClassesAxiom ax = df.getOWLEquivalentClassesAxiom(c, ce);
				AddAxiom add1 = new AddAxiom(ont1, ax);
				AddAxiom add2 = new AddAxiom(ont2, ax);
				man.applyChange(add1);
				man.applyChange(add2);
				counter++;
			}
		}
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

	
	
	/* Testing-purposes-only methods */
	private void print(String desc, Set<? extends OWLObject> obj) {
		try {
			FileWriter out = new FileWriter("/Users/rafa/Desktop/ncit_05.07/" + desc + ".txt");
			SimpleShortFormProvider fp = new SimpleShortFormProvider();
			for(OWLObject o : obj) {
				out.append(getManchesterSyntax(o, fp) + "\n");
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getManchesterSyntax(OWLObject obj, SimpleShortFormProvider fp) {
		StringWriter wr = new StringWriter();

		ManchesterOWLSyntaxObjectRenderer render = new ManchesterOWLSyntaxObjectRenderer(wr, fp);
		render.setUseWrapping(false);
		obj.accept(render);

		String str = wr.getBuffer().toString();

		return str;
	}
}