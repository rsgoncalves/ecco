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
package uk.ac.manchester.cs.diff.axiom.changeset;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;

import uk.ac.manchester.cs.diff.axiom.change.CategorisedChange;
import uk.ac.manchester.cs.diff.axiom.change.CategorisedEffectualAddition;
import uk.ac.manchester.cs.diff.axiom.change.CategorisedEffectualRemoval;
import uk.ac.manchester.cs.diff.axiom.change.CategorisedIneffectualAddition;
import uk.ac.manchester.cs.diff.axiom.change.CategorisedIneffectualRemoval;
import uk.ac.manchester.cs.diff.axiom.change.CategorisedEffectualAddition.EffectualAdditionCategory;
import uk.ac.manchester.cs.diff.axiom.change.CategorisedEffectualRemoval.EffectualRemovalCategory;
import uk.ac.manchester.cs.diff.axiom.change.CategorisedIneffectualAddition.IneffectualAdditionCategory;
import uk.ac.manchester.cs.diff.axiom.change.CategorisedIneffectualRemoval.IneffectualRemovalCategory;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class CategorisedChangeSet implements ChangeSet {
	private double diffTime, eaTime, erTime, iaTime, irTime, iaJustTime, irJustTime, iaLacJustTime, irLacJustTime;
	private Set<CategorisedEffectualAddition> effectualAdditions;
	private Set<CategorisedIneffectualAddition> ineffectualAdditions; 
	private Set<CategorisedEffectualRemoval> effectualRemovals;
	private Set<CategorisedIneffectualRemoval> ineffectualRemovals;
	private Set<CategorisedEffectualAddition> strengthenings, strengtheningsnt, pureadds, pureaddsnt, newdesc, amoddef, amoddefnt;
	private Set<CategorisedEffectualRemoval> weakenings, weakeningsrt, purerems, pureremsrt, retdesc, rmoddef, rmoddefrt;
	private Set<CategorisedIneffectualAddition> arewrite, aprewrite, aredundancy, areshuffle, anovel, apseudo, aprosp, anew;
	private Set<CategorisedIneffectualRemoval> rrewrite, rprewrite, rredundancy, rreshuffle, rnovel, rpseudo, rprosp, rnew;
	private Set<OWLAxiom> sharedAxioms, effRems, ineffRems, effAdds, ineffAdds;
	
	/**
	 * Constructor
	 * @param effectualAdditions	Set of categorised effectual additions
	 * @param ineffectualAdditions	Set of categorised ineffectual additions
	 * @param effectualRemovals	Set of categorised effectual removals
	 * @param ineffectualRemovals	Set of categorised ineffectual removals
	 */
	public CategorisedChangeSet(Set<CategorisedEffectualAddition> effectualAdditions, Set<CategorisedIneffectualAddition> ineffectualAdditions, 
			Set<CategorisedEffectualRemoval> effectualRemovals, Set<CategorisedIneffectualRemoval> ineffectualRemovals, Set<OWLAxiom> sharedAxioms) {
		this.effectualAdditions = effectualAdditions;
		this.ineffectualAdditions = ineffectualAdditions;
		this.effectualRemovals = effectualRemovals;
		this.ineffectualRemovals = ineffectualRemovals;
		this.sharedAxioms = sharedAxioms;
	}
	
	
	/**
	 * Set the effectual additions categorisation time
	 * @param time	Time (in seconds)
	 * @deprecated
	 */
	public void setEffectualAdditionCategorisationTime(double time) {
		eaTime = time;
	}
	
	
	/**
	 * Set the effectual removals categorisation time
	 * @param time	Time (in seconds)
	 * @deprecated
	 */
	public void setEffectualRemovalCategorisationTime(double time) {
		erTime = time;
	}
	
	
	/**
	 * Set the ineffectual removals categorisation time
	 * @param time	Time (in seconds)
	 * @deprecated
	 */
	public void setIneffectualRemovalCategorisationTime(double time) {
		irTime = time;
	}
	
	
	/**
	 * Set the ineffectual removals categorisation time
	 * @param time	Time (in seconds)
	 * @deprecated
	 */
	public void setIneffectualAdditionCategorisationTime(double time) {
		iaTime = time;
	}
	
	
	/**
	 * Set the ineffectual addition justification finding time
	 * @param time	Time (in seconds)
	 * @deprecated
	 */
	public void setIneffectualAdditionJustificationFindingTime(double time) {
		iaJustTime = time;
	}
	
	
	/**
	 * Set the ineffectual removal justification finding time
	 * @param time	Time (in seconds)
	 * @deprecated
	 */
	public void setIneffectualRemovalJustificationFindingTime(double time) {
		irJustTime = time;
	}
	
	
	/**
	 * Set the ineffectual addition laconic justification finding time
	 * @param time	Time (in seconds)
	 * @deprecated
	 */
	public void setIneffectualAdditionLaconicJustificationFindingTime(double time) {
		iaLacJustTime = time;
	}
	
	
	/**
	 * Set the ineffectual removal laconic justification finding time
	 * @param time	Time (in seconds)
	 * @deprecated
	 */
	public void setIneffectualRemovalLaconicJustificationFindingTime(double time) {
		irLacJustTime = time;
	}
	
	
	/**
	 * Get the effectual additions categorisation time
	 * @return Categorisation time (in seconds)
	 */
	public double getEffectualAdditionCategorisationTime() {
		return eaTime;
	}
	
	
	/**
	 * Get the effectual removals categorisation time
	 * @return Categorisation time (in seconds)
	 */
	public double getEffectualRemovalCategorisationTime() {
		return erTime;
	}
	
	
	/**
	 * Get the ineffectual removals categorisation time
	 * @return Categorisation time (in seconds)
	 */
	public double getIneffectualRemovalCategorisationTime() {
		return irTime;
	}
	
	
	/**
	 * Get the ineffectual additions categorisation time
	 * @return Categorisation time (in seconds)
	 */
	public double getIneffectualAdditionCategorisationTime() {
		return iaTime;
	}
	
	
	/**
	 * Get the ineffectual additions justification finding time
	 * @return Justification finding time (in seconds)
	 */
	public double getIneffectualAdditionJustificationFindingTime() {
		return iaJustTime;
	}
	
	
	/**
	 * Get the ineffectual removals justification finding time
	 * @return Justification finding time (in seconds)
	 */
	public double getIneffectualRemovalJustificationFindingTime() {
		return irJustTime;
	}
	
	
	/**
	 * Get the ineffectual additions laconic justification finding time
	 * @return Justification finding time (in seconds)
	 */
	public double getIneffectualAdditionLaconicJustificationFindingTime() {
		return iaLacJustTime;
	}
	
	
	/**
	 * Get the ineffectual removals laconic justification finding time
	 * @return Justification finding time (in seconds)
	 */
	public double getIneffectualRemovalLaconicJustificationFindingTime() {
		return irLacJustTime;
	}
	
	
	/**
	 * Instantiate the effectual addition categories sets and populate them accordingly
	 */
	private void sortOutEffectualAdditions() {
		instantiateEASets();
		for(CategorisedEffectualAddition c : effectualAdditions) {
			if(c.getCategory().equals(EffectualAdditionCategory.STRENGTHENING))
				strengthenings.add(c);
			else if(c.getCategory().equals(EffectualAdditionCategory.STRENGTHENINGNT))
				strengtheningsnt.add(c);
			else if(c.getCategory().equals(EffectualAdditionCategory.PUREADDITION))
				pureadds.add(c);
			else if(c.getCategory().equals(EffectualAdditionCategory.PUREADDITIONNT))
				pureaddsnt.add(c);
			else if(c.getCategory().equals(EffectualAdditionCategory.NEWDESCRIPTION))
				newdesc.add(c);
			else if(c.getCategory().equals(EffectualAdditionCategory.MODIFIEDDEFINITION))
				amoddef.add(c);
			else if(c.getCategory().equals(EffectualAdditionCategory.MODIFIEDDEFINITIONNT))
				amoddefnt.add(c);
		}
	}
	
	
	/**
	 * Instantiate the effectual removal categories sets and populate them accordingly
	 */
	private void sortOutEffectualRemovals() {
		instantiateERSets();
		for(CategorisedEffectualRemoval c : effectualRemovals) {
			if(c.getCategory().equals(EffectualRemovalCategory.WEAKENING))
				weakenings.add(c);
			else if(c.getCategory().equals(EffectualRemovalCategory.WEAKENINGRT))
				weakeningsrt.add(c);
			else if(c.getCategory().equals(EffectualRemovalCategory.PUREREMOVAL))
				purerems.add(c);
			else if(c.getCategory().equals(EffectualRemovalCategory.PUREREMOVALRT))
				pureremsrt.add(c);
			else if(c.getCategory().equals(EffectualRemovalCategory.RETIREDDESCRIPTION))
				retdesc.add(c);
			else if(c.getCategory().equals(EffectualRemovalCategory.MODIFIEDDEFINITION))
				rmoddef.add(c);
			else if(c.getCategory().equals(EffectualRemovalCategory.MODIFIEDDEFINITIONRT))
				rmoddefrt.add(c);
		}
	}
	
	
	/**
	 * Instantiate the ineffectual addition categories sets and populate them accordingly
	 */
	private void sortOutIneffectualAdditions() {
		instantiateIASets();
		for(CategorisedIneffectualAddition c : ineffectualAdditions) {
			for(IneffectualAdditionCategory cat : c.getCategories()) {
				if(cat.equals(IneffectualAdditionCategory.REWRITE))
					arewrite.add(c);
				if(cat.equals(IneffectualAdditionCategory.PREWRITE))
					aprewrite.add(c);
				if(cat.equals(IneffectualAdditionCategory.REDUNDANCY))
					aredundancy.add(c);
				if(cat.equals(IneffectualAdditionCategory.NOVELPROSPREDUNDANCY)) {
					anovel.add(c);
					anew.add(c);
					aprosp.add(c);
				}
				if(cat.equals(IneffectualAdditionCategory.PSEUDONOVELPROSPREDUNDANCY)) {
					apseudo.add(c);
					anew.add(c);
					aprosp.add(c);
				}
				if(cat.equals(IneffectualAdditionCategory.RESHUFFLEREDUNDANCY)) {
					areshuffle.add(c);
					aprosp.add(c);
				}
			}
		}
	}
	
	
	/**
	 * Instantiate the ineffectual removal categories sets and populate them accordingly
	 */
	private void sortOutIneffectualRemovals() {
		instantiateIRSets();
		for(CategorisedIneffectualRemoval c : ineffectualRemovals) {
			for(IneffectualRemovalCategory cat : c.getCategories()) {
				if(cat.equals(IneffectualRemovalCategory.REWRITE))
					rrewrite.add(c);
				if(cat.equals(IneffectualRemovalCategory.PREWRITE))
					rprewrite.add(c);
				if(cat.equals(IneffectualRemovalCategory.REDUNDANCY))
					rredundancy.add(c);
				if(cat.equals(IneffectualRemovalCategory.NOVELPROSPREDUNDANCY)) {
					rnovel.add(c);
					rnew.add(c);
					rprosp.add(c);
				}
				if(cat.equals(IneffectualRemovalCategory.PSEUDONOVELPROSPREDUNDANCY)) {
					rpseudo.add(c);
					rnew.add(c);
					rprosp.add(c);
				}
				if(cat.equals(IneffectualRemovalCategory.RESHUFFLEREDUNDANCY)) {
					rreshuffle.add(c);
					rprosp.add(c);
				}
			}
		}
	}
	
	
	/**
	 * Get the set of shared axioms
	 * @return Set of shared axioms
	 */
	public Set<OWLAxiom> getSharedAxioms() {
		return sharedAxioms;
	}
	
	
	/**
	 * Get the set of strengthenings from the change set
	 * @return Set of strengthenings in the change set
	 */
	public Set<CategorisedEffectualAddition> getStrengthenings() {
		if(strengthenings == null) sortOutEffectualAdditions();
		return strengthenings;
	}
	
	
	/**
	 * Get the set of strengthenings with new terms from the change set
	 * @return Set of strengthenings with new terms in the change set
	 */
	public Set<CategorisedEffectualAddition> getStrengtheningsWithNewTerms() {
		if(strengtheningsnt == null) sortOutEffectualAdditions();
		return strengtheningsnt;
	}
	
	
	/**
	 * Get the set of pure additions from the change set
	 * @return Set of pure additions in the change set
	 */
	public Set<CategorisedEffectualAddition> getPureAdditions() {
		if(pureadds == null) sortOutEffectualAdditions();
		return pureadds;
	}
	
	
	/**
	 * Get the set of pure additions with new terms from the change set
	 * @return Set of pure additions with new terms in the change set
	 */
	public Set<CategorisedEffectualAddition> getPureAdditionsWithNewTerms() {
		if(pureaddsnt == null) sortOutEffectualAdditions();
		return pureaddsnt;
	}
	
	
	/**
	 * Get the set of new descriptions from the change set
	 * @return Set of new descriptions in the change set
	 */
	public Set<CategorisedEffectualAddition> getNewDescriptions() {
		if(newdesc == null) sortOutEffectualAdditions();
		return newdesc;
	}
	
	
	/**
	 * Get the set of added modified definitions from the change set
	 * @return Set of added modified definitions in the change set
	 */
	public Set<CategorisedEffectualAddition> getAddedModifiedDefinitions() {
		if(amoddef == null) sortOutEffectualAdditions();
		return amoddef;
	}
	
	
	/**
	 * Get the set of added modified definitions with new terms from the change set
	 * @return Set of added modified definitions with new terms in the change set
	 */
	public Set<CategorisedEffectualAddition> getAddedModifiedDefinitionsWithNewTerms() {
		if(amoddefnt == null) sortOutEffectualAdditions();
		return amoddefnt;
	}
	
	
	/**
	 * Get the set of weakenings from the change set
	 * @return Set of weakenings in the change set
	 */
	public Set<CategorisedEffectualRemoval> getWeakenings() {
		if(weakenings == null) sortOutEffectualRemovals();
		return weakenings;
	}
	
	
	/**
	 * Get the set of weakenings with retired terms from the change set
	 * @return Set of weakenings with retired terms in the change set
	 */
	public Set<CategorisedEffectualRemoval> getWeakeningsWithRetiredTerms() {
		if(weakeningsrt == null) sortOutEffectualRemovals();
		return weakeningsrt;
	}
	
	
	/**
	 * Get the set of pure removals from the change set
	 * @return Set of pure removals in the change set
	 */
	public Set<CategorisedEffectualRemoval> getPureRemovals() {
		if(purerems == null) sortOutEffectualRemovals();
		return purerems;
	}
	
	
	/**
	 * Get the set of pure removals with retired terms from the change set
	 * @return Set of pure removals with retired terms in the change set
	 */
	public Set<CategorisedEffectualRemoval> getPureRemovalsWithRetiredTerms() {
		if(pureremsrt == null) sortOutEffectualRemovals();
		return pureremsrt;
	}
	
	
	/**
	 * Get the set of retired descriptions from the change set
	 * @return Set of retired descriptions in the change set
	 */
	public Set<CategorisedEffectualRemoval> getRetiredDescriptions() {
		if(retdesc == null) sortOutEffectualRemovals();
		return retdesc;
	}
	
	
	/**
	 * Get the set of removed modified definitions from the change set
	 * @return Set of removed modified definitions in the change set
	 */
	public Set<CategorisedEffectualRemoval> getRemovedModifiedDefinitions() {
		if(rmoddef == null) sortOutEffectualRemovals();
		return rmoddef;
	}
	
	
	/**
	 * Get the set of removed modified definitions with retired terms from the change set
	 * @return Set of removed modified definitions with retired terms in the change set
	 */
	public Set<CategorisedEffectualRemoval> getRemovedModifiedDefinitionsWithRetiredTerms() {
		if(rmoddefrt == null) sortOutEffectualRemovals();
		return rmoddefrt;
	}
	
	
	/**
	 * Get the set of added rewrites
	 * @return Set of added rewrites
	 */
	public Set<CategorisedIneffectualAddition> getAddedRewrites() {
		if(arewrite == null) sortOutIneffectualAdditions();
		return arewrite;
	}
	
	
	/**
	 * Get the set of added partial rewrites
	 * @return Set of added partial rewrites
	 */
	public Set<CategorisedIneffectualAddition> getAddedPartialRewrites() {
		if(aprewrite == null) sortOutIneffectualAdditions();
		return aprewrite;
	}
	
	
	/**
	 * Get the set of added redundancies
	 * @return Set of added redundancies
	 */
	public Set<CategorisedIneffectualAddition> getAddedRedundancies() {
		if(aredundancy == null) sortOutIneffectualAdditions();
		return aredundancy;
	}
	

	/**
	 * Get the set of added prospective redundancies
	 * @return Set of added prospective redundancies
	 */
	public Set<CategorisedIneffectualAddition> getAddedProspectiveRedundancies() {
		if(aprosp == null) sortOutIneffectualAdditions();
		return aprosp;
	}
	
	
	/**
	 * Get the set of added reshuffle redundancies
	 * @return Set of added reshuffle redundancies
	 */
	public Set<CategorisedIneffectualAddition> getAddedReshuffleRedundancies() {
		if(areshuffle == null) sortOutIneffectualAdditions();
		return areshuffle;
	}
	
	
	/**
	 * Get the set of added prospective new redundancies
	 * @return Set of added prospective new redundancies
	 */
	public Set<CategorisedIneffectualAddition> getAddedProspectiveNewRedundancies() {
		if(anew == null) sortOutIneffectualAdditions();
		return anew;
	}
	
	
	/**
	 * Get the set of added prospective novel redundancies
	 * @return Set of added prospective novel redundancies
	 */
	public Set<CategorisedIneffectualAddition> getAddedNovelRedundancies() {
		if(anovel == null) sortOutIneffectualAdditions();
		return anovel;
	}
	
	
	/**
	 * Get the set of added prospective pseudo-novel redundancies
	 * @return Set of added prospective pseudo-novel redundancies
	 */
	public Set<CategorisedIneffectualAddition> getAddedPseudoNovelRedundancies() {
		if(apseudo == null) sortOutIneffectualAdditions();
		return apseudo;
	}
	
	
	/**
	 * Get the set of removed rewrites
	 * @return Set of removed rewrites
	 */
	public Set<CategorisedIneffectualRemoval> getRemovedRewrites() {
		if(rrewrite == null) sortOutIneffectualRemovals();
		return rrewrite;
	}
	
	
	/**
	 * Get the set of added partial rewrites
	 * @return Set of added partial rewrites
	 */
	public Set<CategorisedIneffectualRemoval> getRemovedPartialRewrites() {
		if(rprewrite == null) sortOutIneffectualRemovals();
		return rprewrite;
	}
	
	
	/**
	 * Get the set of removed redundancies
	 * @return Set of removed redundancies
	 */
	public Set<CategorisedIneffectualRemoval> getRemovedRedundancies() {
		if(rredundancy == null) sortOutIneffectualRemovals();
		return rredundancy;
	}
	

	/**
	 * Get the set of removed prospective redundancies
	 * @return Set of removed prospective redundancies
	 */
	public Set<CategorisedIneffectualRemoval> getRemovedProspectiveRedundancies() {
		if(rprosp == null) sortOutIneffectualRemovals();
		return rprosp;
	}
	
	
	/**
	 * Get the set of removed reshuffle redundancies
	 * @return Set of removed reshuffle redundancies
	 */
	public Set<CategorisedIneffectualRemoval> getRemovedReshuffleRedundancies() {
		if(rreshuffle == null) sortOutIneffectualRemovals();
		return rreshuffle;
	}
	
	
	/**
	 * Get the set of removed prospective new redundancies
	 * @return Set of removed prospective new redundancies
	 */
	public Set<CategorisedIneffectualRemoval> getRemovedProspectiveNewRedundancies() {
		if(rnew == null) sortOutIneffectualRemovals();
		return rnew;
	}
	
	
	/**
	 * Get the set of removed prospective novel redundancies
	 * @return Set of removed prospective novel redundancies
	 */
	public Set<CategorisedIneffectualRemoval> getRemovedNovelRedundancies() {
		if(rnovel == null) sortOutIneffectualRemovals();
		return rnovel;
	}
	
	
	/**
	 * Get the set of removed prospective pseudo-novel redundancies
	 * @return Set of removed prospective pseudo-novel redundancies
	 */
	public Set<CategorisedIneffectualRemoval> getRemovedPseudoNovelRedundancies() {
		if(rpseudo == null) sortOutIneffectualRemovals();
		return rpseudo;
	}
	
	
	/**
	 * Get the set of categorised effectual additions
	 * @return Set of categorised effectual additions
	 */
	public Set<CategorisedEffectualAddition> getCategorisedEffectualAdditions() {
		return effectualAdditions;
	}
	
	
	/**
	 * Get the set of categorised ineffectual additions
	 * @return Set of categorised ineffectual additions
	 */
	public Set<CategorisedIneffectualAddition> getCategorisedIneffectualAdditions() {
		return ineffectualAdditions;
	}
	
	
	/**
	 * Get the set of categorised ineffectual removals
	 * @return Set of categorised ineffectual removals
	 */
	public Set<CategorisedIneffectualRemoval> getCategorisedIneffectualRemovals() {
		return ineffectualRemovals;
	}
	
	
	/**
	 * Get the set of categorised effectual removals
	 * @return Set of categorised effectual removals
	 */
	public Set<CategorisedEffectualRemoval> getCategorisedEffectualRemovals() {
		return effectualRemovals;
	}
	
	
	/**
	 * Set diff time 
	 * @param time	Diff time
	 * @deprecated
	 */
	public void setDiffTime(double time) {
		diffTime = time;
	}
	
	
	/**
	 * Get the CPU time (in seconds) spent in structural diff
	 * @return CPU time (in seconds) spent in structural diff
	 */
	public double getDiffTime() {
		return diffTime;
	}
	
	
	/**
	 * Get the set of effectual and ineffectual additions in the change set
	 * @return Set of effectual and ineffectual additions in the change set
	 */
	public Set<CategorisedChange> getAdditions() {
		Set<CategorisedChange> result = new HashSet<CategorisedChange>();
		result.addAll(effectualAdditions);
		result.addAll(ineffectualAdditions);
		return result;
	}
	
	
	/**
	 * Get the set of categorised effectual additions
	 * @return Set of categorised effectual additions
	 */
	public Set<? extends CategorisedChange> getEffectualAdditions() {
		return effectualAdditions;
	}
	
	
	/**
	 * Get the set of effectual addition axioms
	 * @return Set of effectual addition axioms
	 */
	public Set<OWLAxiom> getEffectualAdditionAxioms() {
		if(effAdds == null) {
			effAdds = new HashSet<OWLAxiom>();
			for(CategorisedChange c : effectualAdditions) {
				effAdds.add(c.getAxiom());
			}
		}
		return effAdds;
	}
	
	
	/**
	 * Get the set of categorised effectual removals
	 * @return Set of categorised effectual removals
	 */
	public Set<? extends CategorisedChange> getEffectualRemovals() {
		return effectualRemovals;
	}
	
	
	/**
	 * Get the set of effectual removal axioms
	 * @return Set of effectual removal axioms
	 */
	public Set<OWLAxiom> getEffectualRemovalAxioms() {
		if(effRems == null) {
			effRems = new HashSet<OWLAxiom>();
			for(CategorisedChange c : effectualRemovals) {
				effRems.add(c.getAxiom());
			}
		}
		return effRems;
	}
	
	
	/**
	 * Get the set of categorised ineffectual removals
	 * @return Set of categorised ineffectual removals
	 */
	public Set<? extends CategorisedChange> getIneffectualRemovals() {
		return ineffectualRemovals;
	}
	
	
	/**
	 * Get the set of ineffectual removal axioms
	 * @return Set of ineffectual removal axioms
	 */
	public Set<OWLAxiom> getIneffectualRemovalAxioms() {
		if(ineffRems == null) {
			ineffRems = new HashSet<OWLAxiom>();
			for(CategorisedChange c : ineffectualRemovals) {
				ineffRems.add(c.getAxiom());
			}
		}
		return ineffRems;
	}
	
	
	/**
	 * Get the set of categorised ineffectual additions
	 * @return Set of categorised ineffectual additions
	 */
	public Set<? extends CategorisedChange> getIneffectualAdditions() {
		return ineffectualAdditions;
	}
	
	
	/**
	 * Get the set of ineffectual addition axioms
	 * @return Set of ineffectual addition axioms
	 */
	public Set<OWLAxiom> getIneffectualAdditionAxioms() {
		if(ineffAdds == null) {
			ineffAdds = new HashSet<OWLAxiom>();
			for(CategorisedChange c : ineffectualAdditions) {
				ineffAdds.add(c.getAxiom());
			}
		}
		return ineffAdds;
	}
	
	
	/**
	 * Get the set of effectual and ineffectual removals in the change set
	 * @return Set of effectual and ineffectual removals in the change set
	 */
	public Set<CategorisedChange> getRemovals() {
		Set<CategorisedChange> result = new HashSet<CategorisedChange>();
		result.addAll(effectualRemovals);
		result.addAll(ineffectualRemovals);
		return result;
	}
	
	
	/**
	 * Check if change set contains no changes
	 * @return true if change set contains no changes, false otherwise
	 */
	public boolean isEmpty() {
		if(effectualAdditions.isEmpty() && effectualRemovals.isEmpty() 
				&& ineffectualAdditions.isEmpty() && ineffectualAdditions.isEmpty())
			return true;
		else
			return false;
	}
	
	
	/**
	 * Instantiate the effectual addition sets
	 */
	private void instantiateEASets() {
		strengthenings = new HashSet<CategorisedEffectualAddition>();
		strengtheningsnt = new HashSet<CategorisedEffectualAddition>();
		pureadds = new HashSet<CategorisedEffectualAddition>();
		pureaddsnt = new HashSet<CategorisedEffectualAddition>();
		newdesc = new HashSet<CategorisedEffectualAddition>();
		amoddef = new HashSet<CategorisedEffectualAddition>();
		amoddefnt = new HashSet<CategorisedEffectualAddition>();
	}
	
	
	/**
	 * Instantiate the effectual removal sets
	 */
	private void instantiateERSets() {
		weakenings = new HashSet<CategorisedEffectualRemoval>();
		weakeningsrt = new HashSet<CategorisedEffectualRemoval>();
		purerems = new HashSet<CategorisedEffectualRemoval>();
		pureremsrt = new HashSet<CategorisedEffectualRemoval>();
		retdesc = new HashSet<CategorisedEffectualRemoval>();
		rmoddef = new HashSet<CategorisedEffectualRemoval>();
		rmoddefrt = new HashSet<CategorisedEffectualRemoval>();
	}
	
	
	/**
	 * Instantiate the ineffectual addition sets
	 */
	private void instantiateIASets() {
		arewrite = new HashSet<CategorisedIneffectualAddition>();
		aprewrite = new HashSet<CategorisedIneffectualAddition>();
		aredundancy = new HashSet<CategorisedIneffectualAddition>();
		areshuffle = new HashSet<CategorisedIneffectualAddition>();
		anovel = new HashSet<CategorisedIneffectualAddition>();
		apseudo = new HashSet<CategorisedIneffectualAddition>();
		aprosp = new HashSet<CategorisedIneffectualAddition>();
		anew = new HashSet<CategorisedIneffectualAddition>();
	}
	
	
	/**
	 * Instantiate the ineffectual removal sets
	 */
	private void instantiateIRSets() {
		rrewrite = new HashSet<CategorisedIneffectualRemoval>();
		rprewrite = new HashSet<CategorisedIneffectualRemoval>();
		rredundancy = new HashSet<CategorisedIneffectualRemoval>();
		rreshuffle = new HashSet<CategorisedIneffectualRemoval>();
		rnovel = new HashSet<CategorisedIneffectualRemoval>();
		rpseudo = new HashSet<CategorisedIneffectualRemoval>();
		rprosp = new HashSet<CategorisedIneffectualRemoval>();
		rnew = new HashSet<CategorisedIneffectualRemoval>();
	}
}
