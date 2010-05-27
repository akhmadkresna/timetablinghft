package de.hft.timetabling.common;

import java.util.Set;

public interface IProblemInstance {

	String getName();

	int getNumberOfConstraints();

	int getNumberOfCourses();

	int getNumberOfCurricula();

	int getNumberOfDays();

	int getNumberOfRooms();

	int getPeriodsPerDay();

	Set<ICourse> getCourses();

	Set<IRoom> getRooms();

	Set<ICurriculum> getCurricula();

	ICourse getCourseById(String courseId);

	Set<Integer> getUnavailabilityConstraints(ICourse course);

}
