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

import java.util.Set;

/**
 * @author Rafael S. Goncalves <br>
 * Information Management Group (IMG) <br>
 * School of Computer Science <br>
 * University of Manchester <br>
 */
public class ProgressMonitor {
	private Integer total;
	private Double done;
	
	/**
	 * Constructor
	 * @param set	Set of objects
	 */
	public ProgressMonitor(Set<?> set) {
		total = set.size();
	}
	
	
	/**
	 * Increment the number of processed objects by 1
	 * @return Percentage of objects processed
	 */
	public Integer incrementProgress() {
		if(done == null) 
			done = 1.0;
		else 
			done++;
		return getPercentDone();
	}
	
	
	/**
	 * Increment the number of processed objects by the specified amount
	 * @param increment	Amount to increment
	 * @return Percentage of objects processed
	 */
	public Integer incrementProgress(int increment) {
		if(done == null) 
			done = increment + 0.0;
		else 
			done += increment;
		return getPercentDone();
	}
	
	
	/**
	 * Get the percentage of objects processed
	 * @return Percentage of objects processed
	 */
	public Integer getPercentDone() {
		return (int) ((done/total)*100.0);
	}
}
