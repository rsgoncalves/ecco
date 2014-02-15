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
package uk.ac.manchester.cs.diff.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Rafael S. Goncalves <br/>
 * Information Management Group (IMG) <br/>
 * School of Computer Science <br/>
 * University of Manchester <br/>
 */
public class XMLTransformer {
	private String xsltPath;
	private DocumentBuilder builder;
	
	/**
	 * Constructor
	 * @param xsltPath	XSLT file path
	 * @param builder	Document builder
	 */
	public XMLTransformer(String xsltPath, DocumentBuilder builder) {
		this.xsltPath = xsltPath;
		this.builder = builder;
	}
	
	
	/**
	 * Process given XML file
	 * @param f	XML file
	 * @param directory	Directory where XML resides
	 * @throws TransformerException
	 */
	public void processFile(File f, File directory) throws TransformerException {
		System.out.println("  Processing file: " + f.getName());
		Document doc = null;
		try {
			doc = builder.parse(f);
		} catch(SAXException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		if(doc != null) {
			String name = f.getName();
			String suffix = name.substring(name.lastIndexOf("_"), name.lastIndexOf("."));
			String html = getReportAsHTML(doc);
			saveStringToFile(directory.getAbsolutePath(), "index"+suffix+".html", html);
		}
	}
	
	
	/**
	 * Get XML document transformed into HTML as a string
	 * @param doc	XML document
	 * @return String containing the HTML transformation
	 * @throws TransformerException 
	 */
	public String getReportAsHTML(Document doc) throws TransformerException {
		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer(new javax.xml.transform.stream.StreamSource(xsltPath));
		trans.setOutputProperty(OutputKeys.INDENT, "yes");

		// Create string from XML tree
		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		DOMSource source = new DOMSource(doc);

		trans.transform(source, result);
		return sw.toString();
	}
	
	
	/**
	 * Serialize a given string to the specified path
	 * @param outputDir	Output directory
	 * @param filename	File name
	 * @param content	String to output
	 */
	private void saveStringToFile(String outputDir, String filename, String content) {
        try {
            new File(outputDir).mkdirs();
            FileWriter fw = new FileWriter(outputDir + File.separator + filename);
            fw.write(content);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	
	/**
	 * @param args
	 * @throws ParserConfigurationException 
	 * @throws TransformerException 
	 */
	public static void main(String[] args) throws ParserConfigurationException, TransformerException {
		String xsltPath = args[0];
		String rootExampleFolder = args[1];
		
		DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = fac.newDocumentBuilder();
		XMLTransformer trans = new XMLTransformer(xsltPath, builder);
		
		File rootFolder = new File(rootExampleFolder);
		System.out.println("Checking XML files under: " + rootFolder.getName());
		File[] dirs = rootFolder.listFiles();
		for(File dir : dirs) {
			if(dir.isDirectory()) {
				System.out.println(" Checking directory: " + dir.getName());
				for(File f : dir.listFiles()) {
					if(f.getName().endsWith(".xml"))
						trans.processFile(f, dir);
				}
			}
			else if(dir.getName().endsWith(".xml"))
				trans.processFile(dir, rootFolder);
		}
	}
}
