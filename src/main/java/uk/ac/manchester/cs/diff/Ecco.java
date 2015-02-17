package uk.ac.manchester.cs.diff;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.util.OWLEntityURIConverter;
import org.semanticweb.owlapi.util.OWLEntityURIConverterStrategy;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import uk.ac.manchester.cs.diff.EccoSettings.Transformer;
import uk.ac.manchester.cs.diff.axiom.CategoricalDiff;
import uk.ac.manchester.cs.diff.axiom.LogicalDiff;
import uk.ac.manchester.cs.diff.axiom.StructuralDiff;
import uk.ac.manchester.cs.diff.axiom.changeset.AxiomChangeSet;
import uk.ac.manchester.cs.diff.axiom.changeset.CategorisedChangeSet;
import uk.ac.manchester.cs.diff.axiom.changeset.LogicalChangeSet;
import uk.ac.manchester.cs.diff.axiom.changeset.StructuralChangeSet;
import uk.ac.manchester.cs.diff.concept.ContentCVSDiff;
import uk.ac.manchester.cs.diff.concept.GrammarDiff;
import uk.ac.manchester.cs.diff.concept.SubconceptDiff;
import uk.ac.manchester.cs.diff.concept.changeset.ConceptChangeSet;
import uk.ac.manchester.cs.diff.output.OutputHandler;
import uk.ac.manchester.cs.diff.output.csv.CSVDiffReport;
import uk.ac.manchester.cs.diff.output.xml.XMLAxiomDiffReport;
import uk.ac.manchester.cs.diff.output.xml.XMLConceptDiffReport;
import uk.ac.manchester.cs.diff.output.xml.XMLDiffReport;
import uk.ac.manchester.cs.diff.output.xml.XMLUnifiedDiffReport;
import uk.ac.manchester.cs.diff.unity.WitnessJustifier;
import uk.ac.manchester.cs.diff.unity.changeset.AlignedChangeSet;
import uk.ac.manchester.cs.diff.unity.changeset.AlignedDirectChangeSet;
import uk.ac.manchester.cs.diff.unity.changeset.AlignedIndirectChangeSet;

/**
 * @author Rafael S. Goncalves <br>
 * Stanford Center for Biomedical Informatics Research (BMIR) <br>
 * School of Medicine, Stanford University <br>
 */
public class Ecco {
	private OWLOntologyManager man;
	private OWLOntology ont1, ont2;
	private EccoSettings settings;
	private AxiomChangeSet axiomChangeSet; 
	private ConceptChangeSet conceptChangeSet;
	private AlignedChangeSet alignedChangeSet;
	
	/**
	 * Constructor
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 */
	public Ecco(OWLOntology ont1, OWLOntology ont2) {
		this(ont1, ont2, new EccoSettings());
	}
	
	
	/**
	 * Constructor
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 * @param settings	ecco settings
	 */
	public Ecco(OWLOntology ont1, OWLOntology ont2, EccoSettings settings) {
		this.ont1 = ont1;
		this.ont2 = ont2;
		this.settings = settings;
		man = OWLManager.createOWLOntologyManager();
	}

	
	/**
	 * Compute diff
	 */
	public void computeDiff() {
		verifyInput();
		OutputHandler outputHandler = new OutputHandler(settings);
		XMLDiffReport diffReport = compute();
		if(settings.isSavingDocuments()) {
			outputHandler.saveXMLDocuments(diffReport, false);
			outputHandler.copySupportingDocuments();
			outputHandler.saveStringToFile(settings.getOutputDirectory(), "eccoLog.csv", getCSVLog(diffReport));
		}
	}
	
	
	/**
	 * Compute diff as specified in ecco settings between given ontologies
	 * @return XML diff report
	 */
	private XMLDiffReport compute() {
		long start = System.currentTimeMillis();
		XMLDiffReport diffReport = null;
		axiomChangeSet = getAxiomChanges();
		conceptChangeSet = getConceptChanges();
		if(axiomChangeSet != null && conceptChangeSet != null) {
			if(axiomChangeSet instanceof CategorisedChangeSet) {
				alignedChangeSet = getAlignedChanges(conceptChangeSet, (CategorisedChangeSet)axiomChangeSet);
				diffReport = getDiffXMLReport(axiomChangeSet, alignedChangeSet);
			}
		}
		if(axiomChangeSet != null) {
			settings.setTransformer(Transformer.AXIOM);
			diffReport = getDiffXMLReport(axiomChangeSet);
		}
		else if(conceptChangeSet != null) {
			settings.setTransformer(Transformer.CONCEPT);
			diffReport = getDiffXMLReport(conceptChangeSet);
		}
		long end = System.currentTimeMillis();
		System.out.println("-\nfinished diff (total time: " + (end-start)/1000.0 + " seconds)\n");
		return diffReport;
	}
	
	
	/**
	 * Check whether the given ontologies are structurally equivalent w.r.t. OWL 2's notion of structural equivalence
	 * @return true if ontologies are structurally equivalent, false otherwise
	 */
	public boolean areStructurallyEquivalent() {
		if(axiomChangeSet == null) axiomChangeSet = getStructuralAxiomChanges();
		return axiomChangeSet.isEmpty();
	}
	
	
	/**
	 * Check whether the given ontologies are logically equivalent 
	 * @return true if ontologies are logically equivalent, false otherwise
	 */
	public boolean areLogicallyEquivalent() {
		if(axiomChangeSet == null) 
			axiomChangeSet = getLogicalAxiomChanges();
		else if(axiomChangeSet instanceof StructuralChangeSet) 
			axiomChangeSet = getLogicalAxiomChanges();
		else if(axiomChangeSet instanceof CategorisedChangeSet) 
			return ((CategorisedChangeSet)axiomChangeSet).isFreeOfEffectualChanges();
		return axiomChangeSet.isEmpty();
	}
	
	
	/**
	 * Check whether any changes to the meaning of concepts were found
	 * @return true if the meaning of one or more concepts was changed, false otherwise
	 */
	public boolean foundChangesToConcepts() {
		if(conceptChangeSet == null) conceptChangeSet = getConceptChanges();
		return conceptChangeSet.isEmpty();
	}
	
	
	/**
	 * Get XML diff report of an aligned change set
	 * @param axiomChangeSet	Axiom change set
	 * @param alignedChangeSet	Aligned change set
	 * @return XML diff report
	 */
	public XMLDiffReport getDiffXMLReport(AxiomChangeSet axiomChangeSet, AlignedChangeSet alignedChangeSet) {
		return new XMLUnifiedDiffReport(ont1, ont2, axiomChangeSet, alignedChangeSet);
	}
	
	
	/**
	 * Get XML diff report of an axiom change set
	 * @param axiomChangeSet	Axiom change set
	 * @return XML diff report
	 */
	public XMLDiffReport getDiffXMLReport(AxiomChangeSet axiomChangeSet) {
		return new XMLAxiomDiffReport(ont1, ont2, axiomChangeSet);
	}
	
	
	/**
	 * Get XML diff report of a concept change set
	 * @param conceptChangeSet	Concept change set
	 * @return XML diff report
	 */
	public XMLDiffReport getDiffXMLReport(ConceptChangeSet conceptChangeSet) {
		return new XMLConceptDiffReport(conceptChangeSet);
	}
	
	
	/**
	 * Compute aligned change set
	 * @param conceptChanges	Concept change set
	 * @param axiomChanges	Axiom change set
	 * @return Aligned change set
	 */
	public AlignedChangeSet getAlignedChanges(ConceptChangeSet conceptChanges, CategorisedChangeSet axiomChanges) {
		if(alignedChangeSet != null) return alignedChangeSet;
		
		long t2 = System.currentTimeMillis();
		System.out.println("Aligning concept and axiom changes... ");
		System.out.print("   Computing justifications for (lost entailment) witness axioms... ");
		Map<OWLAxiom,Set<Explanation<OWLAxiom>>> ont1witJusts = 
				new WitnessJustifier(ont1, conceptChanges, settings.getNumberOfJustifications(), "lhs").getJustifications();
		long t3 = System.currentTimeMillis();
		System.out.println("done (" + (t3-t2)/1000.0 + " secs)");
		
		System.out.print("   Computing justifications for (new entailment) witness axioms... ");
		Map<OWLAxiom,Set<Explanation<OWLAxiom>>> ont2witJusts = 
				new WitnessJustifier(ont2, conceptChanges, settings.getNumberOfJustifications(), "rhs").getJustifications();
		System.out.println("done (" + (System.currentTimeMillis()-t3)/1000.0 + " secs)");
		
		AlignedDirectChangeSet dirChanges = 
				new AlignedDirectChangeSet(axiomChanges, conceptChanges, ont1witJusts, ont2witJusts);
		AlignedIndirectChangeSet indirChanges = 
				new AlignedIndirectChangeSet(axiomChanges, conceptChanges, ont1witJusts, ont2witJusts);
		return new AlignedChangeSet(dirChanges, indirChanges);
	}
	
	
	/**
	 * Compute concept-based diff
	 * @return Concept change set
	 */
	public ConceptChangeSet getConceptChanges() {
		switch(settings.getConceptDiffType()) {
		case ATOMIC:
			return getAtomicChanges();
		case SUBCONCEPT:
			return getSubconceptChanges();
		case GRAMMAR:
			return getGrammarBasedChanges();
		case CONTENTCVS:
			return getContentCvsBasedChanges();
		default:
			return null;
		}
	}

	
	/**
	 * Compute concept-based diff that checks which concepts have different atomic sub- or super-concepts
	 * @return Concept change set
	 */
	private ConceptChangeSet getAtomicChanges() {
		SubconceptDiff atomic_diff = new SubconceptDiff(ont1, ont2, settings.isVerbose());
		atomic_diff.setAtomicConceptDiff(true);
		return atomic_diff.getDiff();
	}

	
	/**
	 * Compute concept-based diff that checks which concepts have different (possibly complex) sub- or super-concepts.
	 * The set of concepts taken into account is composed of asserted complex concepts mentioned in axioms of either ontology
	 * @return Concept change set
	 */
	private ConceptChangeSet getSubconceptChanges() {
		SubconceptDiff subconcept_diff = new SubconceptDiff(ont1, ont2, settings.isVerbose());
		return subconcept_diff.getDiff();
	}


	/**
	 * Compute concept-based diff that checks which concepts have different complex sub- or super-concepts according to the most
	 * expressive entailment grammar. The set of complex concepts taken into account is composed of complex concepts of the form:
	 * C and D, not C, C or D, r some C, r only C. Where C, D are (possibly complex) concepts mentioned in axioms of either ontology,
	 * and r is an atomic role. This task can be very computationally demanding for large input
	 * @return Concept change set
	 */
	private ConceptChangeSet getGrammarBasedChanges() {
		GrammarDiff grammar_diff = new GrammarDiff(ont1, ont2, settings.isVerbose());
		return grammar_diff.getDiff();
	}


	/**
	 * Compute concept-based diff according to ContentCVS's entailment grammar, which is similar to <computeGrammarDiff> but replaces
	 * concepts with atomic concepts only, i.e., it does not take into account asserted complex concepts
	 * @return Concept change set
	 */
	private ConceptChangeSet getContentCvsBasedChanges() {
		ContentCVSDiff contentcvs_diff = new ContentCVSDiff(ont1, ont2, settings.isVerbose());
		return contentcvs_diff.getDiff();
	}


	/**
	 * Compute axiom based diff specified in the settings
	 * @return Axiom change set
	 */
	public AxiomChangeSet getAxiomChanges() {
		switch(settings.getAxiomDiffType()) {
		case STRUCTURAL: 
			return getStructuralAxiomChanges();
		case LOGICAL:
			return getLogicalAxiomChanges();
		case CATEGORICAL:
			return getCategorisedAxiomChanges();
		default:
			return null;
		}
	}
	
	
	/**
	 * Compute axiom-based structural diff between ontologies, i.e., whether these are structurally
	 * equivalent according to OWL 2's notion of structural equivalence
	 * @return Structural change set
	 */
	public StructuralChangeSet getStructuralAxiomChanges() {
		if(axiomChangeSet != null && axiomChangeSet instanceof StructuralChangeSet) 
			return (StructuralChangeSet)axiomChangeSet;
		StructuralDiff structural_diff = new StructuralDiff(ont1, ont2, settings.isVerbose());
		return structural_diff.getDiff();
	}
	
	
	/**
	 * Compute axiom-based logical diff between ontologies, i.e. verifies whether the given ontologies
	 * are logically equivalent
	 * @return Logical change set
	 */
	public LogicalChangeSet getLogicalAxiomChanges() {
		if(axiomChangeSet != null && axiomChangeSet instanceof LogicalChangeSet) 
			return (LogicalChangeSet)axiomChangeSet;
		LogicalDiff logical_diff = new LogicalDiff(ont1, ont2, settings.isVerbose());
		return logical_diff.getDiff();
	}
	
	
	/**
	 * Compute axiom-based diff that categorises axiom changes between ontologies according to their impact
	 * @return Categorised change set
	 */
	public CategorisedChangeSet getCategorisedAxiomChanges() {
		if(axiomChangeSet != null && axiomChangeSet instanceof CategorisedChangeSet) 
			return (CategorisedChangeSet)axiomChangeSet;
		CategoricalDiff categorical_diff = new CategoricalDiff(ont1, ont2, settings.getNumberOfJustifications(), settings.isVerbose());
		return categorical_diff.getDiff();
	}
	
	
	/**
	 * Get CSV log of changes found
	 * @param diffReport	XML diff report
	 * @return CSV log of change types and number
	 */
	public String getCSVLog(XMLDiffReport diffReport) {
		CSVDiffReport csvReport = new CSVDiffReport(diffReport);
		return csvReport.getCSV();
	}
	
	
	/**
	 * Check whether input to ecco needs alterations. By default, ecco removes
	 * unary disjointness axioms, since dependent tools do not like these
	 */
	private void verifyInput() {
		removeUnaryDisjointnessAxioms(ont1);
		removeUnaryDisjointnessAxioms(ont2);
		
		if(settings.isIgnoringAbox()) {
			removeAbox(ont1);
			removeAbox(ont2);
		}
		if(settings.isNormalizingURIs())
			normalizeEntityURIs(ont1, ont2);	
	}
	
	
	/**
	 * Normalize entity URIs, e.g.: if there exists an entity "A" in sig(O1) and sig(O2) with different URIs
	 * the diff will report changes involving "A" -- not desirable
	 * @param ont1	Ontology 1
	 * @param ont2	Ontology 2
	 */
	private void normalizeEntityURIs(OWLOntology ont1, OWLOntology ont2) {
		System.out.print("  Normalizing entity URIs... ");
		final SimpleShortFormProvider sf = new SimpleShortFormProvider();
		Set<OWLOntology> ontSet = new HashSet<OWLOntology>();
		ontSet.add(ont1); ontSet.add(ont2);

		OWLEntityURIConverter converter = new OWLEntityURIConverter(man, ontSet, new OWLEntityURIConverterStrategy() {
			@Override
			public IRI getConvertedIRI(OWLEntity arg0) {
				String entityName = sf.getShortForm(arg0);
				IRI iri = IRI.create("http://owl.cs.manchester.ac.uk/ecco#" + entityName);
				return iri;
			}
		});
		man.applyChanges(converter.getChanges());
		System.out.println("done");
	}


	/**
	 * Remove unary disjointness axioms from the given ontology
	 * @param ont	OWL ontology
	 */
	private void removeUnaryDisjointnessAxioms(OWLOntology ont) {
		List<RemoveAxiom> toRemove = new ArrayList<RemoveAxiom>();
		for(OWLAxiom ax : ont.getAxioms(AxiomType.DISJOINT_CLASSES)) {
			OWLDisjointClassesAxiom dis = (OWLDisjointClassesAxiom)ax;
			if(dis.getClassesInSignature().size() < 2)
				toRemove.add(new RemoveAxiom(ont, ax));
		}
		ont.getOWLOntologyManager().applyChanges(toRemove);
	}
	
	
	/**
	 * Remove Abox axioms from given ontology
	 * @param ont	Ontology to remove Abox axioms from
	 */
	private void removeAbox(OWLOntology ont) {
		Set<OWLAxiom> aboxAxs = ont.getABoxAxioms(true);
		List<RemoveAxiom> toRemove = new ArrayList<RemoveAxiom>();
		for(OWLAxiom ax : aboxAxs)
			toRemove.add(new RemoveAxiom(ont, ax));
		ont.getOWLOntologyManager().applyChanges(toRemove);
	}
}