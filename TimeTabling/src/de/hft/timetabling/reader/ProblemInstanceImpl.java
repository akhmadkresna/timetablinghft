package de.hft.timetabling.reader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

	private List<ICourse> courses;

	private List<IRoom> rooms;

	private List<ICurriculum> curricula;

	public ProblemInstanceImpl() {
		courses = new ArrayList<ICourse>();
		rooms = new ArrayList<IRoom>();
		curricula = new ArrayList<ICurriculum>();
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
