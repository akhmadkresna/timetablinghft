package de.hft.timetabling.common;

import java.util.Set;

public interface ICourse {

	String getId();

	String getTeacher();

	int getNumberOfLectures();

	int getMinWorkingDays();

	int getNumberOfStudents();

	Set<ICurriculum> getCurricula();

}
