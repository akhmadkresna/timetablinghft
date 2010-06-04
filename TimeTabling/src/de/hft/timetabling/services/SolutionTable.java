package de.hft.timetabling.services;

import java.util.HashMap;
import java.util.Map;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.ISolution;

/**
 * Implementation of the solution table service.
 * 
 * @author Alexander Weickmann
 * 
 * @see ISolutionTableService
 */
public final class SolutionTable implements ISolutionTableService {

	/**
	 * The solution table is implemented as a map which assigns a unique number
	 * to each solution.
	 */
	private final Map<Integer, SolutionVote> solutionTable;

	private SolutionVote bestSolution;

	public SolutionTable() {
		solutionTable = new HashMap<Integer, SolutionVote>();
	}

	@Override
	public ISolution createNewSolution(ICourse[][] coding,
			IProblemInstance problemInstance) {

		int numberOfPeriods = problemInstance.getNumberOfDays()
				* problemInstance.getPeriodsPerDay();
		if (coding.length != numberOfPeriods) {
			throw new IllegalArgumentException(
					"Incomplete coding: period-dimension (x) not matching the number of periods of the problem instance.");
		}
		for (int period = 0; period < numberOfPeriods; period++) {
			ICourse[] coursesPerPeriod = coding[period];
			if (coursesPerPeriod.length != problemInstance.getNumberOfRooms()) {
				throw new IllegalArgumentException(
						"Incomplete coding: room-dimension (y) not matching the number of rooms of the problem instance in period "
								+ period + ".");
			}
		}
		return new SolutionImpl(coding, problemInstance);
	}

	@Override
	public ISolution getSolution(int solutionNumber) {
		checkSolutionNumber(solutionNumber);
		SolutionVote solutionVote = solutionTable.get(solutionNumber);
		return (solutionVote == null) ? null : solutionVote.getSolution();
	}

	@Override
	public void putSolution(int solutionNumber, ISolution solution) {
		checkSolutionNumber(solutionNumber);
		solutionTable.put(solutionNumber, new SolutionVote(solution, -1));
	}

	@Override
	public void voteForSolution(ISolution solution, int penaltyPoints) {
		SolutionVote solutionVote = getSolutionVoteForSolution(solution);
		int penaltySum = solutionVote.getPenaltySum();
		if (penaltySum == -1) {
			penaltySum++;
		}
		penaltySum += penaltyPoints;
		solutionVote.setPenaltySum(penaltySum);

		if (bestSolution == null) {
			bestSolution = new SolutionVote(solution, penaltySum);
		} else {
			updateBestSolution();
		}
	}

	private void updateBestSolution() {
		SolutionVote bestSoFar = null;
		for (Integer solutionNumber : solutionTable.keySet()) {
			SolutionVote currentSolutionVote = solutionTable
					.get(solutionNumber);
			int currentPenaltySum = currentSolutionVote.getPenaltySum();
			if (currentPenaltySum == -1) {
				continue;
			}
			if (bestSoFar == null) {
				bestSoFar = currentSolutionVote;
				continue;
			}
			if (currentPenaltySum < bestSoFar.getPenaltySum()) {
				bestSoFar = currentSolutionVote;
			}
		}
		bestSolution = bestSoFar;
	}

	@Override
	public void voteForSolution(int solutionNumber, int penaltyPoints) {
		checkSolutionNumber(solutionNumber);
		voteForSolution(getSolution(solutionNumber), penaltyPoints);
	}

	@Override
	public int getPenaltySumForSolution(int solutionNumber) {
		checkSolutionNumber(solutionNumber);
		return getPenaltySumForSolution(getSolution(solutionNumber));
	}

	@Override
	public int getPenaltySumForSolution(ISolution solution) {
		return getSolutionVoteForSolution(solution).getPenaltySum();
	}

	/**
	 * Returns the internal data structure used to associate solutions with
	 * votes for the given solution instance.
	 */
	private SolutionVote getSolutionVoteForSolution(ISolution solution) {
		for (Integer solutionNumber : solutionTable.keySet()) {
			SolutionVote solutionVote = solutionTable.get(solutionNumber);
			if (solutionVote.getSolution().equals(solution)) {
				return solutionVote;
			}
		}
		throw new RuntimeException("Solution not found in solution table.");
	}

	/**
	 * Assures that the given solution number is within the valid range.
	 */
	private void checkSolutionNumber(int solutionNumber) {
		if ((solutionNumber < 0) || (solutionNumber >= TABLE_SIZE)) {
			throw new IndexOutOfBoundsException(
					"Solution table numbers only range from 0 to "
							+ (TABLE_SIZE - 1) + ".");
		}
	}

	@Override
	public ISolution getBestSolution() {
		return (bestSolution == null) ? null : bestSolution.getSolution();
	}

	@Override
	public int getBestSolutionPenaltySum() {
		if (bestSolution == null) {
			throw new RuntimeException("No best solution available yet.");
		}
		return bestSolution.getPenaltySum();
	}

	@Override
	public String toString() {
		return "Solution Table";
	}

	@Override
	public int getActualSolutionTableCount() {
		return solutionTable.size();
	}

	@Override
	public void replaceWorstSolution(ISolution newSolution) {
		Integer worstSolutionNumber = -1;
		SolutionVote worstSolutionVote = null;

		for (Integer solutionNumber : solutionTable.keySet()) {
			SolutionVote solutionVote = solutionTable.get(solutionNumber);
			if ((worstSolutionNumber == -1) || (worstSolutionVote == null)) {
				worstSolutionNumber = solutionNumber;
				worstSolutionVote = solutionVote;
				continue;
			}
			if (solutionVote.getPenaltySum() > worstSolutionVote
					.getPenaltySum()) {
				worstSolutionVote = solutionVote;
				worstSolutionNumber = solutionNumber;
			}
		}

		if (worstSolutionNumber == -1) {
			worstSolutionNumber = 0;
		}
		solutionTable.put(worstSolutionNumber,
				new SolutionVote(newSolution, -1));
	}

	/**
	 * Used to associate a given solution with a penalty sum.
	 */
	private static class SolutionVote {

		private final ISolution solution;

		private int penaltySum;

		public SolutionVote(ISolution solution, int penaltySum) {
			this.solution = solution;
			this.penaltySum = penaltySum;
		}

		public ISolution getSolution() {
			return solution;
		}

		public int getPenaltySum() {
			return penaltySum;
		}

		public void setPenaltySum(int penaltySum) {
			this.penaltySum = penaltySum;
		}

	}

}
