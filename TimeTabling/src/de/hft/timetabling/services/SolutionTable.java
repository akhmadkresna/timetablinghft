package de.hft.timetabling.services;

import java.util.HashMap;
import java.util.Map;

import de.hft.timetabling.common.ISolution;

public final class SolutionTable implements ISolutionTableService {

	private final Map<Integer, SolutionVote> solutionTable;

	private SolutionVote bestSolution;

	public SolutionTable() {
		solutionTable = new HashMap<Integer, SolutionVote>();
	}

	@Override
	public ISolution createNewSolution(String[][] coding) {
		return new SolutionImpl(coding);
	}

	@Override
	public ISolution getSolution(int solutionNumber) {
		checkSolutionNumber(solutionNumber);
		return solutionTable.get(solutionNumber).getSolution();
	}

	@Override
	public void setSolution(int solutionNumber, ISolution solution) {
		checkSolutionNumber(solutionNumber);
		solutionTable.put(solutionNumber, new SolutionVote(solution, 0));
	}

	@Override
	public void voteForSolution(ISolution solution, int vote) {
		SolutionVote solutionVote = getSolutionVoteForSolution(solution);
		if (solutionVote.getVoteSum() > 0) {
			throw new RuntimeException("This solution was already voted.");
		}
		solutionVote.setVoteSum(vote);
		if ((bestSolution == null) || (bestSolution.getVoteSum() < vote)) {
			bestSolution = new SolutionVote(solution, vote);
		}
	}

	@Override
	public int getVoteSumForSolution(ISolution solution) {
		return getSolutionVoteForSolution(solution).getVoteSum();
	}

	private SolutionVote getSolutionVoteForSolution(ISolution solution) {
		for (Integer solutionNumber : solutionTable.keySet()) {
			SolutionVote solutionVote = solutionTable.get(solutionNumber);
			if (solutionVote.getSolution().equals(solution)) {
				return solutionVote;
			}
		}
		throw new RuntimeException("Solution not found in solution table.");
	}

	private void checkSolutionNumber(int solutionNumber) {
		if ((solutionNumber < 0) || (solutionNumber > 9)) {
			throw new IndexOutOfBoundsException(
					"Solution table numbers only range from 0 to 9.");
		}
	}

	private static class SolutionVote {

		private final ISolution solution;

		private int voteSum;

		public SolutionVote(ISolution solution, int voteSum) {
			this.solution = solution;
			this.voteSum = voteSum;
		}

		public ISolution getSolution() {
			return solution;
		}

		public int getVoteSum() {
			return voteSum;
		}

		public void setVoteSum(int voteSum) {
			this.voteSum = voteSum;
		}

	}

}
