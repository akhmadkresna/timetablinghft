package de.hft.timetabling.solutiontable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.main.Main;
import de.hft.timetabling.services.ISolutionTableService;

/**
 * Implementation of the solution table service.
 * 
 * @author Alexander Weickmann
 * 
 * @see ISolutionTableService
 */
public final class SolutionTable implements ISolutionTableService {

	private final TreeSet<WeightedSolution> solutionTable;

	private final List<ISolution> notVotedTable;

	private WeightedSolution bestPenaltySolution;

	private WeightedSolution bestFairnessSolution;

	private WeightedSolution worstPenaltySolution;

	private WeightedSolution worstFairnessSolution;

	private int currentNotVotedCount;

	private int maximumSize;

	/**
	 * When voting, a solution moves from the not voted list to the real
	 * solution table. To assure that further indexes of calls to
	 * voteForSolution(int, int, int) are correct, it must be kept track how
	 * often voting has occurred in this iteration.
	 */
	private int voteIndexModification;

	public SolutionTable() {
		maximumSize = 50;
		solutionTable = new TreeSet<WeightedSolution>();
		notVotedTable = new ArrayList<ISolution>(maximumSize);
	}

	@Override
	public int getMaximumSize() {
		return maximumSize;
	}

	@Override
	public void setMaximumSize(final int maximumSize) {
		this.maximumSize = maximumSize;
	}

	@Override
	public ISolution createNewSolution(final ICourse[][] coding,
			final IProblemInstance problemInstance) {

		final int numberOfPeriods = problemInstance.getNumberOfDays()
				* problemInstance.getPeriodsPerDay();
		if (coding.length != numberOfPeriods) {
			throw new IllegalArgumentException(
					"Incomplete coding: period-dimension (x) not matching the number of periods of the problem instance.");
		}
		for (int period = 0; period < numberOfPeriods; period++) {
			final ICourse[] coursesPerPeriod = coding[period];
			if (coursesPerPeriod.length != problemInstance.getNumberOfRooms()) {
				throw new IllegalArgumentException(
						"Incomplete coding: room-dimension (y) not matching the number of rooms of the problem instance in period "
								+ period + ".");
			}
		}
		return new SolutionImpl(coding, problemInstance);
	}

	@Override
	public boolean isFull() {
		return getNumberOfEmptySlots() == 0;
	}

	@Override
	public void addSolution(final ISolution solution) {
		if (getSize(true) == maximumSize) {
			throw new RuntimeException(
					"Insertion of solution failed because the solution table is full.");
		}
		notVotedTable.add(solution);
		currentNotVotedCount++;
	}

	@Override
	public ISolution getBestPenaltySolution() {
		return bestPenaltySolution.getSolution();
	}

	@Override
	public ISolution getBestFairnessSolution() {
		return bestFairnessSolution.getSolution();
	}

	@Override
	public ISolution getWorstPenaltySolution() {
		return worstPenaltySolution.getSolution();
	}

	@Override
	public ISolution getWorstFairnessSolution() {
		return worstFairnessSolution.getSolution();
	}

	@Override
	public List<ISolution> getNotVotedSolutions() {
		final List<ISolution> defensiveCopy = new ArrayList<ISolution>(
				notVotedTable.size());
		defensiveCopy.addAll(notVotedTable);
		return defensiveCopy;
	}

	@Override
	public void voteForSolution(int index, final int penalty, final int fairness) {
		index = index - voteIndexModification;
		voteIndexModification++;
		final ISolution solution = notVotedTable.get(index);
		notVotedTable.remove(index);
		((SolutionImpl) solution).setPenalty(penalty);
		((SolutionImpl) solution).setFairness(fairness);
		final boolean added = solutionTable.add(new WeightedSolution(solution,
				penalty, fairness));
		if (added) {
			Main.solutionTableInsertionSuccess++;
		} else {
			Main.solutionTableInsertionFailure++;
		}
		currentNotVotedCount--;
	}

	@Override
	public int getSize(final boolean includeNotVotedSolutions) {
		if (includeNotVotedSolutions) {
			return solutionTable.size() + currentNotVotedCount;
		}
		return solutionTable.size();
	}

	@Override
	public ISolution removeWorstSolution(final int minAge) {
		final Set<WeightedSolution> removed = new TreeSet<WeightedSolution>();
		ISolution removedSolution = null;
		boolean removalOk = false;
		while (!(removalOk) && (solutionTable.size() > 0)) {
			final WeightedSolution weightedSolution = solutionTable.pollLast();
			if (weightedSolution.getSolution().getAge() >= minAge) {
				removalOk = true;
				removedSolution = weightedSolution.getSolution();
				break;
			}
			removed.add(weightedSolution);
		}

		// Re-insert all solutions that have been removed by mistake.
		for (final WeightedSolution weightedSolution : removed) {
			solutionTable.add(weightedSolution);
		}

		if (removedSolution == null) {
			removedSolution = solutionTable.pollLast().getSolution();
		}

		return removedSolution;
	}

	@Override
	public boolean remove(final ISolution solution) {
		for (final WeightedSolution weightedSolution : solutionTable) {
			if (weightedSolution.getSolution().equals(solution)) {
				return solutionTable.remove(weightedSolution);
			}
		}
		return false;
	}

	@Override
	public ISolution getSolutionMostOftenRecombined() {
		if (getSize(false) == 0) {
			return null;
		}
		WeightedSolution currentResult = null;
		for (final WeightedSolution weightedSolution : solutionTable) {
			if (currentResult == null) {
				currentResult = weightedSolution;
				continue;
			}
			final ISolution currentSolution = weightedSolution.getSolution();
			if (currentSolution.getRecombinationCount() > currentResult
					.getSolution().getRecombinationCount()) {
				currentResult = weightedSolution;
			}
		}
		return (currentResult == null) ? null : currentResult.getSolution();
	}

	@Override
	public int getNumberOfEmptySlots() {
		// Not voted solutions count not as empty slots.
		return maximumSize - getSize(true);
	}

	@Override
	public String toString() {
		return "Solution Table (" + getSize(true) + " entries)";
	}

	@Override
	public ISolution getSolution(final int index) {
		final WeightedSolution[] array = solutionTable
				.toArray(new WeightedSolution[getSize(false)]);
		return array[index].getSolution();
	}

	@Override
	public void update() {
		voteIndexModification = 0;
		updateBestPenaltySolution();
		updateBestFairnessSolution();
		updateWorstPenaltySolution();
		updateWorstFairnessSolution();
		updateSolutionAges();
	}

	private void updateSolutionAges() {
		for (final WeightedSolution weightedSolution : solutionTable) {
			((SolutionImpl) weightedSolution.getSolution()).increaseAge();
		}
	}

	private void updateBestFairnessSolution() {
		WeightedSolution bestFairnessInTable = null;
		for (final WeightedSolution weightedSolution : solutionTable) {
			if (bestFairnessInTable == null) {
				bestFairnessInTable = weightedSolution;
				continue;
			}
			if (weightedSolution.getFairness() < bestFairnessInTable
					.getFairness()) {
				bestFairnessInTable = weightedSolution;
			}
		}

		if (bestFairnessInTable == null) {
			throw new RuntimeException();
		}

		if (bestFairnessSolution == null) {
			bestFairnessSolution = bestFairnessInTable;
		}
		// case when fairness is same, e.g. fairness = 0
		else if (bestFairnessSolution.getFairness() == bestFairnessInTable
				.getFairness()) {
			if (bestFairnessInTable.getPenalty() < bestFairnessSolution
					.getPenalty()) {
				bestFairnessSolution = bestFairnessInTable;
			}
		} else {
			if (bestFairnessInTable.getFairness() < bestFairnessSolution
					.getFairness()) {
				bestFairnessSolution = bestFairnessInTable;
			}
		}
	}

	private void updateBestPenaltySolution() {
		final WeightedSolution bestPenaltyInTable = solutionTable.first();
		if (bestPenaltySolution == null) {
			bestPenaltySolution = bestPenaltyInTable;
		} else {
			if (bestPenaltyInTable.getPenalty() < bestPenaltySolution
					.getPenalty()) {
				bestPenaltySolution = bestPenaltyInTable;
			}
		}
	}

	private void updateWorstFairnessSolution() {
		WeightedSolution worstFairnessInTable = null;
		for (final WeightedSolution weightedSolution : solutionTable) {
			if (worstFairnessInTable == null) {
				worstFairnessInTable = weightedSolution;
				continue;
			}
			if (weightedSolution.getFairness() > worstFairnessInTable
					.getFairness()) {
				worstFairnessInTable = weightedSolution;
			}
		}

		if (worstFairnessInTable == null) {
			throw new RuntimeException();
		}

		if (worstFairnessSolution == null) {
			worstFairnessSolution = worstFairnessInTable;
		}
		// case when fairness is same, e.g. fairness = 0
		else if (worstFairnessSolution.getFairness() == worstFairnessInTable
				.getFairness()) {
			if (worstFairnessInTable.getPenalty() > worstFairnessSolution
					.getPenalty()) {
				worstFairnessSolution = worstFairnessInTable;
			}
		} else {
			if (worstFairnessInTable.getFairness() > worstFairnessSolution
					.getFairness()) {
				worstFairnessSolution = worstFairnessInTable;
			}
		}
	}

	private void updateWorstPenaltySolution() {
		final WeightedSolution worstPenaltyInTable = solutionTable.last();
		if (worstPenaltySolution == null) {
			worstPenaltySolution = worstPenaltyInTable;
		} else {
			if (worstPenaltyInTable.getPenalty() > worstPenaltySolution
					.getPenalty()) {
				worstPenaltySolution = worstPenaltyInTable;
			}
		}
	}

	/**
	 * @author Roy
	 */
	@Override
	public boolean compareWithWorstSolution(final int iPenalty) {
		final int worstSolutionPenalty = solutionTable.last().getPenalty();
		if (iPenalty < worstSolutionPenalty) {
			return true;
		}

		return false;
	}

	@Override
	public void clear() {
		solutionTable.clear();
		notVotedTable.clear();
		bestPenaltySolution = null;
		bestFairnessSolution = null;
		worstPenaltySolution = null;
		worstFairnessSolution = null;
		currentNotVotedCount = 0;
		voteIndexModification = 0;
	}

	private static class WeightedSolution implements
			Comparable<WeightedSolution> {

		private final int penalty;

		private final int fairness;

		private final ISolution solution;

		private WeightedSolution(final ISolution solution, final int penalty,
				final int fairness) {
			this.solution = solution;
			this.penalty = penalty;
			this.fairness = fairness;
		}

		public ISolution getSolution() {
			return solution;
		}

		public int getPenalty() {
			return penalty;
		}

		public int getFairness() {
			return fairness;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + fairness;
			result = prime * result + penalty;
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final WeightedSolution other = (WeightedSolution) obj;
			if (fairness != other.fairness) {
				return false;
			}
			if (penalty != other.penalty) {
				return false;
			}
			return true;
		}

		@Override
		public int compareTo(final WeightedSolution o) {
			if (penalty < o.penalty) {
				return -1;
			} else if (penalty > o.penalty) {
				return 1;
			} else {
				if (fairness < o.fairness) {
					return -1;
				} else if (fairness > o.fairness) {
					return 1;
				}
				return 0;
			}
		}

	}

}
