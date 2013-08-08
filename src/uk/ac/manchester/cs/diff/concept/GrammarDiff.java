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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class GrammarDiff extends SubconceptDiff {
	private FaCTPlusPlusReasoner ont1modExtractor, ont2modExtractor;
	private OWLOntologyManager man;

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
		ont1modExtractor = new FaCTPlusPlusReasoner(ont1, new SimpleConfiguration(), BufferingMode.NON_BUFFERING);
		ont2modExtractor = new FaCTPlusPlusReasoner(ont2, new SimpleConfiguration(), BufferingMode.NON_BUFFERING);
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
		ont1modExtractor = new FaCTPlusPlusReasoner(ont1, new SimpleConfiguration(), BufferingMode.NON_BUFFERING);
		ont2modExtractor = new FaCTPlusPlusReasoner(ont2, new SimpleConfiguration(), BufferingMode.NON_BUFFERING);
	}

	
	/* Note: FPP module extractor: 0 - bot modules, 1 - top modules, 2 - star modules */

	/**
	 * Get the concept-based change set between the given ontologies 
	 * @return Concept-based change set
	 * @throws InterruptedException
	 * @throws OWLOntologyCreationException 
	 */
	public ConceptChangeSet getDiff() throws InterruptedException, OWLOntologyCreationException {
		long start = System.currentTimeMillis();

		Set<OWLObjectProperty> roles = new HashSet<OWLObjectProperty>();
		roles.addAll(ont1.getObjectPropertiesInSignature());
		roles.addAll(ont2.getObjectPropertiesInSignature());
		
		// Get specialisation and generalisation witnesses for each concept
		Set<OWLClass> specialised = computeSpecialisations(roles);
		Set<OWLClass> generalised = computeGeneralisations(roles);
		
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
	 */
	private Set<OWLClass> computeSpecialisations(Set<OWLObjectProperty> roles) throws OWLOntologyCreationException {
		if(verbose) System.out.print("Computing specialisations... ");
		Set<OWLClass> affected = new HashSet<OWLClass>();
		long start = System.currentTimeMillis();

		// Get specialisation witnesses for each concept
		for(OWLClass subc : sig) {
			if(!subc.isOWLNothing()) {
				System.out.println("\n-------------------------\nChecking concept: " + subc);
				Set<OWLEntity> modsig = new HashSet<OWLEntity>();
				modsig.add(subc); modsig.addAll(roles);
				OWLOntology mod_ont1 = man.createOntology(ont1modExtractor.getModule(modsig, false, 0));
				OWLOntology mod_ont2 = man.createOntology(ont2modExtractor.getModule(modsig, false, 0));
				System.out.println("\tModule1: " + mod_ont1.getLogicalAxiomCount() + "\n\tModule2: " + mod_ont2.getLogicalAxiomCount());
				
				if(!equiv(mod_ont1, mod_ont2)) {
					Map<OWLClass,OWLClassExpression> map = getSubConceptsMapping(mod_ont1, mod_ont2, "L");

					OWLReasoner ont1reasoner = new ReasonerLoader(mod_ont1).createReasoner();
					OWLReasoner ont2reasoner = new ReasonerLoader(mod_ont2).createReasoner();
					System.out.println("\tModule1': " + mod_ont1.getLogicalAxiomCount() + 
							"\n\tModule2': " + mod_ont2.getLogicalAxiomCount());
					Set<OWLClass> topSuper1 = ont1reasoner.getSuperClasses(df.getOWLThing(), false).getFlattened();
					Set<OWLClass> topSuper2 = ont2reasoner.getSuperClasses(df.getOWLThing(), false).getFlattened();

					WitnessConcepts specWit = getSpecialisationWitnesses(subc, map, topSuper1, topSuper2, ont1reasoner, ont2reasoner);

					if(!specWit.isEmpty()) affected.add(subc);
					addChangeToMap(subc, specWit.getLHSWitnesses(), ont1_diffL);
					addChangeToMap(subc, specWit.getRHSWitnesses(), ont2_diffL);
				}
				else 
					System.out.println("\tEquivalent!");
			}
		}
		long end = System.currentTimeMillis();
		if(verbose) System.out.println("done (" + (end-start)/1000.0 + " secs)");
		return affected;
	}
	
	
	/**
	 * Compute generalisation witnesses between the given ontologies
	 * @param roles	Set of role names
	 * @return Set of affected concept names
	 * @throws OWLOntologyCreationException 
	 */
	private Set<OWLClass> computeGeneralisations(Set<OWLObjectProperty> roles) throws OWLOntologyCreationException {
		if(verbose) System.out.print("Computing generalisations... ");
		Set<OWLClass> affected = new HashSet<OWLClass>();
		long start = System.currentTimeMillis();

		// Get specialisation witnesses for each concept
		for(OWLClass subc : sig) {
			if(!subc.isOWLThing()) {
				Set<OWLEntity> modsig = new HashSet<OWLEntity>();
				modsig.add(subc); modsig.addAll(roles);
				OWLOntology mod_ont1 = man.createOntology(ont1modExtractor.getModule(modsig, false, 1));
				OWLOntology mod_ont2 = man.createOntology(ont2modExtractor.getModule(modsig, false, 1));

				if(!equiv(mod_ont1, mod_ont2)) {
					Map<OWLClass,OWLClassExpression> map = getSubConceptsMapping(mod_ont1, mod_ont2, "R");

					OWLReasoner ont1reasoner = new ReasonerLoader(mod_ont1).createReasoner();
					OWLReasoner ont2reasoner = new ReasonerLoader(mod_ont2).createReasoner();

					Set<OWLClass> botSub1 = ont1reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
					Set<OWLClass> botSub2 = ont2reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();

					WitnessConcepts genWit = getGeneralisationWitnesses(subc, map, botSub1, botSub2, ont1reasoner, ont2reasoner);

					if(!genWit.isEmpty()) affected.add(subc);
					addChangeToMap(subc, genWit.getLHSWitnesses(), ont1_diffR);
					addChangeToMap(subc, genWit.getRHSWitnesses(), ont2_diffR);
				}
			}
		}
		long end = System.currentTimeMillis();
		if(verbose) System.out.println("done (" + (end-start)/1000.0 + " secs)");
		return affected;
	}


	/**
	 * Create a mapping between a new term "TempX" and each sub-concept, and add the appropriate
	 * equivalence axioms to each ontology
	 * @return Map of new terms to subconcepts
	 */
	private Map<OWLClass,OWLClassExpression> getSubConceptsMapping(OWLOntology mod_ont1, OWLOntology mod_ont2, String diff) {
		Map<OWLClass,OWLClassExpression> map = new HashMap<OWLClass,OWLClassExpression>();
		
		Set<OWLObjectProperty> roles = new HashSet<OWLObjectProperty>();
		roles.addAll(mod_ont1.getObjectPropertiesInSignature());
		roles.addAll(mod_ont2.getObjectPropertiesInSignature());
		
		// Collect possible witnesses
		Set<OWLClassExpression> scs = collectSCs(mod_ont1, mod_ont2);
		
		Set<OWLClassExpression> wits = new HashSet<OWLClassExpression>();
		wits.addAll(scs);
		wits.addAll(getExistentialWitnesses(scs, roles));
		wits.addAll(getUniversalWitnesses(scs, roles));
		wits.addAll(getNegationWitnesses(scs));
//		System.out.println("Total possible witnesses: " + wits.size());
		
		int counter = 1;
		Set<OWLAxiom> extraAxioms = new HashSet<OWLAxiom>();
		for(OWLClassExpression ce : wits) {
			OWLClass c = df.getOWLClass(IRI.create("diffSubc_" + counter));
			map.put(c, ce);
			OWLAxiom ax = null;
			if(diff.equals("L"))
				ax = df.getOWLSubClassOfAxiom(c, ce);
			else if (diff.equals("R"))
				ax = df.getOWLSubClassOfAxiom(ce, c);
			extraAxioms.add(ax); counter++;
		}
		mod_ont1.getOWLOntologyManager().addAxioms(mod_ont1, extraAxioms);
		mod_ont2.getOWLOntologyManager().addAxioms(mod_ont2, extraAxioms);
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