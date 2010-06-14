package de.hft.timetabling.genetist;

import java.util.Random;
import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.ServiceLocator;
import de.hft.timetabling.util.HardConstraintUtil;
import de.hft.timetabling.util.PeriodUtil;

/**
 * This class offers various mutation operators that can be used by the
 * individual recombination strategies.
 * <p>
 * All mutation operators are hard-constraint valid. Therefore it is guaranteed
 * that after mutation the solution is still valid. However, it is possible that
 * because of hard-constraint violations mutation is not possible. In this case
 * all the operators return <tt>null</tt>.
 */
final class MutationOperators {

	/**
	 * Changes the period of a randomly selected course to the next free period.
	 * Returns the mutated solution or <tt>null</tt> if no hard-constraint valid
	 * mutated solution could be found.
	 * 
	 * @param solution
	 *            The solution to mutate.
	 */
	public static ISolution mutateCourseIsolation(final ISolution solution) {
		ISolution mutatedSolution = null;
		final ICourse[][] mutatedCoding = solution.getCoding().clone();
		final IProblemInstance problemInstance = solution.getProblemInstance();
		ICourse courseToSwitch = null;

		// First, randomly find any lecture.
		final Random random = new Random();
		int randomlySelectedPeriod = 0;
		int randomlySelectedRoom = 0;
		while (courseToSwitch == null) {
			randomlySelectedPeriod = random.nextInt(problemInstance
					.getNumberOfPeriods());
			randomlySelectedRoom = random.nextInt(problemInstance
					.getNumberOfRooms());
			courseToSwitch = mutatedCoding[randomlySelectedPeriod][randomlySelectedRoom];
		}

		/*
		 * Second, find the nearest empty valid time table slot with the same
		 * room to switch to.
		 */
		final TimeTableSlot nearestFreeSlot = findNearestFreeValidTimeTableSlotSameRoom(
				mutatedCoding, randomlySelectedPeriod, randomlySelectedRoom,
				problemInstance);
		if (nearestFreeSlot == null) {
			return null;
		}

		// Third, switch the course to the target period.
		mutatedCoding[nearestFreeSlot.getPeriod()][randomlySelectedRoom] = courseToSwitch;
		mutatedCoding[randomlySelectedPeriod][randomlySelectedRoom] = null;
		mutatedSolution = getSolutionTable().createNewSolution(mutatedCoding,
				problemInstance);

		return mutatedSolution;
	}

	private static TimeTableSlot findNearestFreeValidTimeTableSlotSameRoom(
			final ICourse[][] coding, final int basePeriod, final int room,
			final IProblemInstance instance) {

		final TimeTableSlot nextFree = findNextFreeValidTimeTableSlotSameRoom(
				coding, basePeriod, room, true, instance);
		if (nextFree == null) {
			return null;
		}
		final TimeTableSlot previousFree = findNextFreeValidTimeTableSlotSameRoom(
				coding, basePeriod, room, false, instance);
		if (Math.abs(basePeriod - nextFree.getPeriod()) > Math.abs(basePeriod
				- previousFree.getPeriod())) {
			return previousFree;
		}
		return nextFree;
	}

	private static TimeTableSlot findNextFreeValidTimeTableSlotSameRoom(
			final ICourse[][] coding, final int basePeriod, final int room,
			final boolean next, final IProblemInstance instance) {

		final int numberOfPeriods = instance.getNumberOfPeriods();
		final ICourse course = coding[basePeriod][room];

		int targetPeriod = next ? PeriodUtil.getNextPeriod(basePeriod,
				numberOfPeriods) : PeriodUtil.getPreviousPeriod(basePeriod,
				numberOfPeriods);
		while (targetPeriod != basePeriod) {
			final ICourse courseAtTarget = coding[targetPeriod][room];
			if (courseAtTarget == null) {
				final boolean unavailabilityConstraintViolated = HardConstraintUtil
						.existsUnavailabilityConstraint(course, targetPeriod);
				final boolean curriculumViolated = HardConstraintUtil
						.existsCurriculaInPeriod(coding, course.getCurricula(),
								targetPeriod);
				final boolean teacherViolated = HardConstraintUtil
						.existsTeacherInPeriod(coding, course.getTeacher(),
								targetPeriod);
				if (!(unavailabilityConstraintViolated || curriculumViolated || teacherViolated)) {
					break;
				}
			}
			targetPeriod = next ? PeriodUtil.getNextPeriod(targetPeriod,
					numberOfPeriods) : PeriodUtil.getPreviousPeriod(
					targetPeriod, numberOfPeriods);
		}

		if (targetPeriod == basePeriod) {
			return null;
		}

		return new TimeTableSlot(targetPeriod, room);
	}

	private static ISolutionTableService getSolutionTable() {
		return ServiceLocator.getInstance().getSolutionTableService();
	}

	/**
	 * Mutation algorithm.
	 * 
	 * @param solution
	 *            that should be mutated.
	 * @return mutated solution
	 */
	public static ISolution mutateRoomStability(final ISolution solution) {
		final IProblemInstance pi = solution.getProblemInstance();
		final ICourse[][] courses = solution.getCoding();
		int roomY = 0, periodX = 0;
		ICurriculum myCurriculum = null;

		while (myCurriculum == null) {
			roomY = (int) (pi.getRooms().size() * Math.random());
			periodX = (int) (pi.getNumberOfPeriods() * Math.random());
			if (courses[periodX][roomY] != null) {
				final Set<ICurriculum> cur = courses[periodX][roomY]
						.getCurricula();
				final int random = (int) (cur.size() * Math.random());
				final ICurriculum curriculum = cur.toArray(new ICurriculum[cur
						.size()])[random];
				myCurriculum = getCurriculumOutOfSet(cur, curriculum.getId());
			}
		}

		for (int i = 0; i < courses.length; i++) {
			if (i != periodX) {
				for (int j = 0; j < courses[i].length; j++) {
					final ICourse selectedCourse = courses[i][j];
					if (selectedCourse != null) {
						final Set<ICurriculum> tmpCur = selectedCourse
								.getCurricula();

						for (final ICurriculum iCurriculum : tmpCur) {
							if (iCurriculum.getId()
									.equals(myCurriculum.getId())) {
								courses[i][j] = courses[i][roomY];
								courses[i][roomY] = selectedCourse;
							}
						}
					}
				}
			}
		}

		final ISolutionTableService solutionTable = ServiceLocator
				.getInstance().getSolutionTableService();
		final ISolution newSolution = solutionTable.createNewSolution(courses,
				solution.getProblemInstance());
		newSolution.setRecombinationCount(solution.getRecombinationCount() + 1);
		return newSolution;
	}

	/**
	 * Method to get a ICurriculum out of a Set<ICurriculum> chosen by the ID of
	 * a ICurriculum
	 * 
	 * @param set
	 *            Set<ICurriculum>
	 * @param searchedOneId
	 *            ID of ICurriculum that should be searched for
	 * @return the found item
	 */
	private static ICurriculum getCurriculumOutOfSet(
			final Set<ICurriculum> set, final String searchedOneId) {

		for (final ICurriculum iCurriculum : set) {
			if (iCurriculum.getId().equals(searchedOneId)) {
				return iCurriculum;
			}
		}
		return null;
	}

}
