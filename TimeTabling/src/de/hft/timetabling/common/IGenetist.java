package de.hft.timetabling.common;

import de.hft.timetabling.services.ISolutionTableService;

/**
 * Interface of the genetist.
 * 
 * @author SteffenKremer
 * 
 */
public interface IGenetist {

	/**
	 * Method to start one iteration of recombination.
	 * 
	 * @author SteffenKremer
	 * 
	 * @param solution
	 *            SolutionTable to recombine
	 */
	ISolution startRecombination(ISolutionTableService solution);

}
