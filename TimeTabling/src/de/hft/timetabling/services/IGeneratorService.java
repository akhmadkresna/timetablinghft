package de.hft.timetabling.services;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.generator.NoFeasibleSolutionFoundException;

public interface IGeneratorService {

	/**
	 * Tries to generate one feasible solution for the given problem instance.
	 */
	ICourse[][] generateFeasibleSolution(IProblemInstance problemInstance)
			throws NoFeasibleSolutionFoundException;

	/**
	 * Fills up empty slots in the solution table.
	 */
	void fillSolutionTable(IProblemInstance problemInstance);

}
