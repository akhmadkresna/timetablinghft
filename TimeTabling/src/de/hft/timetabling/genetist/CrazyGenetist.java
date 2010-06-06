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
	 * Propability to chose one of the recombination algorithms.
	 */
	private double probability = 0.5;

	/**
	 * public Method to start recombination and mutation process. The solution
	 * table will get from serviceLocator.getSolutionTableService(). The
	 * Solutions that are recombined and mutated are choosen randomly.
	 */
	@Override
	public void recombineAndMutate() {

		ServiceLocator serviceLocator = ServiceLocator.getInstance();

		setSolution(serviceLocator.getSolutionTableService());

		// Solution that is given back afterwards.
		ISolution back = null;

		if (solution.getActualSolutionTableCount() > 1) {
			// Solution that is used to pull values out of.
			ISolution otherSolution = null;
			// Solution that is used as basic for recombination
			ISolution basicSolution = null;
			// Choosing solutions that until they are not null and different
			while ((otherSolution == null) || (basicSolution == null)
					|| basicSolution.equals(otherSolution)) {
				System.out.println("Trying to find solution to work with ...");

				// --> if (Math.random() < probability)

				int n1 = (int) (solution.getActualSolutionTableCount() * Math
						.random());
				int n2 = (int) (solution.getActualSolutionTableCount() * Math
						.random());
				basicSolution = solution.getSolution(n1);
				otherSolution = solution.getSolution(n2);

				System.out.println("... found " + n1 + ", " + n2 + " ...");
			}
			System.out.println(" ... take it!");

			back = mutateRoomStability(recombindation2(basicSolution,
					otherSolution));

			basicSolution.increaseRecombinationCount();
			otherSolution.increaseRecombinationCount();

		}
		System.out.println("Crazy Genetist is done.");

		// Hand in solution
		if ((back != null) && new ValidatorImpl().isValidSolution(back)) {
			System.out.println("### Solution valid!!");
			getSolution().replaceWorstSolution(back);
		} else {
			System.out.println("### Solution not valid!!");
		}
	}

	/**
	 * Method to set Solution that should be used.
	 * 
	 * @param solution
	 *            Solution table that should be recomined and mutated.
	 */
	public void setSolution(ISolutionTableService solution) {
		this.solution = solution;
	}

	/**
	 * Method to get Solution that should be recombined.
	 * 
	 * @return solution Solution table that should be recomined and mutated.
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
		System.out.println("Starting mutation ...");
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

		System.out.println("... mutate ...");
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
		System.out.println("... mutation done.");
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
		System.out.println("Start recombnination 2 process ...");
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

		System.out.println("... done with recombindation 2 process. ("
				+ !checkForNullCourse(newSolution) + ")");
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
	 * Method for getting the set probability of what algorithm should be
	 * chosen.
	 * 
	 * @return probability of algorithm choosing.
	 */
	public double getProbability() {
		return probability;
	}

	/**
	 * Method for setting the probability of what algorithm should be chosen.
	 * 
	 * @param probability
	 *            of algorithm choosing.
	 */
	public void setProbability(double probability) {
		this.probability = probability;
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

	/**
	 * Method to check if one ISolution only contains of null elements
	 * 
	 * @param solution
	 *            ISolution that should be analysed.
	 * @return True if the whole timetable only consists of null elements.
	 */
	private boolean checkForNullCourse(ISolution solution) {
		ICourse[][] course = solution.getCoding();
		for (int i = 0; i < course.length; i++) {
			for (int j = 0; j < course[i].length; j++) {
				if (course[i][j] != null) {
					return false;
				}
			}
		}
		return true;
	}

}
