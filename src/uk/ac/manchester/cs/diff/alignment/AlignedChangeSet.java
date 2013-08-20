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
package uk.ac.manchester.cs.diff.alignment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import uk.ac.manchester.cs.diff.axiom.changeset.CategorisedChangeSet;
import uk.ac.manchester.cs.diff.concept.change.ConceptChange;
import uk.ac.manchester.cs.diff.concept.changeset.ConceptChangeSet;
import uk.ac.manchester.cs.diff.justifications.JustificationFinder;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class AlignedChangeSet {
	private OWLOntology ont1, ont2;
	private ConceptChangeSet conceptChangeSet;
	private Set<OWLAxiom> eff_adds, eff_rems;
	private Map<OWLAxiom,Set<ConceptChange>> ont1map_spec, ont1map_gen, ont2map_spec, ont2map_gen;
	private int nrJusts;
	
	/**
	 * Constructor
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 * @param axiomChangeSet	Axiom change set
	 * @param conceptChangeSet	Concept change set
	 * @param nrJusts	Maximum number of justifications computed per change
	 */
	public AlignedChangeSet(OWLOntology ont1, OWLOntology ont2, CategorisedChangeSet axiomChangeSet, 
			ConceptChangeSet conceptChangeSet, int nrJusts) {
		this.ont1 = ont1;
		this.ont2 = ont2;
		this.conceptChangeSet = conceptChangeSet;
		this.nrJusts = nrJusts;
		eff_adds = axiomChangeSet.getEffectualAdditionAxioms();
		eff_rems = axiomChangeSet.getEffectualRemovalAxioms();
		init();
	}
	
	
	/**
	 * Initialise data structures
	 */
	private void init() {
		ont1map_spec = new HashMap<OWLAxiom,Set<ConceptChange>>();
		ont1map_gen = new HashMap<OWLAxiom,Set<ConceptChange>>();
		ont2map_spec = new HashMap<OWLAxiom,Set<ConceptChange>>();
		ont2map_gen = new HashMap<OWLAxiom,Set<ConceptChange>>();
	}
		
	
	/**
	 * Align all concept and axiom changes
	 * @throws OWLOntologyCreationException
	 */
	public void alignChanges() {
		try {
			align(ont1map_spec, ont1, eff_rems, conceptChangeSet.getLHSDirectlySpecialised(), true);
			align(ont1map_gen, ont1, eff_rems, conceptChangeSet.getLHSDirectlyGeneralised(), false);
			align(ont2map_spec, ont2, eff_adds, conceptChangeSet.getRHSDirectlySpecialised(), true);
			align(ont2map_gen, ont2, eff_adds, conceptChangeSet.getRHSDirectlyGeneralised(), false);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Align a given set of concept changes, based on their witness axioms' justifications, with the respective axioms
	 * @param map	Map to be populated
	 * @param ont	Ontology
	 * @param effChanges	Effectual change set
	 * @param changes	Concept changes
	 * @param spec	true if checking specialisations, false if generalisations
	 * @throws OWLOntologyCreationException
	 */
	private void align(Map<OWLAxiom,Set<ConceptChange>> map, OWLOntology ont, Set<OWLAxiom> effChanges, 
			Set<? extends ConceptChange> changes, boolean spec) throws OWLOntologyCreationException {
		JustificationFinder justs = new JustificationFinder(ont, nrJusts);
		
		for(ConceptChange c : changes) {
			Set<OWLAxiom> wits = null;
			if(spec) wits = c.getDirectSpecialisationWitnesses();
			else wits = c.getDirectGeneralisationWitnesses();
			
			for(OWLAxiom ax : wits) {
				Set<Explanation<OWLAxiom>> exps = justs.getJustifications(ax);
				for(Explanation<OWLAxiom> e : exps) {
					for(OWLAxiom just_ax : e.getAxioms()) {
						if(effChanges.contains(just_ax)) {
							if(map.containsKey(just_ax)) {
								Set<ConceptChange> mappings = map.get(just_ax);
								mappings.add(c);
								map.put(just_ax, mappings);
							}
							else {
								Set<ConceptChange> mappings = new HashSet<ConceptChange>();
								mappings.add(c);
								map.put(just_ax, mappings);
							}
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * Get the map of axioms to concepts they specialise in ontology 1
	 * @return Map  of axioms to concepts they specialise in ontology 1
	 */
	public Map<OWLAxiom,Set<ConceptChange>> getOnt1SpecialisationsMap() {
		return ont1map_spec;
	}
	
	
	/**
	 * Get the map of axioms to concepts they generalise in ontology 1
	 * @return Map  of axioms to concepts they generalise in ontology 1
	 */
	public Map<OWLAxiom,Set<ConceptChange>> getOnt1GeneralisationsMap() {
		return ont1map_gen;
	}
	
	
	/**
	 * Get the map of axioms to concepts they specialise in ontology 2
	 * @return Map  of axioms to concepts they specialise in ontology 2
	 */
	public Map<OWLAxiom,Set<ConceptChange>> getOnt2SpecialisationsMap() {
		return ont2map_spec;
	}
	
	
	/**
	 * Get the map of axioms to concepts they generalise in ontology 2
	 * @return Map  of axioms to concepts they generalise in ontology 2
	 */
	public Map<OWLAxiom,Set<ConceptChange>> getOnt2GeneralisationsMap() {
		return ont2map_gen;
	}
}