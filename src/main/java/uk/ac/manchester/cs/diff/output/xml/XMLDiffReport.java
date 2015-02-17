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
package uk.ac.manchester.cs.diff.output.xml;

import org.w3c.dom.Document;

import uk.ac.manchester.cs.diff.unity.changeset.ChangeSet;

/**
 * @author Rafael S. Goncalves <br>
 * Information Management Group (IMG) <br>
 * School of Computer Science <br>
 * University of Manchester <br>
 */
public interface XMLDiffReport {

	/**
	 * Get XML document using term names
	 * @return Term name based XML document
	 */
	public Document getXMLDocumentUsingTermNames();
	
	
	/**
	 * Get XML document using term labels
	 * @return Label based XML document
	 */
	public Document getXMLDocumentUsingLabels();
	
	
	/**
	 * Get XML document using automatically generated symbols
	 * @return Gensym based XML document
	 */
	public Document getXMLDocumentUsingGenSyms();
	
	
	/**
	 * Get a specified XML document as a string
	 * @param doc	XML document
	 * @return String containing the given XML document
	 */
	public String getReportAsString(Document doc);
	
	
	/**
	 * Get a transformation of the given document, according to the specified XSLT file, into HTML
	 * @param doc	XML document
	 * @param xsltPath	File path to XSLT file
	 * @return String containing the HTML code resulting from the transformation
	 */
	public String getReportAsHTML(Document doc, String xsltPath);

	
	/**
	 * Get the change set associated with this report
	 * @return Change set
	 */
	public ChangeSet getChangeSet();
	
}
