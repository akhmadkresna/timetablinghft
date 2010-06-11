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
		return isValidSolution(sol.getProblemInstance(), sol.getCoding());
	}

	public boolean isValidSolution(final IProblemInstance instance,
			final ICourse[][] coding) {

		boolean noUnavailabilityViolations = noUnavailabilityViolations(coding,
				instance);
		boolean noCurriculaOverlap = noCurriculaOverlap(coding);
		boolean noTeacherOverlap = noTeacherOverlap(coding);
		boolean allCoursesHeld = allCoursesHeld(coding, instance);

		return noUnavailabilityViolations && noCurriculaOverlap
				&& noTeacherOverlap && allCoursesHeld;
	}

	/**
	 * Checks whether courses which belong to the same curriculum in the same
	 * period.
	 */
	private boolean noCurriculaOverlap(final ICourse[][] coding) {

		final Set<ICurriculum> curriculaInPeriod = new HashSet<ICurriculum>();

		for (int i = 0; i < coding.length; i++) {

			curriculaInPeriod.clear();

			for (int j = 0; j < coding[i].length; j++) {
				ICourse course = coding[i][j];

				if (course != null) {
					if (curriculaInPeriod.isEmpty()) {
						curriculaInPeriod.addAll(course.getCurricula());
					} else {
						Set<ICurriculum> intersection = new HashSet<ICurriculum>();
						intersection.addAll(curriculaInPeriod);
						intersection.retainAll(course.getCurricula());

						if (intersection.size() > 0) {
							System.out.println("CHECK:--noCurriculaOverlap("
									+ course.getId() + ")");
							return false;
						}

						curriculaInPeriod.addAll(course.getCurricula());
					}
				}
			}
		}
		return true;
	}

	/**
	 * Checks whether a teacher gives more than one lecture in the same period.
	 */
	private boolean noTeacherOverlap(final ICourse[][] coding) {

		Set<String> teachersInPeriod = new HashSet<String>();

		for (int i = 0; i < coding.length; i++) {

			teachersInPeriod.clear();

			for (int j = 0; j < coding[i].length; j++) {
				ICourse course = coding[i][j];

				if (course != null) {
					if (teachersInPeriod.contains(course.getTeacher())) {
						System.out.println("CHECK:---noTeacherOverlap("
								+ course.getId() + ")");
						return false;
					}

					teachersInPeriod.add(course.getTeacher());
				}
			}

		}

		return true;
	}

	/**
	 * Checks whether all courses are held the designated amount of times.
	 */
	private boolean allCoursesHeld(final ICourse[][] coding,
			final IProblemInstance inst) {
		Map<ICourse, Integer> courseCount = new HashMap<ICourse, Integer>();

		for (int period = 0; period < coding.length; period++) {
			for (int room = 0; room < coding[period].length; room++) {

				ICourse course = coding[period][room];

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
		 * Check if all courses are assigned at least once
		 */
		Set<ICourse> difference = new HashSet<ICourse>();
		difference.addAll(inst.getCourses());
		difference.removeAll(courseCount.keySet());

		if (difference.size() > 0) {
			System.out.println("CHECK:---allCoursesHeld1(" + difference.size()
					+ " > 0)");
			return false;
		}

		/*
		 * Check if all courses are given at least the specified amount of times
		 */
		for (ICourse course : inst.getCourses()) {
			if (courseCount.get(course) < course.getNumberOfLectures()) {
				System.out.println("CHECK:---allCoursesHeld2(" + course.getId()
						+ " " + courseCount.get(course) + " / "
						+ course.getNumberOfLectures() + ")");
				return false;
			}
		}

		return true;
	}

	/**
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
					System.out.println("CHECK:---noUnavailabilityViolations"
							+ course.getId() + ")");
					return false;
				}
			}
		}
		return true;
	}

}
