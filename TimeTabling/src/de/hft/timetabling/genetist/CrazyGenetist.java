package de.hft.timetabling.genetist;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.services.ICrazyGenetistService;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.ServiceLocator;

/**
 * The CrazyGenetist class is for recombinating and mutating a solution table.
 * 
 * @author Steffen
 * @author Sotiris
 * 
 */
public class CrazyGenetist implements ICrazyGenetistService {

	/**
	 * Solutions that should be improved.
	 */
	private ISolutionTableService solution;

	/**
	 * Iterations to chose one of the recombination algorithms. This number
	 * means the percentage of the maximum table size.
	 */
	private static final int ITERATIONS = 25;

	/**
	 * public Method to start recombination and mutation process. The solution
	 * table will get from serviceLocator.getSolutionTableService(). The
	 * Solutions that are recombined and mutated are chosen randomly.
	 */
	@Override
	public void recombineAndMutate() {
		ServiceLocator serviceLocator = ServiceLocator.getInstance();

		setSolution(serviceLocator.getSolutionTableService());

		// Solution that is given back afterwards.
		ISolution back = null;

		if (solution.getSize(false) > 1) {
			// Solution that is used to pull values out of.
			ISolution otherSolution = null;
			// Solution that is used as basic for recombination
			ISolution basicSolution = null;
			// Choosing solutions that until they are not null and different
			int handedInSolution = 0;
			int iterationsRounds = (ITERATIONS * ISolutionTableService.TABLE_SIZE) / 100;

			System.out.print("CRAZY GENETIST: Starting to create "
					+ iterationsRounds + " children (" + ITERATIONS + "%) ...");

			// Make sure that it is executed at least once
			if (iterationsRounds == 0) {
				iterationsRounds = 1;
			}

			// Doing as long as the maximum iterations are reached or in
			// (maximum iterations)*2 are done
			for (int i = 0; handedInSolution < iterationsRounds; i++) {
				while ((otherSolution == null) || (basicSolution == null)
						|| basicSolution.equals(otherSolution)) {

					// --> if (Math.random() < probability)

					int n1 = (int) (solution.getSize(false) * Math.random());
					int n2 = (int) (solution.getSize(false) * Math.random());
					basicSolution = solution.getSolution(n1);
					otherSolution = solution.getSolution(n2);

					/*
					 * //for debugging reasons if ((otherSolution == null)) {
					 * System.out.println("otherSolution is null"); } if
					 * ((basicSolution == null)) {
					 * System.out.println("basicSolution is null"); } if
					 * (((basicSolution != null) && (otherSolution != null)) &&
					 * basicSolution.equals(otherSolution)) {
					 * System.out.println("solutions are equal!:");
					 * System.out.println("Basic solution:\n" +
					 * CrazyGenetistUtility .coursesToStringId(basicSolution
					 * .getCoding())); System.out.println("Other solution:\n" +
					 * CrazyGenetistUtility .coursesToStringId(otherSolution
					 * .getCoding())); System.exit(0); }
					 */
				}

				back = mutateRoomStability(recombindation2(basicSolution,
						otherSolution));

				basicSolution.increaseRecombinationCount();
				otherSolution.increaseRecombinationCount();

				// Hand in solution
				if ((back != null) && new ValidatorImpl().isValidSolution(back)) {
					getSolution().removeWorstSolution();
					getSolution().addSolution(back);
					handedInSolution++;
				} else {
					System.out
							.println("CRAZY GENETIST: No valid solution found.");
					break;
				}
			}

			System.out.print(" done.\n");
		}
	}

	/**
	 * Method to set Solution that should be used.
	 * 
	 * @param solution
	 *            Solution table that should be recombined and mutated.
	 */
	public void setSolution(ISolutionTableService solution) {
		this.solution = solution;
	}

	/**
	 * Method to get Solution that should be recombined.
	 * 
	 * @return solution Solution table that should be recombined and mutated.
	 */
	public ISolutionTableService getSolution() {
		return solution;
	}

	/**
	 * Mutation algorithm.
	 * 
	 * @param solution
	 *            that should be mutated.
	 * @return mutated solution
	 */
	private ISolution mutateRoomStability(ISolution solution) {
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

				String curriculumNr = String.format("q%03d",
						(int) (cur.size() * Math.random()));

				myCurriculum = getCurriculumOutOfSet(cur, curriculumNr);
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
								// No check neccessary because we only changed
								// the
								// room --> soft constraint!?
							}
						}
					}
				}
			}
		}

		ISolution newSolution = this.solution.createNewSolution(courses,
				solution.getProblemInstance());
		newSolution.setRecombinationCount(solution.getRecombinationCount() + 1);
		return newSolution;
	}

	/**
	 * Algorithm 2 that recombines two solutions.
	 * 
	 * @param solution1
	 *            Basic solution for recombination
	 * @param solution2
	 *            Other solutions where ICourses are put of.
	 * @return recombied solution
	 */
	private ISolution recombindation2(ISolution solution1, ISolution solution2) {
		// ICourse[][] oldBestSolution;
		// ValidatorImpl vi = new ValidatorImpl();
		// Set<ICourse> savingList = new HashSet<ICourse>();
		ISolution newSolution = solution1.clone();

		for (int i = 0; i < newSolution.getCoding().length; i++) {
			for (int j = 0; j < newSolution.getCoding()[i].length; j++) {

				// Fill gap
				if ((newSolution.getCoding()[i][j] == null)
						&& (solution2.getCoding()[i][j] != null)) {
					if (!existsSameCurriculumInPeriod(newSolution, solution2
							.getCoding()[i][j], i)
							&& !existsSameTeacherInPeriod(newSolution,
									solution2.getCoding()[i][j], i)) {
						CoursePosition cp1 = getCoursePositionRandomly(getPositionOfCourse(
								newSolution, solution2.getCoding()[i][j]));

						newSolution.getCoding()[cp1.getX()][cp1.getY()] = null;

						newSolution.getCoding()[i][j] = solution2.getCoding()[i][j];

					} else if (existsSameCurriculumInPeriod(newSolution,
							solution2.getCoding()[i][j], i)
							&& existsSameTeacherInPeriod(newSolution, solution2
									.getCoding()[i][j], i)) {

						CoursePosition cp1 = getCoursePositionRandomly(getPositionOfCourse(
								newSolution, solution2.getCoding()[i][j]));
						CoursePosition cp2 = getIfSameCurriculumAndSameTeacher(
								newSolution, solution2.getCoding()[i][j], i);
						if (cp2 != null) {

							newSolution.getCoding()[cp1.getX()][cp1.getY()] = newSolution
									.getCoding()[cp2.getX()][cp2.getY()];

							newSolution.getCoding()[i][j] = solution2
									.getCoding()[i][j];
							newSolution.getCoding()[cp2.getX()][cp2.getY()] = null;

						}
					}
				}
			}
		}

		return newSolution;
	}

	/**
	 * Method that returns a set of CoursePositions where a course is find in
	 * the coding of courses.
	 * 
	 * @param courses
	 *            Solution that should be looked at.
	 * @param course
	 *            Searched course
	 * @return Set of the position of found courses
	 */
	private Set<CoursePosition> getPositionOfCourse(ISolution courses,
			ICourse course) {
		Set<CoursePosition> positions = new HashSet<CoursePosition>();
		for (int i = 0; i < courses.getCoding().length; i++) {
			for (int j = 0; j < courses.getCoding()[i].length; j++) {
				if (courses.getCoding()[i][j] != null) {
					if (courses.getCoding()[i][j].getId()
							.equals(course.getId())) {
						positions.add(new CoursePosition(i, j));
					}
				}
			}
		}
		return positions;
	}

	/**
	 * Method to get a Position out of a set of positions randomly.
	 * 
	 * @param set
	 *            Set of CoursePositions
	 * @return randomly selected CoursePosition
	 */
	private CoursePosition getCoursePositionRandomly(Set<CoursePosition> set) {
		int n = (int) (set.size() * Math.random());
		Iterator<CoursePosition> iter = set.iterator();
		for (int i = 0; iter.hasNext(); i++) {
			CoursePosition o = iter.next();
			if (i == n) {
				return o;
			}
		}
		return null;
	}

	/**
	 * Method return a Course position of a course that can be found in a
	 * specific period and has the same curriculum and teacher as the input
	 * course
	 * 
	 * @param courses
	 *            Solution that should be analysed.
	 * @param givenCourse
	 *            Course that should be found.
	 * @param period
	 *            period in which that course should be
	 * @return Position of found course
	 */
	private CoursePosition getIfSameCurriculumAndSameTeacher(ISolution courses,
			ICourse givenCourse, int period) {
		for (int i = 0; i < courses.getCoding()[period].length; i++) {
			if ((courses.getCoding()[period][i] != null)
					&& courses.getCoding()[period][i].getTeacher().equals(
							givenCourse.getTeacher())) {
				Set<ICurriculum> tmpCur = courses.getCoding()[period][i]
						.getCurricula();

				if ((tmpCur.size() == givenCourse.getCurricula().size())
						&& tmpCur.containsAll(givenCourse.getCurricula())) {
					return new CoursePosition(period, i);
				}
			}
		}
		return null;
	}

	/**
	 * Checks if there is a curriculum in the same period
	 * 
	 * @param courses
	 *            Solution that should be analysed.
	 * @param givenCourse
	 *            Course that should be found.
	 * @param period
	 *            period in which that course should be
	 * @return true if there is a course of the same curriculum in the same
	 *         period
	 */
	private boolean existsSameCurriculumInPeriod(ISolution courses,
			ICourse givenCourse, int period) {
		Set<ICurriculum> givenCourseCurriculum = givenCourse.getCurricula();

		for (int i = 0; i < courses.getCoding()[period].length; i++) {

			if (courses.getCoding()[period][i] != null) {
				Iterator<ICurriculum> iter = courses.getCoding()[period][i]
						.getCurricula().iterator();
				while (iter.hasNext()) {
					ICurriculum coursename = iter.next();
					if (givenCourseCurriculum.contains(coursename)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Method to check if there is a techaer holding a course in the same period
	 * 
	 * @param courses
	 *            Solution that should be analysed.
	 * @param givenCourse
	 *            Course that should be found.
	 * @param period
	 *            period in which that course should be
	 * @return true if in the same period is a course tought by the same teacher
	 *         as givenCourse
	 */
	private boolean existsSameTeacherInPeriod(ISolution courses,
			ICourse givenCourse, int period) {
		for (int i = 0; i < courses.getCoding()[period].length; i++) {
			if ((courses.getCoding()[period][i] != null)
					&& courses.getCoding()[period][i].getTeacher().equals(
							givenCourse.getTeacher())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Method to get a ICurriculum out of a Set<ICurriculum>
	 * 
	 * @param set
	 *            Set<ICurriculum>
	 * @param searchedOne
	 *            ICurriculum that should be searched for
	 * @return the found item
	 */
	public ICurriculum getCurriculumOutOfSet(Set<ICurriculum> set,
			ICurriculum searchedOne) {
		return getCurriculumOutOfSet(set, searchedOne.getId());
	}

	/**
	 * Method to get a ICurriculum out of a Set<ICurriculum> choosen by the ID
	 * of a ICurriculum
	 * 
	 * @param set
	 *            Set<ICurriculum>
	 * @param searchedOneId
	 *            ID of ICurriculum that should be searched for
	 * @return the found item
	 */
	public ICurriculum getCurriculumOutOfSet(Set<ICurriculum> set,
			String searchedOneId) {
		for (ICurriculum iCurriculum : set) {
			if (iCurriculum.getId().equals(searchedOneId)) {
				return iCurriculum;
			}
		}
		return null;
	}

}
