package uk.ac.manchester.cs.diff.output.csv;

import uk.ac.manchester.cs.diff.axiom.changeset.AxiomChangeSet;
import uk.ac.manchester.cs.diff.concept.changeset.ConceptChangeSet;
import uk.ac.manchester.cs.diff.exception.NotImplementedException;
import uk.ac.manchester.cs.diff.output.xml.XMLAxiomDiffReport;
import uk.ac.manchester.cs.diff.output.xml.XMLConceptDiffReport;
import uk.ac.manchester.cs.diff.output.xml.XMLDiffReport;
import uk.ac.manchester.cs.diff.output.xml.XMLUnifiedDiffReport;
import uk.ac.manchester.cs.diff.unity.changeset.ChangeSet;

/**
 * @author Rafael S. Goncalves <br>
 * Stanford Center for Biomedical Informatics Research (BMIR) <br>
 * School of Medicine, Stanford University <br>
 */
public class CSVDiffReport {
	private XMLDiffReport report;
	
	
	public CSVDiffReport(XMLDiffReport report) {
		this.report = report;
	}
	
	
	public String getCSV() {
		ChangeSet changeSet = report.getChangeSet();
		if(report instanceof XMLAxiomDiffReport)
			return new CSVAxiomDiffReport().getReport((AxiomChangeSet)changeSet);
		else if(report instanceof XMLConceptDiffReport)
			return new CSVConceptDiffReport().getReport((ConceptChangeSet)changeSet);
		else if(report instanceof XMLUnifiedDiffReport)
			// TODO: not implemented
			throw new NotImplementedException("not implemented".toUpperCase());
		else
			return null;
	}
}
