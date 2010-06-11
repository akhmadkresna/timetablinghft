package de.hft.timetabling.genetist;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.ISolution;

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

	@Override
	public ISolution recombine(ISolution solution1, ISolution solution2) {
		ISolution newSolution = solution1.clone();

		for (int i = 0; i < newSolution.getCoding().length; i++) {
			for (int j = 0; j < newSolution.getCoding()[i].length; j++) {

				// Fill gap
				if ((newSolution.getCoding()[i][j] == null)
						&& (solution2.getCoding()[i][j] != null)) {

					boolean sameCurriculumInPeriod = existsCurriculaInPeriod(
							newSolution.getCoding(),
							solution2.getCoding()[i][j].getCurricula(), i);
					boolean sameTeacherInPeriod = existsTeacherInPeriod(
							newSolution.getCoding(),
							solution2.getCoding()[i][j].getTeacher(), i);

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
	 *            Solution that should be analyzed.
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

	@Override
	public String getName() {
		return "Neighborhood v1";
	}

}
