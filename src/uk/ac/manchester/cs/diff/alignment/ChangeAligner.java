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
import java.util.concurrent.RecursiveTask;

import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.manchester.cs.diff.concept.change.ConceptChange;
import uk.ac.manchester.cs.diff.justifications.JustificationFinder;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class ChangeAligner extends RecursiveTask<Map<OWLAxiom,Set<? extends ConceptChange>>> {
	private static final long serialVersionUID = 6310419390814641451L;
	private Set<? extends ConceptChange> changes;
	private Set<OWLAxiom> effChanges;
	private OWLOntology ont;
	private boolean spec, direct;
	private int limit;
	private int MAX_SEQ = 20;
	
	/**
	 * Constructor
	 * @param ont	Ontology to extract justification from
	 * @param changes	Set of concept changes
	 * @param effChanges	Effectual changes
	 * @param limit	Number of desired justifications per axiom
	 * @param spec	true if checking specialisations, false if generalisations
	 * @param direct	true if checking direct changes, false if indirect
	 */
	public ChangeAligner(OWLOntology ont, Set<? extends ConceptChange> changes, Set<OWLAxiom> effChanges, int limit, boolean spec, boolean direct) {
		this.ont = ont;
		this.changes = changes;
		this.effChanges = effChanges;
		this.limit = limit;
		this.spec = spec;
		this.direct = direct;
	}

	
	@SuppressWarnings("unchecked")
	public Map<OWLAxiom,Set<? extends ConceptChange>> computeDirectly() {
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
				Set<Explanation<OWLAxiom>> exps = new JustificationFinder(ont, limit).getJustifications(ax);
				for(Explanation<OWLAxiom> e : exps) {
					for(OWLAxiom just_ax : e.getAxioms()) {
						if(effChanges.contains(just_ax)) {
							if(map.containsKey(just_ax)) {
								Set<ConceptChange> mappings = (Set<ConceptChange>) map.get(just_ax);
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
		return map;
	}

	@Override
	protected Map<OWLAxiom,Set<? extends ConceptChange>> compute() {
		Map<OWLAxiom,Set<? extends ConceptChange>> result = new HashMap<OWLAxiom,Set<? extends ConceptChange>>();
		if(changes.size() > MAX_SEQ) {
			int mid = changes.size()/2;
			ConceptChange[] changeArr = changes.toArray(new ConceptChange[changes.size()]);
			
			Set<ConceptChange> firstHalf = new HashSet<ConceptChange>();
			Set<ConceptChange> secondHalf = new HashSet<ConceptChange>();
			for(int i = 0; i < mid; i++)			
				firstHalf.add(changeArr[i]);
			for(int i = mid; i < changeArr.length; i++)	
				secondHalf.add(changeArr[i]);
	
			ChangeAligner cat1 = new ChangeAligner(ont, firstHalf, effChanges, limit, spec, direct);
			cat1.fork();
			ChangeAligner cat2 = new ChangeAligner(ont, secondHalf, effChanges, limit, spec, direct);
			result.putAll(cat2.invoke());
			result.putAll(cat1.join());
		}
		else result.putAll(computeDirectly());
		return result;
	}
}
