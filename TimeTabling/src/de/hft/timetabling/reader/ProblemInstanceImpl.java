package de.hft.timetabling.reader;

import de.hft.timetabling.common.IProblemInstance;

final class ProblemInstanceImpl implements IProblemInstance {

	private String name;

	private int numberOfConstraints;

	private int numberOfCourses;

	private int numberOfCurricula;

	private int numberOfDays;

	private int numberOfRooms;

	private int periodsPerDay;

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

}
