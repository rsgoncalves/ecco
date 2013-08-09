package uk.ac.manchester.cs.diff.utils;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.diff.concept.GrammarDiffv1;

public class ConceptDiffRunner {

	/**
	 * Tester class
	 * @param args
	 * @throws OWLOntologyCreationException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws OWLOntologyCreationException, InterruptedException {
		File f1 = new File(args[0]), f2 = new File(args[1]);
		OWLOntologyManager man1 = OWLManager.createOWLOntologyManager(), man2 = OWLManager.createOWLOntologyManager();
		
		OWLOntology ont1 = man1.loadOntologyFromOntologyDocument(f1);
		System.out.println("Loaded ontology 1: " + f1.getAbsolutePath());
		
		OWLOntology ont2 = man2.loadOntologyFromOntologyDocument(f2);
		System.out.println("Loaded ontology 2: " + f2.getAbsolutePath());
		
		// Remove abox for NCIt
		man1.removeAxioms(ont1, ont1.getABoxAxioms(true)); man2.removeAxioms(ont2, ont2.getABoxAxioms(true));

		// Get random signature sample
		SignatureSampler sampler = new SignatureSampler(ont2);
		
		// Instantiate diff
		GrammarDiffv1 diff = new GrammarDiffv1(ont1, ont2, sampler.getSample(382), 
				"/Users/rafa/Documents/PhD/workspace/ecco/test", true);
		diff.getDiff();
	}
}
