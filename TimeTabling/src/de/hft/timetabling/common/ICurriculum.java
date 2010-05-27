package de.hft.timetabling.common;

import java.util.Set;

public interface ICurriculum {

	String getId();

	int getNumberOfCourses();

	Set<ICourse> getCourses();

	boolean containsCourse(ICourse course);

}
