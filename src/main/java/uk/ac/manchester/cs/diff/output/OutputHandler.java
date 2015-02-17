package uk.ac.manchester.cs.diff.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;

import uk.ac.manchester.cs.diff.EccoSettings;
import uk.ac.manchester.cs.diff.exception.NotImplementedException;
import uk.ac.manchester.cs.diff.output.xml.XMLDiffReport;

/**
 * @author Rafael S. Goncalves <br>
 * Stanford Center for Biomedical Informatics Research (BMIR) <br>
 * School of Medicine, Stanford University <br>
 */
public class OutputHandler {
	private EccoSettings settings;  
	private String outputDir, jarName = "ecco.jar!", classes = "classes", xsltPath;

	
	/**
	 * Constructor
	 * @param settings	ecco settings
	 */
	public OutputHandler(EccoSettings settings) {
		this.settings = settings;
		outputDir = settings.getOutputDirectory();
		xsltPath = setXSLTFilePath();
	}
	

	/**
	 * Set the path to the XSLT file 
	 */
	private String setXSLTFilePath() {
		if(settings.getXSLTFilePath() != null) return settings.getXSLTFilePath();
		
		String xsltFileName = "";
		switch(settings.getTransformType()) {
		case AXIOM:
			xsltFileName = "xslt_client.xsl"; break;
		case CONCEPT:
			// TODO: not implemented
			throw new NotImplementedException("not implemented".toUpperCase());
		case UNITY: 
			xsltFileName = "xslt_full_client.xsl"; break;
		default: break;
		}
		getClass().getClassLoader();
		String path = ClassLoader.getSystemResource("xslt" + File.separator + xsltFileName).getPath();
		if(path.contains(jarName))
			path = path.replace(jarName, "classes");
		settings.setXSLTFilePath(path);
		return path;
	}
	
	
	/**
	 * Get the XSLT file path defined here
	 * @return XSLT file path as a string
	 */
	public String getXSLTFilePath() {
		return xsltPath;
	}
	
	
	/**
	 * Process local output by saving XML change sets, CSV log, and, if applicable, the HTML transformation 
	 * @param report	XML report instance
	 * @param includeTimestamp	true if timestamp should be included in file name(s), false otherwise
	 */
	public void saveXMLDocuments(XMLDiffReport report, boolean includeTimestamp) {
		String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(Calendar.getInstance().getTime());
		saveDocumentToFile(report, report.getXMLDocumentUsingTermNames(), outputDir, 
				(includeTimestamp ? "_names_" + timeStamp : "_names"));	// term name based document
		saveDocumentToFile(report, report.getXMLDocumentUsingLabels(), outputDir, 
				(includeTimestamp ? "_labels_" + timeStamp : "_labels"));	// label based document
		saveDocumentToFile(report, report.getXMLDocumentUsingGenSyms(), outputDir, 
				(includeTimestamp ? "_gensyms_" + timeStamp : "_gensyms"));	// gensym based document
	}
	
	
	/**
	 * Serialize the given XML file and, if applicable, transform it to HTML and serialize the file
	 * @param report	XMLReport instance
	 * @param doc	XML document
	 * @param outputDir	Output directory
	 * @param suffix	Suffix for the different variants of the XML report
	 * @throws TransformerException	Transformer exception
	 */
	private void saveDocumentToFile(XMLDiffReport report, Document doc, String outputDir, String suffix) {
		String docString = report.getReportAsString(doc);
		saveStringToFile(outputDir, "eccoChangeSet" + suffix + ".xml", docString);
		if(settings.isTransformingToHTML()) {
			String html = report.getReportAsHTML(doc, settings.getXSLTFilePath());
			saveStringToFile(outputDir, "index" + suffix + ".html", html);
		}
	}
	
	
	/**
	 * Serialize a given string to the specified path
	 * @param outputDir	Output directory
	 * @param filename	File name
	 * @param content	String to output
	 */
	public void saveStringToFile(String outputDir, String filename, String content) {
        try {
            new File(outputDir).mkdirs();
            FileWriter fw = new FileWriter(outputDir + File.separator + filename, false);
            fw.write(content);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	
	/**
	 * Copy supporting resources: CSS, JavaScript, and image files
	 */
	public void copySupportingDocuments() {
		try {
			copySupportingDocuments("css");
			copySupportingDocuments("js");
			copySupportingDocuments("images");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Copy supporting documents in the specified folder that are necessary to browse the output
	 * @param folderName	Type of file
	 * @throws IOException	IO exception
	 */
	private void copySupportingDocuments(String folderName) throws IOException {
		Enumeration<URL> e = ClassLoader.getSystemResources(folderName);
		while (e.hasMoreElements()) {
			String path = e.nextElement().getPath();
			if(path.contains(jarName)) {
				path = path.replace(jarName, classes);
				if(path.startsWith("file:"))
					path = path.replace("file:", "");
			}
			File f = new File(path);
			FileUtils.copyDirectory(f, new File(outputDir + f.getName()));
		}
	}	
}