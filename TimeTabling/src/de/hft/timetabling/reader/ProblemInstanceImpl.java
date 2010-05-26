package de.hft.timetabling.reader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.IRoom;

final class ProblemInstanceImpl implements IProblemInstance {

	private String name;

	private int numberOfConstraints;

	private int numberOfCourses;

	private int numberOfCurricula;

	private int numberOfDays;

	private int numberOfRooms;

	private int periodsPerDay;

	private final List<ICourse> courses;

	private final List<IRoom> rooms;

	private final List<ICurriculum> curricula;

	private final Map<ICourse, List<Integer>> unavailabilityConstraints;

	public ProblemInstanceImpl() {
		courses = new ArrayList<ICourse>();
		rooms = new ArrayList<IRoom>();
		curricula = new ArrayList<ICurriculum>();
		unavailabilityConstraints = new HashMap<ICourse, List<Integer>>();
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

	void setName(String name) {
		this.name = name;
	}

	void setNumberOfConstraints(int numberOfConstraints) {
		this.numberOfConstraints = numberOfConstraints;
	}

	void setNumberOfCourses(int numberOfCourses) {
		this.numberOfCourses = numberOfCourses;
	}

	void setNumberOfCurricula(int numberOfCurricula) {
		this.numberOfCurricula = numberOfCurricula;
	}

	void setNumberOfDays(int numberOfDays) {
		this.numberOfDays = numberOfDays;
	}

	void setNumberOfRooms(int numberOfRooms) {
		this.numberOfRooms = numberOfRooms;
	}

	void setPeriodsPerDay(int periodsPerDay) {
		this.periodsPerDay = periodsPerDay;
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
		List<Integer> periodsForCourse = unavailabilityConstraints.get(course);
		if (periodsForCourse == null) {
			periodsForCourse = new ArrayList<Integer>();
		}
		periodsForCourse.add(period);
		unavailabilityConstraints.put(course, periodsForCourse);
	}

	@Override
	public List<ICourse> getCourses() {
		return Collections.unmodifiableList(courses);
	}

	@Override
	public List<ICurriculum> getCurricula() {
		return Collections.unmodifiableList(curricula);
	}

	@Override
	public List<IRoom> getRooms() {
		return Collections.unmodifiableList(rooms);
	}

	@Override
	public Map<ICourse, List<Integer>> getUnavailabilityConstraints() {
		return Collections.unmodifiableMap(unavailabilityConstraints);
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
