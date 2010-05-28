package de.hft.timetabling.reader;

import java.util.Collections;
import java.util.HashMap;
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

	public ProblemInstanceImpl(String fileName, String name,
			int numberOfCourses, int numberOfRooms, int numberOfDays,
			int periodsPerDay, int numberOfCurricula, int numberOfConstraints) {

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

	void addCourse(ICourse course) {
		courses.add(course);
	}

	void addRoom(IRoom room) {
		rooms.add(room);
	}

	void addCurriculum(ICurriculum curriculum) {
		curricula.add(curriculum);
	}

	void addUnavailabilityConstraint(ICourse course, int period) {
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
	public Set<Integer> getUnavailabilityConstraints(ICourse course) {
		Set<Integer> constraints = unavailabilityConstraints.get(course);
		if (constraints == null) {
			constraints = new LinkedHashSet<Integer>(0);
		}
		return Collections.unmodifiableSet(constraints);
	}

	@Override
	public ICourse getCourseById(String courseId) {
		for (ICourse course : courses) {
			if (course.getId().equals(courseId)) {
				return course;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "Problem Instance: " + name;
	}

}
