package de.hft.timetabling.services;

import de.hft.timetabling.genetist.CourseExchangeRecombinationStrategy;
import de.hft.timetabling.genetist.RecombinationStrategy;

/**
 * Interface of the genetist.
 * 
 * @author SteffenKremer
 * 
 */
public interface ICrazyGenetistService {

	/**
	 * Iterations to chose one of the recombination algorithms. This number
	 * means the percentage of the maximum table size.
	 */
	int getRecombinationPercentage();

	/** The strategy to use for recombination. */
	RecombinationStrategy RECOMBINATION_STRATEGY = new CourseExchangeRecombinationStrategy();

	/**
	 * Method to start one iteration of recombination.
	 * 
	 * @author SteffenKremer
	 */
	void recombineAndMutate(int iteration, int totalIterations);

}
