package de.hft.timetabling.services;

import de.hft.timetabling.common.IProblemInstance;

public interface IGeneratorService {

	/**
	 * Fills up empty slots in the solution table.
	 */
	void fillSolutionTable(IProblemInstance problemInstance);

}
