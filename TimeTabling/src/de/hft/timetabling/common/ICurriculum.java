package de.hft.timetabling.common;

import java.util.List;

public interface ICurriculum {

	String getId();

	int getNumberOfCourses();

	List<ICourse> getCourses();

}
