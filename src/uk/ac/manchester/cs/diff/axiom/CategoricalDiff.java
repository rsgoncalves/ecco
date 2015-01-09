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

import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxObjectRenderer;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import uk.ac.manchester.cs.diff.axiom.change.CategorisedChange;
import uk.ac.manchester.cs.diff.axiom.change.CategorisedEffectualAddition;
import uk.ac.manchester.cs.diff.axiom.change.CategorisedEffectualAddition.EffectualAdditionCategory;
import uk.ac.manchester.cs.diff.axiom.change.CategorisedEffectualChange;
import uk.ac.manchester.cs.diff.axiom.change.CategorisedEffectualRemoval;
import uk.ac.manchester.cs.diff.axiom.change.CategorisedEffectualRemoval.EffectualRemovalCategory;
import uk.ac.manchester.cs.diff.axiom.change.CategorisedIneffectualAddition;
import uk.ac.manchester.cs.diff.axiom.change.CategorisedIneffectualAddition.IneffectualAdditionCategory;
import uk.ac.manchester.cs.diff.axiom.change.CategorisedIneffectualRemoval;
import uk.ac.manchester.cs.diff.axiom.change.CategorisedIneffectualRemoval.IneffectualRemovalCategory;
import uk.ac.manchester.cs.diff.axiom.changeset.CategorisedChangeSet;
import uk.ac.manchester.cs.diff.axiom.changeset.LogicalChangeSet;
import uk.ac.manchester.cs.diff.axiom.changeset.StructuralChangeSet;
import uk.ac.manchester.cs.diff.justifications.JustificationFinder;
import uk.ac.manchester.cs.diff.output.csv.CSVAxiomDiffReport;
import uk.ac.manchester.cs.diff.output.xml.XMLAxiomDiffReport;
import uk.ac.manchester.cs.diff.utils.ProgressMonitor;
import uk.ac.manchester.cs.diff.utils.ReasonerLoader;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import com.clarkparsia.owlapi.modularity.locality.LocalityClass;
import com.clarkparsia.owlapi.modularity.locality.SyntacticLocalityEvaluator;

/**
 * @author Rafael S. Goncalves <br>
 * Information Management Group (IMG) <br>
 * School of Computer Science <br>
 * University of Manchester <br>
 */
public class CategoricalDiff implements AxiomDiff {
	private OWLOntology ont1, ont2;
	private OWLOntologyManager man;
	private OWLDataFactory df;
	private StructuralChangeSet structuralChangeSet;
	private CategorisedChangeSet categorisedChangeSet;
	private LogicalChangeSet logicalChangeSet;
	private OWLReasoner ont1reasoner, ont2reasoner, emptyOntReasoner;
	private Set<OWLAxiom> sharedAxioms;
	private double diffTime, eaTime, erTime, iaTime, irTime, iaJustTime, irJustTime;
	private int nrJusts;
	private boolean verbose;
	private static ShortFormProvider p = new SimpleShortFormProvider();

	/**
	 * Constructor
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 * @param nrJusts	Number of justifications to be computed
	 * @param verbose	Verbose mode
	 */
	public CategoricalDiff(OWLOntology ont1, OWLOntology ont2, int nrJusts, boolean verbose) {
		this.ont1 = ont1;
		this.ont2 = ont2;
		this.nrJusts = nrJusts;
		this.verbose = verbose;
		man = OWLManager.createOWLOntologyManager();
		df = man.getOWLDataFactory();
		emptyOntReasoner = createEmptyOntologyReasoner();
	}
	
	
	/**
	 * Constructor that takes a logical change set
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 * @param logicalChangeSet	Logical change set
	 * @param nrJusts	Number of justifications to be computed
	 * @param verbose	Verbose mode
	 */
	public CategoricalDiff(OWLOntology ont1, OWLOntology ont2, LogicalChangeSet logicalChangeSet, int nrJusts, boolean verbose) {
		this.ont1 = ont1;
		this.ont2 = ont2;
		this.logicalChangeSet = logicalChangeSet;
		this.nrJusts = nrJusts;
		this.verbose = verbose;
		man = OWLManager.createOWLOntologyManager();
		df = man.getOWLDataFactory();
		emptyOntReasoner = createEmptyOntologyReasoner();
	}
	
	
	/**
	 * Get categorised logical changes between ontologies
	 * @return Categorised logical change set
	 */
	@SuppressWarnings("deprecation")
	public CategorisedChangeSet getDiff() {
		if(categorisedChangeSet != null) return categorisedChangeSet;
		System.out.println("\nComputing axiom diff...");
		long start = System.currentTimeMillis();
		ont1reasoner = new ReasonerLoader(ont1, false).createReasoner(false);
		ont2reasoner = new ReasonerLoader(ont2, false).createReasoner(false);
		
		if(logicalChangeSet == null) {
			LogicalDiffConcurrent lDiff = new LogicalDiffConcurrent(ont1, ont2, verbose);
			logicalChangeSet = lDiff.getDiff(ont1reasoner, ont2reasoner);
			
			structuralChangeSet = lDiff.getStructuralChangeSet();
			sharedAxioms = structuralChangeSet.getShared();
			if(logicalChangeSet == null) return null;
		}
		
		Set<OWLAxiom> ea = logicalChangeSet.getEffectualAdditionAxioms();
		Set<OWLAxiom> er = logicalChangeSet.getEffectualRemovalAxioms();
		Set<OWLAxiom> ia = logicalChangeSet.getIneffectualAdditionAxioms();
		Set<OWLAxiom> ir = logicalChangeSet.getIneffectualRemovalAxioms();
		
		System.out.print("   Computing change categorisation... ");
		long start2 = System.currentTimeMillis();
		Set<CategorisedEffectualAddition> effAdds = categoriseEffectualAdditions(ea, er, ir);
		Set<CategorisedEffectualRemoval> effRems = categoriseEffectualRemovals(er, ea, ia);
		Set<CategorisedIneffectualAddition> ineffAdds = categoriseIneffectualAdditions(ia, er, ir);
		Set<CategorisedIneffectualRemoval> ineffRems = categoriseIneffectualRemovals(ir, ea, ia);
		
		long end = System.currentTimeMillis();
		if(!verbose) System.out.print("done (" + (end-start2)/1000.0 + " secs)");
		diffTime = (end-start)/1000.0;
		
		categorisedChangeSet = new CategorisedChangeSet(effAdds, ineffAdds, effRems, ineffRems, sharedAxioms);
		categorisedChangeSet.setDiffTime(diffTime);
		categorisedChangeSet.setEffectualAdditionCategorisationTime(eaTime);
		categorisedChangeSet.setEffectualRemovalCategorisationTime(erTime);
		categorisedChangeSet.setIneffectualRemovalCategorisationTime(irTime);
		categorisedChangeSet.setIneffectualAdditionCategorisationTime(iaTime);
		categorisedChangeSet.setIneffectualAdditionJustificationFindingTime(iaJustTime);
		categorisedChangeSet.setIneffectualRemovalJustificationFindingTime(irJustTime);
		
		System.out.println("\nfinished axiom diff (" + diffTime + " seconds)");
		if(verbose) printDiff();
		return categorisedChangeSet;
	}

	
	/**
	 * Categorise effectual additions
	 * @param ea	Effectual additions
	 * @param er	Effectual removals
	 * @param ir	Ineffectual removals
	 * @return Set of categorised effectual additions
	 */
	@SuppressWarnings("unchecked")
	private Set<CategorisedEffectualAddition> categoriseEffectualAdditions(Set<OWLAxiom> ea, Set<OWLAxiom> er, Set<OWLAxiom> ir) {
		if(verbose) System.out.println("\n    Categorising effectual additions... ");
		Set<CategorisedEffectualAddition> effAdds = new HashSet<CategorisedEffectualAddition>();
		if(!ea.isEmpty())
			try { effAdds = (Set<CategorisedEffectualAddition>) categoriseEffectualChanges(true, ea, ont1, er, ir); }
			catch (OWLOntologyCreationException e) { e.printStackTrace(); }
		else
			if(verbose) System.out.println("    done (no effectual additions)");
		return effAdds;
	}
	
	
	/**
	 * Categorise effectual removals
	 * @param er	Effectual removals
	 * @param ea	Effectual additions
	 * @param ia	Ineffectual additions
	 * @return Set of categorised effectual removals
	 */
	@SuppressWarnings("unchecked")
	private Set<CategorisedEffectualRemoval> categoriseEffectualRemovals(Set<OWLAxiom> er, Set<OWLAxiom> ea, Set<OWLAxiom> ia) {
		if(verbose) System.out.println("    Categorising effectual removals... ");
		Set<CategorisedEffectualRemoval> effRems = new HashSet<CategorisedEffectualRemoval>();
		if(!er.isEmpty())
			try { effRems = (Set<CategorisedEffectualRemoval>) categoriseEffectualChanges(false, er, ont2, ea, ia); }
			catch (OWLOntologyCreationException e) { e.printStackTrace(); }
		else
			if(verbose) System.out.println("    done (no effectual removals)");
		return effRems;
	}
	
	
	/**
	 * Categorise ineffectual additions
	 * @param ia	Ineffectual additions
	 * @param er	Effectual removals
	 * @param ir	Ineffectual removals
	 * @return Set of categorised ineffectual additions
	 */
	@SuppressWarnings("unchecked")
	private Set<CategorisedIneffectualAddition> categoriseIneffectualAdditions(Set<OWLAxiom> ia, Set<OWLAxiom> er, Set<OWLAxiom> ir) {
		if(verbose) System.out.println("    Categorising ineffectual additions... ");
		Set<CategorisedIneffectualAddition> ineffAdds = new HashSet<CategorisedIneffectualAddition>();
		if(!ia.isEmpty())
			ineffAdds = (Set<CategorisedIneffectualAddition>) categoriseIneffectualChanges("rhs", ia, er, ir, ont1, ont2reasoner);
		else
			if(verbose) System.out.println("    done (no ineffectual additions)");
		return ineffAdds;
	}
	
	
	/**
	 * Categorise ineffectual removals
	 * @param ir	Ineffectual removals
	 * @param ea	Effectual additions
	 * @param ia	Ineffectual additions
	 * @return Set of categorised ineffectual removals
	 */
	@SuppressWarnings("unchecked")
	private Set<CategorisedIneffectualRemoval> categoriseIneffectualRemovals(Set<OWLAxiom> ir, Set<OWLAxiom> ea, Set<OWLAxiom> ia) {
		if(verbose) System.out.println("    Categorising ineffectual removals... ");
		Set<CategorisedIneffectualRemoval> ineffRems = new HashSet<CategorisedIneffectualRemoval>();
		if(!ir.isEmpty())
			ineffRems = (Set<CategorisedIneffectualRemoval>) categoriseIneffectualChanges("lhs", ir, ea, ia, ont2, ont1reasoner);
		else
			if(verbose) System.out.println("    done (no ineffectual removals)");
		return ineffRems;
	}


	/**
	 * Categorise effectual changes
	 * @param effAdds	true if checking effectual additions, false for removals
	 * @param axioms	Set of changes to categorise
	 * @param ont	if(effAdds) then ont1, else ont2
	 * @param effectual	if(effAdds) then Set of effectual removals, else Set of effectual additions
	 * @param ineffectual	if(effAdds) then Set of ineffectual removals, else Set of ineffectual additions
	 * @return Set of categorised changes
	 * @throws OWLOntologyCreationException	Ontology creation exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Set<? extends CategorisedEffectualChange> categoriseEffectualChanges(boolean effAdds, Set<OWLAxiom> axioms, OWLOntology ont, 
			Set<OWLAxiom> effectual, Set<OWLAxiom> ineffectual) throws OWLOntologyCreationException {
		long start = System.currentTimeMillis();
		
		Set result = null;
		if(effAdds) result = new HashSet<CategorisedEffectualAddition>();
		else result = new HashSet<CategorisedEffectualRemoval>();

		ProgressMonitor progress = new ProgressMonitor(axioms);
		int status = 0;
		Set<OWLEntity> ontSig = ont.getSignature();
		OWLOntologyManager man = ont.getOWLOntologyManager();
		SyntacticLocalityModuleExtractor modExtractorEff = new SyntacticLocalityModuleExtractor(man, ont, ModuleType.STAR);
//		FaCTPlusPlusReasoner modExtractorEff = new FaCTPlusPlusReasoner(ont, new SimpleConfiguration(), BufferingMode.NON_BUFFERING);
		SyntacticLocalityEvaluator eval = new SyntacticLocalityEvaluator(LocalityClass.BOTTOM_BOTTOM);

		// Loop effectual changes
		for(OWLAxiom ax : axioms) {
			if(ax.isLogicalAxiom()) {
				boolean stOrWkAx = false, newOrRetiredAx = false, modEquiv = false;
				Set<OWLEntity> sig = ax.getSignature();
				Set<OWLEntity> newTerms = new HashSet<OWLEntity>();		// New terms in the axiom				
				for(OWLEntity e : sig) {
					if(!ontSig.contains(e) && !e.isTopEntity() && !e.isBottomEntity())
						newTerms.add(e);
				}
				// Check New or Retired Descriptions
				CategorisedChange nd = checkNewOrRetiredDescription(effAdds, man, ax, newTerms, eval);
				if(nd != null) {
					newOrRetiredAx = true; 
					result.add(nd);
				}
				// Check Strengthenings or Weakenings
				if(!newOrRetiredAx) {
					CategorisedChange st = checkStrengtheningOrWeakening(effAdds, man, ax, ineffectual, newTerms);
					if(st != null) {
						stOrWkAx = true; 
						result.add(st);
					}
				}
				// Check Modified Definitions
				if(!newOrRetiredAx && !stOrWkAx) {
					if(ax.isOfType(AxiomType.EQUIVALENT_CLASSES)) {
						CategorisedChange me = checkModifiedDefinitions(effAdds, man, ax, modExtractorEff, newTerms);
						if(me != null) { 
							modEquiv = true; 
							result.add(me);
						}
					}
				}
				// Pure Alterations
				if(!newOrRetiredAx && !stOrWkAx && !modEquiv) {
					if(effAdds) {
						if(newTerms.isEmpty()) 
							result.add(new CategorisedEffectualAddition(ax, EffectualAdditionCategory.PUREADDITION, new HashSet<OWLAxiom>(), newTerms));
						else 
							result.add(new CategorisedEffectualAddition(ax, EffectualAdditionCategory.PUREADDITIONNT, new HashSet<OWLAxiom>(), newTerms));
					}
					else {
						if(newTerms.isEmpty()) 
							result.add(new CategorisedEffectualRemoval(ax, EffectualRemovalCategory.PUREREMOVAL, new HashSet<OWLAxiom>(), newTerms));
						else 
							result.add(new CategorisedEffectualRemoval(ax, EffectualRemovalCategory.PUREREMOVALRT, new HashSet<OWLAxiom>(), newTerms));
					}
				}
			} // end if logical axiom
			int p = progress.incrementProgress();
			if(p > status && verbose) { 
				System.out.print("\r\t" + p + "%");
				status = p;
			}
		} // end for loop
		modExtractorEff = null; progress = null;
		
		long end = System.currentTimeMillis();
		double total = (end-start)/1000.0;
		if(verbose) System.out.println("\n    done (" + total + " secs)");
		
		if(effAdds) eaTime = total;
		else erTime = total;
		
		return result;
	}
	
	
	/**
	 * Check whether a given change is a strengthening (or weakening)
	 * @param effAdds	true if checking additions, false if checking removals
	 * @param man	OWL ontology manager
	 * @param ax	OWL axiom to be checked
	 * @param searchSpace	Set of axioms in the search space
	 * @param newTerms	Set of new terms used in this axiom
	 * @return Strengthening or Weakening-type change, or null if not a strengthening or weakening
	 */
	private CategorisedChange checkStrengtheningOrWeakening(boolean effAdds, OWLOntologyManager man, OWLAxiom ax, 
			Set<OWLAxiom> searchSpace, Set<OWLEntity> newTerms) {
		CategorisedChange change = null;
		OWLOntology axOnt = createOntology(ax);
		OWLReasoner reasoner = new ReasonerLoader(axOnt).createReasoner(false);
		Set<OWLAxiom> stAlignments = new HashSet<OWLAxiom>();
		for(OWLAxiom axiom : searchSpace) {
			if(axiom.isLogicalAxiom() && signatureOverlaps(ax, axiom)) {
				if(reasoner.isEntailed(axiom) && !emptyOntReasoner.isEntailed(axiom)) {
					stAlignments.add(axiom);
					break;
				}
			}
		}
		if(!stAlignments.isEmpty()) {
			if(newTerms.isEmpty()) {
				if(effAdds) 
					change = new CategorisedEffectualAddition(ax, EffectualAdditionCategory.STRENGTHENING, stAlignments, newTerms);
				else 
					change = new CategorisedEffectualRemoval(ax, EffectualRemovalCategory.WEAKENING, stAlignments, newTerms);
			}
			else {
				if(effAdds) 
					change = new CategorisedEffectualAddition(ax, EffectualAdditionCategory.STRENGTHENINGNT, stAlignments, newTerms);
				else 
					change = new CategorisedEffectualRemoval(ax, EffectualRemovalCategory.WEAKENINGRT, stAlignments, newTerms);
			}
		}
		cleanUp(reasoner); cleanUp(axOnt);
		return change;
	}
	
	
	/**
	 * Check whether a given change is a new or retired description
	 * @param effAdds	true if checking additions, false if checking removals
	 * @param man	OWL ontology manager
	 * @param ax	OWL axiom to be checked
	 * @param newTerms	Set of new terms used in this axiom
	 * @param eval	Syntactic locality evaluator
	 * @return New or Retired Description-type change, or null if not one
	 * @throws OWLOntologyCreationException	Ontology creation exception
	 */
	private CategorisedChange checkNewOrRetiredDescription(boolean effAdds, OWLOntologyManager man, OWLAxiom ax, 
			Set<OWLEntity> newTerms, SyntacticLocalityEvaluator eval) throws OWLOntologyCreationException {
		CategorisedChange change = null;
		if(!newTerms.isEmpty()) {
			boolean atomicLhs = true;
			if(ax instanceof OWLSubClassOfAxiom) {
				OWLClassExpression c = ((OWLSubClassOfAxiom)ax).getSubClass();
				if(c.isAnonymous() || !newTerms.contains(c))
					atomicLhs = false;
			}
			else if(ax instanceof OWLEquivalentClassesAxiom)
				atomicLhs = false;

			if(atomicLhs && !(eval.isLocal(ax, newTerms))) {
				if(effAdds) 
					change = new CategorisedEffectualAddition(ax, EffectualAdditionCategory.NEWDESCRIPTION, new HashSet<OWLAxiom>(), newTerms);
				else 
					change = new CategorisedEffectualRemoval(ax, EffectualRemovalCategory.RETIREDDESCRIPTION, new HashSet<OWLAxiom>(), newTerms);
			}
		}
		return change;
	}
	
	
	/**
	 * Check if given change is a modified equivalence
	 * @param effAdds	true if checking additions, false if checking removals
	 * @param man	OWL ontology manager
	 * @param ax	OWL axiom to be checked
	 * @param modExtractor	Star module extractor
	 * @param newTerms	Set of new terms used in this axiom
	 * @return Modified definition-type change, or null if not one
	 * @throws OWLOntologyCreationException	Ontology creation exception
	 */
	private CategorisedChange checkModifiedDefinitions(boolean effAdds, OWLOntologyManager man, OWLAxiom ax, 
			SyntacticLocalityModuleExtractor modExtractor, Set<OWLEntity> newTerms)  throws OWLOntologyCreationException {
		CategorisedChange change = null;
		Set<OWLAxiom> alignment = new HashSet<OWLAxiom>();
		OWLEquivalentClassesAxiom equiv = (OWLEquivalentClassesAxiom) ax;
		Set<OWLSubClassOfAxiom> subs = equiv.asOWLSubClassOfAxioms();

		OWLSubClassOfAxiom sub1 = (OWLSubClassOfAxiom) subs.toArray()[0];
		OWLClassExpression lhs = sub1.getSubClass();
		OWLClassExpression rhs = sub1.getSuperClass();

		Set<OWLAxiom> mod = modExtractor.extract(ax.getSignature());
		loop:
		for(OWLAxiom a : mod) {
			if(a.isOfType(AxiomType.EQUIVALENT_CLASSES)) {
				OWLEquivalentClassesAxiom y = (OWLEquivalentClassesAxiom) a;
				Set<OWLSubClassOfAxiom> subs2 = y.asOWLSubClassOfAxioms();
				for(OWLSubClassOfAxiom sub : subs2) {
					boolean isEquiv = false;
					if(sub.getSubClass().equals(lhs)) {
						if(emptyOntReasoner.isEntailed(df.getOWLSubClassOfAxiom(sub.getSuperClass(), rhs)) ||
								emptyOntReasoner.isEntailed(df.getOWLSubClassOfAxiom(rhs, sub.getSuperClass())))
							isEquiv = true;
					}
					else if(sub.getSubClass().equals(rhs)) {
						if(emptyOntReasoner.isEntailed(df.getOWLSubClassOfAxiom(sub.getSuperClass(), lhs)) ||
								emptyOntReasoner.isEntailed(df.getOWLSubClassOfAxiom(lhs, sub.getSuperClass())))
							isEquiv = true;
					}
					else if(sub.getSuperClass().equals(lhs)) {
						if(emptyOntReasoner.isEntailed(df.getOWLSubClassOfAxiom(sub.getSubClass(), rhs)) ||
								emptyOntReasoner.isEntailed(df.getOWLSubClassOfAxiom(rhs, sub.getSubClass())))
							isEquiv = true;
					}
					else if(sub.getSuperClass().equals(lhs)) {
						if(emptyOntReasoner.isEntailed(df.getOWLSubClassOfAxiom(sub.getSubClass(), lhs)) ||
								emptyOntReasoner.isEntailed(df.getOWLSubClassOfAxiom(lhs, sub.getSubClass())))
							isEquiv = true;
					}
					
					if(isEquiv) {
						alignment.add(a);
						break loop;
					}
				}
			}
		}
		if(!alignment.isEmpty()) {
			if(effAdds) {
				if(newTerms.isEmpty()) 
					change = new CategorisedEffectualAddition(ax, EffectualAdditionCategory.MODIFIEDDEFINITION, alignment, newTerms);
				else 
					change = new CategorisedEffectualAddition(ax, EffectualAdditionCategory.MODIFIEDDEFINITIONNT, alignment, newTerms);
			}
			else {
				if(newTerms.isEmpty()) 
					change = new CategorisedEffectualRemoval(ax, EffectualRemovalCategory.MODIFIEDDEFINITION, alignment, newTerms);
				else 
					change = new CategorisedEffectualRemoval(ax, EffectualRemovalCategory.MODIFIEDDEFINITIONRT, alignment, newTerms);
			}
		}
		return change;
	}
	

	/**
	 * Categorise the given set of ineffectual changes
	 * @param desc	"rhs" if categorising additions, "lhs" if removals
	 * @param axioms	Axiom changes to categorise
	 * @param effectual	Set of effectual changes of the opposite ontology
	 * @param ineffectual	Set of ineffectual changes of the opposite ontology
	 * @param ont	Opposite ontology
	 * @param src_reasoner	Reasoner instance for source ontology
	 * @return Set of categorised ineffectual changes
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Set<? extends CategorisedChange> categoriseIneffectualChanges(String desc, Set<OWLAxiom> axioms, Set<OWLAxiom> effectual, 
			Set<OWLAxiom> ineffectual, OWLOntology ont, OWLReasoner src_reasoner) {
		Set result = null;
		if(desc.equals("rhs")) result = new HashSet<CategorisedIneffectualAddition>();
		else result = new HashSet<CategorisedIneffectualRemoval>();
		
		long start = System.currentTimeMillis();
		
		if(verbose) System.out.print("\tComputing justifications... ");
		JustificationFinder just = new JustificationFinder(ont, nrJusts);
		Map<OWLAxiom,Set<Explanation<OWLAxiom>>> exps = null;
		try { 
			exps = just.getJustifications(axioms); 
		} catch (OWLOntologyCreationException e) { e.printStackTrace(); }
		
		double justTime = (System.currentTimeMillis()-start)/1000.0;
		if(verbose) System.out.println("done (" + justTime + " secs)");
		
		ProgressMonitor progress = new ProgressMonitor(exps.keySet());
		int status = 0;
		for(Set<Explanation<OWLAxiom>> expsSet : exps.values()) {
			try {
				if(!expsSet.isEmpty())
					result.add(categoriseIneffectualChange(desc, expsSet, effectual, ineffectual, ont, just, src_reasoner));
			} catch (OWLOntologyCreationException e) {
				e.printStackTrace();
			}
			int p = progress.incrementProgress();
			if(p > status && verbose) { 
				System.out.print("\r\t" + p + "%");
				status = p;
			}
		}

		long end = System.currentTimeMillis();
		double total = (end-start)/1000.0;
		
		if(desc.equals("rhs")) {
			iaTime = total;
			iaJustTime = justTime;
		}
		else {
			irTime = total;
			irJustTime = justTime;
		}
		if(verbose) System.out.println("\n    done (" + total + " secs)");
		cleanUp(src_reasoner); cleanUp(exps); just = null;
		return result;
	}
	
	
	/**
	 * Categorise the given ineffectual change
	 * @param desc	"rhs" if categorising additions, "lhs" if removals
	 * @param exps	Set of justifications for this change
	 * @param effectual	Effectual changes
	 * @param ineffectual	Ineffectual changes
	 * @param ont	Target ontology
	 * @param just	Justification generator interface
	 * @param src_reasoner	Reasoner instance with the source ontology loaded
	 * @return Categorised change
	 * @throws OWLOntologyCreationException	Ontology creation exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private CategorisedChange categoriseIneffectualChange(String desc, Set<Explanation<OWLAxiom>> exps, Set<OWLAxiom> effectual, 
			Set<OWLAxiom> ineffectual, OWLOntology ont, JustificationFinder just, OWLReasoner src_reasoner) 
					throws OWLOntologyCreationException {
		boolean entailmentAssigned = false;
		OWLAxiom entailment = null;
		HashMap justMap = null;
		if(desc.equals("rhs")) justMap = new HashMap<Explanation<OWLAxiom>,Set<IneffectualAdditionCategory>>();
		else justMap = new HashMap<Explanation<OWLAxiom>,Set<IneffectualRemovalCategory>>();
		
		OWLOntology entOnt = null; OWLReasoner reasoner = null;
		for(Explanation<OWLAxiom> explanation : exps) {
			boolean prospRedundantNovelAx = false, rewrittenAx = false, redundancyAx = false, prospRedundantAx = false;
			if(!entailmentAssigned) {
				entailment = explanation.getEntailment();
				entailmentAssigned = true;
				entOnt = createOntology(entailment);
				reasoner = new ReasonerLoader(entOnt).createReasoner(false);
			}
			
			int entailedAxs = 0, shared = 0;
			for(OWLAxiom ax : explanation.getAxioms()) {
				// Rewrite: If the axiom entails the justification, the axiom is rewritten
				if(signatureOverlaps(entailment, ax) && reasoner.isEntailed(ax)) entailedAxs ++;
				// Prospective novel redundancy: If the justification intersects with the effectual additions or removals
				if(effectual.contains(ax)) prospRedundantNovelAx = true;
				// Redundant: If the justification intersects with the intersection
				if(sharedAxioms.contains(ax)) shared++;
				if(sharedAxioms.contains(ax) || ineffectual.contains(ax)) prospRedundantAx = true;
			}
			// Rewritten axiom
			if(entailedAxs == explanation.getSize()) {
				rewrittenAx = true;
				if(shared > 0)
					updateJustificationMap(desc, justMap, explanation, "prewrite");	// partial rewrite
				else
					updateJustificationMap(desc, justMap, explanation, "rewrite"); // complete rewrite
			}
			// Redundant axiom
			if(shared == explanation.getSize() && !rewrittenAx && !prospRedundantNovelAx) {
				updateJustificationMap(desc, justMap, explanation, "redundant");
				redundancyAx = true;
			}
			// Prospective reshuffle redundancy
			if(prospRedundantAx && !rewrittenAx && !redundancyAx && !prospRedundantNovelAx)
				updateJustificationMap(desc, justMap, explanation, "reshuffle");
			// Prospective new redundancy 
			if(prospRedundantNovelAx && !rewrittenAx && !redundancyAx)
				updateJustificationMap(desc, justMap, explanation, "new");
		} // end for each explanation
		cleanUp(reasoner);
		cleanUp(entOnt);
		
		if(desc.equals("lhs")) return new CategorisedIneffectualRemoval(entailment, justMap);
		else return new CategorisedIneffectualAddition(entailment, justMap);
	}
	
	
	/**
	 * Update ineffectual change justification map with a new category
	 * @param desc	"rhs" if checking additions, "lhs" for removals
	 * @param justMap	Justification map
	 * @param exp	Justification
	 * @param cat	New category
	 */
	@SuppressWarnings("rawtypes")
	private void updateJustificationMap(String desc, HashMap justMap, Explanation<OWLAxiom> exp, String cat) {
		if(desc.equals("rhs"))
			updateJustificationMapWithAddition(justMap, exp, cat);
		else
			updateJustificationMapWithRemoval(justMap, exp, cat);
	}
	
	
	/**
	 * Update ineffectual addition justification map with a new category
	 * @param justMap	Justification map
	 * @param exp	Justification
	 * @param cat	New category
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void updateJustificationMapWithAddition(HashMap justMap, Explanation<OWLAxiom> exp, String cat) {
		IneffectualAdditionCategory category = null;
		if(cat.equalsIgnoreCase("redundant"))
			category = IneffectualAdditionCategory.REDUNDANCY;
		else if(cat.equalsIgnoreCase("rewrite"))
			category = IneffectualAdditionCategory.REWRITE;
		else if(cat.equalsIgnoreCase("prewrite"))
			category = IneffectualAdditionCategory.PREWRITE;
		else if(cat.equalsIgnoreCase("reshuffle"))
			category = IneffectualAdditionCategory.RESHUFFLEREDUNDANCY;
		else if(cat.equalsIgnoreCase("new"))
			category = IneffectualAdditionCategory.NEWPROSPREDUNDANCY;
		
		if(justMap.containsKey(exp)) {
			Set<IneffectualAdditionCategory> catSet = (Set<IneffectualAdditionCategory>) justMap.get(exp);
			catSet.add(category);
			justMap.put(exp, catSet);
		}
		else
			justMap.put(exp, new HashSet<IneffectualAdditionCategory>(Collections.singleton(category)));
	}
	
	
	/**
	 * Update ineffectual removal justification map with a new category
	 * @param justMap	Justification map
	 * @param exp	Justification
	 * @param cat	New category
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void updateJustificationMapWithRemoval(HashMap justMap, Explanation<OWLAxiom> exp, String cat) {
		IneffectualRemovalCategory category = null;
		if(cat.equalsIgnoreCase("redundant"))
			category = IneffectualRemovalCategory.REDUNDANCY;
		else if(cat.equalsIgnoreCase("rewrite"))
			category = IneffectualRemovalCategory.REWRITE;
		else if(cat.equalsIgnoreCase("prewrite"))
			category = IneffectualRemovalCategory.PREWRITE;
		else if(cat.equalsIgnoreCase("reshuffle"))
			category = IneffectualRemovalCategory.RESHUFFLEREDUNDANCY;
		else if(cat.equalsIgnoreCase("new"))
			category = IneffectualRemovalCategory.NEWPROSPREDUNDANCY;

		if(justMap.containsKey(exp)) {
			Set<IneffectualRemovalCategory> set = (Set<IneffectualRemovalCategory>) justMap.get(exp);
			set.add(category);
			justMap.put(exp, set);
		}
		else
			justMap.put(exp, new HashSet<IneffectualRemovalCategory>(Collections.singleton(category)));
	}
	
	
	/**
	 * Determine if ontologies are logically equivalent
	 * @return true if ontologies are logically equivalent, false otherwise
	 */
	public boolean isEquivalent() {
		if(logicalChangeSet == null) logicalChangeSet = new LogicalDiff(ont1, ont2, verbose).getDiff();
		if(logicalChangeSet.getEffectualAdditionAxioms().isEmpty() && logicalChangeSet.getEffectualRemovalAxioms().isEmpty()) 
			return true;
		else 
			return false;
	}
	
	
	/**
	 * Create an empty ontology reasoner, where the empty ontology contains entity declarations
	 * for all entities in sig(O1) and sig(O2)
	 * @return Empty ontology reasoner
	 */
	private OWLReasoner createEmptyOntologyReasoner() {
		OWLOntology emptyOnt = null;
		try { emptyOnt = man.createOntology(); }
		catch (OWLOntologyCreationException e) { e.printStackTrace(); }
		return new ReasonerLoader(emptyOnt).createReasoner(false); 
	}

	
	/**
	 * Check if the signatures of 2 axioms overlap
	 * @param ax1	Axiom 1
	 * @param ax2	Axiom 2
	 * @return true if the signatures overlap, false otherwise
	 */
	private boolean signatureOverlaps(OWLAxiom ax1, OWLAxiom ax2) {
		boolean overlaps = false;
		for(OWLEntity e : ax1.getSignature()) {
			if(ax2.getSignature().contains(e))
				overlaps = true;
		}
		return overlaps;
	}
	
	
	/**
	 * Get signature union of both ontologies
	 * @return Signature union of both ontologies
	 */
	private Set<OWLEntity> getSignatureUnion() {
		Set<OWLEntity> sig = new HashSet<OWLEntity>();
		sig.addAll(ont1.getSignature());
		sig.addAll(ont2.getSignature());
		return sig;
	}
	
	
	/**
	 * Get the set of declarations for the signature union
	 * @return Set of declarations for the signature union
	 */
	@SuppressWarnings("unused")
	private Set<OWLAxiom> getSignatureDeclarations() {
		OWLDataFactory df = man.getOWLDataFactory();
		Set<OWLAxiom> result = new HashSet<OWLAxiom>();
		for(OWLEntity e : getSignatureUnion()) {
			result.add(df.getOWLDeclarationAxiom(e));
		}
		return result;
	}
	
	
	/**
	 * Create a new ontology containing the specified axiom and all entity declarations
	 * @param axiom	Axiom
	 * @return OWL ontology
	 */
	private OWLOntology createOntology(OWLAxiom axiom) {
		OWLOntology ont = null;
		try { ont = man.createOntology(Collections.singleton(axiom)); } 
		catch (OWLOntologyCreationException e) { e.printStackTrace(); }
		return ont;
	}
	
	
	/**
	 * Get the structural change set between given ontologies
	 * @return Structural change set
	 */
	public StructuralChangeSet getStructuralChangeSet() {
		return structuralChangeSet;
	}
	
	
	/**
	 * Get Manchester syntax of an OWL object
	 * @param obj	OWL object
	 * @return A string with the object's conversion to Manchester syntax 
	 */
	public static String getManchesterRendering(OWLObject obj) {
		StringWriter wr = new StringWriter();
		ManchesterOWLSyntaxObjectRenderer render = new ManchesterOWLSyntaxObjectRenderer(wr, p);
		obj.accept(render);

		String str = wr.getBuffer().toString();
		str = str.replace("<", "");
		str = str.replace(">", "");
		return str;
	}
	

	/**
	 * Print diff results
	 */
	public void printDiff() {
		System.out.println("   Categorised axiom changes:" + 
				"\n\tEffectual Additions: " + categorisedChangeSet.getEffectualAdditions().size() +
				"\n\t   Strengthenings: " + categorisedChangeSet.getStrengthenings().size() +
				"\n\t   Strengthenings w/ New Terms: " + categorisedChangeSet.getStrengtheningsWithNewTerms().size() +
				"\n\t   New Descriptions: " + categorisedChangeSet.getNewDescriptions().size() +
				"\n\t   Modified Definitions: " + categorisedChangeSet.getAddedModifiedDefinitions().size() +
				"\n\t   Modified Definitions w/ New Terms: " + categorisedChangeSet.getAddedModifiedDefinitionsWithNewTerms().size() +
				"\n\t   Pure Additions: " + categorisedChangeSet.getPureAdditions().size() +
				"\n\t   Pure Additions w/ New Terms: " + categorisedChangeSet.getPureAdditionsWithNewTerms().size() +
				
				"\n\tEffectual Removals: " + categorisedChangeSet.getEffectualRemovals().size() +
				"\n\t   Weakenings: " + categorisedChangeSet.getWeakenings().size() +
				"\n\t   Weakenings w/ Retired Terms: " + categorisedChangeSet.getWeakeningsWithRetiredTerms().size() +
				"\n\t   Retired Descriptions: " + categorisedChangeSet.getRetiredDescriptions().size() +
				"\n\t   Modified Definitions: " + categorisedChangeSet.getRemovedModifiedDefinitions().size() +
				"\n\t   Modified Definitions w/ Retired Terms: " + categorisedChangeSet.getRemovedModifiedDefinitionsWithRetiredTerms().size() +
				"\n\t   Pure Removals: " + categorisedChangeSet.getPureRemovals().size() +
				"\n\t   Pure Removals w/ Retired Terms: " + categorisedChangeSet.getPureRemovalsWithRetiredTerms().size() +
				
				"\n\tIneffectual Additions: " + categorisedChangeSet.getIneffectualAdditions().size() +
				"\n\t   Rewrites: " + (categorisedChangeSet.getAddedRewrites().size()+categorisedChangeSet.getAddedPartialRewrites().size()) +
				"\n\t     Complete Rewrites: " + categorisedChangeSet.getAddedRewrites().size() +
				"\n\t     Partial Rewrites: " + categorisedChangeSet.getAddedPartialRewrites().size() +
				"\n\t   Redundancies: " + categorisedChangeSet.getAddedRedundancies().size() +
				"\n\t   Prospective Redundancies: " + categorisedChangeSet.getAddedProspectiveRedundancies().size() +
				"\n\t     Reshuffle Redundancies: " + categorisedChangeSet.getAddedReshuffleRedundancies().size() +
				"\n\t     New Redundancies: " + categorisedChangeSet.getAddedProspectiveNewRedundancies().size() +
				
				"\n\tIneffectual Removals: " + categorisedChangeSet.getIneffectualRemovals().size() +
				"\n\t   Rewrites: " + (categorisedChangeSet.getRemovedRewrites().size()+categorisedChangeSet.getRemovedPartialRewrites().size()) +
				"\n\t     Complete Rewrites: " + categorisedChangeSet.getRemovedRewrites().size() +
				"\n\t     Partial Rewrites: " + categorisedChangeSet.getRemovedPartialRewrites().size() +
				"\n\t   Redundancies: " + categorisedChangeSet.getRemovedRedundancies().size() +
				"\n\t   Prospective Redundancies: " + categorisedChangeSet.getRemovedProspectiveRedundancies().size() +
				"\n\t     Reshuffle Redundancies: " + categorisedChangeSet.getRemovedReshuffleRedundancies().size() +
				"\n\t     New Redundancies: " + categorisedChangeSet.getRemovedProspectiveNewRedundancies().size() + "\n");
	}
	
	
	/**
	 * Get an XML change report for the change set computed by this diff
	 * @return XML change report object 
	 */
	public XMLAxiomDiffReport getXMLReport() {
		if(categorisedChangeSet == null) categorisedChangeSet = getDiff();
		return new XMLAxiomDiffReport(ont1, ont2, categorisedChangeSet);
	}

	
	/**
	 * Get a CSV change report
	 * @return Change report as a CSV document
	 */
	public String getCSVChangeReport() {
		if(categorisedChangeSet == null)  categorisedChangeSet = getDiff();
		CSVAxiomDiffReport report = new CSVAxiomDiffReport();
		report.getReport(structuralChangeSet);
		report.getReport(logicalChangeSet);
		return report.getReport(categorisedChangeSet);
	}
	
	
	/**
	 * Dispose and nullify reasoner instance
	 * @param reasoner	OWL reasoner
	 */
	private void cleanUp(OWLReasoner reasoner) {
		reasoner.dispose(); reasoner = null;
	}
	
	
	/**
	 * Remove and nullify ontology instance
	 * @param ont	OWL ontology
	 */
	private void cleanUp(OWLOntology ont) {
		OWLOntologyManager man = ont.getOWLOntologyManager();
		man.removeOntology(ont); ont = null;
	}
	
	
	/**
	 * Empty and nullify map of objects
	 * @param s	Map of objects
	 */
	private void cleanUp(Map<?,?> s) {
		s.clear(); s = null;
	}
}
