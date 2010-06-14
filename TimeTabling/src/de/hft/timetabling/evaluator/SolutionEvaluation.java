package de.hft.timetabling.evaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.ISolution;

public class SolutionEvaluation {

	private final ISolution solution;

	private int totalPenalty = 0;

	private int totalFairness = 0;

	private int totalRoomCapacityPenalty = 0;

	private int totalMinimumWorkingDaysPenalty = 0;

	private int totalCurriculumCompactnessPenalty = 0;

	private int totalRoomStabilityPenalty = 0;

	private final Map<ICurriculum, Integer> specificRoomCapacityPenalty = new HashMap<ICurriculum, Integer>();

	private final Map<ICurriculum, Integer> specificMinimumWorkingDaysPenalty = new HashMap<ICurriculum, Integer>();

	private final Map<ICurriculum, Integer> specificCurriculumCompactnessPenalty = new HashMap<ICurriculum, Integer>();

	private final Map<ICurriculum, Integer> specificRoomStabilityPenalty = new HashMap<ICurriculum, Integer>();

	public SolutionEvaluation(final ISolution solution) {
		this.solution = solution;

		for (final ICurriculum curriculum : solution.getProblemInstance()
				.getCurricula()) {
			specificCurriculumCompactnessPenalty.put(curriculum, 0);
			specificMinimumWorkingDaysPenalty.put(curriculum, 0);
			specificRoomCapacityPenalty.put(curriculum, 0);
			specificRoomStabilityPenalty.put(curriculum, 0);
		}
	}

	public ISolution getSolution() {
		return solution;
	}

	public int getTotalPenalty() {
		return totalPenalty;
	}

	public int getTotalFairness() {
		return totalFairness;
	}

	public int getRoomCapacityPenalty(final ICurriculum curriculum) {
		return specificRoomCapacityPenalty.get(curriculum);
	}

	public int getMinimumWorkingDaysPenalty(final ICurriculum curriculum) {
		return specificMinimumWorkingDaysPenalty.get(curriculum);
	}

	public int getCurriculimCompactnessPenalty(final ICurriculum curriculum) {
		return specificCurriculumCompactnessPenalty.get(curriculum);
	}

	public int getRoomStabilityPenalty(final ICurriculum curriculum) {
		return specificRoomStabilityPenalty.get(curriculum);
	}

	// public void addTotalPenalty(int penalty) {
	// totalPenalty += penalty;
	// }

	public void setTotalFairness(final int totalFairness) {
		this.totalFairness = totalFairness;
	}

	public void addTotalRoomCapacityPenalty(final int newPenalty) {
		totalRoomCapacityPenalty += newPenalty;
		totalPenalty += newPenalty;
	}

	public void addTotalMinimumWorkingDaysPenalty(final int newPenalty) {
		totalMinimumWorkingDaysPenalty += newPenalty;
		totalPenalty += newPenalty;
	}

	public void addTotalCurriculumCompactnessPenalty(final int newPenalty) {
		totalCurriculumCompactnessPenalty += newPenalty;
		totalPenalty += newPenalty;
	}

	public void addTotalRoomStabilityPenalty(final int newPenalty) {
		totalRoomStabilityPenalty += newPenalty;
		totalPenalty += newPenalty;
	}

	public int getTotalRoomCapacityPenalty() {
		return totalRoomCapacityPenalty;
	}

	public int getTotalMinimumWorkingDaysPenalty() {
		return totalMinimumWorkingDaysPenalty;
	}

	public int getTotalCurriculumCompactnessPenalty() {
		return totalCurriculumCompactnessPenalty;
	}

	public int getTotalRoomStabilityPenalty() {
		return totalRoomStabilityPenalty;
	}

	public void addRoomCapacityPenalty(final ICurriculum curriculum,
			final int newPenalty) {
		int penalty = specificRoomCapacityPenalty.get(curriculum);
		penalty += newPenalty;
		specificRoomCapacityPenalty.put(curriculum, penalty);
	}

	public void addMinimumWorkingDaysPenalty(final ICurriculum curriculum,
			final int newPenalty) {
		int penalty = specificMinimumWorkingDaysPenalty.get(curriculum);
		penalty += newPenalty;
		specificMinimumWorkingDaysPenalty.put(curriculum, penalty);
	}

	public void addCurriculumCompactnessPenalty(final ICurriculum curriculum,
			final int newPenalty) {
		int penalty = specificCurriculumCompactnessPenalty.get(curriculum);
		penalty += newPenalty;
		specificCurriculumCompactnessPenalty.put(curriculum, penalty);
	}

	void addRoomStabilityPenalty(final ICurriculum curriculum,
			final int newPenalty) {
		int penalty = specificRoomStabilityPenalty.get(curriculum);
		penalty += newPenalty;
		specificRoomStabilityPenalty.put(curriculum, penalty);
	}

	public List<Integer> getPenaltyPerCurriculum() {
		final List<Integer> penalties = new ArrayList<Integer>();

		for (final ICurriculum curriculum : solution.getProblemInstance()
				.getCurricula()) {
			int penalty = 0;
			penalty += specificCurriculumCompactnessPenalty.get(curriculum);
			penalty += specificMinimumWorkingDaysPenalty.get(curriculum);
			penalty += specificRoomCapacityPenalty.get(curriculum);
			penalty += specificRoomStabilityPenalty.get(curriculum);

			penalties.add(penalty);
		}

		return penalties;
	}
}
