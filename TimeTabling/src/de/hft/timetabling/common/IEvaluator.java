package de.hft.timetabling.common;

/**
 * This class contains method that other parts will call for the evaluation
 * part.
 * 
 * @author Steffen
 * 
 */
public interface IEvaluator {

	/**
	 * Method that need to hand in solution for evaluation. This class will be
	 * implemented in the class "EvaluatorInput".
	 * 
	 * @param solution
	 */
	void handInSolution(ISolution solution);
}
