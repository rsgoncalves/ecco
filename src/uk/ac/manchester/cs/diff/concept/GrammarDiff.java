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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import uk.ac.manchester.cs.diff.axiom.LogicalDiff;
import uk.ac.manchester.cs.diff.concept.changeset.ConceptChangeSet;
import uk.ac.manchester.cs.diff.concept.changeset.WitnessConcepts;
import uk.ac.manchester.cs.diff.utils.ReasonerLoader;
import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasoner;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class GrammarDiff extends SubconceptDiff {
	private FaCTPlusPlusReasoner ont1modExtractor, ont2modExtractor;
	private OWLOntologyManager man;
	private final boolean debug = true;

	/**
	 * Constructor
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 * @param outputDir	Output directory
	 * @param verbose	Verbose mode
	 */
	public GrammarDiff(OWLOntology ont1, OWLOntology ont2, String outputDir, boolean verbose) {
		super(ont1, ont2, outputDir, verbose);
		man = OWLManager.createOWLOntologyManager();
	}

	
	/**
	 * Constructor that takes a signature
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 * @param sig	Signature (set of concept names)
	 * @param outputDir	Output directory
	 * @param verbose	Verbose mode
	 */
	public GrammarDiff(OWLOntology ont1, OWLOntology ont2, Set<OWLClass> sig, String outputDir, boolean verbose) {
		super(ont1, ont2, sig, outputDir, verbose);
		man = OWLManager.createOWLOntologyManager();
	}


	/**
	 * Get the concept-based change set between the given ontologies 
	 * @return Concept-based change set
	 * @throws InterruptedException
	 * @throws OWLOntologyCreationException 
	 */
	public ConceptChangeSet getDiff() throws InterruptedException, OWLOntologyCreationException {
		long start = System.currentTimeMillis();
		if(verbose) System.out.println("Input signature: sigma contains " + sig.size() + " concept names");
		
		Set<OWLObjectProperty> roles = new HashSet<OWLObjectProperty>(ont1.getObjectPropertiesInSignature());
		roles.addAll(ont2.getObjectPropertiesInSignature());
		
		if(sig.size() < ont1.getClassesInSignature().size() &&
				sig.size() < ont2.getClassesInSignature().size()) {
			Set<OWLEntity> modsig = new HashSet<OWLEntity>(sig);
			modsig.addAll(roles);
			
			ont1 = man.createOntology(new SyntacticLocalityModuleExtractor(ont1.getOWLOntologyManager(), ont1, ModuleType.STAR).extract(modsig));
			ont2 = man.createOntology(new SyntacticLocalityModuleExtractor(ont2.getOWLOntologyManager(), ont2, ModuleType.STAR).extract(modsig));
			
			modsig.clear();
			if(debug) System.out.println(" mod(sigma)1 size: " + ont1.getLogicalAxiomCount() + " axioms\n" +
					" mod(sigma)2 size: " + ont2.getLogicalAxiomCount() + " axioms");
		}
		roles.clear();
		
		// Initialise FaCT++ based module extractors. Note: 0 - bot modules, 1 - top modules, 2 - star modules
		ont1modExtractor = new FaCTPlusPlusReasoner(ont1, new SimpleConfiguration(), BufferingMode.BUFFERING);
		ont2modExtractor = new FaCTPlusPlusReasoner(ont2, new SimpleConfiguration(), BufferingMode.BUFFERING);
		
		
		Set<OWLObjectProperty> mod_roles = new HashSet<OWLObjectProperty>(ont1.getObjectPropertiesInSignature());
		roles.addAll(ont2.getObjectPropertiesInSignature());
		
		// Get specialisation and generalisation witnesses for each concept
		Set<OWLClass> specialised = computeSpecialisations(mod_roles);
		Set<OWLClass> generalised = computeGeneralisations(mod_roles);
		
		Set<OWLClass> affected = new HashSet<OWLClass>();
		affected.addAll(specialised);
		affected.addAll(generalised);
		
		OWLReasoner ont1reasoner = new ReasonerLoader(ont1).createFactReasoner();
		OWLReasoner ont2reasoner = new ReasonerLoader(ont2).createFactReasoner();
		
		ConceptChangeSet changeSet = splitDirectIndirectChanges(affected, ont1reasoner, ont2reasoner);
		long end = System.currentTimeMillis();
		printDiff(changeSet);
		System.out.println("finished (total diff time: " + (end-start)/1000.0 + " secs)");
		return changeSet;
	}


	
	/**
	 * Compute specialisation witnesses between the given ontologies
	 * @param roles	Set of role names
	 * @return Set of affected concept names
	 * @throws OWLOntologyCreationException 
	 * @throws InterruptedException 
	 */
	private Set<OWLClass> computeSpecialisations(Set<OWLObjectProperty> roles) throws OWLOntologyCreationException, InterruptedException {
		if(verbose) System.out.print("Computing specialisations... ");
		Set<OWLClass> affected = new HashSet<OWLClass>();
		long start = System.currentTimeMillis();

		if(debug) System.out.println("\n--------------------------------\nDIFF L\n--------------------------------");
		int counter = 1;
		// Get specialisation witnesses for each concept
		for(OWLClass subc : sig) {
			if(!subc.isOWLNothing()) {
				if(debug) System.out.println("   Checking concept " + counter + "/" + sig.size() + ": " + getManchesterRendering(subc));
				
				Set<OWLEntity> modsig = new HashSet<OWLEntity>();
				modsig.add(subc); modsig.addAll(roles);
				OWLOntology mod_ont1 = man.createOntology(ont1modExtractor.getModule(modsig, false, 0));
				OWLOntology mod_ont2 = man.createOntology(ont2modExtractor.getModule(modsig, false, 0));
				
				if(debug) System.out.println("\t|mod1| = " + mod_ont1.getLogicalAxiomCount() + "\t|mod2| = " + mod_ont2.getLogicalAxiomCount());
				
				if(!equiv(mod_ont1, mod_ont2)) {
					ExecutorService exec1 = Executors.newFixedThreadPool(2);
					Classifier mod1worker = new Classifier(mod_ont1);
					Classifier mod2worker = new Classifier(mod_ont2);
					exec1.execute(mod1worker); exec1.execute(mod2worker);
					exec1.shutdown(); exec1.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
					OWLReasoner mod1reasoner = mod1worker.getReasoner();
					OWLReasoner mod2reasoner = mod2worker.getReasoner();
					
					Map<OWLClass,OWLClassExpression> map = getSubConceptsMapping(subc, mod1reasoner, mod2reasoner, mod_ont1, mod_ont2, "L");
					if(debug) System.out.println("\t|mod1'| = " + mod_ont1.getLogicalAxiomCount() + "\t|mod2'| = " + mod_ont2.getLogicalAxiomCount());
					
					// Classify modules
					if(debug) System.out.print("\tClassifying modules... ");
					long start2 = System.currentTimeMillis();
					ExecutorService exec = Executors.newFixedThreadPool(2);
					
					Classifier ont1worker = new Classifier(mod_ont1);
					Classifier ont2worker = new Classifier(mod_ont2);
					
					exec.execute(ont1worker); exec.execute(ont2worker);
					exec.shutdown();
					exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
					OWLReasoner ont1reasoner = ont1worker.getReasoner();
					OWLReasoner ont2reasoner = ont2worker.getReasoner();
					if(debug) System.out.println("done ("+ (System.currentTimeMillis()-start2)/1000.0 + " secs)");
					
					Set<OWLClass> topSuper1 = ont1reasoner.getSuperClasses(df.getOWLThing(), false).getFlattened();
					Set<OWLClass> topSuper2 = ont2reasoner.getSuperClasses(df.getOWLThing(), false).getFlattened();

					WitnessConcepts specWit = getSpecialisationWitnesses(subc, map, topSuper1, topSuper2, ont1reasoner, ont2reasoner);

					if(!specWit.isEmpty()) {
						affected.add(subc);
						if(debug) System.out.println("   Specialised!");
					} else if(debug) 
						System.out.println("   Unaffected!");
					
					addChangeToMap(subc, specWit.getLHSWitnesses(), ont1_diffL);
					addChangeToMap(subc, specWit.getRHSWitnesses(), ont2_diffL);
					
					ont1reasoner.dispose(); ont1reasoner = null;
					ont2reasoner.dispose(); ont2reasoner = null;
					map.clear(); map = null;
					topSuper1.clear(); topSuper1 = null;
					topSuper2.clear(); topSuper2 = null;
				}
				else if(debug) 
					System.out.println("   Equivalent!");
				
				mod_ont1.getOWLOntologyManager().removeOntology(mod_ont1);
				mod_ont2.getOWLOntologyManager().removeOntology(mod_ont2);
				modsig.clear(); modsig = null;
				if(debug) System.out.println("   -------------------------");
			}
			counter++;
		}
		long end = System.currentTimeMillis();
		if(verbose) System.out.println("done (" + (end-start)/1000.0 + " secs). Nr. of specialised concept names: " + affected.size() + "\n");
		return affected;
	}
	
	
	/**
	 * Compute generalisation witnesses between the given ontologies
	 * @param roles	Set of role names
	 * @return Set of affected concept names
	 * @throws OWLOntologyCreationException 
	 * @throws InterruptedException 
	 */
	private Set<OWLClass> computeGeneralisations(Set<OWLObjectProperty> roles) throws OWLOntologyCreationException, InterruptedException {
		if(verbose) System.out.print("Computing generalisations... ");
		Set<OWLClass> affected = new HashSet<OWLClass>();
		long start = System.currentTimeMillis();

		if(debug) System.out.println("\n--------------------------------\nDIFF R\n--------------------------------");
		int counter = 1;
		// Get specialisation witnesses for each concept
		for(OWLClass subc : sig) {
			if(!subc.isOWLThing()) {
				if(debug) System.out.println("   Checking concept " + counter + "/" + sig.size() + ": " + getManchesterRendering(subc));
				
				Set<OWLEntity> modsig = new HashSet<OWLEntity>();
				modsig.add(subc); modsig.addAll(roles);
				OWLOntology mod_ont1 = man.createOntology(ont1modExtractor.getModule(modsig, false, 1));
				OWLOntology mod_ont2 = man.createOntology(ont2modExtractor.getModule(modsig, false, 1));
				
				if(debug) System.out.println("\t|mod1| = " + mod_ont1.getLogicalAxiomCount() + "\t|mod2| = " + mod_ont2.getLogicalAxiomCount());
				
				if(!equiv(mod_ont1, mod_ont2)) {
					ExecutorService exec1 = Executors.newFixedThreadPool(2);
					Classifier mod1worker = new Classifier(mod_ont1);
					Classifier mod2worker = new Classifier(mod_ont2);
					exec1.execute(mod1worker); exec1.execute(mod2worker);
					exec1.shutdown(); exec1.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
					OWLReasoner mod1reasoner = mod1worker.getReasoner();
					OWLReasoner mod2reasoner = mod2worker.getReasoner();
					
					Map<OWLClass,OWLClassExpression> map = getSubConceptsMapping(subc, mod1reasoner, mod2reasoner, mod_ont1, mod_ont2, "R");
					if(debug) System.out.println("\t|mod1'| = " + mod_ont1.getLogicalAxiomCount() + "\t|mod2'| = " + mod_ont2.getLogicalAxiomCount());
					
					// Classify modules
					if(debug) System.out.print("\tClassifying modules... ");
					long start2 = System.currentTimeMillis();
					ExecutorService exec = Executors.newFixedThreadPool(2);
					
					Classifier ont1worker = new Classifier(mod_ont1);
					Classifier ont2worker = new Classifier(mod_ont2);
					
					exec.execute(ont1worker); exec.execute(ont2worker);
					exec.shutdown();
					exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
					OWLReasoner ont1reasoner = ont1worker.getReasoner();
					OWLReasoner ont2reasoner = ont2worker.getReasoner();
					
					if(debug) System.out.println("done ("+ (System.currentTimeMillis()-start2)/1000.0 + " secs)");
		
					Set<OWLClass> botSub1 = ont1reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
					Set<OWLClass> botSub2 = ont2reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();

					WitnessConcepts genWit = getGeneralisationWitnesses(subc, map, botSub1, botSub2, ont1reasoner, ont2reasoner);

					if(!genWit.isEmpty()) {
						affected.add(subc);
						if(debug) System.out.println("   Generalised!");
					} else if(debug) 
						System.out.println("   Unaffected!");
				
					addChangeToMap(subc, genWit.getLHSWitnesses(), ont1_diffR);
					addChangeToMap(subc, genWit.getRHSWitnesses(), ont2_diffR);
					
					ont1reasoner.dispose(); ont1reasoner = null;
					ont2reasoner.dispose(); ont2reasoner = null;
					map.clear(); map = null;
					botSub1.clear(); botSub1 = null;
					botSub2.clear(); botSub2 = null;
				}
				else if(debug) 
					System.out.println("   Equivalent!");
				
				mod_ont1.getOWLOntologyManager().removeOntology(mod_ont1);
				mod_ont2.getOWLOntologyManager().removeOntology(mod_ont2);
				modsig.clear(); modsig = null;
				if(debug) System.out.println("   -------------------------");
			}
			counter++;
		}
		long end = System.currentTimeMillis();
		if(verbose) System.out.println("done (" + (end-start)/1000.0 + " secs). Nr. of generalised concept names: " + affected.size() + "\n");
		return affected;
	}


	/**
	 * Create a mapping between a new term "TempX" and each sub-concept, and add the appropriate
	 * equivalence axioms to each ontology
	 * @return Map of new terms to subconcepts
	 */
	private Map<OWLClass,OWLClassExpression> getSubConceptsMapping(OWLClass subc, OWLReasoner mod1reasoner, OWLReasoner mod2reasoner, OWLOntology mod_ont1, OWLOntology mod_ont2, String diff) {
		Map<OWLClass,OWLClassExpression> map = new HashMap<OWLClass,OWLClassExpression>();
		
		Set<OWLObjectProperty> roles = new HashSet<OWLObjectProperty>();
		roles.addAll(mod_ont1.getObjectPropertiesInSignature());
		roles.addAll(mod_ont2.getObjectPropertiesInSignature());
		
		Set<OWLClassExpression> scs = collectSCs(mod_ont1, mod_ont2);
		
		Set<OWLClassExpression> wits = new HashSet<OWLClassExpression>();
		wits.addAll(scs);
		wits.addAll(getExistentialWitnesses(scs, roles));
		wits.addAll(getUniversalWitnesses(scs, roles));
		wits.addAll(getNegationWitnesses(scs));
		if(diff.equals("L"))
			wits.addAll(getConjunctionWitnesses(subc, mod1reasoner, mod2reasoner, scs));
		else if(diff.equals("R"))
			wits.addAll(getDisjunctionWitnesses(subc, mod1reasoner, mod2reasoner, scs));
		
		int counter = 1;
		Set<OWLAxiom> extraAxioms = new HashSet<OWLAxiom>();
		for(OWLClassExpression ce : wits) {
			OWLClass c = df.getOWLClass(IRI.create("diffSubc_" + counter));
			map.put(c, ce);
			OWLAxiom ax = null;
			if(diff.equals("R"))
				ax = df.getOWLSubClassOfAxiom(c, ce);
			else if (diff.equals("L") || diff.equals("E"))
				ax = df.getOWLEquivalentClassesAxiom(c, ce);
			
			if(ax!=null) extraAxioms.add(ax); 
			counter++;
		}
		man.addAxioms(mod_ont1, extraAxioms);
		man.addAxioms(mod_ont2, extraAxioms);
		return map;
	}
	
	
	/**
	 * Collect sub-concepts in both ontologies
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 * @return Set of subconcepts
	 */
	private Set<OWLClassExpression> collectSCs(OWLOntology ont1, OWLOntology ont2) {
		Set<OWLClassExpression> scs = new HashSet<OWLClassExpression>(); 
		getSubConcepts(ont1, scs);
		getSubConcepts(ont2, scs);
		return scs;
	}
	
	
	/**
	 * Get the set of possible negation witnesses
	 * @param sc	Set of subconcepts
	 * @return Set of negation witnesses
	 */
	private Set<OWLClassExpression> getNegationWitnesses(Set<OWLClassExpression> sc) {
		Set<OWLClassExpression> out = new HashSet<OWLClassExpression>();
		for(OWLClassExpression c : sc)
			out.add(df.getOWLObjectComplementOf(c));
		return out;
	}
	
	
	/**
	 * Get the set of possible existential witnesses
	 * @param sc	Set of subconcepts
	 * @param roles	Set of role names
	 * @return Set of existential witnesses
	 */
	private Set<OWLClassExpression> getExistentialWitnesses(Set<OWLClassExpression> sc, Set<OWLObjectProperty> roles) {
		Set<OWLClassExpression> out = new HashSet<OWLClassExpression>();
		for(OWLClassExpression c : sc) {
			for(OWLObjectProperty r : roles)
				out.add(df.getOWLObjectSomeValuesFrom(r, c));
		}
		return out;
	}
	
	
	/**
	 * Get the set of possible universal witnesses
	 * @param sc	Set of subconcepts
	 * @param roles	Set of role names
	 * @return Set of universal witnesses
	 */
	private Set<OWLClassExpression> getUniversalWitnesses(Set<OWLClassExpression> sc, Set<OWLObjectProperty> roles) {
		Set<OWLClassExpression> out = new HashSet<OWLClassExpression>();
		for(OWLClassExpression c : sc) {
			for(OWLObjectProperty r : roles)
				out.add(df.getOWLObjectAllValuesFrom(r, c));
		}
		return out;
	}
	
	
	/**
	 * Get the set of conjunction witnesses for a given concept
	 * @param c	Concept being verified
	 * @param mod1reasoner	Reasoner instance of module for ontology 1
	 * @param mod2reasoner	Reasoner instance of module for ontology 2
	 * @param sc	Set of subconcepts within the modules
	 * @return Set of conjunction witnesses
	 */
	private Set<OWLClassExpression> getConjunctionWitnesses(OWLClass c, OWLReasoner mod1reasoner, OWLReasoner mod2reasoner, Set<OWLClassExpression> sc) {
		Set<OWLClassExpression> out = new HashSet<OWLClassExpression>();
		List<OWLClassExpression> pool = new ArrayList<OWLClassExpression>();
		for(OWLClassExpression ce : sc) {
			if(!ce.equals(c)) {
				if(!mod1reasoner.isEntailed(df.getOWLSubClassOfAxiom(c, ce)) && !mod2reasoner.isEntailed(df.getOWLSubClassOfAxiom(c, ce)))
					pool.add(ce);
			}
		}
		for(int i = 0; i < pool.size(); i++) {
			for(int j = i+1; j < pool.size(); j++)
				out.add(df.getOWLObjectIntersectionOf(pool.get(i), pool.get(j)));
		}
		return out;
	}
	
	
	/**
	 * Get the set of disjunction witnesses for a given concept
	 * @param c	Concept being verified
	 * @param mod1reasoner	Reasoner instance of module for ontology 1
	 * @param mod2reasoner	Reasoner instance of module for ontology 2
	 * @param sc	Set of subconcepts within the modules
	 * @return Set of disjunction witnesses
	 */
	private Set<OWLClassExpression> getDisjunctionWitnesses(OWLClass c, OWLReasoner mod1reasoner, OWLReasoner mod2reasoner, Set<OWLClassExpression> sc) {
		Set<OWLClassExpression> out = new HashSet<OWLClassExpression>();
		List<OWLClassExpression> pool = new ArrayList<OWLClassExpression>();
		for(OWLClassExpression ce : sc) {
			if(!ce.equals(c)) {
				if(!mod1reasoner.isEntailed(df.getOWLSubClassOfAxiom(ce, c)) && !mod2reasoner.isEntailed(df.getOWLSubClassOfAxiom(ce, c)))
					pool.add(ce);
			}
		}
		for(int i = 0; i < pool.size(); i++) {
			for(int j = i+1; j < pool.size(); j++)
				out.add(df.getOWLObjectUnionOf(pool.get(i), pool.get(j)));
		}
		return out;
	}
	
	
	/**
	 * Determine if the given pair of ontologies are logically equivalent
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 * @return true if ontologies are equivalent, false otherwise
	 */
	private boolean equiv(OWLOntology ont1, OWLOntology ont2) {
		LogicalDiff diff = new LogicalDiff(ont1, ont2, false);
		return diff.isEquivalent();
	}
}