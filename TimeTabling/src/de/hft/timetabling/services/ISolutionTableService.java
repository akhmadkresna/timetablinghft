package de.hft.timetabling.services;

import de.hft.timetabling.common.ISolution;

public interface ISolutionTableService {

	ISolution createNewSolution(String[][] coding);

	void setSolution(int solutionNumber, ISolution solution);

	ISolution getSolution(int solutionNumber);

	void voteForSolution(ISolution solution, int vote);

	int getVoteSumForSolution(ISolution solution);

}
