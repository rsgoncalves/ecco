package uk.ac.manchester.cs.diff;

import java.io.File;

/**
 * @author Rafael S. Goncalves <br>
 * Stanford Center for Biomedical Informatics Research (BMIR) <br>
 * School of Medicine, Stanford University <br>
 */
public class EccoSettings {
	private boolean saveDocuments, ignoreAbox, processImports, normalizeURIs, transform, verbose;
	private AxiomDiffType axiomDiffType;
	private ConceptDiffType conceptDiffType;
	private Transformer transformer;
	private String outputDir, xsltPath;
	private int nrJusts;
	
	
	/**
	 * Settings constructor: initiates all settings fields with default behavior
	 */
	public EccoSettings() {
		axiomDiffType = AxiomDiffType.CATEGORICAL;
		conceptDiffType = ConceptDiffType.ATOMIC;
		transformer = Transformer.UNITY;
		outputDir = "ecco-output";
		nrJusts = 10;
		saveDocuments = true;
		ignoreAbox = false;
		processImports = true;
		normalizeURIs = false;
		transform = false;
		verbose = false;
	}
	
	
	/* Setters */
	
	
	/**
	 * Set axiom diff type
	 * @param type	Axiom diff type
	 */
	public void setAxiomDiffType(AxiomDiffType type) {
		axiomDiffType = type;
	}
	
	
	/**
	 * Set concept diff type
	 * @param type	Concept diff type
	 */
	public void setConceptDiffType(ConceptDiffType type) {
		conceptDiffType = type;
	}
	
	
	/**
	 * Set concept diff type
	 * @param type	String representing concept diff type
	 */
	public void setConceptDiffType(String type) {
		ConceptDiffType[] diffTypes = ConceptDiffType.values();
		for(int i = 0; i < diffTypes.length; i++) {
			ConceptDiffType diffType = diffTypes[i];
			if(diffType.name().equalsIgnoreCase(type))
				conceptDiffType = diffType;
		}
	}
	
	
	/**
	 * Set output directory
	 * @param outputDir	Output directory
	 */
	public void setOutputDirectory(String outputDir) {
		if(!outputDir.endsWith(File.separator)) outputDir += File.separator;
		this.outputDir = outputDir;
	}
	
	
	/**
	 * Set XSLT file path
	 * @param xsltPath	XSLT file path
	 */
	public void setXSLTFilePath(String xsltPath) {
		this.xsltPath = xsltPath;
	}
	
	
	/**
	 * Set number of justifications to be computed per change
	 * @param nrJusts	Number of justifications
	 */
	public void setNumberOfJustifications(int nrJusts) {
		this.nrJusts = nrJusts;
	}
	
	
	/**
	 * Set whether output XML documents should be serialised 
	 * @param saveDocuments	true if documents should be saved, false otherwise
	 */
	public void setSaveDocuments(boolean saveDocuments) {
		this.saveDocuments = saveDocuments;
	}
	
	
	/**
	 * Set whether ontology ABoxes should be ignored
	 * @param ignoreAbox	true if ABoxes should be ignored, false otherwise
	 */
	public void setIgnoreAbox(boolean ignoreAbox) {
		this.ignoreAbox = ignoreAbox;
	}

	
	/**
	 * Set whether imports should be processed or ignored
	 * @param processImports	true if imports should be processed, false otherwise
	 */
	public void setProcessImports(boolean processImports) {
		this.processImports = processImports;
	}
	
	
	/**
	 * Set whether URI's should be normalized into a common namespace
	 * @param normalizeURIs	true if URIs should be normalized, false otherwise
	 */
	public void setNormalizeURIs(boolean normalizeURIs) {
		this.normalizeURIs = normalizeURIs;
	}
	
	
	/**
	 * Set whether XML change sets should be tranformed to HTML
	 * @param transform	true if XML files should be transformed into HTML, false otherwise
	 */
	public void setTransformToHTML(boolean transform) {
		this.transform = transform;
	}
	
	
	/**
	 * Set the type of XSLT to be used
	 * @param transformer	Transform option
	 */
	public void setTransformer(Transformer transformer) {
		this.transformer = transformer;
	}
	
	
	/**
	 * Set whether verbose mode is on
	 * @param verbose	true if verbose, false otherwise
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	
	/* Getters */
	
	
	/**
	 * Get axiom diff type
	 * @return Axiom diff type
	 */
	public AxiomDiffType getAxiomDiffType() {
		return axiomDiffType;
	}


	/**
	 * Get concept diff type
	 * @return Concept diff type
	 */
	public ConceptDiffType getConceptDiffType() {
		return conceptDiffType;
	}
	
	
	/**
	 * Get the type of transformation to be performed
	 * @return Type of transformation to be performed
	 */
	public Transformer getTransformType() {
		return transformer;
	}


	/**
	 * Get number of justifications to be computed per change
	 * @return Number of justifications
	 */
	public int getNumberOfJustifications() {
		return nrJusts;
	}
	
	
	/**
	 * Get output directory
	 * @return Output directory
	 */
	public String getOutputDirectory() {
		return outputDir;
	}

	
	/**
	 * Get XSLT file path
	 * @return XSLT file path
	 */
	public String getXSLTFilePath() {
		return xsltPath;
	}
	
	
	/**
	 * Check whether documents are set to be saved
	 * @return true if documents are set to be saved, false otherwise
	 */
	public boolean isSavingDocuments() {
		return saveDocuments;
	}
	
	
	/**
	 * Check whether ABoxes are being ignored
	 * @return true if ABoxes are being ignored, false otherwise
	 */
	public boolean isIgnoringAbox() {
		return ignoreAbox;
	}
	
	
	/**
	 * Check whether imports are being processed
	 * @return true if imports are being processed, false otherwise
	 */
	public boolean isProcessingImports() {
		return processImports;
	}
	
	
	/**
	 * Check whether URIs are being normalized
	 * @return true if URIs are being normalized, false otherwise
	 */
	public boolean isNormalizingURIs() {
		return normalizeURIs;
	}
	
	
	/**
	 * Check whether ecco is set to transform XML change sets to HTML
	 * @return true if XML change sets are set to be transformed to HTML, false otherwise
	 */
	public boolean isTransformingToHTML() {
		return transform;
	}
	
	
	/**
	 * Check whether verbose output mode is on
	 * @return true if verbose mode is on, false otherwise
	 */
	public boolean isVerbose() {
		return verbose;
	}
	

	/**
	 * @author Rafael S. Goncalves <br>
	 * Stanford Center for Biomedical Informatics Research (BMIR) <br>
	 * School of Medicine, Stanford University <br>
	 * <p>Axiom diff types</p>
	 */
	public enum AxiomDiffType {
		STRUCTURAL, LOGICAL, CATEGORICAL;
		
		public String toString() {
	        return name().charAt(0) + name().substring(1).toLowerCase();
	    }
	}
	
	
	/**
	 * @author Rafael S. Goncalves <br>
	 * Stanford Center for Biomedical Informatics Research (BMIR) <br>
	 * School of Medicine, Stanford University <br>
	 * <p>Concept diff types</p>
	 */
	public enum ConceptDiffType {
		ATOMIC, SUBCONCEPT, GRAMMAR, CONTENTCVS;
		
		public String toString() {
	        return name().charAt(0) + name().substring(1).toLowerCase();
	    }
	}
	
	
	/**
	 * @author Rafael S. Goncalves <br>
	 * Stanford Center for Biomedical Informatics Research (BMIR) <br>
	 * School of Medicine, Stanford University <br>
	 * <p>XSLT options</p>
	 */
	public enum Transformer {
		AXIOM, CONCEPT, UNITY;
		
		public String toString() {
	        return name().charAt(0) + name().substring(1).toLowerCase();
	    }
	}
}
