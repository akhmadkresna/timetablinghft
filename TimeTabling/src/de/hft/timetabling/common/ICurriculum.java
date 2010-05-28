package de.hft.timetabling.common;

import java.util.Set;

/**
 * An <tt>ICurriculum</tt> captures all the information provided by the
 * competition for a specific curriculum.
 * 
 * @author Alexander Weickmann
 */
public interface ICurriculum {

	/**
	 * Returns the ID of the curriculum.
	 */
	String getId();

	/**
	 * Returns the number how many courses this curriculum is made of.
	 */
	int getNumberOfCourses();

	/**
	 * Returns a set of all the courses making up this curriculum.
	 */
	Set<ICourse> getCourses();

	/**
	 * Returns whether the given course is part of this curriculum.
	 * 
	 * @param course
	 *            The course to check whether it is part of this curriculum.
	 */
	boolean containsCourse(ICourse course);

	/**
	 * Returns the problem instance this curriculum belongs to.
	 */
	IProblemInstance getProblemInstance();

}
