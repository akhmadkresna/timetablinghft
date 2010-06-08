package de.hft.timetabling.services;

import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.generator.NoFeasibleSolutionFoundException;

public interface IGeneratorService {

	/**
	 * Fills up empty slots in the solution table.
	 */
	void fillSolutionTable(IProblemInstance problemInstance)
			throws NoFeasibleSolutionFoundException;

}
