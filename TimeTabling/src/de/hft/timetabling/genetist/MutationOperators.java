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
	public static ISolution mutateCourseIsolation(ISolution solution) {
		ISolution mutatedSolution = null;
		ICourse[][] mutatedCoding = solution.getCoding().clone();
		IProblemInstance problemInstance = solution.getProblemInstance();
		ICourse courseToSwitch = null;

		// First, randomly find any lecture.
		Random random = new Random();
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
		TimeTableSlot nearestFreeSlot = findNearestFreeValidTimeTableSlotSameRoom(
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
			ICourse[][] coding, int basePeriod, int room,
			IProblemInstance instance) {

		TimeTableSlot nextFree = findNextFreeValidTimeTableSlotSameRoom(coding,
				basePeriod, room, true, instance);
		if (nextFree == null) {
			return null;
		}
		TimeTableSlot previousFree = findNextFreeValidTimeTableSlotSameRoom(
				coding, basePeriod, room, false, instance);
		if (Math.abs(basePeriod - nextFree.getPeriod()) > Math.abs(basePeriod
				- previousFree.getPeriod())) {
			return previousFree;
		}
		return nextFree;
	}

	private static TimeTableSlot findNextFreeValidTimeTableSlotSameRoom(
			ICourse[][] coding, int basePeriod, int room, boolean next,
			IProblemInstance instance) {

		int numberOfPeriods = instance.getNumberOfPeriods();
		ICourse course = coding[basePeriod][room];

		int targetPeriod = next ? PeriodUtil.getNextPeriod(basePeriod,
				numberOfPeriods) : PeriodUtil.getPreviousPeriod(basePeriod,
				numberOfPeriods);
		while (targetPeriod != basePeriod) {
			ICourse courseAtTarget = coding[targetPeriod][room];
			if (courseAtTarget == null) {
				boolean unavailabilityConstraintViolated = HardConstraintUtil
						.existsUnavailabilityConstraint(course, targetPeriod);
				boolean curriculumViolated = HardConstraintUtil
						.existsCurriculaInPeriod(coding, course.getCurricula(),
								targetPeriod);
				boolean teacherViolated = HardConstraintUtil
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
	public static ISolution mutateRoomStability(ISolution solution) {
		IProblemInstance pi = solution.getProblemInstance();
		ICourse[][] courses = solution.getCoding();
		int roomY = 0, periodX = 0;
		ICurriculum myCurriculum = null;

		while (myCurriculum == null) {
			roomY = (int) (pi.getRooms().size() * Math.random());
			periodX = (int) (pi.getNumberOfPeriods() * Math.random());
			if (courses[periodX][roomY] != null) {
				Set<ICurriculum> cur = courses[periodX][roomY].getCurricula();
				int random = (int) (cur.size() * Math.random());
				ICurriculum curriculum = cur
						.toArray(new ICurriculum[cur.size()])[random];
				myCurriculum = getCurriculumOutOfSet(cur, curriculum.getId());
			}
		}

		for (int i = 0; i < courses.length; i++) {
			if (i != periodX) {
				for (int j = 0; j < courses[i].length; j++) {
					ICourse selectedCourse = courses[i][j];
					if (selectedCourse != null) {
						Set<ICurriculum> tmpCur = selectedCourse.getCurricula();

						for (ICurriculum iCurriculum : tmpCur) {
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

		ISolutionTableService solutionTable = ServiceLocator.getInstance()
				.getSolutionTableService();
		ISolution newSolution = solutionTable.createNewSolution(courses,
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
	private static ICurriculum getCurriculumOutOfSet(Set<ICurriculum> set,
			String searchedOneId) {

		for (ICurriculum iCurriculum : set) {
			if (iCurriculum.getId().equals(searchedOneId)) {
				return iCurriculum;
			}
		}
		return null;
	}

}
