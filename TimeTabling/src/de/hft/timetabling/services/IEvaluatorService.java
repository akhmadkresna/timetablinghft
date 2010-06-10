package de.hft.timetabling.services;

import de.hft.timetabling.common.ISolution;

public interface IEvaluatorService {

	/**
	 * Evaluates penalty and fairness of the solutions from solutionTable and
	 * adds them to the table
	 * 
	 */
	void evaluateSolutions();

	/**
	 * Called my Genetist to get the penalty of new solution.
	 * 
	 * @param newSolution
	 * @return Returns the penalty of given solution
	 * 
	 */
	int evaluateSolution(ISolution newSolution);

}
