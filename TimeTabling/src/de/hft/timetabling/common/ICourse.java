package de.hft.timetabling.common;

import java.util.Set;

/**
 * An <tt>ICourse</tt> captures all the information provided by the competition
 * for a specific course.
 * 
 * @author Alexander Weickmann
 */
public interface ICourse {

	/**
	 * Returns the ID of the course.
	 */
	String getId();

	/**
	 * Returns the name of the teacher teaching this course.
	 */
	String getTeacher();

	/**
	 * Returns how often this course takes place in one week.
	 */
	int getNumberOfLectures();

	/**
	 * Returns the minimum number of working days this course should be spread
	 * on.
	 */
	int getMinWorkingDays();

	/**
	 * Returns how many students are attending this course in total. This
	 * information is important to know when a room must be assigned to the
	 * course so that there is enough space for all visitors.
	 */
	int getNumberOfStudents();

	/**
	 * Returns a set containing all curricula this course is part of.
	 */
	Set<ICurriculum> getCurricula();

	/**
	 * Returns the problem instance this course belongs to.
	 */
	IProblemInstance getProblemInstance();

}
