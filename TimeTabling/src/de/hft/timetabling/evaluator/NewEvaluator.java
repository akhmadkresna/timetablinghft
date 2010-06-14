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
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.ServiceLocator;

public final class NewEvaluator {

	private EvaluationResult result;

	public void evaluateSolutions() {
		final ISolutionTableService table = ServiceLocator.getInstance()
				.getSolutionTableService();

		int i = 0;

		for (final ISolution solution : table.getNotVotedSolutions()) {
			EvaluationResult result = evaluateSolution(solution);
			table.voteForSolution(i++, result.getTotalPenalty(), result
					.getTotalFairness());
		}
	}

	public EvaluationResult evaluateSolution(final ISolution newSolution) {
		final EvaluationResult result = new EvaluationResult(newSolution);

		calcCurCompPen(newSolution, result);
		calcMinWorkDaysPen(newSolution, result);
		calcRoomCapPen(newSolution, result);
		calcRoomStabPen(newSolution, result);
		evaluateFairness(result);

		return result;
	}

	public int evaluateFairness(EvaluationResult result) {
		int iFairnessCost = 0, maxAvgDiff, minAvgDiff;
		int maxPenalty = -1, minPenalty = -1, avgPenalty = -1, penaltySum = 0;

		List<Integer> curriculumCosts = result.getPenaltyPerCurriculum();

		// initial value to compare with
		maxPenalty = curriculumCosts.get(0);
		minPenalty = curriculumCosts.get(0);
		for (int i = 1; i < curriculumCosts.size(); i++) {
			// Take max value, min value, and average value... and compare
			if (curriculumCosts.get(i) > maxPenalty) {
				maxPenalty = curriculumCosts.get(i);
			}
			if (curriculumCosts.get(i) < minPenalty) {
				minPenalty = curriculumCosts.get(i);
			}
			penaltySum += curriculumCosts.get(i);
		}
		avgPenalty = (penaltySum / curriculumCosts.size());

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

	private void calcRoomCapPen(final ISolution sol, final EvaluationResult res) {
		final ICourse[][] schedule = sol.getCoding();
		final IProblemInstance instance = sol.getProblemInstance();

		for (final ICourse[] element : schedule) {
			for (int room = 0; room < element.length; room++) {
				final ICourse course = element[room];

				if (course == null) {
					continue;
				}

				/* calculate room capacity penalty */
				final int roomStudentDifference = course.getNumberOfStudents()
						- instance.getRoomByUniqueNumber(room).getCapacity();
				final int roomCapacityPenalty = roomStudentDifference < 0 ? 0
						: roomStudentDifference;
				res.addTotalRoomCapacityPenalty(roomCapacityPenalty);

				for (final ICurriculum curriculum : course.getCurricula()) {
					res.addRoomCapacityPenalty(curriculum, roomCapacityPenalty);
				}
			}
		}
	}

	private void calcRoomStabPen(final ISolution sol, final EvaluationResult res) {
		final ICourse[][] schedule = sol.getCoding();
		final HashMap<ICourse, Set<Integer>> periods = new HashMap<ICourse, Set<Integer>>();

		for (final ICourse[] element : schedule) {
			for (int room = 0; room < element.length; room++) {
				final ICourse course = element[room];

				if (course == null) {
					continue;
				}

				if (periods.get(course) == null) {
					periods.put(course, new HashSet<Integer>());
				}

				periods.get(course).add(room);
			}
		}

		for (final ICourse course : periods.keySet()) {
			final int roomStabilityPenalty = periods.get(course).size() - 1;
			res.addTotalRoomStabilityPenalty(roomStabilityPenalty);

			for (final ICurriculum cur : course.getCurricula()) {
				res.addRoomCapacityPenalty(cur, roomStabilityPenalty);
			}
		}

	}

	private void calcMinWorkDaysPen(final ISolution sol,
			final EvaluationResult res) {
		final IProblemInstance instance = sol.getProblemInstance();
		final ICourse[][] schedule = sol.getCoding();
		final Map<ICourse, Set<Integer>> workingDaysPerCourse = new HashMap<ICourse, Set<Integer>>();

		for (int period = 0; period < schedule.length; period++) {
			for (int room = 0; room < schedule[period].length; room++) {
				final ICourse course = schedule[period][room];

				if (course == null) {
					continue;
				}

				if (workingDaysPerCourse.get(course) == null) {
					workingDaysPerCourse.put(course, new HashSet<Integer>());
				}

				/* store all days on which a course takes place */
				final int day = period / instance.getPeriodsPerDay();
				workingDaysPerCourse.get(course).add(day);
			}
		}

		for (final ICourse course : instance.getCourses()) {
			/* calculate the minimum working days penalty */
			final int workingDaysDifference = course.getMinWorkingDays()
					- workingDaysPerCourse.get(course).size();
			final int minimumWorkingDaysPenalty = workingDaysDifference < 0 ? 0
					: workingDaysDifference * 5;
			res.addTotalMinimumWorkingDaysPenalty(minimumWorkingDaysPenalty);

			/* add penalty for specific curricula */
			for (final ICurriculum curriculum : course.getCurricula()) {
				res.addMinimumWorkingDaysPenalty(curriculum,
						minimumWorkingDaysPenalty);
			}
		}
	}

	private void calcCurCompPen(final ISolution sol, final EvaluationResult res) {
		final IProblemInstance instance = sol.getProblemInstance();
		final ICourse[][] schedule = sol.getCoding();
		final Map<ICurriculum, List<Integer>> curriculaInPeriods = new HashMap<ICurriculum, List<Integer>>();

		for (int period = 0; period < schedule.length; period++) {
			final Set<ICurriculum> curriculaInSinglePeriod = new HashSet<ICurriculum>();

			for (int room = 0; room < schedule[period].length; room++) {
				final ICourse course = schedule[period][room];

				if (course == null) {
					continue;
				}

				curriculaInSinglePeriod.addAll(course.getCurricula());
			}

			for (final ICurriculum curriculum : curriculaInSinglePeriod) {
				if (curriculaInPeriods.get(curriculum) == null) {
					curriculaInPeriods
							.put(curriculum, new ArrayList<Integer>());
				}
				curriculaInPeriods.get(curriculum).add(period);
			}
		}

		for (final ICurriculum curriculum : curriculaInPeriods.keySet()) {
			final List<Integer> periods = new ArrayList<Integer>(
					curriculaInPeriods.get(curriculum));
			if (periods.size() == 1) {
				continue;
			}
			Collections.sort(periods);

			int curriculumCompactnessPenalty = 0;

			for (int i = 0; i < periods.size(); i++) {
				int previousPeriod = -1;
				int dayOfPreviousPeriod = -1;
				if (i > 0) {
					previousPeriod = periods.get(i - 1);
					dayOfPreviousPeriod = previousPeriod
							/ instance.getPeriodsPerDay();
				}

				final int currentPeriod = periods.get(i);
				final int dayOfCurrentPeriod = currentPeriod
						/ instance.getPeriodsPerDay();

				int nextPeriod = -1;
				int dayOfNextPeriod = -1;
				if (i + 1 < periods.size()) {
					nextPeriod = periods.get(i + 1);
					dayOfNextPeriod = nextPeriod / instance.getPeriodsPerDay();
				}

				if ((dayOfPreviousPeriod != dayOfCurrentPeriod)
						&& (dayOfCurrentPeriod == dayOfNextPeriod)) {
					if (nextPeriod - currentPeriod > 1) {
						curriculumCompactnessPenalty++;
						// System.out.println("Curriculum " + curriculum.getId()
						// + " Period " + currentPeriod);
					}
				} else if ((dayOfPreviousPeriod == dayOfCurrentPeriod)
						&& (dayOfCurrentPeriod == dayOfNextPeriod)) {
					if ((currentPeriod - previousPeriod > 1)
							&& (nextPeriod - currentPeriod > 1)) {
						curriculumCompactnessPenalty++;
						// System.out.println("Curriculum " + curriculum.getId()
						// + " Period " + currentPeriod);
					}
				} else if ((dayOfPreviousPeriod == dayOfCurrentPeriod)
						&& (dayOfCurrentPeriod != dayOfNextPeriod)) {
					if (currentPeriod - previousPeriod > 1) {
						curriculumCompactnessPenalty++;
						// System.out.println("Curriculum " + curriculum.getId()
						// + " Period " + currentPeriod);
					}
				} else if ((dayOfPreviousPeriod != dayOfCurrentPeriod)
						&& (dayOfCurrentPeriod != dayOfNextPeriod)) {
					curriculumCompactnessPenalty++;
					// System.out.println("Curriculum " + curriculum.getId()
					// + " Period " + currentPeriod);
				}
			}

			curriculumCompactnessPenalty *= 2;
			res
					.addTotalCurriculumCompactnessPenalty(curriculumCompactnessPenalty);

			res.addCurriculumCompactnessPenalty(curriculum,
					curriculumCompactnessPenalty);
		}
	}
}
