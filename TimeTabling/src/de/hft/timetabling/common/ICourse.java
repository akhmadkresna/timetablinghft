package de.hft.timetabling.common;

public interface ICourse {

	String getId();

	String getTeacher();

	int getNumberOfLectures();

	int getMinWorkingDays();

	int getNumberOfStudents();

}
