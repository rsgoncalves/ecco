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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owlapi.model.OWLAxiom;

import uk.ac.manchester.cs.diff.concept.change.ConceptChange;

/**
 * @author Rafael S. Goncalves <br>
 * Information Management Group (IMG) <br>
 * School of Computer Science <br>
 * University of Manchester <br>
 */
public class ChangeAligner {
	private Map<OWLAxiom,Set<Explanation<OWLAxiom>>> justsMap;
	private Set<? extends ConceptChange> changes;
	private Set<OWLAxiom> effChanges;
	private boolean spec, direct;
	
	/**
	 * Constructor
	 * @param changes	Set of concept changes
	 * @param effChanges	Effectual changes
	 * @param justsMap	Map of justifications to entailments
	 * @param spec	true if checking specialisations, false if generalisations
	 * @param direct	true if checking direct changes, false if indirect
	 */
	public ChangeAligner(Set<? extends ConceptChange> changes, Set<OWLAxiom> effChanges, Map<OWLAxiom,Set<Explanation<OWLAxiom>>> justsMap, 
			boolean spec, boolean direct) {
		this.changes = changes;
		this.effChanges = effChanges;
		this.spec = spec;
		this.direct = direct;
		this.justsMap = justsMap;
	}

	
	/**
	 * Align each concept change with the asserted axiom that gives rise to that, by
	 * analysing justifications for witness axioms of each concept change
	 * @return Map of asserted axioms to the concepts they affect 
	 */
	public Map<OWLAxiom,Set<? extends ConceptChange>> alignChangeWitnesses() {
		Map<OWLAxiom,Set<? extends ConceptChange>> map = new HashMap<OWLAxiom,Set<? extends ConceptChange>>();
		for(ConceptChange c : changes) {
			Set<OWLAxiom> wits = null;
			if(spec) {
				if(direct) wits = c.getDirectSpecialisationWitnesses();
				else wits = c.getIndirectSpecialisationWitnesses();
			}
			else {
				if(direct) wits = c.getDirectGeneralisationWitnesses();
				else wits = c.getIndirectGeneralisationWitnesses();
			}

			for(OWLAxiom ax : wits) {
				Set<Explanation<OWLAxiom>> exps = justsMap.get(ax);
				for(Explanation<OWLAxiom> e : exps) {
					for(OWLAxiom just_ax : e.getAxioms()) {
						if(effChanges.contains(just_ax)) {
							if(map.containsKey(just_ax)) {
								@SuppressWarnings("unchecked")
								Set<ConceptChange> mappings = (Set<ConceptChange>) map.get(just_ax);
								mappings.add(c);
								map.put(just_ax, mappings);
							}
							else {
								Set<ConceptChange> mappings = new HashSet<ConceptChange>();
								mappings.add(c);
								map.put(just_ax, mappings);
							}
							if(spec && direct) c.addDirectSpecialisationWitnessForAxiom(just_ax, ax);
							if(!spec && direct) c.addDirectGeneralisationWitnessForAxiom(just_ax, ax);
							if(spec && !direct) c.addIndirectSpecialisationWitnessForAxiom(just_ax, ax);
							if(!spec && !direct) c.addIndirectGeneralisationWitnessForAxiom(just_ax, ax);
						}
					}
				}
			}
		}
		return map;
	}
}
