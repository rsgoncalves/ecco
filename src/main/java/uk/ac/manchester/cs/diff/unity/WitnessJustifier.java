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
package uk.ac.manchester.cs.diff.unity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.manchester.cs.diff.concept.change.ConceptChange;
import uk.ac.manchester.cs.diff.concept.changeset.ConceptChangeSet;
import uk.ac.manchester.cs.diff.justifications.JustificationFinder;

/**
 * @author Rafael S. Goncalves <br>
 * Information Management Group (IMG) <br>
 * School of Computer Science <br>
 * University of Manchester <br>
 */
public class WitnessJustifier {
	private OWLOntology ont;
	private ConceptChangeSet conceptChangeSet;
	private String side;
	private int nrJusts;
	
	/**
	 * Constructor
	 * @param ont	Ontology
	 * @param conceptChangeSet	Concept changes
	 * @param nrJusts	Number of justifications to extract per witness axiom
	 * @param side	Extract justifications for witness axioms to changes in ontology 1 (lhs) or ontology 2 (rhs)
	 */
	public WitnessJustifier(OWLOntology ont, ConceptChangeSet conceptChangeSet, int nrJusts, String side) {
		this.ont = ont;
		this.conceptChangeSet = conceptChangeSet;
		this.nrJusts = nrJusts;
		this.side = side;
	}
	
	
	/**
	 * Get justifications for each witness axiom
	 * @return Map of witness axioms to their corresponding justifications in the given ontology
	 */
	public Map<OWLAxiom, Set<Explanation<OWLAxiom>>> getJustifications() {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		Set<? extends ConceptChange> changes = null;
		if(side.equals("lhs"))
			changes = conceptChangeSet.getLHSConceptChanges();
		else if(side.equals("rhs"))
			changes = conceptChangeSet.getRHSConceptChanges();
		
		for(ConceptChange c : changes) 
			axioms.addAll(c.getAllWitnesses());
		
		JustificationFinder justFinder = new JustificationFinder(ont, nrJusts);
		return justFinder.getJustifications(axioms);
	}
}
