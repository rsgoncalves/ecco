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
package uk.ac.manchester.cs.diff.axiom;

import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owlapi.apibinding.OWLManager;
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
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
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
import uk.ac.manchester.cs.diff.output.CSVReport;
import uk.ac.manchester.cs.diff.output.XMLReport;
import uk.ac.manchester.cs.diff.utils.ReasonerLoader;
import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasoner;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxObjectRenderer;

import com.clarkparsia.owlapi.modularity.locality.LocalityClass;
import com.clarkparsia.owlapi.modularity.locality.SyntacticLocalityEvaluator;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class CategoricalDiffConcurrent implements AxiomDiff {
	private OWLOntology ont1, ont2;
	private OWLOntologyManager man;
	private OWLDataFactory df;
	private StructuralChangeSet structuralChangeSet;
	private CategorisedChangeSet categorisedChangeSet;
	private LogicalChangeSet logicalChangeSet;
	private OWLReasoner ont1reasoner, ont2reasoner, emptyOntReasoner;
	private Set<OWLAxiom> sharedAxioms;
	private double diffTime, eaTime, erTime, iaTime, irTime;
	private boolean verbose;
	private ShortFormProvider p = new SimpleShortFormProvider();
	private ForkJoinPool eaPool, erPool, iaPool, irPool;

	/**
	 * Constructor
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 * @param verbose	Verbose mode
	 */
	public CategoricalDiffConcurrent(OWLOntology ont1, OWLOntology ont2, boolean verbose) {
		this.ont1 = ont1;
		this.ont2 = ont2;
		this.verbose = verbose;
		man = OWLManager.createOWLOntologyManager();
		df = man.getOWLDataFactory();
		emptyOntReasoner = createEmptyOntologyReasoner();
		eaPool = new ForkJoinPool(); erPool = new ForkJoinPool();
		iaPool = new ForkJoinPool(); irPool = new ForkJoinPool();
	}
	
	
	/**
	 * Constructor that takes a logical change set
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 * @param logicalChangeSet	Logical change set
	 * @param verbose	Verbose mode
	 */
	public CategoricalDiffConcurrent(OWLOntology ont1, OWLOntology ont2, LogicalChangeSet logicalChangeSet, boolean verbose) {
		this.ont1 = ont1;
		this.ont2 = ont2;
		this.logicalChangeSet = logicalChangeSet;
		this.verbose = verbose;
		man = OWLManager.createOWLOntologyManager();
		df = man.getOWLDataFactory();
		emptyOntReasoner = createEmptyOntologyReasoner();
		eaPool = new ForkJoinPool(); erPool = new ForkJoinPool();
		iaPool = new ForkJoinPool(); irPool = new ForkJoinPool();
	}
	
	
	/**
	 * Get categorised logical changes between ontologies
	 * @return Categorised logical change set
	 */
	@SuppressWarnings("deprecation")
	public CategorisedChangeSet getDiff() {
		if(categorisedChangeSet != null) return categorisedChangeSet;
		
		long start = System.currentTimeMillis();
		ont1reasoner = new ReasonerLoader(ont1).createReasoner();
		ont2reasoner = new ReasonerLoader(ont2).createReasoner();
		
		if(logicalChangeSet == null) {
			LogicalDiffConcurrent lDiff = new LogicalDiffConcurrent(ont1, ont2, verbose);
			logicalChangeSet = lDiff.getDiff(ont1reasoner, ont2reasoner);
			structuralChangeSet = lDiff.getStructuralChangeSet();
			sharedAxioms = structuralChangeSet.getShared();
		}
		
		Set<OWLAxiom> ea = logicalChangeSet.getEffectualAdditionAxioms();
		Set<OWLAxiom> er = logicalChangeSet.getEffectualRemovalAxioms();
		Set<OWLAxiom> ia = logicalChangeSet.getIneffectualAdditionAxioms();
		Set<OWLAxiom> ir = logicalChangeSet.getIneffectualRemovalAxioms();
		
		System.out.println("Computing change categorisation...");
		Set<CategorisedIneffectualAddition> ineffAdds = categoriseIneffectualAdditions(ia, er, ir);
		Set<CategorisedIneffectualRemoval> ineffRems = categoriseIneffectualRemovals(ir, ea, ia);
		Set<CategorisedEffectualAddition> effAdds = categoriseEffectualAdditions(ea, er, ir);
		Set<CategorisedEffectualRemoval> effRems = categoriseEffectualRemovals(er, ea, ia);
		
		long end = System.currentTimeMillis();
		diffTime = (end-start)/1000.0;
		
		categorisedChangeSet = new CategorisedChangeSet(effAdds, ineffAdds, effRems, ineffRems, sharedAxioms);
		categorisedChangeSet.setDiffTime(diffTime);
		categorisedChangeSet.setEffectualAdditionCategorisationTime(eaTime);
		categorisedChangeSet.setEffectualRemovalCategorisationTime(erTime);
		categorisedChangeSet.setIneffectualRemovalCategorisationTime(irTime);
		categorisedChangeSet.setIneffectualAdditionCategorisationTime(iaTime);
		
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
		System.out.println("   Categorising effectual additions... ");
		Set<CategorisedEffectualAddition> effAdds = new HashSet<CategorisedEffectualAddition>();
		if(!ea.isEmpty())
			try { effAdds = (Set<CategorisedEffectualAddition>) categoriseEffectualChanges(true, ea, ont1, er, ir); }
			catch (OWLOntologyCreationException e) { e.printStackTrace(); }
		else
			System.out.println("   done (no effectual additions)");
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
		System.out.println("   Categorising effectual removals... ");
		Set<CategorisedEffectualRemoval> effRems = new HashSet<CategorisedEffectualRemoval>();
		if(!er.isEmpty())
			try { effRems = (Set<CategorisedEffectualRemoval>) categoriseEffectualChanges(false, er, ont2, ea, ia); }
			catch (OWLOntologyCreationException e) { e.printStackTrace(); }
		else
			System.out.println("   done (no effectual removals)");
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
		System.out.println("   Categorising ineffectual additions... ");
		Set<CategorisedIneffectualAddition> ineffAdds = new HashSet<CategorisedIneffectualAddition>();
		if(!ia.isEmpty())
			try {
				ineffAdds = (Set<CategorisedIneffectualAddition>) categoriseIneffectualChanges("rhs", ia, er, ir, ont1, ont2reasoner);
			} catch (OWLOntologyCreationException e) { e.printStackTrace(); }
		else
			System.out.println("   done (no ineffectual additions)");
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
		System.out.println("   Categorising ineffectual removals... ");
		Set<CategorisedIneffectualRemoval> ineffRems = new HashSet<CategorisedIneffectualRemoval>();
		if(!ir.isEmpty())
			try {
				ineffRems = (Set<CategorisedIneffectualRemoval>) categoriseIneffectualChanges("lhs", ir, ea, ia, ont2, ont1reasoner);
			} catch (OWLOntologyCreationException e) { e.printStackTrace(); }
		else
			System.out.println("   done (no ineffectual removals)");
		return ineffRems;
	}

	
	
	/**
	 * Categorise effectual changes
	 * @param effAdds	true if checking effectual additions, false for removals
	 * @param axioms	Set of changes to categorise
	 * @param ont	if(effAdds) => ont1, else ont2
	 * @param effectual	if(effAdds) => Set of effectual removals, else Set of effectual additions
	 * @param ineffectual	if(effAdds) => Set of ineffectual removals, else Set of ineffectual additions
	 * @return Set of categorised changes
	 * @throws OWLOntologyCreationException
	 */
	private Set<? extends CategorisedEffectualChange> categoriseEffectualChanges(boolean effAdds, Set<OWLAxiom> axioms, OWLOntology ont, 
			Set<OWLAxiom> effectual, Set<OWLAxiom> ineffectual) throws OWLOntologyCreationException {
		long start = System.currentTimeMillis();
		Set<OWLEntity> ontSig = ont.getSignature();
		OWLOntologyManager man = ont.getOWLOntologyManager();
		
		SyntacticLocalityEvaluator eval = new SyntacticLocalityEvaluator(LocalityClass.BOTTOM_BOTTOM);
		
		EffecualAxiomCategoriser axCat = new EffecualAxiomCategoriser(axioms, man, ontSig, ineffectual, ont, eval, effAdds);
		Set<? extends CategorisedEffectualChange> result = null;
		if(effAdds) result = eaPool.invoke(axCat);
		else result = erPool.invoke(axCat);

		long end = System.currentTimeMillis();
		double total = (end-start)/1000.0;
		
		System.out.println("\n   done (" + total + " secs)");
		return result;
	}
	
	
	/**
	 * Axiom categorisation worker
	 */
	public class EffecualAxiomCategoriser extends RecursiveTask<Set<? extends CategorisedEffectualChange>> {
		private static final long serialVersionUID = 5592072360145135129L;
		private Set<OWLAxiom> axioms;
		private OWLOntology ont;
		private OWLOntologyManager man;
		private Set<OWLEntity> ontSig;
		private Set<OWLAxiom> ineffectual;
		private SyntacticLocalityEvaluator botEval;
		private boolean effAdds;
		private int MAX_AXIOM_SET_SIZE = 10;
		
		/**
		 * Constructor
		 * @param axioms	Set of axioms to categorise
		 * @param man	OWL ontology manager
		 * @param ontSig	Ontology signature
		 * @param ineffectual	Ineffectual changes
		 * @param effAdds	true if checking additions, false if removals
		 */
		public EffecualAxiomCategoriser(Set<OWLAxiom> axioms, OWLOntologyManager man, Set<OWLEntity> ontSig, Set<OWLAxiom> ineffectual, 
				OWLOntology ont, SyntacticLocalityEvaluator botEval, boolean effAdds) {
			this.axioms = axioms;
			this.man = man;
			this.ontSig = ontSig;
			this.ineffectual = ineffectual;
			this.ont = ont;
			this.botEval = botEval;
			this.effAdds = effAdds;
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Set<? extends CategorisedEffectualChange> compute() {
			Set result = new HashSet<CategorisedEffectualChange>();
			if(axioms.size() > MAX_AXIOM_SET_SIZE) {
				int mid = axioms.size()/2;
				OWLAxiom[] axArr = axioms.toArray(new OWLAxiom[axioms.size()]);
				Set<OWLAxiom> firstHalf = new HashSet<OWLAxiom>();
				Set<OWLAxiom> secondHalf = new HashSet<OWLAxiom>();
				
				for(int i = 0; i < mid; i++) 
					firstHalf.add(axArr[i]);
				for(int i = mid; i < axArr.length; i++)
					secondHalf.add(axArr[i]);
		
				EffecualAxiomCategoriser cat1 = new EffecualAxiomCategoriser(firstHalf, man, ontSig, ineffectual, ont, botEval, effAdds);
				cat1.fork();
				EffecualAxiomCategoriser cat2 = new EffecualAxiomCategoriser(secondHalf, man, ontSig, ineffectual, ont, botEval, effAdds);
				result.addAll(cat2.invoke());
				result.addAll(cat1.join());
			}
			else result.addAll(computeDirectly(axioms));
			return result;
		}
		
		
		/**
		 * Compute the categorisation of the given axioms
		 * @param axioms	Set of axioms to categorise
		 * @return Set of categorised changes
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private Set<? extends CategorisedEffectualChange> computeDirectly(Set<OWLAxiom> axioms) {
			FaCTPlusPlusReasoner modExtractorEff = new FaCTPlusPlusReasoner(ont, new SimpleConfiguration(), BufferingMode.NON_BUFFERING);
			Set result = null;
			if(effAdds) result = new HashSet<CategorisedEffectualAddition>();
			else result = new HashSet<CategorisedEffectualRemoval>();
			
			for(OWLAxiom ax : axioms) {
				if(ax.isLogicalAxiom()) {
					try {
						if(effAdds) result.add((CategorisedEffectualAddition)categorise(ax, modExtractorEff));
						else result.add((CategorisedEffectualRemoval)categorise(ax, modExtractorEff)); } 
					catch (OWLOntologyCreationException e) { e.printStackTrace(); }
				}
			}
			return result;
		}
		
		
		/**
		 * Categorise a given axiom
		 * @param ax	Axiom to be categorised
		 * @return Categorised axiom
		 * @throws OWLOntologyCreationException
		 */
		private CategorisedEffectualChange categorise(OWLAxiom ax, FaCTPlusPlusReasoner modExtractorEff) throws OWLOntologyCreationException {
			CategorisedEffectualChange result = null;
			boolean stOrWkAx = false, newOrRetiredAx = false, modEquiv = false;
			Set<OWLEntity> sig = ax.getSignature();
			Set<OWLEntity> newTerms = new HashSet<OWLEntity>();		// New terms in the axiom				
			for(OWLEntity e : sig) {
				if(!ontSig.contains(e) && !e.isTopEntity() && !e.isBottomEntity())
					newTerms.add(e);
			}
			// Check New or Retired Descriptions
			CategorisedEffectualChange nd = checkNewOrRetiredDescription(effAdds, man, ax, newTerms, botEval);
			if(nd != null) {
				newOrRetiredAx = true; 
				result = nd;
			}
			// Check Strengthenings or Weakenings
			if(!newOrRetiredAx) {
				CategorisedEffectualChange st = checkStrengtheningOrWeakening(effAdds, man, ax, ineffectual, newTerms);
				if(st != null) {
					stOrWkAx = true; 
					result = st;
				}
			}
			// Check Modified Definitions
			if(!newOrRetiredAx && !stOrWkAx) {
				if(ax.isOfType(AxiomType.EQUIVALENT_CLASSES)) {
					CategorisedEffectualChange me = checkModifiedDefinitions(effAdds, man, ax, modExtractorEff, newTerms);
					if(me != null) { 
						modEquiv = true; 
						result = me;
					}
				}
			}
			// Pure Alterations
			if(!newOrRetiredAx && !stOrWkAx && !modEquiv) {
				if(effAdds) {
					if(newTerms.isEmpty()) result = new CategorisedEffectualAddition(ax, EffectualAdditionCategory.PUREADDITION, new HashSet<OWLAxiom>(), newTerms);
					else result = new CategorisedEffectualAddition(ax, EffectualAdditionCategory.PUREADDITIONNT, new HashSet<OWLAxiom>(), newTerms);
				}
				else {
					if(newTerms.isEmpty()) result = new CategorisedEffectualRemoval(ax, EffectualRemovalCategory.PUREREMOVAL, new HashSet<OWLAxiom>(), newTerms);
					else result = new CategorisedEffectualRemoval(ax, EffectualRemovalCategory.PUREREMOVALRT, new HashSet<OWLAxiom>(), newTerms);
				}
			}
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
		 * @throws OWLOntologyCreationException
		 */
		private CategorisedEffectualChange checkStrengtheningOrWeakening(boolean effAdds, OWLOntologyManager man, OWLAxiom ax, Set<OWLAxiom> searchSpace, Set<OWLEntity> newTerms) {
			CategorisedEffectualChange change = null;
			OWLOntology axOnt = createOntology(ax);
			OWLReasoner reasoner = new ReasonerLoader(axOnt).createReasoner();
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
					if(effAdds) change = new CategorisedEffectualAddition(ax, EffectualAdditionCategory.STRENGTHENING, stAlignments, newTerms);
					else change = new CategorisedEffectualRemoval(ax, EffectualRemovalCategory.WEAKENING, stAlignments, newTerms);
				}
				else {
					if(effAdds) change = new CategorisedEffectualAddition(ax, EffectualAdditionCategory.STRENGTHENINGNT, stAlignments, newTerms);
					else change = new CategorisedEffectualRemoval(ax, EffectualRemovalCategory.WEAKENINGRT, stAlignments, newTerms);
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
		 * @param searchSpace	Set of axioms in the search space
		 * @param newTerms	Set of new terms used in this axiom
		 * @return New or Retired Description-type change, or null if not one
		 * @throws OWLOntologyCreationException
		 */
		private CategorisedEffectualChange checkNewOrRetiredDescription(boolean effAdds, OWLOntologyManager man, OWLAxiom ax, Set<OWLEntity> newTerms, SyntacticLocalityEvaluator eval) 
				throws OWLOntologyCreationException {
			CategorisedEffectualChange change = null;
			if(!newTerms.isEmpty()) {
				if(!(eval.isLocal(ax, newTerms))) {
					if(effAdds) change = new CategorisedEffectualAddition(ax, EffectualAdditionCategory.NEWDESCRIPTION, new HashSet<OWLAxiom>(), newTerms);
					else change = new CategorisedEffectualRemoval(ax, EffectualRemovalCategory.RETIREDDESCRIPTION, new HashSet<OWLAxiom>(), newTerms);
				}
			}
			return change;
		}
		
		
		/**
		 * Check if given change is a modified equivalence
		 * @param effAdds	true if checking additions, false if checking removals
		 * @param man	OWL ontology manager
		 * @param ax	OWL axiom to be checked
		 * @param searchSpace	Set of axioms in the search space
		 * @param modExtractor	Star module extractor
		 * @param newTerms	Set of new terms used in this axiom
		 * @return Modified definition-type change, or null if not one
		 * @throws OWLOntologyCreationException
		 */
		private CategorisedEffectualChange checkModifiedDefinitions(boolean effAdds, OWLOntologyManager man, OWLAxiom ax, 
				FaCTPlusPlusReasoner modExtractor, Set<OWLEntity> newTerms)  throws OWLOntologyCreationException {
			CategorisedEffectualChange change = null;
			Set<OWLAxiom> alignment = new HashSet<OWLAxiom>();
			OWLEquivalentClassesAxiom equiv = (OWLEquivalentClassesAxiom) ax;
			Set<OWLSubClassOfAxiom> subs = equiv.asOWLSubClassOfAxioms();

			OWLSubClassOfAxiom sub1 = (OWLSubClassOfAxiom) subs.toArray()[0];
			OWLClassExpression lhs = sub1.getSubClass();
			OWLClassExpression rhs = sub1.getSuperClass();
			Set<OWLAxiom> mod = modExtractor.getModule(ax.getSignature(), false, 2);
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
					if(newTerms.isEmpty()) change = new CategorisedEffectualAddition(ax, EffectualAdditionCategory.MODIFIEDDEFINITION, alignment, newTerms);
					else change = new CategorisedEffectualAddition(ax, EffectualAdditionCategory.MODIFIEDDEFINITIONNT, alignment, newTerms);
				}
				else {
					if(newTerms.isEmpty()) change = new CategorisedEffectualRemoval(ax, EffectualRemovalCategory.MODIFIEDDEFINITION, alignment, newTerms);
					else change = new CategorisedEffectualRemoval(ax, EffectualRemovalCategory.MODIFIEDDEFINITIONRT, alignment, newTerms);
				}
			}
			return change;
		}
		
		
		/**
		 * Create a new ontology containing the specified axiom and all entity declarations
		 * @param man	OWL ontology manager
		 * @param axiom	Axiom
		 * @return OWL ontology
		 */
		private OWLOntology createOntology(OWLAxiom axiom) {
			OWLOntology ont = null;
			try { ont = man.createOntology(Collections.singleton(axiom)); } 
			catch (OWLOntologyCreationException e) { e.printStackTrace(); }
			return ont;
		}
	}
	

	
	/**
	 * Categorise the given set of ineffectual changes
	 * @param desc	"rhs" if categorising additions, "lhs" if removals
	 * @param axioms	Axiom changes to categorise
	 * @param effectual	Set of effectual changes of the opposite ontology
	 * @param ineffectual	Set of ineffectual changes of the opposite ontology
	 * @param ont	Opposite ontology
	 * @param source	Ontology containing the given set of ineffectual axioms
	 * @return Set of categorised ineffectual changes
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Set<? extends CategorisedChange> categoriseIneffectualChanges(String desc, Set<OWLAxiom> axioms, Set<OWLAxiom> effectual, Set<OWLAxiom> ineffectual, 
			OWLOntology ont, OWLReasoner src_reasoner) throws OWLOntologyCreationException {
		Set result = null;
		if(desc.equals("rhs")) result = new HashSet<CategorisedIneffectualAddition>();
		else result = new HashSet<CategorisedIneffectualRemoval>();
		
		long start = System.currentTimeMillis();
		
		JustificationFinder just = new JustificationFinder(ont);
		Set<Set<Explanation<OWLAxiom>>> exps = null;
		try { 
			exps = just.getJustifications(axioms); 
		} catch (OWLOntologyCreationException e) { e.printStackTrace(); }
		
		long end_js = System.currentTimeMillis();
		double total2 = (end_js-start)/1000.0;
		
		IneffecualAxiomCategoriser axCat = new IneffecualAxiomCategoriser(desc, exps, effectual, ineffectual, ont, just, src_reasoner);
		if(desc.equals("lhs")) result = irPool.invoke(axCat);
		else result = iaPool.invoke(axCat);

		long end = System.currentTimeMillis();
		double total = (end-start)/1000.0;
		System.out.println("\n   done (" + total + " secs, of which " + total2 + " seconds justification finding)");
		if(desc.equals("rhs")) iaTime = total;
		else irTime = total;
		
		cleanUp(src_reasoner); cleanUp(exps); just = null;
		return result;
	}
	
	
	
	
	public class IneffecualAxiomCategoriser extends RecursiveTask<Set<? extends CategorisedChange>> {
		private static final long serialVersionUID = 828016935840247855L;
		private String desc;
		private Set<Set<Explanation<OWLAxiom>>> exps;
		private Set<OWLAxiom> effectual, ineffectual;
		private OWLOntology ont;
		private JustificationFinder just;
		private OWLReasoner src_reasoner;
		private final int MAX_AXIOM_SET_SIZE = 10;
		
		/**
		 * Constructor
		 * @param desc
		 * @param exps
		 * @param effectual
		 * @param ineffectual
		 * @param ont
		 * @param src_reasoner
		 */
		public IneffecualAxiomCategoriser(String desc, Set<Set<Explanation<OWLAxiom>>> exps, Set<OWLAxiom> effectual,
				Set<OWLAxiom> ineffectual, OWLOntology ont, JustificationFinder just, OWLReasoner src_reasoner) {
			this.desc = desc;
			this.exps = exps;
			this.effectual = effectual;
			this.ineffectual = ineffectual;
			this.ont = ont;
			this.just = just;
			this.src_reasoner = src_reasoner;
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		protected Set<? extends CategorisedChange> compute() {
			Set result = null;
			if(desc.equals("rhs")) result = new HashSet<CategorisedIneffectualAddition>();
			else result = new HashSet<CategorisedIneffectualRemoval>();
			
			if(exps.size() > MAX_AXIOM_SET_SIZE) {
				int mid = exps.size()/2;
				Set<Set<Explanation<OWLAxiom>>> firstHalf = new HashSet<Set<Explanation<OWLAxiom>>>();
				Set<Set<Explanation<OWLAxiom>>> secondHalf = new HashSet<Set<Explanation<OWLAxiom>>>();
				
				int counter = 0;
				for(Set<Explanation<OWLAxiom>> set : exps) {
					if(counter < mid) firstHalf.add(set);
					else secondHalf.add(set);
					counter++;
				}
				
				IneffecualAxiomCategoriser cat1 = new IneffecualAxiomCategoriser(desc, exps, effectual, ineffectual, ont, just, src_reasoner);
				cat1.fork();
				IneffecualAxiomCategoriser cat2 = new IneffecualAxiomCategoriser(desc, exps, effectual, ineffectual, ont, just, src_reasoner);
				result.addAll(cat2.invoke());
				result.addAll(cat1.join());
			}
			else result.addAll(computeDirectly(exps));
			return result;
		}
		
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private Set<? extends CategorisedChange> computeDirectly(Set<Set<Explanation<OWLAxiom>>> exps) {
			Set result = null;
			if(desc.equals("rhs")) result = new HashSet<CategorisedIneffectualAddition>();
			else result = new HashSet<CategorisedIneffectualRemoval>();
			
			for(Set<Explanation<OWLAxiom>> set : exps) {
				try {
					result.add(categoriseIneffectualChange(desc, set, effectual, ineffectual, ont, just, src_reasoner));
				} catch (OWLOntologyCreationException e) { e.printStackTrace(); }
			}
			return result;
		}
		
		
		
		/**
		 * Categorise the given ineffectual change
		 * @param desc	"rhs" if categorising additions, "lhs" if removals
		 * @param exps	Set of justifications for this change
		 * @param effectual	Effectual changes
		 * @param ineffectual	Ineffectual changes
		 * @param ont	Target ontology
		 * @param source	Source ontology (where changes are asserted)
		 * @param just	Justification generator interface
		 * @param src_reasoner	Reasoner instance with the source ontology loaded
		 * @return Categorised change
		 * @throws OWLOntologyCreationException
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private CategorisedChange categoriseIneffectualChange(String desc, Set<Explanation<OWLAxiom>> exps, Set<OWLAxiom> effectual, Set<OWLAxiom> ineffectual,
				OWLOntology ont, JustificationFinder just, OWLReasoner src_reasoner) throws OWLOntologyCreationException {
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
					reasoner = new ReasonerLoader(entOnt).createReasoner();
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
					updateJustificationMap(desc, justMap, explanation, "rewrite");
					rewrittenAx = true;
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
				if(prospRedundantNovelAx && !rewrittenAx && !redundancyAx) {
					Set<Set<OWLAxiom>> lacJusts = just.getLaconicJustifications(entailment, Collections.singleton(explanation));
					boolean isNew = false;
					loopExps:
					for(Set<OWLAxiom> exp : lacJusts) {
						if(!src_reasoner.isEntailed(exp))
							isNew = true; break loopExps;
					}
					if(isNew)  updateJustificationMap(desc, justMap, explanation, "novel");
					else updateJustificationMap(desc, justMap, explanation, "pseudo");
				}
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
			switch(cat) {
			case "redundant": category = IneffectualAdditionCategory.REDUNDANCY; break;
			case "rewrite": category = IneffectualAdditionCategory.REWRITE; break;
			case "reshuffle": category = IneffectualAdditionCategory.RESHUFFLEREDUNDANCY; break;
			case "novel": category = IneffectualAdditionCategory.NOVELPROSPREDUNDANCY; break;
			case "pseudo": category = IneffectualAdditionCategory.PSEUDONOVELPROSPREDUNDANCY; break;
			}
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
			switch(cat) {
			case "redundant": category = IneffectualRemovalCategory.REDUNDANCY; break;
			case "rewrite": category = IneffectualRemovalCategory.REWRITE; break;
			case "reshuffle": category = IneffectualRemovalCategory.RESHUFFLEREDUNDANCY; break;
			case "novel": category = IneffectualRemovalCategory.NOVELPROSPREDUNDANCY; break;
			case "pseudo": category = IneffectualRemovalCategory.PSEUDONOVELPROSPREDUNDANCY; break;
			}
			if(justMap.containsKey(exp)) {
				Set<IneffectualRemovalCategory> set = (Set<IneffectualRemovalCategory>) justMap.get(exp);
				set.add(category);
				justMap.put(exp, set);
			}
			else
				justMap.put(exp, new HashSet<IneffectualRemovalCategory>(Collections.singleton(category)));
		}
		
		
		/**
		 * Create a new ontology containing the specified axiom and all entity declarations
		 * @param man	OWL ontology manager
		 * @param axiom	Axiom
		 * @return OWL ontology
		 */
		private OWLOntology createOntology(OWLAxiom axiom) {
			OWLOntology ont = null;
			try { ont = man.createOntology(Collections.singleton(axiom)); } 
			catch (OWLOntologyCreationException e) { e.printStackTrace(); }
			return ont;
		}
	}
	

	/**
	 * Categorise the given set of ineffectual changes
	 * @param desc	"rhs" if categorising additions, "lhs" if removals
	 * @param axioms	Axiom changes to categorise
	 * @param effectual	Set of effectual changes of the opposite ontology
	 * @param ineffectual	Set of ineffectual changes of the opposite ontology
	 * @param ont	Opposite ontology
	 * @param source	Ontology containing the given set of ineffectual axioms
	 * @return Set of categorised ineffectual changes
	 */
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	private Set<? extends CategorisedChange> categoriseIneffectualChanges(String desc, Set<OWLAxiom> axioms, Set<OWLAxiom> effectual, Set<OWLAxiom> ineffectual, 
//			OWLOntology ont, OWLReasoner src_reasoner) {
//		Set result = null;
//		if(desc.equals("rhs")) result = new HashSet<CategorisedIneffectualAddition>();
//		else result = new HashSet<CategorisedIneffectualRemoval>();
//		
//		long start = System.currentTimeMillis();
//		
//		Justifications just = new Justifications(ont);
//		Set<Set<Explanation<OWLAxiom>>> exps = null;
//		try { 
//			exps = just.getJustifications(axioms); 
//		} catch (OWLOntologyCreationException e) { e.printStackTrace(); }
//		
//		long end_js = System.currentTimeMillis();
//		double total2 = (end_js-start)/1000.0;
//		
//		ProgressMonitor progress = new ProgressMonitor(exps);
//		int status = 0;
//		
//		for(Set<Explanation<OWLAxiom>> expsSet : exps) {
//			try {
//				result.add(categoriseIneffectualChange(desc, expsSet, effectual, ineffectual, ont, just, src_reasoner));
//			} catch (OWLOntologyCreationException e) {
//				e.printStackTrace();
//			}
//			int p = progress.incrementProgress();
//			if(p > status && verbose) { 
//				System.out.print("\r\t" + p + "%");
//				status = p;
//			}
//		}
//
//		long end = System.currentTimeMillis();
//		double total = (end-start)/1000.0;
//		System.out.println("\n   done (" + total + " secs, of which " + total2 + " seconds justification finding)");
//		if(desc.equals("rhs")) iaTime = total;
//		else irTime = total;
//		
//		cleanUp(src_reasoner); cleanUp(exps); just = null;
//		return result;
//	}
	
	
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
		
//		List<AddAxiom> declarations = new ArrayList<AddAxiom>();
//		for(OWLAxiom ax : sigDeclarations) {
//			declarations.add(new AddAxiom(emptyOnt, ax));
//		}
//		man.applyChanges(declarations);
		return new ReasonerLoader(emptyOnt).createReasoner(); 
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
	 * Get Manchester syntax of an OWL object
	 * @param obj	OWL object
	 * @param p	Short form provider
	 * @return A string with the object's conversion to Manchester syntax 
	 */
	@SuppressWarnings("unused")
	private String getManchesterRendering(OWLObject obj) {
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
		System.out.println("   Total diff time: " + diffTime + " seconds");
		System.out.println("   Categorised changes:" + 
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
				"\n\t   Rewrites: " + categorisedChangeSet.getAddedRewrites().size() +
				"\n\t   Redundancies: " + categorisedChangeSet.getAddedRedundancies().size() +
				"\n\t   Prospective Redundancies: " + categorisedChangeSet.getAddedProspectiveRedundancies().size() +
				"\n\t     Reshuffle Redundancies: " + categorisedChangeSet.getAddedReshuffleRedundancies().size() +
				"\n\t     New Redundancies: " + categorisedChangeSet.getAddedProspectiveNewRedundancies().size() +
				"\n\t       Novel Redundancies: " + categorisedChangeSet.getAddedNovelRedundancies().size() +
				"\n\t       Pseudo-Novel Redundancies: " + categorisedChangeSet.getAddedPseudoNovelRedundancies().size() +
				
				"\n\tIneffectual Removals: " + categorisedChangeSet.getIneffectualRemovals().size() +
				"\n\t   Rewrites: " + categorisedChangeSet.getRemovedRewrites().size() +
				"\n\t   Redundancies: " + categorisedChangeSet.getRemovedRedundancies().size() +
				"\n\t   Prospective Redundancies: " + categorisedChangeSet.getRemovedProspectiveRedundancies().size() +
				"\n\t     Reshuffle Redundancies: " + categorisedChangeSet.getRemovedReshuffleRedundancies().size() +
				"\n\t     New Redundancies: " + categorisedChangeSet.getRemovedProspectiveNewRedundancies().size() +
				"\n\t       Novel Redundancies: " + categorisedChangeSet.getRemovedNovelRedundancies().size() +
				"\n\t       Pseudo-Novel Redundancies: " + categorisedChangeSet.getRemovedPseudoNovelRedundancies().size());
	}
	
	
	/**
	 * Get an XML change report
	 * @return Change report as an XML document 
	 */
	public XMLReport getXMLReport() {
		if(categorisedChangeSet == null) categorisedChangeSet = getDiff();
		return new XMLReport(ont1, ont2, categorisedChangeSet);
	}

	
	/**
	 * Get a CSV change report
	 * @return Change report as a CSV document
	 */
	public String getCSVChangeReport() {
		if(categorisedChangeSet == null)  categorisedChangeSet = getDiff();
		CSVReport report = new CSVReport();
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
	 * @param man	OWL ontology manager
	 * @param ont	OWL ontology
	 */
	private void cleanUp(OWLOntology ont) {
		OWLOntologyManager man = ont.getOWLOntologyManager();
		man.removeOntology(ont); ont = null;
	}
	
	
	/**
	 * Empty and nullify set of objects
	 * @param s	Set of objects
	 */
	private void cleanUp(Set<?> s) {
		s.clear(); s = null;
	}
}
