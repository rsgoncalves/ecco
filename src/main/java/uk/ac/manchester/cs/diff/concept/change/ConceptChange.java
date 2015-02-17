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
package uk.ac.manchester.cs.diff.concept.change;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;

import uk.ac.manchester.cs.diff.concept.witnesses.WitnessAxioms;

/**
 * @author Rafael S. Goncalves <br>
 * Information Management Group (IMG) <br>
 * School of Computer Science <br>
 * University of Manchester <br>
 */
public class ConceptChange {
	private OWLClass c;
	private Set<OWLAxiom> dirSpecWit, indirSpecWit, dirGenWit, indirGenWit, allWit;
	private Map<OWLAxiom,Set<OWLAxiom>> dirSpecWitsOfAx, dirGenWitsOfAx, indirSpecWitsOfAx, indirGenWitsOfAx;

	/**
	 * Constructor
	 * @param c	Concept
	 * @param specWits	Witnesses for a specialisation
	 * @param genWits	Witnesses for a generalisation
	 */
	public ConceptChange(OWLClass c, WitnessAxioms specWits, WitnessAxioms genWits) {
		this.c = c;
		dirSpecWit = specWits.getDirectWitnesses();
		indirSpecWit = specWits.getIndirectWitnesses();
		dirGenWit = genWits.getDirectWitnesses();
		indirGenWit = genWits.getIndirectWitnesses();
		allWit = new HashSet<OWLAxiom>();
		dirSpecWitsOfAx = new HashMap<OWLAxiom,Set<OWLAxiom>>();
		dirGenWitsOfAx = new HashMap<OWLAxiom,Set<OWLAxiom>>();
		indirSpecWitsOfAx = new HashMap<OWLAxiom,Set<OWLAxiom>>();
		indirGenWitsOfAx = new HashMap<OWLAxiom,Set<OWLAxiom>>();
	}
	
	
	/**
	 * Alternative constructor
	 * @param c	Concept
	 * @param dirSpecWit	Set of direct specialisation witnesses
	 * @param indirSpecWit	Set of indirect specialisation witnesses
	 * @param dirGenWit	Set of direct generalisation witnesses
	 * @param indirGenWit	Set of indirect generalisation witnesses
	 */
	public ConceptChange(OWLClass c, Set<OWLAxiom> dirSpecWit, Set<OWLAxiom> indirSpecWit, Set<OWLAxiom> dirGenWit, Set<OWLAxiom> indirGenWit) {
		this.c = c;
		this.dirSpecWit = dirSpecWit;
		this.indirSpecWit = indirSpecWit;
		this.dirGenWit = dirGenWit;
		this.indirGenWit = indirGenWit;
		allWit = new HashSet<OWLAxiom>();
		dirSpecWitsOfAx = new HashMap<OWLAxiom,Set<OWLAxiom>>();
		dirSpecWitsOfAx = new HashMap<OWLAxiom,Set<OWLAxiom>>();
		indirSpecWitsOfAx = new HashMap<OWLAxiom,Set<OWLAxiom>>();
		indirSpecWitsOfAx = new HashMap<OWLAxiom,Set<OWLAxiom>>();
	}
		
	
	/**
	 * Get the affected concept name
	 * @return Concept name
	 */
	public OWLClass getConcept() {
		return c;
	}
	
	
	/**
	 * Get all witness axioms for the concept change
	 * @return Set of witness axioms
	 */
	public Set<OWLAxiom> getAllWitnesses() {
		allWit.addAll(dirSpecWit);
		allWit.addAll(dirGenWit);
		allWit.addAll(indirSpecWit);
		allWit.addAll(indirGenWit);
		return allWit;
	}
	
	
	/**
	 * Get the set of witnesses for a directly specialised concept
	 * @return Set of direct specialisation witnesses
	 */
	public Set<OWLAxiom> getDirectSpecialisationWitnesses() {
		return dirSpecWit;
	}
	
	
	/**
	 * Get the set of witnesses for an indirectly specialised concept
	 * @return Set of indirect specialisation witnesses
	 */
	public Set<OWLAxiom> getIndirectSpecialisationWitnesses() {
		return indirSpecWit;
	}
	
	
	/**
	 * Get the set of witnesses for a directly generalised concept
	 * @return Set of direct generalisation witnesses
	 */
	public Set<OWLAxiom> getDirectGeneralisationWitnesses() {
		return dirGenWit;
	}
	
	
	/**
	 * Get the set of witnesses for an indirectly generalised concept
	 * @return Set of indirect generalisation witnesses
	 */
	public Set<OWLAxiom> getIndirectGeneralisationWitnesses() {
		return indirGenWit;
	}
	
	
	/**
	 * Check if the concept is directly affected
	 * @return true if concept is directly affected, false otherwise
	 */
	public boolean isDirectlyAffected() {
		if(!dirSpecWit.isEmpty() || !dirGenWit.isEmpty())
			return true;
		else
			return false;
	}
	
	
	/**
	 * Check if the concept is indirectly affected
	 * @return true if concept is indirectly affected, false otherwise
	 */
	public boolean isIndirectlyAffected() {
		if(!indirSpecWit.isEmpty() || !indirGenWit.isEmpty())
			return true;
		else
			return false;
	}
	
	
	/**
	 * Check if concept is specialised
	 * @return true if concept is specialised, false otherwise
	 */
	public boolean isSpecialised() {
		if(!dirSpecWit.isEmpty() || !indirSpecWit.isEmpty())
			return true;
		else
			return false;
	}
	
	
	/**
	 * Check if concept is generalised
	 * @return true if concept is generalised, false otherwise
	 */
	public boolean isGeneralised() {
		if(!dirGenWit.isEmpty() || !indirGenWit.isEmpty())
			return true;
		else
			return false;
	}
	
	
	/**
	 * Check if concept is directly specialised
	 * @return true if concept is directly specialised, false otherwise
	 */
	public boolean isDirectlySpecialised() {
		if(!dirSpecWit.isEmpty())
			return true;
		else 
			return false;
	}
	
	
	/**
	 * Check if concept is directly generalised
	 * @return true if concept is directly generalised, false otherwise
	 */
	public boolean isDirectlyGeneralised() {
		if(!dirGenWit.isEmpty())
			return true;
		else 
			return false;
	}
	
	
	/**
	 * Check if concept is indirectly specialised
	 * @return true if concept is indirectly specialised, false otherwise
	 */
	public boolean isIndirectlySpecialised() {
		if(!indirSpecWit.isEmpty())
			return true;
		else 
			return false;
	}
	
	
	/**
	 * Check if concept is indirectly generalised
	 * @return true if concept is indirectly generalised, false otherwise
	 */
	public boolean isIndirectlyGeneralised() {
		if(!indirGenWit.isEmpty())
			return true;
		else 
			return false;
	}
	
	
	/**
	 * Check if concept is purely directly specialised
	 * @return true if concept is purely directly specialised, false otherwise
	 */
	public boolean isPurelyDirectlySpecialised() {
		if(isDirectlySpecialised() && !isIndirectlySpecialised())
			return true;
		else
			return false;
	}
	
	
	/**
	 * Check if concept is purely directly generalised
	 * @return true if concept is purely directly generalised, false otherwise
	 */
	public boolean isPurelyDirectlyGeneralised() {
		if(isDirectlyGeneralised() && !isIndirectlyGeneralised())
			return true;
		else
			return false;
	}
	
	
	/**
	 * Check if concept is purely indirectly specialised
	 * @return true if concept is purely indirectly specialised, false otherwise
	 */
	public boolean isPurelyIndirectlySpecialised() {
		if(isIndirectlySpecialised() && !isDirectlySpecialised())
			return true;
		else
			return false;
	}
	
	
	/**
	 * Check if concept is purely indirectly generalised
	 * @return true if concept is purely indirectly generalised, false otherwise
	 */
	public boolean isPurelyIndirectlyGeneralised() {
		if(isIndirectlyGeneralised() && !isDirectlyGeneralised())
			return true;
		else
			return false;
	}
	
	
	/**
	 * Add an entailment (witness) axiom which is a consequence of an effectual axiom change (which in turn directly specialises the concept)
	 * @param effAx	Effectual axiom which gives rise to the entailment (witness) axiom change
	 * @param witnessAx	Entailment in the diff - the witness axiom
	 */
	public void addDirectSpecialisationWitnessForAxiom(OWLAxiom effAx, OWLAxiom witnessAx) {
		if(dirSpecWitsOfAx.containsKey(effAx)) {
			Set<OWLAxiom> wits = dirSpecWitsOfAx.get(effAx);
			wits.add(witnessAx);
			dirSpecWitsOfAx.put(effAx, wits);
		}
		else
			dirSpecWitsOfAx.put(effAx, new HashSet<OWLAxiom>(Collections.singleton(witnessAx)));
	}
	
	
	/**
	 * Add an entailment (witness) axiom which is a consequence of an effectual axiom change (which in turn directly generalises the concept)
	 * @param effAx	Effectual axiom which gives rise to the entailment (witness) axiom change
	 * @param witnessAx	Entailment in the diff - the witness axiom
	 */
	public void addDirectGeneralisationWitnessForAxiom(OWLAxiom effAx, OWLAxiom witnessAx) {
		if(dirGenWitsOfAx.containsKey(effAx)) {
			Set<OWLAxiom> wits = dirGenWitsOfAx.get(effAx);
			wits.add(witnessAx);
			dirGenWitsOfAx.put(effAx, wits);
		}
		else
			dirGenWitsOfAx.put(effAx, new HashSet<OWLAxiom>(Collections.singleton(witnessAx)));
	}
	
	
	/**
	 * Add an entailment (witness) axiom which is a consequence of an effectual axiom change (which in turn indirectly specialises the concept)
	 * @param effAx	Effectual axiom which gives rise to the entailment (witness) axiom change
	 * @param witnessAx	Entailment in the diff - the witness axiom
	 */
	public void addIndirectSpecialisationWitnessForAxiom(OWLAxiom effAx, OWLAxiom witnessAx) {
		if(indirSpecWitsOfAx.containsKey(effAx)) {
			Set<OWLAxiom> wits = indirSpecWitsOfAx.get(effAx);
			wits.add(witnessAx);
			indirSpecWitsOfAx.put(effAx, wits);
		}
		else
			indirSpecWitsOfAx.put(effAx, new HashSet<OWLAxiom>(Collections.singleton(witnessAx)));
	}
	
	
	/**
	 * Add an entailment (witness) axiom which is a consequence of an effectual axiom change (which in turn indirectly generalises the concept)
	 * @param effAx	Effectual axiom which gives rise to the entailment (witness) axiom change
	 * @param witnessAx	Entailment in the diff - the witness axiom
	 */
	public void addIndirectGeneralisationWitnessForAxiom(OWLAxiom effAx, OWLAxiom witnessAx) {
		if(indirGenWitsOfAx.containsKey(effAx)) {
			Set<OWLAxiom> wits = indirGenWitsOfAx.get(effAx);
			wits.add(witnessAx);
			indirGenWitsOfAx.put(effAx, wits);
		}
		else
			indirGenWitsOfAx.put(effAx, new HashSet<OWLAxiom>(Collections.singleton(witnessAx)));
	}
	
	
	/**
	 * Get the set of (direct specialisation) witness axioms for this concept change which are related to the given axiom 
	 * @param axiom	Effectual axiom change
	 * @return Set of witness axioms that are a consequence of the given axiom
	 */
	public Set<OWLAxiom> getDirectSpecialisationWitnessesForAxiom(OWLAxiom axiom) {
		return dirSpecWitsOfAx.get(axiom);
	}
	
	
	/**
	 * Get the set of (direct generalisation) witness axioms for this concept change which are related to the given axiom 
	 * @param axiom	Effectual axiom change
	 * @return Set of witness axioms that are a consequence of the given axiom
	 */
	public Set<OWLAxiom> getDirectGeneralisationWitnessesForAxiom(OWLAxiom axiom) {
		return dirGenWitsOfAx.get(axiom);
	}
	
	
	/**
	 * Get the set of (indirect specialisation) witness axioms for this concept change which are related to the given axiom 
	 * @param axiom	Effectual axiom change
	 * @return Set of witness axioms that are a consequence of the given axiom
	 */
	public Set<OWLAxiom> getIndirectSpecialisationWitnessesForAxiom(OWLAxiom axiom) {
		return indirSpecWitsOfAx.get(axiom);
	}
	
	
	/**
	 * Get the set of (indirect generalisation) witness axioms for this concept change which are related to the given axiom 
	 * @param axiom	Effectual axiom change
	 * @return Set of witness axioms that are a consequence of the given axiom
	 */
	public Set<OWLAxiom> getIndirectGeneralisationWitnessesForAxiom(OWLAxiom axiom) {
		return indirGenWitsOfAx.get(axiom);
	}
}
