package de.hft.timetabling.common;

import java.util.List;
import java.util.Map;

public interface IProblemInstance {

	String getName();

	int getNumberOfConstraints();

	int getNumberOfCourses();

	int getNumberOfCurricula();

	int getNumberOfDays();

	int getNumberOfRooms();

	int getPeriodsPerDay();

	List<ICourse> getCourses();

	List<IRoom> getRooms();

	List<ICurriculum> getCurricula();

	ICourse getCourseById(String courseId);

	Map<ICourse, List<Integer>> getUnavailabilityConstraints();

}
