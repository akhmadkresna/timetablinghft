package de.hft.timetabling.services;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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

	private SolutionVote bestPenaltySolution;

	private SolutionVote bestFairnessSolution;

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
		solutionTable.put(solutionNumber, new SolutionVote(solution));
	}

	@Override
	public void addPenaltyToSolution(ISolution solution, int penaltyPoints) {
		SolutionVote solutionVote = getSolutionVoteForSolution(solution);
		int penaltySum = solutionVote.getPenaltySum();
		if (penaltySum == -1) {
			penaltySum++;
		}
		penaltySum += penaltyPoints;
		solutionVote.setPenaltySum(penaltySum);
		// commenting to move to different function
		// why update each time and not compare to current best solution?
		// adding of penalty is a summation meaning the bestSolution cannot be
		// obtained from here
		// if (bestSolution == null) {
		// bestSolution = new SolutionVote(solution, penaltySum);
		// } else {
		// updateBestSolution();
		// }
	}

	/**
	 * @author Roy
	 * 
	 *         Changed to compare the new solution with BestSolution
	 * 
	 * @param currentSolution
	 *            Passing current solution to which fairness was added.
	 */
	private void updateBestSolution(SolutionVote currentSolution) {
		SolutionVote bestSoFar = bestPenaltySolution;
		int bestFairness = bestSoFar.getFairness();
		int bestPenalty = bestSoFar.getPenaltySum();
		int currentFairness = currentSolution.getFairness();
		int currentPenaltySum = currentSolution.getPenaltySum();

		// Check the new solution Penalty with best one
		if (currentPenaltySum < bestPenalty) {
			bestSoFar = currentSolution;
		} else if (currentPenaltySum == bestPenalty) {
			// Check Fairness of the solutions of penalty points are same
			if (currentFairness < bestFairness) {
				bestSoFar = currentSolution;
			}
		}

		/*
		 * Needs to be copied instead of only passing the reference because the
		 * solution is still in the table and might undergo changes.
		 */
		bestPenaltySolution = new SolutionVote(bestSoFar.getSolution().clone(),
				bestSoFar.getPenaltySum(), bestSoFar.getFairness());
	}

	@Override
	public void addPenaltyToSolution(int solutionNumber, int penaltyPoints) {
		checkSolutionNumber(solutionNumber);
		addPenaltyToSolution(getSolution(solutionNumber), penaltyPoints);
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

	@Override
	public int getFairnessForSolution(int solutionNumber) {
		return getFairnessForSolution(getSolution(solutionNumber));
	}

	@Override
	public int getFairnessForSolution(ISolution solution) {
		return getSolutionVoteForSolution(solution).getFairness();
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
		return (bestPenaltySolution == null) ? null : bestPenaltySolution
				.getSolution();
	}

	@Override
	public int getBestSolutionPenaltySum() {
		if (bestPenaltySolution == null) {
			throw new RuntimeException("No best solution available yet.");
		}
		return bestPenaltySolution.getPenaltySum();
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
		solutionTable.put(worstSolutionNumber, new SolutionVote(newSolution,
				-1, -1));
	}

	/**
	 * Add the fairness to the solution
	 * 
	 * @author Roy
	 */
	@Override
	public void addFairnessToSolution(ISolution solution, int fairness) {
		SolutionVote solutionVote = getSolutionVoteForSolution(solution);
		int iFairness = solutionVote.getFairness();

		if (iFairness == -1) {
			iFairness++;
		}
		iFairness = fairness;
		solutionVote.setFairness(iFairness);

		// Call the method to update the best and fairest solutions
		callMethodToFindBestandFairestSolutions(solution);

	}

	/**
	 * Call the updateBestSolution and the updateFairestSolution methods
	 * 
	 * @param solution
	 */
	private void callMethodToFindBestandFairestSolutions(ISolution solution) {
		SolutionVote solutionVote = getSolutionVoteForSolution(solution);
		int iFairness = solutionVote.getFairness();
		int iPenalty = solutionVote.getPenaltySum();
		// set the current solution was the fairest if null
		if (bestFairnessSolution == null) {
			bestFairnessSolution = new SolutionVote(solution, iPenalty,
					iFairness);
		} else {
			updateFairestSolution(solutionVote);
		}
		// set the current solution as best if null
		if (bestPenaltySolution == null) {
			bestPenaltySolution = new SolutionVote(solution, iPenalty,
					iFairness);
		} else {
			updateBestSolution(solutionVote);
		}
	}

	private void updateFairestSolution(SolutionVote currentSolution) {
		SolutionVote fairestSoFar = bestFairnessSolution;
		int bestFairness = fairestSoFar.getFairness();
		int currentFairness = currentSolution.getFairness();

		// Check the new solution fairness with best one
		if (currentFairness < bestFairness) {
			fairestSoFar = currentSolution;
		}

		/*
		 * Needs to be copied instead of only passing the reference because the
		 * solution is still in the table and might undergo changes.
		 */
		bestFairnessSolution = new SolutionVote(fairestSoFar.getSolution()
				.clone(), fairestSoFar.getPenaltySum(), fairestSoFar
				.getFairness());

	}

	/**
	 * Add the fairness to the solution using solution number
	 * 
	 * @author Roy
	 */
	@Override
	public void addFairnessToSolution(int solutionNumber, int fairness) {
		checkSolutionNumber(solutionNumber);
		addFairnessToSolution(getSolution(solutionNumber), fairness);
	}

	@Override
	public ISolution getFairestSolution() {
		return (bestFairnessSolution == null) ? null : bestFairnessSolution
				.getSolution();
	}

	@Override
	public int getFairestSolutionPenalty() {
		if (bestFairnessSolution == null) {
			throw new RuntimeException("No best solution available yet.");
		}
		return bestFairnessSolution.getPenaltySum();
	}

	@Override
	public int getFairestSolutionFairness() {
		if (bestFairnessSolution == null) {
			throw new RuntimeException("No best solution available yet.");
		}
		return bestFairnessSolution.getFairness();
	}

	@Override
	public int getBestSolutionFairness() {
		if (bestPenaltySolution == null) {
			throw new RuntimeException("No best solution available yet.");
		}
		return bestPenaltySolution.getFairness();
	}

	@Override
	public void removeSolution(ISolution solution) {
		for (Integer solutionNumber : solutionTable.keySet()) {
			SolutionVote solutionVote = solutionTable.get(solutionNumber);
			if (solutionVote.getSolution().equals(solution)) {
				solutionTable.remove(solutionNumber);
				break;
			}
		}
	}

	@Override
	// TODO AW: Not yet finished
	public List<ISolution> getSolutionsOrderedByPenalty() {
		LinkedList<ISolution> orderedList = new LinkedList<ISolution>();
		for (Integer solutionNumber : solutionTable.keySet()) {
			ISolution solution = solutionTable.get(solutionNumber)
					.getSolution();
			if (orderedList.isEmpty()) {
				orderedList.add(solution);
				continue;
			}
			int penaltySum = getPenaltySumForSolution(solutionNumber);
			int penaltySumFirst = getPenaltySumForSolution(orderedList
					.getFirst());
			int penaltySumLast = getPenaltySumForSolution(orderedList.getLast());
			if (penaltySum < penaltySumFirst) {
				orderedList.addFirst(solution);
			} else if (penaltySum > penaltySumLast) {
				orderedList.addLast(solution);
			}
		}
		return orderedList;
	}

	/**
	 * Used to associate a given solution with a penalty sum.
	 */
	private static class SolutionVote {

		private final ISolution solution;

		private int penaltySum;

		private int fairness;

		public SolutionVote(ISolution solution) {
			this(solution, -1, -1);
		}

		public SolutionVote(ISolution solution, int penaltySum, int fairness) {
			this.solution = solution;
			this.penaltySum = penaltySum;
			this.fairness = fairness;
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

		public int getFairness() {
			return fairness;
		}

		public void setFairness(int fairness) {
			this.fairness = fairness;
		}

	}

}
