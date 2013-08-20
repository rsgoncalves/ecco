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
package uk.ac.manchester.cs.diff.output.xml;

import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import uk.ac.manchester.cs.diff.alignment.AlignedDirectChangeSet;
import uk.ac.manchester.cs.diff.alignment.AlignedIndirectChangeSet;
import uk.ac.manchester.cs.diff.axiom.change.CategorisedChange;
import uk.ac.manchester.cs.diff.axiom.change.CategorisedEffectualChange;
import uk.ac.manchester.cs.diff.axiom.changeset.AxiomChangeSet;
import uk.ac.manchester.cs.diff.concept.change.ConceptChange;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class XMLUnifiedReport extends XMLAxiomDiffReport {
	private Map<OWLAxiom,Set<ConceptChange>> ont1DirSpec, ont1DirGen, ont2DirSpec, ont2DirGen;
	private Map<OWLAxiom,Set<ConceptChange>> ont1IndirSpec, ont1IndirGen, ont2IndirSpec, ont2IndirGen;
	
	/**
	 * Constructor
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 * @param changeSet	Change set
	 * @param directChanges	Aligned direct change set
	 * @param indirectChanges	Aligned indirect change set
	 */
	public XMLUnifiedReport(OWLOntology ont1, OWLOntology ont2, AxiomChangeSet changeSet, 
			AlignedDirectChangeSet directChanges, AlignedIndirectChangeSet indirectChanges) {
		super(ont1, ont2, changeSet);
		assignMaps(directChanges, indirectChanges);
	}
	
	
	/**
	 * Get the direct and indirect change maps
	 * @param directChanges	Aligned direct change set
	 * @param indirectChanges	Aligned indirect change set
	 */
	private void assignMaps(AlignedDirectChangeSet directChanges, AlignedIndirectChangeSet indirectChanges) {
		ont1DirSpec = directChanges.getOnt1SpecialisationsMap();
		ont1DirGen = directChanges.getOnt1GeneralisationsMap();
		ont2DirSpec = directChanges.getOnt2SpecialisationsMap();
		ont2DirGen = directChanges.getOnt2GeneralisationsMap();
		
		ont1IndirSpec = indirectChanges.getOnt1SpecialisationsMap();
		ont1IndirGen = indirectChanges.getOnt1GeneralisationsMap();
		ont2IndirSpec = indirectChanges.getOnt2SpecialisationsMap();
		ont2IndirGen = indirectChanges.getOnt2GeneralisationsMap();
	}
	
	
	/**
	 * Add a change element, which contains an axiom child element
	 * @param name	Name of the change element
	 * @param id	Id of the change element
	 * @param axiom	Child axiom of this change
	 * @param d	Document to be added to
	 * @param parent	Parent element of the change element
	 * @param sf	Short form provider
	 */
	@Override
	public void addAxiomChange(String id, CategorisedChange change, Document d, String parent, ShortFormProvider sf) {
		Element ele = d.createElement("Change");
		ele.setAttribute("id", id);
		ele.setIdAttribute("id", true);
		
		Element root = d.getElementById(parent);
		root.appendChild(ele);
		
		Element axEle = d.createElement("Axiom");
		OWLAxiom axiom = change.getAxiom();
		axEle.setTextContent(getManchesterRendering(axiom, sf));
		ele.appendChild(axEle);
		checkAndAddEffect(axiom, d, ele, sf);
		
		appendEffectualChange((CategorisedEffectualChange)change, d, ele, sf);
	}
	
	
	/**
	 * Check whether the axiom affected any concepts, and if so add the affected concepts
	 * @param axiom	Axiom being checked
	 * @param d	XML document
	 * @param parent	Parent element to append the effect, if applicable
	 * @param sf	Short form provider
	 */
	private void checkAndAddEffect(OWLAxiom axiom, Document d, Element parent, ShortFormProvider sf) {
		Element effEle = d.createElement("ConceptChanges");
		
		Element direct = d.createElement("DirectChanges");
		Element indirect = d.createElement("IndirectChanges");
		
		int nrDir = addDirectChanges(axiom, direct, d, sf);
		direct.setAttribute("size", "" + nrDir);
		
		int nrIndir = addIndirectChanges(axiom, indirect, d, sf);
		indirect.setAttribute("size", "" + nrIndir);
		
		int total = nrDir + nrIndir;
		effEle.setAttribute("size", "" + total);
		effEle.appendChild(direct);
		effEle.appendChild(indirect);
		
		parent.appendChild(effEle);
	}
	
	
	/**
	 * Add direct concept changes caused by the given axiom, and get the total number of changes
	 * @param axiom	Axiom
	 * @param parent	Parent element in the document
	 * @param d	XML document
	 * @param sf	Short form provider
	 * @return Number of direct concept changes caused by the given axiom
	 */
	private int addDirectChanges(OWLAxiom axiom, Element parent, Document d, ShortFormProvider sf) {
		int nrChanges = 0;
		if(ont1DirSpec.containsKey(axiom)) {
			Set<ConceptChange> changes = ont1DirSpec.get(axiom);
			nrChanges += changes.size();
			addConceptChanges(changes, "Specialisation", true, parent, d, sf);
		}
		if(ont1DirGen.containsKey(axiom)) {
			Set<ConceptChange> changes = ont1DirGen.get(axiom);
			nrChanges += changes.size();
			addConceptChanges(changes, "Generalisation", true, parent, d, sf);
		}
		if(ont2DirSpec.containsKey(axiom)) {
			Set<ConceptChange> changes = ont2DirSpec.get(axiom);
			nrChanges += changes.size();
			addConceptChanges(changes, "Specialisation", true, parent, d, sf);
		}
		if(ont2DirGen.containsKey(axiom)) {
			Set<ConceptChange> changes = ont2DirGen.get(axiom);
			nrChanges += changes.size();
			addConceptChanges(changes, "Generalisation", true, parent, d, sf);
		}
		return nrChanges;
	}
	
	
	/**
	 * Add indirect concept changes caused by the given axiom, and get the total number of changes
	 * @param axiom	Axiom
	 * @param parent	Parent element in the document
	 * @param d	XML document
	 * @param sf	Short form provider
	 * @return Number of indirect concept changes caused by the given axiom
	 */
	private int addIndirectChanges(OWLAxiom axiom, Element parent, Document d, ShortFormProvider sf) {
		int nrChanges = 0;
		if(ont1IndirSpec.containsKey(axiom)) {
			Set<ConceptChange> changes = ont1IndirSpec.get(axiom);
			nrChanges += changes.size();
			addConceptChanges(changes, "Specialisation", false, parent, d, sf);
		}
		if(ont1IndirGen.containsKey(axiom)) {
			Set<ConceptChange> changes = ont1IndirGen.get(axiom);
			nrChanges += changes.size();
			addConceptChanges(changes, "Generalisation", false, parent, d, sf);
		}
		if(ont2IndirSpec.containsKey(axiom)) {
			Set<ConceptChange> changes = ont2IndirSpec.get(axiom);
			nrChanges += changes.size();
			addConceptChanges(changes, "Specialisation", false, parent, d, sf);
		}
		if(ont2IndirGen.containsKey(axiom)) {
			Set<ConceptChange> changes = ont2IndirGen.get(axiom);
			nrChanges += changes.size();
			addConceptChanges(changes, "Generalisation", false, parent, d, sf);
		}
		return nrChanges;
	}
	
	
	/**
	 * Add set of concepts to the given element
	 * @param concepts	Set of concepts
	 * @param parent	Element to append concept elements to
	 * @param d	XML document
	 * @param sf	Short form provider
	 */
	private void addConceptChanges(Set<ConceptChange> concepts, String type, boolean direct, Element parent, Document d, ShortFormProvider sf) {
		for(ConceptChange c : concepts) {
			Element change = d.createElement(type);
			Element concept = d.createElement("Concept");
			concept.setTextContent(getManchesterRendering(c.getConcept(), sf));
			change.appendChild(concept);
			
			Set<OWLAxiom> witnesses = null;
			if(type.equalsIgnoreCase("Specialisation")) {
				if(direct)
					witnesses = c.getDirectSpecialisationWitnesses();
				else
					witnesses = c.getIndirectSpecialisationWitnesses();
			}
			else if(type.equals("Generalisation")) {
				if(direct)
					witnesses = c.getDirectGeneralisationWitnesses();
				else
					witnesses = c.getIndirectGeneralisationWitnesses();
			}
			addWitnesses(witnesses, change, d, sf);
			parent.appendChild(change);
		}
	}
	
	
	/**
	 * Add a set of witnesses to a concept change
	 * @param witnesses	Set of witnesses for a change to some concept
	 * @param parent	Parent element to append to
	 * @param d	XML document
	 * @param sf	Short form provider
	 */
	private void addWitnesses(Set<OWLAxiom> witnesses, Element parent, Document d, ShortFormProvider sf) {
		Element witEle = d.createElement("WitnessAxioms");
		for(OWLAxiom ax : witnesses) {
			Element axEle = d.createElement("Axiom");
			axEle.setTextContent(getManchesterRendering(ax, sf));
			witEle.appendChild(axEle);
		}
		parent.appendChild(witEle);
	}
}
