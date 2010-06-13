package de.hft.timetabling.generator;

import java.util.Set;

import de.hft.timetabling.common.ICourse;

/**
 * This interface is used during construction of a feasible solution. It stores
 * all necessary information needed during the construction of *one* feasible
 * solution and offers methods required by the construction algorithm. Objects
 * of this class are discarded after each failed attempt to construct a feasible
 * solution.
 * 
 * @author Matthias Ruszala
 */
public interface GeneratorAlgorithm {

	/**
	 * This method takes a subset of unassigned courses and calculates the
	 * course which has the least amount of viable slots which it can be
	 * assigned to without violating any hard constraints. This course is called
	 * the most critical course. Note that there can be more than one courses
	 * which are critical, i.e. which have the same amount of available slots.
	 * 
	 * @param courses
	 *            the (sub)set of unassigned courses
	 * @return the course with the least available viable slots in the current
	 *         schedule
	 */
	ICourse getMostCriticalEvent(Set<ICourse> unassignedCourses);

	/**
	 * This method checks whether there are enough viable slots in the schedule
	 * w.r.t. the number of lectures which are associated with that course.
	 * 
	 * @param the
	 *            course to be checked
	 * @return true if and only if the number of available viable slots is
	 *         equals or higher than the number of lectures, false otherwise
	 */
	boolean isAssignable(ICourse course);

	/**
	 * This method assigns random slots in the schedule to the given course with
	 * respect to the minimal number of lectures for the course. This method
	 * does not check whether there are sufficiently free periods available.
	 * 
	 * @param course
	 *            the course which will be assigned to slots
	 */
	public void assignRandomViableSlots(ICourse course);

	/**
	 * This method returns the two-dimensional array representing the current
	 * assignments of courses to periods and rooms. This method does not check
	 * whether the schedule is valid.
	 * 
	 * @return the coded schedule
	 */
	ICourse[][] getCoding();

}
