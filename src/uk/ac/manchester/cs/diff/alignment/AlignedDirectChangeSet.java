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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.manchester.cs.diff.axiom.changeset.CategorisedChangeSet;
import uk.ac.manchester.cs.diff.concept.change.ConceptChange;
import uk.ac.manchester.cs.diff.concept.changeset.ConceptChangeSet;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class AlignedDirectChangeSet {
	private OWLOntology ont1, ont2;
	private ConceptChangeSet conceptChangeSet;
	private Set<OWLAxiom> eff_adds, eff_rems;
	private Map<OWLAxiom,Set<? extends ConceptChange>> ont1map_spec, ont1map_gen, ont2map_spec, ont2map_gen;
	private int nrJusts;
	
	/**
	 * Constructor
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 * @param axiomChangeSet	Axiom change set
	 * @param conceptChangeSet	Concept change set
	 * @param nrJusts	Maximum number of justifications computed per change
	 */
	public AlignedDirectChangeSet(OWLOntology ont1, OWLOntology ont2, CategorisedChangeSet axiomChangeSet, 
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
		ForkJoinPool fjPool = new ForkJoinPool();
		ont1map_spec = fjPool.invoke(new ChangeAligner(ont1, conceptChangeSet.getLHSDirectlySpecialised(), eff_rems, nrJusts, true, true));
		ont1map_gen = fjPool.invoke(new ChangeAligner(ont1, conceptChangeSet.getLHSDirectlyGeneralised(), eff_rems, nrJusts, false, true));
		ont2map_spec = fjPool.invoke(new ChangeAligner(ont2, conceptChangeSet.getRHSDirectlySpecialised(), eff_adds, nrJusts, true, true));
		ont2map_gen = fjPool.invoke(new ChangeAligner(ont2, conceptChangeSet.getRHSDirectlySpecialised(), eff_adds, nrJusts, false, true));
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