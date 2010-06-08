package de.hft.timetabling.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

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

	private final TreeSet<WeightedSolution> solutionTable;

	private final List<ISolution> notVotedTable;

	private WeightedSolution bestPenaltySolution;

	private WeightedSolution bestFairnessSolution;

	private int currentNotVotedCount;

	public SolutionTable() {
		solutionTable = new TreeSet<WeightedSolution>();
		notVotedTable = new ArrayList<ISolution>(
				ISolutionTableService.TABLE_SIZE);
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
	public void addSolution(ISolution solution) {
		if (getSize() == ISolutionTableService.TABLE_SIZE) {
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
	public int getBestFairnessSolutionFairness() {
		return (bestFairnessSolution == null) ? 0 : bestFairnessSolution
				.getFairness();
	}

	@Override
	public int getBestFairnessSolutionPenalty() {
		return (bestFairnessSolution == null) ? 0 : bestFairnessSolution
				.getPenalty();
	}

	@Override
	public int getBestPenaltySolutionFairness() {
		return (bestPenaltySolution == null) ? 0 : bestPenaltySolution
				.getFairness();
	}

	@Override
	public int getBestPenaltySolutionPenalty() {
		return (bestPenaltySolution == null) ? 0 : bestPenaltySolution
				.getPenalty();
	}

	@Override
	public List<ISolution> getNotVotedSolutions() {
		return Collections.unmodifiableList(notVotedTable);
	}

	@Override
	public void voteForSolution(int index, int penalty, int fairness) {
		ISolution solution = notVotedTable.get(index);
		solutionTable.add(new WeightedSolution(solution, penalty, fairness));
		currentNotVotedCount--;
	}

	@Override
	public int getSize() {
		return solutionTable.size() + currentNotVotedCount;
	}

	@Override
	public void removeWorstSolution() {
		solutionTable.pollLast();
	}

	@Override
	public int getNumberOfEmptySlots() {
		return TABLE_SIZE - getSize();
	}

	@Override
	public String toString() {
		return "Solution Table (" + getSize() + " entries)";
	}

	@Override
	public ISolution getSolution(int index) {
		WeightedSolution[] array = solutionTable
				.toArray(new WeightedSolution[getSize()]);
		return array[index].getSolution();
	}

	@Override
	public void update() {
		notVotedTable.clear();
		currentNotVotedCount = 0;
		bestPenaltySolution = solutionTable.first();
		WeightedSolution bestFairnessInTable = null;
		for (WeightedSolution weightedSolution : solutionTable) {
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
		} else {
			if (bestFairnessInTable.getFairness() < bestFairnessSolution
					.getFairness()) {
				bestFairnessSolution = bestFairnessInTable;
			}
		}
	}

	private static class WeightedSolution implements
			Comparable<WeightedSolution> {

		private final int penalty;

		private final int fairness;

		private final ISolution solution;

		private WeightedSolution(ISolution solution, int penalty, int fairness) {
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
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			WeightedSolution other = (WeightedSolution) obj;
			if (fairness != other.fairness) {
				return false;
			}
			if (penalty != other.penalty) {
				return false;
			}
			return true;
		}

		@Override
		public int compareTo(WeightedSolution o) {
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
