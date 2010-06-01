package de.hft.timetabling.services;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.generator.NoFeasibleSolutionFoundException;

public interface IGeneratorService {
	public ICourse[][] generateFeasibleSolution(IProblemInstance inst)
			throws NoFeasibleSolutionFoundException;
}
