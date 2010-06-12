package de.hft.timetabling.genetist;

import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.ServiceLocator;

/**
 * This class offers various mutation operators that can be used by the
 * individual recombination strategies.
 */
final class MutationOperators {

	/**
	 * Mutation algorithm to mutate course stability.
	 * 
	 * @param solution
	 *            solution to mutate
	 * @return mutated solution
	 */
	public static ISolution mutateCourseIsolation(ISolution solution) {
		ISolutionTableService solutionTable = ServiceLocator.getInstance()
				.getSolutionTableService();
		ISolution newSolution = solutionTable.createNewSolution(solution
				.getCoding(), solution.getParentSolutions(), solution
				.getProblemInstance());

		int n1 = 0, n2 = 0;
		while (n1 == n2) {
			n1 = (int) (newSolution.getCoding().length * Math.random());
			n2 = (int) (newSolution.getCoding().length * Math.random());
		}

		ICourse[] temp = newSolution.getCoding()[n1];
		newSolution.getCoding()[n1] = newSolution.getCoding()[n2];
		newSolution.getCoding()[n2] = temp;

		return newSolution;
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
			// System.out.println("... trying to find curriculum [" + periodX+
			// "][" + roomY + "] ...");
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
								// No check necessary because we only changed
								// the room --> soft constraint!?
							}
						}
					}
				}
			}
		}

		ISolutionTableService solutionTable = ServiceLocator.getInstance()
				.getSolutionTableService();
		ISolution newSolution = solutionTable.createNewSolution(courses,
				solution.getParentSolutions(), solution.getProblemInstance());
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
