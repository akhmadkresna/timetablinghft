package de.hft.timetabling.evaluator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.services.IEvaluatorService;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.ServiceLocator;

public class NewEvaluator implements IEvaluatorService {

	private final ISolutionTableService table = ServiceLocator.getInstance()
			.getSolutionTableService();

	// TODO more sane data handling
	private int[] curriculumCosts;

	@Override
	public int evaluateSolution(ISolution newSolution) {
		final IProblemInstance instance = newSolution.getProblemInstance();

		// TODO combine into one variable once it works reliably
		int totalRoomCapacityPenalty = 0;
		int totalMinimumWorkingDaysPenalty = 0;
		int totalCurriculumCompactnessPenalty = 0;
		int totalRoomStabilityPenalty = 0;

		// TODO combine into one variable once it works reliably
		final Map<ICurriculum, Integer> specificRoomCapacityPenalty = new HashMap<ICurriculum, Integer>();
		final Map<ICurriculum, Integer> specificMinimumWorkingDaysPenalty = new HashMap<ICurriculum, Integer>();
		final Map<ICurriculum, Integer> specificCurriculumCompactnessPenalty = new HashMap<ICurriculum, Integer>();
		final Map<ICurriculum, Integer> specificRoomStabilityPenalty = new HashMap<ICurriculum, Integer>();

		final Map<ICourse, Set<Integer>> roomsPerCourse = new HashMap<ICourse, Set<Integer>>();
		final Map<ICourse, Set<Integer>> workingDaysPerCourse = new HashMap<ICourse, Set<Integer>>();
		final Set<ICurriculum> curriculaInSinglePeriod = new HashSet<ICurriculum>();
		final Map<ICurriculum, List<Integer>> curriculaInPeriods = new HashMap<ICurriculum, List<Integer>>();

		curriculumCosts = new int[instance.getCurricula().size()];

		for (final ICourse course : instance.getCourses()) {
			roomsPerCourse.put(course, new HashSet<Integer>());
			workingDaysPerCourse.put(course, new HashSet<Integer>());
		}

		for (ICurriculum curriculum : instance.getCurricula()) {
			specificCurriculumCompactnessPenalty.put(curriculum, 0);
			specificMinimumWorkingDaysPenalty.put(curriculum, 0);
			specificRoomCapacityPenalty.put(curriculum, 0);
			specificRoomStabilityPenalty.put(curriculum, 0);
			curriculaInPeriods.put(curriculum, new ArrayList<Integer>());
		}

		ICourse[][] schedule = newSolution.getCoding();
		for (int period = 0; period < schedule.length; period++) {
			curriculaInSinglePeriod.clear();

			for (int room = 0; room < schedule[period].length; room++) {
				ICourse course = schedule[period][room];

				if (course != null) {
					/* calculate room capacity penalty */
					int roomStudentDifference = course.getNumberOfStudents()
							- instance.getRoomByUniqueNumber(room)
									.getCapacity();
					int roomCapacityPenalty = roomStudentDifference < 0 ? 0
							: roomStudentDifference;
					totalRoomCapacityPenalty += roomCapacityPenalty;

					for (ICurriculum curriculum : course.getCurricula()) {
						int penalty = specificRoomCapacityPenalty
								.get(curriculum);
						penalty += roomCapacityPenalty;
						specificRoomCapacityPenalty.put(curriculum, penalty);
					}

					/* store all rooms in which a course takes place */
					roomsPerCourse.get(course).add(room);

					/* store all days on which a course takes place */
					int day = period / instance.getPeriodsPerDay();
					workingDaysPerCourse.get(course).add(day);

					/* store which curricula occur in a single period */
					curriculaInSinglePeriod.addAll(course.getCurricula());
				}
			}

			for (ICurriculum curriculum : curriculaInSinglePeriod) {
				curriculaInPeriods.get(curriculum).add(period);
			}
		}

		for (ICourse course : instance.getCourses()) {
			/* calculate room stability penalty */
			int roomStabilityPenalty = roomsPerCourse.get(course).size() - 1;
			totalRoomStabilityPenalty += roomStabilityPenalty;

			/* calculate the minimum working days penalty */
			int workingDaysDifference = course.getMinWorkingDays()
					- workingDaysPerCourse.get(course).size();
			int minimumWorkingDaysPenalty = workingDaysDifference < 0 ? 0
					: workingDaysDifference * 5;
			totalMinimumWorkingDaysPenalty += minimumWorkingDaysPenalty;

			/* add penalty for specific curricula */
			for (ICurriculum curriculum : course.getCurricula()) {
				int penalty = specificRoomStabilityPenalty.get(curriculum);
				penalty += roomStabilityPenalty;
				specificRoomStabilityPenalty.put(curriculum, penalty);

				penalty = specificMinimumWorkingDaysPenalty.get(curriculum);
				penalty += minimumWorkingDaysPenalty;
				specificMinimumWorkingDaysPenalty.put(curriculum, penalty);
			}
		}

		// TODO ugly, make pretty
		int k = 0;
		for (ICurriculum curriculum : curriculaInPeriods.keySet()) {
			List<Integer> periods = new ArrayList<Integer>(curriculaInPeriods
					.get(curriculum));
			if (periods.size() == 1) {
				continue;
			}
			Collections.sort(periods);

			int curriculumCompactnessPenalty = 0;

			for (int i = 0; i < periods.size(); i++) {
				int dayOfPreviousPeriod = -1;
				int previousPeriod = -1;
				if (i > 0) {
					previousPeriod = periods.get(i - 1);
					dayOfPreviousPeriod = previousPeriod
							/ instance.getNumberOfDays();
				}

				int currentPeriod = periods.get(i);
				int dayOfCurrentPeriod = currentPeriod
						/ instance.getNumberOfDays();

				int nextPeriod = -1;
				int dayOfNextPeriod = -1;
				if (i + 1 < periods.size()) {
					nextPeriod = periods.get(i + 1);
					dayOfNextPeriod = nextPeriod / instance.getNumberOfDays();
				}

				if ((dayOfPreviousPeriod != dayOfCurrentPeriod)
						&& (dayOfCurrentPeriod == dayOfNextPeriod)) {
					if (nextPeriod - currentPeriod > 1) {
						curriculumCompactnessPenalty++;
						// System.out.println("1. Curriculum "
						// + curriculum.getId() + " Period "
						// + currentPeriod);
					}
				} else if ((dayOfPreviousPeriod == dayOfCurrentPeriod)
						&& (dayOfCurrentPeriod == dayOfNextPeriod)) {
					if ((currentPeriod - previousPeriod > 1)
							&& (nextPeriod - currentPeriod > 1)) {
						curriculumCompactnessPenalty++;
						// System.out.println("2. Curriculum "
						// + curriculum.getId() + " Period "
						// + currentPeriod);
					}
				} else if ((dayOfPreviousPeriod == dayOfCurrentPeriod)
						&& (dayOfCurrentPeriod != dayOfNextPeriod)) {
					if (currentPeriod - previousPeriod > 1) {
						curriculumCompactnessPenalty++;
						// System.out.println("3. Curriculum "
						// + curriculum.getId() + " Period "
						// + currentPeriod);
					}
				} else if ((dayOfPreviousPeriod != dayOfCurrentPeriod)
						&& (dayOfCurrentPeriod != dayOfNextPeriod)) {
					curriculumCompactnessPenalty++;
					// System.out.println("4. Curriculum " + curriculum.getId()
					// + " Period " + currentPeriod);
				}
			}

			curriculumCompactnessPenalty *= 2;
			totalCurriculumCompactnessPenalty += curriculumCompactnessPenalty;
			int penalty = specificCurriculumCompactnessPenalty.get(curriculum);
			penalty += curriculumCompactnessPenalty;
			specificCurriculumCompactnessPenalty.put(curriculum, penalty);

			/* calculate total costs for specific curriculum */
			curriculumCosts[k++] = specificCurriculumCompactnessPenalty
					.get(curriculum)
					+ specificMinimumWorkingDaysPenalty.get(curriculum)
					+ specificRoomCapacityPenalty.get(curriculum)
					+ specificRoomStabilityPenalty.get(curriculum);
		}

		// System.out.println("Cost of RoomCapacity: " +
		// totalRoomCapacityPenalty);
		// System.out.println("Cost of MinWorkingDays: "
		// + totalMinimumWorkingDaysPenalty);
		// System.out.println("Cost of CurriculumCompactness: "
		// + totalCurriculumCompactnessPenalty);
		// System.out.println("Cost of RoomStability: "
		// + totalRoomStabilityPenalty);
		//
		// System.out.println("Fairness new evalutor: "
		// + calculateSolutionFairness());

		return totalRoomCapacityPenalty + totalMinimumWorkingDaysPenalty
				+ totalCurriculumCompactnessPenalty + totalRoomStabilityPenalty;
	}

	@Override
	public void evaluateSolutions() {
		int i = 0;
		for (ISolution solution : table.getNotVotedSolutions()) {
			int penalty = evaluateSolution(solution);
			int fairness = calculateSolutionFairness();
			System.out.println("Fairness new evaluator:" + fairness);
			table.voteForSolution(i++, penalty, fairness);
		}
	}

	private int calculateSolutionFairness() {
		int iFairnessCost = 0, maxAvgDiff, minAvgDiff;
		int maxPenalty = -1, minPenalty = -1, avgPenalty = -1, penaltySum = 0;

		// initial value to compare with
		maxPenalty = curriculumCosts[0];
		minPenalty = curriculumCosts[0];
		for (int i = 1; i < curriculumCosts.length; i++) {
			// Take max value, min value, and average value... and compare
			if (curriculumCosts[i] > maxPenalty) {
				maxPenalty = curriculumCosts[i];
			}
			if (curriculumCosts[i] < minPenalty) {
				minPenalty = curriculumCosts[i];
			}
			penaltySum += curriculumCosts[i];
		}
		avgPenalty = (penaltySum / curriculumCosts.length);

		maxAvgDiff = maxPenalty - avgPenalty;
		minAvgDiff = avgPenalty - minPenalty;

		if (maxAvgDiff >= minAvgDiff) {
			// iFairnessCost = maxAvgDiff;
			iFairnessCost = maxAvgDiff;
		} else {
			// iFairnessCost = minAvgDiff;
			iFairnessCost = minAvgDiff;
		}

		return iFairnessCost;
	}
}
