package de.hft.timetabling.genetist;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.ServiceLocator;
import de.hft.timetabling.util.HardConstraintUtil;

/**
 * Recombination strategy from Sotiris and Steffen. This recombination strategy
 * tries to adapt the neighborhood N1 mutation strategy (see paper in doc
 * folder) in order to perform recombination.
 * 
 * @author Steffen
 * @author Sotiris
 */
public final class NeighborhoodRecombinationStrategy extends
		RecombinationStrategy {

	/** Value between 0 and 100. */
	private static final int RECOMBINATION_PERCENTAGE = 10;

	private static final int SOLUTION_TABLE_SIZE = 100;

	@Override
	public ISolution recombine(ISolution solution1, ISolution solution2) {
		ISolution newSolution = getSolutionTable().createNewSolution(
				solution1.getCoding(), solution1.getProblemInstance());

		for (int i = 0; i < newSolution.getCoding().length; i++) {
			for (int j = 0; j < newSolution.getCoding()[i].length; j++) {

				// Fill gap
				if ((newSolution.getCoding()[i][j] == null)
						&& (solution2.getCoding()[i][j] != null)) {

					boolean sameCurriculumInPeriod = HardConstraintUtil
							.existsCurriculaInPeriod(newSolution.getCoding(),
									solution2.getCoding()[i][j].getCurricula(),
									i);
					boolean sameTeacherInPeriod = HardConstraintUtil
							.existsTeacherInPeriod(newSolution.getCoding(),
									solution2.getCoding()[i][j].getTeacher(), i);

					if (!(sameCurriculumInPeriod) && !(sameTeacherInPeriod)) {
						Lecture cp1 = getCoursePositionRandomly(getPositionOfCourse(
								newSolution, solution2.getCoding()[i][j]));
						if (cp1 != null) {
							newSolution.getCoding()[cp1.getSlot().getPeriod()][cp1
									.getSlot().getRoom()] = null;
							newSolution.getCoding()[i][j] = solution2
									.getCoding()[i][j];
						}

					} else if (sameCurriculumInPeriod && sameTeacherInPeriod) {
						Lecture cp1 = getCoursePositionRandomly(getPositionOfCourse(
								newSolution, solution2.getCoding()[i][j]));
						Lecture cp2 = getIfSameCurriculumAndSameTeacher(
								newSolution, solution2.getCoding()[i][j], i);

						if (cp2 != null) {
							newSolution.getCoding()[cp1.getSlot().getPeriod()][cp1
									.getSlot().getRoom()] = newSolution
									.getCoding()[cp2.getSlot().getPeriod()][cp2
									.getSlot().getRoom()];

							newSolution.getCoding()[i][j] = solution2
									.getCoding()[i][j];
							newSolution.getCoding()[cp2.getSlot().getPeriod()][cp2
									.getSlot().getRoom()] = null;
						}
					}
				}
			}
		}

		return newSolution;
	}

	@Override
	protected void reset() {
		// Nothing to do.
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
	private Set<Lecture> getPositionOfCourse(ISolution courses, ICourse course) {
		Set<Lecture> positions = new HashSet<Lecture>();
		for (int i = 0; i < courses.getCoding().length; i++) {
			for (int j = 0; j < courses.getCoding()[i].length; j++) {
				if (courses.getCoding()[i][j] != null) {
					if (courses.getCoding()[i][j].getId()
							.equals(course.getId())) {
						positions.add(new Lecture(course, i, j));
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
	private Lecture getCoursePositionRandomly(Set<Lecture> set) {
		if (set.size() == 0) {
			return null;
		}
		Random random = new Random();
		int n = random.nextInt(set.size());
		return set.toArray(new Lecture[set.size()])[n];
	}

	/**
	 * Method return a Course position of a course that can be found in a
	 * specific period and has the same curriculum and teacher as the input
	 * course
	 * 
	 * @param courses
	 *            Solution that should be analyzed.
	 * @param givenCourse
	 *            Course that should be found.
	 * @param period
	 *            period in which that course should be
	 * @return Position of found course
	 */
	private Lecture getIfSameCurriculumAndSameTeacher(ISolution courses,
			ICourse givenCourse, int period) {
		for (int i = 0; i < courses.getCoding()[period].length; i++) {
			if ((courses.getCoding()[period][i] != null)
					&& courses.getCoding()[period][i].getTeacher().equals(
							givenCourse.getTeacher())) {
				Set<ICurriculum> tmpCur = courses.getCoding()[period][i]
						.getCurricula();

				if ((tmpCur.size() == givenCourse.getCurricula().size())
						&& tmpCur.containsAll(givenCourse.getCurricula())) {
					return new Lecture(givenCourse, period, i);
				}
			}
		}
		return null;
	}

	@Override
	protected ISolution mutate(ISolution recombinedSolution) {
		recombinedSolution = MutationOperators
				.mutateRoomStability(recombinedSolution);
		recombinedSolution = MutationOperators
				.mutateCourseIsolation(recombinedSolution);
		return recombinedSolution;
	}

	@Override
	protected void configure() {
		ISolutionTableService solutionTable = ServiceLocator.getInstance()
				.getSolutionTableService();
		solutionTable.setMaximumSize(SOLUTION_TABLE_SIZE);
	}

	@Override
	public int getRecombinationPercentage() {
		return RECOMBINATION_PERCENTAGE;
	}

	@Override
	protected void eliminate(ISolution parent1, ISolution parent2,
			Set<ISolution> eliminatedSolutions) {
		ISolutionTableService solutionTable = ServiceLocator.getInstance()
				.getSolutionTableService();
		ISolution worstSolution = solutionTable.removeWorstSolution();
		eliminatedSolutions.add(worstSolution);
	}

	@Override
	protected void newInterationStarted(int interation, int totalIterations) {
		// Nothing to do
	}

	@Override
	public String getName() {
		return "Neighborhood v1";
	}

}
