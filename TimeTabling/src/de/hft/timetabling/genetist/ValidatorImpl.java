package de.hft.timetabling.genetist;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.services.IValidatorService;

/**
 * This class is a utility class to check whether solutions are valid, i.e. that
 * the amount of hard constraint violations is zero.
 * 
 * A solution is valid if 1) No two courses from the same curriculum are in the
 * same period 2) A teacher is not assigned to two courses in the same period 3)
 * No unavailability constraints are violated 4) All courses are held the
 * specified amount of times
 * 
 * @author Matthias Ruszala
 */
public final class ValidatorImpl implements IValidatorService {

	/**
	 * Evaluates if a solution violates any hard constraints.
	 */
	public boolean isValidSolution(final ISolution sol) {
		ICourse[][] coding = sol.getCoding();

		return noUnavailabilityViolations(coding, sol.getProblemInstance())
				&& noCurriculaOverlap(coding) && noTeacherOverlap(coding)
				&& allCoursesHeld(coding, sol.getProblemInstance());
	}

	public boolean isValidSolution(final IProblemInstance instance,
			final ICourse[][] coding) {
		return noUnavailabilityViolations(coding, instance)
				&& noCurriculaOverlap(coding) && noTeacherOverlap(coding)
				&& allCoursesHeld(coding, instance);
	}

	/*
	 * Checks whether courses which belong to the same curriculum in the same
	 * period.
	 */
	private boolean noCurriculaOverlap(final ICourse[][] coding) {
		for (int i = 0; i < coding.length; i++) {
			boolean firstCourse = true;
			Set<ICurriculum> curriculaInPeriod = new HashSet<ICurriculum>();

			for (int j = 0; j < coding[i].length; j++) {
				ICourse course = coding[i][j];

				if (course != null) {
					if (firstCourse) {
						curriculaInPeriod.addAll(course.getCurricula());
						firstCourse = false;
					} else {
						Set<ICurriculum> intersection = new HashSet<ICurriculum>();
						intersection.addAll(curriculaInPeriod);
						intersection.retainAll(course.getCurricula());

						if (intersection.size() > 0) {
							return false;
						}

					}
				}
			}
		}
		return true;
	}

	/*
	 * Checks whether a techer gives more than one lecture in the same period.
	 */
	private boolean noTeacherOverlap(final ICourse[][] coding) {
		Set<String> teachersInPeriod;

		for (int i = 0; i < coding.length; i++) {
			teachersInPeriod = new HashSet<String>();

			for (int j = 0; j < coding[i].length; j++) {
				ICourse course = coding[i][j];

				if (course != null) {
					if (teachersInPeriod.contains(course.getTeacher())) {
						return false;
					}

					teachersInPeriod.add(course.getTeacher());
				}
			}

		}

		return true;
	}

	/*
	 * Checks whether all courses are held the designated amount of times.
	 */
	private boolean allCoursesHeld(final ICourse[][] coding,
			final IProblemInstance inst) {
		Map<ICourse, Integer> courseCount = new HashMap<ICourse, Integer>();

		for (int i = 0; i < coding.length; i++) {
			for (int j = 0; j < coding[i].length; j++) {
				ICourse course = coding[i][j];

				if (course != null) {
					/*
					 * Check if map contains key for this course. If true
					 * increment otherwise create one with start value
					 */
					if (courseCount.keySet().contains(course)) {
						int count = courseCount.get(course);
						count++;
						courseCount.put(course, count);
					} else {
						courseCount.put(course, 1);
					}
				}
			}
		}

		/*
		 * Check if all courses are assigned
		 */
		Set<ICourse> difference = new HashSet<ICourse>();
		difference.addAll(inst.getCourses());
		difference.removeAll(courseCount.keySet());

		if (difference.size() > 0) {
			return false;
		}

		/*
		 * Check if all courses are given at least the specified amount of times
		 */
		for (ICourse course : inst.getCourses()) {
			if (courseCount.get(course) < course.getNumberOfLectures()) {
				return false;
			}
		}

		return true;
	}

	/*
	 * Checks whether there is an assignment of a course to a period which
	 * violates unavailability violations.
	 */
	private boolean noUnavailabilityViolations(final ICourse[][] coding,
			final IProblemInstance inst) {

		for (int i = 0; i < coding.length; i++) {
			for (int j = 0; j < coding[i].length; j++) {
				ICourse course = coding[i][j];

				if ((course != null)
						&& inst.getUnavailabilityConstraints(course)
								.contains(i)) {

					return false;
				}
			}
		}
		return true;
	}
}
