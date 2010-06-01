package de.hft.timetabling.common;

import de.hft.timetabling.generator.NoFeasibleSolutionFoundException;

public interface IGenerator {
	public ICourse[][] generateFeasibleSolution(IProblemInstance inst)
			throws NoFeasibleSolutionFoundException;
}
