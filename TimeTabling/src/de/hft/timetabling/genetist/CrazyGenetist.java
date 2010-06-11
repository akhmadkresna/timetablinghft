package de.hft.timetabling.genetist;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.services.ICrazyGenetistService;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.IValidatorService;
import de.hft.timetabling.services.ServiceLocator;

/**
 * The CrazyGenetist class is for recombinating and mutating a solution table.
 * 
 * @author Steffen
 * @author Sotiris
 * 
 */
public class CrazyGenetist implements ICrazyGenetistService {

	public static int success = 0;
	public static int failure = 0;

	/**
	 * Iterations to chose one of the recombination algorithms. This number
	 * means the percentage of the maximum table size.
	 */
	private static final int RECOMBINE_PERCENTAGE = 50;

	/**
	 * public Method to start recombination and mutation process. The solution
	 * table will get from serviceLocator.getSolutionTableService(). The
	 * Solutions that are recombined and mutated are chosen randomly.
	 */
	@Override
	public void recombineAndMutate() {
		ISolutionTableService solutionTable = ServiceLocator.getInstance()
				.getSolutionTableService();

		if (solutionTable.getSize(false) <= 1) {
			return;
		}

		// Choosing solutions that until they are not null and different
		int iterationRounds = (RECOMBINE_PERCENTAGE * ISolutionTableService.TABLE_SIZE) / 100;

		System.out.print("CRAZY GENETIST: Starting to create "
				+ iterationRounds + " children (" + RECOMBINE_PERCENTAGE
				+ "%) ...");

		// The solutions ordered by rank (low to high, highest rank is best)
		ISolution[] rankedSolutions = new ISolution[solutionTable
				.getSize(false)];
		int solutionIndex = 0;
		for (int i = rankedSolutions.length - 1; i >= 0; i--) {
			rankedSolutions[i] = solutionTable.getSolution(solutionIndex);
			solutionIndex++;
		}

		// Compute slot sum
		int slotSum = 0;
		for (int i = 1; i <= rankedSolutions.length; i++) {
			slotSum += i;
		}

		int handedInSolutions = 0;
		for (int i = 0; handedInSolutions < iterationRounds; i++) {

			// Solution that is used to pull values out of.
			ISolution otherSolution = null;
			// Solution that is used as a basis for recombination
			ISolution basisSolution = null;

			while ((otherSolution == null) || (basisSolution == null)
					|| basisSolution.equals(otherSolution)) {

				Random random = new Random();
				int selectedSlot1 = random.nextInt(slotSum) + 1;
				int selectedSlot2 = random.nextInt(slotSum) + 1;
				int rank1 = slotToRank(selectedSlot1, solutionTable
						.getSize(false), slotSum);
				int rank2 = slotToRank(selectedSlot2, solutionTable
						.getSize(false), slotSum);
				basisSolution = rankedSolutions[rank1 - 1];
				otherSolution = rankedSolutions[rank2 - 1];
			}

			ISolution recombinedSolution = recombination2(basisSolution,
					otherSolution);

			recombinedSolution = mutateRoomStability(recombinedSolution);
			if (Math.random() <= 0.05) {
				recombinedSolution = mutateCourseIsolation(recombinedSolution);
			}

			basisSolution.increaseRecombinationCount();
			otherSolution.increaseRecombinationCount();

			// Hand in solution
			IValidatorService validatorService = ServiceLocator.getInstance()
					.getValidatorService();
			boolean validSolution = validatorService
					.isValidSolution(recombinedSolution);
			if ((recombinedSolution != null) && validSolution) {
				success++;
				solutionTable.removeWorstSolution();
				solutionTable.addSolution(recombinedSolution);
				handedInSolutions++;

			} else {
				System.out.println("CRAZY GENETIST: No valid solution found.");
				failure++;
				break;
			}
		}

		System.out.print(" done.\n");
	}

	/**
	 * Returns the rank that the given slot is assigned to.
	 * 
	 * @param slot
	 *            A rank consists of as many slots as the rank number is. For
	 *            example, rank 100 has 100 slots.
	 * @param nrSolutions
	 *            The number of solutions in total.
	 * @param slotSum
	 *            The sum of all slots.
	 */
	private int slotToRank(int slot, int nrSolutions, int slotSum) {
		int rank = 1;
		int currentSlotSum = 1;
		while (rank <= nrSolutions) {
			if (slot <= currentSlotSum) {
				break;
			}
			rank++;
			currentSlotSum += rank + 1;
		}
		return rank;
	}

	/**
	 * Mutation algorithm to mutate course stability.
	 * 
	 * @param solution
	 *            solution to mutate
	 * @return mutated solution
	 */
	private ISolution mutateCourseIsolation(ISolution solution) {
		ISolution newSolution = solution.clone();

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
								// No check neccessary because we only changed
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
	private ISolution recombination2(ISolution solution1, ISolution solution2) {
		ISolution newSolution = solution1.clone();

		for (int i = 0; i < newSolution.getCoding().length; i++) {
			for (int j = 0; j < newSolution.getCoding()[i].length; j++) {

				// Fill gap
				if ((newSolution.getCoding()[i][j] == null)
						&& (solution2.getCoding()[i][j] != null)) {

					boolean sameCurriculumInPeriod = existsSameCurriculumInPeriod(
							newSolution, solution2.getCoding()[i][j], i);
					boolean sameTeacherInPeriod = existsSameTeacherInPeriod(
							newSolution, solution2.getCoding()[i][j], i);

					if (!(sameCurriculumInPeriod) && !(sameTeacherInPeriod)) {
						CoursePosition cp1 = getCoursePositionRandomly(getPositionOfCourse(
								newSolution, solution2.getCoding()[i][j]));
						if (cp1 != null) {
							newSolution.getCoding()[cp1.getX()][cp1.getY()] = null;
							newSolution.getCoding()[i][j] = solution2
									.getCoding()[i][j];
						}

					} else if (sameCurriculumInPeriod && sameTeacherInPeriod) {
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
		if (set.size() == 0) {
			return null;
		}
		Random random = new Random();
		int n = random.nextInt(set.size());
		return set.toArray(new CoursePosition[set.size()])[n];
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
