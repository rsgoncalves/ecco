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
package uk.ac.manchester.cs.diff.concept.changeset;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;

import uk.ac.manchester.cs.diff.concept.change.ConceptChange;
import uk.ac.manchester.cs.diff.concept.change.LHSConceptChange;
import uk.ac.manchester.cs.diff.concept.change.RHSConceptChange;

/**
 * @author Rafael S. Goncalves <br>
 * Information Management Group (IMG) <br>
 * School of Computer Science <br>
 * University of Manchester <br>
 */
public class ConceptChangeSet {
	private Set<ConceptChange> allChanges;
	private Set<RHSConceptChange> rhsChanges;
	private Set<LHSConceptChange> lhsChanges;
	private Set<ConceptChange> puDirSpec, puDirGen, puIndSpec, puIndGen, mixGen, mixSpec;
	private Set<RHSConceptChange> rhsPuDirSpec, rhsPuDirGen, rhsPuIndSpec, rhsPuIndGen, rhsMixGen, rhsMixSpec;
	private Set<LHSConceptChange> lhsPuDirSpec, lhsPuDirGen, lhsPuIndSpec, lhsPuIndGen, lhsMixGen, lhsMixSpec;
	private Set<OWLClass> lhsSpec, rhsSpec, allSpec, lhsGen, rhsGen, allGen, lhsAffected, rhsAffected, allAffected;
	private double entDiffTime, partitionTime, totalTime;
	
	/**
	 * Constructor
	 * @param lhsChanges	Ontology 1 concept changes
	 * @param rhsChanges	Ontology 2 concept changes
	 * @param allChanges	All concept changes
	 */
	public ConceptChangeSet(Set<LHSConceptChange> lhsChanges, Set<RHSConceptChange> rhsChanges, Set<ConceptChange> allChanges) {
		this.lhsChanges = lhsChanges;
		this.rhsChanges = rhsChanges;
		this.allChanges = allChanges;
	}
	
	
	/**
	 * Get the set of all concept changes (in both ontologies)
	 * @return Set of all concept changes (in both ontologies)
	 */
	public Set<ConceptChange> getAllConceptChanges() {
		return allChanges;
	}
	
	
	/**
	 * Get the set of concept changes in ontology 2
	 * @return Set of concept changes in ontology 2
	 */
	public Set<RHSConceptChange> getRHSConceptChanges() {
		return rhsChanges;
	}
	
	
	/**
	 * Get the set of concept changes in ontology 1
	 * @return Set of concept changes in ontology 1
	 */
	public Set<LHSConceptChange> getLHSConceptChanges() {
		return lhsChanges;
	}
	
	
	/**
	 * Get the set of all specialised concepts between ontologies
	 * @return Set of specialised concepts in both ontology 1 and 2
	 */
	public Set<OWLClass> getAllSpecialisedConcepts() {
		if(allSpec == null) {
			sortOutRHSAffectedConcepts();
			sortOutLHSAffectedConcepts();
		}
		return allSpec;
	}
	
	
	/**
	 * Get the set of specialised concepts in ontology 1
	 * @return Set of specialised concepts in ontology 1
	 */
	public Set<OWLClass> getLHSSpecialisedConcepts() {
		if(lhsSpec == null) sortOutLHSAffectedConcepts();
		return lhsSpec;
	}
	
	
	/**
	 * Get the set of specialised concepts in ontology 2
	 * @return Set of specialised concepts in ontology 2
	 */
	public Set<OWLClass> getRHSSpecialisedConcepts() {
		if(rhsSpec == null) sortOutRHSAffectedConcepts();
		return rhsSpec;
	}
	
	
	/**
	 * Get the set of all generalised concepts between ontologies
	 * @return Set of generalised concepts in both ontology 1 and 2
	 */
	public Set<OWLClass> getAllGeneralisedConcepts() {
		if(allGen == null) {
			sortOutRHSAffectedConcepts();
			sortOutLHSAffectedConcepts();
		}
		return allGen;
	}
	
	
	/**
	 * Get the set of generalised concepts in ontology 1
	 * @return Set of generalised concepts in ontology 1
	 */
	public Set<OWLClass> getLHSGeneralisedConcepts() {
		if(lhsGen == null) sortOutLHSAffectedConcepts();
		return lhsGen;
	}
	
	
	/**
	 * Get the set of generalised concepts in ontology 2
	 * @return Set of generalised concepts in ontology 2
	 */
	public Set<OWLClass> getRHSGeneralisedConcepts() {
		if(rhsGen == null) sortOutRHSAffectedConcepts();
		return rhsGen;
	}
	
	
	/**
	 * Get the set of all affected concepts between ontologies
	 * @return Set of affected concepts in both ontology 1 and 2
	 */
	public Set<OWLClass> getAllAffectedConcepts() {
		if(allAffected == null) {
			sortOutRHSAffectedConcepts();
			sortOutLHSAffectedConcepts();
		}
		return allAffected;
	}
	
	
	/**
	 * Get the set of affected concepts in ontology 1
	 * @return Set of affected concepts in ontology 1
	 */
	public Set<OWLClass> getLHSAffectedConcepts() {
		if(lhsAffected == null) sortOutLHSAffectedConcepts();
		return lhsAffected;
	}
	
	
	/**
	 * Get the set of affected concepts in ontology 2
	 * @return Set of affected concepts in ontology 2
	 */
	public Set<OWLClass> getRHSAffectedConcepts() {
		if(rhsAffected == null) sortOutRHSAffectedConcepts();
		return rhsAffected;
	}
	
	
	/**
	 * Get the set of witness axiom for all specialised concepts
	 * @return Set of witness axioms for all specialised concepts
	 */
	public Set<OWLAxiom> getSpecialisationWitnesses() {
		Set<OWLAxiom> output = new HashSet<OWLAxiom>();
		for(ConceptChange c : allChanges) {
			output.addAll(c.getDirectSpecialisationWitnesses());
			output.addAll(c.getIndirectSpecialisationWitnesses());
		}
		return output;
	}
	
	
	/**
	 * Get the set of witness axiom for all generalised concepts
	 * @return Set of witness axioms for all generalised concepts
	 */
	public Set<OWLAxiom> getGeneralisationWitnesses() {
		Set<OWLAxiom> output = new HashSet<OWLAxiom>();
		for(ConceptChange c : allChanges) {
			output.addAll(c.getDirectGeneralisationWitnesses());
			output.addAll(c.getIndirectGeneralisationWitnesses());
		}
		return output;
	}
	
	
	/**
	 * Get all directly generalised concepts (in ontology 1 and 2)
	 * @return Set of all directly generalised concepts in both ontologies
	 */
	public Set<OWLClass> getAllDirectlyGeneralisedClasses() {
		if(puDirGen == null || mixGen == null) sortOutAllChanges();
		Set<OWLClass> output = new HashSet<OWLClass>();
		for(ConceptChange c : puDirGen)
			output.add(c.getConcept());
		for(ConceptChange c : mixGen) 
			output.add(c.getConcept());
		return output;
	}
	
	
	/**
	 * Get all direct generalised concept changes
	 * @return Set of direct generalised changes
	 */
	public Set<ConceptChange> getAllDirectlyGeneralised() {
		if(puDirGen == null || mixGen == null) sortOutAllChanges();
		Set<ConceptChange> out = new HashSet<ConceptChange>();
		out.addAll(puDirGen); out.addAll(mixGen);
		return out;
	}
	
	
	/**
	 * Get the directly generalised concepts in ontology 1
	 * @return Set of directly generalised concepts in ontology 1 
	 */
	public Set<LHSConceptChange> getLHSDirectlyGeneralised() {
		Set<LHSConceptChange> output = new HashSet<LHSConceptChange>();
		output.addAll(getLHSPurelyDirectlyGeneralised());
		output.addAll(getLHSMixedGeneralised());
		return output;
	}
	
	
	/**
	 * Get the directly generalised concepts in ontology 2
	 * @return Set of directly generalised concepts in ontology 2 
	 */
	public Set<RHSConceptChange> getRHSDirectlyGeneralised() {
		Set<RHSConceptChange> output = new HashSet<RHSConceptChange>();
		output.addAll(getRHSPurelyDirectlyGeneralised());
		output.addAll(getRHSMixedGeneralised());
		return output;
	}
	
	
	/**
	 * Get all purely directly generalised concepts (in ontology 1 and 2)
	 * @return Set of all purely directly generalised concepts in both ontologies
	 */
	public Set<OWLClass> getAllPurelyDirectlyGeneralisedClasses() {
		if(puDirGen == null) sortOutAllChanges();
		Set<OWLClass> output = new HashSet<OWLClass>();
		for(ConceptChange c : puDirGen)
			output.add(c.getConcept());
		return output;
	}
	
	
	/**
	 * Get all purely directly generalised concept changes
	 * @return Set of all purely directly generalised concept changes
	 */
	public Set<ConceptChange> getAllPurelyDirectlyGeneralised() {
		if(puDirGen == null) sortOutAllChanges();
		return puDirGen;
	}
	
	
	/**
	 * Get the purely directly generalised concepts in ontology 1
	 * @return Set of purely directly generalised concepts in ontology 1
	 */
	public Set<LHSConceptChange> getLHSPurelyDirectlyGeneralised() {
		if(lhsPuDirGen == null) sortOutLHSChanges();
		return lhsPuDirGen;
	}
	
	
	/**
	 * Get the purely directly generalised concepts in ontology 2
	 * @return Set of purely directly generalised concepts in ontology 2
	 */
	public Set<RHSConceptChange> getRHSPurelyDirectlyGeneralised() {
		if(rhsPuDirGen == null) sortOutRHSChanges();
		return rhsPuDirGen;
	}
	
	
	/**
	 * Get all indirect generalised concept changes
	 * @return Set of indirect generalised changes
	 */
	public Set<ConceptChange> getAllIndirectlyGeneralised() {
		if(puIndGen == null || mixGen == null) sortOutAllChanges();
		Set<ConceptChange> out = new HashSet<ConceptChange>();
		out.addAll(puIndGen); out.addAll(mixGen);
		return out;
	}
	
	
	/**
	 * Get the indirectly generalised concepts in ontology 1
	 * @return Set of indirectly generalised concepts in ontology 1 
	 */
	public Set<LHSConceptChange> getLHSIndirectlyGeneralised() {
		Set<LHSConceptChange> output = new HashSet<LHSConceptChange>();
		output.addAll(getLHSPurelyIndirectlyGeneralised());
		output.addAll(getLHSMixedGeneralised());
		return output;
	}
	
	
	/**
	 * Get the indirectly generalised concepts in ontology 2
	 * @return Set of indirectly generalised concepts in ontology 2 
	 */
	public Set<RHSConceptChange> getRHSIndirectlyGeneralised() {
		Set<RHSConceptChange> output = new HashSet<RHSConceptChange>();
		output.addAll(getRHSPurelyIndirectlyGeneralised());
		output.addAll(getRHSMixedGeneralised());
		return output;
	}
	
	
	/**
	 * Get all purely indirectly generalised concepts (in ontology 1 and 2)
	 * @return Set of all purely indirectly generalised concepts in both ontologies
	 */
	public Set<OWLClass> getAllPurelyIndirectlyGeneralisedClasses() {
		if(puIndGen == null) sortOutAllChanges();
		Set<OWLClass> output = new HashSet<OWLClass>();
		for(ConceptChange c : puIndGen)
			output.add(c.getConcept());
		return output;
	}
	
	
	/**
	 * Get all purely indirectly generalised concept changes
	 * @return Set of all purely indirectly generalised concept changes
	 */
	public Set<ConceptChange> getAllPurelyIndirectlyGeneralised() {
		if(puIndGen == null) sortOutAllChanges();
		return puIndGen;
	}
	
	
	/**
	 * Get the purely indirectly generalised concepts in ontology 1
	 * @return Set of purely indirectly generalised concepts in ontology 1
	 */
	public Set<LHSConceptChange> getLHSPurelyIndirectlyGeneralised() {
		if(lhsPuIndGen == null) sortOutLHSChanges();
		return lhsPuIndGen;
	}
	
	
	/**
	 * Get the purely indirectly generalised concepts in ontology 2
	 * @return Set of purely indirectly generalised concepts in ontology 2
	 */
	public Set<RHSConceptChange> getRHSPurelyIndirectlyGeneralised() {
		if(rhsPuIndGen == null) sortOutRHSChanges();
		return rhsPuIndGen;
	}
	
	
	/**
	 * Get all directly specialised concepts (in ontology 1 and 2)
	 * @return Set of all directly specialised concepts in both ontologies
	 */
	public Set<OWLClass> getAllDirectlySpecialisedClasses() {
		if(puDirSpec == null || mixSpec == null) sortOutAllChanges();
		Set<OWLClass> output = new HashSet<OWLClass>();
		for(ConceptChange c : puDirSpec)
			output.add(c.getConcept());
		for(ConceptChange c : mixSpec) 
			output.add(c.getConcept());
		return output;
	}
	
	
	/**
	 * Get all direct specialised concept changes
	 * @return Set of direct specialised changes
	 */
	public Set<ConceptChange> getAllDirectlySpecialised() {
		if(puDirSpec == null || mixSpec == null) sortOutAllChanges();
		Set<ConceptChange> out = new HashSet<ConceptChange>();
		out.addAll(puDirSpec); out.addAll(mixSpec);
		return out;
	}
	
	
	/**
	 * Get the directly specialised concepts in ontology 1
	 * @return Set of directly specialised concepts in ontology 1 
	 */
	public Set<LHSConceptChange> getLHSDirectlySpecialised() {
		Set<LHSConceptChange> output = new HashSet<LHSConceptChange>();
		output.addAll(getLHSPurelyDirectlySpecialised());
		output.addAll(getLHSMixedSpecialised());
		return output;
	}
	
	
	/**
	 * Get the directly specialised concepts in ontology 2
	 * @return Set of directly specialised concepts in ontology 2 
	 */
	public Set<RHSConceptChange> getRHSDirectlySpecialised() {
		Set<RHSConceptChange> output = new HashSet<RHSConceptChange>();
		output.addAll(getRHSPurelyDirectlySpecialised());
		output.addAll(getRHSMixedSpecialised());
		return output;
	}
	
	
	/**
	 * Get all purely directly specialised concepts (in ontology 1 and 2)
	 * @return Set of all purely directly specialised concepts in both ontologies
	 */
	public Set<OWLClass> getAllPurelyDirectlySpecialisedClasses() {
		if(puDirSpec == null) sortOutAllChanges();
		Set<OWLClass> output = new HashSet<OWLClass>();
		for(ConceptChange c : puDirSpec)
			output.add(c.getConcept());
		return output;
	}
	
	
	/**
	 * Get all purely directly specialised concept changes
	 * @return Set of purely directly specialised changes
	 */
	public Set<ConceptChange> getAllPurelyDirectlySpecialised() {
		if(puDirSpec == null) sortOutAllChanges();
		return puDirSpec;
	}
	
	
	/**
	 * Get the purely directly specialised concepts in ontology 1
	 * @return Set of purely directly specialised concepts in ontology 1
	 */
	public Set<LHSConceptChange> getLHSPurelyDirectlySpecialised() {
		if(lhsPuDirSpec == null) sortOutLHSChanges();
		return lhsPuDirSpec;
	}
	
	
	/**
	 * Get the purely directly specialised concepts in ontology 2
	 * @return Set of purely directly specialised concepts in ontology 2
	 */
	public Set<RHSConceptChange> getRHSPurelyDirectlySpecialised() {
		if(rhsPuDirSpec == null) sortOutRHSChanges();
		return rhsPuDirSpec;
	}
	
	
	/**
	 * Get all indirect specialised concept changes
	 * @return Set of indirect specialised changes
	 */
	public Set<ConceptChange> getAllIndirectlySpecialised() {
		if(puIndSpec == null || mixSpec == null) sortOutAllChanges();
		Set<ConceptChange> out = new HashSet<ConceptChange>();
		out.addAll(puIndSpec); out.addAll(mixSpec);
		return out;
	}

	
	/**
	 * Get the directly specialised concepts in ontology 1
	 * @return Set of directly specialised concepts in ontology 1 
	 */
	public Set<LHSConceptChange> getLHSIndirectlySpecialised() {
		Set<LHSConceptChange> output = new HashSet<LHSConceptChange>();
		output.addAll(getLHSPurelyIndirectlySpecialised());
		output.addAll(getLHSMixedSpecialised());
		return output;
	}
	
	
	/**
	 * Get the indirectly specialised concepts in ontology 2
	 * @return Set of indirectly specialised concepts in ontology 2 
	 */
	public Set<RHSConceptChange> getRHSIndirectlySpecialised() {
		Set<RHSConceptChange> output = new HashSet<RHSConceptChange>();
		output.addAll(getRHSPurelyIndirectlySpecialised());
		output.addAll(getRHSMixedSpecialised());
		return output;
	}
	
	
	/**
	 * Get all purely indirectly specialised concepts (in ontology 1 and 2)
	 * @return Set of all purely indirectly specialised concepts in both ontologies
	 */
	public Set<OWLClass> getAllPurelyIndirectlySpecialisedClasses() {
		if(puIndSpec == null) sortOutAllChanges();
		Set<OWLClass> output = new HashSet<OWLClass>();
		for(ConceptChange c : puIndSpec)
			output.add(c.getConcept());
		return output;
	}
	
	
	/**
	 * Get all purely indirectly specialised concept changes
	 * @return Set of purely indirect concepts changes
	 */
	public Set<ConceptChange> getAllPurelyIndirectlySpecialised() {
		if(puIndSpec == null) sortOutAllChanges();
		return puIndSpec;
	}
	
	
	/**
	 * Get the purely indirectly specialised concepts in ontology 1
	 * @return Set of purely indirectly specialised concepts in ontology 1
	 */
	public Set<LHSConceptChange> getLHSPurelyIndirectlySpecialised() {
		if(lhsPuIndSpec == null) sortOutLHSChanges();
		return lhsPuIndSpec;
	}
	
	
	/**
	 * Get the purely indirectly specialised concepts in ontology 2
	 * @return Set of purely indirectly specialised concepts in ontology 2
	 */
	public Set<RHSConceptChange> getRHSPurelyIndirectlySpecialised() {
		if(rhsPuIndSpec == null) sortOutRHSChanges();
		return rhsPuIndSpec;
	}
	
	
	/**
	 * Get the mixed specialised concepts (in ontology 1 and 2)
	 * @return Set of mixed specialised concepts in both ontologies
	 */
	public Set<OWLClass> getAllMixedSpecialisedClasses() {
		if(mixSpec == null) sortOutAllChanges();
		Set<OWLClass> output = new HashSet<OWLClass>();
		for(ConceptChange c : mixSpec)
			output.add(c.getConcept());
		return output;
	}
	
	
	/**
	 * Get the mixed specialised concept changes
	 * @return Set of mixed specialised concept changes
	 */
	public Set<ConceptChange> getAllMixedSpecialised() {
		if(mixSpec == null) sortOutAllChanges();
		return mixSpec;
	}
	
	
	/**
	 * Get the mixed specialised concepts in ontology 2
	 * @return Set of mixed specialised concepts in ontology 2
	 */
	public Set<RHSConceptChange> getRHSMixedSpecialised() {
		if(rhsMixSpec == null) sortOutRHSChanges();
		return rhsMixSpec;
	}
	
	
	/**
	 * Get the mixed specialised concepts in ontology 1
	 * @return Set of mixed specialised concepts in ontology 1
	 */
	public Set<LHSConceptChange> getLHSMixedSpecialised() {
		if(lhsMixSpec == null) sortOutLHSChanges();
		return lhsMixSpec;
	}
	
	
	/**
	 * Get the mixed generalised concepts (in ontology 1 and 2)
	 * @return Set of mixed generalised concepts in both ontologies
	 */
	public Set<OWLClass> getAllMixedGeneralisedClasses() {
		if(mixGen == null) sortOutAllChanges();
		Set<OWLClass> output = new HashSet<OWLClass>();
		for(ConceptChange c : mixGen)
			output.add(c.getConcept());
		return output;
	}
	
	
	/**
	 * Get the mixed generalised concept changes
	 * @return Set of mixed generalised concept changes
	 */
	public Set<ConceptChange> getAllMixedGeneralised() {
		if(mixGen == null) sortOutAllChanges();
		return mixGen;
	}
	
	
	/**
	 * Get the mixed generalised concepts in ontology 2
	 * @return Set of mixed generalised concepts in ontology 2
	 */
	public Set<RHSConceptChange> getRHSMixedGeneralised() {
		if(rhsMixGen == null) sortOutRHSChanges();
		return rhsMixGen;
	}
	
	
	/**
	 * Get the mixed generalised concepts in ontology 1
	 * @return Set of mixed generalised concepts in ontology 1
	 */
	public Set<LHSConceptChange> getLHSMixedGeneralised() {
		if(lhsMixGen == null) sortOutLHSChanges();
		return lhsMixGen;
	}
	
	
	/**
	 * Sort out the overall concept changes into the appropriate sets
	 */
	private void sortOutAllChanges() {
		initOverallChanges();
		for(ConceptChange c : allChanges) {
			if(c.isPurelyDirectlySpecialised())
				puDirSpec.add(c);
			if(c.isPurelyDirectlyGeneralised())
				puDirGen.add(c);
			if(c.isPurelyIndirectlySpecialised())
				puIndSpec.add(c);
			if(c.isPurelyIndirectlyGeneralised())
				puIndGen.add(c);
			if(c.isDirectlySpecialised() && c.isIndirectlySpecialised())
				mixSpec.add(c);
			if(c.isDirectlyGeneralised() && c.isIndirectlyGeneralised())
				mixGen.add(c);
		}
	}
	
	
	/**
	 * Sort out the RHS concept changes into the appropriate sets
	 */
	private void sortOutRHSChanges() {
		initRHSChanges();
		for(RHSConceptChange c : rhsChanges) {
			if(c.isPurelyDirectlySpecialised())
				rhsPuDirSpec.add(c);
			if(c.isPurelyDirectlyGeneralised())
				rhsPuDirGen.add(c);
			if(c.isPurelyIndirectlySpecialised())
				rhsPuIndSpec.add(c);
			if(c.isPurelyIndirectlyGeneralised())
				rhsPuIndGen.add(c);
			if(c.isDirectlySpecialised() && c.isIndirectlySpecialised())
				rhsMixSpec.add(c);
			if(c.isDirectlyGeneralised() && c.isIndirectlyGeneralised())
				rhsMixGen.add(c);
		}
	}
	
	
	/**
	 * Sort out the LHS concept changes into the appropriate sets
	 */
	private void sortOutLHSChanges() {
		initLHSChanges();
		for(LHSConceptChange c : lhsChanges) {
			if(c.isPurelyDirectlySpecialised())
				lhsPuDirSpec.add(c);
			if(c.isPurelyDirectlyGeneralised())
				lhsPuDirGen.add(c);
			if(c.isPurelyIndirectlySpecialised())
				lhsPuIndSpec.add(c);
			if(c.isPurelyIndirectlyGeneralised())
				lhsPuIndGen.add(c);
			if(c.isDirectlySpecialised() && c.isIndirectlySpecialised())
				lhsMixSpec.add(c);
			if(c.isDirectlyGeneralised() && c.isIndirectlyGeneralised())
				lhsMixGen.add(c);
		}
	}
	
	
	/**
	 * Sort out the RHS affected concepts sets
	 */
	private void sortOutRHSAffectedConcepts() {
		initRHSConceptSets();
		if(allSpec == null) initOverallConceptSets();
		for(RHSConceptChange c : rhsChanges) {
			OWLClass ac = c.getConcept();
			if(c.isSpecialised()) {
				rhsSpec.add(ac);
				allSpec.add(ac);
			}
			if(c.isGeneralised()) {
				rhsGen.add(ac);
				allGen.add(ac);
			}
			rhsAffected.add(ac);
			allAffected.add(ac);
		}
	}
	
	
	/**
	 * Sort out the LHS affected concepts sets
	 */
	private void sortOutLHSAffectedConcepts() {
		initLHSConceptSets();
		if(allSpec == null) initOverallConceptSets();
		for(LHSConceptChange c : lhsChanges) {
			OWLClass ac = c.getConcept();
			if(c.isSpecialised()) {
				lhsSpec.add(ac);
				allSpec.add(ac);
			}
			if(c.isGeneralised()) {
				lhsGen.add(ac);
				allGen.add(ac);
			}
			lhsAffected.add(ac);
			allAffected.add(ac);
		}
	}
	
	
	/**
	 * Initialize RHS concept change sets
	 */
	private void initRHSChanges() {
		rhsPuDirSpec = new HashSet<RHSConceptChange>();
		rhsPuDirGen = new HashSet<RHSConceptChange>();
		rhsPuIndSpec = new HashSet<RHSConceptChange>();
		rhsPuIndGen = new HashSet<RHSConceptChange>();
		rhsMixSpec = new HashSet<RHSConceptChange>();
		rhsMixGen = new HashSet<RHSConceptChange>();
	}
	
	
	/**
	 * Initialize LHS concept change sets
	 */
	private void initLHSChanges() {
		lhsPuDirSpec = new HashSet<LHSConceptChange>();
		lhsPuDirGen = new HashSet<LHSConceptChange>();
		lhsPuIndSpec = new HashSet<LHSConceptChange>();
		lhsPuIndGen = new HashSet<LHSConceptChange>();
		lhsMixSpec = new HashSet<LHSConceptChange>();
		lhsMixGen = new HashSet<LHSConceptChange>();
	}
	
	
	/**
	 * Initialize concept change sets
	 */
	private void initOverallChanges() {
		puDirSpec = new HashSet<ConceptChange>();
		puDirGen = new HashSet<ConceptChange>();
		puIndSpec = new HashSet<ConceptChange>();
		puIndGen = new HashSet<ConceptChange>();
		mixSpec = new HashSet<ConceptChange>();
		mixGen = new HashSet<ConceptChange>();
	}
	
	
	/**
	 * Initialize RHS affected concept sets
	 */
	private void initRHSConceptSets() {
		rhsSpec = new HashSet<OWLClass>();
		rhsGen = new HashSet<OWLClass>();
		rhsAffected = new HashSet<OWLClass>();
	}
	
	
	/**
	 * Initialize LHS affected concept sets
	 */
	private void initLHSConceptSets() {
		lhsSpec = new HashSet<OWLClass>();
		lhsGen = new HashSet<OWLClass>();
		lhsAffected = new HashSet<OWLClass>();
	}
	
	
	/**
	 * Initialize overall affected concept sets
	 */
	private void initOverallConceptSets() {
		allSpec = new HashSet<OWLClass>();
		allGen = new HashSet<OWLClass>();
		allAffected = new HashSet<OWLClass>();
	}
	
	
	/**
	 * Set the direct-indirect change partitioning time
	 * @param d	Time in seconds
	 */
	public void setPartitioningTime(double d) {
		partitionTime = d;
	}
	
	
	/**
	 * Set the entailment diff time
	 * @param d	Time in seconds
	 */
	public void setEntailmentDiffTime(double d) {
		entDiffTime = d;
	}
	
	
	/**
	 * Set the total diff time
	 * @param d	Time in seconds
	 */
	public void setTotalTime(double d) {
		totalTime = d;
	}
	
	
	/**
	 * Get the direct-indirect change partitioning time
	 * @return Partitioning time in seconds
	 */
	public double getPartitioningTime() {
		return partitionTime;
	}
	
	
	/**
	 * Get the entailment diff time
	 * @return Diff time in seconds
	 */
	public double getEntailmentDiffTime() {
		return entDiffTime;
	}
	
	
	/**
	 * Get the total diff time
	 * @return Diff time in seconds
	 */
	public double getTotalDiffTime() {
		return totalTime;
	}
}
