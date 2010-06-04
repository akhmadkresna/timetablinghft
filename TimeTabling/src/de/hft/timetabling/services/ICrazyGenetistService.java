package de.hft.timetabling.services;

import de.hft.timetabling.common.ISolution;

/**
 * Interface of the genetist.
 * 
 * @author SteffenKremer
 * 
 */
public interface ICrazyGenetistService {

	/**
	 * Method to start one iteration of recombination.
	 * 
	 * @author SteffenKremer
	 * 
	 */
	ISolution recombineAndMutate();

}
