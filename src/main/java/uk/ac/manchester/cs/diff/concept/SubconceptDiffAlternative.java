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
package uk.ac.manchester.cs.diff.concept;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.w3c.dom.Document;

import uk.ac.manchester.cs.diff.concept.change.ConceptChange;
import uk.ac.manchester.cs.diff.concept.change.LHSConceptChange;
import uk.ac.manchester.cs.diff.concept.change.RHSConceptChange;
import uk.ac.manchester.cs.diff.concept.changeset.ConceptChangeSet;
import uk.ac.manchester.cs.diff.concept.changeset.DiffResult;
import uk.ac.manchester.cs.diff.concept.changeset.WitnessAxioms;
import uk.ac.manchester.cs.diff.concept.changeset.WitnessPack;
import uk.ac.manchester.cs.diff.output.xml.XMLConceptDiffReport;

/**
 * @author Rafael S. Goncalves <br>
 * Information Management Group (IMG) <br>
 * School of Computer Science <br>
 * University of Manchester <br>
 */
public class SubconceptDiffAlternative {
	private File ont1, ont2;
	private String outputDir;
	private boolean verbose;

	/**
	 * Constructor for subconcept diff w.r.t. Sigma = sig(O1) U sig(O2)
	 * @param ont1	Ontology 1 file
	 * @param ont2	Ontology 2 file
	 * @param outputDir	Output directory
	 * @param verbose	Verbose mode
	 */
	public SubconceptDiffAlternative(File ont1, File ont2, String outputDir, boolean verbose) {
		this.ont1 = ont1;
		this.ont2 = ont2;
		this.outputDir = outputDir;
		this.verbose = verbose;
	}
	

	/**
	 * Distinguish between directly and indirectly affected concepts
	 * @return Concept-based change set
	 * @throws ExecutionException	Execution exception
	 * @throws InterruptedException	Interruption exception
	 */
	public ConceptChangeSet getDiff() throws InterruptedException, ExecutionException {
		long start = System.currentTimeMillis();
		Set<OWLClass> affected = new HashSet<OWLClass>();
		
		if(verbose) System.out.println("Initialising diff workers...");
		ExecutorService exec = Executors.newCachedThreadPool();
		
		Diff diffL = new Diff(ont1, ont2, "L", verbose);
		Diff diffR = new Diff(ont1, ont2, "R", verbose);
		Future<DiffResult> diffLfuture = exec.submit(diffL); 
		Future<DiffResult> diffRfuture = exec.submit(diffR);
		
		DiffResult diffLresults = diffLfuture.get();
		DiffResult diffRresults = diffRfuture.get();
		exec.shutdown();
		if(verbose) System.out.println("done (" + (System.currentTimeMillis()-start)/1000.0 + " secs)");

		
		Set<RHSConceptChange> rhsConceptChanges = new HashSet<RHSConceptChange>();
		Set<LHSConceptChange> lhsConceptChanges = new HashSet<LHSConceptChange>();
		Set<ConceptChange> conceptChanges = new HashSet<ConceptChange>();
		WitnessPack lhs_spec = diffLresults.getLHSWitnessPack();
		WitnessPack lhs_gen = diffRresults.getLHSWitnessPack();
		WitnessPack rhs_spec = diffLresults.getRHSWitnessPack();
		WitnessPack rhs_gen = diffRresults.getRHSWitnessPack();

		affected.addAll(diffL.getAffectedConcepts());
		affected.addAll(diffR.getAffectedConcepts());
		
		for(OWLClass c : affected) {
			WitnessAxioms ls = new WitnessAxioms(lhs_spec.getDirectWitnesses(c), lhs_spec.getIndirectWitnesses(c));
			WitnessAxioms rs = new WitnessAxioms(rhs_spec.getDirectWitnesses(c), rhs_spec.getIndirectWitnesses(c));

			WitnessAxioms lg = new WitnessAxioms(lhs_gen.getDirectWitnesses(c), lhs_gen.getIndirectWitnesses(c));
			WitnessAxioms rg = new WitnessAxioms(rhs_gen.getDirectWitnesses(c), rhs_gen.getIndirectWitnesses(c));

			if(!ls.isEmpty() || !lg.isEmpty())
				lhsConceptChanges.add(new LHSConceptChange(c, ls, lg));

			if(!rs.isEmpty() || !rg.isEmpty())
				rhsConceptChanges.add(new RHSConceptChange(c, rs, rg));

			// Create overall change
			if(!ls.isEmpty() || !lg.isEmpty() || !rs.isEmpty() || !rg.isEmpty()) {
				Set<OWLAxiom> dirSpec = new HashSet<OWLAxiom>(ls.getDirectWitnesses());
				dirSpec.addAll(rs.getDirectWitnesses());

				Set<OWLAxiom> indirSpec = new HashSet<OWLAxiom>(ls.getIndirectWitnesses());
				indirSpec.addAll(rs.getIndirectWitnesses());

				Set<OWLAxiom> dirGen = new HashSet<OWLAxiom>(lg.getDirectWitnesses());
				dirGen.addAll(rg.getDirectWitnesses());

				Set<OWLAxiom> indirGen = new HashSet<OWLAxiom>(lg.getIndirectWitnesses());
				indirGen.addAll(rg.getIndirectWitnesses());

				ConceptChange change = new ConceptChange(c, dirSpec, indirSpec, dirGen, indirGen);
				conceptChanges.add(change);
			}
		}
		ConceptChangeSet changeSet = new ConceptChangeSet(lhsConceptChanges, rhsConceptChanges, conceptChanges);
		if(verbose) printDiff(changeSet);
		long end = System.currentTimeMillis();
		System.out.println("finished (total diff time: " + (end-start)/1000.0 + " secs)");
		return changeSet;
	}
	

	/**
	 * Print diff
	 * @param changeSet	Concept-based change set
	 */
	public void printDiff(ConceptChangeSet changeSet) {
		System.out.println("\nSubconcept diff results:");
		System.out.println("  [ont1]" +
				"\tSpecialised: " + changeSet.getLHSSpecialisedConcepts().size() + 
				"\tGeneralised: " + changeSet.getLHSGeneralisedConcepts().size() +
				"\tTotal affected: " + changeSet.getLHSAffectedConcepts().size());
		System.out.println("  [ont2]" +
				"\tSpecialised: " + changeSet.getRHSSpecialisedConcepts().size() + 
				"\tGeneralised: " + changeSet.getRHSGeneralisedConcepts().size() +
				"\tTotal affected: " + changeSet.getRHSAffectedConcepts().size());
		System.out.println("  [total]" +
				"\tSpecialised: " + changeSet.getAllSpecialisedConcepts().size() + 
				"\tGeneralised: " + changeSet.getAllGeneralisedConcepts().size() +
				"\tTotal affected: " + changeSet.getAllAffectedConcepts().size());
		
		System.out.println("\n  Overall affected concepts categorisation:");
		System.out.println("    Direct Generalised: " + changeSet.getAllDirectlyGeneralised().size());
		System.out.println("    Direct Specialised: " + changeSet.getAllDirectlySpecialised().size());
		System.out.println("    Purely directly generalised: " + changeSet.getAllPurelyDirectlyGeneralised().size());
		System.out.println("    Purely directly specialised: " + changeSet.getAllPurelyDirectlySpecialised().size());
		System.out.println("    Purely indirectly generalised: " + changeSet.getAllPurelyIndirectlyGeneralised().size());
		System.out.println("    Purely indirectly specialised: " + changeSet.getAllPurelyIndirectlySpecialised().size());
		System.out.println("    Mixed generalised: " + changeSet.getAllMixedGeneralised().size());
		System.out.println("    Mixed specialised: " + changeSet.getAllMixedSpecialised().size());
		
		serializeXMLReport(changeSet);
	}
	
	
	/**
	 * Get the XML change set and serialise it
	 * @param changeSet	Concept diff change set
	 */
	private void serializeXMLReport(ConceptChangeSet changeSet) {
		XMLConceptDiffReport report = getXMLReport(changeSet);
		Document doc = report.getReport();
		
		Transformer transformer = null;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		} catch(TransformerConfigurationException e) {
			e.printStackTrace();
		} catch(TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		}
		if(!outputDir.endsWith(File.separator)) outputDir += File.separator;
		Result output = new StreamResult(new File(outputDir + "difflog.xml"));
		Source input = new DOMSource(doc);

		try {
			transformer.transform(input, output);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Get the XML report for concept diff
	 * @param changeSet	Concept change set
	 * @return Concept diff report object
	 */
	public XMLConceptDiffReport getXMLReport(ConceptChangeSet changeSet) {
		return new XMLConceptDiffReport(changeSet);
	}
}