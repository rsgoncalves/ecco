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
package uk.ac.manchester.cs.diff.alignment;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owlapi.model.OWLAxiom;

import uk.ac.manchester.cs.diff.axiom.changeset.CategorisedChangeSet;
import uk.ac.manchester.cs.diff.concept.change.ConceptChange;
import uk.ac.manchester.cs.diff.concept.changeset.ConceptChangeSet;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class AlignedIndirectChangeSet {
	private ConceptChangeSet conceptChangeSet;
	private Set<OWLAxiom> eff_adds, eff_rems;
	private Map<OWLAxiom,Set<? extends ConceptChange>> ont1map_spec, ont1map_gen, ont2map_spec, ont2map_gen;
	private Map<OWLAxiom, Set<Explanation<OWLAxiom>>> justsMap1, justsMap2;
	
	/**
	 * Constructor
	 * @param axiomChangeSet	Axiom change set
	 * @param conceptChangeSet	Concept change set
	 * @param justsMap1	Map of ontology1 witness axioms to their justifications in ontology 1
	 * @param justsMap2	Map of ontology2 witness axioms to their justifications in ontology 2
	 */
	public AlignedIndirectChangeSet(CategorisedChangeSet axiomChangeSet, ConceptChangeSet conceptChangeSet, 
			Map<OWLAxiom, Set<Explanation<OWLAxiom>>> justsMap1, Map<OWLAxiom, Set<Explanation<OWLAxiom>>> justsMap2) {
		this.conceptChangeSet = conceptChangeSet;
		this.justsMap1 = justsMap1;
		this.justsMap2 = justsMap2;
		eff_adds = axiomChangeSet.getEffectualAdditionAxioms();
		eff_rems = axiomChangeSet.getEffectualRemovalAxioms();
		init();
	}
	
	
	/**
	 * Initialise data structures and execute change alignment
	 */
	private void init() {
		ont1map_spec = new HashMap<OWLAxiom,Set<? extends ConceptChange>>();
		ont1map_gen = new HashMap<OWLAxiom,Set<? extends ConceptChange>>();
		ont2map_spec = new HashMap<OWLAxiom,Set<? extends ConceptChange>>();
		ont2map_gen = new HashMap<OWLAxiom,Set<? extends ConceptChange>>();
		alignChanges();
	}
		
	
	/**
	 * Align all concept and axiom changes
	 */
	public void alignChanges() {
		ont1map_spec = 
				new ChangeAligner(conceptChangeSet.getLHSIndirectlySpecialised(),eff_rems,justsMap1,true,false).alignChangeWitnesses();
		ont1map_gen = 
				new ChangeAligner(conceptChangeSet.getLHSIndirectlyGeneralised(),eff_rems,justsMap1,false,false).alignChangeWitnesses();
		ont2map_spec = 
				new ChangeAligner(conceptChangeSet.getRHSIndirectlySpecialised(),eff_adds,justsMap2,true,false).alignChangeWitnesses();
		ont2map_gen = 
				new ChangeAligner(conceptChangeSet.getRHSIndirectlyGeneralised(),eff_adds,justsMap2,false,false).alignChangeWitnesses();
	}
	
	
	/**
	 * Get the map of axioms to concepts they specialise in ontology 1
	 * @return Map  of axioms to concepts they specialise in ontology 1
	 */
	public Map<OWLAxiom,Set<? extends ConceptChange>> getOnt1SpecialisationsMap() {
		return ont1map_spec;
	}
	
	
	/**
	 * Get the map of axioms to concepts they generalise in ontology 1
	 * @return Map  of axioms to concepts they generalise in ontology 1
	 */
	public Map<OWLAxiom,Set<? extends ConceptChange>> getOnt1GeneralisationsMap() {
		return ont1map_gen;
	}
	
	
	/**
	 * Get the map of axioms to concepts they specialise in ontology 2
	 * @return Map  of axioms to concepts they specialise in ontology 2
	 */
	public Map<OWLAxiom,Set<? extends ConceptChange>> getOnt2SpecialisationsMap() {
		return ont2map_spec;
	}
	
	
	/**
	 * Get the map of axioms to concepts they generalise in ontology 2
	 * @return Map  of axioms to concepts they generalise in ontology 2
	 */
	public Map<OWLAxiom,Set<? extends ConceptChange>> getOnt2GeneralisationsMap() {
		return ont2map_gen;
	}
}