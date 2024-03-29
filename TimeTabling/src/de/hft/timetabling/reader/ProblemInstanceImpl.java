package de.hft.timetabling.reader;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.IRoom;

/**
 * Immutable implementation of the {@link IProblemInstance} interface.
 * 
 * @author Alexander Weickmann
 * 
 * @see IProblemInstance
 */
final class ProblemInstanceImpl implements IProblemInstance {

	private final String fileName;

	private final String name;

	private final int numberOfConstraints;

	private final int numberOfCourses;

	private final int numberOfCurricula;

	private final int numberOfDays;

	private final int numberOfRooms;

	private final int periodsPerDay;

	private final Set<ICourse> courses;

	private final Set<IRoom> rooms;

	private final Set<ICurriculum> curricula;

	private final Map<ICourse, Set<Integer>> unavailabilityConstraints;

	private final Map<String, Set<ICourse>> coursesByTeacher;

	private final Map<String, IRoom> roomsById;

	private final Map<String, ICourse> coursesById;

	public ProblemInstanceImpl(final String fileName, final String name,
			final int numberOfCourses, final int numberOfRooms,
			final int numberOfDays, final int periodsPerDay,
			final int numberOfCurricula, final int numberOfConstraints) {

		this.fileName = fileName;
		this.name = name;
		this.numberOfCourses = numberOfCourses;
		this.numberOfRooms = numberOfRooms;
		this.numberOfDays = numberOfDays;
		this.periodsPerDay = periodsPerDay;
		this.numberOfCurricula = numberOfCurricula;
		this.numberOfConstraints = numberOfConstraints;

		courses = new LinkedHashSet<ICourse>();
		rooms = new LinkedHashSet<IRoom>();
		curricula = new LinkedHashSet<ICurriculum>();
		unavailabilityConstraints = new HashMap<ICourse, Set<Integer>>();
		coursesByTeacher = new HashMap<String, Set<ICourse>>();
		roomsById = new HashMap<String, IRoom>();
		coursesById = new HashMap<String, ICourse>();
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getNumberOfConstraints() {
		return numberOfConstraints;
	}

	@Override
	public int getNumberOfCourses() {
		return numberOfCourses;
	}

	@Override
	public int getNumberOfCurricula() {
		return numberOfCurricula;
	}

	@Override
	public int getNumberOfDays() {
		return numberOfDays;
	}

	@Override
	public int getNumberOfRooms() {
		return numberOfRooms;
	}

	@Override
	public int getPeriodsPerDay() {
		return periodsPerDay;
	}

	void addCourse(final ICourse course) {
		courses.add(course);
		coursesById.put(course.getId(), course);
	}

	void addRoom(final IRoom room) {
		rooms.add(room);
		roomsById.put(room.getId(), room);
	}

	void addCurriculum(final ICurriculum curriculum) {
		curricula.add(curriculum);
	}

	void addUnavailabilityConstraint(final ICourse course, final int period) {
		Set<Integer> periodsForCourse = unavailabilityConstraints.get(course);
		if (periodsForCourse == null) {
			periodsForCourse = new LinkedHashSet<Integer>();
		}
		periodsForCourse.add(period);
		unavailabilityConstraints.put(course, periodsForCourse);
	}

	@Override
	public Set<ICourse> getCourses() {
		return Collections.unmodifiableSet(courses);
	}

	@Override
	public Set<ICurriculum> getCurricula() {
		return Collections.unmodifiableSet(curricula);
	}

	@Override
	public Set<IRoom> getRooms() {
		return Collections.unmodifiableSet(rooms);
	}

	@Override
	public Set<Integer> getUnavailabilityConstraints(final ICourse course) {
		Set<Integer> constraints = unavailabilityConstraints.get(course);
		if (constraints == null) {
			constraints = new LinkedHashSet<Integer>(0);
		}
		return Collections.unmodifiableSet(constraints);
	}

	@Override
	public ICourse getCourseById(final String courseId) {
		return coursesById.get(courseId);
	}

	@Override
	public IRoom getRoomByUniqueNumber(final int uniqueRoomNumber) {
		for (final IRoom room : rooms) {
			if (room.getUniqueNumber() == uniqueRoomNumber) {
				return room;
			}
		}
		return null;
	}

	@Override
	public int getNumberOfPeriods() {
		return numberOfDays * periodsPerDay;
	}

	@Override
	public String toString() {
		return "Problem Instance: " + name;
	}

	@Override
	public Set<ICourse> getCoursesForTeacher(final String teacher) {
		Set<ICourse> teachersCourses = coursesByTeacher.get(teacher);

		if (teachersCourses == null) {
			teachersCourses = new HashSet<ICourse>();

			for (final ICourse course : courses) {
				if (course.getTeacher().equals(teacher)) {
					teachersCourses.add(course);
				}
			}
			coursesByTeacher.put(teacher, teachersCourses);
		}

		return Collections.unmodifiableSet(teachersCourses);
	}

	@Override
	public IRoom getRoomById(final String roomId) {
		return roomsById.get(roomId);
	}

}
