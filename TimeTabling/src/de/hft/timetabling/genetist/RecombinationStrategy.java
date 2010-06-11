package de.hft.timetabling.genetist;

import de.hft.timetabling.common.ISolution;

/**
 * Abstract base class that any recombination strategy that is written for our
 * project has to extend from. Recombination is the process of creating a new
 * solution from a number of given parent solutions.
 * 
 * @author Alexander Weickmann
 */
abstract class RecombinationStrategy {

	/**
	 * Recombines the given solutions in order create a new solution. Returns
	 * the newly created solution.
	 * 
	 * @param solution1
	 *            The first of the two parent solutions to recombine.
	 * @param solution2
	 *            The second of the two parent solutions to recombine.
	 */
	abstract ISolution recombine(ISolution solution1, ISolution solution2);

}
