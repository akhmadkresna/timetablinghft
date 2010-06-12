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
 * same period 2) No teacher is assigned to more than one course in the same
 * period 3) No unavailability constraints are violated 4) All courses are held
 * the specified amount of times
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

	public boolean isValidSolution(final IProblemInstance problemInstance,
			final ICourse[][] coding) {
		/*
		 * Calling the individual functions this way will enable premature
		 * abortion of the check if result can be safely determined.
		 */
		return noUnavailabilityViolations(coding, problemInstance)
				&& noCurriculaOverlap(coding) && noTeacherOverlap(coding)
				&& allCoursesHeld(coding, problemInstance);
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
				final ICourse course = coding[i][j];

				if (course != null) {
					if (curriculaInPeriod.isEmpty()) {
						curriculaInPeriod.addAll(course.getCurricula());
					} else {
						for (final ICurriculum curriculum : course
								.getCurricula()) {
							if (curriculaInPeriod.contains(curriculum)) {
								System.out
										.println("CHECK:--noCurriculaOverlap("
												+ course.getId() + ")");
								return false;
							}
							curriculaInPeriod.add(curriculum);
						}
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
				final ICourse course = coding[period][room];

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
		if (!courseCount.keySet().containsAll(inst.getCourses())) {
			System.out.println("CHECK:---allCoursesHeld1");
			return false;
		}

		/*
		 * Check if all courses are given at least the specified amount of times
		 */
		for (final ICourse course : inst.getCourses()) {

			if (courseCount.get(course) != course.getNumberOfLectures()) {
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
				final ICourse course = coding[i][j];

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
