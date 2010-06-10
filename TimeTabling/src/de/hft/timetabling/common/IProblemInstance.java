package de.hft.timetabling.common;

import java.util.Set;

/**
 * A problem instance contains all the information provided by the competition
 * for a certain time tabling problem.
 * 
 * @author Alexander Weickmann
 */
public interface IProblemInstance {

	/**
	 * Returns the file name of the input file.
	 */
	String getFileName();

	/**
	 * Returns the name of the problem.
	 */
	String getName();

	/**
	 * Returns how many unavailability constraints are contained in this
	 * problem.
	 */
	int getNumberOfConstraints();

	/**
	 * Returns how many different courses are contained in this problem.
	 */
	int getNumberOfCourses();

	/**
	 * Returns how many curricula are contained in this problem.
	 */
	int getNumberOfCurricula();

	/**
	 * Returns how many days of the week are used in this problem.
	 */
	int getNumberOfDays();

	/**
	 * Returns how many rooms are available in this problem.
	 */
	int getNumberOfRooms();

	/**
	 * Returns how many periods one day has in this problem.
	 */
	int getPeriodsPerDay();

	/**
	 * Returns all the courses that are contained in this problem.
	 */
	Set<ICourse> getCourses();

	/**
	 * Returns all the rooms that are contained in this problem.
	 */
	Set<IRoom> getRooms();

	/**
	 * Returns all the curricula that are contained in this problem.
	 */
	Set<ICurriculum> getCurricula();

	/**
	 * Returns the course identified by the given course ID or <tt>null</tt> if
	 * there is no course with the given ID.
	 * 
	 * @param courseId
	 *            The ID identifying the course to retrieve.
	 */
	ICourse getCourseById(String courseId);

	/**
	 * Returns the set of unavailability constraints for the given course. If a
	 * course does not have any unavailability constraints then an empty list
	 * will be returned.
	 * 
	 * @param course
	 *            The course to retrieve the unavailability constraints for.
	 */
	Set<Integer> getUnavailabilityConstraints(ICourse course);

	/**
	 * Returns the room with the given unique room number or <tt>null</tt> if no
	 * room with the given unique room number exists.
	 * 
	 * @param uniqueRoomNumber
	 *            The searched room's unique number.
	 */
	IRoom getRoomByUniqueNumber(int uniqueRoomNumber);

	/**
	 * Returns how many periods this problem instance spans. This number is a
	 * shortcut for <tt>getNumberOfDays() * getPeriodsPerDay()</tt>.
	 */
	int getNumberOfPeriods();

	/**
	 * Returns the set of all courses which a teacher gives.
	 * 
	 * @param The
	 *            teacher which
	 * @return the courses held by the teacher
	 */
	Set<ICourse> getCoursesForTeacher(String teacher);

	IRoom getRoomById(String roomId);

}
