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
package uk.ac.manchester.cs.diff.output;

import java.io.StringWriter;
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

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import uk.ac.manchester.cs.diff.concept.change.ConceptChange;
import uk.ac.manchester.cs.diff.concept.changeset.ConceptChangeSet;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxObjectRenderer;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class XMLReportConceptDiff {
	private final String uuid = UUID.randomUUID().toString();
	private ConceptChangeSet changeSet;
	private DocumentBuilderFactory dbfac;
	private DocumentBuilder docBuilder;
	private SimpleShortFormProvider sf;
	private Document doc;
	
	
	/**
	 * Constructor
	 * @param changeSet	Concept diff change set
	 */
	public XMLReportConceptDiff(ConceptChangeSet changeSet) {
		this.changeSet = changeSet;
		sf = new SimpleShortFormProvider();
		dbfac = DocumentBuilderFactory.newInstance();
	}
	
	
	/**
	 * Get diff report as an XML document
	 * @return XML document
	 */
	public Document getReport() {
		try { this.docBuilder = dbfac.newDocumentBuilder(); } 
		catch (ParserConfigurationException e) { e.printStackTrace(); }
		doc = docBuilder.newDocument();
		prepDocument(doc, "");
		populateDocument();
		return doc;
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
	 * Populate the XML document with ontology 1, 2, and overall changes
	 */
	private void populateDocument() {
		addElement("Ontology1", "lhs", changeSet.getLHSAffectedConcepts().size(), doc, "root", true);
		addElement("Ontology2", "rhs", changeSet.getRHSAffectedConcepts().size(), doc, "root", true);
		addLHSConceptDiff("lhs", doc);
		addRHSConceptDiff("rhs", doc);
	}
	
	
	/**
	 * Add ontology 1 (LHS) affected concepts
	 * @param parent	Parent element
	 * @param d	XML document
	 */
	private void addLHSConceptDiff(String parent, Document d) {
		addElement("Specialised", "lhs_spec", changeSet.getLHSSpecialisedConcepts().size(), d, parent, true);
		addSpecialisationChanges("PurelyDirect", "lhs_pds", changeSet.getLHSPurelyDirectlySpecialised(), d, "lhs_spec");
		addSpecialisationChanges("PurelyIndirect", "lhs_pis", changeSet.getLHSPurelyIndirectlySpecialised(), d, "lhs_spec");
		addSpecialisationChanges("Mixed", "lhs_ms", changeSet.getLHSMixedSpecialised(), d, "lhs_spec");
		
		addElement("Generalised", "lhs_gen", changeSet.getLHSGeneralisedConcepts().size(), d, parent, true);
		addGeneralisationChanges("PurelyDirect", "lhs_pdg", changeSet.getLHSPurelyDirectlyGeneralised(), d, "lhs_gen");
		addGeneralisationChanges("PurelyIndirect", "lhs_pig", changeSet.getLHSPurelyIndirectlyGeneralised(), d, "lhs_gen");
		addGeneralisationChanges("Mixed", "lhs_mg", changeSet.getLHSMixedGeneralised(), d, "lhs_gen");
	}
	
	/**
	 * Add ontology 2 (RHS) affected concepts
	 * @param parent	Parent element
	 * @param d	XML document
	 */	
	private void addRHSConceptDiff(String parent, Document d) {
		addElement("Specialised", "rhs_spec", changeSet.getRHSSpecialisedConcepts().size(), d, parent, true);
		addSpecialisationChanges("PurelyDirect", "rhs_pds", changeSet.getRHSPurelyDirectlySpecialised(), d, "rhs_spec");
		addSpecialisationChanges("PurelyIndirect", "rhs_pis", changeSet.getRHSPurelyIndirectlySpecialised(), d, "rhs_spec");
		addSpecialisationChanges("Mixed", "rhs_ms", changeSet.getRHSMixedSpecialised(), d, "rhs_spec");
		
		addElement("Generalised", "rhs_gen", changeSet.getRHSGeneralisedConcepts().size(), d, parent, true);
		addGeneralisationChanges("PurelyDirect", "rhs_pdg", changeSet.getRHSPurelyDirectlyGeneralised(), d, "rhs_gen");
		addGeneralisationChanges("PurelyIndirect", "rhs_pig", changeSet.getRHSPurelyIndirectlyGeneralised(), d, "rhs_gen");
		addGeneralisationChanges("Mixed", "rhs_mg", changeSet.getRHSMixedGeneralised(), d, "rhs_gen");
	}
	
	
	/**
	 * Add generalisation changes to the given document
	 * @param desc	Change group element name  
	 * @param id	Id for the element above
	 * @param changes	Set of concept changes
	 * @param d	XML Document
	 * @param parent	Parent element of the change group
	 */
	private void addGeneralisationChanges(String desc, String id, Set<? extends ConceptChange> changes, Document d, String parent) {
		addElement(desc, id, changes.size(), d, parent, true);
		for(ConceptChange c : changes) {
			Element change = addConceptChange(c, d, id);
			if(c.isDirectlyGeneralised() && c.isIndirectlyGeneralised()) {
				addWitnessAxioms(change, c.getDirectGeneralisationWitnesses(), d, true);
				addWitnessAxioms(change, c.getIndirectGeneralisationWitnesses(), d, false);
			}
			else if(c.isDirectlyGeneralised())
				addWitnessAxioms(change, c.getDirectGeneralisationWitnesses(), d, true);
			else if(c.isIndirectlyGeneralised())
				addWitnessAxioms(change, c.getIndirectGeneralisationWitnesses(), d, false);
		}
	}
	
	
	/**
	 * Add specialisation changes to the given document
	 * @param desc	Change group element name  
	 * @param id	Id for the element above
	 * @param changes	Set of concept changes
	 * @param d	XML Document
	 * @param parent	Parent element of the change group
	 */
	private void addSpecialisationChanges(String desc, String id, Set<? extends ConceptChange> changes, Document d, String parent) {
		addElement(desc, id, changes.size(), d, parent, true);
		for(ConceptChange c : changes) {
			Element change = addConceptChange(c, d, id);
			if(c.isDirectlySpecialised() && c.isIndirectlySpecialised()) {
				addWitnessAxioms(change, c.getDirectSpecialisationWitnesses(), d, true);
				addWitnessAxioms(change, c.getIndirectSpecialisationWitnesses(), d, false);
			}
			else if(c.isDirectlySpecialised())
				addWitnessAxioms(change, c.getDirectSpecialisationWitnesses(), d, true);
			else if(c.isIndirectlySpecialised())
				addWitnessAxioms(change, c.getIndirectSpecialisationWitnesses(), d, false);
		}
	}
	
	
	/**
	 * Add a concept change element
	 * @param c	Concept change
	 * @param d	XML Document
	 * @param parent	Parent element id
	 * @return WitnessAxioms element
	 */
	private Element addConceptChange(ConceptChange c, Document d, String parent) {
		Element cChange = d.createElement("ConceptChange");
		Element root = d.getElementById(parent);
		root.appendChild(cChange);
		
		Element cName = d.createElement("ConceptName");
		cName.setTextContent(getManchesterRendering(c.getConcept(), sf));
		cChange.appendChild(cName);
		
		Element wits = d.createElement("WitnessAxioms");
		cChange.appendChild(wits);
		return wits;
	}
	
	
	/**
	 * Add a set of witness axioms to the WitnessAxioms element specified
	 * @param parent	WitnessAxioms element
	 * @param witnesses	Set of witness axioms
	 * @param d	XML Document
	 * @param direct	true if given witness axioms are for a direct change
	 */
	private void addWitnessAxioms(Element parent, Set<OWLAxiom> witnesses, Document d, boolean direct) {
		for(OWLAxiom ax : witnesses) {
			Element axEle = d.createElement("Axiom");
			axEle.setAttribute("direct", "" + direct);
			axEle.setTextContent(getManchesterRendering(ax, sf));
			parent.appendChild(axEle);
		}
		parent.setAttribute("size", ""+witnesses.size());
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
	private Element addElement(String name, String id, int size, Document d, String parent, boolean includeSize) {
		Element ele = d.createElement(name);
		if(includeSize)
			ele.setAttribute("size", "" + size);
	
		ele.setAttribute("id", id);
		ele.setIdAttribute("id", true);

		Element root = d.getElementById(parent);
		root.appendChild(ele);
		return ele;
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
	private String getManchesterRendering(OWLObject obj, ShortFormProvider sf) {
		StringWriter wr = new StringWriter();
		ManchesterOWLSyntaxObjectRenderer render = new ManchesterOWLSyntaxObjectRenderer(wr, sf);
		obj.accept(render);

		String str = wr.getBuffer().toString();
		str = str.replace("<", "");
		str = str.replace(">", "");
		return str;
	}
}