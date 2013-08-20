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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import uk.ac.manchester.cs.diff.axiom.change.CategorisedChange;
import uk.ac.manchester.cs.diff.axiom.change.CategorisedEffectualAddition;
import uk.ac.manchester.cs.diff.axiom.change.CategorisedEffectualChange;
import uk.ac.manchester.cs.diff.axiom.change.CategorisedEffectualRemoval;
import uk.ac.manchester.cs.diff.axiom.change.CategorisedIneffectualAddition;
import uk.ac.manchester.cs.diff.axiom.change.CategorisedIneffectualAddition.IneffectualAdditionCategory;
import uk.ac.manchester.cs.diff.axiom.change.CategorisedIneffectualRemoval;
import uk.ac.manchester.cs.diff.axiom.change.CategorisedIneffectualRemoval.IneffectualRemovalCategory;
import uk.ac.manchester.cs.diff.axiom.changeset.CategorisedChangeSet;
import uk.ac.manchester.cs.diff.axiom.changeset.AxiomChangeSet;
import uk.ac.manchester.cs.diff.axiom.changeset.LogicalChangeSet;
import uk.ac.manchester.cs.diff.axiom.changeset.StructuralChangeSet;
import uk.ac.manchester.cs.diff.output.GenSymShortFormProvider;
import uk.ac.manchester.cs.diff.output.LabelShortFormProvider;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxObjectRenderer;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class XMLAxiomDiffReport implements XMLReport {
	protected final String uuid = UUID.randomUUID().toString();
	protected SimpleShortFormProvider sf;
	protected GenSymShortFormProvider gp;
	protected LabelShortFormProvider lp;
	protected Document doc, genSymDoc, labelDoc;
	protected HashMap<OWLEntity, String> genSymMap, labelMap;
	protected HashMap<OWLAxiom,Integer> axiomIds;
	protected OWLOntology ont1, ont2;
	protected OWLDataFactory df;
	protected DocumentBuilderFactory dbfac;
	protected DocumentBuilder docBuilder;
	protected AxiomChangeSet changeSet;
	protected int changeNr = 1;
	protected Set<OWLAxiom> sharedAxioms;
	
	/**
	 * Constructor
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 * @param changeSet	Change set
	 */
	public XMLAxiomDiffReport(OWLOntology ont1, OWLOntology ont2, AxiomChangeSet changeSet) {
		this.ont1 = ont1;
		this.ont2 = ont2;
		this.dbfac = DocumentBuilderFactory.newInstance();
		this.changeSet = changeSet;
		initMapsAndSFPs();
	}
		
	
	/**
	 * Initialise labels and gensyms maps, and short form providers
	 */
	private void initMapsAndSFPs() {
		try {
			this.docBuilder = dbfac.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		df = ont1.getOWLOntologyManager().getOWLDataFactory();
		genSymMap = new HashMap<OWLEntity,String>();
		labelMap = new HashMap<OWLEntity, String>();
		sf = new SimpleShortFormProvider();
		generateGenSyms(); 		// Prep gen syms output
		mapLabels(ont1); mapLabels(ont2);		// Prep labels output
		gp = new GenSymShortFormProvider(genSymMap);
		lp = new LabelShortFormProvider(labelMap);
	}
	
	
	/**
	 * Get the entity name based XML change report
	 * @return XML change report document
	 */
	public Document getXMLDocumentUsingTermNames() {
		doc = docBuilder.newDocument();
		prepDocument(doc, "");
		
		if(changeSet instanceof StructuralChangeSet)
			return getStructuralChangeSetReport((StructuralChangeSet)changeSet, doc, sf);
		else if(changeSet instanceof LogicalChangeSet)
			return getLogicalChangeSetReport((LogicalChangeSet)changeSet, doc, sf);
		else if(changeSet instanceof CategorisedChangeSet)
			return getCategorisedChangeSetReport((CategorisedChangeSet)changeSet, doc, sf);
		else
			throw new Error("Invalid change set");
	}
	
	
	/**
	 * Get the rdfs:label based XML change report
	 * @return XML change report document
	 */
	public Document getXMLDocumentUsingLabels() {	
		labelDoc = docBuilder.newDocument();
		prepDocument(labelDoc, "-lbl");
		if(changeSet instanceof StructuralChangeSet)
			return getStructuralChangeSetReport((StructuralChangeSet)changeSet, labelDoc, lp);
		else if(changeSet instanceof LogicalChangeSet)
			return getLogicalChangeSetReport((LogicalChangeSet)changeSet, labelDoc, lp);
		else if(changeSet instanceof CategorisedChangeSet)
			return getCategorisedChangeSetReport((CategorisedChangeSet)changeSet, labelDoc, lp);
		else
			throw new Error("Invalid change set");
	}
	
	
	/**
	 * Get the auto generated symbols based XML change report
	 * @return XML change report document
	 */
	public Document getXMLDocumentUsingGenSyms() {
		genSymDoc = docBuilder.newDocument();
		prepDocument(genSymDoc, "-gs");
		
		if(changeSet instanceof StructuralChangeSet)
			return getStructuralChangeSetReport((StructuralChangeSet)changeSet, genSymDoc, gp);
		else if(changeSet instanceof LogicalChangeSet)
			return getLogicalChangeSetReport((LogicalChangeSet)changeSet, genSymDoc, gp);
		else if(changeSet instanceof CategorisedChangeSet)
			return getCategorisedChangeSetReport((CategorisedChangeSet)changeSet, genSymDoc, gp);
		else
			throw new Error("Invalid change set");
	}
	
	
	/**
	 * Get the XML report of a structural change set
	 * @param changeSet	Structural change set
	 * @param doc	XML document
	 * @param sf	Short form provider
	 * @return XML report of a structural change set
	 */
	private Document getStructuralChangeSetReport(StructuralChangeSet changeSet, Document doc, ShortFormProvider sf) {
		if(axiomIds == null) axiomIds = new HashMap<OWLAxiom,Integer>();
		addElementAndChildren("Additions", "adds", changeSet.getAddedAxioms(), doc, "root", true, sf);
		addElementAndChildren("Removals", "rems", changeSet.getRemovedAxioms(), doc, "root", true, sf);
		addElementAndChildren("Shared", "shared", changeSet.getRemovedAxioms(), doc, "root", true, sf);
		return doc;
	}
	
	
	/**
	 * Get the XML report of a logical change set
	 * @param changeSet	Logical change set
	 * @param doc	XML document
	 * @param sf	Short form provider
	 * @return XML report of a logical change set
	 */
	private Document getLogicalChangeSetReport(LogicalChangeSet changeSet, Document doc, ShortFormProvider sf) {
		if(axiomIds == null) axiomIds = new HashMap<OWLAxiom,Integer>();
		
		addElement("Additions", "adds", changeSet.getAdditions().size(), doc, "root", true);
		addElementAndChildren("Effectual", "effadds", changeSet.getEffectualAdditionAxioms(), doc, "adds", true, sf);
		addElementAndChildren("Ineffectual", "ineffadds", changeSet.getIneffectualAdditionAxioms(), doc, "adds", true, sf);
		
		addElement("Removals", "rems", changeSet.getRemovals().size(), doc, "root", true);
		addElementAndChildren("Effectual", "effrems", changeSet.getEffectualRemovalAxioms(), doc, "rems", true, sf);
		addElementAndChildren("Ineffectual", "ineffrems", changeSet.getIneffectualRemovalAxioms(), doc, "rems", true, sf);
		return doc;
	}
	
	
	
	/**
	 * Get the XML report of a categorised change set
	 * @param changeSet	Categorised change set
	 * @param doc	XML document
	 * @param sf	Short form provider
	 * @return XML report of a categorised change set
	 */
	private Document getCategorisedChangeSetReport(CategorisedChangeSet changeSet, Document doc, ShortFormProvider sf) {
		sharedAxioms = changeSet.getSharedAxioms(); 
		if(axiomIds == null) axiomIds = new HashMap<OWLAxiom,Integer>();

		addElement("Additions", "adds", changeSet.getAdditions().size(), doc, "root", true);
		addElement("Removals", "rems", changeSet.getRemovals().size(), doc, "root", true);
		
		addElement("Effectual", "effadds", changeSet.getEffectualAdditions().size(), doc, "adds", true);
		addEffectualCategoryElementAndChildren(
				"Strengthening", "st", changeSet.getStrengthenings(), doc, "effadds", true, sf);
		addEffectualCategoryElementAndChildren(
				"StrengtheningWithNewTerms", "stnt", changeSet.getStrengtheningsWithNewTerms(), doc, "effadds", true, sf);
		addEffectualCategoryElementAndChildren(
				"NewDescription", "newdesc", changeSet.getNewDescriptions(), doc, "effadds", true, sf);
		addEffectualCategoryElementAndChildren(
				"PureAddition", "padd", changeSet.getPureAdditions(), doc, "effadds", true, sf);
		addEffectualCategoryElementAndChildren(
				"PureAdditionWithNewTerms", "paddnt", changeSet.getPureAdditionsWithNewTerms(), doc, "effadds", true, sf);
		addEffectualCategoryElementAndChildren(
				"NewModifiedDefinition", "stequiv", changeSet.getAddedModifiedDefinitions(), doc, "effadds", true, sf);
		addEffectualCategoryElementAndChildren(
				"NewModifiedDefinitionWithNewTerms", "stequivnt", changeSet.getAddedModifiedDefinitionsWithNewTerms(), doc, "effadds", true, sf);
		
		addElement("Ineffectual", "ineffadds", changeSet.getIneffectualAdditions().size(), doc, "adds", true);
		addIneffectualAdditions(
				"AddedRedundancy", "ared", changeSet.getAddedRedundancies(), doc, "ineffadds", true, IneffectualAdditionCategory.REDUNDANCY, sf);
		addElement("AddedRewrite", "arws", (changeSet.getAddedRewrites().size()+changeSet.getAddedPartialRewrites().size()), doc, "ineffadds", true);
		addIneffectualAdditions(
				"AddedCompleteRewrite", "arw", changeSet.getAddedRewrites(), doc, "arws", true, IneffectualAdditionCategory.REWRITE, sf);
		addIneffectualAdditions(
				"AddedPartialRewrite", "aprw", changeSet.getAddedPartialRewrites(), doc, "arws", true, IneffectualAdditionCategory.PREWRITE, sf);
		addElement("AddedProspectiveRedundancy", "apred", changeSet.getAddedProspectiveRedundancies().size(), doc, "ineffadds", true);
		addIneffectualAdditions(
				"AddedReshuffleProspectiveRedundancy", "aavred", changeSet.getAddedReshuffleRedundancies(), doc, "apred", true, 
				IneffectualAdditionCategory.RESHUFFLEREDUNDANCY, sf);
		addIneffectualAdditions(
				"AddedNewProspectiveRedundancy", "anpred", changeSet.getAddedProspectiveNewRedundancies(), doc, "apred", true, 
				IneffectualAdditionCategory.NEWPROSPREDUNDANCY, sf);
		
		addElement("Effectual", "effrems", changeSet.getEffectualRemovals().size(), doc, "rems", true);
		addEffectualCategoryElementAndChildren(
				"Weakening", "wk", changeSet.getWeakenings(), doc, "effrems", true, sf);
		addEffectualCategoryElementAndChildren(
				"WeakeningWithRetiredTerms", "wkrt", changeSet.getWeakeningsWithRetiredTerms(), doc, "effrems", true, sf);
		addEffectualCategoryElementAndChildren(
				"RetiredDescription", "retdesc", changeSet.getRetiredDescriptions(), doc, "effrems", true, sf);
		addEffectualCategoryElementAndChildren(
				"PureRemoval", "prem", changeSet.getPureRemovals(), doc, "effrems", true, sf);
		addEffectualCategoryElementAndChildren(
				"PureRemovalWithRetiredTerms", "premrt", changeSet.getPureRemovalsWithRetiredTerms(), doc, "effrems", true, sf);
		addEffectualCategoryElementAndChildren(
				"RetiredModifiedDefinition", "wkequiv", changeSet.getRemovedModifiedDefinitions(), doc, "effrems", true, sf);
		addEffectualCategoryElementAndChildren(
				"RetiredModifiedDefinitionWithRetiredTerms", "wkequivrt", changeSet.getRemovedModifiedDefinitionsWithRetiredTerms(), 
				doc, "effrems", true, sf);
		
		addElement("Ineffectual", "ineffrems", changeSet.getIneffectualRemovals().size(), doc, "rems", true);
		addIneffectualRemovals(
				"RemovedRedundancy", "rred", changeSet.getRemovedRedundancies(), doc, "ineffrems", true, IneffectualRemovalCategory.REDUNDANCY, sf);
		addElement("RemovedRewrite", "rrws", (changeSet.getRemovedRewrites().size()+changeSet.getRemovedPartialRewrites().size()), doc, "ineffrems", true);
		addIneffectualRemovals(
				"RemovedCompleteRewrite", "rrw", changeSet.getRemovedRewrites(), doc, "rrws", true, IneffectualRemovalCategory.REWRITE, sf);
		addIneffectualRemovals(
				"RemovedPartialRewrite", "rprw", changeSet.getRemovedPartialRewrites(), doc, "rrws", true, IneffectualRemovalCategory.PREWRITE, sf);
		addElement("RemovedProspectiveRedundancy", "rpred", changeSet.getRemovedProspectiveRedundancies().size(), doc, "ineffrems", true);
		addIneffectualRemovals(
				"RemovedReshuffleProspectiveRedundancy", "ravred", changeSet.getRemovedReshuffleRedundancies(), doc, "rpred", true, 
				IneffectualRemovalCategory.RESHUFFLEREDUNDANCY, sf);
		addIneffectualRemovals(
				"RemovedNewProspectiveRedundancy", "rnpred", changeSet.getRemovedProspectiveNewRedundancies(), doc, "rpred", true, 
				IneffectualRemovalCategory.NEWPROSPREDUNDANCY, sf);
		
		return doc;
	}
	
	
	
	/**
	 * Add a given element and, where appropriate, its children included in the specified set of axioms 
	 * @param name	Name of the element
	 * @param id	Id of the element
	 * @param set	Set of children axioms
	 * @param d	Document to be added to
	 * @param parent	Parent of the new element
	 * @param includeSize	Include the size of the children set as an attribute of the new element
	 * @param sf	Short form provider
	 */
	private void addElementAndChildren(String name, String id, Set<OWLAxiom> set, Document d, String parent, boolean includeSize, ShortFormProvider sf) {
		addElement(name, id, set.size(), d, parent, includeSize);
		List<OWLAxiom> orderedList = sortAxioms(set);
		for(OWLAxiom ax : orderedList) {
			if(axiomIds.containsKey(ax))
				addAxiomChange(axiomIds.get(ax) + "", ax, d, id, sf);
			else {
				addAxiomChange(changeNr + "", ax, d, id, sf);
				axiomIds.put(ax, changeNr);
				changeNr++;
			}
		}
	}
	
	
	/**
	 * Add a given element and, where appropriate, its children included in the specified set of axioms 
	 * @param name	Name of the element
	 * @param id	Id of the element
	 * @param set	Set of children axioms
	 * @param d	Document to be added to
	 * @param parent	Parent of the new element
	 * @param includeSize	Include the size of the children set as an attribute of the new element
	 * @param sf	Short form provider
	 */
	private void addEffectualCategoryElementAndChildren(String name, String id, Set<? extends CategorisedChange> set, Document d, 
			String parent, boolean includeSize, ShortFormProvider sf) {
		addElement(name, id, set.size(), d, parent, includeSize);
		List<? extends CategorisedChange> orderedList = sort(set, sf);
		for(CategorisedChange change : orderedList) {
			OWLAxiom ax = change.getAxiom();
			if(axiomIds.containsKey(ax))
				addAxiomChange(axiomIds.get(ax) + "", change, d, id, sf);
			else {
				addAxiomChange(changeNr + "", change, d, id, sf);
				axiomIds.put(ax, changeNr);
				changeNr++;
			}
		}
	}
	
	
	/**
	 * Add a given element and, where appropriate, its children included in the specified set of axioms 
	 * @param name	Name of the element
	 * @param id	Id of the element
	 * @param set	Set of children axioms
	 * @param d	Document to be added to
	 * @param parent	Parent of the new element
	 * @param includeSize	Include the size of the children set as an attribute of the new element
	 * @param sf	Short form provider
	 */
	private void addIneffectualRemovals(String name, String id, Set<? extends CategorisedChange> set, Document d, String parent, 
			boolean includeSize, IneffectualRemovalCategory cat, ShortFormProvider sf) {
		addElement(name, id, set.size(), d, parent, includeSize);
		List<? extends CategorisedChange> orderedList = sort(set, sf);
		for(CategorisedChange change : orderedList) {
			OWLAxiom ax = change.getAxiom();
			if(axiomIds.containsKey(ax))
				appendIneffectualRemoval(axiomIds.get(ax) + "", id, (CategorisedIneffectualRemoval)change, d, cat, sf);
			else {
				appendIneffectualRemoval(changeNr + "", id, (CategorisedIneffectualRemoval)change, d, cat, sf);
				axiomIds.put(ax, changeNr);
				changeNr++;
			}
		}
	}
	
	
	/**
	 * Add a given element and, where appropriate, its children included in the specified set of axioms 
	 * @param name	Name of the element
	 * @param id	Id of the element
	 * @param set	Set of children axioms
	 * @param d	Document to be added to
	 * @param parent	Parent of the new element
	 * @param includeSize	Include the size of the children set as an attribute of the new element
	 * @param sf	Short form provider
	 */
	private void addIneffectualAdditions(String name, String id, Set<? extends CategorisedChange> set, Document d, String parent, 
			boolean includeSize, IneffectualAdditionCategory cat, ShortFormProvider sf) {
		addElement(name, id, set.size(), d, parent, includeSize);
		List<? extends CategorisedChange> orderedList = sort(set, sf);
		for(CategorisedChange change : orderedList) {
			OWLAxiom ax = change.getAxiom();
			if(axiomIds.containsKey(ax))
				appendIneffectualAddition(axiomIds.get(ax) + "", id, (CategorisedIneffectualAddition)change, d, cat, sf);
			else {
				appendIneffectualAddition(changeNr + "", id, (CategorisedIneffectualAddition)change, d, cat, sf);
				axiomIds.put(ax, changeNr);
				changeNr++;
			}
		}
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
	public void addAxiomChange(String id, OWLAxiom axiom, Document d, String parent, ShortFormProvider sf) {
		Element ele = d.createElement("Change");
		ele.setAttribute("id", id);
		ele.setIdAttribute("id", true);
		
		Element root = d.getElementById(parent);
		root.appendChild(ele);

		Element axEle = d.createElement("Axiom");
		if(sharedAxioms != null && sharedAxioms.contains(axiom))
			axEle.setAttribute("shared", "true");
		
		axEle.setTextContent(getManchesterRendering(axiom, sf));
		ele.appendChild(axEle);
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
		
		appendEffectualChange((CategorisedEffectualChange)change, d, ele, sf);
	}
	
	
	/**
	 * Append an effectual change to the specified document report
	 * @param change	Change to be added
	 * @param d	Document to be updated
	 * @param ele	Parent element
	 * @param sf	Short form provider
	 */
	protected void appendEffectualChange(CategorisedEffectualChange c, Document d, Element ele, ShortFormProvider sf) {
		Set<OWLAxiom> aligns = c.getAxiomAlignment();
		if(!aligns.isEmpty()) {
			Element src = d.createElement("Source");
			ele.appendChild(src);
			for(OWLAxiom ax : aligns) {
				Element srcAx = d.createElement("Axiom");
				if(sharedAxioms != null && sharedAxioms.contains(ax))
					srcAx.setAttribute("shared", "true");
				srcAx.setTextContent(getManchesterRendering(ax, sf));
				src.appendChild(srcAx);
			}
		}
		if(!c.getDifferentTerms().isEmpty()) {
			Element terms = null;
			if(c instanceof CategorisedEffectualRemoval)
				terms = d.createElement("Retired_Terms");
			else if(c instanceof CategorisedEffectualAddition)
				terms = d.createElement("New_Terms");
			
			String diffEnts = getManchesterRendering(c.getDifferentTerms(), sf);
			terms.setTextContent(diffEnts);
			ele.appendChild(terms);
		}
	}
	
	
	/**
	 * Append an ineffectual change to the specified document report
	 * @param change	Change to be added
	 * @param d	Document to be updated
	 * @param ele	Parent element
	 * @param cat	Change category
	 * @param sf	Short form provider
	 */
	private void appendIneffectualAddition(String id, String parent, CategorisedIneffectualAddition change, Document d, IneffectualAdditionCategory cat, ShortFormProvider sf) {
		Element ele = d.createElement("Change");
		ele.setAttribute("id", id);
		ele.setIdAttribute("id", true);
		
		Element root = d.getElementById(parent);
		root.appendChild(ele);
		
		Element axEle = d.createElement("Axiom");
		OWLAxiom axiom = change.getAxiom();
		axEle.setTextContent(getManchesterRendering(axiom, sf));
		ele.appendChild(axEle);
		
		Map<Explanation<OWLAxiom>,Set<IneffectualAdditionCategory>> aligns = change.getJustificationMap();
		if(!aligns.isEmpty()) {
			for(Explanation<OWLAxiom> exp : aligns.keySet()) {
				for(IneffectualAdditionCategory catc : aligns.get(exp)) {
					if(catc.equals(cat)) {
						Element src = d.createElement("Source");
						ele.appendChild(src);
						for(OWLAxiom ax : exp.getAxioms()) {
							Element srcAx = d.createElement("Axiom");
							if(sharedAxioms != null && sharedAxioms.contains(ax))
								srcAx.setAttribute("shared", "true");
							else if(((CategorisedChangeSet)changeSet).getIneffectualRemovalAxioms().contains(ax))
								srcAx.setAttribute("ineffectual", "true");
							else if(((CategorisedChangeSet)changeSet).getEffectualRemovalAxioms().contains(ax))
								srcAx.setAttribute("effectual", "true");
							srcAx.setTextContent(getManchesterRendering(ax, sf));
							src.appendChild(srcAx);
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * Append an ineffectual change to the specified document report
	 * @param change	Change to be added
	 * @param d	Document to be updated
	 * @param ele	Parent element
	 * @param cat	Change category
	 * @param sf	Short form provider
	 */
	private void appendIneffectualRemoval(String id, String parent, CategorisedIneffectualRemoval change, Document d, IneffectualRemovalCategory cat, ShortFormProvider sf) {
		Element ele = d.createElement("Change");
		ele.setAttribute("id", id);
		ele.setIdAttribute("id", true);
		
		Element root = d.getElementById(parent);
		root.appendChild(ele);
		
		Element axEle = d.createElement("Axiom");
		OWLAxiom axiom = change.getAxiom();
		axEle.setTextContent(getManchesterRendering(axiom, sf));
		ele.appendChild(axEle);
		
		Map<Explanation<OWLAxiom>,Set<IneffectualRemovalCategory>> aligns = change.getJustificationMap();
		if(!aligns.isEmpty()) {
			for(Explanation<OWLAxiom> exp : aligns.keySet()) {
				for(IneffectualRemovalCategory catc : aligns.get(exp)) {
					if(catc.equals(cat)) {
						Element src = d.createElement("Source");
						ele.appendChild(src);
						for(OWLAxiom ax : exp.getAxioms()) {
							Element srcAx = d.createElement("Axiom");
							if(sharedAxioms != null && sharedAxioms.contains(ax))
								srcAx.setAttribute("shared", "true");
							else if(((CategorisedChangeSet)changeSet).getIneffectualAdditionAxioms().contains(ax))
								srcAx.setAttribute("ineffectual", "true");
							else if(((CategorisedChangeSet)changeSet).getEffectualAdditionAxioms().contains(ax))
								srcAx.setAttribute("effectual", "true");
							srcAx.setTextContent(getManchesterRendering(ax, sf));
							src.appendChild(srcAx);
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * Add element with given name and Id to an XML document
	 * @param name	Name of the element
	 * @param id	Id of the element
	 * @param size	Size of the elements children (axioms)
	 * @param d	Document to be added to
	 * @param parent	Parent of this new element
	 * @param includeSize	Include the size of the children as an attribute of the new element
	 */
	private void addElement(String name, String id, int size, Document d, String parent, boolean includeSize) {
		Element ele = d.createElement(name);
		if(includeSize)
			ele.setAttribute("size", "" + size);
	
		ele.setAttribute("id", id);
		ele.setIdAttribute("id", true);

		Element root = d.getElementById(parent);
		root.appendChild(ele);
	}

	
	/**
	 * Prepare output XML document
	 * @param d	Document to prepare
	 * @param suffix	Suffix for the document identifier
	 */
	private void prepDocument(Document d, String suffix) {
		Element root = d.createElement("root");
		root.setAttribute("id", "root");
		root.setIdAttribute("id", true);
		
		String id = uuid + suffix;
		root.setAttribute("uuid", id);
		d.appendChild(root);
	}
	
	
	/**
	 * Get Manchester syntax of an OWL object
	 * @param obj	OWL object
	 * @param sf	Short form provider
	 * @return A string with the object's conversion to Manchester syntax 
	 */
	protected String getManchesterRendering(OWLObject obj, ShortFormProvider sf) {
		StringWriter wr = new StringWriter();
		ManchesterOWLSyntaxObjectRenderer render = new ManchesterOWLSyntaxObjectRenderer(wr, sf);
		obj.accept(render);

		String str = wr.getBuffer().toString();
		str = str.replace("<", "");
		str = str.replace(">", "");
		return str;
	}
	
	
	/**
	 * Get a string with the given set of entities rendered in Manchester syntax, and comma-separated
	 * @param entities	Set of entities
	 * @param sf	Short form provider
	 * @return String with entities rendered in Manchester syntax, comma-separated
	 */
	private String getManchesterRendering(Set<OWLEntity> entities, ShortFormProvider sf) {
		String out = "";
		OWLEntity[] arr = entities.toArray(new OWLEntity[entities.size()]);
		for(int i = 0; i < arr.length; i++) {
			OWLEntity e = arr[i];
			if(!e.isTopEntity() && !e.isBottomEntity()) {
				if(i < 1) 
					out += sf.getShortForm(e);
				else
					out += ", " + sf.getShortForm(e);
			}
		}
		out = out.replace("<", "");
		out = out.replace(">", "");
		return out;
	}
	
	
	/**
	 * Get XML document as a string
	 * @param doc	XML document
	 * @return String version of the XML document
	 * @throws TransformerException 
	 */
	public String getReportAsString(Document doc) throws TransformerException {
		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
		return getXMLAsString(trans, doc);
	}
	
	
	/**
	 * Get XML document transformed into HTML as a string
	 * @param doc	XML document
	 * @param xsltPath	Path to the XSL Transformation file
	 * @return String containing the HTML transformation
	 * @throws TransformerException 
	 */
	public String getReportAsHTML(Document doc, String xsltPath) throws TransformerException {
		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer(new StreamSource(xsltPath));
		return getXMLAsString(trans, doc);
	}
	
	
	/**
	 * Transform the document using the given transformer and return the transformation as a string
	 * @param trans	Transformer
	 * @param doc	XML document
	 * @return String result of transforming the XML document
	 * @throws TransformerException
	 */
	private String getXMLAsString(Transformer trans, Document doc) throws TransformerException {
		trans.setOutputProperty(OutputKeys.INDENT, "yes");

		// Create string from XML tree
		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		DOMSource source = new DOMSource(doc);

		trans.transform(source, result);
		return sw.toString();
	}
	
	
	/**
	 * Map entities to their respective rdfs:label, where applicable
	 * @param ont	Ontology
	 */
	private void mapLabels(OWLOntology ont) {
		Set<OWLEntity> ents = ont.getSignature();
		for(OWLEntity e : ents) {
			Set<OWLAnnotation> labels = e.getAnnotations(ont, df.getRDFSLabel());
			if(!labels.isEmpty()) {
				for(OWLAnnotation a : labels) {
					String entry = a.getValue().toString();
					if(entry.startsWith("\"")) {
						entry = entry.substring(1);
						entry = entry.substring(0, entry.indexOf("\""));
					}
					if(!entry.equals(""))
						labelMap.put(e, entry);
					else
						labelMap.put(e, sf.getShortForm(e));
				}
			}
			else labelMap.put(e, sf.getShortForm(e));
		}
	}
	
	
	/**
	 * Sort a given set of categorised changes into a list
	 * @param set	Set of changes
	 * @param sf	Short form provider
	 * @return List of ordered categorised changes
	 */
	private List<? extends CategorisedChange> sort(Set<? extends CategorisedChange> set, ShortFormProvider sf) {
		Map<String,CategorisedChange> map = new HashMap<String,CategorisedChange>();
		for(CategorisedChange c : set) {
			String ax = getManchesterRendering(c.getAxiom(), sf);
			map.put(ax, c);
		}
		List<String> axStrings = new ArrayList<String>(map.keySet());
		Collections.sort(axStrings);
		
		List<CategorisedChange> output = new ArrayList<CategorisedChange>();
		for(String s : axStrings) {
			output.add(map.get(s));
		}
		return output;
	}
	
	
	/**
	 * Sort a given set of axioms into a list
	 * @param set	Set of axioms
	 * @param sf	Short form provider
	 * @return List of ordered axioms 
	 */
	private List<OWLAxiom> sortAxioms(Set<OWLAxiom> set) {
		Map<String,OWLAxiom> map = new HashMap<String,OWLAxiom>();
		for(OWLAxiom axiom : set) {
			String ax = getManchesterRendering(axiom, sf);
			map.put(ax, axiom);
		}
		List<String> axStrings = new ArrayList<String>(map.keySet());
		Collections.sort(axStrings);
		
		List<OWLAxiom> output = new ArrayList<OWLAxiom>();
		for(String s : axStrings) {
			output.add(map.get(s));
		}
		return output;
	}
	
	
	/**
	 * Generate GenSyms for entities
	 */
	private void generateGenSyms() {
		HashSet<OWLEntity> entSet = new HashSet<OWLEntity>();
		entSet.addAll(ont1.getSignature());
		entSet.addAll(ont2.getSignature());
		
		int classCounter = 0, propCounter = 0, indCounter = 0;
		char curChar1 = 'A', curChar2 = 'A', curChar3 = 'A';
		boolean twoChars = false, threeChars = false;
		
		for(OWLEntity e : entSet) {
			if(e.isOWLClass()) {
				if(!twoChars && !threeChars) {
					classCounter++;
					String base = "" + curChar1 + classCounter;
					genSymMap.put(e, base);
					if(curChar1 == 'Z' && classCounter == 9) {
						twoChars = true; curChar1 = 'A'; classCounter = 0;
					}
					else if(classCounter == 9) {
						curChar1++; classCounter = 0;
					}
				}
				else if(twoChars) {
					classCounter++;
					String base = "" + curChar1 + curChar2 + classCounter;
					genSymMap.put(e, base);

					if(curChar1 == 'Z' && curChar2 == 'Z' && classCounter == 9) {
						threeChars = true; twoChars = false; curChar1 = 'A'; curChar2 = 'A'; classCounter = 0;
					}
					else if(curChar2 == 'Z' && classCounter == 9) {
						curChar1 ++; curChar2 = 'A'; classCounter = 0;
					}
					else if(classCounter == 9) {
						curChar2++; classCounter = 0;
					}
				}
				else if(threeChars) {
					classCounter++;
					String base = "" + curChar1 + curChar2 + curChar3 + classCounter;
					genSymMap.put(e, base);
					if(curChar2 == 'Z' && curChar3 == 'Z' && classCounter == 9) {
						curChar1++; curChar2 = 'A'; curChar3 = 'A'; classCounter = 0;
					}
					else if(curChar3 == 'Z' && classCounter == 9) {
						curChar2 ++; curChar3 = 'A'; classCounter = 0;
					}
					else if(classCounter == 9) {
						curChar3++; classCounter = 0;
					}
				}
			}
			else if(e.isOWLDataProperty() || e.isOWLObjectProperty() || e.isOWLAnnotationProperty()) {
				propCounter++; genSymMap.put(e, "prop" + propCounter);
			}
			else if(e.isOWLNamedIndividual()) {
				indCounter++; genSymMap.put(e, "ind" + indCounter);
			}
		}
	}
}
