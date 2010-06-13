package de.hft.timetabling.util;

import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IProblemInstance;

/**
 * Provides different utility methods to check for hard constraint violations.
 * 
 * @author Alexander Weickmann
 */
public final class HardConstraintUtil {

	/**
	 * Checks whether the given teacher is already holding a course in the given
	 * period (<tt>true</tt>) or not (<tt>false</tt>).
	 * 
	 * @param coding
	 *            The coding that should be analyzed.
	 * @param teacher
	 *            The teacher to search for.
	 * @param period
	 *            The period (period-only format) to be inspected.
	 */
	public static boolean existsTeacherInPeriod(ICourse[][] coding,
			String teacher, int period) {

		for (int room = 0; room < coding[period].length; room++) {
			if (coding[period][room] == null) {
				continue;
			}
			if (coding[period][room].getTeacher().equals(teacher)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether one of the given curricula has already courses in the
	 * given period (<tt>true</tt>) or not (<tt>false</tt>).
	 * 
	 * @param coding
	 *            The coding that should be analyzed.
	 * @param curricula
	 *            The curriculua to search for.
	 * @param period
	 *            The period (period-only format) to be inspected.
	 */
	public static boolean existsCurriculaInPeriod(ICourse[][] coding,
			Set<ICurriculum> curricula, int period) {

		for (int room = 0; room < coding[period].length; room++) {
			if (coding[period][room] == null) {
				continue;
			}
			for (ICurriculum curriculum : coding[period][room].getCurricula()) {
				if (curricula.contains(curriculum)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks whether there exists an unavailability constraint for the given
	 * course in the given period (<tt>true</tt>) or not (<tt>false</tt>).
	 * 
	 * @param course
	 *            The course to look for unavailability constraints.
	 * @param period
	 *            The period (period-only format) to look for unavailability
	 *            constraints.
	 */
	public static boolean existsUnavailabilityConstraint(ICourse course,
			int period) {

		IProblemInstance instance = course.getProblemInstance();
		return instance.getUnavailabilityConstraints(course).contains(period);
	}

}
