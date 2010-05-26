package de.hft.timetabling.common;

public interface IProblemInstance {

	String getName();

	int getNumberOfConstraints();

	int getNumberOfCourses();

	int getNumberOfCurricula();

	int getNumberOfDays();

	int getNumberOfRooms();

	int getPeriodsPerDay();

}
