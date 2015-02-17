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
package uk.ac.manchester.cs.diff.axiom;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import uk.ac.manchester.cs.diff.axiom.changeset.LogicalChangeSet;
import uk.ac.manchester.cs.diff.axiom.changeset.StructuralChangeSet;
import uk.ac.manchester.cs.diff.output.csv.CSVAxiomDiffReport;
import uk.ac.manchester.cs.diff.output.xml.XMLAxiomDiffReport;
import uk.ac.manchester.cs.diff.utils.ReasonerLoader;

/**
 * @author Rafael S. Goncalves <br>
 * Information Management Group (IMG) <br>
 * School of Computer Science <br>
 * University of Manchester <br>
 */
public class LogicalDiffConcurrent implements AxiomDiff {
	private OWLOntology ont1, ont2;
	private StructuralChangeSet structChangeSet;
	private LogicalChangeSet logicalChangeSet;
	private OWLReasoner ont1reasoner, ont2reasoner;
	private double diffTime;
	private boolean verbose;
	
	/**
	 * Constructor
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 * @param verbose	Verbose mode
	 */
	public LogicalDiffConcurrent(OWLOntology ont1, OWLOntology ont2, boolean verbose) {
		this.ont1 = ont1;
		this.ont2 = ont2;
		this.verbose = verbose;
	}
	
	
	/**
	 * Constructor that takes a structural change set
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 * @param changeSet	Structural change set
	 * @param verbose	Verbose mode
	 */
	public LogicalDiffConcurrent(OWLOntology ont1, OWLOntology ont2, StructuralChangeSet changeSet, boolean verbose) {
		this.ont1 = ont1;
		this.ont2 = ont2;
		this.structChangeSet = changeSet;
		this.verbose = verbose;
	}

	
	/**
	 * Get logical changes between ontologies given a reasoner instance per ontology
	 * @param ont1reasoner	Instance of a reasoner loaded with ontology 1 
	 * @param ont2reasoner	Instance of a reasoner loaded with ontology 2
	 * @return Logical change set
	 */
	public LogicalChangeSet getDiff(OWLReasoner ont1reasoner, OWLReasoner ont2reasoner) {
		this.ont1reasoner = ont1reasoner;
		this.ont2reasoner = ont2reasoner;
		return getDiff();
	}

	
	/**
	 * Get logical changes between ontologies
	 * @return Logical change set
	 */
	@SuppressWarnings("deprecation")
	public LogicalChangeSet getDiff() {
		if(logicalChangeSet != null) return logicalChangeSet;
		if(structChangeSet == null) structChangeSet = new StructuralDiff(ont1, ont2, verbose).getDiff();
		
		if(ont1reasoner == null) ont1reasoner = new ReasonerLoader(ont1, verbose).createReasoner(false);
		if(ont2reasoner == null) ont2reasoner = new ReasonerLoader(ont2, verbose).createReasoner(false);
		
		if(!ont1reasoner.isConsistent()) {
			System.err.println("\n! Ontology 1 is inconsistent. Cannot perform logical diff on inconsistent input.");
			return null;
		}
		else if(!ont2reasoner.isConsistent()) {
			System.err.println("\n! Ontology 2 is inconsistent. Cannot perform logical diff on inconsistent input.");
			return null;
		}
		
		System.out.print("   Verifying axiom impact... ");
		long start = System.currentTimeMillis();
		
		IneffectualChangeChecker ineffAddChecker = new IneffectualChangeChecker(structChangeSet.getAddedAxioms(), ont1reasoner);
		IneffectualChangeChecker ineffRemChecker = new IneffectualChangeChecker(structChangeSet.getRemovedAxioms(), ont2reasoner);
        
		ExecutorService executor = Executors.newFixedThreadPool(2);
		List<Callable<Set<OWLAxiom>>> l = new ArrayList<Callable<Set<OWLAxiom>>>();
		l.add(ineffRemChecker); l.add(ineffAddChecker);
		
		Future<Set<OWLAxiom>> ia = executor.submit(ineffAddChecker);
		Future<Set<OWLAxiom>> ir = executor.submit(ineffRemChecker);
		
		Set<OWLAxiom> ineffectualAdditions = null, ineffectualRemovals = null;
		try {
			ineffectualAdditions = ia.get();
			ineffectualRemovals = ir.get();
		} catch(InterruptedException e) {
			e.printStackTrace();
		} catch(ExecutionException e) {
			e.printStackTrace();
		}
		executor.shutdown();
				
		Set<OWLAxiom> effectualAdditions = new HashSet<OWLAxiom>(structChangeSet.getAddedAxioms());
		effectualAdditions.removeAll(ineffectualAdditions);
		
		Set<OWLAxiom> effectualRemovals = new HashSet<OWLAxiom>(structChangeSet.getRemovedAxioms());
		effectualRemovals.removeAll(ineffectualRemovals);
		
		long end = System.currentTimeMillis();
		diffTime = (end-start)/1000.0;
	
		logicalChangeSet = new LogicalChangeSet(effectualAdditions, ineffectualAdditions, effectualRemovals, ineffectualRemovals, structChangeSet);
		logicalChangeSet.setDiffTime(diffTime);

		System.out.println("done (" + diffTime + " secs)"); 
		if(verbose) printDiff();
		return logicalChangeSet;
	}
	
	
	/**
	 * Ineffectual change checker worker  
	 */
	public class IneffectualChangeChecker implements Callable<Set<OWLAxiom>> {
		private Set<OWLAxiom> axioms;
		private OWLReasoner reasoner;

		/**
		 * Constructor
		 * @param axioms	Set of axioms to be checked
		 * @param reasoner	Reasoner instance
		 */
		public IneffectualChangeChecker(Set<OWLAxiom> axioms, OWLReasoner reasoner) {
			this.axioms = axioms;
			this.reasoner = reasoner;
		}
		
		@Override
		public Set<OWLAxiom> call() {
			Set<OWLAxiom> ineffectual = null;
			Set<OWLEntity> ontSig = reasoner.getRootOntology().getSignature();
			ineffectual = new HashSet<OWLAxiom>();
			for(OWLAxiom axiom : axioms) {
				if(ontSig.containsAll(axiom.getSignature())) {
					if(reasoner.isEntailed(axiom))
						ineffectual.add(axiom);
				}
			}
			return ineffectual;
		}
	}
	
	
	/**
	 * Print diff results
	 */
	public void printDiff() {
		System.out.println( 
				"\tEffectual Additions: " + logicalChangeSet.getEffectualAdditionAxioms().size() +
				"\n\tEffectual Removals: " + logicalChangeSet.getEffectualRemovalAxioms().size() + 
				"\n\tIneffectual Additions: " + logicalChangeSet.getIneffectualAdditionAxioms().size() +
				"\n\tIneffectual Removals: " + logicalChangeSet.getIneffectualRemovalAxioms().size());
	}
	
	
	/**
	 * Get an XML change report for the change set computed by this diff
	 * @return XML change report object 
	 */
	public XMLAxiomDiffReport getXMLReport() {
		if(logicalChangeSet == null) logicalChangeSet = getDiff();
		return new XMLAxiomDiffReport(ont1, ont2, logicalChangeSet);
	}
	
	
	/**
	 * Get a CSV change report
	 * @return Change report as a CSV document
	 */
	public String getCSVChangeReport() {
		if(logicalChangeSet == null) logicalChangeSet = this.getDiff();
		CSVAxiomDiffReport report = new CSVAxiomDiffReport();
		report.getReport(structChangeSet);
		return report.getReport(logicalChangeSet);
	}
	
	
	/**
	 * Determine if ontologies are logically equivalent
	 * @return true if ontologies are logically equivalent, false otherwise
	 */
	public boolean isEquivalent() {
		if(logicalChangeSet == null) logicalChangeSet = this.getDiff();
		if(logicalChangeSet.getEffectualAdditionAxioms().isEmpty() && logicalChangeSet.getEffectualRemovalAxioms().isEmpty()) 
			return true;
		else 
			return false;
	}
	
	
	/**
	 * Convenience method to get the StructuralChangeSet
	 * @return Structural diff change set
	 */
	public StructuralChangeSet getStructuralChangeSet() {
		if(structChangeSet != null)
			return structChangeSet;
		else
			return new StructuralDiff(ont1, ont2, verbose).getDiff();
	}
}
